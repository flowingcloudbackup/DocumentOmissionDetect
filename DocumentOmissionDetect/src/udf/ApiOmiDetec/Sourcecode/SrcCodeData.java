package udf.ApiOmiDetec.Sourcecode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import udf.ApiOmiDetec.Globals.GlobalFiles;
import udf.ApiOmiDetec.Infomations.ExExpression;
import udf.ApiOmiDetec.Infomations.InfoBox;

public class SrcCodeData {
	
	private Block srcblock;
	
	private String methodName;
	private List<SingleVariableDeclaration> methodParams;
	
	private List<InfoBox> infoBoxes;
	public SrcCodeData(Block bl, String name, 
				List<SingleVariableDeclaration> params){
		
		this.srcblock = bl;
		
		this.methodName = name;
		this.methodParams = params;
		
		this.infoBoxes = new FindingFailurePoints(this.srcblock, methodName, this.methodParams).getInfoBoxes();
		//	获取infoBoxes之后需要进行进一步处理
		for (InfoBox box : this.infoBoxes){
			box.SinglizeExpressions();
		}
		splitNonsenseSingleExpressions();
		//	对于每个infoBox，删掉那些不包含param的触发条件
		
		//System.out.println(this.srcblock);
		for (InfoBox box : this.infoBoxes){
			if (//box.expList.size() > 0 && 
					box.singleExpList.size() > 0){
				//System.out.println(box.presentation());
				//	有条件的infoBoxes都打印出来
				try {
					GlobalFiles.PrintIntoConsole(box.presentation());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public List<InfoBox> getBoxes()
	{	return this.infoBoxes;		}
	
	private void splitNonsenseExpressions(){
		for (InfoBox box : this.infoBoxes){
			List<ExExpression> resExps = new ArrayList();
			for (ExExpression exp : box.expList){
				if (StringContainsParameters(exp.toString())){
					resExps.add(exp);
				}
			}
			box.expList = resExps;
		}
	}
	private void splitNonsenseSingleExpressions(){
		for (InfoBox box : this.infoBoxes){
			List<String> resExps = new ArrayList();
			for (String exp : box.singleExpList){
				if (StringContainsParameters(exp)){
					resExps.add(exp);
				}
			}
			box.singleExpList = resExps;
		}
	}
	private boolean StringContainsParameters(String expression){
		String testStr = "%" + expression + "%";
		String reglex;
		boolean flag = false;
		for (SingleVariableDeclaration param : this.methodParams){
			reglex = "[^a-zA-Z0-9]" + param.getName().getIdentifier() + "[^a-zA-Z0-9]";
			Pattern pat = Pattern.compile(reglex);
			Matcher mat = pat.matcher(testStr);
			if (mat.find()){
				flag = true;
				break;
			}
		}
		
		return flag;
	}
	
	public static void main(String[] args) throws IOException{
		String filename = "F:/JavaProjects/DocumentOmissionDetect/testFiles/testStatementIterate.java";
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
			SrcCodeData src = new SrcCodeData(met.getBody(), "testStatementIterate.java",
					met.parameters());
			
			for(InfoBox box : src.getBoxes()){
				if (box.expList.size() >= 1 ){
					System.out.println(box.presentation());
				}
			}
		}
	}

}
