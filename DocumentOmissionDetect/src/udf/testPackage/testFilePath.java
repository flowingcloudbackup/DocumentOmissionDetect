package udf.testPackage;

import java.io.File;
import java.io.IOException;

public class testFilePath {
	
	public static void main(String[] args) throws IOException{
		String root = "F:/DocErrorDetect/CallGraphs/JDT_way/ATryTest///////";
		String name = "/test.res";
		
		File file = new File(root + name);
		file.createNewFile();
	}

}
