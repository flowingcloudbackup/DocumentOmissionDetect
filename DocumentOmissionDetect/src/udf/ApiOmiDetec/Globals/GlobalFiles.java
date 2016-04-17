package udf.ApiOmiDetec.Globals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GlobalFiles {
	
	public static File ConsoleLog_4Test ;
	
	public static FileWriter ConsoleLogWriter_4Test;
	
	public static boolean FileInited = false;
	
	public static void PrintIntoConsole(String str) throws IOException{
		if (FileInited)
			ConsoleLogWriter_4Test.append(str + "\n");
	}
	
	public static void InitGlobalLogFile(String root, String name) throws IOException{
		GlobalValues.InitTotalNumbers();
		
		ConsoleLog_4Test = new File(root + name);
		if (!ConsoleLog_4Test.exists()){
			ConsoleLog_4Test.createNewFile();
		}
		ConsoleLogWriter_4Test = new FileWriter(GlobalFiles.ConsoleLog_4Test);
		
		FileInited = true;
	}
	
	public static void CloseGlobalLogFile() throws IOException{
		GlobalValues.PublishTotalNumbers();
		
		String resAccounts = "";
		resAccounts += "Classes numbers:\t" + GlobalValues.Summary_TotalNumber_Classes + "\n";
		resAccounts += "Methods numbers:\t" + GlobalValues.Summary_TotalNumber_Methods + "\n";
		resAccounts += "Total Exceptions:\t" + GlobalValues.Summary_TotalNum_Exception + "\n";
		resAccounts += "Total SoloIf:\t" + GlobalValues.Summary_TotalNum_SoloIf + "\n";
		resAccounts += "Total FirstReturn:\t" + GlobalValues.Summary_TotalNum_FirstReturn + "\n";
		PrintIntoConsole(resAccounts);
		
		ConsoleLogWriter_4Test.close();
		FileInited = false;
	}

}
