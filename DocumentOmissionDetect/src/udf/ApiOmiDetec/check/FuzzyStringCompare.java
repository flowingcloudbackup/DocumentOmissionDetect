package udf.ApiOmiDetec.check;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import HeuristicExtraction.inThrowTag.ConsInThrow;

public class FuzzyStringCompare {
	
	private static String FloatIntegerize(String str){
		str = " " + str + " ";
		
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
		
		return str;
	}
	public static boolean FuzzyCompare(String a, String b){
		
		a = a.replace("(", "");
		a = a.replace(")", "");
		
		b = b.replace("(", "");
		b = b.replace(")", "");	
		//	取消所有括号限制
		
		a = FloatIntegerize(a);
		b = FloatIntegerize(b);
		//	便于比较，float转成int型
		
		Pattern pat = Pattern.compile("\\s+");
		Matcher mat = pat.matcher(a);
		a = mat.replaceAll("");
		
		mat = pat.matcher(b);
		b = mat.replaceAll("");
		//	取消所有空格
		b = b.replace("!!", "");	//	去掉重复的空格
		
		return FuzzyExpressionCompare(a, b);
	}
	
	private static boolean FuzzyExpressionCompare(String a, String b){
		String[] Acells = a.split("(\\|\\|)|(\\&\\&)");
		String[] Bcells = b.split("(\\|\\|)|(\\&\\&)");
		if (Acells.length != Bcells.length)	return false;
		boolean flag = true;
		for (int i = 0;i < Acells.length; i++){
			if (!FuzzyExpressionCompare_Cell(Acells[i], Bcells[i])){
				flag = false;
				break;
			}
		}
		return flag;
	}
	private static boolean FuzzyExpressionCompare_Cell(String a, String b){
		int ia = a.indexOf(ConsInThrow.WildCard);
		int ib = b.indexOf(ConsInThrow.WildCard);
		if (ia > 0 || ib > 0){
			int i = (ia > 0)?ia:ib;
			return a.substring(i).equals(b.substring(i));
		}
		return a.equals(b);
	}
	public static void main(String[] args){
		String a = "test||t|test&&t&hehe";
		System.out.println(a);
		for (String s : a.split("(\\|\\|)|(\\&\\&)")){
			System.out.println(s);
		}
	}
}
