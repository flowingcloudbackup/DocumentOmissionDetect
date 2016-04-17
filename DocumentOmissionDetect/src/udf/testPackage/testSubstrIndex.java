package udf.testPackage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import udf.ApiOmiDetec.Globals.GlobalTools;

public class testSubstrIndex {
	
	public static void main(String[] args){
		/*
		String mainstr = "hehedalegeheheda";
		String sub  = "li77";
		System.out.println(mainstr.indexOf(sub));
		*/
		
		/*
		String a = "F:\\DocErrorDetect\\CellDoc\\testProj\\res\\jdk_part\\awt\\color\\ColorSpace.java\\getMaxValue(int)\\";
		String b = "F:\\DocErrorDetect\\CellDoc\\testProj\\res\\jdk_part\\";
		
		System.out.println(a);
		System.out.println(b.replaceAll("\\\\", "\\\\\\\\"));
		System.out.println(a.replaceAll(b.replaceAll("\\\\", "\\\\\\\\"), ""));
		
		*/
		
		String test = "Map<? extends Attribute,?>-attributes";
		String[] strs = test.split("-");
		int i = 0;
		for (String s : strs){
			System.out.println(i++ + ":" + s);
		}
			
		//System.out.println(GlobalTools.ProcessSpecialNote(b));
		//System.out.println(test.replaceAll(GlobalTools.ProcessSpecialNote(a), GlobalTools.ProcessSpecialNote(b)));
	}

}
