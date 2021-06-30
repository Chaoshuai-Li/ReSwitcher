package switchrefactor.refactoring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;

public class SwitchForBreak {

	public static void checkEndOfBlock(LinkedHashMap<Integer, Block> blockMap,
			boolean[] flagArray) {
		int lable = 1;
		while (blockMap.containsKey(lable)) {
			Block blockTemp = blockMap.get(lable);
			int index = blockTemp.statements().size() - 1;
			if (index >= 0) {
				if (blockTemp.statements()
						.get(index) instanceof SwitchStatement) {
					flagArray[0] = true;
				}
			}
			lable++;
		}
	}

	// break检查，结合条件分支后执行语句情况
	public static boolean checkBreak(LinkedHashMap<SwitchCase, Integer> caseMap,
			LinkedHashMap<Integer, Block> blockMap, boolean[] flagArray) {
		int label = 1;
		boolean checkBreakResult = false;

		int iterLabel = 1;
		boolean branchMiss = false;

		Iterator<SwitchCase> iter = caseMap.keySet().iterator();
		while (iter.hasNext()) {
			iterLabel = caseMap.get(iter.next());
			if (!blockMap.containsKey(iterLabel)) {
				branchMiss = true;
			}
		}

		if (branchMiss) {
			if (!blockMap.containsKey(label)) {
				return true;
			}
			while (blockMap.containsKey(label)) {
				Block block = blockMap.get(label);
				if (block.statements().size() > 0) {
					int index = block.statements().size() - 1;
					if (block.statements().get(index) instanceof BreakStatement
							|| block.statements()
									.get(index) instanceof ThrowStatement) {
						label++;
						continue;
					}
				}
				if (block.statements().size() > 0) {
					int index = block.statements().size() - 1;
					if (block.statements().get(index) instanceof ReturnStatement
					// || block.statements().get(index) instanceof
					// ContinueStatement
					) {
						label++;
						continue;
					}
				}
				if (block.statements().size() > 0) {
					int index = block.statements().size() - 1;
					if (block.statements().get(index) instanceof TryStatement) {
						boolean tryEnd = SwitchForEndIf.checkEndTry(
								(TryStatement) block.statements().get(index));
						if (tryEnd) {
							label++;
							continue;
						}
					}
				}
				if (block.statements().size() > 0) {
					int index = block.statements().size() - 1;
					if (block.statements().get(index) instanceof IfStatement) {
						boolean ifEnd = SwitchForEndIf.checkEndIf(
								(IfStatement) block.statements().get(index),
								true);
						if (ifEnd) {
							label++;
							continue;
						}
					}
				}
				if (block.statements().size() > 0) {
					int index = block.statements().size() - 1;
					if (block.statements()
							.get(index) instanceof SwitchStatement) {
						flagArray[0] = true;
					}
				}
				label = 0;
				checkBreakResult = false;
				break;
			}
			if (label != 0) {
				checkBreakResult = true;
			}
		} else {
			if (!blockMap.containsKey(label)) {
				return true;
			}
			while (blockMap.containsKey(label)) {
				Block block = blockMap.get(label);
				if (block.statements().size() > 0) {
					int index = block.statements().size() - 1;
					if (block.statements().get(index) instanceof BreakStatement
							|| block.statements()
									.get(index) instanceof ThrowStatement) {
						label++;
						continue;
					}
				}
				if (block.statements().size() > 0) {
					int index = block.statements().size() - 1;
					if (block.statements().get(index) instanceof ReturnStatement
							|| block.statements()
									.get(index) instanceof ContinueStatement) {
						label++;
						continue;
					}
				}
				if (block.statements().size() > 0) {
					int index = block.statements().size() - 1;
					if (block.statements().get(index) instanceof TryStatement) {
						boolean tryEnd = SwitchForEndIf.checkEndTry(
								(TryStatement) block.statements().get(index));
						if (tryEnd) {
							label++;
							continue;
						}
					}
				}
				if (block.statements().size() > 0) {
					int index = block.statements().size() - 1;
					if (block.statements().get(index) instanceof IfStatement) {
						boolean ifEnd = SwitchForEndIf.checkEndIf(
								(IfStatement) block.statements().get(index),
								true);
						if (ifEnd) {
							label++;
							continue;
						}
					}
				}
				if (block.statements().size() > 0) {
					int index = block.statements().size() - 1;
					if (block.statements()
							.get(index) instanceof SwitchStatement) {
						flagArray[0] = true;
					}
				}
				if (blockMap.containsKey(label + 1)) {
					label = 0;
					checkBreakResult = false;
					break;
				} else {
					label++;
				}
			}
			if (label != 0) {
				checkBreakResult = true;
			}
		}
		if (!checkBreakResult) {
			SwitchRefactoring.BREAK = true;
		}
		return checkBreakResult;
	}

	// 重构为新特性（->,Expression），去除无用的break
	public static void deleteBreak(LinkedHashMap<Integer, Block> blockMap) {
		int label = 1;
		while (blockMap.containsKey(label)) {
			Block block = blockMap.get(label);
			if (block.statements().size() >= 1) {
				int index = block.statements().size() - 1;
				if (block.statements().get(index) instanceof BreakStatement) {
					BreakStatement bs = (BreakStatement) block.statements()
							.get(index);
					if (bs.getLabel() == null) {
						block.statements().remove(index);
					}
				}
			}
			if (block.statements().size() == 1) {
				if (block.statements().get(0) instanceof Block) {
					blockMap.replace(label, (Block) block.statements().get(0));
				}
			}
			label++;
		}
	}

	public static void deleteIfBreak(LinkedHashMap<Integer, Block> blockMap) {
		int label = 1;
		while (blockMap.containsKey(label)) {
			Block block = blockMap.get(label);
			if (block.statements().size() >= 1) {
				int index = block.statements().size() - 1;
				if (block.statements().get(index) instanceof IfStatement) {
					IfStatement ifTemp = (IfStatement) block.statements().get(index);
					deleteBreakInIf(ifTemp);
				}
			}
			label++;
		}
	}

	private static boolean deleteBreakInIf(IfStatement ifTemp) {
		List<Statement> list = new ArrayList<>();
		while (true) {
			if (ifTemp.getThenStatement() != null) {
				list.add(ifTemp.getThenStatement());
			}
			if (ifTemp.getElseStatement() != null) {
				Statement sTemp = ifTemp.getElseStatement();
				if (sTemp instanceof IfStatement) {
					ifTemp = (IfStatement) sTemp;
				} else {
					list.add(sTemp);
					break;
				}
			} else {
				break;
			}
		}
		for (Statement s : list) {
			if (s instanceof BreakStatement) {
				
			} else if (s instanceof IfStatement) {
				boolean label = deleteBreakInIf((IfStatement) s);
				if (label) {
					continue;
				}
			} else if (s instanceof TryStatement) {
				boolean label = deleteBreakInTry((TryStatement) s);
				if (label) {
					continue;
				}
			} else if (s instanceof Block) {
				Block blockTemp = (Block) s;
				int index = blockTemp.statements().size();
				Statement sTemp = (Statement) blockTemp.statements().get(index - 1);
				if (sTemp instanceof BreakStatement) {
					blockTemp.statements().remove(index - 1);
					continue;
				} else if (sTemp instanceof IfStatement) {
					boolean label = deleteBreakInIf((IfStatement) sTemp);
					if (label) {
						continue;
					}
				} else if (sTemp instanceof TryStatement) {
					boolean label = deleteBreakInTry((TryStatement) sTemp);
					if (label) {
						continue;
					}
				}
			}
		}
		return true;
	}

	public static void deleteTryBreak(LinkedHashMap<Integer, Block> blockMap) {
		int label = 1;
		while (blockMap.containsKey(label)) {
			Block block = blockMap.get(label);
			if (block.statements().size() >= 1) {
				int index = block.statements().size() - 1;
				if (block.statements().get(index) instanceof TryStatement) {
					TryStatement tryTemp = (TryStatement) block.statements().get(index);
					deleteBreakInTry(tryTemp);
				}
			}
			label++;
		}
	}

	@SuppressWarnings("unchecked")
	private static boolean deleteBreakInTry(TryStatement tTemp) {
		Block bTemp = null;
		bTemp = tTemp.getBody();
		if(bTemp.statements().size() > 0 && bTemp.statements().get(bTemp.statements().size() - 1) instanceof BreakStatement) {
			bTemp.statements().remove(bTemp.statements().size() - 1);
		} else if (bTemp.statements().size() > 0 && bTemp.statements().get(bTemp.statements().size() - 1) instanceof IfStatement) {
			IfStatement ifTemp = (IfStatement) bTemp.statements().get(bTemp.statements().size() - 1);
			deleteBreakInIf(ifTemp);
		}
		List<CatchClause> list =  tTemp.catchClauses();
		for (CatchClause cc : list) {
			bTemp = cc.getBody();
			if(bTemp.statements().size() > 0 && bTemp.statements().get(bTemp.statements().size() - 1) instanceof BreakStatement) {
				bTemp.statements().remove(bTemp.statements().size() - 1);
			} else if (bTemp.statements().size() > 0 && bTemp.statements().get(bTemp.statements().size() - 1) instanceof IfStatement) {
				IfStatement ifTemp = (IfStatement) bTemp.statements().get(bTemp.statements().size() - 1);
				deleteBreakInIf(ifTemp);
			}
		}
		bTemp = tTemp.getFinally();
		if (bTemp != null) {
			if(bTemp.statements().size() > 0 && bTemp.statements().get(bTemp.statements().size() - 1) instanceof BreakStatement) {
				bTemp.statements().remove(bTemp.statements().size() - 1);
			} else if (bTemp.statements().size() > 0 && bTemp.statements().get(bTemp.statements().size() - 1) instanceof IfStatement) {
				IfStatement ifTemp = (IfStatement) bTemp.statements().get(bTemp.statements().size() - 1);
				deleteBreakInIf(ifTemp);
			}
		}
		return true;
	}
}

/*
 * if (block.statements().size() > 0) { int index = block.statements().size() -
 * 1; if (block.statements().get(index) instanceof IfStatement) { sumReturn++; }
 * } if (block.statements().size() > 0) { int index = block.statements().size()
 * - 1; if (block.statements().get(index) instanceof SwitchStatement) {
 * SwitchSearchRefactoring ssr = new
 * SwitchSearchRefactoring(SwitchInformationInit.pathName,
 * SwitchInformationInit.td, SwitchInformationInit.ast, SwitchInformationInit.m,
 * SwitchInformationInit.label, SwitchInformationInit.category,
 * SwitchInformationInit.analysisLabel, SwitchInformationInit.checkDefaultLabel,
 * SwitchInformationInit.checkDefaultAddLabel,
 * SwitchInformationInit.defaultAddLabel, SwitchInformationInit.branchAddLabel);
 * Statement sTempTemp = (Statement) block.statements().get(index);
 * sTempTemp.delete(); Statement sTemp = ssr.switchSearchRefactor(sTempTemp);
 * block.statements().add(index, sTemp);
 * 
 * if (block.statements().get(index) instanceof ReturnStatement) { sumReturn++;
 * } } }
 */