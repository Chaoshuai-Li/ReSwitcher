package switchrefactor.refactoring;

import java.util.LinkedHashMap;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.SwitchCase;

public class SwitchForNew {
	//条件分支连接符为"->"
	public static int refactorSwitchForNew(AST ast, 
			LinkedHashMap<SwitchCase, Integer> caseMap, LinkedHashMap<Integer, Block> blockMap, boolean[] flagArray) {
		int category;
		if (!SwitchRefactoring.ENDSWITCH1) {
			SwitchRefactoring.sumCase += caseMap.size();			
		}
		SwitchRefactoring.sumCategoryNew ++;
		SwitchForBreak.checkEndOfBlock(blockMap, flagArray);
		SwitchForBreak.deleteBreak(blockMap);
		category = SwitchForYield.yieldDetermine(blockMap, flagArray);
		return category;
	}
}
