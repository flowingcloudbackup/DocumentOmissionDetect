package udf.ApiOmiDetec.Infomations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import udf.ApiOmiDetec.Globals.GlobalTools;

public class InfoBox {
	
	public static enum InfoType{EXCEPTION, SOLOIF, FIRSTRETURN, FALSEJUDGMENT};
	
	private InfoType infoType;
	private String exception;
	private SingleVariableDeclaration[] params;
	public ParamType[] simParams;	//	这个类型用于存储从src.java文件中获取的参数信息
	
	public List<ExExpression> expList;
	public List<String> singleExpList;
	private List<String> ExpSrcList;	//	这个field用于存储可能存在的条件源语句
	
	public InfoBox(String type, String excep, String pars, List<String> expresses, List<String> srcExp){
		//	这个构造方法用于从src.res文件中获取InfoBox
		this.infoType = InfoType.valueOf(type);
		this.exception = excep;
		String[] paramInits = pars.split(",");
		int paramNum = paramInits.length;
		this.simParams = new ParamType[paramNum];
		for (int i = 0;i< paramNum; i++){
			this.simParams[i] = new ParamType(paramInits[i]);
		}
		this.simParams = ParamType.SimpliseParamInfos(this.simParams);
		this.singleExpList = expresses;
		this.ExpSrcList = srcExp;
		this.expList = null;
	}
	
	public InfoBox(InfoType type, String exp, SingleVariableDeclaration[] pars){
		this.infoType = type;
		this.exception = (type == InfoType.EXCEPTION)?exp : "NO EXCEPTION";
		this.params = pars;
		
		this.expList = new ArrayList();
	}
	
	public InfoBox(InfoType type, String exp, SingleVariableDeclaration[] pars, List<String> conds){
		//	从程序分析中构造infobox专用
		this.infoType = type;
		this.exception = (type == InfoType.EXCEPTION)?exp : "NO EXCEPTION";
		this.params = pars;
		
		this.expList = null;
		this.ExpSrcList = conds;
	}
	
	public static InfoBox ExcepSample = 
			new InfoBox(InfoBox.InfoType.EXCEPTION, 
					"IllegalArgumentException",
					null//new String[]{"comp", "container"}
			);
	
	public void SinglizeExpressions(){
		this.singleExpList = new ArrayList();
		/*
		for (ExExpression exp : this.expList){
			if (exp.isObay()){
				String[] singleOnes = exp.toString().split("[&|]");
				for (String s : singleOnes){
					if (s.length() > 0){
					//this.singleExpList.addAll(Arrays.asList(singleOnes));
						this.singleExpList.add(s);
					}
				}
			}else{
				this.singleExpList.add(exp.toString());
			}
		}*/
		if (this.expList != null){
			for (ExExpression exp : this.expList){
				String newExp = ExExpression.getSimExpressions(exp.getExpression(), exp.isObay());
				this.singleExpList.add(newExp);
			}
		}else{
			for (String exp : this.ExpSrcList){
				this.singleExpList.add(exp);
			}
		}
	}
	
	public void setParams(SingleVariableDeclaration[] pas)
	{	this.params = pas;	}
	
	public String getException()
	{	return this.exception;	}
	public SingleVariableDeclaration[] getParameters()
	{	return this.params;		}
	public String[] getParameterNames(){
		List<String> resList = new ArrayList<String>();
		if (!(this.params == null || this.params.length <= 0)){
				//	如果this.param可访问
			for (SingleVariableDeclaration v : this.params){
				resList.add(v.getName().toString());
			}
		}else if (!(this.simParams == null || this.simParams.length <= 0)){
				//	如果 this.simParams 可访问
			for (ParamType p : this.simParams){
				resList.add(p.getName());
			}
		}
		String[] res = new String[resList.size()];
		int i = 0;
		for (String par : resList){
			res[i++] = par;
		}
		
		return res;
	}
	public InfoType getInfoType()
	{	return this.infoType;	}
	
	public String formsFuzzyCompareExpression(){
		String res = "";
		if (this.expList != null){
			for (ExExpression ex : this.expList){
				res += "(" + ex.toString() + ")" + "&&";
			}
		}else{
			for (String ex : this.singleExpList){
				res += "(" + ex + ")" + "&&";
			}
		}
		if (res.length() > 2){	res = res.substring(0, res.length() -2);}
		res = ChangingAllExps(res);	//	把<=之类 改成！> 等等。。
		return res;
	}
	public String presentation(){
		String res = "Type:\t" + this.infoType.toString() + "\n";
		res  += "Except:\t" + this.exception + "\n";
		res += "params:\t";
		if ((this.params == null || this.params.length <= 0)
				&&(this.simParams == null || this.simParams.length <= 0)){
					//	this.params没有，并且this.simParams也没有
			res += "No params";
		}else if(this.params != null && this.params.length > 0){
			for (SingleVariableDeclaration p : this.params){
				res += p.getType().toString() + "-" + p.getName() + ", ";
			}
			res = res.substring(0, res.length()-2);
		}else if (this.simParams != null && this.simParams.length > 0){
			for (ParamType p : this.simParams){
				res += p.getType() + "-" + p.getName() + ", ";
			}
			res = res.substring(0, res.length()-2);
		}
		if (!(this.expList == null || this.expList.size() <= 0)){
			res += "\nExpresses:\n";
			for (ExExpression ex : this.expList){
				res += ex.toString();
			}
		}else{
			res += "\nNo Expresses Info";
		}
		res += "\nSingle Expresses:\n";
		for (String str : this.singleExpList){
			res += str + "\n";
		}
		if (!(this.ExpSrcList == null || this.ExpSrcList.size() <= 0)){
			res += "Expression Sources:\n";
			for (String exp : this.ExpSrcList){
				res += exp + "\n";
			}
		}else{
			res += "No Expression Sources:\n";
		}
		//res += "=================================================	";
		return res;
	}
	private String ParamTypeStringPreProcess(String type){
		//	对于可能存在的复杂的参数类型进行简化。如 Map<String, Integer>要改成 Map
		int index = type.indexOf("<");
		if (index > 0){
			return type.substring(0, index);
		}else{
			return type;
		}
	}
	public String ToPlantString(){
		//	 返回一串单行字符串
		String res = "[";
		if (this.infoType.equals(InfoType.EXCEPTION)){
			res += this.exception;
		}else{
			res += this.infoType.toString();
		}
		res += "][";
		
		for (ParamType p : this.simParams){
			res += "[" + p.getType() + "-" + p.getName() + "]";
		}
		res += "][";
		for (String exp : this.singleExpList){
			res += "[" + exp + "]";
		}
		res += "]";
		
		return res;
	}
	public String PresentForStorage(){
		String res = this.infoType.toString() + "\n";
		res += this.exception + "\n";
		if (this.params == null || this.params.length <= 0){
			res += " \n";
		}else{
			for (SingleVariableDeclaration p : this.params){
				res += ParamTypeStringPreProcess(p.getType().toString());
				res += "-" + p.getName() + ",";
			}
			res = res.substring(0, res.length()-1);
		}
		int size;
		if (this.expList!= null){ size = this.expList.size();}
		else{	size = this.ExpSrcList.size();	}
		res += "\n" + size + "\n";
		if (this.expList != null){
			for (ExExpression ex : this.expList){
				res += ex.toString() + "\n";
			}
		}else{
			for (String ex : this.ExpSrcList){
				res += ex + "\n";
			}
		}
		
		size = this.singleExpList.size();
		res += size+ "\n";
		for (String str : this.singleExpList){
			res += str + "\n";
		}
		
		return res;
	}
	
	public void RefreshParamInfos(List<String[]> passedParam){
		List<ParamType> res = new ArrayList<ParamType>();
		for (ParamType thisParam : this.simParams){
			for (String[] passedPars : passedParam){
				if (passedPars[2].equals(thisParam.getName())){
					//	找到当前参数对应的传递信息
					res.add(new ParamType(passedPars[0] + "-" + passedPars[1]));
					//	接下来要把this换成passedPar[1]
					for (String exp : this.ExpSrcList){
						exp = replaceParam(exp, thisParam.getName(), passedPars[1]);
					}
					break;
				}
			}
		}
		ParamType[] resArray = new ParamType[res.size()];
		int i = 0;
		for (ParamType p : res){
			resArray[i++] = p;
		}
		this.simParams = resArray;
	}
	
	public boolean HasParameters(){
		return (
				(this.params != null && this.params.length > 0)
				||
				(this.simParams != null && this.simParams.length > 0)
				);
		//	如果 this.params 或者 this.simParams 有一个不为空，则返回true
	}
	
	private String replaceParam(String srcStr, String comp, String toReplace){
		String rlExp = "\\W+" + comp + "\\W+";
		String resStr = "%" + srcStr + "%";
		
		Pattern pat = Pattern.compile(rlExp);
		Matcher mat = pat.matcher(resStr);
		List<String> groups = new ArrayList<String>();
		while(mat.find()){
			groups.add(mat.group());
		}
		groups = GlobalTools.RemoveDuplicated(groups);	//	去重复
		
		for (String torpl : groups){
			String t = torpl.replaceAll(comp, toReplace);
			resStr = resStr.replaceAll(
					GlobalTools.ProcessSpecialNote(torpl), 
					GlobalTools.ProcessSpecialNote(t));
		}
		
		resStr = resStr.substring(1);
		resStr = resStr.substring(0, resStr.length()-1);
		
		return resStr;
	}
	
	public static List<InfoBox> RefreshParamInfosInInfoBoxList(List<InfoBox> boxes, List<String[]> passedParam){
		for (InfoBox box : boxes){
			box.RefreshParamInfos(passedParam);
		}
		return boxes;
	}
	//===================以下都是工具方法===================
	public static HashMap<String, String> ChangeMap = new HashMap<String, String>(){
		{
			put("<=", ">");	//	a<=b → !a>b
			put(">=", "<");	//	a>=b → !a<b
			put("!=", "==");//	a!=b → !a==b
		}
	} ;
	public static char[] ParCharSet = "._ \t".toCharArray();
	
	public static String replacingFirstWord(String str, String rp1, String rp2){
		//	replace the first rp1 of str with rp2
		//	用rp2替换str中第一个rp1
		int index = str.indexOf(rp1);
		if (index < 0)
			//	如果没有，就返回原字符串
			return str;
		String res = str.substring(0, index);
		res += rp2;
		res += str.substring(index + rp1.length());
		return res;
	}
	private static boolean IsLegalParamChar(char c){
		if (c >= '0' && c <= '9') return true;
		if (c >= 'a' && c <= 'z') return true;
		if (c >= 'A' && c <= 'Z') return true;
		boolean flag = false;
		for (char ch : ParCharSet){
			if (c == ch){ flag = true;break;}
		}
		return flag;
	}
	public static String addNegSymbol(String exp, int index){
		char[] chars = exp.toCharArray();
		String resA;
		String resB;
		for(;index >= 0;index--){
			if (!IsLegalParamChar(chars[index])){
				break;
			}
		}
		resA = exp.substring(0, index +1);
		resB = exp.substring(index + 1);
		
		return resA + "!" + resB;
	}
	public static String ChangingAllExps(String exp){
		for (Iterator itr = ChangeMap.keySet().iterator(); itr.hasNext();){
			String key = (String)itr.next();
			int index = exp.indexOf(key);
			if (index < 0) continue;
			exp = replacingFirstWord(exp, key, ChangeMap.get(key));
			exp = addNegSymbol(exp, index-1);
		}
		
		return exp;
	}

}
