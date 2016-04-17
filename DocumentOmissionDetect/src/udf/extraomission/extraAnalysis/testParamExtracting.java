package udf.extraomission.extraAnalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class testParamExtracting {

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
		System.out.println("------"+ method.getName() +"------");
		System.out.println(res);
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
	
	private static void AnalysisJavaFileContent(String content){
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(content.toCharArray());
		parser.setResolveBindings(true);
		CompilationUnit result = (CompilationUnit) parser.createAST(null);
		List types = result.types();
		TypeDeclaration typedec = (TypeDeclaration) types.get(0);
		
		MethodDeclaration[] methdec = typedec.getMethods();
		
		for (MethodDeclaration met : methdec){
			getParamList(met);
		}
	}
	
	public static void main(String[] args){
		String filename = "F:\\JavaProjects\\DocumentOmissionDetect\\testFiles\\ExtraAnalysis\\BasicStroke.java";
		String content = getFileContent(new File(filename));
		AnalysisJavaFileContent(content);
	}
	
}
