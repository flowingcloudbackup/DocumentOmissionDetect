package udf.ApiOmiDetec.Globals;

import java.util.HashMap;

public class GlobalSettings {
	
	public static String CellInfoSavePathRoot = "F:\\DocErrorDetect\\CellDoc\\testProj\\res\\jdk_part\\";
	//	cell info �洢Ŀ¼
	public static String CallGraphBaseRoot = "F:\\DocErrorDetect\\CallGraphs\\JDT_way\\ATryTest\\jdk_part\\";
	//	call graph �洢��Ŀ¼
	public static String InconsistencyInfoPathRoot = "F:\\DocErrorDetect\\CellDocViaInvokations\\jdk_part_NLP_analyz\\";
	//	inconsistency ��Ϣ�洢
	public static boolean SplitNoParametersMethod = true;	//	�Ƿ�ɾ���޲����ķ�����Ϣ
	public static boolean PrintOutCorrectInfoBoxes = true;	//	�Ƿ����û�в�һ���Ե�InfoBox
	
	public static int CallGraphExploringDepth = 4;
	
	public static int SOLOIF_Num_AcceptableOtherStatements = 3;
	//	��if֮ǰ���Դ��ڵĿɽ������
	
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
	//	��if֮ǰ���Դ��ڵĿɽ������
	
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
