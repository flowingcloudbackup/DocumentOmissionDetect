package udf.testPackage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class testChangeNotEqualSymbol {

	public static HashMap<String, String> ChangeMap = new HashMap<String, String>(){
		{
			put("<=", ">");	//	a<=b → !a>b
			put(">=", "<");	//	a>=b → !a<b
			put("!=", "==");//	a!=b → !a==b
		}
	} ;
	public static char[] ParCharSet = "._ \t".toCharArray();
	
	public static String replacingFirstWord(String str, String rp1, String rp2){
		//	replace the first rp1 of str with rp2
		//	用rp2替换str中第一个rp1
		int index = str.indexOf(rp1);
		if (index < 0)
			//	如果没有，就返回原字符串
			return str;
		String res = str.substring(0, index);
		res += rp2;
		res += str.substring(index + rp1.length());
		return res;
	}
	private static boolean IsLegalParamChar(char c){
		if (c >= '0' && c <= '9') return true;
		if (c >= 'a' && c <= 'z') return true;
		if (c >= 'A' && c <= 'Z') return true;
		boolean flag = false;
		for (char ch : ParCharSet){
			if (c == ch){ flag = true;break;}
		}
		return flag;
	}
	public static String addNegSymbol(String exp, int index){
		char[] chars = exp.toCharArray();
		String resA;
		String resB;
		for(;index >= 0;index--){
			if (!IsLegalParamChar(chars[index])){
				break;
			}
		}
		resA = exp.substring(0, index +1);
		resB = exp.substring(index + 1);
		
		return resA + "!" + resB;
	}
	public static String ChangingAllExps(String exp){
		for (Iterator itr = ChangeMap.keySet().iterator(); itr.hasNext();){
			String key = (String)itr.next();
			int index = exp.indexOf(key);
			exp = replacingFirstWord(exp, key, ChangeMap.get(key));
			exp = addNegSymbol(exp, index-1);
		}
		
		return exp;
	}
	public static void main(String[] args){
		String exp = "a != b||a <= b||a >= b";
		System.out.println(ChangingAllExps(exp));
	}

}
