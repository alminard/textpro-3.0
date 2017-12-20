#!/bin/sh
echo "Testing..."

if [ -e "test/output/trento_wiki_it.txt.txp" ]; then
  \rm test/output/trento_wiki_it.txt.txp
fi

if [ -e "test/output/trento_wiki_en.txt.txp" ]; then
  \rm test/output/trento_wiki_en.txt.txp
fi

if [ -e "test/output/trento_wiki_en.api.txt.txp" ]; then
  \rm test/output/trento_wiki_en.api.txt.txp
fi

if [ -e "test/output/BigFiles-4-files-wikinews-en.txt.txp" ]; then
  \rm test/output/BigFiles-4-files-wikinews-en.txt.txp
fi

echo
echo
##echo "TextPro is running on test/input/trento_wiki_it.txt"
./textpro.sh -v -l ita -o test/output/ -n trento_wiki_it.txt.txp -y -c token+tokenid+tokennorm+tokenstart+tokenend+tokentype+pos+full_morpho+comp_morpho+lemma+entity+geoinfo+chunk+parserid+head+deprel+keywords+sentiment test/input/trento_wiki_it.txt

echo 
echo
##echo "TextPro is running on test/input/trento_wiki_en.txt"
./textpro.sh -v -l eng -o test/output/ -n trento_wiki_en.txt.txp -y -c token+tokenid+tokennorm+tokenstart+tokenend+tokentype+pos+full_morpho+comp_morpho+lemma+chunk+timex+keywords+sentiment test/input/trento_wiki_en.txt

echo
echo
##echo "TextPro is running on test/input/trento_wiki_en.txt"
./textpro.sh -v -l eng -o test/output/ -n trento_wiki_en-core.txt.txp -y -c token+tokenid+tokennorm+tokenstart+tokenend+tokentype+pos+full_morpho+comp_morpho+lemma+chunk+keywords test/input/trento_wiki_en.txt

echo 
echo
##echo "Testing TextPro-API on test/input/trento_wiki_en.txt"
$JAVA_HOME/bin/java -Dfile.encoding=UTF8 -cp "./classes/:./lib/textpro-api.jar:./lib/junit-4.5.jar" eu.fbk.textpro.tester.apiTester

echo 
echo
##echo "TextPro is running on test/input/trento_wiki_en.txt"
./textpro.sh -v -l eng -o test/output/ -n BigFiles-4-files-wikinews-en.txt.txp -y -c token+tokenid+tokennorm+tokenstart+tokenend+tokentype+pos+full_morpho+comp_morpho+lemma+chunk+timex+keywords+sentiment test/input/BigFiles-4-files-wikinews-en.txt

echo
$JAVA_HOME/bin/java -Dfile.encoding=UTF8 -cp "./classes/:./lib/junit-4.5.jar" eu.fbk.textpro.tester.TextProTester -e eu.fbk.textpro.tester.exact -runTextpro false -t test/gold/trento_wiki_it.txt.txp -f test/output/trento_wiki_it.txt.txp
$JAVA_HOME/bin/java -Dfile.encoding=UTF8 -cp "./classes/:./lib/junit-4.5.jar" eu.fbk.textpro.tester.TextProTester -e eu.fbk.textpro.tester.exact -runTextpro false -t test/gold/trento_wiki_en.txt.txp -f test/output/trento_wiki_en.txt.txp
$JAVA_HOME/bin/java -Dfile.encoding=UTF8 -cp "./classes/:./lib/junit-4.5.jar" eu.fbk.textpro.tester.TextProTester -e eu.fbk.textpro.tester.exact -runTextpro false -t test/gold/trento_wiki_en-core.txt.txp -f test/output/trento_wiki_en.api.txt.txp
$JAVA_HOME/bin/java -Dfile.encoding=UTF8 -cp "./classes/:./lib/junit-4.5.jar" eu.fbk.textpro.tester.TextProTester -e eu.fbk.textpro.tester.exactLineString -runTextpro false -t test/gold/BigFiles-4-files-wikinews-en.txt.txp -f test/output/BigFiles-4-files-wikinews-en.txt.txp

