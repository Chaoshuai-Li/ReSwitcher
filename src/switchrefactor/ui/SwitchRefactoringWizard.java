package switchrefactor.ui;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

public class SwitchRefactoringWizard extends RefactoringWizard {

	UserInputWizardPage page;
	//重构界面
	public SwitchRefactoringWizard(Refactoring refactoring) {
		super(refactoring , WIZARD_BASED_USER_INTERFACE);
	}

	@Override
	protected void addUserInputPages() {
		//用户设计界面
		page = new SwitchRefactoringWizardPage("SwitchRefactor");
		addPage(page);
	}
	
}
