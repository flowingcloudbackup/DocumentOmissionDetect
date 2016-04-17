package udf.ApiOmiDetec.Javadoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import udf.ApiOmiDetec.Globals.GlobalValues;

public class AnnotationData {
	
	private Javadoc javadocAnnotation;
	public boolean GotJavadocDoc = false;
		//	因为后面会通过判断有没有获取javadoc数据源来进行进一步操作，所以设置这个开关
	
	private List<TagElement> srcAnnTags;
	private List<processedTagElement> AnnTags;
	
	public AnnotationData(Javadoc doc){
		//	从.java文件中提取
		this.javadocAnnotation = doc;
		if (doc == null)
			return;
		
		this.GotJavadocDoc = true;
		extractInformation();
	}
	
	private void extractInformation(){

		this.srcAnnTags = this.javadocAnnotation.tags();
		this.AnnTags = new ArrayList();
		for (TagElement tag : this.srcAnnTags){
			processedTagElement newTag = new processedTagElement(tag);
			this.AnnTags.add(newTag);
		}
		
		//System.out.println(presentation());
	}
	
	public AnnotationData(){
		//	从doc.res文件中提取
		this.srcAnnTags = new ArrayList<TagElement>();
		this.AnnTags = new ArrayList<processedTagElement>();
	}
	public void addProcessedTag(processedTagElement tag){
		//	尽量只在从doc.res文件读入中使用
		this.AnnTags.add(tag);
	}
	
	public Javadoc getSrcJavadoc()
	{	return this.javadocAnnotation;	}
	
	public List<processedTagElement> getProcessedTagData()
	{	return this.AnnTags;	}
	
	public String presentation(int kind){
			//	kind : 1-输出TagElements 2-输出processdTagElement
		String str = "";
		if (kind == 1){
			for (TagElement e: this.srcAnnTags){
				str += "Tag:\t";
				str += e.getTagName() + "\n";
				str += "Description:";
				str += e.toString();
				str += "\n----------------------------------\n";
			}
		}else if(kind == 2){
			int count = 1;
			for (processedTagElement tag : this.AnnTags){
				str += "Tag_" + (count++)+ ":\n";
				str += "\tTag Type:\t" + tag.getTagName() + "\n";
				str += "\tKey Word:\t" + tag.getKeyWord() + "\n";
				str += "\tDescript:\t" + tag.getTagDesc() + "\n";
				str += "----------------------------------\n";
			}
		}
			
		return str;
	}
	public String PresentForStorage(){
		if (this.AnnTags == null)
			return "";
		String res = this.AnnTags.size() + "\n";
		for (processedTagElement tag : this.AnnTags){
			res += tag.PresentForStorage();
		}
		
		return res;
	}
	
	public static void main(String [] args) throws IOException{
		String filePath =	"F:\\JavaProjects\\DocumentOmissionDetect\\testFiles\\testInconsCheck\\testSrcTake\\"	; 
				//"F:/JavaProjects/DocumentOmissionDetect/testFiles/testMethodLev/";
		String fileName =
				//"Container.java";
				//"testStatementIterate.java";
				//"testsoloif.java";
				//"testfirstreturn.java";
				"InputEvent.java";
		File file = new File(filePath + fileName);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String strline;
		String fileContent = "";
		while((strline=reader.readLine())!=null){
			fileContent += strline + "\n";
		}
		reader.close();
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(fileContent.toCharArray());
		parser.setResolveBindings(true);
		CompilationUnit result = (CompilationUnit) parser.createAST(null);
		TypeDeclaration root = (TypeDeclaration)result.types().get(0);
		
		//GlobalValues.InitTotalNumbers();
		for (MethodDeclaration met : root.getMethods()){
			/*
			System.out.println(met.getName());
			AnnotationData ann = new AnnotationData(met.getJavadoc());
			
			List<processedTagElement> l = ann.getProcessedTagData();
			for (processedTagElement t : l){
				System.out.println(t.presentation());
			}*/
			if (met.getName().toString().contains("getMaskForButton")){
				AnnotationData ann = new AnnotationData(met.getJavadoc());
				List<processedTagElement> l = ann.getProcessedTagData();
				for (processedTagElement t : l){
					System.out.println(t.presentation());
				}
			}
		}
		//GlobalValues.PublishTotalNumbers();
	}
}
