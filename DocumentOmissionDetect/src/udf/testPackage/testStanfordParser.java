package udf.testPackage;

import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
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

public class testStanfordParser {
	
	private static String StanfordParsing(String str){
		
		String ParsingContent = str;
		
		TokenizerFactory<CoreLabel> tokenizerFactory =
		        PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
		Tokenizer<CoreLabel> tok =
		        tokenizerFactory.getTokenizer(new StringReader(ParsingContent));
		List<CoreLabel> rawWords = tok.tokenize();
		
		
		String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
		
		LexicalizedParser lexicalparser = LexicalizedParser.loadModel(parserModel);
		Tree parseTree = lexicalparser.apply(rawWords);
		

		//---------------------
		/*
		String[] sent = {"this","is", "an", "easy", "sentence", "."};
		Tree ptree = lexicalparser.apply(Sentence.toWordList(sent));
		*/
		
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parseTree);
		
		GrammaticalStructureCollapse("basic", gs.typedDependencies());
		GrammaticalStructureCollapse("nonCollapsed", gs.allTypedDependencies());
		GrammaticalStructureCollapse("collapsed", gs.typedDependenciesCollapsed());
		GrammaticalStructureCollapse("CCPropagated", gs.typedDependenciesCCprocessed());
		GrammaticalStructureCollapse("tree", gs.typedDependenciesCollapsedTree());
		//---------------------
		
		
		
		return parseTree.toString();
	}
	private static void GrammaticalStructureCollapse(String title, Collection<TypedDependency> tdl){
		System.out.println(title + ":");
		System.out.println("-----------");
		for (TypedDependency dependency : tdl){
			System.out.println(dependency);
		}
		System.out.println("=============================");
	}
	
	
	public static void main(String[] args){
		String str = //"Sam eat red meat";
				"if this parameter is  null  then the value of the the MIME Content Type is used";
		System.out.println(StanfordParsing(str));
	}

}
