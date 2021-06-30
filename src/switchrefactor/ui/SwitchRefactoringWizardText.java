package switchrefactor.ui;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

public class SwitchRefactoringWizardText extends RefactoringWizard {

	UserInputWizardPage page;
	//�ع�����
	public SwitchRefactoringWizardText(Refactoring refactoring) {
		super(refactoring , DIALOG_BASED_USER_INTERFACE);
	}

	@Override
	protected void addUserInputPages() {
		//�û���ƽ���
		page = new SwitchRefactoringWizardPage("SwitchRefactor");
		addPage(page);
	}
	
}