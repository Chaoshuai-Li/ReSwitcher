package switchrefactor.refactoring;

import java.util.LinkedHashMap;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.YieldStatement;

public class SwitchPatternAnalysis {

	// switchÓï¾ä·Ö½â¶Á³ö´æ´¢£¬":"
	@SuppressWarnings("unchecked")
	public static void caseAnalysisOld(SwitchStatement switchStatement, AST ast,
			LinkedHashMap<SwitchCase, Integer> caseMap, LinkedHashMap<Integer, Block> blockMap) {
		int num = 1;

		for (int i = 0; i < switchStatement.statements().size();) {

			if (switchStatement.statements().get(i) instanceof SwitchCase) {
				SwitchCase switchcaseTemp = (SwitchCase) switchStatement.statements().get(i);
//				switchcaseTemp.delete();
				caseMap.put(switchcaseTemp, num);
				int j = 0;
				for (j = i + 1; j < switchStatement.statements().size(); j++) {
					if (switchStatement.statements().get(j) instanceof SwitchCase) {
						SwitchCase caseTemp = (SwitchCase) switchStatement.statements().get(j);
//						if (caseTemp.isDefault()) {
//							SwitchRefactoring.BREAK = true;
//						}
//						caseTemp.delete();
						caseMap.put(caseTemp, num);
					} else {
						break;
					}
				}
				i = j;
			}
			if (i >= switchStatement.statements().size()) {
				break;
			}
			if (switchStatement.statements().get(i) instanceof Block
					&& ((i + 1) >= switchStatement.statements().size())) {

				Block blockTemp = (Block) switchStatement.statements().get(i);

//				blockTemp.delete();

				if (blockTemp.statements().size() == 0) {
					num--;
				} else {
					blockMap.put(num, blockTemp);
				}
				i++;
			} else if (switchStatement.statements().get(i) instanceof Block
					&& switchStatement.statements().get(i + 1) instanceof SwitchCase) {
				Block blockTemp = (Block) switchStatement.statements().get(i);

//				blockTemp.delete();

				if (blockTemp.statements().size() == 0) {
					num--;
				} else {
					blockMap.put(num, blockTemp);
				}
				i++;
			} else {
				int j = i;
				Block blockTemp = ast.newBlock();

				while (!(switchStatement.statements().get(j) instanceof SwitchCase)) {

					Statement state = (Statement) switchStatement.statements().get(j);
					switchStatement.statements().remove(j);
					state.delete();
					if (state instanceof Block) {
						Block b = (Block) state;
						if (b.statements().size() == 0) {
							continue;
						}
					}
					blockTemp.statements().add(state);
					if (j >= switchStatement.statements().size()) {
						break;
					}
				}
				if (blockTemp.statements().size() != 0) {
					blockMap.put(num, blockTemp);
				} else {
					num--;
				}
				blockTemp.delete();
				i = j;
			}
			num++;
		}
	}

	// switchÓï¾ä·Ö½â¶Á³ö´æ´¢£¬"->"
	@SuppressWarnings("unchecked")
	public static void caseAnalysisNew(SwitchStatement switchStatement, AST ast,
			LinkedHashMap<SwitchCase, Integer> caseMap, LinkedHashMap<Integer, Block> blockMap) {
		int num = 1;

		for (int i = 0; i < switchStatement.statements().size();) {

			if (switchStatement.statements().get(i) instanceof SwitchCase) {
				SwitchCase switchcaseTemp = (SwitchCase) switchStatement.statements().get(i);
//				switchcaseTemp.delete();
				caseMap.put(switchcaseTemp, num);
			}
			i++;
			if (i >= switchStatement.statements().size()) {
				break;
			}
			if (switchStatement.statements().get(i) instanceof Block
					&& ((i + 1) >= switchStatement.statements().size())) {
				Block blockTemp = (Block) switchStatement.statements().get(i);

//				blockTemp.delete();
				blockMap.put(num, blockTemp);
				i++;
			} else if (switchStatement.statements().get(i) instanceof Block
					&& switchStatement.statements().get(i + 1) instanceof SwitchCase) {
				Block blockTemp = (Block) switchStatement.statements().get(i);

//				blockTemp.delete();
				blockMap.put(num, blockTemp);
				i++;
			} else {
				int j = i;
				Block blockTemp = ast.newBlock();

				while (!(switchStatement.statements().get(j) instanceof SwitchCase)) {

					Statement state = (Statement) switchStatement.statements().get(j);					
					switchStatement.statements().remove(j);
					state.delete();

					if (state instanceof YieldStatement && ((YieldStatement) state).isImplicit()) {
						Expression eTemp = ((YieldStatement) state).getExpression();
						state = blockTemp.getAST()
								.newExpressionStatement((Expression) ASTNode.copySubtree(blockTemp.getAST(), eTemp));
					}
					if (state instanceof Block) {
						Block b = (Block) state;
						if (b.statements().size() == 0) {
							continue;
						}
					}

					blockTemp.statements().add(state);

					if (j >= switchStatement.statements().size()) {
						break;
					}
				}

				if (blockTemp.statements().size() != 0) {
					blockMap.put(num, blockTemp);
				} else {
					num--;
				}
				blockTemp.delete();
				i = j;
			}
			num++;
		}
	}
}


//try {
//	blockTemp.statements().add(ASTNode.copySubtree(blockTemp.getAST(), state));
//} catch (Exception e) {
//	blockTemp = astTemp2.newBlock();
//	blockTemp.statements().add(ASTNode.copySubtree(blockTemp.getAST(), state));
//}
