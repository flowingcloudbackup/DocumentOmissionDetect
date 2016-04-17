package udf.ApiOmiDetec.Layers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import udf.ApiOmiDetec.Globals.GlobalFiles;
import udf.ApiOmiDetec.Globals.GlobalTools;
import udf.ApiOmiDetec.Globals.GlobalValues;

/*
 * Divide files in the project fold into single files
 * */
public class ProjectLevel {
	
	private String rootpath;
	private String projName;
	private String srcPath;
	private String resPath;
	
	public ProjectLevel(String root, String name){
		this.rootpath = root;
		this.srcPath = root + "src/";
		this.resPath = root + "res/";
		this.projName = name + "/";
		
		File toCleanUp = new File(this.resPath + this.projName + "/");
		GlobalTools.FoldsCleanUp(toCleanUp);
		SeparateFiles(this.srcPath + this.projName, this.resPath + this.projName);
	}
	
	public void SeparateFiles(String path, String save){
		File root = new File(path);
		File[] files = root.listFiles();
		
		for (File f : files){
			if (f.isDirectory()){
				SeparateFiles(path + f.getName() + "/", save + f.getName() + "/");
			}
			String filename = f.getName();
			if (filename.endsWith(".java")){
				//try{
					System.out.println("Analysing ..." + f.getAbsolutePath());
					
					
					/* here should invoke class level to read .java file.
					 * pass File f as parameter to class level
					 * pass Save Path as parameter to class level
					 * 
					 * and delete empty files*/
					
					ClassLevel cla = new ClassLevel(f, save);
					GlobalValues.Summary_TotalNumber_Classes++;
					//new ClassLevel(path, filename, save);
					//--delete empty file folds--
					/*
					File classfilepath  =new File(save + filename);
					if (classfilepath.exists() && classfilepath.listFiles().length <= 0){
						classfilepath.delete();
					}
					*/
					
				//}catch(Exception e){;}
			}
		}		
	}
	
	public static void main(String[] args) throws IOException{
		String root = "F:/DocErrorDetect/CellDoc/testProj/";
		String projName = "jdk_part";
		/*
		GlobalFiles.ConsoleLog_4Test = new File(root + "/logs.res");
		if (!GlobalFiles.ConsoleLog_4Test.exists()){
			GlobalFiles.ConsoleLog_4Test.createNewFile();
		}
		GlobalFiles.ConsoleLogWriter_4Test = new FileWriter(GlobalFiles.ConsoleLog_4Test);
		*/
		GlobalFiles.InitGlobalLogFile(root, "log.res");
		new ProjectLevel(root, projName);
		GlobalTools.EmptyFolderCleaner(new File(root + "res/" + projName + "/"));
		GlobalFiles.CloseGlobalLogFile();
		//GlobalFiles.ConsoleLogWriter_4Test.close();
	}
	

}
