package switchrefactor.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import switchrefactor.datalog.DataLogList;
import switchrefactor.refactoring.SwitchRefactoring;

public class SwitchSearchInType {
	String pathName;
	TypeDeclaration td;
	AST ast;
	boolean defaultAddLabel;
	boolean branchAddLabel;
	boolean dealC_DLabel;
	
	ArrayList<SwitchStatement> listTemp = new ArrayList<>();
	
	ASTRewrite astr;

	// 数据初始化，递归遍历算法设计，找到所有的switch并重构
	public SwitchSearchInType(String pathName, TypeDeclaration td, AST ast, boolean defaultAddLabel,
			boolean branchAddLabel, boolean dealC_DLabel, ASTRewrite astr, ArrayList<SwitchStatement> listTemp) {
		this.pathName = pathName;
		this.td = td;
		this.ast = ast;
		this.defaultAddLabel = defaultAddLabel;
		this.branchAddLabel = branchAddLabel;
		this.dealC_DLabel = dealC_DLabel;
		this.astr = astr;
		this.listTemp = listTemp;
	}

	public void traverseType() {
		MethodDeclaration[] methods = td.getMethods();
		
		for (MethodDeclaration m : methods) {
			List<SwitchStatement> switchs = new ArrayList<>();
			findSwitchs(m, switchs);
			for (SwitchStatement ss : switchs) {
				SwitchSearchRefactoring ssr = new SwitchSearchRefactoring(pathName, td, 
						ast, m, defaultAddLabel, branchAddLabel, dealC_DLabel);
				if (listTemp.contains(ss)) {
					listTemp.remove(ss);
				}
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

		TypeDeclaration[] typesTemp = td.getTypes();
		if (typesTemp.length != 0) {
			for (TypeDeclaration tdTemp : typesTemp) {
				SwitchSearchInType ssi = new SwitchSearchInType(pathName + "\\" + td.getName(), tdTemp, ast, defaultAddLabel, branchAddLabel, dealC_DLabel, astr, listTemp);
				ssi.traverseType();
			}
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
