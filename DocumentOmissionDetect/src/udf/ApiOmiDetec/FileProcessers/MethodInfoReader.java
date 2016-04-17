package udf.ApiOmiDetec.FileProcessers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import udf.ApiOmiDetec.Globals.GlobalSettings;
import udf.ApiOmiDetec.Infomations.ParamType;

public class MethodInfoReader {
	
	private String metSig;
	private int metModifiers;
	private String returnType;
	
	public List<ParamType> params;
	
	public MethodInfoReader(String pack, String sig){
		String foldpath = GlobalSettings.CellInfoSavePathRoot;
		if (pack.endsWith(".java")){
			foldpath += pack.substring(0, pack.length()-5).replaceAll("\\.", "\\\\") + ".java\\" + sig + "\\";
		}else{
			foldpath += pack.replaceAll("\\.", "\\\\") + "\\" + sig + "\\";
		}
		
		ExtractFileInformation(foldpath);
	}
	
	private void ExtractFileInformation(String filepath){
		File file = new File(filepath + "metInfo.res");
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			this.metSig = reader.readLine();
			this.metModifiers = Integer.valueOf(reader.readLine());
			this.returnType = reader.readLine();
			int paramNum = Integer.valueOf(reader.readLine());
			this.params = new ArrayList<ParamType>();
			for (int i = 0;i< paramNum; i++){
				this.params.add(new ParamType(reader.readLine()));
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public String ParamsToString(){
		String res = "(";
		for (ParamType pt : this.params){
			res += pt.getType() + "-" + pt.getName() + ",";
		}
		res = res.substring(0, res.length()-1);
		res += ")";
		return res;
	}
	
	public static void main(String[] args){
		String pack = "awt.Container.java";
		String sig = "addImpl(Component,Object,int)";
		
		MethodInfoReader r = new MethodInfoReader(pack, sig);
		for (ParamType p : r.params){
			System.out.println(p.getType() + "-" + p.getName());
		}
	}

}
