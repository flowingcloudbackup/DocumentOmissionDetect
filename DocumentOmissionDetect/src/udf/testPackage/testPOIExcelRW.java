package udf.testPackage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class testPOIExcelRW {
	
	public static void main(String[] args){
		try{
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet("Sheet_1");
			
			HSSFRow row = sheet.createRow(2);
			HSSFCell cell = row.createCell(2);
			
			cell.setCellValue("test cell");
			
			String filePathName = "F:\\JavaProjects\\DocumentOmissionDetect\\testFiles\\testPOI\\testExcel.xls";
			FileOutputStream os = new FileOutputStream(filePathName);
			wb.write(os);
			os.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

}
