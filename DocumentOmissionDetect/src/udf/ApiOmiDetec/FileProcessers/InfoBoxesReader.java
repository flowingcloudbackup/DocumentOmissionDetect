package udf.ApiOmiDetec.FileProcessers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import udf.ApiOmiDetec.Globals.GlobalSettings;
import udf.ApiOmiDetec.Infomations.InfoBox;

public class InfoBoxesReader {
	
	private List<InfoBox> InfoBoxes;
	
	public InfoBoxesReader(String pack, String metSig){
		String path = GlobalSettings.CellInfoSavePathRoot;
		if (pack.endsWith(".java")){
			pack = pack.substring(0, pack.length()-5);
			path += pack.replaceAll("\\.", "\\\\") + ".java" + "\\" + metSig + "\\src.res";
		}else{
			path += pack.replaceAll("\\.", "\\\\") + ".java" + "\\" + metSig + "\\src.res";
		}
		File file = new File(path);
		ExtractInformation(file);
	}
	public InfoBoxesReader(String filename){
		File file = new File(filename);
		ExtractInformation(file);
	}
	public InfoBoxesReader(File file){
		ExtractInformation(file);
	}
	private void ExtractInformation(File file){
		if (!file.exists())	return;	//	如果file不存在，就不读取数据，返回的InfoBox List自动为null
		this.InfoBoxes = new ArrayList<InfoBox>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			int num = Integer.valueOf(reader.readLine());	//	第一行，infobox的数量
			for (int i = 0;i< num;i++){
				String type = reader.readLine();
				String excep = reader.readLine();
				String params = reader.readLine();
				int ExpressNum = Integer.valueOf(reader.readLine());
				List<String> ExpSrcList = new ArrayList();
				for (int j = 0; j < ExpressNum ; j++){
					String express = reader.readLine();
					ExpSrcList.add(express);
				}
				int SingleExpressNum = Integer.valueOf(reader.readLine());
				List<String> newList = new ArrayList<String>();
				for (int j = 0;j < SingleExpressNum ;j++){
					String SingleExpress = reader.readLine();
					newList.add(SingleExpress);
				}
				
				this.InfoBoxes.add(new InfoBox(type, excep, params, newList, ExpSrcList));
			}
			
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<InfoBox> getInfoBoxes()
	{	return this.InfoBoxes;	}
	
	public static void main(String[] args){
		String filename = "F:\\DocErrorDetect\\CellDoc\\testProj\\res\\jdk_part\\awt\\Container.java\\remove(Component)\\src.res";
		File file = new File(filename);
		
		List<InfoBox> l = new InfoBoxesReader(file).getInfoBoxes();
		for (InfoBox box : l){
			System.out.println(box.presentation());
		}
	}

}
