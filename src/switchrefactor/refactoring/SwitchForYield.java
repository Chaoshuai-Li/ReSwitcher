package switchrefactor.refactoring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.YieldStatement;

public class SwitchForYield {

	// 可能重构的switch表达式类型，初判断
	public static int yieldDetermine(LinkedHashMap<Integer, Block> blockMap, boolean[] flagArray) {
		int label = 1;
		int num = 0;
		int numMethod = 0;
		int numAssign = 0;
		int numReturn = 0;
		int numThrow = 0;
		while (true) {
			if (blockMap.containsKey(label)) {
				Block block = blockMap.get(label);
				if (block.statements().size() > 0) {
					num = block.statements().size() - 1;

					if (block.statements().get(num) instanceof Block) {
						block = (Block) block.statements().get(num);
						num = block.statements().size() - 1;
					}
					if (block.statements().get(num) instanceof ThrowStatement) {
						numAssign++;
						numMethod++;
						numReturn++;
						numThrow++;
					} else if (block.statements().get(num) instanceof ReturnStatement) {
						ReturnStatement rs = (ReturnStatement) block.statements().get(num);
						if (rs.getExpression() != null) {
							numReturn++;
						}
					} else if (block.statements().get(num) instanceof ExpressionStatement) {
						ExpressionStatement esTemp = (ExpressionStatement) block.statements().get(num);
						Expression expre = esTemp.getExpression();
						if (expre instanceof Assignment) {
							numAssign++;
						}
						if (expre instanceof MethodInvocation) {
							numMethod++;
						}
					} else if (block.statements().get(num) instanceof YieldStatement) {
						YieldStatement ysTemp = (YieldStatement) block.statements().get(num);
						Expression expre = ysTemp.getExpression();
						if (expre instanceof Assignment) {
							numAssign++;
						}
						if (expre instanceof MethodInvocation) {
							numMethod++;
						}
					} else if (block.statements().get(num) instanceof IfStatement) {
						boolean flag = SwitchForEndIf.checkEndIf((IfStatement) block.statements().get(num), false);
						if (!flag) {
							return 0;
						} else {
							int temp = SwitchForEndIf.analyEndIf((IfStatement) block.statements().get(num));

							if (temp == 0) {
								numMethod++;
							} else if (temp == 1) {
								numAssign++;
							} else if (temp == 2) {
								numReturn++;
							} else {
								return 0;
							}
						}
					} else if (block.statements().get(num) instanceof SwitchStatement && !flagArray[0]) {
						flagArray[1] = true;
					}
				}
				label++;
			} else {
				label--;
				break;
			}
		}

//		 System.out.println("Method 数目：" + numMethod);
//		 System.out.println("Assign 数目：" + numAssign);
//		 System.out.println("Throw 数目：" + numThrow);
//		 System.out.println("Return 数目：" + numReturn);
//		 System.out.println("label 数目：" + label);

		if (numMethod == label && numThrow != numMethod) {
			return 1;
		} else if (numAssign == label && numThrow != numAssign) {
			return 2;
		} else if (numReturn == label && numThrow != numReturn) {
			boolean midFlag = midSwitchReturn(blockMap);
			if (!midFlag) {
				return 3;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	private static boolean midSwitchReturn(LinkedHashMap<Integer, Block> blockMap) {
		int label = 1;
		while (blockMap.containsKey(label)) {
			Block blockTemp = blockMap.get(label);
			int index = blockTemp.statements().size() - 1;
			for (int i = 0; i < index; i++) {
				ArrayList<SwitchStatement> listTemp = new ArrayList<>();
				Statement sTemp = (Statement) blockTemp.statements().get(i);
				findSwitchs(sTemp, listTemp);
				if (listTemp.size() != 0) {
					ArrayList<ReturnStatement> listReturn = new ArrayList<ReturnStatement>();
					for (SwitchStatement s : listTemp) {
						findReturn(s, listReturn);
					}
					if (listReturn.size() != 0) {
						return true;
					}
				}
				listTemp.clear();
			}
			label++;
		}
		return false;
	}

	private static void findSwitchs(ASTNode root, List<SwitchStatement> switchs) {
		root.accept(new ASTVisitor() {
			public boolean visit(SwitchStatement node) {
				switchs.add(node);
				return true;
			}
		});
	}

	private static void findReturn(ASTNode root, List<ReturnStatement> returns) {
		root.accept(new ASTVisitor() {
			public boolean visit(ReturnStatement node) {
				returns.add(node);
				return false;
			}
		});
	}

	// default缺失检查并记录
	public static int checkDefault(AST ast, LinkedHashMap<SwitchCase, Integer> caseMap,
			LinkedHashMap<Integer, Block> blockMap, boolean defaultAddLabel, Expression e) {
		int label = 1;
		Iterator<SwitchCase> iter = caseMap.keySet().iterator();
		SwitchCase switchCaseTemp;
//		 boolean switchLabelRule = true;

		while (iter.hasNext()) {
			switchCaseTemp = iter.next();
//			 switchLabelRule = switchCaseTemp.isSwitchLabeledRule();
//			 if (!switchCaseTemp.isSwitchLabeledRule()) {
//				return false;
//			}
			if (switchCaseTemp.isDefault()) {
				return 1;
			}
			label = caseMap.get(switchCaseTemp) > label ? caseMap.get(switchCaseTemp) : label;
		}

		SwitchRefactoring.DEFAULT = true;

		if (defaultAddLabel) {
			return 2;
		} else {
			return 3;
		}
	}

	// default缺失检查并记录
	public static void checkDefault(AST ast, LinkedHashMap<SwitchCase, Integer> caseMap,
			LinkedHashMap<Integer, Block> blockMap) {
		int label = 1;
		Iterator<SwitchCase> iter = caseMap.keySet().iterator();
		SwitchCase switchCaseTemp;

		while (iter.hasNext()) {
			switchCaseTemp = iter.next();
			if (switchCaseTemp.isDefault()) {
				return;
			}
			label = caseMap.get(switchCaseTemp) > label ? caseMap.get(switchCaseTemp) : label;
		}
		
		SwitchRefactoring.DEFAULT = true;
	}

	// default添加，结合图形界面选择结果
	@SuppressWarnings("unchecked")
	public static void defaultAddSpec(AST ast, Expression e, SwitchStatement s, boolean switchCaseRule) {
		SwitchRefactoring.sumDefault++;
		
		s.setExpression((Expression) ASTNode.copySubtree(ast, e));
		
		SwitchCase sc = ast.newSwitchCase();
		sc.setSwitchLabeledRule(switchCaseRule);
		s.statements().add(sc);

		ThrowStatement ts = ast.newThrowStatement();
		ClassInstanceCreation cic = ast.newClassInstanceCreation();
		Type type = ast.newSimpleType(ast.newName("IllegalArgumentException"));
		cic.setType(type);
		InfixExpression ife = ast.newInfixExpression();
		StringLiteral sDoneLeft = ast.newStringLiteral();
		sDoneLeft.setEscapedValue("\"Unexpected value \"");
		ife.setLeftOperand(sDoneLeft);
		ife.setOperator(InfixExpression.Operator.PLUS);
		ife.setRightOperand((Expression) ASTNode.copySubtree(ife.getAST(), e));
		cic.arguments().add(ife);
		ts.setExpression(cic);
		Block block = ast.newBlock();
		block.statements().add(ts);
		s.statements().add(block);
	}

	@SuppressWarnings("unchecked")
	public static void defaultAdd(AST ast, Expression e, SwitchStatement s, boolean switchCaseRule) {
		SwitchRefactoring.sumDefault++;
		
		SwitchCase sc = ast.newSwitchCase();
		sc.setSwitchLabeledRule(switchCaseRule);
		s.statements().add(sc);

		ThrowStatement ts = ast.newThrowStatement();
		ClassInstanceCreation cic = ast.newClassInstanceCreation();
		Type type = ast.newSimpleType(ast.newName("IllegalArgumentException"));
		cic.setType(type);
		InfixExpression ife = ast.newInfixExpression();
		StringLiteral sDoneLeft = ast.newStringLiteral();
		sDoneLeft.setEscapedValue("\"Unexpected value \"");
		ife.setLeftOperand(sDoneLeft);
		ife.setOperator(InfixExpression.Operator.PLUS);
		ife.setRightOperand((Expression) ASTNode.copySubtree(ife.getAST(), e));
		cic.arguments().add(ife);
		ts.setExpression(cic);
		Block block = ast.newBlock();
		block.statements().add(ts);
		s.statements().add(block);
	}

	@SuppressWarnings("unchecked")
	public static void defaultAdd(AST ast, Expression e, SwitchExpression s, boolean switchCaseRule) {
		SwitchRefactoring.sumDefault++;
		SwitchCase sc = ast.newSwitchCase();
		sc.setSwitchLabeledRule(switchCaseRule);
		s.statements().add(sc);

		ThrowStatement ts = ast.newThrowStatement();
		ClassInstanceCreation cic = ast.newClassInstanceCreation();
		Type type = ast.newSimpleType(ast.newName("IllegalArgumentException"));
		cic.setType(type);
		InfixExpression ife = ast.newInfixExpression();
		StringLiteral sDoneLeft = ast.newStringLiteral();
		sDoneLeft.setEscapedValue("\"Unexpected value :\"");
		ife.setLeftOperand(sDoneLeft);
		ife.setOperator(InfixExpression.Operator.PLUS);
		ife.setRightOperand((Expression) ASTNode.copySubtree(ife.getAST(), e));
		cic.arguments().add(ife);
		ts.setExpression(cic);
		Block block = ast.newBlock();
		block.statements().add(ts);
		s.statements().add(block);
	}

	// branch条件分支后执行语句缺失检查并记录
	@SuppressWarnings("unchecked")
	public static boolean checkBranch(AST ast, LinkedHashMap<SwitchCase, Integer> caseMap,
			LinkedHashMap<Integer, Block> blockMap, boolean caseAddLabel, Expression e, boolean breakLabel) {
//		 AST astTemp = AST.newAST(14, true);
		int label = 1;
		Block blockTemp;
		Iterator<SwitchCase> iter = caseMap.keySet().iterator();
		SwitchCase switchCaseTemp;
		while (iter.hasNext()) {
			switchCaseTemp = iter.next();
			label = caseMap.get(switchCaseTemp);
			if (blockMap.containsKey(label)) {
				continue;
			} else {

				if (breakLabel) {
					SwitchRefactoring.BRANCH = true;
				}

				if (caseAddLabel && breakLabel) {
					SwitchRefactoring.sumBranch++;
					ThrowStatement ts = ast.newThrowStatement();
					// MalformedTreeException: No target edit provided.为AST操作
					ClassInstanceCreation cic = ast.newClassInstanceCreation();
					Type type = ast.newSimpleType(ast.newName("IllegalArgumentException"));
					cic.setType(type);
					InfixExpression ife = ast.newInfixExpression();
					StringLiteral sDoneLeft = ast.newStringLiteral();
					sDoneLeft.setEscapedValue("\"Unexpected value :\"");
					ife.setLeftOperand(sDoneLeft);
					ife.setOperator(InfixExpression.Operator.PLUS);
					ife.setRightOperand((Expression) ASTNode.copySubtree(ast, e));
//					StringLiteral sDoneRight = ast.newStringLiteral();
//				    sDoneRight.setEscapedValue("\"" + expression.toString() +"\"");
//					ife.setRightOperand(sDoneRight);
					cic.arguments().add(ife);
					ts.setExpression(cic);
					blockTemp = ast.newBlock();
					blockTemp.statements().add(ts);
					blockMap.put(label, blockTemp);
//					blockTemp = (Block) ASTNode.copySubtree(ast, block);
					return true;
				} else {
					blockTemp = ast.newBlock();
					blockMap.put(label, blockTemp);
					return false;
				}
			}
		}
		return true;
	}
}
