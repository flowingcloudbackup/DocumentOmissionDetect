package udf.testPackage;

public class testSplitExpressions {
	
	public static void main(String[] args){
		String exp = "a*b=c";
		String spExp = "[\\s\\()\\.,=+-/!\\*]";
		String[] snips = exp.split(spExp);
		for (String s : snips){
			System.out.println(s);
		}
	}

}
