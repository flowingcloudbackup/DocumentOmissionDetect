package udf.ApiOmiDetec.Layers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import udf.ApiOmiDetec.Globals.GlobalSettings;
import udf.ApiOmiDetec.Globals.GlobalValues;
import udf.ApiOmiDetec.Infomations.InfoBox;
import udf.ApiOmiDetec.Javadoc.AnnotationData;
import udf.ApiOmiDetec.Javadoc.processedTagElement;
import udf.ApiOmiDetec.Sourcecode.SrcCodeData;
import udf.ApiOmiDetec.check.CheckInconsistency_CellCompare;

public class MethodLevel {
	
	private String savePath;
	public String methodName;
	
	private MethodDeclaration methodDeclaration;
	
	private List<IExtendedModifier> methodModifiers;
	private Type returnType;
	private List<SingleVariableDeclaration> methodParams;
	
	private AnnotationData annotationData;
	private SrcCodeData srcCodeData;
	
	private boolean hasInconsistency;
	
	public MethodLevel(MethodDeclaration dec, String path){
		this.methodDeclaration = dec;
		this.savePath = path;
		
		this.methodName = ((SimpleName)dec.getName()).getIdentifier();

		if (this.methodName.indexOf("insertTab") >= 0){
			System.out.println("bingo");
		}
		extractInformation();
		
		if (GlobalSettings.SplitNoParametersMethod //	根据是否删除的设置
				&& this.methodParams.size() <= 0){	//	判断是否删除没有参数的方法信息
			return;
		}
		
		WriteDocAndSrcInfoBoxesFiles();	//	不管当前有没有InfoBoxes，都输出
						//	因为当前没有，可能递归之后就有
		if (this.srcCodeData.getBoxes().size() > 0){
			//WriteInfoBoxes2Files();
			//	目前的方法是对某个InfoBox进行inconsistency检测
			//WriteInconsistencyLogs();
			
		}
	}
	
	private void extractInformation(){

		this.methodModifiers = this.methodDeclaration.modifiers();
		this.methodParams = this.methodDeclaration.parameters();
		this.returnType = this.methodDeclaration.getReturnType2();
		
		this.annotationData = new AnnotationData(this.methodDeclaration.getJavadoc());
		this.srcCodeData = new SrcCodeData(this.methodDeclaration.getBody(), this.methodName, 
								this.methodParams);
	}
	
	private void WriteDocAndSrcInfoBoxesFiles(){
		
		String metSignature = getMethodSignatureFilename();
		File dir = new File(this.savePath + metSignature + "\\");
		File docFile = new File(this.savePath + metSignature + "\\" + "doc.res");
		File srcFile = new File(this.savePath + metSignature + "\\" + "src.res");
		File metFile = new File(this.savePath + metSignature + "\\" + "metInfo.res");
		try{
			if (!dir.exists()) {dir.mkdirs();}
			if (! docFile.exists()) {docFile.createNewFile();}
			if (! srcFile.exists()) {srcFile.createNewFile();}
			
			FileWriter docOut = new FileWriter(docFile);
			String DocContent = this.annotationData.PresentForStorage();
			docOut.append(DocContent);
			docOut.close();
			if (DocContent.length() <= 0){
				docFile.delete();
			}
			
			FileWriter srcOut = new FileWriter(srcFile);
			List<InfoBox> infoboxes = this.srcCodeData.getBoxes();
			srcOut.append(infoboxes.size() + "\n");
			for (InfoBox box : infoboxes){
				srcOut.append(box.PresentForStorage());
			}
			srcOut.close();
			
			FileWriter metOut = new FileWriter(metFile);
			metOut.append(metSignature + "\n");
			metOut.append(this.methodDeclaration.getModifiers() + "\n");
			Type returnType = this.methodDeclaration.getReturnType2();
			metOut.append(((returnType == null)?"void":returnType.toString()) + "\n");
			List<SingleVariableDeclaration> params = this.methodDeclaration.parameters();
			metOut.append(params.size() + "\n");
			for (SingleVariableDeclaration p : params){
				metOut.append(p.getType().toString() + "-" + p.getName() + "\n");
			}
			metOut.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void WriteInconsistencyLogs(){
		File dir = new File(this.savePath);
		File file = new File(this.savePath + 
				getMethodSignatureFilename() + "_log.res");
		try {
			if (! dir.exists())	{dir.mkdirs();}
			if (! file.exists())	{file.createNewFile();}
			FileWriter writer = new FileWriter(file);
			this.hasInconsistency = false;
			for (InfoBox box : this.srcCodeData.getBoxes()){
				writer.append(box.presentation() + "\n--------------------------------\n");
				writer.append(CheckInfoboxVsAnn(box) + "\n==============================\n");
			}
			writer.close();	
			if (!this.hasInconsistency){
				file.delete();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private String CheckInfoboxVsAnn(InfoBox box){
		String res = null;
		CheckInconsistency_CellCompare check = 
				new CheckInconsistency_CellCompare(box, this.annotationData);
		int TagIndex = check.FirstAnnotationTagMetionedFailure();
		
		if (TagIndex < 0){
			this.hasInconsistency = true;
			switch(TagIndex){
			case -2:res = "There is no annotation javadoc";break;
			case -1:res = "Not mentiond this information";break;
			}
			
		}else{
			processedTagElement theTag = this.annotationData.getProcessedTagData().get(TagIndex);
			res = "Tag " + TagIndex + " mentioned this information :\t" + theTag.getTagName() + ": " + theTag.getKeyWord();
			//System.out.println("Tag " + TagIndex + " mentioned this information :\t" + theTag.getTagName() + ": " + theTag.getKeyWord());
		}
		
		return res;
	}
	
	private void WriteInfoBoxes2Files(){
		File dir = new File(this.savePath);
		File file = new File(this.savePath + 
				getMethodSignatureFilename() + ".res");
		try {
			if (! dir.exists())	{dir.mkdirs();}
			if (! file.exists())	{file.createNewFile();}
			FileWriter writer = new FileWriter(file);
			writer.append("============== source code ==============\n");
			writer.append(this.methodDeclaration.toString());
			writer.append("============== Info Boxes ==============\n");
			for (InfoBox b : this.srcCodeData.getBoxes()){
				writer.append(b.presentation() + "\n");
				GlobalValues.CountSummaryNumber(b);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getMethodSignatureFilename(){
		String res = this.methodName + "(";
		if (this.methodParams.size() > 0){
			for (SingleVariableDeclaration param : this.methodParams){
				String typeName = param.getType().toString();
				int index = typeName.indexOf("<");
				if (index>0){
					typeName = typeName.substring(0, index);
				}
				res += typeName + ",";
			}
			res = res.substring(0, res.length()-1);
		}
		res += ")";
		
		return res;
	}
	
	public AnnotationData getAnnotationData()
	{	return this.annotationData;		}
	
	public static void main(String[] args) throws IOException{
		String filePath = "F:\\JavaProjects\\DocumentOmissionDetect\\testFiles\\testInconsCheck\\testSrcTake\\"; 
				//"F:\\JavaProjects\\DocumentOmissionDetect\\testFiles\\assisMemory\\"; 
				//"F:\\DocErrorDetect\\CellDoc\\testProj\\src\\jdk_part\\java\\awt\\";
		String fileName =
				"BasicStroke.java";
				//"InputEvent.java";
				//"testStatementIterate.java";
				//"testsoloif.java";
				//"testfirstreturn.java";
				//"ActiveEvent.java";
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
		
		GlobalValues.InitTotalNumbers();
		for (MethodDeclaration met : root.getMethods()){
			if (met.getName().toString().contains("getMaskForButton")){
				System.out.println("bingo");
			}
			System.out.println(met.getName());
			System.out.println(met.getModifiers());
			String savePath = "F:\\JavaProjects\\DocumentOmissionDetect\\testFiles\\testInconsCheck\\testSrcTake\\"; 
					//"F:\\DocErrorDetect\\CellDoc\\testProj\\res\\jdk_part\\java\\awt\\ActiveEvent.java\\";
			MethodLevel mtl = new MethodLevel(met, savePath);
			
		}
		GlobalValues.PublishTotalNumbers();
	}
}
