package udf.ApiOmiDetec.check.taginconsistency;

import udf.ApiOmiDetec.Infomations.InfoBox;
import udf.ApiOmiDetec.Javadoc.processedTagElement;

public class InThrowsTag {
	
	private processedTagElement tagInfo;
	private InfoBox boxInfo;
	
	private String ThrownException;
	private String srcDescription;
	private String strDescription;
	
	public InThrowsTag(processedTagElement tag, InfoBox box){
		this.tagInfo = tag;
		this.boxInfo = box;
		
		TagInformationExtraction();
		PreProcess();	//	�޳�һЩ����ԵĶ���������{@code Tag}֮��
	}
	
	private void TagInformationExtraction(){
		this.ThrownException = tagInfo.getKeyWord();
		this.srcDescription = tagInfo.getTagDesc();
	}
	private void PreProcess(){
		
	}

}
