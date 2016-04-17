package udf.ApiOmiDetec.Javadoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import udf.ApiOmiDetec.Globals.GlobalValues;
import udf.ApiOmiDetec.Layers.MethodLevel;

public class processedTagElement {
	
	private TagElement srcTag;
	/*
	 * TagName:
	 *		@param  @throw @see ...
	 *	if null, name is descrip
	 *	需要注意的是，@throw 和 @exception 没有统一化名称
	 * */
	private String tagName;
	private String tagDesc;
	
	private String keyWord;
	
	public processedTagElement(TagElement tag){
		//	从.java文件中提取
		this.srcTag = tag;
		
		processTag();
		extractKeyWord();
		processDescription();
		
		//System.out.println(this.presentation());
	}
	public processedTagElement(String name, String key, String desc){
		//	从doc.res文件中提取
		this.tagName = name;
		this.keyWord = key;
		this.tagDesc = desc;
		
		this.srcTag = null;
	}
	private static String getTagDescription(TagElement tag){
		String res = "";
		if (tag == null) return res;
		List<ASTNode> fragments = tag.fragments();
		for (ASTNode ast : fragments){
			res += ast + " ";
		}
		return res;
	}
	private void processTag(){
		this.tagName = this.srcTag.getTagName();
		
		if (this.tagName == null){
			this.tagName = "descrip";
		}else{
			this.tagName = this.tagName.substring(1);
		}
		
		this.tagDesc = getTagDescription(this.srcTag);
		/*
		int index = this.tagName.equals("descrip")? 0:this.tagName.length()+2;
		index += 4;
		if (index < this.tagDesc.length() -1){
			this.tagDesc = this.tagDesc.substring(index);
		}else{
			this.tagDesc = "";
		}
		*/
		this.tagDesc += " ";

	}
	
	private void extractKeyWord(){
		if (this.tagName.equals("param") ||
			this.tagName.equals("exception") ||
			this.tagName.equals("throws")	){
			
			int index = this.tagDesc.indexOf(" ");
			if (index >= 0 && index <= this.tagDesc.length()){
				this.keyWord = this.tagDesc.substring(0, index);
			}else{	this.keyWord = null;	}
			this.tagDesc = this.tagDesc.substring(index+1);
			
		}else{
			this.keyWord = null;
		}
	}
	
	private void processDescription(){
		/* regularise description.
		 * including spliting illegal marks or html symbols
		 * 
		 * examples:
		 * <code> </code>
		 * */
		this.tagDesc = SplitHtmlSymbols(this.tagDesc);
	}
	
	public String getTagName()
	{	return this.tagName;	}
	public String getTagDesc()
	{	return this.tagDesc;	}
	public String getKeyWord()
	{	return this.keyWord;	}
	
	private static String SplitHtmlSymbols(String srcStr){
		String resStr = "";
		/*	之前的算法对于tag中的换行符处理效果不佳
		Pattern pat = Pattern.compile("<.+?>", Pattern.DOTALL);
		Matcher mat = pat.matcher(srcStr);
		resStr = mat.replaceAll(" ");
		*/
		
		Pattern pat = Pattern.compile("</?\\w+.*?>", Pattern.DOTALL);
		Matcher mat = pat.matcher(srcStr);
		resStr = mat.replaceAll(" ");
		
		//System.out.println(resStr);
		resStr = resStr.replaceAll("<!DOCTYPE>", " ");
		resStr = resStr.replaceAll("<!--...-->", " ");
		
		//System.out.println(resStr);
		return resStr;
	}
	
	public String presentation(){
		String str = "TagName:\t" + this.tagName + "\n";
		str += "KeyWord:\t";
		str += this.keyWord==null?"null":this.keyWord;
		str += "\n";
		str += this.tagDesc + "\n";
		
		return str;
	}
	
	public String PresentForStorage(){
		String res = this.tagName + "\n";
		res += this.keyWord + "\n";
		res += this.tagDesc.replaceAll("\\n", "") + "\n";
		
		return res;
	}
	
}
