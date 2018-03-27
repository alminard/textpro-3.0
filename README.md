                     -------- TextPro v3.0 -------

         --- FBK (Fondazione Bruno Kessler, Povo, Italy) ----
--- Anne-Lyse Minard, Mohammed Qwaider, Emanuele Pianta, Christian Girardi, Roberto Zanoli ----

TextPro supports the most common NLP tasks, such as tokenization, sentence segmentation, part-of-speech tagging, lemmatization, named entity extraction, chunking and keywords extraction for Italian and English.

Requirements
------------
    * Java 1.8 

Installing TextPro
------------------

1) Unzip the zip file (e.g. TextProFBK-3.0.zip) into a directory of your choice (e.g. /home/).

2) TextPro needs a Java™ Virtual Machine on your computer. If you don't have Java™ runtime environment (JRE) yet, download and install it from http://java.sun.com/javase/downloads/index.jsp.
Then set the JAVA_HOME environment variable to point to the directory where the Java™ runtime environment is installed on your  computer.
On csh/tcsh shell type this command:
$> setenv JAVA_HOME <directory where the JDK is installed>

3) Change the path TEXTPROHOME in resources/conf/config.properties

4) Build with maven:

$> mvn clean package

5) Untar the executable textpro:

$> cd target/

$> tar -xzf textpro-0.0.1-SNAPSHOT-textpro.tar.gz

$> cd textpro/


Usage TextPro
-------------

INPUT

TextPro needs a plain text. The encoding must be UTF8. 
The plain text can be already tokenized (one token per line) or not. 
In the first case you must disable the tokenization by "-dis tokenization" option. The empty lines are left.


OUTPUT

TextPro produces an output file with one token per line. The informations for each token are represented in the line with the columns separated by tabular space. 
You choose the output information with -c option (see the document docs/TextPro_annotations.html for details).


RUN

TextPro 3.0 can be also used in client-server mode. 

1) Client-server mode:
* Launch the server

$> bash start-server.sh

When you see "Start listening to server name: ..." the server is ready to be used.

* Call TextPro (on an other terminal)

$> bash textpro-client.sh -l ita -c token+pos -i example_ita.txt

2) Classic mode:

$> textpro.sh -l ita -c token+pos -i example_ita.txt


EXAMPLES 

* Part of Speech tagging for Italian language:

$> textpro.sh -l ita -c token+pos -o /tmp/ -i example_ita.txt

* Named Entity Recognition for English language:

$> textpro.sh -l eng -c token+entity -o /tmp/ -i example_eng.txt


HELP
----
$> ./textpro.sh -h

Usage:
   textpro.sh [OPTIONS] <INPUT FILE or DIR>

Options:
* -help                                 show the help and exit;
* -debug                                debug mode, do not delete tmp-files and to get more verbose output;
* -report                               check the input text and print a report on the unknown things;
* -v                                    verbose mode;
* -l        <LANGUAGE>                  the language: 'eng' or 'ita' are possible; 'eng' is the default;
* -c        <COLUMN or HEADER fields> the sequence of column values: token+tokenid+tokennorm+tokenstart+tokenend+tokentype+pos+full_morpho+comp_morpho+lemma+entity+chunk+tmx+tmxvalue+tmxid+tmxanchor+beginpoint+endpoint+mod;
* -o        <DIRNAME>                   the output directory path;
* -n        <FILENAME>                  the output filename. If this value is specified the output is redirected to the file named as FILENAME. By default the file named as INPUTFILE plus '.txp' suffix;
* -y                                    force rewriting all existing output files;
* -dis        tokenization+sentence       disable the tokenization or/and sentence splitting;
* -rec                                    process all files in the input directory recursively;
* -dct	  <DCT>			      set the document creation time.
* -i	  <INPUT FILE or DIR>	      input raw text, html text or directory.
* -type	  colloquial		      the texts to process are colloquial texts (avialable for French and English)
