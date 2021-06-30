package switchrefactor.actions;

import java.io.IOException;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import switchrefactor.refactoring.SwitchRefactoring;
import switchrefactor.ui.SwitchRefactoringWizardText;

@SuppressWarnings("restriction")
public class SwitchRefactorText implements IEditorActionDelegate{

	private JavaEditor editor;
	private ITextSelection select;
	private int textLength = 0;
	private int textStartLine = 0;
	private String textContent = null;
	
	@Override
	public void run(IAction arg0) {
		textLength = select.getLength();
		textStartLine = select.getStartLine();
		textContent = select.getText();
		Shell shell = editor.getSite().getShell();
		
		IJavaElement element = SelectionConverter.getInputAsCompilationUnit(editor);
		
		if (textLength != 0 && textContent != null && element != null) {
			
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				page.showView("switchrefactor.DataView");
			} catch (PartInitException e1) {
				e1.printStackTrace();
			}
			
			SwitchRefactoring.textLength = textLength;
			SwitchRefactoring.textStartLine = textStartLine;
			SwitchRefactoring.textContent = String.copyValueOf(textContent.toCharArray(), 0, textContent.length());
			SwitchRefactoring.textRefactor = true;
			
			try {
				SwitchRefactoring refactoring = null;
				try {
					refactoring = new SwitchRefactoring(element);
				} catch (IllegalArgumentException | IOException e) {
					e.printStackTrace();
				}
				SwitchRefactoringWizardText refactoringWizard = new SwitchRefactoringWizardText(refactoring);
				RefactoringWizardOpenOperation openOperation = new RefactoringWizardOpenOperation(refactoringWizard);
				
				try {
					openOperation.run(shell, "SwitchRefactor");
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else {
			MessageDialog.openError(shell, "Error SwitchRefactor", "No switch was found");
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof ITextSelection) {
			select = (ITextSelection) selection;
		}
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof JavaEditor) {
			editor = (JavaEditor) targetEditor;
		}
	}

}
