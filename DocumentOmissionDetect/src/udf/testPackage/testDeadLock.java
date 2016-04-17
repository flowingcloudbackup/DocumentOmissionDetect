package udf.testPackage;

public class testDeadLock {

	public static void main(String[] args){
		try{
			boolean flag = true;
			while(flag){
				System.out.println("heheda");
			}
		}catch(Exception e){
			System.out.println("dead lock");
		}
	}
}
