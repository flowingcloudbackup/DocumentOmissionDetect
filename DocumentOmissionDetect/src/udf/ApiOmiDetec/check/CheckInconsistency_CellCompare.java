package udf.ApiOmiDetec.check;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import udf.ApiOmiDetec.FileProcessers.DocFileReader;
import udf.ApiOmiDetec.FileProcessers.InfoBoxesReader;
import udf.ApiOmiDetec.Infomations.InfoBox;
import udf.ApiOmiDetec.Javadoc.AnnotationData;
import udf.ApiOmiDetec.Javadoc.processedTagElement;

public class CheckInconsistency_CellCompare {
	
	private InfoBox infoBox;
	private AnnotationData annData;
	
	private List<processedTagElement> tags;
	
	public CheckInconsistency_CellCompare(InfoBox srcInfo, AnnotationData ann){
		this.infoBox = srcInfo;
		this.annData = ann;
		
		if (this.annData == null || this.infoBox == null)
			throw new NullPointerException("Neither Annotation Data or Information Box can be null");
		// extract processed Tag
		this.tags = this.annData.getProcessedTagData();
	}
	
	public int FirstAnnotationTagMetionedFailure(){
		/*	return :
		 * 		-2 : there is no annotation
		 * 		-1 : the annotation do not mention failure
		 * 		>= 0: the index of mentioned tag
		 * 		
		 * 		>= 100: the index of no description inconsistency
		 * */
		if (!this.annData.GotJavadocDoc){
			return -2;
		}
		int index = -1;
		int count = 0;
		for (processedTagElement tag : this.tags){
			if (Tag_MentionFailure_ExceptionWay(tag)){
				index = count;
				break;
			}
			count ++;
		}
		
		return index;
	} 
	private boolean Tag_MentionFailure_ExceptionWay(processedTagElement tag){
		/*called when infoBox.infoType is EXCEPTION
		 * so we infoBox.exception should not be null
		 * When tag *not* mention this condition may introduce failure, return false  
		 * */
		String tagName = tag.getTagName();
		String keyWord = tag.getKeyWord();
		if (tagName.equalsIgnoreCase("throws") || tagName.equalsIgnoreCase("exception")){
			if (!keyWord.equals(this.infoBox.getException())){
				//	not exactly same kind of exception
				return false;
			}else{
				return Desc_MentionFailure_ExceptionTag(tag.getTagDesc(), -1);
			}
		}else if (tagName.equalsIgnoreCase("param")){
			/* if tagName is param, tag.keyWord should be one of infoBox.params, 
			 * and tag.desc should contain the other params
			 * */
			int indexParam = TagParamIsInInfoBox(keyWord);
			if (indexParam < 0){
				return false;
			}else{
				return Desc_MentionFailure_ExceptionTag(tag.getTagDesc(), indexParam);
			}
		}
		return false;
	}
	
	private int TagParamIsInInfoBox(String keyWord) {
		int index = -1;
		int count = 0;
		if (this.infoBox == null) return index;
		if (this.infoBox.getParameters() == null) return index;
		for (SingleVariableDeclaration param : this.infoBox.getParameters()){
			if (keyWord.equals(param.getName().toString())){
				index = count;
				break;
			}
			count ++;
		}
		
		return index;
	}
	
	private boolean Desc_MentionFailure_ExceptionTag(String desc, int type){
		/* if is @exception tag, it must mention all params	|| type : -1
		 * if is @param tag, it must mention left params	|| type : other (order in infoBox.params)
		 * */
		boolean flag = true;
		//	SingleVariableDeclaration[] paramDecs = this.infoBox.getParameters();
		String[] params = this.infoBox.getParameterNames();
		/*new String[paramDecs.length];
		for (int i = 0 ;i < paramDecs.length ;i++){
			params[i] = paramDecs[i].getName().toString();
		}
		其实这段代码可以删了
		*/
		desc = " " + desc + " ";
		for (String param : params){
			
			String reg = "[^a-zA-Z0-9]" + param + "[^a-zA-Z0-9]";
			Pattern pat = Pattern.compile(reg);
			Matcher mat = pat.matcher(desc);
			
			if (!mat.find()){
				
				if (type < 0){
					flag = false;
					break;
				}else{
					if (params[type].equals(param)){
						continue;
					}else{
						flag = false;
						break;
					}
				}
			}

		}
		
		return flag;
	}
	
	public static void main(String[] args){
		String path = "F:\\JavaProjects\\DocumentOmissionDetect\\testFiles\\assisMemory\\checkNotAWindow(Component)\\";
		
		DocFileReader doc = new DocFileReader(path + "doc.res");
		InfoBoxesReader box = new InfoBoxesReader(path + "src.res");
		
		AnnotationData ann = doc.getAnnData();
		List<InfoBox> boxes = box.getInfoBoxes();
		
		for (InfoBox b : boxes){
			CheckInconsistency_CellCompare check 
				= 	new CheckInconsistency_CellCompare(b, ann);
			//System.out.println(check.FirstAnnotationTagMetionedFailure());
		}
	}

}
