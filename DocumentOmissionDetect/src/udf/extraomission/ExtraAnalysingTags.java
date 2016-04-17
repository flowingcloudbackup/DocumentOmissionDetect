package udf.extraomission;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/*
 * 从.cmp文件中抽取信息，构造xls文件
 * 包括构建 AST 树
 * */
public class ExtraAnalysingTags {
	public static String foldPathRoot = "F:\\JavaProjects\\DocumentOmissionDetect\\testFiles\\testJDKPart\\";
	public static String[] fileNames = {"descrip.cmp","exception.cmp","param.cmp","throw.cmp","throws.cmp"};
	
	public static void ProcessTagDescriptionInfo(String filename) throws IOException{
		File file = new File(foldPathRoot + filename);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Sheet1");
		
		String tagsName = reader.readLine();
		int TagNumber = Integer.valueOf(reader.readLine());
		int rowNumber = 0;
		for (int i = 0;i< TagNumber; i++){
			String tagLine = reader.readLine();
			
			tagLine = SpecialTagReplace.HtmlNotesProcess(tagLine);	//	html特殊标记符替换
			
			int SplitIndex = tagLine.indexOf("#####");
			String PackagePathInfo = tagLine.substring(0, SplitIndex);
			tagLine = tagLine.substring(SplitIndex + 5);	//	获取PackagePathInfo
			
			SplitIndex = tagLine.indexOf("#####");
			String methodParams = tagLine.substring(0, SplitIndex);
			tagLine = tagLine.substring(SplitIndex + 5);	//	 获取 param list
			
			String key = null;
			int sentenceNumber = 0;
			
			if(!IsDescriptionTag(filename)){
					//	desc不需要关键词
				int index = IndexOfFirstBlankSymbol(tagLine);
				key = tagLine.substring(0, index);
				tagLine = tagLine.substring(index);
				
				
				String[] descs = tagLine.split("[\\.;] ");
				for (String desc : descs){
					HSSFRow desRow = sheet.createRow(rowNumber + sentenceNumber);
					if (sentenceNumber == 0){
						HSSFCell keyCell = desRow.createCell(0);
						keyCell.setCellValue(key);
					}
					HSSFCell desCell = desRow.createCell(1);
					desCell.setCellValue(desc);
					
					String processdDesc = SpecialTagReplace.SpecialCodeTagProcess(desc);
					HSSFCell processedDesCell = desRow.createCell(2);
					processedDesCell.setCellValue(processdDesc);
					HSSFCell astCell = desRow.createCell(3);
					astCell.setCellValue(StanfordNLPAnalyzer.getASTTree(processdDesc));
					//	获取AST信息
					
					HSSFCell packCell = desRow.createCell(4);
					packCell.setCellValue(PackagePathInfo);
					HSSFCell paramListCell = desRow.createCell(5);
					paramListCell.setCellValue(methodParams);
					sentenceNumber ++;
				}
			}else{
					//	description tag 的处理方式
				String[] descs = tagLine.split("[\\.;] ");
				for (String desc : descs){
					HSSFRow desRow = sheet.createRow(rowNumber + sentenceNumber);
					HSSFCell desCell = desRow.createCell(0);
					desCell.setCellValue(desc);
					
					String processdDesc = SpecialTagReplace.SpecialCodeTagProcess(desc);
					HSSFCell processedDesCell = desRow.createCell(2);
					processedDesCell.setCellValue(processdDesc);
					HSSFCell astCell = desRow.createCell(3);
					astCell.setCellValue(StanfordNLPAnalyzer.getASTTree(processdDesc));
					
					HSSFCell packCell = desRow.createCell(4);
					packCell.setCellValue(PackagePathInfo);
					HSSFCell paramListCell = desRow.createCell(5);
					paramListCell.setCellValue(methodParams);
					sentenceNumber ++;
				}
			}
			
			rowNumber += sentenceNumber;
			System.out.println("Finish row:\t" + rowNumber);
		}
		
		String excelFileName = filename.substring(0, filename.lastIndexOf(".")) + ".xls";
		FileOutputStream outstream = new FileOutputStream(foldPathRoot + "excels\\" + excelFileName);
		workbook.write(outstream);
		outstream.close();
		
	}
	private static int IndexOfFirstBlankSymbol(String str){
		Pattern pat = Pattern.compile("\\s");
		Matcher mat = pat.matcher(str);
		if (mat.find()){
			return mat.start();
		}else{
			return -1;
		}
	}
	private static boolean IsDescriptionTag(String filename){
		return filename.equals("descrip.cmp");
	}
	
	public static void main(String[] args){
		//Test();
		RunFunction();
		
	}
	private static void Test(){
		String fileRoot = "F:\\JavaProjects\\DocumentOmissionDetect\\testFiles\\ExtraAnalysis\\";
		String fileName = "AbstractRegionPainter.java";
	}
	private static void RunFunction(){
		for (String f : fileNames){
			System.out.println("Dealing with...\t" + f);
			try {
				ProcessTagDescriptionInfo(f);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println(StanfordTreeTag.PresentTreeTagList());
	}

}
