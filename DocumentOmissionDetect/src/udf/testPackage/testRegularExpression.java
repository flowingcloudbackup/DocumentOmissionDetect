package udf.testPackage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class testRegularExpression {
	
	public static void main(String[] args){
		/*
		String expression = "!aContainer.isFocusTraversalPolicyProvider() && !aContainer.isFocusCycleRoot()";
		String reglx = "[&|]";
		String[] strs = expression.split(reglx);
		
		for (String str : strs){
			System.out.println(str);
		}
		*/
		String firstline = "    \t    public static org.apache.poi.hpsf.PropertySet create"
				+ "(java.io.InputStream, int, java.lang.String) "
				+ "throws org.apache.poi.hpsf.NoPropertySetStreamException, org.apache.poi.hpsf.MarkUnsupportedException, org.apache.poi.hpsf.UnexpectedPropertySetTypeException, java.io.IOException";
		StringBuilder test = new StringBuilder(firstline);
		char[] testc = firstline.toCharArray();
		int i = 0;
		char c = testc[0];
		while(!legal(c)){
			c = testc[++i];
		}
		System.out.println(firstline.substring(i));
		
	}
	
	public static boolean legal(char c){
		if ((c >= 'a' && c <= 'z')||(c >='A' && c <= 'Z'))
			return true;
		return false;
	}

}
