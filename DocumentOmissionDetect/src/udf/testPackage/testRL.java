package udf.testPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import udf.ApiOmiDetec.Globals.GlobalTools;

public class testRL {
	
	public static void main(String[] args){
		String comp = "comp";
		String exp = "\\W+" + comp + "\\W+";
		
		String testString = "comp!= null && component.replace &&cn == comp";
		
		System.out.println(replaceParam(testString, comp, "param"));
		
	}
	
	public static String replaceParam(String srcStr, String comp, String toReplace){
		String rlExp = "\\W+" + comp + "\\W+";
		String resStr = "%" + srcStr + "%";
		
		Pattern pat = Pattern.compile(rlExp);
		Matcher mat = pat.matcher(resStr);
		List<String> groups = new ArrayList<String>();
		while(mat.find()){
			groups.add(mat.group());
		}
		groups = GlobalTools.RemoveDuplicated(groups);	//	»•÷ÿ∏¥
		
		for (String torpl : groups){
			String t = torpl.replaceAll(comp, toReplace);
			resStr = resStr.replaceAll(torpl, t);
		}
		
		resStr = resStr.substring(1);
		resStr = resStr.substring(0, resStr.length()-1);
		
		return resStr;
	}

}
