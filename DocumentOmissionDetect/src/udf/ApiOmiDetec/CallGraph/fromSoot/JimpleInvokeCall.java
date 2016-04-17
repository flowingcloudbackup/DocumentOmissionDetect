package udf.ApiOmiDetec.CallGraph.fromSoot;

public class JimpleInvokeCall {
	
	public static enum InvokeType {specialinvoke, staticinvoke, virtualinvoke, interfaceinvoke};
	
	public String invokeType;
	public String classPath;
	public String returnValue;
	public String callMethodName;
	public String[] parameters;
	
	public JimpleInvokeCall(String invokeString){
		/* assume invokeString satisfy the form of invoke sentence
		 * here i don't consider the circumstance when invoke string is illegal
		 */
		String[] firstStage = FirstStageProcess(invokeString);
		this.invokeType = firstStage[0];
		processMethodInfo(firstStage[1]);
		processParamsInfo(firstStage[2]);
	}
	
	public static String[] FirstStageProcess(String str){
		/*
		 * process the string into 3 subStrings:
		 * invoke type
		 * method infos
		 * params infos
		 * 
		 * can also using it to judge if str is invoke legal
		 * if not legal, return null
		 * but not useful
		 * */
		String[] res = new String[3];
		int index = str.length();
		InvokeType type = null;
		for (InvokeType t : InvokeType.values()){
			int indexoftype = str.indexOf(t.toString());
			if (indexoftype < index && indexoftype >= 0){
				index = indexoftype;
				type = t;
			}
		}
		if (type == null){	return null;	}
		res[0] = type.toString();
		
		int l_v = str.indexOf("<");
		int r_v = str.lastIndexOf(">");
		String sub1 = str.substring(l_v + 1, r_v);
		
		int l_u = sub1.indexOf("(");
		int r_u = sub1.lastIndexOf(")");
		
		res[1] = sub1.substring(0, l_u);
		res[2] = sub1.substring(l_u +1, r_u);

		return res;
	}
	
	private void processMethodInfo(String methodInfos){
		int index = methodInfos.indexOf(":");
		this.classPath = methodInfos.substring(0, index);
		
		String sub = methodInfos.substring(index+2);
		String[] Return_and_Name = sub.split("[\\s]+");
		
		this.returnValue = Return_and_Name[0];
		this.callMethodName = Return_and_Name[1];
	}
	
	private void processParamsInfo(String paramsInfos){
		if (paramsInfos.length() <= 0){
			this.parameters = null;
		}else{
			this.parameters = paramsInfos.split("[\\s,]+");
		}
	}
	public boolean equals(JimpleInvokeCall that){
		if (!this.invokeType.equals(that.invokeType))
			return false;
		if (!this.classPath.equals(that.classPath))
			return false;
		if (!this.returnValue.equals(that.returnValue))
			return false;
		if (!this.callMethodName.equals(that.callMethodName))
			return false;
		// ------Params judge---------
		if (IsEmptySet(this.parameters) ^ IsEmptySet(that.parameters))
			return false;
		if (IsEmptySet(this.parameters) && IsEmptySet(that.parameters))
			return true;
		boolean flag = true;
		for (int i = 0 ;i< this.parameters.length; i++){
			if (!this.parameters[i].equals(that.parameters[i])){
				flag = false;
				break;
			}
		}
		return flag;
	}
	private boolean IsEmptySet(Object[] a){
		if (a == null)	return true;
		if (a.length <= 0) return true;
		return false;
	}
	
	public String presentation(){
		String res = "Invoke Type:\t" + this.invokeType + "\n";
		res += "Class Path:\t" + this.classPath + "\n";
		res += "Return Value:\t" + this.returnValue + "\n";
		res += "Method Name:\t" + this.callMethodName + "\n";
		res += "Parameters:\t";
		if (this.parameters != null){
			for (String par : this.parameters){
				res += par + ",\t";
			}
			res = res.substring(0, res.length()-2);
		}
		
		return res;
	}
	
	public static void main(String[] args){
		String test1 = "       virtualinvoke r2.<org.apache.poi.hpsf.PropertySet: boolean isSummaryInformation()>();";
		String test2 = "   specialinvoke $r4.<org.apache.poi.hpsf.DocumentSummaryInformation: void <init>(org.apache.poi.hpsf.PropertySet)>(r2);";
		String test3 = "     $r3 = virtualinvoke r0.<org.apache.poi.hpsf.Property: java.util.Map readDictionary(byte[],long,int,int)>(r1, l1, i2, i3);";
		JimpleInvokeCall call = new JimpleInvokeCall(test3);
		System.out.println(call.presentation());
	}

}
