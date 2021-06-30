package switchrefactor.refactoring;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.YieldStatement;

import switchrefactor.search.SwitchSearchRefactoring;

public class SwitchForRefactor {

	String pathName;
	TypeDeclaration td;
	AST ast;
	MethodDeclaration m;
	boolean defaultAddLabel;
	boolean branchAddLabel;
	boolean dealC_DLabel;

	// 数据初始化
	public SwitchForRefactor(String pathName, TypeDeclaration td, AST ast, MethodDeclaration m, boolean label,
			int category, boolean analysisLabel, int checkDefaultLabel, boolean defaultAddLabel, boolean branchAddLabel,
			boolean dealC_DLabel) {
		this.pathName = pathName;
		this.td = td;
		this.ast = ast;
		this.m = m;
		this.defaultAddLabel = defaultAddLabel;
		this.branchAddLabel = branchAddLabel;
		this.dealC_DLabel = dealC_DLabel;
	}

	// 解决switch内部嵌套switch
	private void searchBlock(Block blockTemp) {
		SwitchSearchRefactoring ssr = new SwitchSearchRefactoring(pathName, td, ast, m, defaultAddLabel, branchAddLabel,
				dealC_DLabel);
		ssr.blockSearch(blockTemp);
	}

	// 无法重构case default连用，组装switch语句
	@SuppressWarnings("unchecked")
	public void switchForCDrefactor(AST ast, LinkedHashMap<SwitchCase, Integer> caseMap,
			LinkedHashMap<Integer, Block> blockMap, Expression expression, SwitchStatement s,
			boolean checkDefaultAddLabel) {
		boolean switchCaseRule = false;

		s.setExpression((Expression) ASTNode.copySubtree(ast, expression));
		Iterator<SwitchCase> iter = caseMap.keySet().iterator();
		SwitchCase scTemp = null;

		while (true) {
			Block blockTemp = null;
			if (scTemp == null) {
				scTemp = iter.next();
			}
			int labelTemp = caseMap.get(scTemp);
			blockTemp = blockMap.get(labelTemp);

			SwitchCase scTempTemp = ast.newSwitchCase();
			List<Expression> list = scTempTemp.expressions();
			list.addAll(ASTNode.copySubtrees(scTempTemp.getAST(), scTemp.expressions()));
			scTempTemp.setSwitchLabeledRule(switchCaseRule);
			scTemp.delete();
			s.statements().add(scTempTemp);

			if (iter.hasNext()) {
				scTemp = iter.next();
			} else {
				searchBlock(blockTemp);
				blockTemp.delete();
				s.statements().add(blockTemp);
				break;
			}
			if (labelTemp == caseMap.get(scTemp)) {
				continue;
			}
			/*
			 * 结点复制后，会丧失其保持的预览特性，自动初始化条件连接符为：
			 */
			searchBlock(blockTemp);
			blockTemp.delete();
			s.statements().add(blockTemp);
		}
	}

	// 无法重构，组装switch语句
	@SuppressWarnings("unchecked")
	public void switchForUnrefactor(AST ast, LinkedHashMap<SwitchCase, Integer> caseMap,
			LinkedHashMap<Integer, Block> blockMap, Expression expression, SwitchStatement s,
			boolean checkDefaultAddLabel) {
		boolean switchCaseRule = true;

		s.setExpression((Expression) ASTNode.copySubtree(ast, expression));

		Iterator<SwitchCase> iter = caseMap.keySet().iterator();
		while (iter.hasNext()) {
			Block blockTemp = null;
			SwitchCase scTemp = iter.next();
			switchCaseRule = scTemp.isSwitchLabeledRule();
			int labelTemp = caseMap.get(scTemp);
			blockTemp = blockMap.get(labelTemp);
			SwitchCase scTempTemp = ast.newSwitchCase();
			List<Expression> list = scTempTemp.expressions();
			list.addAll(ASTNode.copySubtrees(scTempTemp.getAST(), scTemp.expressions()));
			scTempTemp.setSwitchLabeledRule(switchCaseRule);
			scTemp.delete();
			s.statements().add(scTempTemp);

			/*
			 * 结点复制后，会丧失其保持的预览特性，自动初始化条件连接符为：
			 */
			searchBlock(blockTemp);
			blockTemp.delete();
			s.statements().add(blockTemp);

			iter.remove();
		}
		if (checkDefaultAddLabel) {
			SwitchForYield.defaultAdd(ast, expression, s, switchCaseRule);
		}
	}

	// 方法类重构检查，并重构
	@SuppressWarnings("unchecked")
	public void switchForMethodrefactor(AST ast, LinkedHashMap<SwitchCase, Integer> caseMap,
			LinkedHashMap<Integer, Block> blockMap, Expression expression, SwitchStatement s, MethodInvocation me,
			boolean checkDefaultAddLabel) {
		int label = 1;
		int num = 0;
		boolean flag = true;
		boolean flagE = false;
		boolean flagY = false;
		boolean flagI = false;
		boolean flagIntent = true;
		boolean switchCaseRule = true;
		MethodInvocation meLabel = null;
		MethodInvocation meTemp = null;
		Expression eTemp = null;
//		AST astTemp = AST.newAST(14, true);

		while (blockMap.containsKey(label)) {
			Block block = blockMap.get(label);
			if (block.statements().size() >= 1) {
				num = block.statements().size() - 1;
				if (block.statements().get(num) instanceof ThrowStatement) {
					label++;
					continue;
				}
				if (block.statements().get(num) instanceof YieldStatement) {
					YieldStatement ys = (YieldStatement) block.statements().get(num);
					Expression expre = ys.getExpression();
					if (expre instanceof MethodInvocation) {
						if (meLabel == null) {
							meLabel = (MethodInvocation) expre;
							if (meLabel.arguments().size() > 1 || meLabel.arguments().size() == 0) {
								SwitchRefactoring.llllllabel = true; 
								flag = false;
								break;
							}
						} else {
							meTemp = (MethodInvocation) expre;
							if (meTemp.arguments().size() > 1 || meTemp.arguments().size() == 0) {
								SwitchRefactoring.llllllabel = true; 
								flag = false;
								break;
							}
							if (!(meTemp.getName().toString().equals(meLabel.getName().toString()))) {
								flag = false;
								flagIntent = false;
								break;
							}
							if ((meTemp.getExpression() == null && meLabel.getExpression() == null)
									|| (meTemp.getExpression() != null && meLabel.getExpression() != null)) {
								if (meTemp.getExpression() != null) {
									if (!(meTemp.getExpression().toString()
											.equals(meLabel.getExpression().toString()))) {
										flag = false;
										flagIntent = false;
										break;
									}
								}
							} else {
								flag = false;
								flagIntent = false;
								break;
							}
						}
					} else {
						flag = false;
						flagIntent = false;
						break;
					}
				} else if (block.statements().get(num) instanceof ExpressionStatement) {
					ExpressionStatement es = (ExpressionStatement) block.statements().get(num);
					Expression expre = es.getExpression();
					if (expre instanceof MethodInvocation) {
						if (meLabel == null) {
							meLabel = (MethodInvocation) expre;
							if (meLabel.arguments().size() > 1 || meLabel.arguments().size() == 0) {
								SwitchRefactoring.llllllabel = true; 
								flag = false;
								break;
							}
						} else {
							meTemp = (MethodInvocation) expre;
							if (meTemp.arguments().size() > 1 || meTemp.arguments().size() == 0) {
								SwitchRefactoring.llllllabel = true; 
								flag = false;
								break;
							}
							if (!(meTemp.getName().toString().equals(meLabel.getName().toString()))) {
								flag = false;
								flagIntent = false;
								break;
							}
							if ((meTemp.getExpression() == null && meLabel.getExpression() == null)
									|| (meTemp.getExpression() != null && meLabel.getExpression() != null)) {
								if (meTemp.getExpression() != null) {
									if (!(meTemp.getExpression().toString()
											.equals(meLabel.getExpression().toString()))) {
										flag = false;
										flagIntent = false;
										break;
									}
								}
							} else {
								flag = false;
								flagIntent = false;
								break;
							}
						}
					} else {
						flag = false;
						flagIntent = false;
						break;
					}
				} else if (block.statements().get(num) instanceof IfStatement) {
					Statement state = SwitchForEndIf.getAnalyEndIf((IfStatement) block.statements().get(num));
					Expression expre = null;
					if (state == null) {
						flag = false;
						flagIntent = false;
						break;
					} else if (state instanceof ExpressionStatement) {
						ExpressionStatement es = (ExpressionStatement) state;
						expre = es.getExpression();
					} else if (state instanceof YieldStatement) {
						YieldStatement ys = (YieldStatement) state;
						expre = ys.getExpression();
					} else {
						flag = false;
						flagIntent = false;
						break;
					}

					if (expre instanceof MethodInvocation) {
						if (meLabel == null) {
							meLabel = (MethodInvocation) expre;
							if (meLabel.arguments().size() > 1 || meLabel.arguments().size() == 0) {
								SwitchRefactoring.llllllabel = true; 
								flag = false;
								break;
							}
						} else {
							meTemp = (MethodInvocation) expre;
							if (meTemp.arguments().size() > 1 || meTemp.arguments().size() == 0) {
								SwitchRefactoring.llllllabel = true; 
								flag = false;
								break;
							}
							if (!(meTemp.getName().toString().equals(meLabel.getName().toString()))) {
								flag = false;
								flagIntent = false;
								break;
							}
							if ((meTemp.getExpression() == null && meLabel.getExpression() == null)
									|| (meTemp.getExpression() != null && meLabel.getExpression() != null)) {
								if (meTemp.getExpression() != null) {
									if (!(meTemp.getExpression().toString()
											.equals(meLabel.getExpression().toString()))) {
										flag = false;
										flagIntent = false;
										break;
									}
								}
							} else {
								flag = false;
								flagIntent = false;
								break;
							}
						}
					} else {
						flag = false;
						flagIntent = false;
						break;
					}

				} else {
					flag = false;
					flagIntent = false;
					break;
				}
			}
			label++;
		}
		
		if (flag) {
			boolean checkMidIFLabel = SwitchForEndIf.checkMidIf(meLabel, blockMap);
			if (!checkMidIFLabel) {
				flag = false;
				flagIntent = false;
			}
		}

		if(!flag && !flagIntent) {
			SwitchRefactoring.sumValueExpression ++;
		}
		
		if (flag) {
			me.setName((SimpleName) ASTNode.copySubtree(ast, meLabel.getName()));
			if (meLabel.getExpression() != null) {
				me.setExpression((Expression) ASTNode.copySubtree(ast, meLabel.getExpression()));
			}
			SwitchExpression sExpression = ast.newSwitchExpression();
			sExpression.setExpression((Expression) ASTNode.copySubtree(ast, expression));
			Iterator<SwitchCase> iter = caseMap.keySet().iterator();
			while (iter.hasNext()) {
				Block blockTemp = null;
				SwitchCase scTemp = iter.next();
				switchCaseRule = scTemp.isSwitchLabeledRule();
				int labelTemp = caseMap.get(scTemp);
				if (blockMap.containsKey(labelTemp)) {
					blockTemp = blockMap.get(labelTemp);
					if (blockTemp.statements().get(blockTemp.statements().size() - 1) instanceof IfStatement) {
						flagI = true;
					} else if (blockTemp.statements().size() == 1) {
						if (!(blockTemp.statements().get(0) instanceof ThrowStatement)) {
							flagE = true;
							MethodInvocation mInvocation;
							if (blockTemp.statements().get(0) instanceof YieldStatement) {
								YieldStatement yStatement = (YieldStatement) blockTemp.statements().get(0);
								mInvocation = (MethodInvocation) yStatement.getExpression();
								eTemp = (Expression) mInvocation.arguments().get(0);
							}
							if (blockTemp.statements().get(0) instanceof ExpressionStatement) {
								ExpressionStatement eStatement = (ExpressionStatement) blockTemp.statements().get(0);
								mInvocation = (MethodInvocation) eStatement.getExpression();
								eTemp = (Expression) mInvocation.arguments().get(0);
							}
						}
					} else {
						num = blockTemp.statements().size() - 1;
						if (!(blockTemp.statements().get(num) instanceof ThrowStatement)) {
							flagY = true;
							MethodInvocation mInvocation;
							if (blockTemp.statements().get(num) instanceof YieldStatement) {
								YieldStatement yStatement = (YieldStatement) blockTemp.statements().get(num);
								mInvocation = (MethodInvocation) yStatement.getExpression();
								eTemp = (Expression) mInvocation.arguments().get(0);
							}
							if (blockTemp.statements().get(num) instanceof ExpressionStatement) {
								ExpressionStatement eStatement = (ExpressionStatement) blockTemp.statements().get(num);
								mInvocation = (MethodInvocation) eStatement.getExpression();
								eTemp = (Expression) mInvocation.arguments().get(0);
							}
						}
					}
				}

				SwitchCase switchCase = ast.newSwitchCase();
				List<Expression> list = switchCase.expressions();
				list.addAll(ASTNode.copySubtrees(switchCase.getAST(), scTemp.expressions()));
				switchCase.setSwitchLabeledRule(true);
				sExpression.statements().add(switchCase);
				scTemp.delete();

				if (flagE) {
					eTemp.delete();
					Expression expressionTemp = eTemp;
					ExpressionStatement expressionStatement = ast.newExpressionStatement(expressionTemp);
					sExpression.statements().add(expressionStatement);
					flagE = false;
				} else if (flagY) {
					eTemp.delete();
					Expression expressionTemp = eTemp;
					YieldStatement yieldStatement = ast.newYieldStatement();
					yieldStatement.setExpression(expressionTemp);
					blockTemp.statements().remove(blockTemp.statements().size() - 1);
					searchBlock(blockTemp);
					blockTemp.delete();
					blockTemp.statements().add(yieldStatement);
					sExpression.statements().add(blockTemp);

					flagY = false;
				} else if (flagI) {
					if (blockTemp.statements().size() == 1) {
						IfStatement ifTemp = (IfStatement) blockTemp.statements().get(0);
						SwitchForEndIf.refactorEndIfM(ifTemp);
						blockTemp.delete();
						sExpression.statements().add(blockTemp);
						flagI = false;
					} else {
						IfStatement ifTemp = (IfStatement) blockTemp.statements()
								.get(blockTemp.statements().size() - 1);
						SwitchForEndIf.refactorEndIfM(ifTemp);
						searchBlock(blockTemp);
						blockTemp.delete();
						sExpression.statements().add(blockTemp);
						flagI = false;
					}
				} else {
					searchBlock(blockTemp);
					blockTemp.delete();
					sExpression.statements().add(blockTemp);
				}
			}

			if (checkDefaultAddLabel) {
				SwitchForYield.defaultAdd(ast, expression, sExpression, switchCaseRule);
			}
			me.arguments().add(sExpression);
		} else {
			s.setExpression((Expression) ASTNode.copySubtree(ast, expression));
			Iterator<SwitchCase> iter = caseMap.keySet().iterator();
			while (iter.hasNext()) {
				Block blockTemp = null;
				SwitchCase scTemp = iter.next();
				switchCaseRule = scTemp.isSwitchLabeledRule();
				int labelTemp = caseMap.get(scTemp);
				if (blockMap.containsKey(labelTemp)) {
					blockTemp = blockMap.get(labelTemp);
				}

				SwitchCase switchCase = ast.newSwitchCase();
				List<Expression> list = switchCase.expressions();
				list.addAll(ASTNode.copySubtrees(switchCase.getAST(), scTemp.expressions()));
				switchCase.setSwitchLabeledRule(true);
				s.statements().add(switchCase);
				scTemp.delete();

				searchBlock(blockTemp);
				blockTemp.delete();
				s.statements().add(blockTemp);

			}
			if (checkDefaultAddLabel) {
				SwitchForYield.defaultAdd(ast, expression, s, switchCaseRule);
			}
		}
	}

	// 等式类重构检查，并重构
	@SuppressWarnings("unchecked")
	public void switchForAssignrefactor(AST ast, LinkedHashMap<SwitchCase, Integer> caseMap,
			LinkedHashMap<Integer, Block> blockMap, Expression expression, SwitchStatement s, Assignment a,
			boolean checkDefaultAddLabel) {
		int label = 1;
		int num = 0;
		boolean flag = true;
		boolean flagE = false;
		boolean flagY = false;
		boolean flagI = false;
		boolean switchCaseRule = true;
		Assignment asLabel = null;
		Assignment asTemp = null;
		Expression eTemp = null;
//		AST astTemp = AST.newAST(14, true);

		while (blockMap.containsKey(label)) {
			Block block = blockMap.get(label);
			if (block.statements().size() >= 1) {
				num = block.statements().size() - 1;
				if (block.statements().get(num) instanceof ThrowStatement) {
					label++;
					continue;
				}
				if (block.statements().get(num) instanceof YieldStatement) {
					YieldStatement ys = (YieldStatement) block.statements().get(num);
					Expression expre = ys.getExpression();
					if (expre instanceof Assignment) {
						if (asLabel == null) {
							asLabel = (Assignment) expre;
							if (asLabel.getOperator() != Assignment.Operator.ASSIGN) {
								flag = false;
								break;
							}
						} else {
							asTemp = (Assignment) expre;
							if (asTemp.getOperator() != Assignment.Operator.ASSIGN) {
								flag = false;
								break;
							}
							if (!(asTemp.getLeftHandSide().toString().equals(asLabel.getLeftHandSide().toString()))) {
								flag = false;
								break;
							}
						}
					} else {
						flag = false;
						break;
					}
				} else if (block.statements().get(num) instanceof ExpressionStatement) {
					ExpressionStatement es = (ExpressionStatement) block.statements().get(num);
					Expression expre = es.getExpression();
					if (expre instanceof Assignment) {
						if (asLabel == null) {
							asLabel = (Assignment) expre;
							if (asLabel.getOperator() != Assignment.Operator.ASSIGN) {
								flag = false;
								break;
							}
						} else {
							asTemp = (Assignment) expre;
							if (asTemp.getOperator() != Assignment.Operator.ASSIGN) {
								flag = false;
								break;
							}
							if (!(asTemp.getLeftHandSide().toString().equals(asLabel.getLeftHandSide().toString()))) {
								flag = false;
								break;
							}
						}
					} else {
						flag = false;
						break;
					}
				} else if (block.statements().get(num) instanceof IfStatement) {
					Statement state = SwitchForEndIf.getAnalyEndIf((IfStatement) block.statements().get(num));
					Expression expre = null;
					if (state == null) {
						flag = false;
						break;
					} else if (state instanceof ExpressionStatement) {
						ExpressionStatement es = (ExpressionStatement) state;
						expre = es.getExpression();
					} else if (state instanceof YieldStatement) {
						YieldStatement ys = (YieldStatement) state;
						expre = ys.getExpression();
					} else {
						flag = false;
						break;
					}

					if (expre instanceof Assignment) {
						if (asLabel == null) {
							asLabel = (Assignment) expre;
							if (asLabel.getOperator() != Assignment.Operator.ASSIGN) {
								flag = false;
								break;
							}
						} else {
							asTemp = (Assignment) expre;
							if (asTemp.getOperator() != Assignment.Operator.ASSIGN) {
								flag = false;
								break;
							}
							if (!(asTemp.getLeftHandSide().toString().equals(asLabel.getLeftHandSide().toString()))) {
								flag = false;
								break;
							}
						}
					} else {
						flag = false;
						break;
					}
				} else {
					flag = false;
					break;
				}
			}
			label++;
		}

		if (flag) {
			boolean checkMidIFLabel = SwitchForEndIf.checkMidIf(asLabel, blockMap);
			if (!checkMidIFLabel) {
				flag = false;
			}
		}

		if (flag) {
			a.setLeftHandSide((Expression) ASTNode.copySubtree(ast, asLabel.getLeftHandSide()));
			a.setOperator(Assignment.Operator.ASSIGN);
			SwitchExpression sExpression = ast.newSwitchExpression();
			sExpression.setExpression((Expression) ASTNode.copySubtree(ast, expression));
			Iterator<SwitchCase> iter = caseMap.keySet().iterator();

			while (iter.hasNext()) {
				Block blockTemp = null;
				SwitchCase scTemp = iter.next();
				switchCaseRule = scTemp.isSwitchLabeledRule();
				int labelTemp = caseMap.get(scTemp);
				if (blockMap.containsKey(labelTemp)) {
					blockTemp = blockMap.get(labelTemp);
					if (blockTemp.statements().get(blockTemp.statements().size() - 1) instanceof IfStatement) {
						flagI = true;
					} else if (blockTemp.statements().size() == 1) {
						if (!(blockTemp.statements().get(0) instanceof ThrowStatement)) {
							flagE = true;
							Assignment assignment;
							if (blockTemp.statements().get(0) instanceof YieldStatement) {
								YieldStatement yStatement = (YieldStatement) blockTemp.statements().get(0);
								assignment = (Assignment) yStatement.getExpression();
								eTemp = assignment.getRightHandSide();
							}
							if (blockTemp.statements().get(0) instanceof ExpressionStatement) {
								ExpressionStatement eStatement = (ExpressionStatement) blockTemp.statements().get(0);
								assignment = (Assignment) eStatement.getExpression();
								eTemp = assignment.getRightHandSide();
							}
						}
					} else {
						num = blockTemp.statements().size() - 1;
						if (!(blockTemp.statements().get(num) instanceof ThrowStatement)) {
							flagY = true;
							Assignment assignment = null;
							if (blockTemp.statements().get(num) instanceof YieldStatement) {
								YieldStatement yStatement = (YieldStatement) blockTemp.statements().get(num);
								assignment = (Assignment) yStatement.getExpression();
								eTemp = assignment.getRightHandSide();
							}
							if (blockTemp.statements().get(num) instanceof ExpressionStatement) {
								ExpressionStatement eStatement = (ExpressionStatement) blockTemp.statements().get(num);
								assignment = (Assignment) eStatement.getExpression();
								eTemp = assignment.getRightHandSide();
							}
						}
					}
				}

				SwitchCase switchCase = ast.newSwitchCase();
				List<Expression> list = switchCase.expressions();
				list.addAll(ASTNode.copySubtrees(switchCase.getAST(), scTemp.expressions()));
				switchCase.setSwitchLabeledRule(true);
				sExpression.statements().add(switchCase);
				scTemp.delete();

				if (flagE) {
					Expression expressionTemp = (Expression) ASTNode.copySubtree(ast, eTemp);
					ExpressionStatement expressionStatement = ast.newExpressionStatement(expressionTemp);
					sExpression.statements().add(expressionStatement);
					flagE = false;
				} else if (flagY) {
					YieldStatement yieldStatement = ast.newYieldStatement();
					yieldStatement.setExpression((Expression) ASTNode.copySubtree(ast, eTemp));

					blockTemp.statements().remove(blockTemp.statements().size() - 1);
					searchBlock(blockTemp);
					blockTemp.delete();
					blockTemp.statements().add(yieldStatement);
					sExpression.statements().add(blockTemp);

					flagY = false;
				} else if (flagI) {
					if (blockTemp.statements().size() == 1) {
						IfStatement ifTemp = (IfStatement) blockTemp.statements().get(0);
						SwitchForEndIf.refactorEndIfA(ifTemp);
						blockTemp.delete();
						sExpression.statements().add(blockTemp);
						flagI = false;
					} else {
						IfStatement ifTemp = (IfStatement) blockTemp.statements()
								.get(blockTemp.statements().size() - 1);
						SwitchForEndIf.refactorEndIfA(ifTemp);
						searchBlock(blockTemp);
						blockTemp.delete();
						sExpression.statements().add(blockTemp);
						flagI = false;
					}
				} else {
					searchBlock(blockTemp);
					blockTemp.delete();
					sExpression.statements().add(blockTemp);
				}
			}
			if (checkDefaultAddLabel) {
				SwitchForYield.defaultAdd(ast, expression, sExpression, switchCaseRule);
			}
			a.setRightHandSide(sExpression);
		} else {
			s.setExpression((Expression) ASTNode.copySubtree(ast, expression));
			Iterator<SwitchCase> iter = caseMap.keySet().iterator();
			while (iter.hasNext()) {
				Block blockTemp = null;
				SwitchCase scTemp = iter.next();
				switchCaseRule = scTemp.isSwitchLabeledRule();
				int labelTemp = caseMap.get(scTemp);
				if (blockMap.containsKey(labelTemp)) {
					blockTemp = blockMap.get(labelTemp);
				}

				SwitchCase switchCase = ast.newSwitchCase();
				List<Expression> list = switchCase.expressions();
				list.addAll(ASTNode.copySubtrees(switchCase.getAST(), scTemp.expressions()));
				switchCase.setSwitchLabeledRule(true);
				s.statements().add(switchCase);
				scTemp.delete();

				searchBlock(blockTemp);
				blockTemp.delete();
				s.statements().add(blockTemp);
			}
			if (checkDefaultAddLabel) {
				SwitchForYield.defaultAdd(ast, expression, s, switchCaseRule);
			}
		}
	}

	// return类重构检查，并重构
	@SuppressWarnings("unchecked")
	public void switchForReturnrefactor(AST ast, LinkedHashMap<SwitchCase, Integer> caseMap,
			LinkedHashMap<Integer, Block> blockMap, Expression expression, ReturnStatement r,
			boolean checkDefaultAddLabel) {
		int num = 0;
		boolean flagE = false;
		boolean flagY = false;
		boolean flagI = false;
		boolean switchCaseRule = true;
		Expression eTemp = null;
//		AST astTemp = AST.newAST(14, true);

		SwitchExpression sExpression = ast.newSwitchExpression();
		sExpression.setExpression((Expression) ASTNode.copySubtree(ast, expression));
		Iterator<SwitchCase> iter = caseMap.keySet().iterator();
		while (iter.hasNext()) {
			eTemp = null;
			Block blockTemp = null;
			SwitchCase scTemp = iter.next();
			switchCaseRule = scTemp.isSwitchLabeledRule();
			int labelTemp = caseMap.get(scTemp);
			if (blockMap.containsKey(labelTemp)) {
				blockTemp = blockMap.get(labelTemp);
				if (blockTemp.statements().get(blockTemp.statements().size() - 1) instanceof IfStatement) {
					flagI = true;
				} else if (blockTemp.statements().size() == 1) {
					if (!(blockTemp.statements().get(0) instanceof ThrowStatement)) {
						flagE = true;
						ReturnStatement rsTemp = (ReturnStatement) blockTemp.statements().get(0);
						eTemp = rsTemp.getExpression();
					}
				} else {
					num = blockTemp.statements().size() - 1;
					if (!(blockTemp.statements().get(num) instanceof ThrowStatement)) {
						flagY = true;
						ReturnStatement rsTemp = (ReturnStatement) blockTemp.statements().get(num);
						eTemp = rsTemp.getExpression();
					}
				}
			}

			SwitchCase switchCase = ast.newSwitchCase();
			List<Expression> list = switchCase.expressions();
			list.addAll(ASTNode.copySubtrees(switchCase.getAST(), scTemp.expressions()));
			switchCase.setSwitchLabeledRule(true);
			sExpression.statements().add(switchCase);
			scTemp.delete();

			if (eTemp != null) {
				eTemp.delete();
			}

			if (flagE) {
				try {
					ExpressionStatement expressionStatement = ast.newExpressionStatement(eTemp);
					sExpression.statements().add(expressionStatement);
					flagE = false;
				} catch (IllegalArgumentException e) {
					sExpression.statements().add(ASTNode.copySubtree(ast, blockTemp));
					flagE = false;
				}
			} else if (flagY) {
				YieldStatement yieldStatement = ast.newYieldStatement();
				yieldStatement.setExpression(eTemp);

				blockTemp.statements().remove(blockTemp.statements().size() - 1);
				searchBlock(blockTemp);
				blockTemp.delete();
				blockTemp.statements().add(yieldStatement);
				sExpression.statements().add(blockTemp);

				flagY = false;
			} else if (flagI) {
				if (blockTemp.statements().size() == 1) {
					IfStatement ifTemp = (IfStatement) blockTemp.statements().get(0);
					SwitchForEndIf.refactorEndIfR(ifTemp);
					blockTemp.delete();
					sExpression.statements().add(blockTemp);
					flagI = false;
				} else {
					IfStatement ifTemp = (IfStatement) blockTemp.statements().get(blockTemp.statements().size() - 1);
					SwitchForEndIf.refactorEndIfR(ifTemp);
					searchBlock(blockTemp);
					blockTemp.delete();
					sExpression.statements().add(blockTemp);
					flagI = false;
				}
			} else {
				searchBlock(blockTemp);
				blockTemp.delete();
				sExpression.statements().add(blockTemp);
			}
		}
		if (checkDefaultAddLabel) {
			SwitchForYield.defaultAdd(ast, expression, sExpression, switchCaseRule);
		}

		r.setExpression(sExpression);
	}
}
