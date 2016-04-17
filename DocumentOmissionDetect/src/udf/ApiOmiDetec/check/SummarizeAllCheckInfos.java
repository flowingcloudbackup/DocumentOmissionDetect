package udf.ApiOmiDetec.check;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import udf.ApiOmiDetec.Globals.GlobalSettings;

public class SummarizeAllCheckInfos {
	
	public static List<String> checkContent = new ArrayList<String>();
	public static String InconsisInfoRootPath = GlobalSettings.InconsistencyInfoPathRoot;
	
	private static void getInconsistencyInfos(File file){
		try{
			BufferedReader readr = new BufferedReader(new FileReader(file));
			String line = readr.readLine();
			while(line != null && line.length() > 0){
				checkContent.add(line);
				line = readr.readLine();
			}
			readr.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private static void ScanningFiles(File file){
		System.out.println("Extracting..." + file.getAbsolutePath());
		if (file.getName().endsWith("check.res")){
			getInconsistencyInfos(file);
			return;
		}
		if (file.isDirectory()){
			File[] files = file.listFiles();
			for (File f : files){
				ScanningFiles(f);
			}
			return ;
		}
	}
	
	private static void WriteToLog(){
		try{
			FileWriter w = new FileWriter(InconsisInfoRootPath + "checkLog.csv");
			for(String line : checkContent){
				w.append(line + "\n");
			}
			w.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		String rootPath = InconsisInfoRootPath;
				//"F:\\DocErrorDetect\\CellDocViaInvokations\\jdk_part_NLP_analyz\\";
		ScanningFiles(new File(rootPath));
		WriteToLog();
	}

}
