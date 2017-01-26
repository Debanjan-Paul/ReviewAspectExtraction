# Review Aspect Extraction

Given a review corpus, extracts out the aspects.

Input:
* Positive-opinions-file : File which contains list of positive opinions (adjectives), separated by newline.
* Negative-opinions-file : File which contains list of negative opinions (adjectives), separated by newline.
* Reviews-file : File which contains all the reviews. (Format: <productId>,<rating>,<review-text>\n)

Output:

* Generates one file which contains extracted aspects for each review.
* Format: <review-index>,<product-aspect>,<opinion-polarity[1/-1]>

# Run the project:

* `cd <project-root>`
* `./scripts/run.sh <path of the directory which contains 3 files, positive.txt, negative.txt, reviews.txt>`
* Output file will be generated in the same directory with file-name: aspects.txt
* eg: `./scripts/run.sh /home/user1/data/mobile`

# Run with IntellijIdea:

    - `cd <project-root>`
    - mvn clean install assembly:single
    - mvn idea:idea
    - Run > Edit Configurations > Add new configuration(click on + icon) > Application
        - Name: AspectExtraction
        - MainClass: com.kgp.aspect.extraction.AspectExtractionApp
        - Program Arguments: <path of input directory> (eg: /Users/user-1/data/mobile)
        - Working Dir: <path to git root (ReviewAspectExtraction)>
        - Classpath of module: ReviewAspectExtraction
        - Check the top right box (single instance only), [this will avoid accidental multiple startups]

## Note:

* Smiley Problem: Line 100 to Line 112 is resposible for preprocessing steps.There some manual smiley detection has been done.It needs to be improved.
