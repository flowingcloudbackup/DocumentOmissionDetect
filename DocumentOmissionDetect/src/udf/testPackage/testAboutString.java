package udf.testPackage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class testAboutString {
	
	public static void main(String[] args){/*
		String str = "abc. kdslkj;; ;;. ....sdfsl ssdfidisom,lskd";
		String[] ss = str.split("[\\.;] ");
		
		for (String s : ss){
			System.out.println(s);
		}
		*/
		String dot = "\\(";
		String str = "hehe.heh()e(da)";
		String rep = str.replaceAll(dot, "_");
		System.out.println(dot);
		System.out.println(rep);
	}

}
