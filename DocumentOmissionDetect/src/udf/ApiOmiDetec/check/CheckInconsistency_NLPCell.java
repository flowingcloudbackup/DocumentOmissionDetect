package udf.ApiOmiDetec.check;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import HeuristicExtraction.dataStructure.MultiConstraints;
import HeuristicExtraction.dataStructure.ParamInfo;
import HeuristicExtraction.inThrowTag.ConsInThrow;
import HeuristicExtraction.inThrowTag.MultiConstraintsSeperator;
import HeuristicExtraction.inThrowTag.NullConstraint;
import HeuristicExtraction.inThrowTag.SetValueCons;
import HeuristicExtraction.inThrowTag.TypeCons;
import HeuristicExtraction.inThrowTag.ValueCons;
import udf.ApiOmiDetec.FileProcessers.DocFileReader;
import udf.ApiOmiDetec.FileProcessers.InfoBoxesReader;
import udf.ApiOmiDetec.FileProcessers.MethodInfoReader;
import udf.ApiOmiDetec.Infomations.InconsistencyResultInfomation;
import udf.ApiOmiDetec.Infomations.InfoBox;
import udf.ApiOmiDetec.Infomations.ParamType;
import udf.ApiOmiDetec.Javadoc.AnnotationData;
import udf.ApiOmiDetec.Javadoc.processedTagElement;

public class CheckInconsistency_NLPCell {
	/*	将要返回查找结果, start with 下列几个情况
	 * 	1.	NoAnnDoc-当前不存在annotation文档
	 * 	2.	AnnPerfect-没有不一致情况
	 *  3.	DescripErr-描述错误，代码条件和文档条件不一致
	 *  4.	DescripConfusing-文档描述模糊
	 *  5.	NotMetioned-对该异常，文档未提及
	 *  6.	InOtherTags-不在@throws中进行的描述		只存在于 Exception 的 smell中
	 * */
	public static final String InconsisCase_NoAnn = "NoAnnDoc";
	public static final String InconsisCase_Perfect = "AnnPerfect";
	public static final String InconsisCase_DescErr = "DescripErr";
	public static final String InconsisCase_DescConf = "DescripConfusing";
	public static final String InconsisCase_NotMent = "NotMetioned";
	public static final String InconsisCase_OtherTag = "InOtherTags";
	
	public static enum ConstraintType{NULL_CONSTRAINT, VALUE_CONSTRAINT, TYPE_CONSTRAINT, SET_CONSTRAINT,
				KEY_MATCH, NO_MATCH};
	
	/*	一致化规则记录：
	 * 		1. zero -> 0
	 * 		2. 1.0, 0.0这类浮点数化成整数	//	暂无实现方法
	 * */
	
	public static final HashMap<String, String> SpecialFieldReplacedWord = new HashMap<String, String>(){
		{
			put("zero", "0");
		}
	};
	public static final String[] SpecialFiledReplacedWordList = new String[]{
		"zero"
	};
	
	private InfoBox infoBox;
	private AnnotationData annData;
	private String ParamString;
	
	private List<processedTagElement> tags;
	//private List<String> InconsPrimeInfos;
	private String InconsInfo;
	
	public boolean CreateSuccessful = true;
	
	private boolean PerfectMatch = false;
	private boolean DocMatch = false;
	
	public ConstraintType ConsType = ConstraintType.NO_MATCH;	// 记录当前约束是何种类型
	public String InWhichTag = "NoTag";
	
	public static CheckInconsistency_NLPCell createNewInstance(InfoBox srcInfo, AnnotationData ann, String pars){
		CheckInconsistency_NLPCell newCheck = new CheckInconsistency_NLPCell(srcInfo, ann, pars);
		if (newCheck.CreateSuccessful == false){	return null;	}
		return newCheck;
	}
	private CheckInconsistency_NLPCell(InfoBox srcInfo, AnnotationData ann, String pars){
		
		this.InconsInfo = InconsisCase_Perfect;	//	先设定为正确
		
		this.infoBox = srcInfo;
		this.annData = ann;
		this.ParamString = pars;
		
		if (this.annData == null || this.infoBox == null){
			this.CreateSuccessful = false;
			return;
		}
		this.tags = this.annData.getProcessedTagData();
	}
	
	public int FirstAnnotationTagMetionedFailure(){
		//	将就老的API
		FindInConsistency();
		if (this.InconsInfo.equals(InconsisCase_NoAnn))	return -2;
		if (this.InconsInfo.equals(InconsisCase_NotMent)) return -1;
		if (this.InconsInfo.equals(InconsisCase_DescErr)) return -3;
		else return 1;
	}
	public String FindInConsistency(){
		/*	返回查找结果, start with 下列几个情况
		 * 	NoAnnDoc-当前不存在annotation文档
		 * 	Perfect-没有不一致情况
		 *  DescripErr-描述错误，代码条件和文档条件不一致
		 *  DescripConfusing-文档描述模糊	（暂时不管这类）
		 *  InOtherTags-不在@throws中进行的描述
		 *  NotMetioned-对该异常，文档未提及
		 * */
		if (!this.annData.GotJavadocDoc){
			this.InconsInfo = InconsisCase_NoAnn;
			return this.InconsInfo;
		}
		if (this.infoBox.getInfoType().equals(InfoBox.InfoType.EXCEPTION)){
			ProcessingInfoBox_Exception();
		}else{
			ProcessingInfoBox_DescTags();
		}
		
		return this.InconsInfo;
		
	}
	
	private void ProcessingInfoBox_DescTags(){
		for (processedTagElement eachTag : this.annData.getProcessedTagData()){
			ProcessingDescripTags(eachTag);
			if (this.DocMatch)	break;
		}
		if (this.DocMatch){
			this.InWhichTag = "DescripTags";
			if (!this.PerfectMatch){
				this.InconsInfo = InconsisCase_DescErr;
			}else{
				this.InconsInfo = InconsisCase_OtherTag;
			}
		}else{
			this.InconsInfo = InconsisCase_NotMent;
		}
	}
	private void ProcessingInfoBox_Exception(){
		for (processedTagElement eachTag : this.annData.getProcessedTagData()){
			if (!(eachTag.getTagName().contains("throw")|| eachTag.getTagName().contains("exception"))){
				continue;
			}
			if (!eachTag.getKeyWord().equals(this.infoBox.getException())){
				continue;
			}
			
			ProcessingExceptionTag(eachTag);
			if (this.DocMatch)	break;
		}
		if (this.DocMatch == true){
			this.InWhichTag = "ExceptionTags";
			if (!this.PerfectMatch){
				this.InconsInfo = InconsisCase_DescErr;
			}
			return;
		}
		//	接下来搜索其他的tags
		for (processedTagElement eachTag : this.annData.getProcessedTagData()){
			if (eachTag.getTagName().contains("throw")|| eachTag.getTagName().contains("exception")){
				continue;
			}
			
			ProcessingDescripTags(eachTag);
			if (this.DocMatch)	break;
		}
		if (this.DocMatch){
			this.InWhichTag = "DescripTags";
			if (!this.PerfectMatch){
				this.InconsInfo = InconsisCase_DescErr;
			}else{
				this.InconsInfo = InconsisCase_OtherTag;
			}
		}else{
			this.InconsInfo = InconsisCase_NotMent;
		}
		
	}
	private void ProcessingDescripTags(processedTagElement tag){
		List<String> SplitedStrs = SplitStrings(tag.getTagDesc());
		//System.out.println(tag.getTagDesc());
		for (String descStr : SplitedStrs){
			System.out.println(descStr);
			MultiConstraintsSeperator mc = new MultiConstraintsSeperator(descStr, this.ParamString);
			MultiConstraints c = mc.sepString;
			if (c.constraint == null && c.relation == null){
				//	此时应该是句子没有分析出来，目前直接跳过吧
				continue;
			}
			String ConsExp_doc = getConstraintExpression(c);
			String ConsExp_src = this.infoBox.formsFuzzyCompareExpression();
			
			if (ConsExp_doc == null) {
				continue;
			}
			boolean flag = FuzzyStringCompare.FuzzyCompare(ConsExp_doc, ConsExp_src);
			if(flag == true){	
				this.PerfectMatch = true;
				this.DocMatch = true;
				return;
			}else{
				this.PerfectMatch = false;
				this.DocMatch = true;
				return ;
			}
		}
	}
	
	private boolean KeyWordsMatching(String tagDesc){
		boolean flag = true;	//	对当前infobox的所有参数都有提到
		for (ParamType p : this.infoBox.simParams){
			if (tagDesc.indexOf(p.getName()) < 0){
				flag = false;
				break;
			}
		}
		return flag;
	}
	private void ProcessingExceptionTag(processedTagElement tag){
		List<String> SplitedStrs = SplitStrings(tag.getTagDesc());
		for (String descStr : SplitedStrs){
			if (descStr.contains("miterlimit"));
				//System.out.println("bingo");
			MultiConstraintsSeperator mc = new MultiConstraintsSeperator(descStr, this.ParamString);
			MultiConstraints c = mc.sepString;
			String ConsExp_doc = getConstraintExpression(c);
			String ConsExp_src = this.infoBox.formsFuzzyCompareExpression();
			//	此时 ConsExp_src 里面可能存在 !(!a<b)之类的，要在FuzzyCompare中，去掉括号之后，去掉重复的！！
			if (ConsExp_doc == null) {
				if (!this.DocMatch){
					//	如果都没找到，那么试试关键字匹配
					if (KeyWordsMatching(tag.getTagDesc())){
						this.ConsType = ConstraintType.KEY_MATCH;
						this.PerfectMatch = false;
						this.DocMatch = true;
					}
				}
				continue;
			}
			boolean flag = FuzzyStringCompare.FuzzyCompare(ConsExp_doc, ConsExp_src);
			if(flag == true){	
				this.PerfectMatch = true;
				this.DocMatch = true;
				return;
			}else{
				this.PerfectMatch = false;
				this.DocMatch = true;
				return ;
			}
		}
		
	}
	
	private String SpecialWordReplacing(String src){
		Pattern pat;
		Matcher mat;
		for (String s : SpecialFiledReplacedWordList){
			pat = Pattern.compile("\\W" + s + "\\W");
			mat = pat.matcher(src);
			if (!mat.find()){
				continue;
			}
			String tr = mat.group();
			src = src.replace(tr, " " + SpecialFieldReplacedWord.get(s) + " ");
		}
		
		return src;
	}
	private String getConstraintExpression(MultiConstraints cons){
		String resExp = "";
		if (cons.relation != null){
			String rel = "";
			if (cons.relation.compareToIgnoreCase("and") == 0){
				rel = "&&";
			}else if (cons.relation.compareToIgnoreCase("or") == 0){
				rel = "||";
			}
			
			for (MultiConstraints c : cons.subConstraints){
				String cex = getConstraintExpression(c);
				if (cex == null) continue;
				resExp += "(" + cex + ")" + rel;
			}
			if (resExp.length() > 2){
				resExp = resExp.substring(0, resExp.length() -rel.length());
			}
		}else{
			resExp = getSingleConstraintExpression(cons);
		}
		
		if (resExp == null || resExp.length() == 0)	return null;
		resExp = SpecialWordReplacing(resExp);
		return resExp;
	}
	
	private boolean ShareParams(List<ParamInfo> pars){
		boolean flag = false;
		for (ParamType part : this.infoBox.simParams){
			for (ParamInfo pari : pars){
				if (part.getName().equals(pari.paramName)){
					flag = true;
					return flag;
				}
			}
		}
		return flag;
	}
	private String getSingleConstraintExpression(MultiConstraints con){
		ConsInThrow conExp = null;
		if (con.constraint == null && con.relation == null)	return null;
		for (int i = 0; i< 1; i++){
			conExp = NullConstraint.createNewInstance(con.constraint, this.ParamString, con.SpecRep);
			this.ConsType = ConstraintType.NULL_CONSTRAINT;
			if (conExp != null)	break;
			conExp = ValueCons.createNewInstance(con.constraint, this.ParamString, con.SpecRep);
			this.ConsType = ConstraintType.VALUE_CONSTRAINT;
			if (conExp != null) break;
			conExp = SetValueCons.createNewInstance(con.constraint, this.ParamString, con.SpecRep);
			this.ConsType = ConstraintType.SET_CONSTRAINT;
			if (conExp != null) break;
			conExp = TypeCons.createNewInstance(con.constraint, this.ParamString, con.SpecRep);
			this.ConsType = ConstraintType.TYPE_CONSTRAINT;
			if (conExp != null) break;
			this.ConsType = ConstraintType.NO_MATCH;
		}
		//return "TRUE";
		if (conExp == null) {	return null;	}
		if (!ShareParams(conExp.getParamList())){
			return null;
		}
		return conExp.ReportAsExpression();
	}
	
	
	//=================工具方法======================================
	private static List<String> SplitStrings(String str){
		List<String> resArray = new ArrayList<String>();
		Pattern pat = Pattern.compile("\\w\\s*\\.\\s*[A-Z]");
		Matcher mat = pat.matcher(str);
		
		while(mat.find()){
			String tr = mat.group();
			tr = tr.substring(1, tr.length()-1);
			int index = str.indexOf(tr);
			resArray.add(str.substring(0, index));
			
			str = str.substring(index + tr.length());
			mat = pat.matcher(str);
		}
		
		resArray.add(str);
		
		return resArray;
		
	}
	
	public static void main(String[] args){
		String path = "F:\\JavaProjects\\DocumentOmissionDetect\\testFiles\\testInconsCheck\\java\\awt\\Component.java\\addComponentListener(ComponentListener)\\";
				//"F:\\JavaProjects\\DocumentOmissionDetect\\testFiles\\testInconsCheck\\java\\awt\\event\\InputEvent.java\\getMaskForButton(int)\\"; 
				// "F:\\DocErrorDetect\\CellDoc\\testProj\\res\\jdk_part\\java\\awt\\Container.java\\setComponentZOrder(Component,int)\\";
				//"F:\\JavaProjects\\DocumentOmissionDetect\\testFiles\\testInconsCheck\\testFunc(int-a,int-b)\\";
		
		DocFileReader doc = new DocFileReader(path + "doc.res");
		InfoBoxesReader box = new InfoBoxesReader(path + "src.res");
		MethodInfoReader met = new MethodInfoReader("java.awt.Component.java", "addComponentListener(ComponentListener)");
		AnnotationData ann = doc.getAnnData();
		List<InfoBox> boxes = box.getInfoBoxes();
		int i = 0;
		for (InfoBox b : boxes){
			//if (i < 2){i++; continue;}
			CheckInconsistency_NLPCell check 
				= 	CheckInconsistency_NLPCell.createNewInstance(b, ann, met.ParamsToString());
			System.out.println(check.FirstAnnotationTagMetionedFailure() + "\t" + check.InWhichTag);
		}
	}
}
