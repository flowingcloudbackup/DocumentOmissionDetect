package udf.ApiOmiDetec.Infomations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InconsistencyResultInfomation {
	
	public boolean NoAnnDoc = false;
	
	public String MethodInfo;
	public List<InfoBox> boxes;
	public List<Integer> indexes;	//	与InfoBox对应的判定结果
	
	public List<String> docPath;
	public List<String> boxPath;
	public List<String> consTypes;
	public List<String> tagTypes;
	
	public static String smallSpliter = "--------------------------------------";
	public static String bigSpliter = "======================================";
	public static String exSpliter = "======================================\n"
									+ "######################################\n"
									+ "======================================";
	
	public InconsistencyResultInfomation(boolean noDoc){
		InitDatas();
		this.NoAnnDoc = noDoc;
	}
	public InconsistencyResultInfomation(String path, boolean noDoc){
		this.MethodInfo = path;
		InitDatas();
		this.NoAnnDoc = noDoc;
	}
	private void InitDatas(){
		this.boxes = new ArrayList<InfoBox>();
		this.indexes = new ArrayList<Integer>();
		this.docPath = new ArrayList<String>();
		this.boxPath = new ArrayList<String>();
		this.consTypes = new ArrayList<String>();
		this.tagTypes = new ArrayList<String>();
	}
	
	public void addNewInfoBoxCheck(InfoBox box, int index, String docpath, String boxpath, String contype, String tagtype){
		this.boxes.add(box);
		this.indexes.add(index);
		this.docPath.add(docpath);
		this.boxPath.add(boxpath);
		this.consTypes.add(contype);
		this.tagTypes.add(tagtype);
	}
	
	public String ToPlantStrings(){
		//	每行代表一个infoBox
		String res = "";
		for (int i = 0; i< this.boxes.size(); i++){
			res += this.docPath.get(i) + ",";
			res += this.boxPath.get(i) + ",";
			res += this.indexes.get(i) + ",";
			res += this.consTypes.get(i) + ",";
			res += this.tagTypes.get(i) + ",";
			res += this.boxes.get(i).ToPlantString() + "\n";
		}
		
		return res;
	}
	
	public String present(){
		String res = "In Method:\t" + this.MethodInfo + "\n";
		res += bigSpliter + "\n";
		if (this.NoAnnDoc){
			res += "No Annotation Document\n";
			res += bigSpliter + "\n";
			return res;
		}
		int boxNum = boxes.size();
		int indexNum = indexes.size();
		if(boxNum != indexNum)	return null;
		
		Iterator<InfoBox> boxItr = boxes.iterator();
		Iterator<Integer> indexItr = indexes.iterator();
		for (int i = 0 ;i< boxNum ;i++){
			res += indexItr.next() + "\n" + smallSpliter + "\n";
			res += boxItr.next().presentation() + "\n";
			res += bigSpliter + "\n";
		}
		
		//res += exSpliter + "\n";
		
		return res;
	}
	
}
