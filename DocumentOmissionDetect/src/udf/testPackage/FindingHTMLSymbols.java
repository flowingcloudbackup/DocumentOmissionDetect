package udf.testPackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindingHTMLSymbols {
	
	public static List<String> Symbols = new ArrayList<String>();
	public static Pattern pat = Pattern.compile("&\\w+;");
	
	public static void main(String[] args){
		String path = "F:\\DocErrorDetect\\DocumentInfomation\\jdk_part\\1.20°æ±¾\\";
		File root = new File(path);
		File[] files = root.listFiles();
		for (File file : files){
			ProcessdFile(file);
		}
		System.out.println("result:");
		int count = 1;
		for (String str : Symbols){
			System.out.println((count++) + "\t" + str);
		}
	}
	
	private static void ProcessdFile(File file){
		int count = 0;
		String name = file.getName();
		if (!file.getName().endsWith(".cmp"))	return;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while(line != null){
				System.out.println("reading file:\t" + name + "\t, getting line:\t" + count++);
				processLine(line);
				line = reader.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void processLine(String line){
		Matcher mat = pat.matcher(line);
		while(mat.find()){
			String s = mat.group();
			addToList(s);
		}
	}
	private static void addToList(String item){
		boolean flag = false;
		for (String s : Symbols){
			if (item.equals(s)){
				flag = true;
				break;
			}
		}
		if (!flag){
			Symbols.add(item);
		}
	}
	

}
