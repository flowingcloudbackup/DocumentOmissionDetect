package udf.ApiOmiDetec.CallGraph.fromSoot;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import udf.ApiOmiDetec.CallGraph.fromSoot.JimpleInvokeCall.InvokeType;

public class JimpleMethod {
	
	private String[] modifiers;
	private String returnValue;
	private String methodName;
	private String[] params;
	
	private List<JimpleInvokeCall> invokeCalls;
	
	public JimpleMethod(String firstLine, String methodBody){
		extractMethodMeta(firstLine);
		this.invokeCalls = new ArrayList();
		extractCallInfos(methodBody);
		SimplifyInvokeCalls();
	}
	
	private void extractMethodMeta(String firstline){
		//----------split empty symbols---------------
		firstline = SplitFirstBlankSymbols(firstline);
		
		int l = firstline.indexOf("(");
		int r = firstline.indexOf(")");
		String leftSign = firstline.substring(0, l);
		String rightSign = firstline.substring(l+1, r);
		
		//-----------------left-----------------------
		String[] itms_1 = leftSign.split("[\\s]+");
		int size_1 = itms_1.length-1;
		
		this.returnValue = itms_1[size_1-1];
		this.methodName = itms_1[size_1];
		this.modifiers = new String[size_1 -1];
		for (int i = 0;i < size_1 - 1; i++){
			this.modifiers[i] = new String(itms_1[i]);
		}
		
		//----------------right-----------------------
		this.params = rightSign.split("[\\s,]+");
		
		
	}
	
	private String SplitFirstBlankSymbols(String str){
		char[] chars = str.toCharArray();
		int i = 0;
		char c = chars[i];
		while(!isEngChar(c)){	c = chars[++i];	}
		return str.substring(i);
	}
	private boolean isEngChar(char c){
		if ((c >= 'a' && c <= 'z')||(c >='A' && c <= 'Z'))
			return true;
		return false;
	}
	
	private void extractCallInfos(String body){
		int brk = body.indexOf("\n");
		String sen;
		while(brk >= 0){
			sen = body.substring(0, brk);
			//if (sen is a sentence contains invoke information)
			if (containsInvokeInformation(sen)){
				JimpleInvokeCall call = new JimpleInvokeCall(sen);
				this.invokeCalls.add(call);
				//System.out.println(call.presentation());
			}
			
			body = body.substring(brk +1);
			brk = body.indexOf("\n");
		}
	}
	
	private void SimplifyInvokeCalls(){
		List<JimpleInvokeCall> simplified = new ArrayList();
		for (JimpleInvokeCall callInList : this.invokeCalls){
			boolean existed = false;
			for (JimpleInvokeCall call : simplified){
				if (call.equals(callInList)){
					existed = true;
					break;
				}
			}
			if (!existed){
				simplified.add(callInList);
			}
		}
		
		this.invokeCalls = simplified;
	}
	
	private boolean containsInvokeInformation(String sen){
		boolean flag = false;
		for (InvokeType type : InvokeType.values()){
			String reglx = "[^a-zA-Z0-9]+" + type.toString() + "[^a-zA-Z0-9]+";
			Pattern pat = Pattern.compile(reglx);
			Matcher mat = pat.matcher(" " + sen);
			if (mat.find()){
				flag = true;
				break;
			}
		}
		return flag;
	}
	
	public String presentation(){
		String res = "Modifiers:\t";
		if (this.modifiers.length > 0){
			for (String modifier : this.modifiers){
				res += modifier + ", ";
			}
			res = res.substring(0, res.length() -2);
		}
		res += "\nReturn Value:\t" + this.returnValue + "\nMethod Name:\t" + this.methodName;
		res += "\nParameters:\t";
		if (this.params != null){
			if (this.params.length > 0){
				for (String par : this.params){
					res += par + ", ";
				}
				res = res.substring(0, res.length() -2);
			}
		}
		//res += "\nBody:\n" + this.bodyString;
		res += "\n------------------------------------\nInvoke Calls(" + this.invokeCalls.size() + "):";
		for (JimpleInvokeCall call : this.invokeCalls){
			res += "\n-----------\n";
			res += call.presentation();
		}
		res += "\n------------------------------------";
		return res;
	}
	
	public static void main(String[] args){
		
		JimpleMethod me = new JimpleMethod(
				"        public static org.apache.poi.hpsf.PropertySet create"
				+ "(java.io.InputStream, int, java.lang.String) "
				+ "throws org.apache.poi.hpsf.NoPropertySetStreamException, org.apache.poi.hpsf.MarkUnsupportedException, org.apache.poi.hpsf.UnexpectedPropertySetTypeException, java.io.IOException"
				, 
				
				"     $r3 = virtualinvoke r0.<org.apache.poi.hpsf.Property: java.util.Map readDictionary(byte[],long,int,int)>(r1, l1, i2, i3);\n"
				+"       virtualinvoke r2.<org.apache.poi.hpsf.PropertySet: boolean isSummaryInformation()>();\n"
				+" $r92 = virtualinvoke r5.<java.io.File: java.io.File[] listFiles()>();\n"
				+"$i1 = lengthof $r92;\n"
        		+"if i0 < $i1 goto label0;\n"
				+"       virtualinvoke r2.<org.apache.poi.hpsf.PropertySet: boolean isSummaryInformation()>();\n"
        );
		System.out.println(me.presentation());
	}

}
