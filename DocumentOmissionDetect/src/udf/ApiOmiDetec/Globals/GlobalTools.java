package udf.ApiOmiDetec.Globals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GlobalTools {
	
	public static void EmptyFolderCleaner(File path){
		if(!path.isDirectory())	return;
		File[] files = path.listFiles();
		for (File file : files){
			EmptyFolderCleaner(file);
		}
		int num_filesleft = path.listFiles().length;
		if (num_filesleft <= 0){
			System.out.println("deleting..." + path.getAbsolutePath());
			path.delete();
		}
	}
	
	public static void FoldsCleanUp(File path){
		if (!path.isDirectory()){
			System.out.println("deleting..." + path.getAbsolutePath());
			path.delete();
			return;
		}
		File[] files = path.listFiles();
		for (File file : files){
			FoldsCleanUp(file);
		}
		path.delete();
	}
	
	public static List<String> RemoveDuplicated(List<String> list){
		List<String> resList = new ArrayList<String>();
		for (String s : list){
			boolean exist = false;
			for (String ss : resList){
				if (s.equals(ss)){
					exist = true;
					break;
				}
			}
			if (!exist){	resList.add(s);	}
		}
		
		return resList;
	}
	
	public static String ProcessSpecialNote(String str){
		String res = ReplaceBackslash(str);
		String[] SNote = new String[]{"(",")","[","]","{","}","^","-","$","|","?","*","+","."};
		for (String snote : SNote){
			res = res.replaceAll("\\" + snote, "\\\\" + snote);
		}
		
		return res;
	}
	private static String ReplaceBackslash(String str){
		//	Ìæ»»·´Ð±¸Ü'\'Îª "\\"
		List<Character> res = new ArrayList<Character>();
		char[] srcArray = str.toCharArray();
		for (char c : srcArray){
			if (c == '\\'){
				res.add(c);
				res.add(c);
			}else{
				res.add(c);
			}
		}
		
		char[] resArray = new char[res.size()];
		int i = 0;
		for (Character c : res){
			resArray[i++] = c;
		}
		
		return resArray.toString();
	}
	
	public static void main(String[] args) {
		List<String> l = Arrays.asList(new String[]{"a","b","c","d","a","dd","ss"});
		for (String res : RemoveDuplicated(l)){
			System.out.println(res);
		}
	}

}
