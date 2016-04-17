package udf.extraomission;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javatuples.Pair;


public class SpecialTagReplace {
	
	public static Pair[] htmlNotes = {
			new Pair<String, String>("&nbsp;", " "),
			new Pair<String, String>("&lt;", "<"),
			new Pair<String, String>("&le;", "≤"),
			new Pair<String, String>("&gt;", ">"),
			new Pair<String, String>("&ge;", "≥"),
			new Pair<String, String>("&amp;", "&"),
			new Pair<String, String>("&quot;", "\""),
			new Pair<String, String>("&times;", "×")
			//×
			/* html 标记记录不全，暂时先使用这几个有用的
			 * */
	};
	
	public static void main(String[] args){
		String src = "if  rule  is not one ofthe following:   {@link #CLEAR},  {@link #SRC},  {@link #DST}, {@link #SRC_OVER},  {@link #DST_OVER},  {@link #SRC_IN}, {@link #DST_IN},  {@link #SRC_OUT},  {@link #DST_OUT}, {@link #SRC_ATOP},  {@link #DST_ATOP}, or  {@link #XOR} ";
		
		System.out.println(SpecialCodeTagProcess(src));
		
	}
	
	public static String HtmlNotesProcess(String str){
		String res = str;
		for (Pair<String, String> pair : htmlNotes){
			res = res.replaceAll(pair.getValue0(), pair.getValue1());
		}
		
		return res;
	}
	
	public static String SpecialCodeTagProcess(String str){
		String resStr = str;
		String reg = "\\{@\\w+\\s[^\\}]+\\}";
		//	正则表达式应该是	{@\w+\s[^}]+}
		Pattern pat = Pattern.compile(reg);
		Matcher mat = pat.matcher(str);
		while (mat.find()){
			String tagStr = mat.group();
			//System.out.println(tagStr);
			//System.out.println(toReplace(tagStr));
			resStr = resStr.replace(tagStr, toReplace(tagStr));
		}
		
		return resStr;
	}
	private static String toReplace(String tagStr){
		String res = "SP_";
		
		Pair[] repPairs = {
				new Pair<String, String>("#", "SYMBSUBLINE"),
				new Pair<String, String>("\\.", "SYMBDOT"),
				new Pair<String, String>("\\(", "SYMBLEFTBRACKET"),
				new Pair<String, String>("\\)", "SYMBRIGHTBRACKET"),
		};
		
		//String tag = tagStr.replaceAll("#", "_");
		String tag = tagStr;
		for (Pair<String, String> pair : repPairs){
			tag = tag.replaceAll(pair.getValue0(), pair.getValue1());
		}
		
		int inAt = tag.indexOf("@");
		int space = tag.indexOf(" ");
		String type = tag.substring(inAt + 1, space);
		res += type.toUpperCase() + "_";
		tag = tag.substring(space + 1);
		tag = tag.substring(0, tag.length() -1);
		res += tag;
		
		return res;
	}
}
