package switchrefactor.actions;

import java.io.IOException;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import switchrefactor.refactoring.SwitchRefactoring;
import switchrefactor.ui.SwitchRefactoringWizard;

public class SwitchRefactorMenu implements IObjectActionDelegate {

	IJavaElement select;

	//ÓÒ¼ü²Ëµ¥À¸
	@Override
	public void run(IAction action) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		try {
			if (action.getId().equals("switchrefactor.actions.SwitchRefactorMenu")) {
				Shell shell = new Shell();
				shell.setSize(500, 150);

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

			} else if (action.getId().equals("switchrefactor.actions.break")) {
				page.showView("switchrefactor.BreakView");
			} else if (action.getId().equals("switchrefactor.actions.default")) {
				page.showView("switchrefactor.DefaultView");
			} else if (action.getId().equals("switchrefactor.actions.branch")) {
				page.showView("switchrefactor.BranchView");
			} else if (action.getId().equals("switchrefactor.actions.information")) {
				page.showView("switchrefactor.DataView");
			} else if (action.getId().equals("switchrefactor.actions.CaseDefault")) {
				page.showView("switchrefactor.CaseDefaultView");
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection.isEmpty())
			select = null;
		else if (selection instanceof IStructuredSelection) {
			IStructuredSelection strut = ((IStructuredSelection) selection);
			if (strut.size() != 1)
				select = null;
			if (strut.getFirstElement() instanceof IJavaElement)
				select = (IJavaElement) strut.getFirstElement();
		} else {
			select = null;
		}
//		action.setEnabled(true);
		action.setEnabled(select != null);
	}

	@Override
	public void setActivePart(IAction arg0, IWorkbenchPart arg1) {
		// TODO Auto-generated method stub

	}

}
