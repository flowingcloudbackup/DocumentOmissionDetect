package udf.testPackage;

import java.io.File;

public class testfilename {
	
	public static void main(String[] args){
		String test = "F:\\DocErrorDetect\\DocumentInfomation\\";
		String r = "F:\\DocErrorDetect\\";
		r = r.replaceAll("\\\\", "\\\\\\\\");
		System.out.println(r);
		String res = test.replaceAll(r, "");
		System.out.println(res);
		
		String  test2 = "hehe#####da";
		int index = test2.indexOf("#####");
		System.out.println(test2.substring(0, index));
		System.out.println(test2.substring(index + 5));
	}

}
