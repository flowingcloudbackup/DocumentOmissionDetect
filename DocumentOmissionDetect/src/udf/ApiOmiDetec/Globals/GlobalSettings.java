package udf.ApiOmiDetec.Globals;

import java.util.HashMap;

public class GlobalSettings {
	
	public static String CellInfoSavePathRoot = "F:\\DocErrorDetect\\CellDoc\\testProj\\res\\jdk_part\\";
	//	cell info 存储目录
	public static String CallGraphBaseRoot = "F:\\DocErrorDetect\\CallGraphs\\JDT_way\\ATryTest\\jdk_part\\";
	//	call graph 存储根目录
	public static String InconsistencyInfoPathRoot = "F:\\DocErrorDetect\\CellDocViaInvokations\\jdk_part_NLP_analyz\\";
	//	inconsistency 信息存储
	public static boolean SplitNoParametersMethod = true;	//	是否删除无参数的方法信息
	public static boolean PrintOutCorrectInfoBoxes = true;	//	是否输出没有不一致性的InfoBox
	
	public static int CallGraphExploringDepth = 4;
	
	public static int SOLOIF_Num_AcceptableOtherStatements = 3;
	//	在if之前可以存在的可接受语句
	
	public static HashMap SOLOIF_Types_AcceptableOtherStatements = new HashMap(){
		{
			String publicstart = "class ";
			String publicPacks = "org.eclipse.jdt.core.dom.";
			String[] AccStates = {
					"ExpressionStatement",
					"BreakStatement",
					"ContinueStatement",
					"EmptyStatement",
					"ExpressionStatement",
					"VariableDeclarationStatement",
					"TypeDeclarationStatement"
			};
			for (String s : AccStates){
				put(publicstart + publicPacks + s, 1);
			}
		}
	};
	
	public static int FIRSTRETURN_Num_AcceptableOtherStatements = 3;
	//	在if之前可以存在的可接受语句
	
	public static HashMap FIRSTRETURN_Types_AcceptableOtherStatements = new HashMap(){
		{
			String publicstart = "class ";
			String publicPacks = "org.eclipse.jdt.core.dom.";
			String[] AccStates = {
					"ExpressionStatement",
					"BreakStatement",
					"ContinueStatement",
					"EmptyStatement",
					"ExpressionStatement",
					"VariableDeclarationStatement",
					"TypeDeclarationStatement"
			};
			for (String s : AccStates){
				put(publicstart + publicPacks + s, 1);
			}
		}
	};
	
	public static void main(String[] args){
		String testStr = "hehe.hehe.hehehe";
		System.out.println(testStr);
	}

}
