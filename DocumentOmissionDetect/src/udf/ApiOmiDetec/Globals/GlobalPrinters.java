package udf.ApiOmiDetec.Globals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GlobalPrinters {
	
	public static void WriteInfoFile(File file, String content){
		try{
			if (!file.exists())	{	file.createNewFile();	}
			FileWriter writer = new FileWriter(file);
			writer.append(content);
			writer.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

}
