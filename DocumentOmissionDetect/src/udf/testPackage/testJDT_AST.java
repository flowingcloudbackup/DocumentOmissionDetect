package udf.testPackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import HeuristicExtraction.dataStructure.ParamInfo;
import udf.ApiOmiDetec.Globals.GlobalValues;
import udf.ApiOmiDetec.Infomations.InfoBox;
import udf.ApiOmiDetec.Sourcecode.FindingFailurePoints;

public class testJDT_AST {

	private static void ParamOperation(String s, String pars){
		List<ParamInfo> parList = ParamInfo.getParamList(pars);
		
		for (ParamInfo p : parList){
			Pattern pat = Pattern.compile("\\W" + p.paramName + "\\.\\w+");
			Matcher mat = pat.matcher(" " + s + " ");
			while(mat.find()){
				System.out.println(mat.group());
			}
		}
	}
	
	public static void main(String[] args) throws IOException{
		String filePath = "F:/JavaProjects/DocumentOmissionDetect/testFiles/";//testStatementIterate.java";
		String fileName =
				//"Container.java";
				//"testStatementIterate.java";
				//"testsoloif.java";
				//"testfirstreturn.java";
				"testErro/testStatementsName.java";
		File file = new File(filePath + fileName);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String strline;
		String fileContent = "";
		while((strline=reader.readLine())!=null){
			fileContent += strline + "\n";
		}
		reader.close();
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(fileContent.toCharArray());
		parser.setResolveBindings(true);
		CompilationUnit result = (CompilationUnit) parser.createAST(null);
		TypeDeclaration root = (TypeDeclaration)result.types().get(0);
		
		String parStr = "(int-a,String-b)";
		GlobalValues.InitTotalNumbers();
		for (MethodDeclaration met : root.getMethods()){
			//System.out.println(met.getName());
			Block block = met.getBody();
			//System.out.println(block);
			List<Statement> states = block.statements();
			for (Statement s : states){
				//System.out.println(s.toString()+ "\t" + s.getClass().toString());
				ParamOperation(s.toString(), parStr);
			}
			
		}
	}
}
