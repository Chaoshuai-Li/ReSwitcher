package switchrefactor.actions;

import java.io.IOException;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import switchrefactor.refactoring.SwitchRefactoring;
import switchrefactor.ui.SwitchRefactoringWizard;

public class SwitchRefactorActions implements IWorkbenchWindowActionDelegate{

	IWorkbenchWindow windows;
	IJavaElement select;
	
	//…œ∑Ω≤Àµ•¿∏
	public SwitchRefactorActions() {
		// TODO Auto-generated constructor stub
	}
	
	public SwitchRefactorActions(IWorkbenchWindow window) {
		this.windows = window;
	}
	
	@Override
	public void run(IAction action) {
		
		IWorkbenchPage page = windows.getActivePage();
		
		try {
			if (action.getId().equals("switchrefactor.actions.SwitchRefactorActions")) {
				Shell shell = windows.getShell();
				
				IViewPart v1 = page.showView("switchrefactor.BreakView");
				IViewPart v2 = page.showView("switchrefactor.DefaultView");
				IViewPart v3 = page.showView("switchrefactor.BranchView");
				IViewPart v4 = page.showView("switchrefactor.CaseDefaultView");
				IViewPart v5 = page.showView("switchrefactor.DataView");
				page.hideView(v5);
				IViewPart v6 = page.showView("switchrefactor.DataView");
				
				SwitchRefactoring.page = page;
				SwitchRefactoring.vBreak = v1;
				SwitchRefactoring.vDefault = v2;
				SwitchRefactoring.vBranch = v3;
				SwitchRefactoring.vCaDe = v4;
				SwitchRefactoring.vData = v6;
				
				SwitchRefactoring refactoring = null;
				try {
					refactoring = new SwitchRefactoring(select);
				} catch (IllegalArgumentException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				SwitchRefactoringWizard refactoringWizard = new SwitchRefactoringWizard(refactoring);
				RefactoringWizardOpenOperation openOperation = new RefactoringWizardOpenOperation(refactoringWizard);
				
				try {
					openOperation.run(shell, "SwitchRefactor");
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			}else if (action.getId().equals("switchrefactor.actions.Break")) {
				page.showView("switchrefactor.BreakView");
			}else if (action.getId().equals("switchrefactor.actions.Default")) {
				page.showView("switchrefactor.DefaultView");
			}else if (action.getId().equals("switchrefactor.actions.Branch")) {
				page.showView("switchrefactor.BranchView");
			}else if (action.getId().equals("switchrefactor.actions.Information")) {
				page.showView("switchrefactor.DataView");
			}else if (action.getId().equals("switchrefactor.actions.CaseDefault")) {
				page.showView("switchrefactor.CaseDefaultView");
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
		// TODO Auto-generated method stub
		if (arg1.isEmpty()) {
			select = null;
		}else if (arg1 instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) arg1;
			if (structuredSelection.size() != 1) {
				select = null;
			}
			if (structuredSelection.getFirstElement() instanceof IJavaElement) {
				select = (IJavaElement)structuredSelection.getFirstElement();
			}
		}else {
			select = null;
		}
		arg0.setEnabled(select != null);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IWorkbenchWindow arg0) {
		// TODO Auto-generated method stub
		this.windows = arg0;
	}
	
}
