package udf.ApiOmiDetec.CallGraph.fromSoot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JimpleReader {
	
	private String fileRoot;
	private String fileName;
	
	private String fileContent;
	
	private List<JimpleMethod> fileMethods;
	
	public JimpleReader(String root, String name){
		this.fileRoot = root;
		this.fileName = name;
		
		this.fileContent = "";
		this.fileMethods = new ArrayList();
		try {
			readFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/* attention: the file content may be empty string!
		 * */
		ProcessFileContent();
	}
	
	private void readFile() throws IOException{
		File file = new File(this.fileRoot + this.fileName);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String str = reader.readLine();
		while(str != null){
			if (str.length() > 0){
				this.fileContent += str + "\n";
			}
			str = reader.readLine();
		}
		reader.close();
	}
	
	private void ProcessFileContent(){
		int l_f = this.fileContent.indexOf("{");
		int r_f = this.fileContent.lastIndexOf("}");
		String realContent = this.fileContent.substring(l_f + 1, r_f);
		
		l_f = realContent.indexOf("{");
		while(l_f >= 0){
			r_f = realContent.indexOf("}");
			
			String forContent = realContent.substring(0, l_f);
			int semicolon = forContent.lastIndexOf(";");
			if(semicolon >= 0){
				forContent = forContent.substring(semicolon +2);
			}
			//System.out.println(forContent);
			
			String bodyContent = realContent.substring(l_f + 1, r_f);
			JimpleMethod me = new JimpleMethod(forContent, bodyContent);
			//System.out.println(me.presentation());
			//System.out.println("=================================");
			this.fileMethods.add(me);
			
			realContent = realContent.substring(r_f + 1);
			l_f = realContent.indexOf("{");
		}
	}
	
	public String getFileContent()
	{	return this.fileContent;	}
	public String presentation(){
		String res = "";
		for (JimpleMethod me : this.fileMethods){
			res += me.presentation() + "\n";
		}
		return res.substring(0, res.length()-1);
	}
	
	public static void main(String[] args){
		String root = "F:/JavaProjects/DocumentOmissionDetect/testFiles/tryJimple/";
		String name = "testJimple.jimple";
		JimpleReader r = new JimpleReader(root, name);
		System.out.println(r.presentation());
		System.out.println("biaoshi");
	}

}
