package udf.testPackage;

public class testPolymorphic {
	
	public static void func(String a, int b){
		System.out.println("func1");
	}
	
	public static void func(int a, String b){
		System.out.println("func2");
	}
	
	public static void main(String[]	args){
		func("s", 1);
		func(2,"b");
	}

}
