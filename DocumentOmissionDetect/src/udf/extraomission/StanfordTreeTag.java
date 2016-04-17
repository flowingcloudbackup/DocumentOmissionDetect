package udf.extraomission;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.trees.Tree;

public class StanfordTreeTag {
	
	public String tagName;
	public int tagCount;
	
	public StanfordTreeTag(String name){
		this.tagName = name;
		this.tagCount = 1;
	}
	
	public void addTagCount()
	{	this.tagCount ++ ;	}
	
	public static List<StanfordTreeTag> tagList = new ArrayList<StanfordTreeTag>();
	public static void addTreeTag(String name){
		boolean flag = false;
		for (StanfordTreeTag tag : tagList){
			if (tag.tagName.equals(name)){
				flag = true;
				tag.addTagCount();
				break;
			}
		}
		if (!flag){//	如果没找到这种tag，就另立flag
			tagList.add(new StanfordTreeTag(name));
		}
	}
	public static void ScanningTreeTag(Tree t){
		Tree[] children = t.children();
		if (children == null || children.length <= 0){
			return;
		}
		addTreeTag(t.value());
		for (Tree child : children){
			ScanningTreeTag(child);
		}
	}
	
	public static String PresentTreeTagList(){
		String res = "";
		int count = 1;
		for (StanfordTreeTag tag : tagList){
			res += (count++) + "\t" + tag.tagName + "\t" + tag.tagCount + "\n";
		}
		
		return res;
	}

}
