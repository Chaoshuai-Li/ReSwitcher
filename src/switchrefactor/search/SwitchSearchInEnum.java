package switchrefactor.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import switchrefactor.datalog.DataLogList;
import switchrefactor.refactoring.SwitchRefactoring;

public class SwitchSearchInEnum {
	String pathName;
	EnumDeclaration ed;
	AST ast;
	boolean defaultAddLabel;
	boolean branchAddLabel;
	boolean dealC_DLabel;
	ASTRewrite astr;

	// 数据初始化，递归遍历算法设计，找到所有的switch并重构
	public SwitchSearchInEnum(String pathName, EnumDeclaration ed, AST ast, boolean defaultAddLabel,
			boolean branchAddLabel, boolean dealC_DLabel, ASTRewrite astr) {
		this.pathName = pathName;
		this.ed = ed;
		this.ast = ast;
		this.defaultAddLabel = defaultAddLabel;
		this.branchAddLabel = branchAddLabel;
		this.dealC_DLabel = dealC_DLabel;
		this.astr = astr;
	}

	public void traverseType() {
			List<SwitchStatement> switchs = new ArrayList<>();
			findSwitchs(ed, switchs);
			for (SwitchStatement ss : switchs) {
				SwitchSearchRefactoring ssr = new SwitchSearchRefactoring(pathName + "/" + ed.getName(), null, 
						ast, null, defaultAddLabel, branchAddLabel, dealC_DLabel);
				Statement sTemp = ssr.switchSearchRefactor(ss);
				
				SwitchRefactoring.ENDSWITCH1 = false;
				SwitchRefactoring.ENDSWITCH2 = false;
				SwitchRefactoring.ENDSWITCH3 = false;
				SwitchRefactoring.oldToNewLabel = false;
				if (SwitchRefactoring.rebackOldSwitch != 0 
						&& SwitchRefactoring.breakMissList.size() >= SwitchRefactoring.rebackOldSwitch) {
					for(int j = 0; j < SwitchRefactoring.rebackOldSwitch; j++) {
						SwitchRefactoring.breakMissList.remove(SwitchRefactoring.breakMissList.size() - 1);
						DataLogList.listBreakRecordReback();
					}
					SwitchRefactoring.rebackOldSwitch = 0;
				}
				
				astr.replace(ss, sTemp, null);
			}
	}

	private void findSwitchs(ASTNode root, List<SwitchStatement> switchs) {
		root.accept(new ASTVisitor() {
			public boolean visit(SwitchStatement node) {
				switchs.add(node);
				return false;
			}
		});
	}
}
