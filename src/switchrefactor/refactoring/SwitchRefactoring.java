package switchrefactor.refactoring;

import java.io.IOException;

import java.util.ArrayList;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.ui.fix.StaticInnerClassCleanUp;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;

import switchrefactor.datalog.DataLogList;
import switchrefactor.datalog.DataRecord;
import switchrefactor.search.SwitchSearchInEnum;
import switchrefactor.search.SwitchSearchInType;
import switchrefactor.search.SwitchSearchRefactoring;
import switchrefactor.views.BranchView;
import switchrefactor.views.BreakView;
import switchrefactor.views.CaseDefaultView;
import switchrefactor.views.DataView;
import switchrefactor.views.DefaultView;

import java.util.List;

public class SwitchRefactoring extends Refactoring {

	private static long now = 0;
	
	public static IWorkbenchPage page = null;
	public static IViewPart vBreak = null;
	public static IViewPart vDefault = null;
	public static IViewPart vBranch = null;
	public static IViewPart vCaDe = null;
	public static IViewPart vData = null;
	
	public static ASTRewrite rewrite = null;
	public static AST astFlag = null;
	public CompilationUnit arTemp = null;

	private IJavaElement element;
	List<Change> changeManager = new ArrayList<Change>();
	private List<ICompilationUnit> compilationUnits;
	private int[] numTemp = new int[11];
	// 所要重构程序的路径
	static IPath filename;

	// 选中文本重构
	public static int textLength = 0;
	public static int textStartLine = 0;
	public static String textContent = null;
	public static boolean textRefactor = false;
	private boolean textLabel1 = false;
	private boolean textLabel2 = false;

	public static IJavaElement jTemp = null;

	// 标志变量，影响重构结果
//	public boolean breakMissLabel = true;
//	public boolean defaultMissLabel = true;
//	public boolean branchMissLabel = true;
	public boolean defaultAddLabel = false;
	public boolean branchAddLabel = false;
	public boolean dealC_DLabel = true;

	public static boolean BREAK = false;
	public static boolean DEFAULT = false;
	public static boolean BRANCH = false;
	public static boolean CASEDE = false;
	public static boolean CASEDESW = false;
	public static boolean REFAC = true;
	public static boolean ENDSWITCH1 = false;
	public static boolean ENDSWITCH2 = false;
	public static boolean ENDSWITCH3 = false;
	public static boolean oldToNewLabel = false;
	
	public static boolean deleteIfBreakLabel = false;
	public static boolean deleteTryBreakLabel = false;
	
	public static boolean llllllabel = false;

	// 重构信息记录
	public static int sumSwitch = 0;
	public static int sumCaseDefault = 0;
	public static int sumCategoryOne = 0;
	public static int sumCategoryTwo = 0;
	public static int sumCategoryThree = 0;
	public static int sumCategoryZero = 0;
	public static int sumCategoryNew = 0;
	public static int sumCase = 0;
	public static int sumCaseSeries = 0;
	public static int sumBranch = 0;
	public static int sumDefault = 0;
	public static int sumDealCaDe = 0;
	public static int sumNotCaDe = 0;
	
	public static int sumValueExpression = 0;

	public static int sumNewEndSwitchTemp = 0;
	public static int rebackOldSwitch = 0;
	public static int break_caseDefau = 0;

	// 记录影响重构的因素缺失位置
	public static ArrayList<String> breakMissList = new ArrayList<String>();
	public static ArrayList<String> defaultMissList = new ArrayList<String>();
	public static ArrayList<String> branchMissList = new ArrayList<String>();
	public static ArrayList<String> caseDefaultList = new ArrayList<String>();

	public static boolean caseMiss = false;

	public static String programName = "";

	public SwitchRefactoring(IJavaElement select) throws IllegalArgumentException, IOException {

		BREAK = false;
		DEFAULT = false;
		BRANCH = false;
		CASEDE = false;
		CASEDESW = false;
		REFAC = true;
		ENDSWITCH1 = false;
		ENDSWITCH2 = false;
		ENDSWITCH3 = false;
		oldToNewLabel = false;
		
		deleteIfBreakLabel = false;
		deleteTryBreakLabel = false;

		sumSwitch = 0;
		sumCaseDefault = 0;
		sumCategoryOne = 0;
		sumCategoryTwo = 0;
		sumCategoryThree = 0;
		sumCategoryZero = 0;
		sumCategoryNew = 0;
		sumCase = 0;
		sumCaseSeries = 0;
		sumNewEndSwitchTemp = 0;
		break_caseDefau = 0;
		rebackOldSwitch = 0;

		sumBranch = 0;
		sumDefault = 0;
		sumDealCaDe = 0;
		sumNotCaDe = 0;
		
		sumValueExpression = 0;

		breakMissList = new ArrayList<String>();
		defaultMissList = new ArrayList<String>();
		branchMissList = new ArrayList<String>();
		caseDefaultList = new ArrayList<String>();

		caseMiss = false;

		textLabel1 = false;
		textLabel2 = false;

		DataLogList.listClear();
		
		now = 0;

		// 以上为插件重复启动的初始化

		filename = select.getJavaProject().getProject().getLocation();

		System.out.println("文件名：" + filename.toString());
	
		element = select;
		compilationUnits = findAllCompilationUnit(element);
	}

	private List<ICompilationUnit> findAllCompilationUnit(IJavaElement project) {
		List<ICompilationUnit> cUnit = new ArrayList<ICompilationUnit>();

		// 根据选中文件，决定重构文件
		try {
			if (project instanceof IJavaProject) {
				IJavaProject ip = project.getJavaProject();
				programName = "[JavaProject]" + project.getJavaProject().getProject().getName();
				for (IJavaElement element : ip.getChildren()) {
					IPackageFragmentRoot root = (IPackageFragmentRoot) element;
					for (IJavaElement ele : root.getChildren()) {
						if (ele instanceof IPackageFragment) {
							IPackageFragment fragment = (IPackageFragment) ele;
							for (ICompilationUnit unit : fragment.getCompilationUnits()) {
								cUnit.add(unit);
							}
						}
					}
				}
			} else if (project instanceof IPackageFragmentRoot) {
				IPackageFragmentRoot root = (IPackageFragmentRoot) project;
				programName = "[PackageFragmentRoot]" + root.getPath().toString();
				for (IJavaElement ele : root.getChildren()) {
					if (ele instanceof IPackageFragment) {
						IPackageFragment fragment = (IPackageFragment) ele;
						for (ICompilationUnit unit : fragment.getCompilationUnits()) {
							cUnit.add(unit);
						}
					}
				}
			} else if (project instanceof IPackageFragment) {
				IPackageFragment fragment = (IPackageFragment) project;
				programName = "[PackageFragment]" + fragment.getPath().toString();
				for (ICompilationUnit unit : fragment.getCompilationUnits()) {
					cUnit.add(unit);
				}
			} else if (project instanceof ICompilationUnit) {
				ICompilationUnit unit = (ICompilationUnit) project;
				programName = "[CompilationUnit]" + unit.getPath().toString();
				cUnit.add(unit);
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return cUnit;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor arg0)
			throws CoreException, OperationCanceledException {

		try {
			collectChanges();
		} catch (JavaModelException | IllegalArgumentException | IOException e) {
			e.printStackTrace();
		}

		if (textLabel1 == true && textLabel2 == false) {
			return RefactoringStatus.createFatalErrorStatus("Not switch was selected!");
		}
		if (changeManager.size() == 0 || sumSwitch == 0) {
			return RefactoringStatus.createFatalErrorStatus("Not switch was found!");
		} else {
			// 信息统计并反馈于view
			if (dealC_DLabel) {
				sumDealCaDe = caseDefaultList.size() - break_caseDefau;
			} else {
				sumDealCaDe = 0;
			}
			sumCategoryNew += sumNewEndSwitchTemp;
			DataRecord dr = new DataRecord(programName, sumSwitch, sumSwitch - sumCategoryNew, sumCategoryNew,
					sumSwitch - (sumCategoryOne + sumCategoryTwo + sumCategoryThree), sumCategoryOne, sumCategoryTwo,
					sumCategoryThree, defaultMissList.size(), branchMissList.size(), breakMissList.size(),
					caseDefaultList.size());

			DataLogList.DataFactory(dr);

			BreakView.updateTableViewer();
			DefaultView.updateTableViewer();
			BranchView.updateTableViewer();
			CaseDefaultView.updateTableViewer();
			DataView.updateTableViewer();

			System.out.println("执行时间：" + (System.currentTimeMillis() - now));
			System.out.println("不能以SwitchExpression返回一个值的形式实现：" + sumValueExpression);
//			
//			System.out.println("共处理case分支：" + sumCase + "个；\n" + "共合并case分支：" + sumCaseSeries + "个");
//			System.out.println("存在break结束语句缺失：" + breakMissList.size() + "处；\n" + "存在case-default连用："
//					+ caseDefaultList.size() + "处；\n" + "其中两种情况同时出现：" + break_caseDefau + "处");
//			System.out.println("补充default缺失：" + sumDefault + "处；");
//			System.out.println("补充branch缺失：" + sumBranch + "处");
//			System.out.println("补充case-default连用：" + sumDealCaDe + "处" 
//					+ ",无法补充（含switch）" + sumNotCaDe + "处");
//			System.out.println("共转换SwitchStatement->SwitchExpression:" + (sumCategoryOne + sumCategoryTwo + sumCategoryThree) + "处");

			if (sumBranch != 0 || sumDefault != 0) {
				return RefactoringStatus.createInfoStatus("Final Condition has been Checked!"
						+ "    Automatically Add 'default' : " + sumDefault + "; 'branch' : " + sumBranch);
			} else {
				return RefactoringStatus.createInfoStatus("Final Condition has been Checked!");
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void collectChanges() throws JavaModelException, IllegalArgumentException, IOException {
		now = System.currentTimeMillis();	
		for (int i = 0; i < compilationUnits.size();) {
			IJavaElement element = compilationUnits.get(i);

			ICompilationUnit cu = (ICompilationUnit) element;
			String source = cu.getSource();
			Document document = new Document(source);
			// 创建AST
			ASTParser parser = ASTParser.newParser(AST.JLS14);
			parser.setSource(cu);
			CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
			AST ast = astRoot.getAST();
			ASTRewrite astr = ASTRewrite.create(ast);

			rewrite = astr;
			astFlag = ast;
			jTemp = element;
			arTemp = astRoot;

			astRoot.recordModifications();

			if (!textRefactor) {

				ArrayList<EnumDeclaration> enums = new ArrayList<>();
				getEnums(astRoot.getRoot(), enums);
				for (EnumDeclaration ed : enums) {
					SwitchSearchInEnum ssie = new SwitchSearchInEnum(element.getPath().toString(), ed, ast, defaultAddLabel, branchAddLabel, dealC_DLabel, astr);
					ssie.traverseType();
				}

				ArrayList<EnumDeclaration> enumTemp = new ArrayList<>();
				getEnums(astRoot.getRoot(), enumTemp);
				ArrayList<SwitchStatement> switchInEnum = new ArrayList<>();
				for (EnumDeclaration ed : enumTemp) {
					getSwitchs(ed, switchInEnum);
				}

				ArrayList<TypeDeclaration> types = new ArrayList<TypeDeclaration>();
				getTypes(astRoot.getRoot(), types);
				for (TypeDeclaration td : types) {

					ArrayList<SwitchStatement> list = new ArrayList<>();
					findSwitchAll(td, list);

					SwitchSearchInType ssit = new SwitchSearchInType(element.getPath().toString(), td, ast, defaultAddLabel, branchAddLabel, dealC_DLabel, astr, list);
					ssit.traverseType();

					for (SwitchStatement ss : list) {
						if (!switchInEnum.contains(ss)) {
							SwitchSearchRefactoring ssr = new SwitchSearchRefactoring(element.getPath().toString(), td,
									ast, null, defaultAddLabel, branchAddLabel, dealC_DLabel);

							Statement sTemp = ssr.switchSearchRefactor(ss);
							
							SwitchRefactoring.ENDSWITCH1 = false;
							SwitchRefactoring.ENDSWITCH2 = false;
							SwitchRefactoring.ENDSWITCH3 = false;
							SwitchRefactoring.oldToNewLabel = false;
							if (SwitchRefactoring.rebackOldSwitch != 0 
									&& SwitchRefactoring.breakMissList.size() >= SwitchRefactoring.rebackOldSwitch) {
								for(int j = 0; j < SwitchRefactoring.rebackOldSwitch; j++) {
									SwitchRefactoring.breakMissList.remove(SwitchRefactoring.breakMissList.size() - 1);
									DataLogList.listBreakRecordReback();
								}
								SwitchRefactoring.rebackOldSwitch = 0;
							}
							
							astr.replace(ss, sTemp, null);
						}
					}
				}

				if (!REFAC) {
					rewriteTemp();
					REFAC = true;
				}

				TextEdit edit = null;
				try {
					edit = astr.rewriteAST(document, null);
				} catch (Exception e) {
					REFAC = false;
					recordTemp();
					continue;
				}

				TextFileChange change = new TextFileChange("", (IFile) cu.getResource());
				change.setEdit(edit);
				changeManager.add(change);

//				List<TypeDeclaration> types = new ArrayList<TypeDeclaration>();
//				getTypes(astRoot.getRoot(), types);
//				
//				for (TypeDeclaration td : types) {
//					collectChanges(astRoot, td, element.getPath().toString());
//				}
//				TextEdit edits = astRoot.rewrite(document, cu.getJavaProject().getOptions(true));
//				TextFileChange change = new TextFileChange("", (IFile) cu.getResource());
//				change.setEdit(edits);
//				changeManager.add(change);
			} else {
				textLabel1 = true;
				ArrayList<SwitchStatement> switchs = new ArrayList<>();
				getSwitchs(astRoot.getRoot(), switchs);

				for (SwitchStatement ss : switchs) {
					if (astRoot.getLineNumber(ss.getStartPosition()) == (textStartLine + 1)) {// &&ss.getLength()==textLength
						SwitchSearchRefactoring ssr = new SwitchSearchRefactoring(element.getPath().toString(), null,
								ast, null, defaultAddLabel, branchAddLabel, dealC_DLabel);

						astr.replace(ss, ssr.switchSearchRefactor(ss), null);
						
						SwitchRefactoring.ENDSWITCH1 = false;
						SwitchRefactoring.ENDSWITCH2 = false;
						SwitchRefactoring.ENDSWITCH3 = false;
						SwitchRefactoring.oldToNewLabel = false;
						if (SwitchRefactoring.rebackOldSwitch != 0 
								&& SwitchRefactoring.breakMissList.size() >= SwitchRefactoring.rebackOldSwitch) {
							for(int j = 0; j < SwitchRefactoring.rebackOldSwitch; j++) {
								SwitchRefactoring.breakMissList.remove(SwitchRefactoring.breakMissList.size() - 1);
								DataLogList.listBreakRecordReback();
							}
							SwitchRefactoring.rebackOldSwitch = 0;
						}
						
						textLabel2 = true;
						break;
					}
				}

				TextEdit edit = astr.rewriteAST(document, null);
				TextFileChange change = new TextFileChange("", (IFile) cu.getResource());
				change.setEdit(edit);
				changeManager.add(change);

				textRefactor = false;
			}
			i++;
		}
	}

	private void rewriteTemp() {
		sumSwitch = numTemp[0];
		sumCaseDefault = numTemp[1];
		sumCategoryOne = numTemp[2];
		sumCategoryTwo = numTemp[3];
		sumCategoryThree = numTemp[4];
		sumCategoryZero = numTemp[5];
		sumCategoryNew = numTemp[6];
		sumCase = numTemp[7];
		sumCaseSeries = numTemp[8];
		sumBranch = numTemp[9];
		sumDefault = numTemp[10];
	}

	private void recordTemp() {
		numTemp[0] = sumSwitch;
		numTemp[1] = sumCaseDefault;
		numTemp[2] = sumCategoryOne;
		numTemp[3] = sumCategoryTwo;
		numTemp[4] = sumCategoryThree;
		numTemp[5] = sumCategoryZero;
		numTemp[6] = sumCategoryNew;
		numTemp[7] = sumCase;
		numTemp[8] = sumCaseSeries;
		numTemp[9] = sumBranch;
		numTemp[10] = sumDefault;
	}

	private void getTypes(ASTNode root, ArrayList<TypeDeclaration> types) {
		root.accept(new ASTVisitor() {
			public boolean visit(TypeDeclaration node) {
				types.add(node);
				return false;
			}
		});
	}

	private void getEnums(ASTNode root, ArrayList<EnumDeclaration> enums) {
		root.accept(new ASTVisitor() {
			public boolean visit(EnumDeclaration node) {
				enums.add(node);
				return false;
			}
		});
	}

	private void findSwitchAll(ASTNode root, ArrayList<SwitchStatement> switchs) {
		root.accept(new ASTVisitor() {
			public boolean visit(SwitchStatement node) {
				switchs.add(node);
				return false;
			}
		});
	}

	private void getSwitchs(ASTNode root, ArrayList<SwitchStatement> switchs) {
		root.accept(new ASTVisitor() {
			public boolean visit(SwitchStatement node) {
				switchs.add(node);
				return true;
			}
		});
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor arg0)
			throws CoreException, OperationCanceledException {

		return RefactoringStatus.createInfoStatus("Initial Condition is OK!");
	}

	@Override
	public Change createChange(IProgressMonitor arg0) throws CoreException, OperationCanceledException {
		Change[] changes = new Change[changeManager.size()];
		System.arraycopy(changeManager.toArray(), 0, changes, 0, changeManager.size());
		CompositeChange change = new CompositeChange(element.getJavaProject().getElementName(), changes);
		return change;
	}

	@Override
	public String getName() {
		return "Switch Refactor";
	}

}