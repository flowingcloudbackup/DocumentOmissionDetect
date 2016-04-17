package udf.ApiOmiDetec.Sourcecode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.attribute.DosFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.internal.resources.projectvariables.ParentVariableResolver;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;

import udf.ApiOmiDetec.Globals.GlobalSettings;
import udf.ApiOmiDetec.Globals.GlobalValues;
import udf.ApiOmiDetec.Infomations.ExExpression;
import udf.ApiOmiDetec.Infomations.InfoBox;
import udf.ApiOmiDetec.Infomations.InfoBox.InfoType;

public class FindingFailurePoints {
	
	public static final List<String> SimpleVarTypes = Arrays.asList(new String[]{
			"int", "short", "long", "byte", "float", "double", "char", "boolean"
	});
	
	private Block srcBlock;
	private String metName;
	private List<InfoBox> infoBoxes;
	private List<SingleVariableDeclaration> ParamDeclas;
	
	public FindingFailurePoints(Block block, String methodName, 
			List<SingleVariableDeclaration> paras){
		this.srcBlock = this.splitSynchronizedStatement(block);
		this.metName = methodName;
		this.infoBoxes = null;
		this.ParamDeclas = paras;
		
		this.infoBoxes = new ArrayList();
		
		ExtractInfoBoxes_Exceptions();
		ExtractInfoBoxes_NullPointers();
		ExtractInfoBoxes_SoloIf();
		ExtractInfoBoxes_FirstReturn();
		
		//testFunctions();
		/*
		 * 移动到 ExtractInfoBoxes() 方法中去
		try{
			this.infoBoxes = new ArrayList();
			List<InfoBox> tempList = ITRStatements(this.srcBlock);
			//	提取有实际意义的exp信息
			for (InfoBox info : tempList){
				if (info.expList.size() > 0){
					this.infoBoxes.add(info);
					//System.out.println(info.presentation());
				}
			}
		}catch(Exception e){
			this.infoBoxes = null;
			System.out.println(methodName + " can not be analysised correctly!!");
		}
		*/
		 
	}
	
	private void ExtractInfoBoxes_FirstReturn(){
		
		List<InfoBox> fstRets = FirstReturnInfoBoxes(this.srcBlock, new ArrayList());
		for (InfoBox box : fstRets){
			//	验证box是否与param有交集
			SingleVariableDeclaration[] paramsInvolved = this.getInvolvedParams(box);
			if (paramsInvolved == null || paramsInvolved.length <= 0){continue;}
			box.setParams(paramsInvolved);
			this.infoBoxes.add(box);
		}
	}
	
	private List<InfoBox> FirstReturnInfoBoxes(Statement sen, List<ExExpression> express){
		
		if (sen == null) return new ArrayList();
		String classStr = sen.getClass().toString();
		List<InfoBox> resInfoBoxes = null;
		if(GlobalSettings.FIRSTRETURN_Types_AcceptableOtherStatements.get(classStr) != null){
			if (sen instanceof ExpressionStatement &&
					(((ExpressionStatement) sen).getExpression() instanceof MethodInvocation)){
				return null;
			}
			return new ArrayList();
		}
		if (sen instanceof Block){
			List<Statement> subStats = ((Block)sen).statements();
			resInfoBoxes = new ArrayList();
			for (int i = 0 ;i< GlobalSettings.FIRSTRETURN_Num_AcceptableOtherStatements + 1; i++){
				if ( i >= subStats.size()){	break;	}
				List<InfoBox> subList = FirstReturnInfoBoxes(subStats.get(i), express);
				if (subList == null){	break;	}
				resInfoBoxes.addAll(subList);
			}
		}else if(sen instanceof ReturnStatement){
			Expression returnExpress = ((ReturnStatement)sen).getExpression();
			if (returnExpress != null && !(returnExpress instanceof NullLiteral)){
				;
			}else{
				InfoBox resBox = new InfoBox(InfoBox.InfoType.FIRSTRETURN, null, null);
				resBox.expList = express;
				resInfoBoxes = new ArrayList();
				resInfoBoxes.add(resBox);
			}
		}else if(sen instanceof IfStatement){
			Expression exp = ((IfStatement)sen).getExpression();
			IfStatement ifSt = (IfStatement)sen;
			
			List<ExExpression> thenExpress = new ArrayList();
			thenExpress.addAll(express);
			thenExpress.add(new ExExpression(exp, true));
			List<ExExpression> elseExpress = new ArrayList();
			elseExpress.addAll(express);
			elseExpress.add(new ExExpression(exp, false));
			
			resInfoBoxes = new ArrayList();
			List<InfoBox> thenBoxes = FirstReturnInfoBoxes(ifSt.getThenStatement(), thenExpress);
			if (thenBoxes != null){
				resInfoBoxes.addAll(thenBoxes);
			}
			List<InfoBox> elseBoxes = FirstReturnInfoBoxes(ifSt.getElseStatement(), elseExpress);
			if (elseBoxes != null){
				resInfoBoxes.addAll(elseBoxes);
			}
		}
		
		return resInfoBoxes;
	}
	
	private void ExtractInfoBoxes_SoloIf(){
		//List<Statement> subStats = this.srcBlock.statements();
		InfoBox resBox = new InfoBox(InfoBox.InfoType.SOLOIF, null, null);
		List<ExExpression> express = null;
		/*
		if (splitSynchronized.size() == 1 && splitSynchronized.get(0) instanceof SynchronizedStatement){
			express = SoloIfExpressions(
					((SynchronizedStatement)splitSynchronized.get(0)).getBody());
		}else{
		*/
		express = SoloIfExpressions(this.srcBlock);
		
		if (express == null || express.size() <= 0)	return;
		// 判断express是否与param有交叉
		resBox.expList = express;
		SingleVariableDeclaration[] paras = this.getInvolvedParams(resBox);
		if (paras.length <= 0) return;
		resBox.setParams(paras);
		resBox.SinglizeExpressions();
		
		this.infoBoxes.add(resBox);
	}
	
	private List<ExExpression> SoloIfExpressions(Statement sen){
		if (sen instanceof Block){
			List<Statement> subStats = ((Block)sen).statements();
			int senNum = subStats.size();
			if (senNum <= GlobalSettings.SOLOIF_Num_AcceptableOtherStatements + 1
					&&
					senNum > 0){
				boolean acceptable = true;
				for (int i = 0; i < senNum-1 ; i++){
					String senType = subStats.get(i).getClass().toString();
					if (GlobalSettings.SOLOIF_Types_AcceptableOtherStatements.get(senType) == null){
						acceptable = false;
						break;
					}
					if (sen instanceof ExpressionStatement &&
							(((ExpressionStatement) sen).getExpression() instanceof MethodInvocation)){
						acceptable = false;
						break;
					}
				}
				if (acceptable){
					return SoloIfExpressions(subStats.get(senNum-1));
				}
			}
		}
		if (sen instanceof IfStatement){
			IfStatement state = (IfStatement)sen;
			if (state.getElseStatement() == null){

				List<ExExpression> express = new ArrayList();
				express.add(
						new ExExpression(state.getExpression(),true)
						);
				List<ExExpression> thenExps = SoloIfExpressions(state.getThenStatement());
				if (thenExps != null)
					express.addAll(thenExps);
				
				return express;
			}
		}
		return null;
	}
	
	private void ExtractInfoBoxes_Exceptions(){
		try{
			List<InfoBox> tempList = ITRStatements(this.srcBlock);
			for (InfoBox info : tempList){
				if (info.expList.size() > 0){
					//	至少有一条条件才能入选
					SingleVariableDeclaration[] paramsInvolv = this.getInvolvedParams(info);
					if(paramsInvolv.length > 0){
						info.setParams(paramsInvolv);
						this.infoBoxes.add(info);
					}
				}
			}
		}catch(Exception e){
			this.infoBoxes = null;
			System.out.println(this.metName + " can not be analysised correctly!!");
		}
	}
	
	private boolean MayCauseNullPointer(SingleVariableDeclaration par){
		if (SimpleVarTypes.contains(par.getType().toString()))	return false;
		boolean flag = false;
		if (this.srcBlock == null){
			return false;
		}
		List<Statement> strs = this.srcBlock.statements();
		Pattern pat = Pattern.compile("\\W" + par.getName().toString() + "\\.\\w+");
		Matcher mat;
		for (Statement s : strs){
			mat = pat.matcher(" " + s.toString() + " ");
			if (mat.find()){
				flag = true;
				break;
			}
		}
		
		return flag;
	}
	private void ExtractInfoBoxes_NullPointers(){
		try{
			List<InfoBox> tempList = new ArrayList<InfoBox>();
			for (SingleVariableDeclaration par : this.ParamDeclas){
				if (MayCauseNullPointer(par)){
					//	如果可能造成 NullPointerException， 手动添加infoBox
					List<String> conds = new ArrayList<String>();
					conds.add(par.getName().toString() + " == null");
					InfoBox newBox = new InfoBox(InfoBox.InfoType.EXCEPTION, "NullPointerException", 
												new SingleVariableDeclaration[]{par},
												conds);
					this.infoBoxes.add(newBox);
				}
			}
		}catch(Exception e){
			this.infoBoxes = null;
			System.out.println(this.metName + " can not be analysised correctly!!");
		}
	}
	
	private SingleVariableDeclaration[] getInvolvedParams(InfoBox box){
		List<SingleVariableDeclaration> resParams = new ArrayList();
		for (ExExpression exp : box.expList){
			String expStr = exp.toString();
			String[] snippets = expStr.split("[\\s\\()\\.,=+-/!\\*]");
			for (String s : snippets){
				for (SingleVariableDeclaration p : this.ParamDeclas){
					if (s.equals(p.getName().toString())){
						resParams.add(p);
					}
				}
			}
		}
		
		int num = resParams.size();
		SingleVariableDeclaration[] resParamArray = new SingleVariableDeclaration[num];
		for (int i = 0;i< num ;i++){
			resParamArray[i] = resParams.get(i);
		}
		return resParamArray;
	} 
	@Deprecated
	private List<InfoBox> IterateStatements(Statement sen){
		List<InfoBox> ResBoxes = new ArrayList();
		
		if (sen instanceof Block){
			List<Statement> subSens = ((Block)sen).statements();
			for (Statement subSen : subSens){
				ResBoxes.addAll(IterateStatements(subSen));
			}
			return ResBoxes;
		}else if (sen instanceof ForStatement){
			return IterateStatements(((ForStatement)sen).getBody());
		}else if (sen instanceof SynchronizedStatement){
			return IterateStatements(((SynchronizedStatement)sen).getBody());
		}else if (sen instanceof WhileStatement){
			return IterateStatements(((WhileStatement)sen).getBody());
		}else if (sen instanceof DoStatement){
			return IterateStatements(((DoStatement)sen).getBody());
		//}else if (sen instanceof SwitchStatement){
		//	return nullRes;
			//=================== may introduce bugs ========================
		}else if (sen instanceof IfStatement){
			return processIfStatement((IfStatement)sen);
		}else if (sen instanceof ThrowStatement){
			//String exp = ((ThrowStatement)sen).getExpression().toString();
			//InfoBox newbox = new InfoBox(InfoBox.InfoType.EXCEPTION, exp, null);
			return processThrowStatement((ThrowStatement)sen);
		}else if (sen instanceof TryStatement){
			return processTryStatement((TryStatement)sen);
		}
		return ResBoxes;
	}
	
	private List<InfoBox> ITRStatements(Statement sen){
		
		List<InfoBox> ResBoxes = new ArrayList();
		
		if (sen instanceof Block){
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
		return ResBoxes;
	}
	
	private List<InfoBox> processThrowStatement(ThrowStatement sen){
		Expression exp = sen.getExpression();
		String expStr = "";
		InfoBox resBox = null;
		List<InfoBox> resList = new ArrayList();
		
		if (exp instanceof ClassInstanceCreation){
			ClassInstanceCreation exp1 = (ClassInstanceCreation)exp;
			expStr = exp1.getType().toString();
			resBox = new InfoBox(InfoBox.InfoType.EXCEPTION, expStr, null);
			resList.add(resBox);
		}
		
		return resList;
	}
	
	private List<InfoBox> processTryStatement(TryStatement sen){
		List<InfoBox> resList = new ArrayList();
		
		for (Object o : sen.catchClauses()){
			if (!(o instanceof CatchClause)){
				continue;
			}
			CatchClause clause = (CatchClause)o;
			/*
			String expStr = clause.getException().getType().toString();
			//System.out.println(expStr);
			InfoBox newBox = new InfoBox(InfoType.EXCEPTION, expStr, null);
			resList.add(newBox);
			*/
			resList.addAll(ITRStatements(clause.getBody()));
		}
		
		return resList;
	}
	
	private List<InfoBox> processIfStatement(IfStatement sen){
		/* I think it should be done there to depart them as ComplexIf vs AInstanceofB
		 * */
		List<InfoBox> inThenStats = ITRStatements(sen.getThenStatement());
		List<InfoBox> inElseStats = ITRStatements(sen.getElseStatement());
		for (InfoBox box1 : inThenStats){
			box1.expList.add(
					new ExExpression(sen.getExpression(), true)
					);
		}
		for (InfoBox box2 : inElseStats){
			box2.expList.add(
					new ExExpression(sen.getExpression(), false)
					);	//	there should be the reverse of expression
		}
		
		List<InfoBox> res = new ArrayList();
		res.addAll(inThenStats);
		res.addAll(inElseStats);
		
		return res;
	}
	
	private Block splitSynchronizedStatement(Block src){
		if (src == null) 
			return null;
		List<Statement> substs = src.statements();
		if (substs.size() == 1){
			Statement sta = substs.get(0);
			if (sta instanceof SynchronizedStatement){
				return ((SynchronizedStatement)sta).getBody();
			}
		}
		return src;
	}
	
	public List<InfoBox> getInfoBoxes()
	{	return this.infoBoxes;	}
	
	public void testFunctions(){
		List ss = this.srcBlock.statements();
		for (Object obj : ss){
			System.out.println(obj.getClass().toString());
		}
	}
	
	public static void main(String[] args) throws IOException{
		String filePath = "F:/JavaProjects/DocumentOmissionDetect/testFiles/";//testStatementIterate.java";
		String fileName =
				//"Container.java";
				//"testStatementIterate.java";
				//"testsoloif.java";
				//"testfirstreturn.java";
				"testErro/Component.java";
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
		
		GlobalValues.InitTotalNumbers();
		for (MethodDeclaration met : root.getMethods()){
			if (!met.getName().toString().contains("createBufferStrategy"))	continue;
			System.out.println(met.getName());
			
			List<InfoBox> infoBoxes = 
			new FindingFailurePoints(met.getBody(), fileName, //"testStatementIterate.java",
					met.parameters()).getInfoBoxes();
			
			for (InfoBox box : infoBoxes){
				box.SinglizeExpressions();
				System.out.println(box.presentation());
				GlobalValues.CountSummaryNumber(box);
			}
			
		}
		GlobalValues.PublishTotalNumbers();
	}

}
