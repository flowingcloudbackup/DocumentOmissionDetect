package udf.testPackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class testSeparateExpression {
	public static void main(String[] args) throws IOException{
		String filename = "F:/JavaProjects/DocumentOmissionDetect/testFiles/testIfStatement.java";
		File file = new File(filename);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String strline;
		String fileContent = "";
		while((strline=reader.readLine())!=null){
			fileContent += strline + "\n";
		}
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(fileContent.toCharArray());
		parser.setResolveBindings(true);
		CompilationUnit result = (CompilationUnit) parser.createAST(null);
		TypeDeclaration root = (TypeDeclaration)result.types().get(0);
		
		for (MethodDeclaration met : root.getMethods()){
			IfStatement ifs = (IfStatement)met.getBody().statements().get(1);
			Expression expression = ifs.getExpression();
			for (Object o : expression.structuralPropertiesForType()){
				System.out.println(o.getClass());
			}
			System.out.println(ifs.getExpression());
		}
	}

}
