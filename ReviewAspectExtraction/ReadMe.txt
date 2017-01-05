Create a project Directory and run the following command:
git clone https://github.com/Debanjan-Paul/ReviewAspectExtraction.git

Change directory to the project folder and then run the following command:
mvn clean install assembly:single

Now make a folder named "output" in the same directory [mkdir output]

Change directory to the folder named "target" 

Run the following command in the given format:
java -jar <JARFILE> <Filename1> <Filename2> <Filename3>
<Filename1>: Absolute/Relative path address of postive.txt  [Positive opinion lexicon]
<Filename2>: Absolute/Relative path address of negative.txt [Negative opinion lexicon]
<Filename3>: Absolute/Relative path address of sample.txt [Review Input File]

The review Input file must in the following format:
A typical line in the review file is as follow:
<ProductId><,><Overall Ratings><,><Review Text>

example : java -jar ReviewAspectExtraction-0.0.1-SNAPSHOT-jar-with-dependencies.jar E:/work/Debanjan/positive.txt E:/work/Debanjan/negative.txt E:/work/Debanjan/nowInput/sample3.txt>

The file named "annote.txt" created in the above mentioned "output" folder contains the Final Output of our ReviewAspectExctraction System in the following format : <ReviewId>@<ProductAspect>@<Opinion Polarity>

Issues
Note:
Smiley Problem:
Line 100 to Line 112 is resposible for preprocessing steps.There some manual smiley detection has been done.It needs to be improved.
