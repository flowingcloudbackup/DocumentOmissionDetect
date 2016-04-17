package udf.testPackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class testJavaDoc {
	private static void AnalysisClassFile(File file){
		String content = ExtraContentFromFile(file);
		
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
		for (MethodDeclaration met : methods){
			MethodAnalysis(met);
		}
	}
	private static void MethodAnalysis(MethodDeclaration method){
		String name = method.getName().toString();
		System.out.println("in method: " + name + "\n=====================");
		Javadoc metDoc = method.getJavadoc();
		if (metDoc == null) return;
		List<TagElement> tags = metDoc.tags();
		if (tags == null) return;
		for (TagElement tag : tags){
			System.out.println(tag.toString() + "\n----------------------");
			System.out.println(getNewTagString(tag) + "\n----------------------");
			System.out.println(oldWayGetDescription(tag) + "\n----------------------");
		}
		System.out.println("=================================");
	}
	private static String getNewTagString(TagElement tag){
		String res = "";
		if (tag == null) return res;
		List<ASTNode> fragments = tag.fragments();
		for (ASTNode ast : fragments){
			res += ast + " ";
		}
		return res;
	}
	private static String oldWayGetDescription(TagElement tag){
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
		return tagDescript;
	}
	private static String ExtraContentFromFile(File file){
		String resStr = null;
		try {
			BufferedReader readr = new BufferedReader(new FileReader(file));
			String line = readr.readLine();
			resStr = "";
			while(line != null){
				resStr += line + "\n";
				line = readr.readLine();
			}
			readr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resStr;
	}
	
	public static void main(String[] args){
		File file = new File("F:\\JavaProjects\\DocumentOmissionDetect\\testFiles\\testJavadocExtract\\testsoloif.java");
		AnalysisClassFile(file);
	}

}
