package udf.testPackage;

import java.util.ArrayList;
import java.util.List;

public class testAddNullArray {
	
	public static void main(String[] args){
		List a = new ArrayList();
		a.add("a");
		a.add("b");
		
		a.add("c");
		
		for (Object o : a){
			System.out.println(o.toString());
		}
	}

}
