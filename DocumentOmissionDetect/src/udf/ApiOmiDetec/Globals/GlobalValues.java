package udf.ApiOmiDetec.Globals;

import udf.ApiOmiDetec.Infomations.InfoBox;
import udf.ApiOmiDetec.Infomations.InfoBox.InfoType;

public class GlobalValues {
	
	public static int Summary_TotalNum_Exception;
	public static int Summary_TotalNum_SoloIf;
	public static int Summary_TotalNum_FirstReturn;
	
	public static int Summary_TotalNumber_Classes;
	public static int Summary_TotalNumber_Methods;
	
	public static void InitTotalNumbers(){
		Summary_TotalNumber_Classes = 0;
		Summary_TotalNumber_Methods = 0;
		
		Summary_TotalNum_Exception = 0;
		Summary_TotalNum_FirstReturn = 0;
		Summary_TotalNum_SoloIf = 0;
	}
	
	public static void PublishTotalNumbers(){
		String res = "";
		res += "The number of classes is:\t" + Summary_TotalNumber_Classes + "\n";
		res += "The number of methods is:\t" + Summary_TotalNumber_Methods + "\n";
		String start = "Total Numbers of ";
		res += start + "Exception:\t" + Summary_TotalNum_Exception + "\n";
		res += start + "FirstReturn:\t" + Summary_TotalNum_FirstReturn + "\n";
		res += start + "SoloIf:\t" + Summary_TotalNum_SoloIf + "\n";
		
		System.out.println(res);
	}
	
	public static void CountSummaryNumber(InfoBox box){
		InfoType type = box.getInfoType();
		if (type == null) return;
		if (type.equals(InfoType.EXCEPTION)){
			Summary_TotalNum_Exception ++;
		}else if (type.equals(InfoType.SOLOIF)){
			Summary_TotalNum_SoloIf ++;
		}else if (type.equals(InfoType.FIRSTRETURN)){
			Summary_TotalNum_FirstReturn ++;
		}
	}
	
	public static int InfoBoxes_TotalNumber;
	public static int NoDocument_TotalNumber;
	public static int NotMentioned_TotalNumber;
	public static int Correct_TotalNumber;
	public static int Error_TotalNumber;
	public static void init_InfoBoxes_SummaryNumers(){
		InfoBoxes_TotalNumber = 0;
		NoDocument_TotalNumber = 0;
		NotMentioned_TotalNumber = 0;
		Correct_TotalNumber = 0;
		Error_TotalNumber = 0;
	}
	public static void publish_InfoBoxesInfos(){
		String res = "InfoBox Information Summary:\n";
		res += "Total InfoBoxes Included:\t" + InfoBoxes_TotalNumber + "\n";
		res += "Correct InfoBoxes included:\t" + Correct_TotalNumber + "\n";
		res += "Error InfoBoxes included:\t" + Error_TotalNumber + "\n";
		res += "Not mentioned InfoBoxes:\t" + NotMentioned_TotalNumber + "\n";
		res += "No Annotation Document:\t" + NoDocument_TotalNumber + "\n";
		System.out.println(res);
	}
	public static void NoDoc_Count(){
		NoDocument_TotalNumber ++;
		InfoBoxes_TotalNumber ++;
	}
	public static void NotMen_Count(){
		NotMentioned_TotalNumber ++;
		InfoBoxes_TotalNumber ++;
	}
	public static void Cor_Count(){
		Correct_TotalNumber ++;
		InfoBoxes_TotalNumber ++;
	}
	public static void Err_Count(){
		Error_TotalNumber ++;
		InfoBoxes_TotalNumber ++;
	}

}
