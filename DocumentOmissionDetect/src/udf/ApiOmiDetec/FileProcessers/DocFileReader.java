package udf.ApiOmiDetec.FileProcessers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import udf.ApiOmiDetec.Javadoc.AnnotationData;
import udf.ApiOmiDetec.Javadoc.processedTagElement;

public class DocFileReader {
	
	private AnnotationData AnnInfo;
	
	public DocFileReader(File file){
		ExtractInfomation(file);
	}
	public DocFileReader(String path){
		File file = new File(path);
		ExtractInfomation(file);
	}
	private void ExtractInfomation(File file){
		if (!file.exists()){
			this.AnnInfo = null;
			return;
		}
		this.AnnInfo = new AnnotationData();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			int num = Integer.valueOf(reader.readLine());	//	第一行是tag的数量
			
			for (int i = 0;i< num ;i++){
				String name = reader.readLine();
				String key = reader.readLine();
				String desc = reader.readLine();
				this.AnnInfo.addProcessedTag(new processedTagElement(name, key, desc));
			}
			
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (this.AnnInfo.getProcessedTagData().size() > 0){
			this.AnnInfo.GotJavadocDoc = true;
		}
	}
	
	public AnnotationData getAnnData()
	{	return this.AnnInfo;	}
	
	public static void main(String[] args){
		String filename = "F:\\DocErrorDetect\\CellDoc\\testProj\\res\\jdk_part\\awt\\BasicStroke.java\\BasicStroke(float,int,int,float,float,float)\\doc.res";
		File file = new File(filename);
		
		AnnotationData data = new DocFileReader(file).getAnnData();
		System.out.println(data.presentation(2));
	}

}
