package com.kgp.aspect.extraction.support;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * DemoAPI demonstrates other ways of calling the parser with
 * already tokenized text, or in some cases, raw text that needs to
 * be tokenized as a single sentence.  Output is handled with a
 * TreePrint object.  Note that the options used when creating the
 * TreePrint can determine what results to print out.  Once again,
 * one can capture the output by passing a PrintWriter to
 * TreePrint.printTree. This code is for English.
 */
public class DependencyParserImp {
    private static final Logger logger = LoggerFactory.getLogger(DependencyParserImp.class);

    private String PARSER_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
    private LexicalizedParser lexicalizedParser = LexicalizedParser.loadModel(PARSER_MODEL);

    public String demoAPI(String sent[]) {
        List<CoreLabel> rawWords = Sentence.toCoreLabelList(sent);
        Tree parse = lexicalizedParser.apply(rawWords);

        logger.info("\n");

        TreebankLanguagePack tlp1 = lexicalizedParser.treebankLanguagePack(); // PennTreebankLanguagePack for English
        GrammaticalStructureFactory gsf1 = tlp1.grammaticalStructureFactory();
        GrammaticalStructure gs1 = gsf1.newGrammaticalStructure(parse);
        List<TypedDependency> tdl1 = gs1.typedDependenciesCCprocessed();
        return tdl1.toString();
    }
}
