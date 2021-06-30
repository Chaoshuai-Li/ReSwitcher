package switchrefactor.views;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnWeightData;
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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.ViewPart;

import switchrefactor.datalog.DataLogList;
import switchrefactor.datalog.DataRecord;
import switchrefactor.refactoring.SwitchRefactoring;

public class DataView extends ViewPart {

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
		tableViewer = new TableViewer(parent,
				SWT.MULTI | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.FULL_SELECTION);

		Table table = tableViewer.getTable();

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);

		layout.addColumnData(new ColumnWeightData(65));
		new TableColumn(table, SWT.NONE).setText("Program Name");

		layout.addColumnData(new ColumnWeightData(15));
		new TableColumn(table, SWT.NONE).setText("Switch Num");

		layout.addColumnData(new ColumnWeightData(15));
		new TableColumn(table, SWT.NONE).setText("Old Rule");

		layout.addColumnData(new ColumnWeightData(15));
		new TableColumn(table, SWT.NONE).setText("New Rule");

		layout.addColumnData(new ColumnWeightData(15));
		new TableColumn(table, SWT.NONE).setText("None");

		layout.addColumnData(new ColumnWeightData(15));
		new TableColumn(table, SWT.NONE).setText("Method");

		layout.addColumnData(new ColumnWeightData(15));
		new TableColumn(table, SWT.NONE).setText("Assignment");

		layout.addColumnData(new ColumnWeightData(15));
		new TableColumn(table, SWT.NONE).setText("Return");

		layout.addColumnData(new ColumnWeightData(15));
		new TableColumn(table, SWT.NONE).setText("Default Miss");

		layout.addColumnData(new ColumnWeightData(15));
		new TableColumn(table, SWT.NONE).setText("Branch Miss");

		layout.addColumnData(new ColumnWeightData(15));
		new TableColumn(table, SWT.NONE).setText("Break Miss");

		layout.addColumnData(new ColumnWeightData(15));
		new TableColumn(table, SWT.NONE).setText("Case & Default");

		tableViewer.setContentProvider(new TableViewContentProvider());
		tableViewer.setLabelProvider(new TableViewLabelProvider());
		tableViewer.setInput(DataLogList.getDataRecords());
	}

	class TableViewLabelProvider implements ITableLabelProvider {

		@Override
		public void addListener(ILabelProviderListener arg0) {
			tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

				@Override
				public void selectionChanged(SelectionChangedEvent selectTemp) {
					MenuManager menuManager = new MenuManager();
					menuManager.add(new Action("Delete this") {
						@Override
						public void run() {
							IStructuredSelection selection = (IStructuredSelection) selectTemp.getSelection();
							DataRecord dr = (DataRecord) selection.getFirstElement();
							DataLogList.removeList(dr);
							updateTableViewer();
						}
					});
					menuManager.add(new Action("Delete all") {
						@Override
						public void run() {
							DataLogList.removeListDataRecordAll();
							updateTableViewer();
						}
					});
					menuManager.add(new Separator());
					menuManager.add(new Action("Hide this View") {
						@Override
						public void run() {
							if (SwitchRefactoring.page != null) {
								IWorkbenchPage page = SwitchRefactoring.page;
								if (SwitchRefactoring.vData != null) {
									page.hideView(SwitchRefactoring.vData);
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

			DataRecord dr = (DataRecord) element;

			if (columns == 0) {
				return dr.getName();
			}

			if (columns == 1) {
				return dr.getSumSwitch() + "";
			}

			if (columns == 2) {
				return dr.getSumOldRule() + "";
			}

			if (columns == 3) {
				return dr.getSumNewRule() + "";
			}

			if (columns == 4) {
				return dr.getRefactorNone() + "";
			}

			if (columns == 5) {
				return dr.getRefactorMethod() + "";
			}

			if (columns == 6) {
				return dr.getRefactorAssign() + "";
			}

			if (columns == 7) {
				return dr.getRefactorReturn() + "";
			}

			if (columns == 8) {
				return dr.getDefaultMiss() + "";
			}

			if (columns == 9) {
				return dr.getBranchMiss() + "";
			}

			if (columns == 10) {
				return dr.getBreakMiss() + "";
			}

			if (columns == 11) {
				return dr.getCaseDefault() + "";
			}

			return "";
		}

	}

	class TableViewContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof List) {
				return ((List<?>) inputElement).toArray();
			} else {
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
