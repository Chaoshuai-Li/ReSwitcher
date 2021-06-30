package switchrefactor.refactoring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;

//多case连用，重构，需结合break判断结果
public class SwitchForSeriesCase {
	@SuppressWarnings("unchecked")
	public static void seriesCase(AST ast, LinkedHashMap<SwitchCase, Integer> caseMap) {
		Iterator<SwitchCase> iter = caseMap.keySet().iterator();
		List<Expression> list = null;
		int tempLast = 0;
		int temp = 0;
		SwitchCase switchCaseTemp;

		while (iter.hasNext()) {
			if (tempLast == 0) {
				switchCaseTemp = iter.next();
				switchCaseTemp.setSwitchLabeledRule(true);
				list = switchCaseTemp.expressions();
				tempLast = caseMap.get(switchCaseTemp);
			}
			if (tempLast != 0) {
				switchCaseTemp = iter.next();
				switchCaseTemp.setSwitchLabeledRule(true);
				temp = caseMap.get(switchCaseTemp);

				if (switchCaseTemp.isDefault()) {
					tempLast = 0;
					continue;
				}
				if (tempLast == temp) {
					switchCaseTemp.delete();
					iter.remove();
					List<Expression> listTemp = ASTNode.copySubtrees(ast, switchCaseTemp.expressions());
					list.addAll(listTemp);
				} else {
					list = switchCaseTemp.expressions();
					tempLast = caseMap.get(switchCaseTemp);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static int seriesCase(AST ast, LinkedHashMap<SwitchCase, Integer> caseMap,
			LinkedHashMap<Integer, Block> blockMap, Boolean label) {
		Iterator<SwitchCase> iter = caseMap.keySet().iterator();
		List<Expression> list = null;
		int tempLast = 0;
		int temp = 0;
		SwitchCase switchCaseTemp;
		boolean defaultLabel = false;
		boolean defaultExist = false;
		boolean caseDefaultLable = false;

		while (iter.hasNext()) {
			switchCaseTemp = iter.next();
			if (tempLast == 0) {
				switchCaseTemp.setSwitchLabeledRule(label);
				list = switchCaseTemp.expressions();
				tempLast = caseMap.get(switchCaseTemp);
				if (switchCaseTemp.isDefault()) {
					defaultLabel = true;
				}
				if (defaultExist && tempLast == temp) {
					caseDefaultLable = true;
					defaultExist = false;
				}
			} else {
				switchCaseTemp.setSwitchLabeledRule(label);
				temp = caseMap.get(switchCaseTemp);

				if (switchCaseTemp.isDefault()) {
					if (tempLast == temp) {
						caseDefaultLable = true;
					} else {
						defaultExist = true;
					}
					tempLast = 0;
					continue;
				} else if (defaultLabel && tempLast == temp) {
					caseDefaultLable = true;
					defaultLabel = false;
					list = switchCaseTemp.expressions();
					tempLast = caseMap.get(switchCaseTemp);
				} else if (tempLast == temp) {
					switchCaseTemp.delete();
					iter.remove();
					List<Expression> listTemp = ASTNode.copySubtrees(ast, switchCaseTemp.expressions());
					list.addAll(listTemp);
				} else {
					list = switchCaseTemp.expressions();
					tempLast = caseMap.get(switchCaseTemp);
				}
			}
		}
		if (caseDefaultLable == true) {
			Iterator<SwitchCase> iterTemp = caseMap.keySet().iterator();
			SwitchCase scTemp;
			Block blockTemp;
			while (iterTemp.hasNext()) {
				scTemp = iterTemp.next();
				if (scTemp.isDefault()) {
					blockTemp = blockMap.get(caseMap.get(scTemp));
					if (!searchSwitch(blockTemp)) {
						return -1;
					}
				}
			}
			return 0;
		}
		return 1;
	}

	public static void seriesCase(LinkedHashMap<SwitchCase, Integer> caseMap, LinkedHashMap<Integer, Block> blockMap,
			Boolean label) {
		Iterator<SwitchCase> iter = caseMap.keySet().iterator();
		SwitchCase switchCaseTemp;
		while (iter.hasNext()) {
			switchCaseTemp = iter.next();
			switchCaseTemp.setSwitchLabeledRule(label);
		}
	}

	private static boolean searchSwitch(Block blockTemp) {
		ArrayList<SwitchStatement> list = new ArrayList<SwitchStatement>();
		blockTemp.accept(new ASTVisitor() {
			public boolean visit(SwitchStatement node) {
				list.add(node);
				return true;
			}
		});
		if (list.size() != 0) {
			return false;
		} else {
			return true;
		}
	}

	public static void dealC_DMap(AST ast, LinkedHashMap<SwitchCase, Integer> caseMap,
			LinkedHashMap<Integer, Block> blockMap) {
		int recordTemp = 0;
		int record = 0;
		int num = 0;
		boolean label = false;
		Iterator<SwitchCase> iter = caseMap.keySet().iterator();
		while (iter.hasNext()) {
			SwitchCase scTemp = iter.next();
			if (recordTemp == 0) {
				recordTemp = caseMap.get(scTemp);
			} else {
				if (recordTemp == caseMap.get(scTemp)) {
					record = recordTemp;
					num++;
				} else {
					recordTemp = caseMap.get(scTemp);
				}
			}
		}
		
		Iterator<SwitchCase> iterTemp = caseMap.keySet().iterator();
		while (iterTemp.hasNext()) {
			SwitchCase scTemp = iterTemp.next();
			int caseNum = caseMap.get(scTemp);
			if (label) {
				caseMap.replace(scTemp, caseNum, caseNum + num);
			} else if (record == caseMap.get(scTemp)) {
				label = true;
				for (int j = 1; j <= num; j++) {
					SwitchCase scTempTemp = iterTemp.next();
					int numTemp = caseMap.get(scTempTemp);
					caseMap.replace(scTempTemp, numTemp, numTemp + j);
				}
			}
		}
		
		int size = blockMap.size();
		for (int j = size; j > record; j--) {
			Block blockTemp = blockMap.get(j);
			blockMap.remove(j, blockTemp);
			blockMap.put(j + num, blockTemp);
		}
		
		Block blockTempTemp = blockMap.get(record);
		for (int j = 1; j <= num; j ++) {
			blockMap.put(record + j, (Block) ASTNode.copySubtree(ast, blockTempTemp));
		}
	}
}
