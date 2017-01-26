package com.kgp.aspect.extraction;

import com.kgp.aspect.extraction.support.DependencyParserImp;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

public class AspectExtractionApp {
    private static final Logger logger                = LoggerFactory.getLogger(AspectExtractionApp.class);

    private static final String POSITIVE_OPINION_FILE = "/positive.txt";
    private static final String NEGATIVE_OPINION_FILE = "/negative.txt";
    private static final String REVIEWS_FILE          = "/sample.txt";

    private DependencyParserImp dependencyParser = new DependencyParserImp();
    private int flag1, flag2, flag3, flag4, flag5, flag6;

    public static void main(String[] args) throws IOException {
        new AspectExtractionApp().run(args);
    }

    public void run(String args[]) throws IOException {
        String inputDirPath = args[0];
        logger.info("Starting application. dir-path: {}", inputDirPath);

        String positiveOpinionFilePath = inputDirPath + POSITIVE_OPINION_FILE;
        String negativeOpinionFilePath = inputDirPath + NEGATIVE_OPINION_FILE;
        String reviewsFilePath = inputDirPath + REVIEWS_FILE;

        HashMap<String, Integer> opinionToPolarityMap1 = new HashMap<String, Integer>();
        updateOpinions(positiveOpinionFilePath, opinionToPolarityMap1, 1);
        updateOpinions(negativeOpinionFilePath, opinionToPolarityMap1, -1);

        HashMap<String, Integer> opinionToPolarityMap2;
        HashMap<String, Integer> featureToCountMap1 = new HashMap<String, Integer>();
        HashMap<String, Integer> featureToCountMap2;
        ArrayList<String> annote = new ArrayList<>();

        int epoch = 0;
        do {
            resetFlags();

            // Reset featureMap2 and opinionMap2
            featureToCountMap2 = new HashMap<String, Integer>();
            opinionToPolarityMap2 = new HashMap<String, Integer>();

            String line;
            int i;
            int reviewIndex = 0;

            BufferedReader bufferedReader = new BufferedReader(new FileReader(reviewsFilePath));
            while ((line = bufferedReader.readLine()) != null) {
                reviewIndex++;
                logger.info("Processing Review: {}, line: {}", reviewIndex, line);

                int reviewPolarity = getReviewPolarity(reviewIndex, line);
                String rawReviewText = getReviewText(line, reviewIndex);
                String reviewText = getProcessedReviewText(reviewIndex, rawReviewText);

                // Split each review into its constituent sentences
                Reader reader = new StringReader(reviewText);
                DocumentPreprocessor dp = new DocumentPreprocessor(reader);
                ArrayList<String> sentenceList = new ArrayList<String>();

                for (java.util.List<HasWord> sentence : dp) {
                    String sentenceString = Sentence.listToString(sentence);
                    sentenceList.add(sentenceString.toString());
                }
                //for each sentence---1)Word tokenization 2)POS Tag 3) wordPOS array
                for (String sentence : sentenceList) {
                    System.out.println("Review Sentence=" + sentence);
                    String word[] = sentence.split(" ");    //1)Word tokenization
                    String wordPOS[] = new String[word.length];
                    System.out.println(wordPOS.length);
                    for (i = 0; i < word.length; i++)
                        System.out.print(word[i] + " ");

                    //Tag the sentence
                    MaxentTagger tagger = new MaxentTagger("tagger/english-bidirectional-distsim.tagger");//
                    String tagged = tagger.tagString(sentence);
                    System.out.println(tagged);
                    int flag = 0;
                    int count = 0;

                    //Calculate wordPOS array
                    for (i = 0; i < tagged.length(); i++) {
                        if (tagged.charAt(i) == '_' && flag == 0) {
                            flag = 1;
                            wordPOS[count] = Character.toString(tagged.charAt(i + 1));
                            i++;
                        } else if (tagged.charAt(i) != ' ' && flag == 1) {
                            wordPOS[count] += tagged.charAt(i);
                        } else if (tagged.charAt(i) == ' ' && flag == 1) {
                            flag = 0;
                            count++;
                        } else {
                            continue;
                        }
                    }
                    for (i = 0; i < word.length; i++)
                        System.out.print(wordPOS[i] + " ");

                    //Dependency Parsing
                    String dep = dependencyParser.demoAPI(word);
                    System.out.println("DEP=" + dep);

                    int spacecount = 0;
                    for (i = 1; i < dep.length() - 1; i++) {
                        if (dep.charAt(i) == ' ') {
                            spacecount++;
                        }
                    }
                    spacecount = (int) (spacecount / 2) + 1;
                    System.out.println(spacecount);
                    String depMatrix[][] = new String[spacecount][3];
                    flag = 0;
                    int p = 0;
                    String temp = "";
                    String temp1 = "";
                    String temp3 = "";
                    for (i = 1; i < dep.length() - 1; i++) {
                        if (dep.charAt(i) != '(' && flag == 0) {
                            temp3 += dep.charAt(i);
                        } else if (dep.charAt(i) == '(' && flag == 0) {
                            flag = 1;
                        } else if (dep.charAt(i) != ',' && flag == 1) {
                            temp += dep.charAt(i);
                        } else if (dep.charAt(i) == ',' && flag == 1) {
                            flag = 2;
                        } else if (dep.charAt(i) != ')' && flag == 2) {
                            temp1 += dep.charAt(i);
                        } else if (dep.charAt(i) == ')' && flag == 2) {
                            i += 2;
                            flag = 0;
                            // System.out.println(temp3+"$"+temp.trim()+"$"+temp1.trim());
                            //take the words
                            depMatrix[p][0] = temp3.trim();
                            depMatrix[p][1] = temp.substring(0, temp.lastIndexOf('-')).trim();
                            depMatrix[p++][2] = temp1.substring(0, temp1.lastIndexOf('-')).trim();
                            temp = "";
                            temp1 = "";
                            temp3 = "";
                        }
                        //else
                        //  continue;
                    }
                    for (i = 0; i < p; i++)
                        System.out.println(depMatrix[i][0] + " " + depMatrix[i][1] + " " + depMatrix[i][2] + " ");

                    //Rule R1.1

                    for (String name : opinionToPolarityMap1.keySet()) {
                        String key = name.toString();
                        // System.out.println(op[0]);
                        for (int posi = 0; posi < word.length; posi++) {
                            // System.out.println(word[posi]);
                            if (key.equalsIgnoreCase(word[posi]) == true && (wordPOS[posi].equalsIgnoreCase("JJ") == true || wordPOS[posi].equalsIgnoreCase("JJR") == true || wordPOS[posi].equalsIgnoreCase("JJS") == true)) {
                                for (i = 0; i < p; i++) {
                                    if (depMatrix[i][1].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj"))) {
                                        String tword = depMatrix[i][2];
                                        for (int loopi = 1; loopi < word.length - 1; loopi++) {
                                            if (tword.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS") || wordPOS[loopi].equalsIgnoreCase("NNP"))) {
                                                if (wordPOS[loopi - 1].equalsIgnoreCase("NN") || wordPOS[loopi - 1].equalsIgnoreCase("NNS") || wordPOS[loopi - 1].equalsIgnoreCase("NNP")) {
                                                    tword = word[loopi - 1] + " " + tword;
                                                }
                                                if (wordPOS[loopi + 1].equalsIgnoreCase("NN") || wordPOS[loopi + 1].equalsIgnoreCase("NNS") || wordPOS[loopi + 1].equalsIgnoreCase("NNP")) {
                                                    tword = tword + " " + word[loopi + 1];
                                                }
                                                if (featureToCountMap1.containsKey(tword)) {
                                                    Integer counter = ((Integer) featureToCountMap1.get(tword));
                                                    featureToCountMap1.put(tword, new Integer(counter + 1));
                                                } else {
                                                    featureToCountMap1.put(tword, 1);
                                                    flag1 = 1;
                                                }
                                                negPol = 1;
                                                for (int PolVar = Math.max(0, posi - 2); PolVar < Math.min(word.length, posi + 2); PolVar++)
                                                    if (word[PolVar].equalsIgnoreCase("not") || word[PolVar].equalsIgnoreCase("n't") || word[PolVar].equalsIgnoreCase("'t") || word[PolVar].equalsIgnoreCase("however") || word[PolVar].equalsIgnoreCase("but") || word[PolVar].equalsIgnoreCase("despite") || word[PolVar].equalsIgnoreCase("though") || word[PolVar].equalsIgnoreCase("except") || word[PolVar].equalsIgnoreCase("although") || word[PolVar].equalsIgnoreCase("oddly")) {
                                                        negPol = -1;
                                                    }

                                                Integer val = negPol * ((Integer) opinionToPolarityMap1.get(key));
                                                String annt = new String();
                                                annt = reviewIndex + "@" + tword + "@" + Integer.toString(val);
                                                if (annote.contains(annt)) {

                                                } else {
                                                    annote.add(annt);
                                                }

                                            }

                                        }

                                    }
                                    if (depMatrix[i][2].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj"))) {
                                        String tword = depMatrix[i][1];
                                        for (int loopi = 1; loopi < word.length - 1; loopi++) {
                                            if (tword.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS") || wordPOS[loopi].equalsIgnoreCase("NNP"))) {
                                                if (wordPOS[loopi - 1].equalsIgnoreCase("NN") || wordPOS[loopi - 1].equalsIgnoreCase("NNS") || wordPOS[loopi - 1].equalsIgnoreCase("NNP")) {
                                                    tword = word[loopi - 1] + " " + tword;
                                                }
                                                if (wordPOS[loopi + 1].equalsIgnoreCase("NN") || wordPOS[loopi + 1].equalsIgnoreCase("NNS") || wordPOS[loopi + 1].equalsIgnoreCase("NNP")) {
                                                    tword = tword + " " + word[loopi + 1];
                                                }
                                                if (featureToCountMap1.containsKey(tword)) {
                                                    Integer counter = ((Integer) featureToCountMap1.get(tword));
                                                    featureToCountMap1.put(tword, new Integer(counter + 1));
                                                } else {
                                                    featureToCountMap1.put(tword, 1);
                                                    flag1 = 1;
                                                }

                                                negPol = 1;
                                                for (int PolVar = Math.max(0, posi - 2); PolVar < Math.min(word.length, posi + 2); PolVar++)
                                                    if (word[PolVar].equalsIgnoreCase("not") || word[PolVar].equalsIgnoreCase("n't") || word[PolVar].equalsIgnoreCase("'t") || word[PolVar].equalsIgnoreCase("however") || word[PolVar].equalsIgnoreCase("but") || word[PolVar].equalsIgnoreCase("despite") || word[PolVar].equalsIgnoreCase("though") || word[PolVar].equalsIgnoreCase("except") || word[PolVar].equalsIgnoreCase("although") || word[PolVar].equalsIgnoreCase("oddly")) {
                                                        negPol = -1;
                                                    }


                                                Integer val = negPol * ((Integer) opinionToPolarityMap1.get(key));
                                                String annt = new String();
                                                annt = reviewIndex + "@" + tword + "@" + Integer.toString(val);
                                                if (annote.contains(annt)) {

                                                } else {
                                                    annote.add(annt);
                                                }
                                            }

                                        }

                                    }
                                }
                            }
                        }


                    }

                    //Rule R1.2

                    for (String name : opinionToPolarityMap1.keySet()) {
                        String key = name.toString();
                        // System.out.println(op[0]);
                        for (int posi = 0; posi < word.length; posi++) {
                            // System.out.println(word[posi]);
                            if (key.equalsIgnoreCase(word[posi]) == true && (wordPOS[posi].equalsIgnoreCase("JJ") == true || wordPOS[posi].equalsIgnoreCase("JJR") == true || wordPOS[posi].equalsIgnoreCase("JJS") == true)) {
                                for (i = 0; i < p; i++) {
                                    if (depMatrix[i][1].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj"))) {
                                        String tword = depMatrix[i][2];

                                        for (int j = 0; j < p; j++) {
                                            if (depMatrix[j][1].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj"))) {
                                                String tword2 = depMatrix[j][2];

                                                for (int loopi = 1; loopi < word.length - 1; loopi++) {
                                                    if (tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS") || wordPOS[loopi].equalsIgnoreCase("NNP"))) {
                                                        if (wordPOS[loopi - 1].equalsIgnoreCase("NN") || wordPOS[loopi - 1].equalsIgnoreCase("NNS") || wordPOS[loopi - 1].equalsIgnoreCase("NNP")) {
                                                            tword2 = word[loopi - 1] + " " + tword2;
                                                        }
                                                        if (wordPOS[loopi + 1].equalsIgnoreCase("NN") || wordPOS[loopi + 1].equalsIgnoreCase("NNS") || wordPOS[loopi + 1].equalsIgnoreCase("NNP")) {
                                                            tword2 = tword2 + " " + word[loopi + 1];
                                                        }
                                                        if (featureToCountMap1.containsKey(tword2)) {
                                                            Integer counter = ((Integer) featureToCountMap1.get(tword2));
                                                            featureToCountMap1.put(tword2, new Integer(counter + 1));
                                                        } else {
                                                            featureToCountMap1.put(tword2, 1);
                                                            flag2 = 1;
                                                        }
                                                        negPol = 1;
                                                        for (int PolVar = Math.max(0, posi - 2); PolVar < Math.min(word.length, posi + 2); PolVar++)
                                                            if (word[PolVar].equalsIgnoreCase("not") || word[PolVar].equalsIgnoreCase("n't") || word[PolVar].equalsIgnoreCase("'t") || word[PolVar].equalsIgnoreCase("however") || word[PolVar].equalsIgnoreCase("but") || word[PolVar].equalsIgnoreCase("despite") || word[PolVar].equalsIgnoreCase("though") || word[PolVar].equalsIgnoreCase("except") || word[PolVar].equalsIgnoreCase("although") || word[PolVar].equalsIgnoreCase("oddly")) {
                                                                negPol = -1;
                                                            }
                                                        Integer val = negPol * ((Integer) opinionToPolarityMap1.get(key));
                                                        String annt = new String();
                                                        annt = reviewIndex + "@" + tword2 + "@" + Integer.toString(val);
                                                        if (annote.contains(annt)) {

                                                        } else {
                                                            annote.add(annt);
                                                        }
                                                    }

                                                }

                                            }
                                            if (depMatrix[j][2].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj"))) {
                                                String tword2 = depMatrix[j][1];

                                                for (int loopi = 1; loopi < word.length - 1; loopi++) {
                                                    if (tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS") || wordPOS[loopi].equalsIgnoreCase("NNP"))) {
                                                        if (wordPOS[loopi - 1].equalsIgnoreCase("NN") || wordPOS[loopi - 1].equalsIgnoreCase("NNS") || wordPOS[loopi - 1].equalsIgnoreCase("NNP")) {
                                                            tword2 = word[loopi - 1] + " " + tword2;
                                                        }
                                                        if (wordPOS[loopi + 1].equalsIgnoreCase("NN") || wordPOS[loopi + 1].equalsIgnoreCase("NNS") || wordPOS[loopi + 1].equalsIgnoreCase("NNP")) {
                                                            tword2 = tword2 + " " + word[loopi + 1];
                                                        }
                                                        if (featureToCountMap1.containsKey(tword2)) {
                                                            Integer counter = ((Integer) featureToCountMap1.get(tword2));
                                                            featureToCountMap1.put(tword2, new Integer(counter + 1));
                                                        } else {
                                                            featureToCountMap1.put(tword2, 1);
                                                            flag2 = 1;
                                                        }
                                                        negPol = 1;
                                                        for (int PolVar = Math.max(0, posi - 2); PolVar < Math.min(word.length, posi + 2); PolVar++)
                                                            if (word[PolVar].equalsIgnoreCase("not") || word[PolVar].equalsIgnoreCase("n't") || word[PolVar].equalsIgnoreCase("'t") || word[PolVar].equalsIgnoreCase("however") || word[PolVar].equalsIgnoreCase("but") || word[PolVar].equalsIgnoreCase("despite") || word[PolVar].equalsIgnoreCase("though") || word[PolVar].equalsIgnoreCase("except") || word[PolVar].equalsIgnoreCase("although") || word[PolVar].equalsIgnoreCase("oddly")) {
                                                                negPol = -1;
                                                            }
                                                        Integer val = negPol * ((Integer) opinionToPolarityMap1.get(key));
                                                        String annt = new String();
                                                        annt = reviewIndex + "@" + tword2 + "@" + Integer.toString(val);
                                                        if (annote.contains(annt)) {

                                                        } else {
                                                            annote.add(annt);
                                                        }
                                                    }

                                                }

                                            }

                                        }
                                    }
                                    if (depMatrix[i][2].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj"))) {
                                        String tword = depMatrix[i][1];///

                                        for (int j = 0; j < p; j++) {
                                            if (depMatrix[j][1].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj"))) {
                                                String tword2 = depMatrix[j][2];

                                                for (int loopi = 1; loopi < word.length - 1; loopi++) {
                                                    if (tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS") || wordPOS[loopi].equalsIgnoreCase("NNP"))) {
                                                        if (wordPOS[loopi - 1].equalsIgnoreCase("NN") || wordPOS[loopi - 1].equalsIgnoreCase("NNS") || wordPOS[loopi - 1].equalsIgnoreCase("NNP")) {
                                                            tword2 = word[loopi - 1] + " " + tword2;
                                                        }
                                                        if (wordPOS[loopi + 1].equalsIgnoreCase("NN") || wordPOS[loopi + 1].equalsIgnoreCase("NNS") || wordPOS[loopi + 1].equalsIgnoreCase("NNP")) {
                                                            tword2 = tword2 + " " + word[loopi + 1];
                                                        }
                                                        if (featureToCountMap1.containsKey(tword2)) {
                                                            Integer counter = ((Integer) featureToCountMap1.get(tword2));
                                                            featureToCountMap1.put(tword2, new Integer(counter + 1));
                                                        } else {
                                                            featureToCountMap1.put(tword2, 1);
                                                            flag2 = 1;
                                                        }
                                                        negPol = 1;
                                                        for (int PolVar = Math.max(0, posi - 2); PolVar < Math.min(word.length, posi + 2); PolVar++)
                                                            if (word[PolVar].equalsIgnoreCase("not") || word[PolVar].equalsIgnoreCase("n't") || word[PolVar].equalsIgnoreCase("'t") || word[PolVar].equalsIgnoreCase("however") || word[PolVar].equalsIgnoreCase("but") || word[PolVar].equalsIgnoreCase("despite") || word[PolVar].equalsIgnoreCase("though") || word[PolVar].equalsIgnoreCase("except") || word[PolVar].equalsIgnoreCase("although") || word[PolVar].equalsIgnoreCase("oddly")) {
                                                                negPol = -1;
                                                            }
                                                        Integer val = negPol * ((Integer) opinionToPolarityMap1.get(key));
                                                        String annt = new String();
                                                        annt = reviewIndex + "@" + tword2 + "@" + Integer.toString(val);
                                                        if (annote.contains(annt)) {

                                                        } else {
                                                            annote.add(annt);
                                                        }
                                                    }

                                                }

                                            }
                                            if (depMatrix[j][2].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj"))) {
                                                String tword2 = depMatrix[j][1];

                                                for (int loopi = 1; loopi < word.length - 1; loopi++) {
                                                    if (tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS") || wordPOS[loopi].equalsIgnoreCase("NNP"))) {
                                                        if (wordPOS[loopi - 1].equalsIgnoreCase("NN") || wordPOS[loopi - 1].equalsIgnoreCase("NNS") || wordPOS[loopi - 1].equalsIgnoreCase("NNP")) {
                                                            tword2 = word[loopi - 1] + " " + tword2;
                                                        }
                                                        if (wordPOS[loopi + 1].equalsIgnoreCase("NN") || wordPOS[loopi + 1].equalsIgnoreCase("NNS") || wordPOS[loopi + 1].equalsIgnoreCase("NNP")) {
                                                            tword2 = tword2 + " " + word[loopi + 1];
                                                        }
                                                        if (featureToCountMap1.containsKey(tword2)) {
                                                            Integer counter = ((Integer) featureToCountMap1.get(tword2));
                                                            featureToCountMap1.put(tword2, new Integer(counter + 1));
                                                        } else {
                                                            featureToCountMap1.put(tword2, 1);
                                                            flag2 = 1;
                                                        }
                                                        negPol = 1;
                                                        for (int PolVar = Math.max(0, posi - 2); PolVar < Math.min(word.length, posi + 2); PolVar++)
                                                            if (word[PolVar].equalsIgnoreCase("not") || word[PolVar].equalsIgnoreCase("n't") || word[PolVar].equalsIgnoreCase("'t") || word[PolVar].equalsIgnoreCase("however") || word[PolVar].equalsIgnoreCase("but") || word[PolVar].equalsIgnoreCase("despite") || word[PolVar].equalsIgnoreCase("though") || word[PolVar].equalsIgnoreCase("except") || word[PolVar].equalsIgnoreCase("although") || word[PolVar].equalsIgnoreCase("oddly")) {
                                                                negPol = -1;
                                                            }
                                                        Integer val = negPol * ((Integer) opinionToPolarityMap1.get(key));
                                                        String annt = new String();
                                                        annt = reviewIndex + "@" + tword2 + "@" + Integer.toString(val);
                                                        if (annote.contains(annt)) {

                                                        } else {
                                                            annote.add(annt);
                                                        }
                                                    }

                                                }

                                            }

                                        }
                                    }


                                }


                            }

                        }


                    }         //////end of Rule 1.2
                    //Rule 4.1
                    for (String name : opinionToPolarityMap1.keySet()) {
                        String key = name.toString();
                        for (int posi = 0; posi < word.length; posi++) {
                            if (key.equalsIgnoreCase(word[posi]) == true && (wordPOS[posi].equalsIgnoreCase("JJ") == true || wordPOS[posi].equalsIgnoreCase("JJR") == true || wordPOS[posi].equalsIgnoreCase("JJS") == true)) {
                                for (i = 0; i < p; i++) {
                                    if (depMatrix[i][1].equalsIgnoreCase(word[posi]) && depMatrix[i][0].toLowerCase().contains("conj")) {
                                        String tword = depMatrix[i][2];
                                        for (int loopi = 0; loopi < word.length; loopi++) {
                                            if (tword.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("JJ") || wordPOS[loopi].equalsIgnoreCase("JJR") || wordPOS[loopi].equalsIgnoreCase("JJS"))) {
                                                if (opinionToPolarityMap2.containsKey(tword)) {
                                                  /*Integer counter = ((Integer)opinionToPolarityMap2.get(tword));
										          opinionToPolarityMap2.put(tword, new Integer(counter +1));*/
                                                } else {

                                                    Integer value = (Integer) opinionToPolarityMap1.get(name);
                                                    opinionToPolarityMap2.put(tword, value);
                                                }
                                            }

                                        }

                                    }
                                    if (depMatrix[i][2].equalsIgnoreCase(word[posi]) && depMatrix[i][0].toLowerCase().contains("conj")) {
                                        String tword = depMatrix[i][1];
                                        for (int loopi = 0; loopi < word.length; loopi++) {
                                            if (tword.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("JJ") || wordPOS[loopi].equalsIgnoreCase("JJR") || wordPOS[loopi].equalsIgnoreCase("JJS"))) {
                                                if (opinionToPolarityMap2.containsKey(tword)) {
										    	  /*Integer counter = ((Integer)opinionToPolarityMap2.get(tword));
										          opinionToPolarityMap2.put(tword, new Integer(counter +1));*/
                                                } else {

                                                    Integer value = (Integer) opinionToPolarityMap1.get(name);
                                                    opinionToPolarityMap2.put(tword, value);
                                                }
                                            }

                                        }

                                    }
                                }
                            }
                        }


                    }

                    for (String name : opinionToPolarityMap2.keySet()) {
                        String key = name.toString();
                        Integer value = (Integer) opinionToPolarityMap2.get(name);
                        if (opinionToPolarityMap1.containsKey(key)) {
					    	  /*Integer counter = ((Integer)opinionToPolarityMap1.get(key));
					          opinionToPolarityMap1.put(key, new Integer(counter + value));*/
                        } else {
                            opinionToPolarityMap1.put(key, value);
                            flag3 = 1;
                        }
                    }

                    opinionToPolarityMap2 = new HashMap<String, Integer>();

///end of Rule 4.1

                    //  f9.addAll(featureI);
                    ////Rule 3.1
                    for (String name : featureToCountMap1.keySet()) {
                        String key = name.toString();
                        for (int posi = 0; posi < word.length; posi++) {
                            if (key.equalsIgnoreCase(word[posi]) == true && (wordPOS[posi].equalsIgnoreCase("NN") == true || wordPOS[posi].equalsIgnoreCase("NNS") == true || wordPOS[posi].equalsIgnoreCase("NNP") == true)) {
                                for (i = 0; i < p; i++) {
                                    if (depMatrix[i][1].equalsIgnoreCase(word[posi]) && depMatrix[i][0].toLowerCase().contains("conj")) {
                                        String tword = depMatrix[i][2];
                                        for (int loopi = 1; loopi < word.length - 1; loopi++) {
                                            if (tword.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS") || wordPOS[loopi].equalsIgnoreCase("NNP"))) {
                                                if (wordPOS[loopi - 1].equalsIgnoreCase("NN") || wordPOS[loopi - 1].equalsIgnoreCase("NNS") || wordPOS[loopi - 1].equalsIgnoreCase("NNP")) {
                                                    tword = word[loopi - 1] + " " + tword;
                                                }
                                                if (wordPOS[loopi + 1].equalsIgnoreCase("NN") || wordPOS[loopi + 1].equalsIgnoreCase("NNS") || wordPOS[loopi + 1].equalsIgnoreCase("NNP")) {
                                                    tword = tword + " " + word[loopi + 1];
                                                }
                                                if (featureToCountMap2.containsKey(tword)) {
                                                    Integer counter = ((Integer) featureToCountMap2.get(tword));
                                                    featureToCountMap2.put(tword, new Integer(counter + 1));
                                                } else {
                                                    featureToCountMap2.put(tword, 1);
                                                }
                                            }

                                        }

                                    }
                                    if (depMatrix[i][2].equalsIgnoreCase(word[posi]) && depMatrix[i][0].toLowerCase().contains("conj")) {
                                        String tword = depMatrix[i][1];
                                        for (int loopi = 1; loopi < word.length - 1; loopi++) {
                                            if (tword.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNP") || wordPOS[loopi].equalsIgnoreCase("NNS"))) {
                                                if (wordPOS[loopi - 1].equalsIgnoreCase("NN") || wordPOS[loopi - 1].equalsIgnoreCase("NNS") || wordPOS[loopi - 1].equalsIgnoreCase("NNP")) {
                                                    tword = word[loopi - 1] + " " + tword;
                                                }
                                                if (wordPOS[loopi + 1].equalsIgnoreCase("NN") || wordPOS[loopi + 1].equalsIgnoreCase("NNS") || wordPOS[loopi + 1].equalsIgnoreCase("NNP")) {
                                                    tword = tword + " " + word[loopi + 1];
                                                }
                                                if (featureToCountMap2.containsKey(tword)) {
                                                    Integer counter = ((Integer) featureToCountMap2.get(tword));
                                                    featureToCountMap2.put(tword, new Integer(counter + 1));
                                                } else {
                                                    featureToCountMap2.put(tword, 1);
                                                }
                                            }

                                        }

                                    }
                                }
                            }
                        }


                    }
                    for (String name : featureToCountMap2.keySet()) {
                        String key = name.toString();
                        Integer value = (Integer) featureToCountMap2.get(name);
                        if (featureToCountMap1.containsKey(key)) {
                            Integer counter = ((Integer) featureToCountMap1.get(key));
                            featureToCountMap1.put(key, new Integer(counter + value));
                        } else {
                            featureToCountMap1.put(key, value);
                            flag4 = 1;
                        }
                    }

                    featureToCountMap2 = new HashMap<String, Integer>();
                    ///end of Rule 3.1


                    ////Rule 3.2

                    for (String name : featureToCountMap1.keySet()) {
                        String key = name.toString();
                        for (int posi = 0; posi < word.length; posi++) {
                            if (key.equalsIgnoreCase(word[posi]) == true && (wordPOS[posi].equalsIgnoreCase("NN") == true || wordPOS[posi].equalsIgnoreCase("NNP") == true || wordPOS[posi].equalsIgnoreCase("NNS") == true)) {
                                for (i = 0; i < p; i++) {
                                    if (depMatrix[i][1].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj"))) {
                                        String tword = depMatrix[i][2];

                                        for (int j = 0; j < p; j++) {
                                            if (depMatrix[j][1].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj"))) {
                                                String tword2 = depMatrix[j][2];

                                                for (int loopi = 1; loopi < word.length - 1; loopi++) {
                                                    if (tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS") || wordPOS[loopi].equalsIgnoreCase("NNP"))) {
                                                        if (wordPOS[loopi - 1].equalsIgnoreCase("NN") || wordPOS[loopi - 1].equalsIgnoreCase("NNS") || wordPOS[loopi - 1].equalsIgnoreCase("NNP")) {
                                                            tword2 = word[loopi - 1] + " " + tword2;
                                                        }
                                                        if (wordPOS[loopi + 1].equalsIgnoreCase("NN") || wordPOS[loopi + 1].equalsIgnoreCase("NNS") || wordPOS[loopi + 1].equalsIgnoreCase("NNP")) {
                                                            tword2 = tword2 + " " + word[loopi + 1];
                                                        }
                                                        if (featureToCountMap2.containsKey(tword2)) {
                                                            Integer counter = ((Integer) featureToCountMap2.get(tword2));
                                                            featureToCountMap2.put(tword2, new Integer(counter + 1));
                                                        } else {
                                                            featureToCountMap2.put(tword2, 1);
                                                        }


                                                    }

                                                }

                                            }
                                            if (depMatrix[j][2].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj"))) {
                                                String tword2 = depMatrix[j][1];

                                                for (int loopi = 1; loopi < word.length - 1; loopi++) {
                                                    if (tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS") || wordPOS[loopi].equalsIgnoreCase("NNP"))) {
                                                        if (wordPOS[loopi - 1].equalsIgnoreCase("NN") || wordPOS[loopi - 1].equalsIgnoreCase("NNS") || wordPOS[loopi - 1].equalsIgnoreCase("NNP")) {
                                                            tword2 = word[loopi - 1] + " " + tword2;
                                                        }
                                                        if (wordPOS[loopi + 1].equalsIgnoreCase("NN") || wordPOS[loopi + 1].equalsIgnoreCase("NNS") || wordPOS[loopi + 1].equalsIgnoreCase("NNP")) {
                                                            tword2 = tword2 + " " + word[loopi + 1];
                                                        }
                                                        if (featureToCountMap2.containsKey(tword2)) {
                                                            Integer counter = ((Integer) featureToCountMap2.get(tword2));
                                                            featureToCountMap2.put(tword2, new Integer(counter + 1));
                                                        } else {
                                                            featureToCountMap2.put(tword2, 1);
                                                        }
                                                    }

                                                }

                                            }

                                        }
                                    }
                                    if (depMatrix[i][2].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj"))) {
                                        String tword = depMatrix[i][1];///

                                        for (int j = 0; j < p; j++) {
                                            if (depMatrix[j][1].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj"))) {
                                                String tword2 = depMatrix[j][2];

                                                for (int loopi = 1; loopi < word.length - 1; loopi++) {
                                                    if (tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS") || wordPOS[loopi].equalsIgnoreCase("NNP"))) {
                                                        if (wordPOS[loopi - 1].equalsIgnoreCase("NN") || wordPOS[loopi - 1].equalsIgnoreCase("NNS") || wordPOS[loopi - 1].equalsIgnoreCase("NNP")) {
                                                            tword2 = word[loopi - 1] + " " + tword2;
                                                        }
                                                        if (wordPOS[loopi + 1].equalsIgnoreCase("NN") || wordPOS[loopi + 1].equalsIgnoreCase("NNS") || wordPOS[loopi + 1].equalsIgnoreCase("NNP")) {
                                                            tword2 = tword2 + " " + word[loopi + 1];
                                                        }
                                                        if (featureToCountMap2.containsKey(tword2)) {
                                                            Integer counter = ((Integer) featureToCountMap2.get(tword2));
                                                            featureToCountMap2.put(tword2, new Integer(counter + 1));
                                                        } else {
                                                            featureToCountMap2.put(tword2, 1);
                                                        }
                                                    }

                                                }

                                            }
                                            if (depMatrix[j][2].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj"))) {
                                                String tword2 = depMatrix[j][1];

                                                for (int loopi = 1; loopi < word.length - 1; loopi++) {
                                                    if (tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS") || wordPOS[loopi].equalsIgnoreCase("NNP"))) {
                                                        if (wordPOS[loopi - 1].equalsIgnoreCase("NN") || wordPOS[loopi - 1].equalsIgnoreCase("NNS") || wordPOS[loopi - 1].equalsIgnoreCase("NNP")) {
                                                            tword2 = word[loopi - 1] + " " + tword2;
                                                        }
                                                        if (wordPOS[loopi + 1].equalsIgnoreCase("NN") || wordPOS[loopi + 1].equalsIgnoreCase("NNS") || wordPOS[loopi + 1].equalsIgnoreCase("NNP")) {
                                                            tword2 = tword2 + " " + word[loopi + 1];
                                                        }
                                                        if (featureToCountMap2.containsKey(tword2)) {
                                                            Integer counter = ((Integer) featureToCountMap2.get(tword2));
                                                            featureToCountMap2.put(tword2, new Integer(counter + 1));
                                                        } else {
                                                            featureToCountMap2.put(tword2, 1);
                                                        }
                                                    }

                                                }

                                            }

                                        }
                                    }


                                }


                            }

                        }


                    }

                    for (String name : featureToCountMap2.keySet()) {
                        String key = name.toString();
                        Integer value = (Integer) featureToCountMap2.get(name);
                        if (featureToCountMap1.containsKey(key)) {
                            Integer counter = ((Integer) featureToCountMap1.get(key));
                            featureToCountMap1.put(key, new Integer(counter + value));
                        } else {
                            featureToCountMap1.put(key, value);
                            flag5 = 1;
                        }
                    }

                    featureToCountMap2 = new HashMap<String, Integer>();
                    ////End of Rule 3.2


                    //Rule 2.1

                    //opDictexpanded.addAll(opI);


                    for (String name : featureToCountMap1.keySet()) {
                        String key = name.toString();
                        for (int posi = 0; posi < word.length; posi++) {
                            // System.out.println(word[posi]);
                            if (key.equalsIgnoreCase(word[posi]) == true && (wordPOS[posi].equalsIgnoreCase("NN") == true || wordPOS[posi].equalsIgnoreCase("NNP") == true || wordPOS[posi].equalsIgnoreCase("NNS") == true)) {
                                for (i = 0; i < p; i++) {
                                    if (depMatrix[i][1].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj") || depMatrix[i][0].equalsIgnoreCase("dep"))) {
                                        String tword = depMatrix[i][2];
                                        for (int loopi = 0; loopi < word.length; loopi++) {
                                            if (tword.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("JJ") || wordPOS[loopi].equalsIgnoreCase("JJR") || wordPOS[loopi].equalsIgnoreCase("JJS"))) {
                                                if (opinionToPolarityMap2.containsKey(tword)) {
                                                } else {

                                                    opinionToPolarityMap2.put(tword, reviewPolarity);//detect latter
                                                }


                                                String annt = new String();
                                                annt = reviewIndex + "@" + tword + "@" + Integer.toString(reviewPolarity);
                                                if (annote.contains(annt)) {

                                                } else {
                                                    annote.add(annt);
                                                }
                                            }

                                        }

                                    }
                                    if (depMatrix[i][2].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj") || depMatrix[i][0].equalsIgnoreCase("dep"))) {
                                        String tword = depMatrix[i][1];
                                        for (int loopi = 0; loopi < word.length; loopi++) {
                                            if (tword.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("JJ") || wordPOS[loopi].equalsIgnoreCase("JJR") || wordPOS[loopi].equalsIgnoreCase("JJS"))) {
                                                if (opinionToPolarityMap2.containsKey(tword)) {
										    	  /*Integer counter = ((Integer)opinionToPolarityMap2.get(tword));
										          opinionToPolarityMap2.put(tword, new Integer(counter +1));*/
                                                } else {

                                                    //Integer value =(Integer) opinionToPolarityMap1.get(name);
                                                    opinionToPolarityMap2.put(tword, reviewPolarity);
                                                }
                                                String annt = new String();
                                                annt = reviewIndex + "@" + tword + "@" + Integer.toString(reviewPolarity);
                                                if (annote.contains(annt)) {

                                                } else {
                                                    annote.add(annt);
                                                }
                                            }

                                        }

                                    }
                                }
                            }
                        }


                    }

                    //Rule 2.2
                    for (String name : featureToCountMap1.keySet()) {
                        String key = name.toString();
                        // System.out.println(op[0]);
                        for (int posi = 0; posi < word.length; posi++) {
                            // System.out.println(word[posi]);
                            if (key.equalsIgnoreCase(word[posi]) == true && (wordPOS[posi].equalsIgnoreCase("NN") == true || wordPOS[posi].equalsIgnoreCase("NNP") == true || wordPOS[posi].equalsIgnoreCase("NNS") == true)) {
                                for (i = 0; i < p; i++) {
                                    if (depMatrix[i][1].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj") || depMatrix[i][0].equalsIgnoreCase("dep"))) {
                                        String tword = depMatrix[i][2];

                                        for (int j = 0; j < p; j++) {
                                            if (depMatrix[j][1].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj") || depMatrix[i][0].equalsIgnoreCase("dep"))) {
                                                String tword2 = depMatrix[j][2];

                                                for (int loopi = 0; loopi < word.length; loopi++) {
                                                    if (tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("JJ") || wordPOS[loopi].equalsIgnoreCase("JJR") || wordPOS[loopi].equalsIgnoreCase("JJS"))) {
                                                        if (opinionToPolarityMap2.containsKey(tword2)) {
										    	  /*Integer counter = ((Integer)opinionToPolarityMap2.get(tword2));
										          opinionToPolarityMap2.put(tword2, new Integer(counter +1));*/
                                                        } else {
                                                            opinionToPolarityMap2.put(tword2, reviewPolarity);
                                                        }
                                                        String annt = new String();
                                                        annt = reviewIndex + "@" + tword2 + "@" + Integer.toString(reviewPolarity);
                                                        if (annote.contains(annt)) {

                                                        } else {
                                                            annote.add(annt);
                                                        }
                                                    }

                                                }

                                            }
                                            if (depMatrix[j][2].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj") || depMatrix[i][0].equalsIgnoreCase("dep"))) {
                                                String tword2 = depMatrix[j][1];

                                                for (int loopi = 0; loopi < word.length; loopi++) {
                                                    if (tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("JJ") || wordPOS[loopi].equalsIgnoreCase("JJR") || wordPOS[loopi].equalsIgnoreCase("JJS"))) {
                                                        if (opinionToPolarityMap2.containsKey(tword2)) {
										    	  /*Integer counter = ((Integer)opinionToPolarityMap2.get(tword2));
										          opinionToPolarityMap2.put(tword2, new Integer(counter +1));*/
                                                        } else {
                                                            opinionToPolarityMap2.put(tword2, reviewPolarity);
                                                        }
                                                        String annt = new String();
                                                        annt = reviewIndex + "@" + tword2 + "@" + Integer.toString(reviewPolarity);
                                                        if (annote.contains(annt)) {

                                                        } else {
                                                            annote.add(annt);
                                                        }
                                                    }

                                                }

                                            }

                                        }
                                    }
                                    if (depMatrix[i][2].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj") || depMatrix[i][0].equalsIgnoreCase("dep"))) {
                                        String tword = depMatrix[i][1];///

                                        for (int j = 0; j < p; j++) {
                                            if (depMatrix[j][1].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj") || depMatrix[i][0].equalsIgnoreCase("dep"))) {
                                                String tword2 = depMatrix[j][2];

                                                for (int loopi = 0; loopi < word.length; loopi++) {
                                                    if (tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("JJ") || wordPOS[loopi].equalsIgnoreCase("JJR") || wordPOS[loopi].equalsIgnoreCase("JJS"))) {
                                                        if (opinionToPolarityMap2.containsKey(tword2)) {
										    	 /* Integer counter = ((Integer)opinionToPolarityMap2.get(tword2));
										          opinionToPolarityMap2.put(tword2, new Integer(counter +1));*/
                                                        } else {
                                                            opinionToPolarityMap2.put(tword2, reviewPolarity);
                                                        }
                                                        String annt = new String();
                                                        annt = reviewIndex + "@" + tword2 + "@" + Integer.toString(reviewPolarity);
                                                        if (annote.contains(annt)) {

                                                        } else {
                                                            annote.add(annt);
                                                        }
                                                    }

                                                }

                                            }
                                            if (depMatrix[j][2].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj") || depMatrix[i][0].equalsIgnoreCase("dep"))) {
                                                String tword2 = depMatrix[j][1];

                                                for (int loopi = 0; loopi < word.length; loopi++) {
                                                    if (tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("JJ") || wordPOS[loopi].equalsIgnoreCase("JJR") || wordPOS[loopi].equalsIgnoreCase("JJS"))) {
                                                        if (opinionToPolarityMap2.containsKey(tword2)) {
										    	  /*Integer counter = ((Integer)opinionToPolarityMap2.get(tword2));
										          opinionToPolarityMap2.put(tword2, new Integer(counter +1));*/
                                                        } else {
                                                            opinionToPolarityMap2.put(tword2, reviewPolarity);
                                                        }
                                                        String annt = new String();
                                                        annt = reviewIndex + "@" + tword2 + "@" + Integer.toString(reviewPolarity);
                                                        if (annote.contains(annt)) {

                                                        } else {
                                                            annote.add(annt);
                                                        }
                                                    }

                                                }

                                            }

                                        }
                                    }


                                }


                            }

                        }


                    }


                    for (String name : opinionToPolarityMap2.keySet()) {
                        String key = name.toString();
                        Integer value = (Integer) opinionToPolarityMap2.get(name);
                        if (opinionToPolarityMap1.containsKey(key)) {
					    	  /*Integer counter = ((Integer)opinionToPolarityMap1.get(key));
					          opinionToPolarityMap1.put(key, new Integer(counter + value));*/
                        } else {
                            opinionToPolarityMap1.put(key, value);
                            flag6 = 1;
                        }
                    }

                    opinionToPolarityMap2 = new HashMap<String, Integer>();
                    //end of Rule 2.2


                    //break;//sentence break at single review


                }


                //break;//review break

            }
            epoch++;
        } while (flag1 == 1 || flag2 == 1 || flag3 == 1 || flag4 == 1 || flag5 == 1 || flag6 == 1);
        ///
        int gg = 0;
        String content;
        //File file = new File("E:\\work\\Deepanshu\\Video_Game_Output/initialFeatures.txt");
        File file = new File("../output/initialFeatures.txt");
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        for (String name : featureToCountMap1.keySet()) {

            String key = name.toString();
            Integer value = (Integer) featureToCountMap1.get(name);
            //System.out.println(key + ":" + value);
            gg++;
            content = new String();
            content = key + ":" + value;
            bw.write(content + "\r\n");


        }
        bw.close();
        System.out.println(gg);

        //featureToCountMap1 freq

        HashMap<String, Integer> featureFreq = new HashMap<String, Integer>();
        //String str="E:\\work\\Deepanshu\\nowInput\\nu/B000RK3BO0.txt";
        BufferedReader in = new BufferedReader(new FileReader(reviewsFilePath));
        //start Preprocessing
        String sCurrentLine;
        int i = 0;
        int reviewid = 0;
        while ((sCurrentLine = in.readLine()) != null) {

            int val = 0;

            reviewid++;
            String overall = sCurrentLine.substring(sCurrentLine.indexOf(',') + 1, sCurrentLine.indexOf(',', sCurrentLine.indexOf(',') + 1));
            String reviewText = sCurrentLine.substring(sCurrentLine.indexOf(',', sCurrentLine.indexOf(',') + 1) + 1);
            int score = Integer.parseInt(overall.substring(0, overall.indexOf('.')));
            if (score >= 3) {
                val = 1;
            } else {
                val = -1;
            }
            String processedreviewText = new String();
            ;
            //remove multiple dots
            for (i = 0; i < reviewText.length() - 1; i++) {
                if (reviewText.charAt(i) == '.' && reviewText.charAt(i + 1) == '.') {
                    continue;
                } else {
                    processedreviewText += reviewText.charAt(i);
                }

            }
            processedreviewText += reviewText.charAt(i);
            processedreviewText = processedreviewText.toLowerCase();////text after removing multiple dots + all lower case
            processedreviewText = processedreviewText.replace(":o)", " ");
            processedreviewText = processedreviewText.replace(":-)", " ");
            processedreviewText = processedreviewText.replace(";-)", " ");
            processedreviewText = processedreviewText.replace('_', ' ');
            processedreviewText = processedreviewText.replace('-', ' ');

            System.out.println("New=" + processedreviewText);
            //split each review into its constituent sentences
            Reader reader = new StringReader(processedreviewText);
            DocumentPreprocessor dp = new DocumentPreprocessor(reader);
            ArrayList<String> sentenceList = new ArrayList<String>();

            for (java.util.List<HasWord> sentence : dp) {
                String sentenceString = Sentence.listToString(sentence);
                sentenceList.add(sentenceString.toString());
            }
            //for each sentence---1)Word tokenization 2)POS Tag 3) wordPOS array
            for (String sentence : sentenceList) {
                System.out.println("Review Sentence=" + sentence);
                String word[] = sentence.split(" ");    //1)Word tokenization
                String wordPOS[] = new String[word.length];
                //Tag the sentence
                MaxentTagger tagger = new MaxentTagger("tagger/english-bidirectional-distsim.tagger");
                String tagged = tagger.tagString(sentence);
                int flag = 0;
                int count = 0;

                //Calculate wordPOS array
                for (i = 0; i < tagged.length(); i++) {
                    if (tagged.charAt(i) == '_' && flag == 0) {
                        flag = 1;
                        wordPOS[count] = Character.toString(tagged.charAt(i + 1));
                        i++;
                    } else if (tagged.charAt(i) != ' ' && flag == 1) {
                        wordPOS[count] += tagged.charAt(i);
                    } else if (tagged.charAt(i) == ' ' && flag == 1) {
                        flag = 0;
                        count++;
                    } else {
                        continue;
                    }
                }
                for (int var = 0; var < word.length - 2; var++) {
                    String newFeatureWord = new String();
                    if (wordPOS[var].equalsIgnoreCase("JJ") || wordPOS[var].equalsIgnoreCase("JJR") || wordPOS[var].equalsIgnoreCase("JJS")) {
                        int limit1 = Math.min(var + 1, word.length - 1);
                        int limit2 = Math.min(var + 2, word.length - 1);
                        if (wordPOS[limit1].equalsIgnoreCase("NN") || wordPOS[limit1].equalsIgnoreCase("NNS") || wordPOS[limit1].equalsIgnoreCase("NNP")) {
                            newFeatureWord = word[limit1];
                            if (opinionToPolarityMap1.containsKey(word[var])) {
                                val = (Integer) opinionToPolarityMap1.get(word[var]);
                            }
                            int newnegPol = 1;
                            for (int PolVar = Math.max(0, var - 2); PolVar < Math.min(word.length, var + 2); PolVar++)
                                if (word[PolVar].equalsIgnoreCase("not") || word[PolVar].equalsIgnoreCase("n't") || word[PolVar].equalsIgnoreCase("'t") || word[PolVar].equalsIgnoreCase("however") || word[PolVar].equalsIgnoreCase("but") || word[PolVar].equalsIgnoreCase("despite") || word[PolVar].equalsIgnoreCase("though") || word[PolVar].equalsIgnoreCase("except") || word[PolVar].equalsIgnoreCase("although") || word[PolVar].equalsIgnoreCase("oddly")) {
                                    newnegPol = -1;
                                }
                            val *= newnegPol;
                            if (wordPOS[limit2].equalsIgnoreCase("NN") || wordPOS[limit2].equalsIgnoreCase("NNS") || wordPOS[limit2].equalsIgnoreCase("NNP")) {
                                newFeatureWord = newFeatureWord + " " + word[limit2];
                            }
                            if (featureToCountMap1.containsKey(newFeatureWord)) {
                                Integer counter = ((Integer) featureToCountMap1.get(newFeatureWord));
                                featureToCountMap1.put(newFeatureWord, new Integer(counter + 1));
                            } else {
                                featureToCountMap1.put(newFeatureWord, 1);
                            }
                            String annt = new String();
                            annt = reviewid + "@" + newFeatureWord + "@" + Integer.toString(val);
                            System.out.println(annt);
                            if (annote.contains(annt)) {

                            } else {
                                annote.add(annt);
                            }


                        }
                    }


                }

                for (String name : featureToCountMap1.keySet()) {
                    String key = name.toString();
                    if (key.indexOf(' ') >= 0) {
                        if (sentence.toLowerCase().contains(key.toLowerCase())) {
                            if (featureFreq.containsKey(key)) {
                                Integer counter = ((Integer) featureFreq.get(key));
                                featureFreq.put(key, new Integer(counter + 1));
                            } else {
                                featureFreq.put(key, 1);
                            }

                        }
                    } else {
                        for (int j = 0; j < word.length; j++) {
                            if (word[j].equalsIgnoreCase(key)) {
                                if (featureFreq.containsKey(key)) {
                                    Integer counter = ((Integer) featureFreq.get(key));
                                    featureFreq.put(key, new Integer(counter + 1));
                                } else {
                                    featureFreq.put(key, 1);
                                    System.out.println("KEY=" + key);
                                }
                            }
                        }
                    }//else


                }
            }
        }

        gg = 0;
        // file = new File("E:\\work\\Deepanshu\\Video_Game_Output\\Freq_Feature.txt");
        file = new File("../output/Freq_Feature.txt");
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }
        fw = new FileWriter(file.getAbsoluteFile());
        bw = new BufferedWriter(fw);
        for (String name : featureFreq.keySet()) {

            String key = name.toString();
            Integer value = (Integer) featureFreq.get(name);
            //System.out.println(key + ":" + value);
            gg++;
            content = new String();
            content = key + ":" + value;
            bw.write(content + "\r\n");


        }
        bw.close();
        System.out.println(gg);


        gg = 0;
        //file = new File("E:\\work\\Deepanshu\\Video_Game_Output\\opine3.txt");
        file = new File("../output/opine3.txt");
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }
        fw = new FileWriter(file.getAbsoluteFile());
        bw = new BufferedWriter(fw);
        for (String name : opinionToPolarityMap1.keySet()) {


            String key = name.toString();
            Integer value = (Integer) opinionToPolarityMap1.get(name);
            //System.out.println(key + ":" + value);
            content = new String();
            content = key + ":" + value;
            bw.write(content + "\r\n");

            gg++;

        }
        bw.close();
        System.out.println(gg);
        System.out.println(epoch);


        ArrayList<String> finalFeature = new ArrayList<String>();


        for (String name : featureFreq.keySet()) {
            String key = name.toString();
            Integer value = (Integer) featureFreq.get(name);
            if (!key.contains(".") && !key.contains("?") && !key.contains("!")) {
                if (value >= 0) {
                    finalFeature.add(key);
                }

            }


        }


        // str="E:\\work\\Deepanshu\\Video_Game_Output\\FinalFeature.txt";
        String str = "../output/FinalFeature.txt";
        FileWriter fr = new FileWriter(str);
        BufferedWriter out = new BufferedWriter(fr);
        i = 0;
        String newFeature;
        while (i < finalFeature.size()) {
            newFeature = finalFeature.get(i);
            out.write(newFeature + "\r\n");
            i++;
        }

        out.close();

        ArrayList<String> annote1 = new ArrayList<String>();
        for (String name : annote) {
            int flag = 0;
            for (String name1 : finalFeature) {
                if (name.toLowerCase().contains(name1.toLowerCase())) {
                    flag = 1;
                    break;
                }
            }
            if (flag == 1) {
                annote1.add(name);
            }
        }


        //String str5="E:\\work\\Deepanshu\\Video_Game_Output\\annote.txt";
        String str5 = "../output/annote.txt";
        // if file doesnt exists, then create it

        fw = new FileWriter(str5);
        bw = new BufferedWriter(fw);
        for (String name : annote1) {
            System.out.println(name.toString());
            bw.write(name.toString() + "\r\n");
        }
        bw.close();


    }

    /**
     * 1. Remove multiple dots
     * 2. Convert to lowerCase
     * 3. Remove emoticons, hyphen, underscore
     */
    private String getProcessedReviewText(int reviewIndex, String reviewText) {
        logger.info("Request to process review text, reviewIndex :{}, reviewText: {}", reviewIndex, reviewText);
        String processedReviewText = "";

        // Remove multiple dots
        int i;
        for (i = 0; i < reviewText.length() - 1; i++) {
            if (reviewText.charAt(i) == '.' && reviewText.charAt(i + 1) == '.') {
                continue;
            } else {
                processedReviewText += reviewText.charAt(i);
            }

        }
        processedReviewText += reviewText.charAt(i);

        // Convert to lowerCase
        processedReviewText = processedReviewText.toLowerCase();

        // Remove emoticons, hyphen, underscore
        processedReviewText = processedReviewText.replace(":o)", " ");
        processedReviewText = processedReviewText.replace(":-)", " ");
        processedReviewText = processedReviewText.replace(";-)", " ");
        processedReviewText = processedReviewText.replace('_', ' ');
        processedReviewText = processedReviewText.replace('-', ' ');

        logger.info("Returning processedReviewText :{}, reviewIndex: {}, reviewText: {}", processedReviewText, reviewIndex, reviewText);
        return processedReviewText;
    }

    private String getReviewText(String line, int reviewIndex) {
        String text = line.substring(line.indexOf(',', line.indexOf(',') + 1) + 1);
        logger.info("ReviewIndex: {}, text: {}", reviewIndex, text);

        return text;
    }

    private int getReviewPolarity(int reviewIndex, String line) {
        int polarity;
        String ratingString = line.substring(line.indexOf(',') + 1, line.indexOf(',', line.indexOf(',') + 1));
        int rating = Integer.parseInt(ratingString.substring(0, ratingString.indexOf('.')));

        if (rating >= 3) {
            polarity = 1;
        } else {
            polarity = -1;
        }
        logger.info("ReviewIndex: {}, rating: {}", reviewIndex, rating);
        return polarity;
    }

    private void resetFlags() {
        flag1 = 0;
        flag2 = 0;
        flag3 = 0;
        flag4 = 0;
        flag5 = 0;
        flag6 = 0;
    }

    private void updateOpinions(String opinionFilePath, HashMap<String, Integer> opinionToPolarityMap, int polarity) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(opinionFilePath));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            line = line.toLowerCase();
            opinionToPolarityMap.put(line, polarity);
        }
        bufferedReader.close();
    }

}

