package switchrefactor.datalog;

public class DataRecord {
	private String name;
	private int sumSwitch;
	private int sumOldRule;
	private int sumNewRule;
	private int refactorNone;
	private int refactorMethod;
	private int refactorAssign;
	private int refactorReturn;
	private int defaultMiss;
	private int branchMiss;
	private int breakMiss;
	private int caseDefault;
	
	//重构信息记录格式
	public DataRecord(String name, int sumSwitch, int sumOldRule, int sumNewRule, int refactorNone, 
			int refactorMethod, int refactorAssign, int refactorReturn, int defaultMiss,
			int branchMiss, int breakMiss, int caseDefault) {
		this.name = name;
		this.sumSwitch = sumSwitch;
		this.sumOldRule = sumOldRule;
		this.sumNewRule = sumNewRule;
		this.refactorNone = refactorNone;
		this.refactorMethod = refactorMethod;
		this.refactorAssign = refactorAssign;
		this.refactorReturn = refactorReturn;
		this.defaultMiss = defaultMiss;
		this.branchMiss = branchMiss;
		this.breakMiss = breakMiss;
		this.caseDefault = caseDefault;
	}
	
	public String getName() {
		return name;
	}
	
	public int getSumSwitch() {
		return sumSwitch;
	}
	
	public int getSumOldRule() {
		return sumOldRule;
	}
	
	public int getSumNewRule() {
		return sumNewRule;
	}
	
	public int getRefactorNone() {
		return refactorNone;
	}
	
	public int getRefactorMethod() {
		return refactorMethod;
	}
	
	public int getRefactorAssign() {
		return refactorAssign;
	}
	
	public int getRefactorReturn() {
		return refactorReturn;
	}
	
	public int getDefaultMiss() {
		return defaultMiss;
	}

	public int getBranchMiss() {
		return branchMiss;
	}
	
	public int getBreakMiss() {
		return breakMiss;
	}

	public int getCaseDefault() {
		return caseDefault;
	}
}
