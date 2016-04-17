package udf.ApiOmiDetec.check;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.javatuples.Triplet;

import udf.ApiOmiDetec.CallGraph_JDT.CalleMethodInfo;
import udf.ApiOmiDetec.FileProcessers.DocFileReader;
import udf.ApiOmiDetec.FileProcessers.InfoBoxesReader;
import udf.ApiOmiDetec.FileProcessers.MethodInfoReader;
import udf.ApiOmiDetec.Globals.GlobalPrinters;
import udf.ApiOmiDetec.Globals.GlobalSettings;
import udf.ApiOmiDetec.Globals.GlobalValues;
import udf.ApiOmiDetec.Infomations.InconsistencyResultInfomation;
import udf.ApiOmiDetec.Infomations.InfoBox;
import udf.ApiOmiDetec.Infomations.ParamType;
import udf.ApiOmiDetec.Infomations.InfoBox.InfoType;
import udf.ApiOmiDetec.Javadoc.AnnotationData;

public class CheckingWithCallRelation {
	
	/*	先总结一下思路：
	 * 		首先，我们拿到一个方法作为考察对象，主要参考它的文档信息，结合方法本身的modifiers（public private等）
	 * 		第一步，分析本方法内的infoboxes，看是否有不一致
	 * 		第二步，查看这个方法调用了哪些方法，形成集合callees_i{met1,met2,...,metN}(注：i代表第i层递归调用)
	 * 			1)	提取每个met中的infoboxes，与目标方法的文档进行一致性对比
	 * 			2)	对于每个met，再次查看他们的调用方发集合callees_i，逐层提取每个met，查看infoboxes
	 * 			3)	调用层数达到阈值，即返回结果
	 * 	注意事项：在第二步中，查找具有调用关系的方法需要进行参数名称转换。比如metA调用了metB,方法之间被直接传递的参数在A中叫a，在B中叫b，
	 * 则在B的infobox递归上来的时候，需要把param名字改成a，再进行检查。
	 * */
	
	private String methodInfoPath;		//	这里从之前生成的库提取信息，所以记录文件夹位置即可
	private String PackClassPath;		//	记录包路径
	private String methodSignature;		//	记录当前考察目标的方法签名
	
	MethodInfoReader metInfo;
	
	private AnnotationData objAnn;
	
	private List<InconsistencyResultInfomation> InconsistencyResult;
	
	public CheckingWithCallRelation(String path){
		this.methodInfoPath = path;
		this.PackClassPath = path.substring(0, path.length()-1);
		this.PackClassPath = 
				this.PackClassPath.replaceAll(
						GlobalSettings.CellInfoSavePathRoot.replaceAll("\\\\", "\\\\\\\\")
						, "");
		int index = this.PackClassPath.lastIndexOf("\\");
		this.methodSignature = this.PackClassPath.substring(index+1);
		this.PackClassPath = this.PackClassPath.substring(0, index).replaceAll("\\\\", "\\.");
		
		PreProcessing();
	}
	public CheckingWithCallRelation(String pack, String metSig){
		if (pack.endsWith(".java")){
			String temp = pack.substring(0, pack.length()-5);
			this.methodInfoPath = GlobalSettings.CellInfoSavePathRoot;
			this.methodInfoPath += temp.replaceAll("\\.", "\\\\") + ".java" + "\\" + metSig + "\\";
		}
		this.PackClassPath = pack;
		this.methodSignature = metSig;
		
		PreProcessing();
	}
	
	private void PreProcessing(){
		this.InconsistencyResult = new ArrayList<InconsistencyResultInfomation>();
		this.objAnn = new DocFileReader(this.methodInfoPath + "doc.res").getAnnData();
		
		//----第一步，和自身的src作比较
		/*
		List<InfoBox> ownBoxes = getInfoBoxes(this.PackClassPath, this.methodSignature);
		for (InfoBox box : ownBoxes){
			CheckInconsistency_CellCompare check = new CheckInconsistency_CellCompare(box, this.objAnn);
			System.out.println(check.FirstAnnotationTagMetionedFailure());
		}
		集成到第二步的函数里面去
		*/
		
		//----第二步，获取调用关系，并和他们的src作比较----
		//----需要确认的东西：A和B之间有传参调用关系(这里需要维护一个传递参数集合，因为关注的参数在传递过程中可能就没有了)
		this.metInfo = new MethodInfoReader(this.PackClassPath, this.methodSignature);
		List<ParamType> startParams = this.metInfo.params;
		List<String[]> PassedParamSet  = new ArrayList<String[]>();
		for (ParamType param : startParams){
			String[] newStrs = new String[3];
			newStrs[0] = param.getType();
			newStrs[1] = param.getName();
			newStrs[2] = param.getName();
			PassedParamSet.add(newStrs);
		}
		GetInconsistencyThroughCallGraph(GlobalSettings.CallGraphExploringDepth,
				this.PackClassPath, this.methodSignature, PassedParamSet);
	}
	
	private void GetInconsistencyThroughCallGraph(int depth, String pack, String metSig, 
			List<String[]> paramPassed){	/*	维护一个传递参数集合。
			数组类型为String[3]，每一个String[3]记载参数类型、起始变量名、目前变量名，分别对应String[0], String[1], String[2]*/
		if(paramPassed.size() <= 0	//	如果传参量不多于0，则返回
				|| (!MethodIsInBase(pack, metSig))	//	如果该方法不在库中，返回
				||	depth <= 0	//	如果达到阈值搜索深度，分会
				)	{	return;	}
		CheckDocSrcInconsistency(pack, metSig, paramPassed);		//	检测在pack-metsig下是否存在不一致性
		
						//	对于当前的方法，首先，查找和他有调用关系的方法集合（目前只考虑callees）
		String methodInvokationPath = GlobalSettings.CallGraphBaseRoot;
		if (pack.endsWith(".java")){
			methodInvokationPath += pack.substring(0, pack.length()-5).replaceAll("\\.", "\\\\") + ".java";
		}else{
			methodInvokationPath += pack.replaceAll("\\.", "\\\\") + ".java";
		}
		methodInvokationPath += "\\" + metSig + "\\";
		
		File calleePath = new File(methodInvokationPath + "callees\\");
		File[] calleeFiles = calleePath.listFiles();
		if (calleeFiles == null){	return;		}	//	有可能在库中不存在，因为call graph库剔除了很多不需要研究的方法
		for (File calleeFile : calleeFiles){
			CalleMethodInfo calleInfo = new CalleMethodInfo(calleeFile);
			//	获取callee的信息
			//System.out.println(calleInfo.present());
					//	此处需要更新传递参数集合，并且考虑更新infobox中的参数名字问题
			GetInconsistencyThroughCallGraph(depth-1, calleInfo.packPath, calleInfo.metSig,
					GetNewPassedParamSet(paramPassed, calleInfo));
			//systeem	//	下午记得调试这段代码
		}
		
	}
	
	private void CheckDocSrcInconsistency(String pack, String metSig, 
											List<String[]> passedParam){	//	更新infobox中的param
		System.out.println("in method:\t" + pack + "-" + metSig);
		InconsistencyResultInfomation resultInfo;
		if (this.objAnn == null){
			resultInfo = new InconsistencyResultInfomation(pack + "-" + metSig, true);	
			GlobalValues.NoDoc_Count();
													//	标记，这个方法没有doc信息
		}else{
			resultInfo = new InconsistencyResultInfomation(pack + "-" + metSig, false);
			
			List<InfoBox> checkBoxes = getInfoBoxes(pack, metSig);
			if (checkBoxes == null) {	return;		}
			checkBoxes = InfoBox.RefreshParamInfosInInfoBoxList(checkBoxes, passedParam);
			for (InfoBox box : checkBoxes){
				
				//==================进行不一致性判断的地方=====================
				//CheckInconsistency_CellCompare check = new CheckInconsistency_CellCompare(box, this.objAnn);
				CheckInconsistency_NLPCell check 
				= CheckInconsistency_NLPCell.createNewInstance(box, this.objAnn, this.metInfo.ParamsToString());
				int index = check.FirstAnnotationTagMetionedFailure();
				String constype = check.ConsType.toString();
				if (!box.HasParameters()){	//	如果infoBox中的参数为空了，不输出（因为之前步骤要根据传参集合刷新盒子中的参数信息）
					continue;
				}
				
				if (index == -1){
					GlobalValues.NotMen_Count();
				}else if (index == -2){
					GlobalValues.NoDoc_Count();
				}else if(index == -3){
					GlobalValues.Err_Count();
				}else{	
					//	此时 index >= 0，盒子被正确表述了
					GlobalValues.Cor_Count();
					if (!GlobalSettings.PrintOutCorrectInfoBoxes){
						//	如果设置不输出这些正确的盒子，则继续下一循环
						continue;
					}
				}
				resultInfo.addNewInfoBoxCheck(box, index, 
						this.methodInfoPath.replace(",", "-"),
						pack + "-" + metSig.replace(",", "-"), constype,
						check.InWhichTag);
				System.out.println(index);
			}
		}
		
		if (resultInfo.boxes.size() <= 0){
			return;
		}
		this.InconsistencyResult.add(resultInfo);
	}
	
	private boolean MethodIsInBase(String pack, String metSig){
		String abPath = GlobalSettings.CellInfoSavePathRoot;
		if (pack.endsWith(".java")){
			abPath += pack.substring(0, pack.length()-5).replaceAll("\\.", "\\\\") + ".java\\" + metSig + "\\";
		}else{
			abPath += pack.replaceAll("\\.", "\\\\") + ".java\\" + metSig + "\\";
		}
		File metFold = new File(abPath);
		return metFold.exists();
	}
	
	private List<String[]> GetNewPassedParamSet(List<String[]> OldSet, CalleMethodInfo callInfo){
		List<String[]> resSet = new ArrayList<String[]>();
		for (String[] passedPar : OldSet){
			String[] newPassedPar = passedPar.clone();
			for (Triplet<String, String, String> tri : callInfo.PassParamNames){
				if (tri.getValue1().equals(newPassedPar[2])){
					newPassedPar[2] = tri.getValue2();
					resSet.add(newPassedPar);
					break;
				}
			}
		}
		return resSet;
	}
	
	private List<InfoBox> getInfoBoxes(String pack, String metSig){
			//	获取某个方法的infobox
			//	传入参数为方法pack.class.metName
		InfoBoxesReader reader = new InfoBoxesReader(pack, metSig);
		List<InfoBox> boxes = reader.getInfoBoxes();
		
		return boxes;
	}
	
	public String getMetPath()
	{	return this.methodInfoPath;	}
	public String present(){
		String res = "Pack:\t" + this.PackClassPath + "\n";
		res += "Sig :\t" + this.methodSignature + "\n";
		res += "Path:\t" + this.methodInfoPath + "\n";
		
		return res;
	}
	public String presentCheckingResult(){
		String res = "";
		for (InconsistencyResultInfomation info : this.InconsistencyResult){
			//res += info.present();
			res += info.ToPlantStrings();
		}
		return res;
	}
	
	public void WriteInfosIntoFile(){
		if (this.InconsistencyResult.size() <= 0){
				//	如果没有相关信息，不写入文件
			return;
		}
		CountInfoBoxTypes();
		String path = GlobalSettings.InconsistencyInfoPathRoot;
		if (this.PackClassPath.endsWith(".java")){
			path += this.PackClassPath.substring(0, this.PackClassPath.length()-5).replaceAll("\\.", "\\\\") + ".java\\";
		}else{
			path += this.PackClassPath.replaceAll("\\.", "\\\\") + "\\";
		}
		path += this.methodSignature + "\\";
		File fold = new File(path);
		if (!fold.exists()){
			fold.mkdirs();
		}
		
		File file = new File(path + "check.res");
		/*
		String CheckFileContent = "This Method:\t" + this.PackClassPath + "-" + this.methodSignature + "\n";
		CheckFileContent += "///////////////////////////////////////\n" + presentCheckingResult();
		*/
		String CheckFileContent = presentCheckingResult();
		GlobalPrinters.WriteInfoFile(file, CheckFileContent);
		File docFile = new File(path + "doc.res");
		String FileContent = (this.objAnn == null)?"null":this.objAnn.presentation(2);
		GlobalPrinters.WriteInfoFile(docFile, FileContent);
	}
	private void CountInfoBoxTypes(){
		for (InconsistencyResultInfomation info : this.InconsistencyResult){
			for (InfoBox box : info.boxes){
				GlobalValues.CountSummaryNumber(box);
			}
		}
	}
	
	public static void main(String[] args){
		//CheckingWithCallRelation check = new CheckingWithCallRelation("java.awt.Container.java", "addImpl(Component,Object,int)");
		//System.out.println(check.getMetPath());
		
		CheckingWithCallRelation check2 = new CheckingWithCallRelation("java.awt.datatransfer.DataFlavor.java","initialize(String,String,ClassLoader)");
				//("F:\\DocErrorDetect\\CellDoc\\testProj\\res\\jdk_part\\javax\\swing\\JTabbedPane.java\\insertTab(String,Icon,Component,String,int)\\");
		//System.out.println(check2.present());
		check2.WriteInfosIntoFile();
	}

}
