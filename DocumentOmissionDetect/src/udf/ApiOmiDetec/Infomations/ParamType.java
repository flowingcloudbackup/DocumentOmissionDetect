package udf.ApiOmiDetec.Infomations;

import java.util.ArrayList;
import java.util.List;

public class ParamType {
	
	private String type;
	private String name;
	
	public ParamType(String initStr){
		//	初始化方式: 参数类型-参数名
		String[] ss = initStr.split("-");
		this.type = ss[0];
		this.name = ss[1];
		
		ProcessTypeAndName();
	}
	private void ProcessTypeAndName(){
		int index = this.type.indexOf("<");
		if (index > 0){
			this.type = this.type.substring(0, index);
		}
		if (this.name.endsWith("[]")){
			this.type += "[]";
			this.name = this.name.substring(0, this.name.length()-2);
		}
	}
	
	public String getType()
	{	return this.type;	}
	public String getName()
	{	return this.name;	}
	public boolean Equals(ParamType p){
		return this.type.equals(p.getType()) && this.name.equals(p.getName());
	}
	
	public static ParamType[] SimpliseParamInfos(ParamType[] pars){
		List<ParamType> resList = new ArrayList<ParamType>();
		
		for (ParamType par : pars){
			boolean exist = false;
			for (ParamType  p : resList){
				if (p.Equals(par)){
					exist = true;
					break;
				}
			}
			if (! exist){
				resList.add(par);
			}
		}
		
		int size = resList.size();
		ParamType[] resArray = new ParamType[size];
		int i = 0;
		for (ParamType pt : resList){
			resArray[i++] = pt;
		}
		return resArray;
	}

}
