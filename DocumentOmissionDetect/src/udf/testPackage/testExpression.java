package udf.testPackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;

public class testExpression {
	
	class innerA{
		public String name;
		public int value;
		public innerA(){this.name = "null"; this.value = 1;}
	};
	
	private static String getFileContent(File file){
		String res = "";
		try{
			BufferedReader r = new BufferedReader(new FileReader(file));
			String line = r.readLine();
			while(line != null){
				res += line + "\n";
				line = r.readLine();
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		return res;
	}
	
	private static MethodDeclaration[] getMethods(String content){
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(content.toCharArray());
		parser.setResolveBindings(true);
		CompilationUnit result = (CompilationUnit) parser.createAST(null);
		TypeDeclaration root = (TypeDeclaration)result.types().get(0);
		
		return root.getMethods();
	}
	private static void IteratingStatementsList(List<Statement> list){
		for (Statement s : list){
			ScanningSentences(s);
		}
	}
	private static void ScanningSentences(Statement sen){
		if (sen instanceof Block){
			IteratingStatementsList(((Block)sen).statements());
		}else if (sen instanceof ForStatement){
			ScanningSentences(((ForStatement)sen).getBody());
		}else if (sen instanceof SynchronizedStatement){
			ScanningSentences(((SynchronizedStatement)sen).getBody());
		}else if (sen instanceof WhileStatement){
			ScanningSentences(((WhileStatement)sen).getBody());
		}else if (sen instanceof DoStatement){
			ScanningSentences(((DoStatement)sen).getBody());
		}else if (sen instanceof TryStatement){
			ScanningSentences(((TryStatement)sen).getBody());
		}
		
		else if(sen instanceof IfStatement){
			IfStatement ifsen = (IfStatement)sen;
			Expression exp = ifsen.getExpression();
			System.out.println("===================================");
			System.out.println(exp.toString());
			System.out.println("!(" + exp.toString() + ")");
			System.out.println(exp.getClass());
			System.out.println(getSimExpressions(exp, false));
			System.out.println("===================================");
			//ProcessExpression(exp);
			
			/*
			if (exp instanceof PrefixExpression){
				System.out.println("===================================");
				System.out.println(exp.toString());
				//getSimExpressions(exp, true);
				System.out.println(((PrefixExpression) exp).getOperand());
				System.out.println("===================================");
			}
			*/
		}
		/*if (sen instanceof Block){
			List<Statement> subSens = ((Block)sen).statements();
			for (Statement subSen : subSens){
				ResBoxes.addAll(ITRStatements(subSen));
			}
		}else if ((sen instanceof ForStatement)){
			ResBoxes = ITRStatements(((ForStatement)sen).getBody());
		}else if (sen instanceof SynchronizedStatement){
			ResBoxes = ITRStatements(((SynchronizedStatement)sen).getBody());
		}else if (sen instanceof WhileStatement){
			ResBoxes = ITRStatements(((WhileStatement)sen).getBody());
		}else if (sen instanceof DoStatement){
			ResBoxes = ITRStatements(((DoStatement)sen).getBody());
			//=================== may introduce bugs ========================
		}else if (sen instanceof IfStatement){
			ResBoxes = processIfStatement((IfStatement)sen);
		}else if (sen instanceof ThrowStatement){
			ResBoxes = processThrowStatement((ThrowStatement)sen);
		}else if (sen instanceof TryStatement){
			ResBoxes = processTryStatement((TryStatement)sen);
		}
		 * */
	}
	
	private static void ProcessExpression(Expression exppression){
		System.out.println("========================================================");
		System.out.println("find");
		InfixExpression exp = (InfixExpression)exppression;
		System.out.println(exp.getClass());
		System.out.println(exp.toString());
		
		Expression left =  exp.getLeftOperand();
		Expression right =  exp.getRightOperand();
		InfixExpression.Operator op = exp.getOperator();
		
		System.out.println("left:\t" + getObjToString(left));
		System.out.println("right:\t" + getObjToString(right));
		System.out.println("op:\t" + getObjToString(op));
		
		
		System.out.println("========================================================");
	}
	
	private static boolean ExpressionIsExpandable(Expression exp){
		if (exp instanceof InfixExpression){
			String op = ((InfixExpression) exp).getOperator().toString();
			return (op.equals("||") || op.equals("&&"));
		}
		if (exp instanceof ParenthesizedExpression)	return true;
		if (exp instanceof PrefixExpression)	return true;
		return false;
	}
	private static String getSimExpressions(Expression exp, boolean posi){
		//		仅分解3种情况
		if (!ExpressionIsExpandable(exp)){
			String res = posi?"" : "!";
			res += "(" + exp.toString() + ")";
			return res;
		}
		if (exp instanceof InfixExpression){
			InfixExpression infixExp = (InfixExpression)exp;
			String left = getSimExpressions(infixExp.getLeftOperand(), posi);
			String right = getSimExpressions(infixExp.getRightOperand(), posi);
			String op = infixExp.getOperator().toString();
			if (!posi){
				if (op.equals("||")){ op = "&&"; }
				else if (op.equals("&&")){op = "||";}
			}
			return "(" + left + ")" + op + "(" + right + ")";

		}else if (exp instanceof ParenthesizedExpression){
			return "(" + getSimExpressions(((ParenthesizedExpression) exp).getExpression(), posi) + ")";
		}else if (exp instanceof PrefixExpression){
			String op = ((PrefixExpression) exp).getOperator().toString();
			if (op.equals("!")){
				if (posi)	return "!" + getSimExpressions(((PrefixExpression) exp).getOperand(), posi);
				else return getSimExpressions(((PrefixExpression) exp).getOperand(), !posi);
			}else{
				return exp.toString();
			}
		}
		
		return exp.toString();
	}
	
	private static String getObjToString(Object obj){
		String res = "";
		res += obj.getClass() + "\t-\t";
		if (obj == null) res += "null";
		res += obj.toString();
		return res;
	}
	
	public static void main(String[] args){
		File file = new File("F:\\JavaProjects\\DocumentOmissionDetect\\testFiles\\testAST\\testStatements.java");
		String content = getFileContent(file);
		MethodDeclaration[] methods = getMethods(content);
		for (MethodDeclaration met : methods){
			ScanningSentences(met.getBody());
		}
	}

}
