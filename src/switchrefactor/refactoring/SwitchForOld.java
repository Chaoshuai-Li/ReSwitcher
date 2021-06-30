package switchrefactor.refactoring;

import java.util.LinkedHashMap;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SwitchCase;

public class SwitchForOld {
	
	//条件分支连接符为":"
	public static int refactorSwitchForOld(AST ast, LinkedHashMap<SwitchCase, Integer> caseMap
			, LinkedHashMap<Integer, Block> blockMap, boolean caseAddLabel, boolean dealC_DLabel, Expression e, boolean[] flagArray) {
		int category = 0;
		if (!SwitchRefactoring.ENDSWITCH1) {
			SwitchRefactoring.sumCase += caseMap.size();			
		}
		int caseOldNum = caseMap.size();
		boolean label = true;
		
		label = SwitchForBreak.checkBreak(caseMap, blockMap, flagArray);
		
		SwitchForYield.checkBranch(ast, caseMap, blockMap, caseAddLabel, e, label);
		//case连用的特殊情况，case与default连用，不重构
		int seriesCaseLabel = SwitchForSeriesCase.seriesCase(ast, caseMap, blockMap, label);
		if (seriesCaseLabel == 0 || seriesCaseLabel == -1) {
			if (!label && !SwitchRefactoring.ENDSWITCH1) {
				SwitchRefactoring.break_caseDefau ++;
			}
			if (!label && flagArray[0]) {
				SwitchRefactoring.ENDSWITCH3 = true;
			}
			SwitchRefactoring.CASEDE = true;
			SwitchRefactoring.sumCaseSeries += caseOldNum - caseMap.size();
			if (seriesCaseLabel == -1) {
				if (!SwitchRefactoring.ENDSWITCH1) {
					SwitchRefactoring.sumNotCaDe ++;
				}
				flagArray[0] = false;
				SwitchRefactoring.CASEDESW = true;
				SwitchForSeriesCase.seriesCase(caseMap, blockMap, false);
				category = -2;		
			} else if (!dealC_DLabel || !label) {
				if (label && SwitchRefactoring.ENDSWITCH3) {
					SwitchRefactoring.oldToNewLabel = true;
				}
				SwitchForSeriesCase.seriesCase(caseMap, blockMap, false);
				category = -2;				
			} else {
				//为处理的case-default连用，做数量统计
//				if (!SwitchRefactoring.ENDSWITCH1) {
//					SwitchRefactoring.sumDealCaDe++;
//				}
				SwitchRefactoring.oldToNewLabel = true;
				SwitchRefactoring.sumCategoryNew ++;
				SwitchForBreak.deleteBreak(blockMap);
				SwitchForSeriesCase.dealC_DMap(ast, caseMap, blockMap);
				category = SwitchForYield.yieldDetermine(blockMap, flagArray);
				if (category == 0 && SwitchRefactoring.deleteIfBreakLabel) {
					SwitchForBreak.deleteIfBreak(blockMap);
					SwitchRefactoring.deleteIfBreakLabel = false;
				}
				if (category == 0 && SwitchRefactoring.deleteTryBreakLabel) {
					SwitchForBreak.deleteTryBreak(blockMap);
					SwitchRefactoring.deleteTryBreakLabel = false;
				}
			}
			return category;
		}
		SwitchRefactoring.sumCaseSeries += caseOldNum - caseMap.size();
		if (!label) {
			category = -1;
		}else {
			SwitchRefactoring.oldToNewLabel = true;
			SwitchRefactoring.sumCategoryNew ++;
			SwitchForBreak.deleteBreak(blockMap);
			category = SwitchForYield.yieldDetermine(blockMap,flagArray);
			if (category == 0 && SwitchRefactoring.deleteIfBreakLabel) {
				SwitchForBreak.deleteIfBreak(blockMap);
				SwitchRefactoring.deleteIfBreakLabel = false;
			}
			if (category == 0 && SwitchRefactoring.deleteTryBreakLabel) {
				SwitchForBreak.deleteTryBreak(blockMap);
				SwitchRefactoring.deleteTryBreakLabel = false;
			}
		}
		return category;
	}
}
