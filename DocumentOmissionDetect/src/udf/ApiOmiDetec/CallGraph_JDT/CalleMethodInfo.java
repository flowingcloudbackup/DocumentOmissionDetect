package udf.ApiOmiDetec.CallGraph_JDT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.javatuples.Triplet;

public class CalleMethodInfo {
	
	public String metSig;
	public String packPath;
	private String[] paramTypes;
	public Triplet<String, String, String>[] PassParamNames;
	
	public CalleMethodInfo(File file){
		ExtractMethodInfo(file);
	}
	
	private void ExtractMethodInfo(File file){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			this.metSig = line;	//	读取第一行：方法签名
			line = reader.readLine();
			this.packPath = line;	//	读取第二行：	包路径
			
			line = reader.readLine();
			int num = Integer.valueOf(line);
			this.paramTypes = new String[num];
			for (int i = 0; i< num ;i++){
				line = reader.readLine();
				this.paramTypes[i] = line;
			}
			
			line = reader.readLine();
			num = Integer.valueOf(line);
			this.PassParamNames = new Triplet[num];
			for (int i = 0; i< num ;i++){
				line = reader.readLine();
				String[] spls = line.split("-");
				this.PassParamNames[i] = new Triplet<String, String, String>(spls[0], spls[1], spls[2]);
			}
			
			reader.close();
		
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	public String present(){
		String res = this.metSig + "\n";
		res += this.packPath + "\n";
		for (String type : this.paramTypes){
			res += type + "\n";
		}
		for (Triplet t : this.PassParamNames){
			res += t.toString() + "\n";
		}
		
		return res;
	}
	
	public static void main(String[] args){
		String filepath = "F:\\DocErrorDetect\\CallGraphs\\JDT_way\\ATryTest\\jdk_part\\java\\awt\\Container.java\\addImpl(Component,Object,int)\\callees\\";
		String filename = "checkNotAWindow(Component).res";
		File file = new File(filepath + filename);
		
		CalleMethodInfo info = new CalleMethodInfo(file);
		System.out.println(info.present());

	}

}
