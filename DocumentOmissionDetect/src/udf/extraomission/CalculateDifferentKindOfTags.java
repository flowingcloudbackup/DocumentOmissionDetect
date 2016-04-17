package udf.extraomission;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class CalculateDifferentKindOfTags {
	
	public static List<TagkindLists> AllTagList;
	
	public static String projRootPath = "F:\\DocErrorDetect\\DocumentInfomation\\";
	public static String projName = "jdk_part";
	
	public static void ScanningEachJavaFile(File file){
		if (file.isFile()){
			System.out.println("Analysing..." + file.getAbsolutePath());
			String name = file.getName();
			if (!name.endsWith(".java")){
				return;
			}
			AnalysisJavaFile(file);
		}
		File[] files = file.listFiles();
		if (files == null)	return;
		for (File f : files){
			ScanningEachJavaFile(f);
		}
	}
	
	public static void AnalysisJavaFile(File file){
		String content = ExtractFileContent(file);
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(content.toCharArray());
		parser.setResolveBindings(true);
		CompilationUnit result = (CompilationUnit) parser.createAST(null);
		TypeDeclaration root = null;
		try{
			root = (TypeDeclaration)result.types().get(0);
		}catch(Exception e){;}
		
		MethodDeclaration[] methods;
		if (root == null){
			return;
		}
		
		methods = root.getMethods();
		String replaceStr = (projRootPath + projName).replaceAll("\\\\", "\\\\\\\\");
		String packPath = file.getAbsolutePath().replaceAll(replaceStr, "");
		for (MethodDeclaration met : methods){
			AnalysisEachMethodInformation(met, packPath);
		}
	}
	
	public static void AnalysisEachMethodInformation(MethodDeclaration method, String packPath){
		System.out.println("\tin method:\t" + method.getName());
		
		Javadoc doc = method.getJavadoc();
		if (doc == null) return;
		
		String metSig = getMethodSignature(method);
		String paramList = getParamList(method);
		
		List<TagElement> docTags = doc.tags();
		for (TagElement tag : docTags){
			AnalysisEachTags(tag, packPath + "-" + metSig, paramList);
		}
	}
	private static String getMethodSignature(MethodDeclaration method){
		String methodName = method.getName().toString();
		List<SingleVariableDeclaration> methodParams = method.parameters();
		String res = methodName + "(";
		if (methodParams.size() > 0){
			for (SingleVariableDeclaration param : methodParams){
				String typeName = param.getType().toString();
				int index = typeName.indexOf("<");
				if (index>0){
					typeName = typeName.substring(0, index);
				}
				
				if (param.toString().endsWith("[]")){
					typeName += "[]";
				}
				
				res += typeName + ",";
			}
			res = res.substring(0, res.length()-1);
		}
		res += ")";
		
		return res;
	}
	private static String getParamList(MethodDeclaration method){
		List<SingleVariableDeclaration> methodParams = method.parameters();
		String res = "(";
		if (methodParams == null || methodParams.size() <= 0)
			return "()";
		for (SingleVariableDeclaration param : methodParams){
			String typeName = param.getType().toString();
			int index = typeName.indexOf("<");
			if (index>0){
				typeName = typeName.substring(0, index);
			}
			String paramName = param.getName().toString();
			
			if (param.toString().endsWith("[]")){
				typeName += "[]";
			}
			
			res += typeName + "-" + paramName + ",";
		}
		res = res.substring(0, res.length()-1);
		res += ")";
		return res;
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
	public static void AnalysisEachTags(TagElement tag, String headStr, String paramList){
		/*
		 * @deprecated
		String tagName = tag.getTagName();
		if (tagName == null){
			tagName = "descrip";
		}else{
			tagName = tagName.substring(1);
		}
		String tagDescript = tag.toString();
		int index = tagName.equals("descrip")? 0:tagName.length()+2;
		index += 4;
		if (index < tagDescript.length() -1){
			tagDescript = tagDescript.substring(index);
		}else{
			tagDescript = "";
		}
		*/
		String tagDescript = getTagDescription(tag);
		String tagName = tag.getTagName();
		if (tagName == null){
			tagName = "descrip";
		}else{
			tagName = tagName.substring(1);
		}
		tagDescript = SplitHtmlSymbols(tagDescript);
		
		if (paramList == null || paramList == "") paramList = " ";
		tagDescript = headStr + "#####" + paramList + "#####" + tagDescript;
		
		boolean kindExist = false;
		for(TagkindLists klist : AllTagList){
			if (klist.tagName.equals(tagName)){
				kindExist = true;
				klist.tagList.add(tag);
				klist.tagDescripts.add(tagDescript);
			}
		}
		if (!kindExist){
			TagkindLists newList = new TagkindLists(tagName);
			newList.tagList.add(tag);
			newList.tagDescripts.add(tagDescript);
			AllTagList.add(newList);
		}
	}
	private static String SplitHtmlSymbols(String srcStr){
		String resStr = "";
		Pattern pat = Pattern.compile("</?\\w+.*?>", Pattern.DOTALL);
		Matcher mat = pat.matcher(srcStr);
		resStr = mat.replaceAll(" ");
		
		//System.out.println(resStr);
		resStr = resStr.replaceAll("<!DOCTYPE>", " ");
		resStr = resStr.replaceAll("<!--...-->", " ");
		return resStr;
	}
	
	public static String ExtractFileContent(File file){
		String content = "";
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while(line != null){
				content += line + "\n";
				line = reader.readLine();
			}
			reader.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return content;
	}
	
	public static void main(String[] args){
		//TestFunction();
		RunFunction();
	}
	private static void TestFunction(){
		AllTagList = new ArrayList<TagkindLists>();
		String root = "F:\\JavaProjects\\DocumentOmissionDetect\\testFiles\\ExtraAnalysis\\TestFold\\";
		ScanningEachJavaFile(new File(root));
	}
	private static void RunFunction(){
		AllTagList = new ArrayList<TagkindLists>();
		/*
		 * 	public static String projRootPath = "F:\\DocErrorDetect\\DocumentInfomation\\";
			public static String projName = "jdk_part";
		 * */
		ScanningEachJavaFile(new File(projRootPath + projName + "\\"));
		
		//AnalysisJavaFile(new File("F:\\DocErrorDetect\\DocumentInfomation\\jdk_part\\java\\awt\\Container.java"));
		
		for (TagkindLists t : AllTagList){
			//System.out.println(t.WriteFileContent());
			
			t.SaveInFile(projRootPath + projName + "\\");
		}
	}

}
