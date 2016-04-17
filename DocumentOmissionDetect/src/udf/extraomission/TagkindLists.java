package udf.extraomission;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.TagElement;

public class TagkindLists {
	
	public String tagName;
	public List<TagElement> tagList;
	public List<String> tagDescripts;
	
	public TagkindLists(String tname){
		this.tagName = tname;
		this.tagList = new ArrayList<TagElement>();
		this.tagDescripts = new ArrayList<String>();
	}
	
	public String WriteFileContent(){
		String res = tagName + "\n";
		res += tagList.size() + "\n";
		
		int count = 0;
		for (String desc : this.tagDescripts){
			res += desc + "\n";
			System.out.println("writing tag number " + count + ", tag type: " + this.tagName);
			count ++;
		}
		return res;
	}
	public void SaveInFile(String path){
		String filePathName = path + tagName + ".cmp";
		System.out.println("Saving in file:\t" + filePathName);
		
		File file = new File(filePathName);
		try{
			if (!file.exists()){
				file.createNewFile();
			}
			FileWriter writer = new FileWriter(file);
			writer.append(WriteFileContent());
			writer.close();
		}catch(IOException e){e.printStackTrace();}
	}

}
