package switchrefactor.search;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.YieldStatement;

import switchrefactor.datalog.AddressRecord;
import switchrefactor.datalog.DataLogList;
import switchrefactor.refactoring.SwitchForEndIf;
import switchrefactor.refactoring.SwitchForNew;
import switchrefactor.refactoring.SwitchForOld;
import switchrefactor.refactoring.SwitchForRefactor;
import switchrefactor.refactoring.SwitchForYield;
import switchrefactor.refactoring.SwitchPatternAnalysis;
import switchrefactor.refactoring.SwitchRefactoring;

public class SwitchSearchRefactoring {

	String pathName;
	TypeDeclaration td;
	AST ast;
	MethodDeclaration m;
	boolean defaultAddLabel;
	boolean branchAddLabel;
	boolean dealC_DLabel;
	boolean cycleRefac1 = false;
	boolean cycleRefac2 = false;
	boolean[] flagArray = new boolean[] {false, false, false};

	// 数据初始化，递归遍历算法设计，找到所有的switch并重构
	public SwitchSearchRefactoring(String pathName, TypeDeclaration td, AST ast, MethodDeclaration m,
			boolean defaultAddLabel, boolean branchAddLabel, boolean dealC_DLabel) {
		this.pathName = pathName;
		this.td = td;
		this.ast = ast;
		this.m = m;
		this.defaultAddLabel = defaultAddLabel;
		this.branchAddLabel = branchAddLabel;
		this.dealC_DLabel = dealC_DLabel;
	}

	public void labeledStatementSearch(LabeledStatement labeledTemp) {
		Statement sTemp = labeledTemp.getBody();

		if (sTemp instanceof DoStatement) {
			DoStatement doTempTemp = (DoStatement) sTemp;
			doStatementSearch(doTempTemp);
		} else if (sTemp instanceof EnhancedForStatement) {
			EnhancedForStatement enForTemp = (EnhancedForStatement) sTemp;
			enhancedForStatementSearch(enForTemp);
		} else if (sTemp instanceof Block) {
			Block blockTemp = (Block) sTemp;
			blockSearch(blockTemp);
		} else if (sTemp instanceof ForStatement) {
			ForStatement forTemp = (ForStatement) sTemp;
			forStatementSearch(forTemp);
		} else if (sTemp instanceof IfStatement) {
			IfStatement ifTemp = (IfStatement) sTemp;
			ifStatementSearch(ifTemp);
		} else if (sTemp instanceof TryStatement) {
			TryStatement tryTemp = (TryStatement) sTemp;
			tryStatementSearch(tryTemp);
		} else if (sTemp instanceof WhileStatement) {
			WhileStatement whileTemp = (WhileStatement) sTemp;
			whileStatementSearch(whileTemp);
		} else if (sTemp instanceof LabeledStatement) {
			LabeledStatement labeledTempTemp = (LabeledStatement) sTemp;
			labeledStatementSearch(labeledTempTemp);
		} else if (sTemp instanceof SynchronizedStatement) {
			SynchronizedStatement synchronizedTemp = (SynchronizedStatement) sTemp;
			synchronizedStatementSearch(synchronizedTemp);
		}

		if (sTemp instanceof SwitchStatement) {
//			sTemp.delete();
			Statement stateTemp = switchSearchRefactor(sTemp);
			try {
				labeledTemp.setBody(stateTemp);
			} catch (IllegalArgumentException e) {
				labeledTemp.setBody((Statement) ASTNode.copySubtree(labeledTemp.getAST(), stateTemp));
				stateTemp.delete();
			}
		}
	}

	public void synchronizedStatementSearch(SynchronizedStatement synchronizedTemp) {
		Statement sTemp = synchronizedTemp.getBody();

		if (sTemp instanceof Block) {
			Block blockTemp = (Block) sTemp;
			blockSearch(blockTemp);
		}
	}

	public void doStatementSearch(DoStatement doTemp) {
		Statement sTemp = doTemp.getBody();
		if (sTemp instanceof DoStatement) {
			DoStatement doTempTemp = (DoStatement) sTemp;
			doStatementSearch(doTempTemp);
		} else if (sTemp instanceof EnhancedForStatement) {
			EnhancedForStatement enForTemp = (EnhancedForStatement) sTemp;
			enhancedForStatementSearch(enForTemp);
		} else if (sTemp instanceof Block) {
			Block blockTemp = (Block) sTemp;
			blockSearch(blockTemp);
		} else if (sTemp instanceof ForStatement) {
			ForStatement forTemp = (ForStatement) sTemp;
			forStatementSearch(forTemp);
		} else if (sTemp instanceof IfStatement) {
			IfStatement ifTemp = (IfStatement) sTemp;
			ifStatementSearch(ifTemp);
		} else if (sTemp instanceof TryStatement) {
			TryStatement tryTemp = (TryStatement) sTemp;
			tryStatementSearch(tryTemp);
		} else if (sTemp instanceof WhileStatement) {
			WhileStatement whileTemp = (WhileStatement) sTemp;
			whileStatementSearch(whileTemp);
		} else if (sTemp instanceof LabeledStatement) {
			LabeledStatement labeledTemp = (LabeledStatement) sTemp;
			labeledStatementSearch(labeledTemp);
		} else if (sTemp instanceof SynchronizedStatement) {
			SynchronizedStatement synchronizedTemp = (SynchronizedStatement) sTemp;
			synchronizedStatementSearch(synchronizedTemp);
		}

		if (sTemp instanceof SwitchStatement) {
//			sTemp.delete();
//			doTemp.setBody(switchSearchRefactor(sTemp));
			Statement stateTemp = switchSearchRefactor(sTemp);
			try {
				doTemp.setBody(stateTemp);
			} catch (IllegalArgumentException e) {
				doTemp.setBody((Statement) ASTNode.copySubtree(doTemp.getAST(), stateTemp));
				stateTemp.delete();
			}
		}
	}

	public void enhancedForStatementSearch(EnhancedForStatement enForTemp) {
		Statement sTemp = enForTemp.getBody();
		if (sTemp instanceof DoStatement) {
			DoStatement doTemp = (DoStatement) sTemp;
			doStatementSearch(doTemp);
		} else if (sTemp instanceof EnhancedForStatement) {
			EnhancedForStatement enForTempTemp = (EnhancedForStatement) sTemp;
			enhancedForStatementSearch(enForTempTemp);
		} else if (sTemp instanceof Block) {
			Block blockTemp = (Block) sTemp;
			blockSearch(blockTemp);
		} else if (sTemp instanceof ForStatement) {
			ForStatement forTemp = (ForStatement) sTemp;
			forStatementSearch(forTemp);
		} else if (sTemp instanceof IfStatement) {
			IfStatement ifTemp = (IfStatement) sTemp;
			ifStatementSearch(ifTemp);
		} else if (sTemp instanceof TryStatement) {
			TryStatement tryTemp = (TryStatement) sTemp;
			tryStatementSearch(tryTemp);
		} else if (sTemp instanceof WhileStatement) {
			WhileStatement whileTemp = (WhileStatement) sTemp;
			whileStatementSearch(whileTemp);
		} else if (sTemp instanceof LabeledStatement) {
			LabeledStatement labeledTemp = (LabeledStatement) sTemp;
			labeledStatementSearch(labeledTemp);
		} else if (sTemp instanceof SynchronizedStatement) {
			SynchronizedStatement synchronizedTemp = (SynchronizedStatement) sTemp;
			synchronizedStatementSearch(synchronizedTemp);
		}

		if (sTemp instanceof SwitchStatement) {
//			sTemp.delete();
//			enForTemp.setBody(switchSearchRefactor(sTemp));
			Statement stateTemp = switchSearchRefactor(sTemp);
			try {
				enForTemp.setBody(stateTemp);
			} catch (IllegalArgumentException e) {
				enForTemp.setBody((Statement) ASTNode.copySubtree(enForTemp.getAST(), stateTemp));
				stateTemp.delete();
			}
		}
	}

	public void forStatementSearch(ForStatement forTemp) {
		Statement sTemp = forTemp.getBody();
		if (sTemp instanceof DoStatement) {
			DoStatement doTemp = (DoStatement) sTemp;
			doStatementSearch(doTemp);
		} else if (sTemp instanceof EnhancedForStatement) {
			EnhancedForStatement enForTemp = (EnhancedForStatement) sTemp;
			enhancedForStatementSearch(enForTemp);
		} else if (sTemp instanceof Block) {
			Block blockTemp = (Block) sTemp;
			blockSearch(blockTemp);
		} else if (sTemp instanceof ForStatement) {
			ForStatement forTempTemp = (ForStatement) sTemp;
			forStatementSearch(forTempTemp);
		} else if (sTemp instanceof IfStatement) {
			IfStatement ifTemp = (IfStatement) sTemp;
			ifStatementSearch(ifTemp);
		} else if (sTemp instanceof TryStatement) {
			TryStatement tryTemp = (TryStatement) sTemp;
			tryStatementSearch(tryTemp);
		} else if (sTemp instanceof WhileStatement) {
			WhileStatement whileTemp = (WhileStatement) sTemp;
			whileStatementSearch(whileTemp);
		} else if (sTemp instanceof LabeledStatement) {
			LabeledStatement labeledTemp = (LabeledStatement) sTemp;
			labeledStatementSearch(labeledTemp);
		} else if (sTemp instanceof SynchronizedStatement) {
			SynchronizedStatement synchronizedTemp = (SynchronizedStatement) sTemp;
			synchronizedStatementSearch(synchronizedTemp);
		}

		if (sTemp instanceof SwitchStatement) {
//			sTemp.delete();
//			forTemp.setBody(switchSearchRefactor(sTemp));
			Statement stateTemp = switchSearchRefactor(sTemp);
			try {
				forTemp.setBody(stateTemp);
			} catch (IllegalArgumentException e) {
				forTemp.setBody((Statement) ASTNode.copySubtree(forTemp.getAST(), stateTemp));
				stateTemp.delete();
			}
		}
	}

	public void ifStatementSearch(IfStatement ifTemp) {
		// IF
		Statement sTemp = ifTemp.getThenStatement();
		if (sTemp instanceof DoStatement) {
			DoStatement doTemp = (DoStatement) sTemp;
			doStatementSearch(doTemp);
		} else if (sTemp instanceof EnhancedForStatement) {
			EnhancedForStatement enForTemp = (EnhancedForStatement) sTemp;
			enhancedForStatementSearch(enForTemp);
		} else if (sTemp instanceof Block) {
			Block blockTemp = (Block) sTemp;
			blockSearch(blockTemp);
		} else if (sTemp instanceof ForStatement) {
			ForStatement forTemp = (ForStatement) sTemp;
			forStatementSearch(forTemp);
		} else if (sTemp instanceof IfStatement) {
			IfStatement ifTempTemp = (IfStatement) sTemp;
			ifStatementSearch(ifTempTemp);
		} else if (sTemp instanceof TryStatement) {
			TryStatement tryTemp = (TryStatement) sTemp;
			tryStatementSearch(tryTemp);
		} else if (sTemp instanceof WhileStatement) {
			WhileStatement whileTemp = (WhileStatement) sTemp;
			whileStatementSearch(whileTemp);
		} else if (sTemp instanceof LabeledStatement) {
			LabeledStatement labeledTemp = (LabeledStatement) sTemp;
			labeledStatementSearch(labeledTemp);
		} else if (sTemp instanceof SynchronizedStatement) {
			SynchronizedStatement synchronizedTemp = (SynchronizedStatement) sTemp;
			synchronizedStatementSearch(synchronizedTemp);
		}

		if (sTemp instanceof SwitchStatement) {
//			sTemp.delete();
//			ifTemp.setThenStatement(switchSearchRefactor(sTemp));
			Statement stateTemp = switchSearchRefactor(sTemp);
			try {
				ifTemp.setThenStatement(stateTemp);
			} catch (IllegalArgumentException e) {
				ifTemp.setThenStatement((Statement) ASTNode.copySubtree(ifTemp.getAST(), stateTemp));
				stateTemp.delete();
			}
		}

		// ELSE
		sTemp = ifTemp.getElseStatement();
		if (sTemp instanceof DoStatement) {
			DoStatement doTemp = (DoStatement) sTemp;
			doStatementSearch(doTemp);
		} else if (sTemp instanceof EnhancedForStatement) {
			EnhancedForStatement enForTemp = (EnhancedForStatement) sTemp;
			enhancedForStatementSearch(enForTemp);
		} else if (sTemp instanceof Block) {
			Block blockTemp = (Block) sTemp;
			blockSearch(blockTemp);
		} else if (sTemp instanceof ForStatement) {
			ForStatement forTemp = (ForStatement) sTemp;
			forStatementSearch(forTemp);
		} else if (sTemp instanceof IfStatement) {
			IfStatement ifTempTemp = (IfStatement) sTemp;
			ifStatementSearch(ifTempTemp);
		} else if (sTemp instanceof TryStatement) {
			TryStatement tryTemp = (TryStatement) sTemp;
			tryStatementSearch(tryTemp);
		} else if (sTemp instanceof WhileStatement) {
			WhileStatement whileTemp = (WhileStatement) sTemp;
			whileStatementSearch(whileTemp);
		} else if (sTemp instanceof LabeledStatement) {
			LabeledStatement labeledTemp = (LabeledStatement) sTemp;
			labeledStatementSearch(labeledTemp);
		} else if (sTemp instanceof SynchronizedStatement) {
			SynchronizedStatement synchronizedTemp = (SynchronizedStatement) sTemp;
			synchronizedStatementSearch(synchronizedTemp);
		}

		if (sTemp instanceof SwitchStatement) {
//			sTemp.delete();
//			ifTemp.setElseStatement(switchSearchRefactor(sTemp));
			Statement stateTemp = switchSearchRefactor(sTemp);
			try {
				ifTemp.setElseStatement(stateTemp);
			} catch (IllegalArgumentException e) {
				ifTemp.setElseStatement((Statement) ASTNode.copySubtree(ifTemp.getAST(), stateTemp));
				stateTemp.delete();
			}
		}
	}

	public void tryStatementSearch(TryStatement tryTemp) {
		Statement sTemp = tryTemp.getBody();

		if (sTemp instanceof Block) {
			Block blockTemp = (Block) sTemp;
			blockSearch(blockTemp);
		}
	}

	public void whileStatementSearch(WhileStatement whileTemp) {
		Statement sTemp = whileTemp.getBody();
		if (sTemp instanceof DoStatement) {
			DoStatement doTemp = (DoStatement) sTemp;
			doStatementSearch(doTemp);
		} else if (sTemp instanceof EnhancedForStatement) {
			EnhancedForStatement enForTemp = (EnhancedForStatement) sTemp;
			enhancedForStatementSearch(enForTemp);
		} else if (sTemp instanceof Block) {
			Block blockTemp = (Block) sTemp;
			blockSearch(blockTemp);
		} else if (sTemp instanceof ForStatement) {
			ForStatement forTemp = (ForStatement) sTemp;
			forStatementSearch(forTemp);
		} else if (sTemp instanceof IfStatement) {
			IfStatement ifTemp = (IfStatement) sTemp;
			ifStatementSearch(ifTemp);
		} else if (sTemp instanceof TryStatement) {
			TryStatement tryTemp = (TryStatement) sTemp;
			tryStatementSearch(tryTemp);
		} else if (sTemp instanceof WhileStatement) {
			WhileStatement whileTempTemp = (WhileStatement) sTemp;
			whileStatementSearch(whileTempTemp);
		} else if (sTemp instanceof LabeledStatement) {
			LabeledStatement labeledTemp = (LabeledStatement) sTemp;
			labeledStatementSearch(labeledTemp);
		} else if (sTemp instanceof SynchronizedStatement) {
			SynchronizedStatement synchronizedTemp = (SynchronizedStatement) sTemp;
			synchronizedStatementSearch(synchronizedTemp);
		}

		if (sTemp instanceof SwitchStatement) {
//			sTemp.delete();
//			whileTemp.setBody(switchSearchRefactor(sTemp));
			Statement stateTemp = switchSearchRefactor(sTemp);
			try {
				whileTemp.setBody(stateTemp);
			} catch (IllegalArgumentException e) {
				whileTemp.setBody((Statement) ASTNode.copySubtree(whileTemp.getAST(), stateTemp));
				stateTemp.delete();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void blockSearch(Block blockTemp) {

		Statement sTemp = null;
		for (int i = 0; i < blockTemp.statements().size(); i++) {
			sTemp = (Statement) blockTemp.statements().get(i);
			if (sTemp instanceof DoStatement) {
				DoStatement doTemp = (DoStatement) sTemp;
				doStatementSearch(doTemp);
			} else if (sTemp instanceof EnhancedForStatement) {
				EnhancedForStatement enForTemp = (EnhancedForStatement) sTemp;
				enhancedForStatementSearch(enForTemp);
			} else if (sTemp instanceof Block) {
				Block blockTempTemp = (Block) sTemp;
				blockSearch(blockTempTemp);
			} else if (sTemp instanceof ForStatement) {
				ForStatement forTemp = (ForStatement) sTemp;
				forStatementSearch(forTemp);
			} else if (sTemp instanceof IfStatement) {
				IfStatement ifTemp = (IfStatement) sTemp;
				ifStatementSearch(ifTemp);
			} else if (sTemp instanceof TryStatement) {
				TryStatement tryTemp = (TryStatement) sTemp;
				tryStatementSearch(tryTemp);
			} else if (sTemp instanceof WhileStatement) {
				WhileStatement whileTemp = (WhileStatement) sTemp;
				whileStatementSearch(whileTemp);
			} else if (sTemp instanceof LabeledStatement) {
				LabeledStatement labeledTemp = (LabeledStatement) sTemp;
				labeledStatementSearch(labeledTemp);
			} else if (sTemp instanceof SynchronizedStatement) {
				SynchronizedStatement synchronizedTemp = (SynchronizedStatement) sTemp;
				synchronizedStatementSearch(synchronizedTemp);
			}

			if (sTemp instanceof SwitchStatement) {
				blockTemp.statements().remove(i);
				Statement stateTemp = switchSearchRefactor(sTemp);
				try {
					blockTemp.statements().add(i, stateTemp);
				} catch (IllegalArgumentException e) {
					blockTemp = ast.newBlock();
					blockTemp.statements().add(i, (Statement) ASTNode.copySubtree(blockTemp.getAST(), stateTemp));
					stateTemp.delete();
				}
			}
		}
	}

	public Statement switchSearchRefactor(Statement sTemp) {

		// System.out.println("类名" + td.getName());
		// System.out.println("方法名" + m.getName());

		boolean label = false;
		int category = 0;
		boolean analysisLabel = false;
		int checkDefaultLabel = 3;
		boolean checkDefaultAddLabel = false;
		Statement refacTemp = null;

		SwitchRefactoring.sumSwitch++;

		LinkedHashMap<SwitchCase, Integer> caseMap = new LinkedHashMap<SwitchCase, Integer>();
		LinkedHashMap<Integer, Block> blockMap = new LinkedHashMap<Integer, Block>();

		SwitchStatement ss = (SwitchStatement) sTemp;
		Expression e = ss.getExpression();

		IJavaElement jTemp = SwitchRefactoring.jTemp;
		int startPoint = ss.getStartPosition();
		// 这里
//		m.getBody().statements().remove(i);

		SwitchStatement ssTemp = (SwitchStatement) ASTNode.copySubtree(AST.newAST(14, false), ss);

		if (ss.statements().size() != 0) {
			if (ss.statements().get(0) instanceof SwitchCase) {
				SwitchCase scTemp = (SwitchCase) ss.statements().get(0);
				analysisLabel = scTemp.isSwitchLabeledRule();
				if (analysisLabel) {
					SwitchPatternAnalysis.caseAnalysisNew(ss, ast, caseMap, blockMap);
				} else {
					SwitchPatternAnalysis.caseAnalysisOld(ss, ast, caseMap, blockMap);
				}
			} else {
				SwitchRefactoring.sumCategoryZero++;
				// 这里
				return (Statement) ASTNode.copySubtree(ast, ssTemp);
			}
		}

		LinkedHashMap<SwitchCase, Integer> caseMapTemp = new LinkedHashMap<SwitchCase, Integer>();
		caseMapTemp.putAll(caseMap);
		LinkedHashMap<Integer, Block> blockMapTemp = new LinkedHashMap<Integer, Block>();
		blockMapTemp.putAll(blockMap);

		// 判断switch模式符是否为新特性
		label = analysisLabel;
		if (label) {
			category = SwitchForNew.refactorSwitchForNew(ast, caseMap, blockMap, flagArray);
		} else {
			category = SwitchForOld.refactorSwitchForOld(ast, caseMap, blockMap, branchAddLabel, dealC_DLabel, e, flagArray);
		}

		SwitchStatement s = ast.newSwitchStatement();
		MethodInvocation me = ast.newMethodInvocation();
		Assignment a = ast.newAssignment();
		ReturnStatement r = ast.newReturnStatement();

		AddressRecord ar1 = null;
		AddressRecord ar2 = null;
		AddressRecord ar3 = null;
		AddressRecord ar4 = null;

		if (ss.statements().size() == 0 && defaultAddLabel) {
			SwitchRefactoring.sumCategoryZero++;
			SwitchForYield.defaultAddSpec(ast, e, s, true);
			String tdName = "";
			String mName = "";
			if (td != null) {
				tdName = td.getName().toString();
			}
			if (m != null) {
				mName = m.getName().toString();
			}
			if (SwitchRefactoring.REFAC) {
				ar2 = new AddressRecord(pathName, tdName, mName, "switch (" + e.toString() + ")", jTemp, startPoint);
				DataLogList.RecordFactory(ar2, 2);

				SwitchRefactoring.defaultMissList.add(tdName + " :: " + mName + " :: switch ( " + e.toString() + " )");
			}
			return s;
		}

		if (category == -1) {
			SwitchForYield.checkDefault(ast, caseMapTemp, blockMapTemp);
			category = 0;
		} else if (category != -2) {
			checkDefaultLabel = SwitchForYield.checkDefault(ast, caseMap, blockMap, defaultAddLabel, e);
			if (checkDefaultLabel == 3) {
				category = 0;
			} else if (checkDefaultLabel == 2) {
				checkDefaultAddLabel = true;
			} else if (checkDefaultLabel == 1) {

			}
		}
		
		if (SwitchRefactoring.ENDSWITCH1 && !SwitchRefactoring.ENDSWITCH2) {
			if (category == 3) {
				SwitchRefactoring.sumNewEndSwitchTemp ++;
				SwitchRefactoring.rebackOldSwitch ++;
			}
			if (category == 0 && label == false && SwitchRefactoring.oldToNewLabel) {
				SwitchRefactoring.sumNewEndSwitchTemp ++;
				SwitchRefactoring.rebackOldSwitch ++;
				SwitchRefactoring.oldToNewLabel = false;
			}
			if (category == -2 && label == false && SwitchRefactoring.oldToNewLabel) {
				SwitchRefactoring.sumNewEndSwitchTemp ++;
				SwitchRefactoring.rebackOldSwitch ++;
				SwitchRefactoring.oldToNewLabel = false;
//				SwitchRefactoring.ENDSWITCH3 = false;
			}
			SwitchRefactoring.BREAK = false;
			SwitchRefactoring.BRANCH = false;
			SwitchRefactoring.DEFAULT = false;
			SwitchRefactoring.CASEDE = false;
		}
		if (SwitchRefactoring.ENDSWITCH2) {
			SwitchRefactoring.BREAK = false;
			SwitchRefactoring.BRANCH = false;
			SwitchRefactoring.DEFAULT = false;
			SwitchRefactoring.CASEDE = false;
		}
		if (!SwitchRefactoring.ENDSWITCH1 && !SwitchRefactoring.ENDSWITCH2) {
			if (SwitchRefactoring.oldToNewLabel) {
				SwitchRefactoring.oldToNewLabel = false;
			}
			if (SwitchRefactoring.BREAK) {
				// 构建break缺失List
				String tdName = "";
				String mName = "";

				if (td != null) {
					tdName = td.getName().toString();
				}

				if (m != null) {
					mName = m.getName().toString();
				}

				if (SwitchRefactoring.REFAC) {
					ar1 = new AddressRecord(pathName, tdName, mName, "switch (" + e.toString() + ")", jTemp,
							startPoint);
					DataLogList.RecordFactory(ar1, 1);

					SwitchRefactoring.breakMissList
							.add(tdName + " :: " + mName + " :: switch ( " + e.toString() + " )");
				}
				SwitchRefactoring.BREAK = false;
			}
			if (SwitchRefactoring.DEFAULT) {
				// 构建default缺失List
				String tdName = "";
				String mName = "";

				if (td != null) {
					tdName = td.getName().toString();
				}

				if (m != null) {
					mName = m.getName().toString();
				}

				if (SwitchRefactoring.REFAC) {
					ar2 = new AddressRecord(pathName, tdName, mName, "switch (" + e.toString() + ")", jTemp,
							startPoint);
					DataLogList.RecordFactory(ar2, 2);

					SwitchRefactoring.defaultMissList
							.add(tdName + " :: " + mName + " :: switch ( " + e.toString() + " )");
				}
				SwitchRefactoring.DEFAULT = false;
			}
			if (SwitchRefactoring.BRANCH) {
				// 构建branch缺失List
				String tdName = "";
				String mName = "";

				if (td != null) {
					tdName = td.getName().toString();
				}

				if (m != null) {
					mName = m.getName().toString();
				}

				if (SwitchRefactoring.REFAC) {
					ar3 = new AddressRecord(pathName, tdName, mName, "switch (" + e.toString() + ")", jTemp,
							startPoint);
					DataLogList.RecordFactory(ar3, 3);

					SwitchRefactoring.branchMissList
							.add(tdName + " :: " + mName + " :: switch ( " + e.toString() + " )");
				}
				SwitchRefactoring.BRANCH = false;
			}
			if (SwitchRefactoring.CASEDE) {
				// 构建case-default连接List
				String tdName = "";
				String mName = "";

				if (td != null) {
					tdName = td.getName().toString();
				}

				if (m != null) {
					mName = m.getName().toString();
				}

				if (SwitchRefactoring.REFAC) {
					ar4 = new AddressRecord(pathName, tdName, mName, "switch (" + e.toString() + ")", jTemp,
							startPoint);
					DataLogList.RecordFactory(ar4, 4);

					SwitchRefactoring.caseDefaultList
							.add(tdName + " :: " + mName + " :: switch ( " + e.toString() + " )");
				}
				SwitchRefactoring.CASEDE = false;
			}
		}
		
		try {
			SwitchForRefactor sfr = new SwitchForRefactor(pathName, td, ast, m, label, category, analysisLabel,
					checkDefaultLabel, defaultAddLabel, branchAddLabel, dealC_DLabel);

			if (category == 3) {
				boolean checkMidIFLabel = SwitchForEndIf.checkMidIf(null, blockMap);
				if (!checkMidIFLabel) {
					category = 0;
				}
			}

			if (category == 0) {
				sfr.switchForUnrefactor(ast, caseMap, blockMap, e, s, checkDefaultAddLabel);
			} else if (category == 1) {
				sfr.switchForMethodrefactor(ast, caseMap, blockMap, e, s, me, checkDefaultAddLabel);
			} else if (category == 2) {
				sfr.switchForAssignrefactor(ast, caseMap, blockMap, e, s, a, checkDefaultAddLabel);
			} else if (category == 3) {
				sfr.switchForReturnrefactor(ast, caseMap, blockMap, e, r, checkDefaultAddLabel);
			} else if (category == -2) {
				sfr.switchForCDrefactor(ast, caseMap, blockMap, e, s, checkDefaultAddLabel);
			}
		} catch (IllegalArgumentException exception) {
			SwitchRefactoring.sumCategoryZero++;
			Statement ssTempExce = (Statement) ASTNode.copySubtree(ast, ssTemp);
			return ssTempExce;
		}
		
//		System.out.println(caseMap);
//		System.out.println("对比" + caseMapTemp);
//		System.out.println(blockMap);
//		System.out.println("对比" + blockMapTemp);

		if (category == 0 || category == -2) {
			SwitchRefactoring.sumCategoryZero++;
			refacTemp = s;
		} else if (category == 1) {
			if (!me.getName().toString().equals("MISSING")) {
				SwitchRefactoring.sumCategoryOne++;
				refacTemp = ast.newExpressionStatement(me);
			} else {
				SwitchRefactoring.sumCategoryZero++;
				refacTemp = s;
			}
		} else if (category == 2) {
			if (!a.getLeftHandSide().toString().equals("MISSING")) {
				SwitchRefactoring.sumCategoryTwo++;
				refacTemp = ast.newExpressionStatement(a);
			} else {
				SwitchRefactoring.sumCategoryZero++;
				refacTemp = s;
			}
		} else if (category == 3) {
			SwitchRefactoring.sumCategoryThree++;
			refacTemp = r;
			dealReturn(refacTemp);
		}
		
		if (SwitchRefactoring.llllllabel) {
			System.out.println(refacTemp);
			SwitchRefactoring.llllllabel = false;
		}
		
		if (refacTemp != null) {
			if (flagArray[0] == true) {
				int sumSwitchTemp = SwitchRefactoring.sumSwitch;
				int sumNewTemp = SwitchRefactoring.sumCategoryNew;
				
				SwitchRefactoring.ENDSWITCH1 = true;
				flagArray[0] = false;
				if (!cycleRefac1) {
					cycleRefac1 = true;
					refacTemp = switchSearchRefactor(refacTemp);
				}
				
				SwitchRefactoring.sumSwitch = sumSwitchTemp;
				SwitchRefactoring.sumCategoryNew = sumNewTemp;
				
				dealYield(refacTemp);
				return refacTemp;
			} else if (flagArray[1] == true) {
				int sumSwitchTemp = SwitchRefactoring.sumSwitch;
				int sumNewTemp = SwitchRefactoring.sumCategoryNew;
				
				SwitchRefactoring.ENDSWITCH2 = true;
				flagArray[1] = false;
				if (!cycleRefac2) {
					cycleRefac2 = true;
					refacTemp = switchSearchRefactor(refacTemp);
				}
				
				SwitchRefactoring.sumSwitch = sumSwitchTemp;
				SwitchRefactoring.sumCategoryNew = sumNewTemp;
				
				dealYield(refacTemp);
				return refacTemp;
			} else {
				dealYield(refacTemp);
				return refacTemp;
			}
		} else {
			Statement ssTempElse = (Statement) ASTNode.copySubtree(ast, ssTemp);
			return ssTempElse;
		}
	}

	private void dealReturn(Statement refacTemp) {
		Expression e = ((ReturnStatement) refacTemp).getExpression();
		ArrayList<ReturnStatement> list = new ArrayList<>();
		findReturns(e, list);
		for (ReturnStatement rsTemp : list) {
			YieldStatement ysTemp = refacTemp.getAST().newYieldStatement();
			ysTemp.setExpression((Expression) ASTNode.copySubtree(refacTemp.getAST(), rsTemp.getExpression()));
			SwitchRefactoring.rewrite.replace(rsTemp, ysTemp, null);
		}
	}

	private void dealYield(Statement refacTemp) {
		ArrayList<YieldStatement> list = new ArrayList<YieldStatement>();
		findYields(refacTemp, list);
		for (YieldStatement ys : list) {
			if (ys.isImplicit()) {
				Expression eTemp = ys.getExpression();
				ExpressionStatement state = refacTemp.getAST()
						.newExpressionStatement((Expression) ASTNode.copySubtree(refacTemp.getAST(), eTemp));
				SwitchRefactoring.rewrite.replace(ys, state, null);
			} else {
				continue;
			}
		}
	}

	private void findYields(ASTNode root, ArrayList<YieldStatement> list) {
		root.accept(new ASTVisitor() {
			public boolean visit(YieldStatement node) {
				list.add(node);
				return false;
			}
		});
	}

	private void findReturns(ASTNode root, ArrayList<ReturnStatement> list) {
		root.accept(new ASTVisitor() {
			public boolean visit(ReturnStatement node) {
				list.add(node);
				return false;
			}
		});
	}
}
