package udf.testPackage;

import java.io.BufferedReader;
import java.io.File;
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

public class testMetJavadoc {
	
	public static void main(String[] args){
		String fileRoot = "F:\\JavaProjects\\DocumentOmissionDetect\\testFiles\\ExtraAnalysis\\";
		String fileName = "AbstractRegionPainter.java";
		String content = getFileContent(new File(fileRoot + fileName));
		MethodDeclaration[] methods = getMethodsFromJavaFile(content);
		for (MethodDeclaration met : methods){
			System.out.println(met.getName().toString() + ":");
			Javadoc doc = met.getJavadoc();
			if (doc == null) continue;
			List<TagElement> tags = doc.tags();
			for (TagElement tag : tags){
				System.out.println(getTagDescription(tag));
			}
			System.out.println("=================================================================");
		}
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
	
	private static String getFileContent(File file){
		String res = "";
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while(line != null){
				res += line + "\n";
				line = reader.readLine();
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}
		return res;
	}
	private static MethodDeclaration[] getMethodsFromJavaFile(String content){
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(content.toCharArray());
		parser.setResolveBindings(true);
		CompilationUnit result = (CompilationUnit) parser.createAST(null);
		TypeDeclaration root = null;
		try{
			root = (TypeDeclaration)result.types().get(0);
		}catch(Exception e){;}
		
		MethodDeclaration[] methods = null;
		if (root == null){
			return null;
		}
		
		methods = root.getMethods();
		return methods;
	}

}
