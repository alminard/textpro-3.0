package eu.fbk.textpro.modules.lemmapro;

import java.io.*;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import eu.fbk.textpro.modules.lemmapro.Tagsets.Tagset;
import eu.fbk.textpro.modules.lemmapro.Tagsets.Tagset.Tag;
import eu.fbk.textpro.modules.lemmapro.Tagsets.Tagset.Tag.Rules;
import eu.fbk.textpro.modules.lemmapro.Tagsets.Tagset.Tag.Rules.Rule;
import eu.fbk.textpro.toolbox.TEXTPROCONSTANT;
import eu.fbk.textpro.toolbox.TEXTPROVARIABLES;
import eu.fbk.textpro.wrapper.TextProPipeLine;


public class lemmaConfig {
    Hashtable<String, Tag> tagset = new Hashtable<String, Tag>();


    boolean morphoMatcher(String pos, String line) throws UnsupportedEncodingException, FileNotFoundException, JAXBException{
        //Hashtable<String, String> temptags = new  Hashtable<String, String>();
        if (tagset.containsKey(pos)) {
            Tag tag =  tagset.get(pos);
            ListIterator<Rules> rulesl = tag.getRules().listIterator();
            while(rulesl.hasNext()) {
                boolean answer = false; // this to check if the rules tag matches all the regular expression in side the rules, then the tag name which returned in the result set!
                boolean finalAnswer=false;
                Rules rulestmp = rulesl.next();
                ListIterator<Rule> rulel = rulestmp.getRule().listIterator();
                //System.err.println("----------------");
                LinkedList<Boolean> answerrules = new LinkedList<Boolean>();
                while(rulel.hasNext()) {
                    //here are the if statments
                    // now this programming only support the or operation
                    Rule ifS = rulel.next();
                    ListIterator<String> exl = ifS.getExpression().listIterator();
                    while(exl.hasNext()) {
                        String extmp = exl.next();
                        ///checkpoint						System.err.println("line " + line  + " ,extmp " + extmp + " " +line.matches(extmp));
                        if (line.matches(extmp)) {
                            answer = true;
                            //System.out.println("line="+line+"=ex=+"+extmp+"=answer="+line.matches(extmp));
                            break;
                        }
                    }
                    answerrules.add(answer);
                    answer=false;
                }
                ListIterator ansS = answerrules.listIterator();
                //String asd ="";
                while(ansS.hasNext()) {
                    Object ansT = ansS.next();
                    if (ansT.equals(false))
                        finalAnswer = true;
                    //asd+=" "+ansT.toString();
                }
///checkpoint
//System.out.println("line="+line+" output="+tagtmp.getTagName().toString()+" asd="+asd+" finalAns="+!finalAnswer);
                // if the test of the if statments has give true results here, the tagName of that answer should added to the return list, and the answer should re-initialized to false
                if (!finalAnswer) {
                    //temptags.put(tag.tagName, "1");
                    return true;
                }
                //System.out.println("tagName="+tagtmp.getTagName().toString());
                answer = false;
                finalAnswer = false;
            }

        }// if the tag name not equal to pos

        /* if (!languageTest) {
           System.err.println("Language not found: "+lang);
       }*/
        //return temptags;
        return false;
    }

    public String getWNpos (String pos) {
        Tag tag = tagset.get(pos);
        if (tag != null && tag.wnpos != null) {
            return tag.wnpos;
        }
        return TEXTPROCONSTANT.NULL;
    }

    void readConfigFile(String language) throws JAXBException, IOException{
        JAXBContext jc = JAXBContext.newInstance("eu.fbk.textpro.modules.lemmapro");
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        Tagsets tags = null;
        URL url = getClass().getResource("/conf/tagset.xml");
        File overwrittenFile = new File(TEXTPROVARIABLES.getTEXTPROPATH() + "/conf/tagset.xml");
        if(overwrittenFile.exists()&&overwrittenFile.isFile()){
        	tags = (Tagsets) unmarshaller.unmarshal(new InputStreamReader(new FileInputStream(TEXTPROVARIABLES.getTEXTPROPATH() + "/conf/tagset.xml"), "UTF-8"));
        }else if (url != null){
        	tags = (Tagsets) unmarshaller.unmarshal(new InputStreamReader(url.openStream(), "UTF-8"));
        }else{
        	System.out.println("Error: tagset.xml file not found!");
        }





        ListIterator<Tagset> tagsetl = tags.getTagset().listIterator();
        //boolean languageTest=false;
        while(tagsetl.hasNext()) {
            Tagset tagsettmp = tagsetl.next();
            //System.err.println("Language="+tagsettmp.lang+"=inputLang="+lang);
            if (tagsettmp.lang.equalsIgnoreCase(language)) {  //check the language of the patterns the same as the input!
                //System.err.println(tagsettmp.lang.equalsIgnoreCase(lang));
                //languageTest=true;
                ListIterator<Tag> tagl =  tagsettmp.getTag().listIterator();
                while(tagl.hasNext()) {
                    Tag tagtmp = tagl.next();
                    tagset.put(tagtmp.tagName, tagtmp);
                }// end of while tag list
            }/////////here end check of the language
        }// end iterator of tagsets
    }
}
