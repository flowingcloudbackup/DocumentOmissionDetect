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
		//	��Ϊ�����ͨ���ж���û�л�ȡjavadoc����Դ�����н�һ�����������������������
	
	private List<TagElement> srcAnnTags;
	private List<processedTagElement> AnnTags;
	
	public AnnotationData(Javadoc doc){
		//	��.java�ļ�����ȡ
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
		//	��doc.res�ļ�����ȡ
		this.srcAnnTags = new ArrayList<TagElement>();
		this.AnnTags = new ArrayList<processedTagElement>();
	}
	public void addProcessedTag(processedTagElement tag){
		//	����ֻ�ڴ�doc.res�ļ�������ʹ��
		this.AnnTags.add(tag);
	}
	
	public Javadoc getSrcJavadoc()
	{	return this.javadocAnnotation;	}
	
	public List<processedTagElement> getProcessedTagData()
	{	return this.AnnTags;	}
	
	public String presentation(int kind){
			//	kind : 1-���TagElements 2-���processdTagElement
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
