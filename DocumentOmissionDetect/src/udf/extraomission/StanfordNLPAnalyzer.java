package udf.extraomission;

import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class StanfordNLPAnalyzer {
	
	public static String getASTTree(String str){
		/*
		try{
			String ParsingContent = str;
			
			TokenizerFactory<CoreLabel> tokenizerFactory =
			        PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
			Tokenizer<CoreLabel> tok =
			        tokenizerFactory.getTokenizer(new StringReader(ParsingContent));
			List<CoreLabel> rawWords = tok.tokenize();
			
			
			String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
			
			LexicalizedParser lexicalparser = LexicalizedParser.loadModel(parserModel);
			Tree parseTree = lexicalparser.apply(rawWords);
			
			StanfordTreeTag.ScanningTreeTag(parseTree);		//	记录每棵树的tag
			
			return parseTree.toString();
		}catch(Exception e){
			return "AST_PARSING_ERROR";
		}
		*/
		return "";
	}
	
	private static Tree getASTTree_InTreeType(String content){
		//	额外建立的函数，只是用于分析
		try{
			String ParsingContent = content;
			
			TokenizerFactory<CoreLabel> tokenizerFactory =
			        PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
			Tokenizer<CoreLabel> tok =
			        tokenizerFactory.getTokenizer(new StringReader(ParsingContent));
			List<CoreLabel> rawWords = tok.tokenize();
			
			
			String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
			
			LexicalizedParser lexicalparser = LexicalizedParser.loadModel(parserModel);
			Tree parseTree = lexicalparser.apply(rawWords);
			
			return parseTree;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	private static void AnalyseDependencies(String content){
		//	 用于分析关联条件
		try{
			String ParsingContent = content;
			
			TokenizerFactory<CoreLabel> tokenizerFactory =
			        PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
			Tokenizer<CoreLabel> tok =
			        tokenizerFactory.getTokenizer(new StringReader(ParsingContent));
			List<CoreLabel> rawWords = tok.tokenize();
			
			
			String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
			
			LexicalizedParser lexicalparser = LexicalizedParser.loadModel(parserModel);
			Tree parseTree = lexicalparser.apply(rawWords);
			
			TreebankLanguagePack pack = new PennTreebankLanguagePack();
			GrammaticalStructureFactory gfactory = pack.grammaticalStructureFactory();
			GrammaticalStructure gstructure = gfactory.newGrammaticalStructure(parseTree);
			Collection<TypedDependency> dependencies = gstructure.typedDependenciesCCprocessed();
			for(TypedDependency d : dependencies){
				System.out.println(d);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static void analysing(String content){
		Tree t = getASTTree_InTreeType(content);
		System.out.println(t.getClass());
		ScanningTree(t, "");
	}
	private static void ScanningTree(Tree tree, String head){	//	遍历树！
		Tree root = tree;
		Tree[] children = root.children();
		if (children != null && children.length > 0){
			System.out.println(head + root.value());
		}
		for (Tree child : children){
			ScanningTree(child, head + "   ");
		}
		
	}
	private static String processString(String str){
		String res = str.replaceAll("\n", "");
		return res;
	}
	
	public static void main(String[] args){
		String test = "the requested data flavor is not equivalent to either  DataFlavor.stringFlavor  or  DataFlavor.plainTextFlavor  ";
		//System.out.println(getASTTree(test));
		//analysing(test);
		AnalyseDependencies(test);
	}

}
