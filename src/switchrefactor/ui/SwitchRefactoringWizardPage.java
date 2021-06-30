package switchrefactor.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import switchrefactor.refactoring.SwitchRefactoring;

public class SwitchRefactoringWizardPage extends UserInputWizardPage {

//	Button buttonCheck;
//	Label labelName;
//	Label labelName01;
//	Button buttonCheck1;
//	Label labelName11;
//	Label labelName12;
	Button buttonCheck2;
	Label labelName21;
//	Label labelName22;
//	Button buttonCheck3;
//	Label labelName31;
//	Label labelName32;
	Button buttonCheck4;
	Label labelName41;
//	Label labelName42;
	Button buttonCheck5;
	Label labelName51;
//	Label labelName52;

	Text textName;

	public SwitchRefactoringWizardPage(String name) {
		super(name);
	}

	@Override
	public void createControl(Composite arg0) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(arg0, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		// Check break
//		buttonCheck = new Button(composite, SWT.CHECK);
//		buttonCheck.setText("Count the missing position of 'break' in refactoring");
//		buttonCheck.setSelection(true);
//		GridData butData = new GridData();
//		butData.horizontalSpan = 2;
//		butData.horizontalAlignment = GridData.FILL;
//		buttonCheck.setLayoutData(butData);
//		labelName01 = new Label(composite, SWT.WRAP);
//		labelName01.setText("Check to see if the 'break' here is missing");
//		GridData labData01 = new GridData();
//		labData01.horizontalAlignment = GridData.BEGINNING;
//		labData01.grabExcessHorizontalSpace = true;
//		labelName01.setLayoutData(labData01);
//		labelName01.setEnabled(true);
//		labelName = new Label(composite, SWT.SINGLE | SWT.BORDER);
//		labelName.setText("			switch (j){\r\n"
//						+ "			        case 1 : i = 1;break;\r\n"
//						+ "			        case 2 : i = 2;\r\n"
//						+ "			        default : throw new IllegalStateException(\"Unexpected value : \" + j);\r\n"
//						+ "			        }");
//		GridData labData = new GridData();
//		labData.horizontalAlignment = GridData.END;
//		labData.grabExcessHorizontalSpace = true;
//		labelName.setLayoutData(labData);
//		labelName.setEnabled(false);

		// Check default
//		buttonCheck1 = new Button(composite, SWT.CHECK);
//		buttonCheck1.setText("Count the missing position of 'default' in refactoring");
//		buttonCheck1.setSelection(true);
//		GridData butData1 = new GridData();
//		butData1.horizontalSpan = 2;
//		butData1.horizontalAlignment = GridData.FILL;
//		buttonCheck1.setLayoutData(butData1);
//		labelName11 = new Label(composite, SWT.WRAP);
//		labelName11.setText("Check to see if the 'default' here is missing");
//		GridData labData11 = new GridData();
//		labData11.horizontalAlignment = GridData.BEGINNING;
//		labData11.grabExcessHorizontalSpace = true;
//		labelName11.setLayoutData(labData11);
//		labelName11.setEnabled(true);
//		labelName12 = new Label(composite, SWT.SINGLE | SWT.BORDER);
//		labelName12.setText( "			switch (j){\r\n"
//				+ "			        case 1 : i = 1;break;\r\n"
//				+ "			        case 2 : i = 2;\r\n"
//				+ "			        }");
//		GridData labData12 = new GridData();
//		labData12.horizontalAlignment = GridData.END;
//		labData12.grabExcessHorizontalSpace = true;
//		labelName12.setLayoutData(labData12);
//		labelName12.setEnabled(false);

		// Automatically add default
		buttonCheck2 = new Button(composite, SWT.CHECK);
		buttonCheck2.setText("Automatically add 'default' in refactoring");
		GridData butData2 = new GridData();
		butData2.horizontalSpan = 2;
		butData2.horizontalAlignment = GridData.FILL;
		buttonCheck2.setLayoutData(butData2);
		labelName21 = new Label(composite, SWT.WRAP);
		labelName21.setText("The automatically added content is : "
				+ "throw new IllegalStateException(\"Unexpected value : \" + expression)");
		GridData labData21 = new GridData();
		labData21.horizontalAlignment = GridData.BEGINNING;
		labData21.grabExcessHorizontalSpace = true;
		labelName21.setLayoutData(labData21);
		labelName21.setEnabled(false);
//		labelName22 = new Label(composite, SWT.SINGLE | SWT.BORDER);
//		labelName22.setText("			//Automatically add 'default' if the 'default' here is missing\r\n"
//				+ "			switch (j){\r\n"
//				+ "			        case 1 : i = 1;break;\r\n"
//				+ "			        case 2 : i = 2;break;\r\n"
//				+ "			        default : throw new IllegalStateException(\"Unexpected value : \" + j);\r\n"
//				+ "			        }");
//		GridData labData22 = new GridData();
//		labData22.horizontalAlignment = GridData.END;
//		labData22.grabExcessHorizontalSpace = true;
//		labelName22.setLayoutData(labData22);
//		labelName22.setEnabled(false);

		// Case branch is missing
//		buttonCheck3 = new Button(composite, SWT.CHECK);
//		buttonCheck3.setText("Count the position of missing statement executed after branch in refactoring");
//		buttonCheck3.setSelection(true);
//		GridData butData3 = new GridData();
//		butData3.horizontalSpan = 2;
//		butData3.horizontalAlignment = GridData.FILL;
//		buttonCheck3.setLayoutData(butData3);
//		labelName31 = new Label(composite, SWT.WRAP);
//		labelName31.setText("Check to see if the 'statement executed after branch' here is missing");
//		GridData labData31 = new GridData();
//		labData31.horizontalAlignment = GridData.BEGINNING;
//		labData31.grabExcessHorizontalSpace = true;
//		labelName31.setLayoutData(labData31);
//		labelName31.setEnabled(true);
//		labelName32 = new Label(composite, SWT.SINGLE | SWT.BORDER);
//		labelName32.setText("			switch (j){\r\n"
//				+ "			        case 1 : i = 1;break;\r\n"
//				+ "			        case 2 :\r\n"
//				+ "			        }");
//		GridData labData32 = new GridData();
//		labData32.horizontalAlignment = GridData.END;
//		labData32.grabExcessHorizontalSpace = true;
//		labelName32.setLayoutData(labData32);
//		labelName32.setEnabled(false);

		// Automatically add execution statements
		buttonCheck4 = new Button(composite, SWT.CHECK);
		buttonCheck4.setText("Automatically add execution statements in refactoring");
		GridData butData4 = new GridData();
		butData4.horizontalSpan = 2;
		butData4.horizontalAlignment = GridData.FILL;
		buttonCheck4.setLayoutData(butData4);
		labelName41 = new Label(composite, SWT.WRAP);
		labelName41.setText("The automatically added content is : "
				+ "throw new IllegalStateException(\"Unexpected value : \" + expression)");
		GridData labData41 = new GridData();
		labData41.horizontalAlignment = GridData.BEGINNING;
		labData41.grabExcessHorizontalSpace = true;
		labelName41.setLayoutData(labData41);
		labelName41.setEnabled(false);
//		labelName42 = new Label(composite, SWT.SINGLE | SWT.BORDER);
//		labelName42.setText("			switch (j){\r\n"
//				+ "			        case 1 : i = 1;break;\r\n"
//				+ "			        case 2 : throw new IllegalStateException(\"Unexpected value : \" + j);\r\n"
//				+ "			        }");
//		GridData labData42 = new GridData();
//		labData42.horizontalAlignment = GridData.END;
//		labData42.grabExcessHorizontalSpace = true;
//		labelName42.setLayoutData(labData42);
//		labelName42.setEnabled(false);

//		labelName = new Label(composite, SWT.WRAP);
//		labelName.setText("Style constant");
//		GridData labData = new GridData();
//		labData.horizontalAlignment = GridData.BEGINNING;
//		labData.grabExcessHorizontalSpace = true;
//		labelName.setLayoutData(labData);
//		textName = new Text(composite, SWT.SINGLE | SWT.BORDER);
//		GridData textData = new GridData();
//		textData.horizontalAlignment = GridData.END;
//		textData.grabExcessHorizontalSpace = true;
//		textName.setLayoutData(textData);
//		textName.setText("Check to see if the 'break' here is missing");
//		textName.setEnabled(false);		

		buttonCheck5 = new Button(composite, SWT.CHECK);
		buttonCheck5.setText("Automatically separate when the Case-Default is connected");
		buttonCheck5.setSelection(true);
		GridData butData5 = new GridData();
		butData5.horizontalSpan = 2;
		butData5.horizontalAlignment = GridData.FILL;
		buttonCheck5.setLayoutData(butData5);
		labelName51 = new Label(composite, SWT.WRAP);
		labelName51.setText("Separate Case-Default by copying Block after SwitchCase");
		GridData labData51 = new GridData();
		labData51.horizontalAlignment = GridData.BEGINNING;
		labData51.grabExcessHorizontalSpace = true;
		labelName51.setLayoutData(labData51);
		labelName51.setEnabled(true);

		defineListener();
		setControl(composite);
		Dialog.applyDialogFont(composite);

		setTitle("Select The Condition");
		setDescription("Choose carefully as required, " + "and you can't re-select after you go to the next step.");
	}

	private void defineListener() {
		// TODO Auto-generated method stub
		SwitchRefactoring refactor = (SwitchRefactoring) getRefactoring();

		buttonCheck5.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				if (buttonCheck5.getEnabled()) {
					if (refactor.dealC_DLabel == false) {
						refactor.dealC_DLabel = true;
					} else {
						refactor.dealC_DLabel = false;
					}
				}
				if (refactor.dealC_DLabel == true) {
					labelName51.setEnabled(true);
				} else {
					labelName51.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				buttonCheck5.setEnabled(true);
			}
		});

		buttonCheck4.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				if (buttonCheck4.getEnabled()) {
					if (refactor.branchAddLabel == false) {
						refactor.branchAddLabel = true;
					} else {
						refactor.branchAddLabel = false;
					}
				}
				if (refactor.branchAddLabel == true) {
					labelName41.setEnabled(true);
				} else {
					labelName41.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				buttonCheck4.setEnabled(false);
			}
		});

//		buttonCheck3.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent arg0) {
//				// TODO Auto-generated method stub
//				if (buttonCheck3.getEnabled()) {
//					if (refactor.branchMissLabel == false) {
//						refactor.branchMissLabel = true;
//					}else {
//						refactor.branchMissLabel = false;
//					}
//				}
//				if (refactor.branchMissLabel == true) {
//					labelName31.setEnabled(true);
//				}else {
//					labelName31.setEnabled(false);
//				}
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent arg0) {
//				// TODO Auto-generated method stub
//				buttonCheck3.setEnabled(true);
//			}
//		});

		buttonCheck2.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				if (buttonCheck2.getEnabled()) {
					if (refactor.defaultAddLabel == false) {
						refactor.defaultAddLabel = true;
					} else {
						refactor.defaultAddLabel = false;
					}
				}
				if (refactor.defaultAddLabel == true) {
					labelName21.setEnabled(true);
				} else {
					labelName21.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				buttonCheck2.setEnabled(false);
			}
		});

//		buttonCheck1.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent arg0) {
//				// TODO Auto-generated method stub
//				if (buttonCheck1.getEnabled()) {
//					if (refactor.defaultMissLabel == false) {
//						refactor.defaultMissLabel = true;
//					}else {
//						refactor.defaultMissLabel = false;
//					}
//				}
//				if (refactor.defaultMissLabel == true) {
//					labelName11.setEnabled(true);
//				}else {
//					labelName11.setEnabled(false);
//				}
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent arg0) {
//				// TODO Auto-generated method stub
//				buttonCheck1.setEnabled(true);
//			}
//		});

//		buttonCheck.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent arg0) {
//				// TODO Auto-generated method stub
//				if (buttonCheck.getEnabled()) {
//					if (refactor.breakMissLabel == false) {
//						refactor.breakMissLabel = true;
////						textName.setEnabled(true);
//					}else {
//						refactor.breakMissLabel = false;
////						textName.setEnabled(false);
//					}
//				}
//				if (refactor.breakMissLabel == true) {
//					labelName01.setEnabled(true);
//				}else {
//					labelName01.setEnabled(false);
//				}
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent arg1) {
//				// TODO Auto-generated method stub
//				buttonCheck.setEnabled(true);
//			}
//		});
	}

}

//		textName.addModifyListener(new ModifyListener() {
//			
//			@Override
//			public void modifyText(ModifyEvent arg0) {
//				// TODO Auto-generated method stub
//				refactor.breakMissText = textName.getText();
//			}
//		});