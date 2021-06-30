package switchrefactor.views;

import java.util.List;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import switchrefactor.datalog.AddressRecord;
import switchrefactor.datalog.DataLogList;
import switchrefactor.refactoring.SwitchRefactoring;

public class CaseDefaultView extends ViewPart{

	private static TableViewer tableViewer;
	
	public static void updateTableViewer() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (tableViewer != null) {
					tableViewer.refresh();
				}
			}
		});
	}
	
	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.FULL_SELECTION);
		
		Table table = tableViewer.getTable();
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		
		layout.addColumnData(new ColumnWeightData(20));
		new TableColumn(table, SWT.NONE).setText("Location");
		
		layout.addColumnData(new ColumnWeightData(20));
		new TableColumn(table, SWT.NONE).setText("Class Name");
		
		layout.addColumnData(new ColumnWeightData(20));
		new TableColumn(table, SWT.NONE).setText("Method Name");
		
		layout.addColumnData(new ColumnWeightData(20));
		new TableColumn(table, SWT.NONE).setText("Switch Expression");
		
		tableViewer.setContentProvider(new TableViewContentProvider());
		tableViewer.setLabelProvider(new TableViewLabelProvider());
		tableViewer.setInput(DataLogList.getCaseDefaultInformation());
	}

	class TableViewLabelProvider implements ITableLabelProvider{

		@Override
		public void addListener(ILabelProviderListener arg0) {
			// TODO Auto-generated method stub
			tableViewer.addDoubleClickListener(new IDoubleClickListener() {
				
				@Override
				public void doubleClick(DoubleClickEvent event) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					AddressRecord ar = (AddressRecord)selection.getFirstElement();
//					MessageDialog.openInformation(null, "ÌבÊ¾", dr.getName());
					try {
						IEditorPart jEditorPart = JavaUI.openInEditor(ar.getJavaElement());
						 ITextEditor editor= (ITextEditor)jEditorPart;
						 editor.selectAndReveal(ar.getStartPoint(), 6);
					} catch (PartInitException | JavaModelException e) {
						e.printStackTrace();
					}
				}
			});
			
			tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

				@Override
				public void selectionChanged(SelectionChangedEvent selectTemp) {
					MenuManager menuManager = new MenuManager();
					menuManager.add(new Action("Hide this View") {
						@Override
						public void run() {
							if (SwitchRefactoring.page != null) {
								IWorkbenchPage page = SwitchRefactoring.page;
								if (SwitchRefactoring.vCaDe != null) {
									page.hideView(SwitchRefactoring.vCaDe);
								}
							}

						}
					});
					menuManager.add(new Action("Hide all Views") {
						@Override
						public void run() {
							if (SwitchRefactoring.page != null) {
								IWorkbenchPage page = SwitchRefactoring.page;
								if (SwitchRefactoring.vBreak != null) {
									page.hideView(SwitchRefactoring.vBreak);
								}
								if (SwitchRefactoring.vDefault != null) {
									page.hideView(SwitchRefactoring.vDefault);
								}
								if (SwitchRefactoring.vBranch != null) {
									page.hideView(SwitchRefactoring.vBranch);
								}
								if (SwitchRefactoring.vCaDe != null) {
									page.hideView(SwitchRefactoring.vCaDe);
								}
								if (SwitchRefactoring.vData != null) {
									page.hideView(SwitchRefactoring.vData);
								}
							}
						}
					});
					Menu menu = menuManager.createContextMenu(tableViewer.getControl());
					tableViewer.getControl().setMenu(menu);
				}
			});
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isLabelProperty(Object arg0, String arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Image getColumnImage(Object arg0, int arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getColumnText(Object element, int columns) {
			
			AddressRecord ar = (AddressRecord) element;
			
			if (columns == 0) {
				return ar.getLocation();
			}
			
			if (columns == 1) {
				return ar.getClassName();
			}
			
			if (columns == 2) {
				return ar.getMethodName();
			}
			
			if (columns == 3) {
				return ar.getSwitchExpression();
			}
			
			return "";
		}
		
	}
	
	class TableViewContentProvider implements IStructuredContentProvider{

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof List) {
				return ((List<?>)inputElement).toArray();
			}else {
				return new Object[0];
			}
		}
		
		@Override
		public void dispose() {
			
		}
		
		 @Override
		 public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		 }
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
}
