package switchrefactor.ui;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

public class SwitchRefactoringWizardText extends RefactoringWizard {

	UserInputWizardPage page;
	//重构界面
	public SwitchRefactoringWizardText(Refactoring refactoring) {
		super(refactoring , DIALOG_BASED_USER_INTERFACE);
	}

	@Override
	protected void addUserInputPages() {
		//用户设计界面
		page = new SwitchRefactoringWizardPage("SwitchRefactor");
		addPage(page);
	}
	
}