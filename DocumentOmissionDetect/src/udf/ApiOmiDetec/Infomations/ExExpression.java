package udf.ApiOmiDetec.Infomations;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

public class ExExpression {
	//	外部标记该expression是顺意义还是逆意义
	private boolean Obey;	//	true-肯定		false-否定
	private Expression theExp;
	
	public ExExpression(Expression exp, boolean ori){
		this.theExp = exp;
		this.Obey = ori;
	}
	
	public void setExpression(Expression exp)
	{	this.theExp = exp;	}
	public void setObeyOrDeny(boolean set)
	{	this.Obey = set;	}
	
	public Expression getExpression()
	{	return this.theExp;		}
	public boolean isObay()
	{	return this.Obey;	}
	
	public String toString(){
		String res = "";
		if (!this.Obey){
			res = "!(" + this.theExp.toString() + ")";
		}else{
			res = this.theExp.toString();
		}
		return res;
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
	public static String getSimExpressions(Expression exp, boolean posi){
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

}
