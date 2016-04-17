package udf.testPackage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class testSubString {
	
	public static void main(String[] args){
		/*
		String str = "a < 0.0f || b > 1.0 || c == 2.1f ";
		
		Pattern pat = Pattern.compile("\\W[0-9]+\\.0+f?\\W");
		Matcher mat = pat.matcher(str);
		while(mat.find()){
			String tr = mat.group();
			tr = tr.substring(1, tr.length()-1);
			int index = tr.indexOf(".");
			String rep = tr.substring(0, index);
			
			str = str.replace(tr, rep);
			
			mat = pat.matcher(str);
		}
		
		System.out.println(str);
		
		String str = "abc abASTANF";
		int index = str.indexOf("ab", 2);
		System.out.println(index);
		*/
		String str = null;
		System.out.println(str.indexOf("hehe"));
	}

}
