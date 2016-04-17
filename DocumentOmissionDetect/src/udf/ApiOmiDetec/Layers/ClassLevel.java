package udf.ApiOmiDetec.Layers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import udf.ApiOmiDetec.Globals.GlobalValues;

public class ClassLevel {
	
	private File srcFile;
	private String savePath;
	
	private String fileContent;
	private MethodDeclaration[] methodDeclarations;
	
	public ClassLevel(File file, String path) {
		this.srcFile = file;
		this.savePath = path + file.getName() + "/";
		try{
			if (this.srcFile == null)
				throw new IOException("Need a not null file!");
			if (!this.srcFile.exists())
				throw new IOException("File not exist!");
			
			extractFileContent();
			separatingMethodsInfo();
			processEachMethod();
			
		}catch(IOException e){
			e.printStackTrace();
		}
		
		new File(this.savePath).mkdirs();
	}
	
	private void extractFileContent() throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(this.srcFile));
		String strline;
		this.fileContent = "";
		while((strline=reader.readLine())!=null){
			fileContent += strline + "\n";
		}
	}
	
	private void separatingMethodsInfo(){
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(fileContent.toCharArray());
		parser.setResolveBindings(true);
		CompilationUnit result = (CompilationUnit) parser.createAST(null);
		TypeDeclaration root = null;
		try{
			root = (TypeDeclaration)result.types().get(0);
		}catch(Exception e){;}
		
		if (root != null)
			this.methodDeclarations = root.getMethods();
	}
	
	private void processEachMethod(){
		/*
		 * invoke method level to process method
		 * */
		if (this.methodDeclarations == null) return;
		for (MethodDeclaration dec : this.methodDeclarations){
			MethodLevel met = new MethodLevel(dec, this.savePath);
			GlobalValues.Summary_TotalNumber_Methods ++;
			//System.out.println("==========================" + met.methodName + "==========================");
			/*
			CheckInconsistency_Instanceof check = 
					new CheckInconsistency_Instanceof(InfoBox.ExcepSample, met.getAnnotationData());
			int TagIndex = check.FirstAnnotationTagMetionedFailure();
			
			if (TagIndex < 0){
				switch(TagIndex){
				case -2:System.out.println("There is no annotation javadoc");break;
				case -1:System.out.println("Not mentiond this information");break;
				}
				
			}else{
				processedTagElement theTag = met.getAnnotationData().getProcessedTagData().get(TagIndex);
				System.out.println("Tag " + TagIndex + " mentioned this information :\t" + 
									theTag.getTagName() + ": " + theTag.getKeyWord());
			}
			*/
			
		}
	}
	
	public static void main(String[] args){
		File file = new File("testFiles/testStatements.java");
		String save = "testFiles/res/";
		
		ClassLevel cl = new ClassLevel(file, save);
	}

}
