package switchrefactor.datalog;

import org.eclipse.jdt.core.IJavaElement;

public class AddressRecord {
	
	private String location;
	private String className;
	private String methodName;
	private String switchExpression;
	private IJavaElement jTemp;
	private int startPoint;
	
	//影响重构因素统计格式
	public AddressRecord(String location, String className, String methodName, 
			String switchExpression, IJavaElement jTemp, int startPoint) {
		this.location = location;
		this.className = className;
		this.methodName = methodName;
		this.switchExpression = switchExpression;
		this.jTemp = jTemp;
		this.startPoint = startPoint;
	}
	
	public String getLocation() {
		return location;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public String getSwitchExpression() {
		return switchExpression;
	}
	
	public IJavaElement getJavaElement() {
		return jTemp;
	}
	
	public int getStartPoint() {
		return startPoint;
	}
}
