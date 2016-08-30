package com.main;


import com.support.*;

import java.awt.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;



public class ProductaspectExtraction {
	
		public static void main(String args[]) throws IOException{
			
			int negPol=1;
			//String parserModel = "E:\\work\\workspace1\\ReviewAspectExtraction\\depParser\\englishPCFG.ser";
			String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
			 LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);
			 
			 DependencyParserImp ob=new DependencyParserImp(); //Object created for Dependency Parser class.
		
			//opinion dictionary->array List whose each element is an array...each array's 1st cell is opinion word.2nd cell is its polarity 
			 
			//String opWord[]=new String[2];
			HashMap<String,Integer> opDict = new HashMap<String,Integer>();
			HashMap<String,Integer> opDict1 = new HashMap<String,Integer>();
			
			//String path="E:\\work\\Deepanshu\\positive.txt"; //Set the path for "postive.txt" file for initial positive opinion words
			String path=args[0];
			BufferedReader inp= new BufferedReader(new FileReader(path));
			
			String sCurrentLine1;
			while((sCurrentLine1 = inp.readLine()) != null){
				sCurrentLine1=sCurrentLine1.trim();
				sCurrentLine1=sCurrentLine1.toLowerCase();
				opDict.put(sCurrentLine1,1);
			}
			//path="E:\\work\\Deepanshu\\negative.txt"; //Set the path for "negative.txt" file for initial negative opinion words
			path=args[1];
			BufferedReader inp1= new BufferedReader(new FileReader(path));
			
			String sCurrentLine2;
			while((sCurrentLine2 = inp1.readLine()) != null){
				sCurrentLine2=sCurrentLine2.trim();
				sCurrentLine2=sCurrentLine2.toLowerCase();
				opDict.put(sCurrentLine2,-1);
			}
			HashMap<String,Integer> feature=new HashMap<String,Integer>();
			HashMap<String,Integer> feature1=new HashMap<String,Integer>();
			ArrayList<String> annote=new ArrayList<String>();
			int chkflag1=0,chkflag2=0,chkflag3=0,chkflag4=0,chkflag5=0,chkflag6=0,chkflag7=0,chkflag8=0;
			int loopiter=0;
			//take input form a particular file
	do{
		chkflag1=0;chkflag2=0;chkflag3=0;chkflag4=0;chkflag5=0;chkflag6=0;chkflag7=0;chkflag8=0;	
		feature1=new HashMap<String,Integer>();
		opDict1=new HashMap<String,Integer>();
		//String str="E:\\work\\Deepanshu\\nowInput\\sample.txt";  //Set the path for correctly formatted review data file. 
		String str=args[2];
		BufferedReader in= new BufferedReader(new FileReader(str));
		//start Preprocessing
		String sCurrentLine;
		int i=0;
		
		int reviewid=0;
		while((sCurrentLine = in.readLine()) != null){
			reviewid++;
			System.out.println(reviewid);
			String overall=sCurrentLine.substring(sCurrentLine.indexOf(',')+1, sCurrentLine.indexOf(',', sCurrentLine.indexOf(',')+1));
			int score=Integer.parseInt(overall.substring(0, overall.indexOf('.')));
			int POLVAL=0;
			if(score>=3)
				POLVAL=1;
			else
				POLVAL=-1;
			System.out.println("OVERALL="+score+"END");
			String reviewText=sCurrentLine.substring(sCurrentLine.indexOf(',', sCurrentLine.indexOf(',')+1)+1);
			System.out.println(overall+"%"+reviewText);
			String processedreviewText=new String();;
			//remove multiple dots
			for(i=0;i<reviewText.length()-1;i++){
				if(reviewText.charAt(i)=='.' && reviewText.charAt(i+1)=='.')
					continue;
					else
					processedreviewText+=reviewText.charAt(i);
					
			}
			processedreviewText+=reviewText.charAt(i);
			processedreviewText=processedreviewText.toLowerCase();////text after removing multiple dots + all lower case
			processedreviewText=processedreviewText.replace(":o)", " ");processedreviewText=processedreviewText.replace(":-)", " ");
			processedreviewText=processedreviewText.replace(";-)", " ");
			processedreviewText=processedreviewText.replace('_',' ');
			
			System.out.println("New="+processedreviewText);
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
			   System.out.println("Review Sentence="+sentence);
			   String word[]=sentence.split(" ");    //1)Word tokenization
			   String wordPOS[]=new String[word.length];
			   System.out.println(wordPOS.length);
			  for(i=0;i<word.length;i++)
			  System.out.print(word[i]+" ");
			    
			  //Tag the sentence
				 MaxentTagger tagger = new MaxentTagger("tagger/english-bidirectional-distsim.tagger");//
			     String tagged = tagger.tagString(sentence);
			     System.out.println(tagged);
			     int flag=0;int count=0;
			    
			     //Calculate wordPOS array
			     for(i=0;i<tagged.length();i++){
			    	 if(tagged.charAt(i)=='_' && flag==0){
			    		 flag=1;
			    		 wordPOS[count]=Character.toString(tagged.charAt(i+1));i++;
			    	 }
			    	 else if(tagged.charAt(i)!=' ' && flag==1)
			    		 wordPOS[count]+=tagged.charAt(i);
			    	 else if(tagged.charAt(i)==' ' && flag==1){
			    		 flag=0;
			    		 count++;
			    		 }
			    	 else
			    	 continue;
			     }
			     for(i=0;i<word.length;i++)
			    	 System.out.print(wordPOS[i]+" ");
			     
			     //Dependency Parsing
			     String dep=ob.demoAPI(lp,word);
				  System.out.println("DEP="+dep);
				  
				  int spacecount=0;
				  for(i=1;i<dep.length()-1;i++){
					  if(dep.charAt(i)==' ')
						  spacecount++;
				  }
				  spacecount=(int)(spacecount/2)+1;
				  System.out.println(spacecount);
				String depMatrix[][]=new String[spacecount][3]; flag=0;int p=0;
				String temp="";String temp1="";String temp3="";
				  for(i=1;i<dep.length()-1;i++){
					  if(dep.charAt(i)!='(' && flag==0)
						  temp3+=dep.charAt(i);
					  else if(dep.charAt(i)=='(' && flag==0)
						  flag=1;
					  else if(dep.charAt(i)!=',' && flag==1)
						  temp+=dep.charAt(i);
					  else if(dep.charAt(i)==',' && flag==1)
						  flag=2;
					  else if(dep.charAt(i)!=')' && flag==2)
						  temp1+=dep.charAt(i);
					  else if(dep.charAt(i)==')' && flag==2){
						  i+=2;flag=0;
						 // System.out.println(temp3+"$"+temp.trim()+"$"+temp1.trim());
						  //take the words
						  depMatrix[p][0]=temp3.trim();
						 depMatrix[p][1]=temp.substring(0, temp.lastIndexOf('-')).trim();
						 depMatrix[p++][2]=temp1.substring(0, temp1.lastIndexOf('-')).trim();
						  temp="";temp1="";temp3="";
					  }
					  //else
						//  continue;
				}
				  for(i=0;i<p;i++)
				  System.out.println(depMatrix[i][0]+" "+depMatrix[i][1]+" "+depMatrix[i][2]+" ");
				  
		//Rule R1.1		  
				 
				  for (String name: opDict.keySet()){
					  String key =name.toString();
					 // System.out.println(op[0]);
					  for(int posi=0;posi<word.length;posi++){
						 // System.out.println(word[posi]);
						  if(key.equalsIgnoreCase(word[posi])==true && (wordPOS[posi].equalsIgnoreCase("JJ")==true ||wordPOS[posi].equalsIgnoreCase("JJR")==true || wordPOS[posi].equalsIgnoreCase("JJS")==true)){
							  for(i=0;i<p;i++){
								  if(depMatrix[i][1].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj"))){
									  String tword=depMatrix[i][2];
									  for(int loopi=1;loopi<word.length-1;loopi++){
										  if(tword.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS")|| wordPOS[loopi].equalsIgnoreCase("NNP"))){
											  if(wordPOS[loopi-1].equalsIgnoreCase("NN") || wordPOS[loopi-1].equalsIgnoreCase("NNS")|| wordPOS[loopi-1].equalsIgnoreCase("NNP")){
									    		  tword=word[loopi-1]+" "+tword;
									    	  }
											  if(wordPOS[loopi+1].equalsIgnoreCase("NN") || wordPOS[loopi+1].equalsIgnoreCase("NNS")|| wordPOS[loopi+1].equalsIgnoreCase("NNP")){
									    		  tword=tword+" "+word[loopi+1];
									    	  }
											  if(feature.containsKey(tword)){
										    	  Integer counter = ((Integer)feature.get(tword));
										          feature.put(tword, new Integer(counter +1));
										      }
										      else{
										    	  feature.put(tword, 1);chkflag1=1;
										      }
											  negPol=1;
											  for(int PolVar=Math.max(0, posi-2);PolVar<Math.min(word.length, posi+2);PolVar++)
												  if(word[PolVar].equalsIgnoreCase("not")||word[PolVar].equalsIgnoreCase("n't")||word[PolVar].equalsIgnoreCase("'t")||word[PolVar].equalsIgnoreCase("however")||word[PolVar].equalsIgnoreCase("but")||word[PolVar].equalsIgnoreCase("despite")||word[PolVar].equalsIgnoreCase("though")||word[PolVar].equalsIgnoreCase("except")||word[PolVar].equalsIgnoreCase("although")||word[PolVar].equalsIgnoreCase("oddly"))
													  	negPol=-1;
											  
											 Integer val=negPol*((Integer)opDict.get(key));
											  String annt=new String();
											  annt=reviewid + "@"+tword+"@"+Integer.toString(val);
											  if(annote.contains(annt)){
												  
											  }else
												  annote.add(annt);
											  
										  }
											  
									  }
										  
								  }
								  if(depMatrix[i][2].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj"))){
									  String tword=depMatrix[i][1];
									  for(int loopi=1;loopi<word.length-1;loopi++){
										  if(tword.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS")|| wordPOS[loopi].equalsIgnoreCase("NNP"))){
											  if(wordPOS[loopi-1].equalsIgnoreCase("NN") || wordPOS[loopi-1].equalsIgnoreCase("NNS")|| wordPOS[loopi-1].equalsIgnoreCase("NNP")){
									    		  tword=word[loopi-1]+" "+tword;
									    	  }
											  if(wordPOS[loopi+1].equalsIgnoreCase("NN") || wordPOS[loopi+1].equalsIgnoreCase("NNS")|| wordPOS[loopi+1].equalsIgnoreCase("NNP")){
									    		  tword=tword+" "+word[loopi+1];
									    	  }
											  if(feature.containsKey(tword)){
										    	  Integer counter = ((Integer)feature.get(tword));
										          feature.put(tword, new Integer(counter +1));
										      }
										      else{
										    	  feature.put(tword, 1);chkflag1=1;
										      }
											  
											  negPol=1;
											  for(int PolVar=Math.max(0, posi-2);PolVar<Math.min(word.length, posi+2);PolVar++)
												  if(word[PolVar].equalsIgnoreCase("not")||word[PolVar].equalsIgnoreCase("n't")||word[PolVar].equalsIgnoreCase("'t")||word[PolVar].equalsIgnoreCase("however")||word[PolVar].equalsIgnoreCase("but")||word[PolVar].equalsIgnoreCase("despite")||word[PolVar].equalsIgnoreCase("though")||word[PolVar].equalsIgnoreCase("except")||word[PolVar].equalsIgnoreCase("although")||word[PolVar].equalsIgnoreCase("oddly"))
													  	negPol=-1;
											  
											  
											  Integer val=negPol*((Integer)opDict.get(key));
											  String annt=new String();
											  annt=reviewid + "@"+tword+"@"+Integer.toString(val);
											  if(annote.contains(annt)){
												  
											  }else
												  annote.add(annt);
										  }
											  
									  }
										  
								  }
							  }
						  }
					  }
					  
					  
				  }
				  
		//Rule R1.2
				  
				  for (String name: opDict.keySet()){
					  String key =name.toString();
					 // System.out.println(op[0]);
					  for(int posi=0;posi<word.length;posi++){
						 // System.out.println(word[posi]);
						  if(key.equalsIgnoreCase(word[posi])==true && (wordPOS[posi].equalsIgnoreCase("JJ")==true ||wordPOS[posi].equalsIgnoreCase("JJR")==true || wordPOS[posi].equalsIgnoreCase("JJS")==true)){
							  for(i=0;i<p;i++){
								  if(depMatrix[i][1].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj"))){
									  String tword=depMatrix[i][2];
									 
									  for(int j=0;j<p;j++){
										  if(depMatrix[j][1].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj"))){
											  String tword2=depMatrix[j][2];
									 
									  for(int loopi=1;loopi<word.length-1;loopi++){
										  if(tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS")|| wordPOS[loopi].equalsIgnoreCase("NNP"))){
											  if(wordPOS[loopi-1].equalsIgnoreCase("NN") || wordPOS[loopi-1].equalsIgnoreCase("NNS")|| wordPOS[loopi-1].equalsIgnoreCase("NNP")){
									    		  tword2=word[loopi-1]+" "+tword2;
									    	  }
											  if(wordPOS[loopi+1].equalsIgnoreCase("NN") || wordPOS[loopi+1].equalsIgnoreCase("NNS")|| wordPOS[loopi+1].equalsIgnoreCase("NNP")){
									    		  tword2=tword2+" "+word[loopi+1];
									    	  }
											  if(feature.containsKey(tword2)){
										    	  Integer counter = ((Integer)feature.get(tword2));
										          feature.put(tword2, new Integer(counter +1));
										      }
										      else{
										    	  feature.put(tword2, 1);chkflag2=1;}
											  negPol=1;
											  for(int PolVar=Math.max(0, posi-2);PolVar<Math.min(word.length, posi+2);PolVar++)
												  if(word[PolVar].equalsIgnoreCase("not")||word[PolVar].equalsIgnoreCase("n't")||word[PolVar].equalsIgnoreCase("'t")||word[PolVar].equalsIgnoreCase("however")||word[PolVar].equalsIgnoreCase("but")||word[PolVar].equalsIgnoreCase("despite")||word[PolVar].equalsIgnoreCase("though")||word[PolVar].equalsIgnoreCase("except")||word[PolVar].equalsIgnoreCase("although")||word[PolVar].equalsIgnoreCase("oddly"))
													  	negPol=-1;
											  Integer val=negPol*((Integer)opDict.get(key));
											  String annt=new String();
											  annt=reviewid + "@"+tword2+"@"+Integer.toString(val);
											  if(annote.contains(annt)){
												  
											  }else
												  annote.add(annt);
										  }
											  
									  }
										  
								  }
										  if(depMatrix[j][2].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj"))){
											  String tword2=depMatrix[j][1];
									 
									  for(int loopi=1;loopi<word.length-1;loopi++){
										  if(tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS")|| wordPOS[loopi].equalsIgnoreCase("NNP"))){
											  if(wordPOS[loopi-1].equalsIgnoreCase("NN") || wordPOS[loopi-1].equalsIgnoreCase("NNS")|| wordPOS[loopi-1].equalsIgnoreCase("NNP")){
									    		  tword2=word[loopi-1]+" "+tword2;
									    	  }
											  if(wordPOS[loopi+1].equalsIgnoreCase("NN") || wordPOS[loopi+1].equalsIgnoreCase("NNS")|| wordPOS[loopi+1].equalsIgnoreCase("NNP")){
									    		  tword2=tword2+" "+word[loopi+1];
									    	  }
											  if(feature.containsKey(tword2)){
										    	  Integer counter = ((Integer)feature.get(tword2));
										          feature.put(tword2, new Integer(counter +1));
										      }
										      else{
										    	  feature.put(tword2, 1);chkflag2=1;}
											  negPol=1;
											  for(int PolVar=Math.max(0, posi-2);PolVar<Math.min(word.length, posi+2);PolVar++)
												  if(word[PolVar].equalsIgnoreCase("not")||word[PolVar].equalsIgnoreCase("n't")||word[PolVar].equalsIgnoreCase("'t")||word[PolVar].equalsIgnoreCase("however")||word[PolVar].equalsIgnoreCase("but")||word[PolVar].equalsIgnoreCase("despite")||word[PolVar].equalsIgnoreCase("though")||word[PolVar].equalsIgnoreCase("except")||word[PolVar].equalsIgnoreCase("although")||word[PolVar].equalsIgnoreCase("oddly"))
													  	negPol=-1;
											  Integer val=negPol*((Integer)opDict.get(key));
											  String annt=new String();
											  annt=reviewid + "@"+tword2+"@"+Integer.toString(val);
											  if(annote.contains(annt)){
												  
											  }else
												  annote.add(annt);
										  }
											  
									  }
										  
								  }
								
							  }
						  }
								  if(depMatrix[i][2].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj"))){
									  String tword=depMatrix[i][1];///
									 
									  for(int j=0;j<p;j++){
										  if(depMatrix[j][1].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj"))){
											  String tword2=depMatrix[j][2];
									 
									  for(int loopi=1;loopi<word.length-1;loopi++){
										  if(tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS")|| wordPOS[loopi].equalsIgnoreCase("NNP"))){
											  if(wordPOS[loopi-1].equalsIgnoreCase("NN") || wordPOS[loopi-1].equalsIgnoreCase("NNS")|| wordPOS[loopi-1].equalsIgnoreCase("NNP")){
									    		  tword2=word[loopi-1]+" "+tword2;
									    	  }
											  if(wordPOS[loopi+1].equalsIgnoreCase("NN") || wordPOS[loopi+1].equalsIgnoreCase("NNS")|| wordPOS[loopi+1].equalsIgnoreCase("NNP")){
									    		  tword2=tword2+" "+word[loopi+1];
									    	  }
											  if(feature.containsKey(tword2)){
										    	  Integer counter = ((Integer)feature.get(tword2));
										          feature.put(tword2, new Integer(counter +1));
										      }
										      else{
										    	  feature.put(tword2, 1);chkflag2=1;}
											  negPol=1;
											  for(int PolVar=Math.max(0, posi-2);PolVar<Math.min(word.length, posi+2);PolVar++)
												  if(word[PolVar].equalsIgnoreCase("not")||word[PolVar].equalsIgnoreCase("n't")||word[PolVar].equalsIgnoreCase("'t")||word[PolVar].equalsIgnoreCase("however")||word[PolVar].equalsIgnoreCase("but")||word[PolVar].equalsIgnoreCase("despite")||word[PolVar].equalsIgnoreCase("though")||word[PolVar].equalsIgnoreCase("except")||word[PolVar].equalsIgnoreCase("although")||word[PolVar].equalsIgnoreCase("oddly"))
													  	negPol=-1;
											  Integer val=negPol*((Integer)opDict.get(key));
											  String annt=new String();
											  annt=reviewid + "@"+tword2+"@"+Integer.toString(val);
											  if(annote.contains(annt)){
												  
											  }else
												  annote.add(annt);
										  }
											  
									  }
										  
								  }
										  if(depMatrix[j][2].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj"))){
											  String tword2=depMatrix[j][1];
									 
									  for(int loopi=1;loopi<word.length-1;loopi++){
										  if(tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS")|| wordPOS[loopi].equalsIgnoreCase("NNP"))){
											  if(wordPOS[loopi-1].equalsIgnoreCase("NN") || wordPOS[loopi-1].equalsIgnoreCase("NNS")|| wordPOS[loopi-1].equalsIgnoreCase("NNP")){
									    		  tword2=word[loopi-1]+" "+tword2;
									    	  }
											  if(wordPOS[loopi+1].equalsIgnoreCase("NN") || wordPOS[loopi+1].equalsIgnoreCase("NNS")|| wordPOS[loopi+1].equalsIgnoreCase("NNP")){
									    		  tword2=tword2+" "+word[loopi+1];
									    	  }
											  if(feature.containsKey(tword2)){
										    	  Integer counter = ((Integer)feature.get(tword2));
										          feature.put(tword2, new Integer(counter +1));
										      }
										      else{
										    	  feature.put(tword2, 1);chkflag2=1;}
											  negPol=1;
											  for(int PolVar=Math.max(0, posi-2);PolVar<Math.min(word.length, posi+2);PolVar++)
												  if(word[PolVar].equalsIgnoreCase("not")||word[PolVar].equalsIgnoreCase("n't")||word[PolVar].equalsIgnoreCase("'t")||word[PolVar].equalsIgnoreCase("however")||word[PolVar].equalsIgnoreCase("but")||word[PolVar].equalsIgnoreCase("despite")||word[PolVar].equalsIgnoreCase("though")||word[PolVar].equalsIgnoreCase("except")||word[PolVar].equalsIgnoreCase("although")||word[PolVar].equalsIgnoreCase("oddly"))
													  	negPol=-1;
											  Integer val=negPol*((Integer)opDict.get(key));
											  String annt=new String();
											  annt=reviewid + "@"+tword2+"@"+Integer.toString(val);
											  if(annote.contains(annt)){
												  
											  }else
												  annote.add(annt);
										  }
											  
									  }
										  
								  }
								
							  }
						  }
								  
								  
					  }
					  
					  
				  }
				  
					  }
				  
				  
					  }		 //////end of Rule 1.2 
				//Rule 4.1
				   for (String name: opDict.keySet()){
						  String key =name.toString();
					 for(int posi=0;posi<word.length;posi++){
						 if(key.equalsIgnoreCase(word[posi])==true && (wordPOS[posi].equalsIgnoreCase("JJ")==true ||wordPOS[posi].equalsIgnoreCase("JJR")==true || wordPOS[posi].equalsIgnoreCase("JJS")==true)){
							  for(i=0;i<p;i++){
								  if(depMatrix[i][1].equalsIgnoreCase(word[posi]) && depMatrix[i][0].toLowerCase().contains("conj") ){
									  String tword=depMatrix[i][2];
									  for(int loopi=0;loopi<word.length;loopi++){
										  if(tword.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("JJ") || wordPOS[loopi].equalsIgnoreCase("JJR")|| wordPOS[loopi].equalsIgnoreCase("JJS"))){
											  if(opDict1.containsKey(tword)){
										    	  /*Integer counter = ((Integer)opDict1.get(tword));
										          opDict1.put(tword, new Integer(counter +1));*/
										      }
										      else{
										    	  
										    	  Integer value =(Integer) opDict.get(name);
										    	  opDict1.put(tword, value);
										  }
										  }
											  
									  }
										  
								  }
								  if(depMatrix[i][2].equalsIgnoreCase(word[posi]) && depMatrix[i][0].toLowerCase().contains("conj") ){
									  String tword=depMatrix[i][1];
									  for(int loopi=0;loopi<word.length;loopi++){
										  if(tword.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("JJ") || wordPOS[loopi].equalsIgnoreCase("JJR")|| wordPOS[loopi].equalsIgnoreCase("JJS"))){
											  if(opDict1.containsKey(tword)){
										    	  /*Integer counter = ((Integer)opDict1.get(tword));
										          opDict1.put(tword, new Integer(counter +1));*/
										      }
else{
										    	  
										    	  Integer value =(Integer) opDict.get(name);
										    	  opDict1.put(tword, value);
										  }
										  }
											  
									  }
										  
								  }
							  }
						  }
					  }
					  
					  
				  }				  
				  
				   for (String name: opDict1.keySet()){
						  String key =name.toString(); 
						  Integer value =(Integer) opDict1.get(name);
						  if(opDict.containsKey(key)){
					    	  /*Integer counter = ((Integer)opDict.get(key));
					          opDict.put(key, new Integer(counter + value));*/
					      }
					      else{
					    	  opDict.put(key, value);chkflag3=1;}
				   }   
				   
				   opDict1=new HashMap<String,Integer>(); 
				   
///end of Rule 4.1		
				  
				//  f9.addAll(featureI);  
				 ////Rule 3.1
				   for (String name: feature.keySet()){
					  String key =name.toString();
						 for(int posi=0;posi<word.length;posi++){
							 if(key.equalsIgnoreCase(word[posi])==true && (wordPOS[posi].equalsIgnoreCase("NN")==true ||wordPOS[posi].equalsIgnoreCase("NNS")==true || wordPOS[posi].equalsIgnoreCase("NNP")==true)){
								  for(i=0;i<p;i++){
									  if(depMatrix[i][1].equalsIgnoreCase(word[posi]) && depMatrix[i][0].toLowerCase().contains("conj") ){
										  String tword=depMatrix[i][2];
										  for(int loopi=1;loopi<word.length-1;loopi++){
											  if(tword.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS")|| wordPOS[loopi].equalsIgnoreCase("NNP"))){
												  if(wordPOS[loopi-1].equalsIgnoreCase("NN") || wordPOS[loopi-1].equalsIgnoreCase("NNS")|| wordPOS[loopi-1].equalsIgnoreCase("NNP")){
										    		  tword=word[loopi-1]+" "+tword;
										    	  }
												  if(wordPOS[loopi+1].equalsIgnoreCase("NN") || wordPOS[loopi+1].equalsIgnoreCase("NNS")|| wordPOS[loopi+1].equalsIgnoreCase("NNP")){
										    		  tword=tword+" "+word[loopi+1];
										    	  }
												  if(feature1.containsKey(tword)){
											    	  Integer counter = ((Integer)feature1.get(tword));
											          feature1.put(tword, new Integer(counter +1));
											      }
											      else
											    	  feature1.put(tword, 1);
											  }
												  
										  }
											  
									  }
									  if(depMatrix[i][2].equalsIgnoreCase(word[posi]) && depMatrix[i][0].toLowerCase().contains("conj") ){
										  String tword=depMatrix[i][1];
										  for(int loopi=1;loopi<word.length-1;loopi++){
											  if(tword.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNP")|| wordPOS[loopi].equalsIgnoreCase("NNS"))){
												  if(wordPOS[loopi-1].equalsIgnoreCase("NN") || wordPOS[loopi-1].equalsIgnoreCase("NNS")|| wordPOS[loopi-1].equalsIgnoreCase("NNP")){
										    		  tword=word[loopi-1]+" "+tword;
										    	  }
												  if(wordPOS[loopi+1].equalsIgnoreCase("NN") || wordPOS[loopi+1].equalsIgnoreCase("NNS")|| wordPOS[loopi+1].equalsIgnoreCase("NNP")){
										    		  tword=tword+" "+word[loopi+1];
										    	  }
												  if(feature1.containsKey(tword)){
											    	  Integer counter = ((Integer)feature1.get(tword));
											          feature1.put(tword, new Integer(counter +1));
											      }
											      else
											    	  feature1.put(tword, 1);
											  }
												  
										  }
											  
									  }
								  }
							  }
						  }
						  
						  
					  }
				   for (String name: feature1.keySet()){
						  String key =name.toString(); 
						  Integer value =(Integer) feature1.get(name);
						  if(feature.containsKey(key)){
					    	  Integer counter = ((Integer)feature.get(key));
					          feature.put(key, new Integer(counter + value));
					      }
					      else{
					    	  feature.put(key, value);chkflag5=1;}
				   }
				   
				   feature1=new HashMap<String,Integer>();
				  ///end of Rule 3.1
				 
				   
				   
				   
				  
				  ////Rule 3.2
				   
				   for (String name: feature.keySet()){
						  String key =name.toString();
						 for(int posi=0;posi<word.length;posi++){
							 if(key.equalsIgnoreCase(word[posi])==true && (wordPOS[posi].equalsIgnoreCase("NN")==true ||wordPOS[posi].equalsIgnoreCase("NNP")==true || wordPOS[posi].equalsIgnoreCase("NNS")==true)){
								  for(i=0;i<p;i++){
									  if(depMatrix[i][1].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj"))){
										  String tword=depMatrix[i][2];
										 
										  for(int j=0;j<p;j++){
											  if(depMatrix[j][1].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj"))){
												  String tword2=depMatrix[j][2];
										 
										  for(int loopi=1;loopi<word.length-1;loopi++){
											  if(tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS")|| wordPOS[loopi].equalsIgnoreCase("NNP"))){
												  if(wordPOS[loopi-1].equalsIgnoreCase("NN") || wordPOS[loopi-1].equalsIgnoreCase("NNS")|| wordPOS[loopi-1].equalsIgnoreCase("NNP")){
										    		  tword2=word[loopi-1]+" "+tword2;
										    	  }
												  if(wordPOS[loopi+1].equalsIgnoreCase("NN") || wordPOS[loopi+1].equalsIgnoreCase("NNS")|| wordPOS[loopi+1].equalsIgnoreCase("NNP")){
										    		  tword2=tword2+" "+word[loopi+1];
										    	  }
												  if(feature1.containsKey(tword2)){
											    	  Integer counter = ((Integer)feature1.get(tword2));
											          feature1.put(tword2, new Integer(counter +1));
											      }
											      else
											    	  feature1.put(tword2, 1);
												  
												  
												  
											  }
												  
										  }
											  
									  }
											  if(depMatrix[j][2].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj"))){
												  String tword2=depMatrix[j][1];
										 
										  for(int loopi=1;loopi<word.length-1;loopi++){
											  if(tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS")|| wordPOS[loopi].equalsIgnoreCase("NNP"))){
												  if(wordPOS[loopi-1].equalsIgnoreCase("NN") || wordPOS[loopi-1].equalsIgnoreCase("NNS")|| wordPOS[loopi-1].equalsIgnoreCase("NNP")){
										    		  tword2=word[loopi-1]+" "+tword2;
										    	  }
												  if(wordPOS[loopi+1].equalsIgnoreCase("NN") || wordPOS[loopi+1].equalsIgnoreCase("NNS")|| wordPOS[loopi+1].equalsIgnoreCase("NNP")){
										    		  tword2=tword2+" "+word[loopi+1];
										    	  }
												  if(feature1.containsKey(tword2)){
											    	  Integer counter = ((Integer)feature1.get(tword2));
											          feature1.put(tword2, new Integer(counter +1));
											      }
											      else
											    	  feature1.put(tword2, 1);
											  }
												  
										  }
											  
									  }
									
								  }
							  }
									  if(depMatrix[i][2].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj"))){
										  String tword=depMatrix[i][1];///
										 
										  for(int j=0;j<p;j++){
											  if(depMatrix[j][1].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj"))){
												  String tword2=depMatrix[j][2];
										 
										  for(int loopi=1;loopi<word.length-1;loopi++){
											  if(tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS")|| wordPOS[loopi].equalsIgnoreCase("NNP"))){
												  if(wordPOS[loopi-1].equalsIgnoreCase("NN") || wordPOS[loopi-1].equalsIgnoreCase("NNS")|| wordPOS[loopi-1].equalsIgnoreCase("NNP")){
										    		  tword2=word[loopi-1]+" "+tword2;
										    	  }
												  if(wordPOS[loopi+1].equalsIgnoreCase("NN") || wordPOS[loopi+1].equalsIgnoreCase("NNS")|| wordPOS[loopi+1].equalsIgnoreCase("NNP")){
										    		  tword2=tword2+" "+word[loopi+1];
										    	  }
												  if(feature1.containsKey(tword2)){
											    	  Integer counter = ((Integer)feature1.get(tword2));
											          feature1.put(tword2, new Integer(counter +1));
											      }
											      else
											    	  feature1.put(tword2, 1);
											  }
												  
										  }
											  
									  }
											  if(depMatrix[j][2].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj"))){
												  String tword2=depMatrix[j][1];
										 
										  for(int loopi=1;loopi<word.length-1;loopi++){
											  if(tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("NN") || wordPOS[loopi].equalsIgnoreCase("NNS")|| wordPOS[loopi].equalsIgnoreCase("NNP"))){
												  if(wordPOS[loopi-1].equalsIgnoreCase("NN") || wordPOS[loopi-1].equalsIgnoreCase("NNS")|| wordPOS[loopi-1].equalsIgnoreCase("NNP")){
										    		  tword2=word[loopi-1]+" "+tword2;
										    	  }
												  if(wordPOS[loopi+1].equalsIgnoreCase("NN") || wordPOS[loopi+1].equalsIgnoreCase("NNS")|| wordPOS[loopi+1].equalsIgnoreCase("NNP")){
										    		  tword2=tword2+" "+word[loopi+1];
										    	  }
												  if(feature1.containsKey(tword2)){
											    	  Integer counter = ((Integer)feature1.get(tword2));
											          feature1.put(tword2, new Integer(counter +1));
											      }
											      else
											    	  feature1.put(tword2, 1);
											  }
												  
										  }
											  
									  }
									
								  }
							  }
									  
									  
						  }
						  
						  
					  }
					  
						  }
					  
					  
						  }	
				   
				   for (String name: feature1.keySet()){
						  String key =name.toString(); 
						  Integer value =(Integer) feature1.get(name);
						  if(feature.containsKey(key)){
					    	  Integer counter = ((Integer)feature.get(key));
					          feature.put(key, new Integer(counter + value));
					      }
					      else{
					    	  feature.put(key, value);chkflag6=1;}
				   }
				  
				   feature1=new HashMap<String,Integer>();
				  ////End of Rule 3.2
		  
				  
				   
			//Rule 2.1
				  
		//opDictexpanded.addAll(opI);		  
				  
				  
				   for (String name: feature.keySet()){
						  String key =name.toString();
			for(int posi=0;posi<word.length;posi++){
				 // System.out.println(word[posi]);
				  if(key.equalsIgnoreCase(word[posi])==true && (wordPOS[posi].equalsIgnoreCase("NN")==true ||wordPOS[posi].equalsIgnoreCase("NNP")==true || wordPOS[posi].equalsIgnoreCase("NNS")==true)){
					  for(i=0;i<p;i++){
						  if(depMatrix[i][1].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj")|| depMatrix[i][0].equalsIgnoreCase("dep"))){
							  String tword=depMatrix[i][2];
							  for(int loopi=0;loopi<word.length;loopi++){
								  if(tword.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("JJ") || wordPOS[loopi].equalsIgnoreCase("JJR")|| wordPOS[loopi].equalsIgnoreCase("JJS"))){
									  if(opDict1.containsKey(tword)){
										     }
										      
										    	  else{
											    	  
											    	opDict1.put(tword, POLVAL);//detect latter
											  }
									  
									  
									  
									  String annt=new String();
									  annt=reviewid + "@"+tword+"@"+Integer.toString(POLVAL);
									  if(annote.contains(annt)){
										  
									  }else
										  annote.add(annt);
								  }
									  
							  }
								  
						  }
						  if(depMatrix[i][2].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj")|| depMatrix[i][0].equalsIgnoreCase("dep"))){
							  String tword=depMatrix[i][1];
							  for(int loopi=0;loopi<word.length;loopi++){
								  if(tword.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("JJ") || wordPOS[loopi].equalsIgnoreCase("JJR")|| wordPOS[loopi].equalsIgnoreCase("JJS"))){
									 if(opDict1.containsKey(tword)){
										    	  /*Integer counter = ((Integer)opDict1.get(tword));
										          opDict1.put(tword, new Integer(counter +1));*/
										      }
									 else{
								    	  
								    	  //Integer value =(Integer) opDict.get(name);
								    	  opDict1.put(tword, POLVAL);
								  }
									 String annt=new String();
									  annt=reviewid + "@"+tword+"@"+Integer.toString(POLVAL);
									  if(annote.contains(annt)){
										  
									  }else
										  annote.add(annt);
								  }
									  
							  }
								  
						  }
					  }
				  }
			  }
			  
			  
		  }		  
				  
	//Rule 2.2
				   for (String name: feature.keySet()){
						  String key =name.toString();
			 // System.out.println(op[0]);
			  for(int posi=0;posi<word.length;posi++){
				 // System.out.println(word[posi]);
				  if(key.equalsIgnoreCase(word[posi])==true && (wordPOS[posi].equalsIgnoreCase("NN")==true ||wordPOS[posi].equalsIgnoreCase("NNP")==true || wordPOS[posi].equalsIgnoreCase("NNS")==true)){
					  for(i=0;i<p;i++){
						  if(depMatrix[i][1].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj")|| depMatrix[i][0].equalsIgnoreCase("dep"))){
							  String tword=depMatrix[i][2];
							 
							  for(int j=0;j<p;j++){
								  if(depMatrix[j][1].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj")|| depMatrix[i][0].equalsIgnoreCase("dep"))){
									  String tword2=depMatrix[j][2];
							 
							  for(int loopi=0;loopi<word.length;loopi++){
								  if(tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("JJ") || wordPOS[loopi].equalsIgnoreCase("JJR")|| wordPOS[loopi].equalsIgnoreCase("JJS"))){
									  if(opDict1.containsKey(tword2)){
										    	  /*Integer counter = ((Integer)opDict1.get(tword2));
										          opDict1.put(tword2, new Integer(counter +1));*/
										      }
										      else
										    	  opDict1.put(tword2, POLVAL);
									  String annt=new String();
									  annt=reviewid + "@"+tword2+"@"+Integer.toString(POLVAL);
									  if(annote.contains(annt)){
										  
									  }else
										  annote.add(annt);
								  }
									  
							  }
								  
						  }
								  if(depMatrix[j][2].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj")|| depMatrix[i][0].equalsIgnoreCase("dep"))){
									  String tword2=depMatrix[j][1];
							 
							  for(int loopi=0;loopi<word.length;loopi++){
								  if(tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("JJ") || wordPOS[loopi].equalsIgnoreCase("JJR")|| wordPOS[loopi].equalsIgnoreCase("JJS"))){
									 if(opDict1.containsKey(tword2)){
										    	  /*Integer counter = ((Integer)opDict1.get(tword2));
										          opDict1.put(tword2, new Integer(counter +1));*/
										      }
										      else
										    	  opDict1.put(tword2, POLVAL);
									 String annt=new String();
									  annt=reviewid + "@"+tword2+"@"+Integer.toString(POLVAL);
									  if(annote.contains(annt)){
										  
									  }else
										  annote.add(annt);
								  }
									  
							  }
								  
						  }
						
					  }
				  }
						  if(depMatrix[i][2].equalsIgnoreCase(word[posi]) && (depMatrix[i][0].equalsIgnoreCase("amod") || depMatrix[i][0].equalsIgnoreCase("nsubj") || depMatrix[i][0].equalsIgnoreCase("dobj")|| depMatrix[i][0].equalsIgnoreCase("dep"))){
							  String tword=depMatrix[i][1];///
							 
							  for(int j=0;j<p;j++){
								  if(depMatrix[j][1].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj")|| depMatrix[i][0].equalsIgnoreCase("dep"))){
									  String tword2=depMatrix[j][2];
							 
							  for(int loopi=0;loopi<word.length;loopi++){
								  if(tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("JJ") || wordPOS[loopi].equalsIgnoreCase("JJR")|| wordPOS[loopi].equalsIgnoreCase("JJS"))){
									 if(opDict1.containsKey(tword2)){
										    	 /* Integer counter = ((Integer)opDict1.get(tword2));
										          opDict1.put(tword2, new Integer(counter +1));*/
										      }
										      else
										    	  opDict1.put(tword2, POLVAL);
									 String annt=new String();
									  annt=reviewid + "@"+tword2+"@"+Integer.toString(POLVAL);
									  if(annote.contains(annt)){
										  
									  }else
										  annote.add(annt);
								  }
									  
							  }
								  
						  }
								  if(depMatrix[j][2].equalsIgnoreCase(tword) && (depMatrix[j][0].equalsIgnoreCase("amod") || depMatrix[j][0].equalsIgnoreCase("nsubj") || depMatrix[j][0].equalsIgnoreCase("dobj")|| depMatrix[i][0].equalsIgnoreCase("dep"))){
									  String tword2=depMatrix[j][1];
							 
							  for(int loopi=0;loopi<word.length;loopi++){
								  if(tword2.equalsIgnoreCase(word[loopi]) && (wordPOS[loopi].equalsIgnoreCase("JJ") || wordPOS[loopi].equalsIgnoreCase("JJR")|| wordPOS[loopi].equalsIgnoreCase("JJS"))){
									 if(opDict1.containsKey(tword2)){
										    	  /*Integer counter = ((Integer)opDict1.get(tword2));
										          opDict1.put(tword2, new Integer(counter +1));*/
										      }
										      else
										    	  opDict1.put(tword2, POLVAL);
									 String annt=new String();
									  annt=reviewid + "@"+tword2+"@"+Integer.toString(POLVAL);
									  if(annote.contains(annt)){
										  
									  }else
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
				  
				  
				   for (String name: opDict1.keySet()){
						  String key =name.toString(); 
						  Integer value =(Integer) opDict1.get(name);
						  if(opDict.containsKey(key)){
					    	  /*Integer counter = ((Integer)opDict.get(key));
					          opDict.put(key, new Integer(counter + value));*/
					      }
					      else{
					    	  opDict.put(key, value);chkflag7=1;}
				   }   
				   
				   opDict1=new HashMap<String,Integer>();	  
				  //end of Rule 2.2
				   
				   
				   
				   
				 //break;//sentence break at single review
			     
		
}
	          
	        
			
			//break;//review break
			
		}
	loopiter++;	
	}while( chkflag1==1 || chkflag2==1 || chkflag3==1 || chkflag5==1 || chkflag6==1 || chkflag7==1  );
	///
	int gg=0;String content;
	//File file = new File("E:\\work\\Deepanshu\\Video_Game_Output/initialFeatures.txt");
	File file = new File("../output/initialFeatures.txt");
	// if file doesnt exists, then create it
	if (!file.exists()) {
		file.createNewFile();
	}
	FileWriter fw = new FileWriter(file.getAbsoluteFile());
	BufferedWriter bw = new BufferedWriter(fw);
		   for (String name: feature.keySet()){

		          String key =name.toString();
		          Integer value =(Integer) feature.get(name);  
		          //System.out.println(key + ":" + value);  
		          gg++;
		           content=new String();
		          content=key + ":" + value;
		          bw.write(content+"\r\n");
					

		} 
		   bw.close();
		System.out.println(gg);
	
	//feature freq
	
	HashMap<String,Integer> featureFreq=new HashMap<String,Integer>();
	//String str="E:\\work\\Deepanshu\\nowInput\\sample.txt";
	String str=args[2];
	BufferedReader in= new BufferedReader(new FileReader(str));
	//start Preprocessing
	String sCurrentLine;
	int i=0;
	int reviewid=0;
	while((sCurrentLine = in.readLine()) != null){
		
		int val=0;
		
		reviewid++;
		String overall=sCurrentLine.substring(sCurrentLine.indexOf(',')+1, sCurrentLine.indexOf(',', sCurrentLine.indexOf(',')+1));
		String reviewText=sCurrentLine.substring(sCurrentLine.indexOf(',', sCurrentLine.indexOf(',')+1)+1);
		int score=Integer.parseInt(overall.substring(0, overall.indexOf('.')));
		if(score>=3)
			val=1;
		else
			val=-1;
		String processedreviewText=new String();;
		//remove multiple dots
		for(i=0;i<reviewText.length()-1;i++){
			if(reviewText.charAt(i)=='.' && reviewText.charAt(i+1)=='.')
				continue;
				else
				processedreviewText+=reviewText.charAt(i);
				
		}
		processedreviewText+=reviewText.charAt(i);
		processedreviewText=processedreviewText.toLowerCase();////text after removing multiple dots + all lower case
		processedreviewText=processedreviewText.replace('_',' ');
		
		System.out.println("New="+processedreviewText);
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
		   System.out.println("Review Sentence="+sentence);
		   String word[]=sentence.split(" ");    //1)Word tokenization
		   String wordPOS[]=new String[word.length];
			 //Tag the sentence
				 MaxentTagger tagger = new MaxentTagger("tagger/english-bidirectional-distsim.tagger");
			     String tagged = tagger.tagString(sentence);
			    int flag=0;int count=0;
			    
			     //Calculate wordPOS array
			     for(i=0;i<tagged.length();i++){
			    	 if(tagged.charAt(i)=='_' && flag==0){
			    		 flag=1;
			    		 wordPOS[count]=Character.toString(tagged.charAt(i+1));i++;
			    	 }
			    	 else if(tagged.charAt(i)!=' ' && flag==1)
			    		 wordPOS[count]+=tagged.charAt(i);
			    	 else if(tagged.charAt(i)==' ' && flag==1){
			    		 flag=0;
			    		 count++;
			    		 }
			    	 else
			    	 continue;
			     }
			     for(int var=0;var<word.length-2;var++ ){
			    	 String newFeatureWord=new String();
			    	 if(wordPOS[var].equalsIgnoreCase("JJ")||wordPOS[var].equalsIgnoreCase("JJR")||wordPOS[var].equalsIgnoreCase("JJS")){
			    		 int limit1=Math.min(var+1, word.length-1);int limit2=Math.min(var+2,word.length-1);
			    		 if(wordPOS[limit1].equalsIgnoreCase("NN") || wordPOS[limit1].equalsIgnoreCase("NNS")|| wordPOS[limit1].equalsIgnoreCase("NNP")){
			    			 newFeatureWord=word[limit1];
			    			 if(opDict.containsKey(word[var]))
			    				 val=(Integer)opDict.get(word[var]);
							  int newnegPol=1;
							  for(int PolVar=Math.max(0, var-2);PolVar<Math.min(word.length, var+2);PolVar++)
								  if(word[PolVar].equalsIgnoreCase("not")||word[PolVar].equalsIgnoreCase("n't")||word[PolVar].equalsIgnoreCase("'t")||word[PolVar].equalsIgnoreCase("however")||word[PolVar].equalsIgnoreCase("but")||word[PolVar].equalsIgnoreCase("despite")||word[PolVar].equalsIgnoreCase("though")||word[PolVar].equalsIgnoreCase("except")||word[PolVar].equalsIgnoreCase("although")||word[PolVar].equalsIgnoreCase("oddly"))
									  	newnegPol=-1;
				    		 val*=newnegPol;
			    		 if(wordPOS[limit2].equalsIgnoreCase("NN") || wordPOS[limit2].equalsIgnoreCase("NNS")|| wordPOS[limit2].equalsIgnoreCase("NNP"))
				    		  newFeatureWord=newFeatureWord+" "+word[limit2];
			    		 if(feature.containsKey(newFeatureWord)){
					    	  Integer counter = ((Integer)feature.get(newFeatureWord));
					          feature.put(newFeatureWord, new Integer(counter +1));
					      }
					      else{
					    	  feature.put(newFeatureWord, 1);
					      }
			    		 String annt=new String();
						  annt=reviewid + "@"+newFeatureWord+"@"+Integer.toString(val);
						  System.out.println(annt);
						  if(annote.contains(annt)){
							  
						  }else
							  annote.add(annt);
						  
				    		 
			    		 }
			    	 }
			    	 
			    	 
				    	  
			     }
		   
		   for(String name:feature.keySet()){
			   String key =name.toString();
			 if(key.indexOf(' ')>=0){
				 if(sentence.toLowerCase().contains(key.toLowerCase())){
				 if(featureFreq.containsKey(key)){
			    	  Integer counter = ((Integer)featureFreq.get(key));
			    	  featureFreq.put(key, new Integer(counter + 1));
			      }
			      else
			    	  featureFreq.put(key, 1); 
				 	
				 }
			 }
			 else{
		   for(int j=0;j<word.length;j++){
			  if(word[j].equalsIgnoreCase(key)) {
				  if(featureFreq.containsKey(key)){
			    	  Integer counter = ((Integer)featureFreq.get(key));
			    	  featureFreq.put(key, new Integer(counter + 1));
			      }
			      else{
			    	  featureFreq.put(key, 1);System.out.println("KEY="+key);}
			  }
		   }
			 }//else
		   
		   
		}
		}
	}
	
	gg=0;
	// file = new File("E:\\work\\Deepanshu\\Video_Game_Output\\Freq_Feature.txt");
	file=new File("../output/Freq_Feature.txt");
	// if file doesnt exists, then create it
	if (!file.exists()) {
		file.createNewFile();
	}
	 fw = new FileWriter(file.getAbsoluteFile());
	 bw = new BufferedWriter(fw);
		   for (String name: featureFreq.keySet()){

		          String key =name.toString();
		          Integer value =(Integer) featureFreq.get(name);  
		          //System.out.println(key + ":" + value);  
		          gg++;
		           content=new String();
		          content=key + ":" + value;
		          bw.write(content+"\r\n");
					

		} 
		   bw.close();
		System.out.println(gg);
		
	
		gg=0;
		//file = new File("E:\\work\\Deepanshu\\Video_Game_Output\\opine3.txt");
		file = new File("../output/opine3.txt");
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		fw = new FileWriter(file.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		   for (String name: opDict.keySet()){

		    
				   String key =name.toString();
			          Integer value =(Integer) opDict.get(name);  
			          //System.out.println(key + ":" + value);
			          content=new String();
			          content=key + ":" + value;
			          bw.write(content+"\r\n");
			   
		          gg++;

		} 
		   bw.close();
		System.out.println(gg);
		System.out.println(loopiter);
		
		
		ArrayList<String> finalFeature=new ArrayList<String>();
		
		
		
		
		for(String name:featureFreq.keySet()){
			String key =name.toString();
	          Integer value =(Integer) featureFreq.get(name);  
			if(!key.contains(".") && !key.contains("?") && !key.contains("!")){
				if(value>=0){
					finalFeature.add(key);
				}
				
			}
			
			
		}
		
		
		
		// str="E:\\work\\Deepanshu\\Video_Game_Output\\FinalFeature.txt";
		str="../output/FinalFeature.txt";
		FileWriter fr= new FileWriter(str);
		BufferedWriter out = new BufferedWriter(fr);
		 i=0;
		String newFeature;
		while(i<finalFeature.size()){
			newFeature=finalFeature.get(i);
			out.write(newFeature+"\r\n");
			i++;
		}
		
		inp.close();
		out.close();
		
		ArrayList<String> annote1=new ArrayList<String>();
		for(String name:annote){
			int flag=0;
			for(String name1:finalFeature){
				if(name.toLowerCase().contains(name1.toLowerCase())){
					flag=1;
					break;
				}
			}
			if(flag==1)
				annote1.add(name);
		}
		
		
		
		
		//String str5="E:\\work\\Deepanshu\\Video_Game_Output\\annote.txt";
		String str5="../output/annote.txt";
		// if file doesnt exists, then create it
		
		fw = new FileWriter(str5);
		bw = new BufferedWriter(fw);
		for(String name:annote1){
			System.out.println(name.toString());
			bw.write(name.toString()+"\r\n");
		}
			bw.close();
			
			
		
	}

}

