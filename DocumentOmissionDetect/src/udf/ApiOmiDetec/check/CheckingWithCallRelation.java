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
	
	/*	���ܽ�һ��˼·��
	 * 		���ȣ������õ�һ��������Ϊ���������Ҫ�ο������ĵ���Ϣ����Ϸ��������modifiers��public private�ȣ�
	 * 		��һ���������������ڵ�infoboxes�����Ƿ��в�һ��
	 * 		�ڶ������鿴���������������Щ�������γɼ���callees_i{met1,met2,...,metN}(ע��i�����i��ݹ����)
	 * 			1)	��ȡÿ��met�е�infoboxes����Ŀ�귽�����ĵ�����һ���ԶԱ�
	 * 			2)	����ÿ��met���ٴβ鿴���ǵĵ��÷�������callees_i�������ȡÿ��met���鿴infoboxes
	 * 			3)	���ò����ﵽ��ֵ�������ؽ��
	 * 	ע������ڵڶ����У����Ҿ��е��ù�ϵ�ķ�����Ҫ���в�������ת��������metA������metB,����֮�䱻ֱ�Ӵ��ݵĲ�����A�н�a����B�н�b��
	 * ����B��infobox�ݹ�������ʱ����Ҫ��param���ָĳ�a���ٽ��м�顣
	 * */
	
	private String methodInfoPath;		//	�����֮ǰ���ɵĿ���ȡ��Ϣ�����Լ�¼�ļ���λ�ü���
	private String PackClassPath;		//	��¼��·��
	private String methodSignature;		//	��¼��ǰ����Ŀ��ķ���ǩ��
	
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
		
		//----��һ�����������src���Ƚ�
		/*
		List<InfoBox> ownBoxes = getInfoBoxes(this.PackClassPath, this.methodSignature);
		for (InfoBox box : ownBoxes){
			CheckInconsistency_CellCompare check = new CheckInconsistency_CellCompare(box, this.objAnn);
			System.out.println(check.FirstAnnotationTagMetionedFailure());
		}
		���ɵ��ڶ����ĺ�������ȥ
		*/
		
		//----�ڶ�������ȡ���ù�ϵ���������ǵ�src���Ƚ�----
		//----��Ҫȷ�ϵĶ�����A��B֮���д��ε��ù�ϵ(������Ҫά��һ�����ݲ������ϣ���Ϊ��ע�Ĳ����ڴ��ݹ����п��ܾ�û����)
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
			List<String[]> paramPassed){	/*	ά��һ�����ݲ������ϡ�
			��������ΪString[3]��ÿһ��String[3]���ز������͡���ʼ��������Ŀǰ���������ֱ��ӦString[0], String[1], String[2]*/
		if(paramPassed.size() <= 0	//	���������������0���򷵻�
				|| (!MethodIsInBase(pack, metSig))	//	����÷������ڿ��У�����
				||	depth <= 0	//	����ﵽ��ֵ������ȣ��ֻ�
				)	{	return;	}
		CheckDocSrcInconsistency(pack, metSig, paramPassed);		//	�����pack-metsig���Ƿ���ڲ�һ����
		
						//	���ڵ�ǰ�ķ��������ȣ����Һ����е��ù�ϵ�ķ������ϣ�Ŀǰֻ����callees��
		String methodInvokationPath = GlobalSettings.CallGraphBaseRoot;
		if (pack.endsWith(".java")){
			methodInvokationPath += pack.substring(0, pack.length()-5).replaceAll("\\.", "\\\\") + ".java";
		}else{
			methodInvokationPath += pack.replaceAll("\\.", "\\\\") + ".java";
		}
		methodInvokationPath += "\\" + metSig + "\\";
		
		File calleePath = new File(methodInvokationPath + "callees\\");
		File[] calleeFiles = calleePath.listFiles();
		if (calleeFiles == null){	return;		}	//	�п����ڿ��в����ڣ���Ϊcall graph���޳��˺ܶ಻��Ҫ�о��ķ���
		for (File calleeFile : calleeFiles){
			CalleMethodInfo calleInfo = new CalleMethodInfo(calleeFile);
			//	��ȡcallee����Ϣ
			//System.out.println(calleInfo.present());
					//	�˴���Ҫ���´��ݲ������ϣ����ҿ��Ǹ���infobox�еĲ�����������
			GetInconsistencyThroughCallGraph(depth-1, calleInfo.packPath, calleInfo.metSig,
					GetNewPassedParamSet(paramPassed, calleInfo));
			//systeem	//	����ǵõ�����δ���
		}
		
	}
	
	private void CheckDocSrcInconsistency(String pack, String metSig, 
											List<String[]> passedParam){	//	����infobox�е�param
		System.out.println("in method:\t" + pack + "-" + metSig);
		InconsistencyResultInfomation resultInfo;
		if (this.objAnn == null){
			resultInfo = new InconsistencyResultInfomation(pack + "-" + metSig, true);	
			GlobalValues.NoDoc_Count();
													//	��ǣ��������û��doc��Ϣ
		}else{
			resultInfo = new InconsistencyResultInfomation(pack + "-" + metSig, false);
			
			List<InfoBox> checkBoxes = getInfoBoxes(pack, metSig);
			if (checkBoxes == null) {	return;		}
			checkBoxes = InfoBox.RefreshParamInfosInInfoBoxList(checkBoxes, passedParam);
			for (InfoBox box : checkBoxes){
				
				//==================���в�һ�����жϵĵط�=====================
				//CheckInconsistency_CellCompare check = new CheckInconsistency_CellCompare(box, this.objAnn);
				CheckInconsistency_NLPCell check 
				= CheckInconsistency_NLPCell.createNewInstance(box, this.objAnn, this.metInfo.ParamsToString());
				int index = check.FirstAnnotationTagMetionedFailure();
				String constype = check.ConsType.toString();
				if (!box.HasParameters()){	//	���infoBox�еĲ���Ϊ���ˣ����������Ϊ֮ǰ����Ҫ���ݴ��μ���ˢ�º����еĲ�����Ϣ��
					continue;
				}
				
				if (index == -1){
					GlobalValues.NotMen_Count();
				}else if (index == -2){
					GlobalValues.NoDoc_Count();
				}else if(index == -3){
					GlobalValues.Err_Count();
				}else{	
					//	��ʱ index >= 0�����ӱ���ȷ������
					GlobalValues.Cor_Count();
					if (!GlobalSettings.PrintOutCorrectInfoBoxes){
						//	������ò������Щ��ȷ�ĺ��ӣ��������һѭ��
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
			//	��ȡĳ��������infobox
			//	�������Ϊ����pack.class.metName
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
				//	���û�������Ϣ����д���ļ�
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
