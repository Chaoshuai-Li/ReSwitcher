package switchrefactor.ui;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

public class SwitchRefactoringWizard extends RefactoringWizard {

	UserInputWizardPage page;
	//�ع�����
	public SwitchRefactoringWizard(Refactoring refactoring) {
		super(refactoring , WIZARD_BASED_USER_INTERFACE);
	}

	@Override
	protected void addUserInputPages() {
		//�û���ƽ���
		page = new SwitchRefactoringWizardPage("SwitchRefactor");
		addPage(page);
	}
	
}
