package udf.ApiOmiDetec.check;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import udf.ApiOmiDetec.Globals.GlobalSettings;
import udf.ApiOmiDetec.Globals.GlobalTools;
import udf.ApiOmiDetec.Globals.GlobalValues;

public class InconsistencyAnalysisViaCallGraph {
	
	private String AnalysisRoot;
	public List<String> ErrorFileList = new ArrayList<String>();
	
	public InconsistencyAnalysisViaCallGraph(String root){
		this.AnalysisRoot = root;
	}
	
	public void StartAnalysis(){
		File root = new File(this.AnalysisRoot);
		ScanningAnalysis(root);
	}
	
	private void ScanningAnalysis(File file){
		if (file.isFile()){
			return;
		}
		if (file.getAbsolutePath().contains("\\java\\awt\\AlphaComposite.java\\derive(float)")){
			System.out.println("bingo");
		}
		
		String filePath = file.getAbsolutePath();
		if (filePath.endsWith(")")){
			System.out.println("Analysing..." + filePath);
			
			try{
				CheckingWithCallRelation check = new CheckingWithCallRelation(filePath + "\\");
				check.WriteInfosIntoFile();
			}catch(Exception e){
				this.ErrorFileList.add(filePath);
			}
			return;
		}
		File[] files = file.listFiles();
		for (File f : files){
			ScanningAnalysis(f);
		}
	}
	
	public static void main(String[] args){
		GlobalValues.init_InfoBoxes_SummaryNumers();
		GlobalValues.InitTotalNumbers();
		
		File InconsistencyFileRoot = new File(GlobalSettings.InconsistencyInfoPathRoot);
		GlobalTools.FoldsCleanUp(InconsistencyFileRoot);
		InconsistencyAnalysisViaCallGraph c = new InconsistencyAnalysisViaCallGraph(GlobalSettings.CellInfoSavePathRoot);
		c.StartAnalysis();
		GlobalTools.EmptyFolderCleaner(InconsistencyFileRoot);
		
		GlobalValues.publish_InfoBoxesInfos();
		GlobalValues.PublishTotalNumbers();
		
		try{
			FileWriter writer = new FileWriter(new File("F:\\JavaProjects\\DocumentOmissionDetect\\testFiles\\testInconsCheck\\ErrLog.res"));
			for (String s : c.ErrorFileList){
				writer.append(s + "\n");
			}
			writer.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

}
