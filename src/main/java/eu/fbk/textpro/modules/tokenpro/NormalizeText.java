package eu.fbk.textpro.modules.tokenpro;


import eu.fbk.textpro.toolbox.TEXTPROVARIABLES;
import eu.fbk.textpro.wrapper.TextProPipeLine;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: cgirardi and Mohammed Qwaider
 * Date: 14-feb-2013
 * Time: 14.59.43
 */
public class NormalizeText {
    private static final String PUNCTUATION_REGEX ="\\p{Punct}+";
    private static final String EMAIL_REGEX="^[\\w|\\-|\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
    private static final String URL_REGEX =
            "^https?\\:\\/\\/[A-Za-z0-9\\.-]+((\\:\\d+)?\\/\\S*)?.+";
    private static Pattern[] pattern = {};
    private Hashtable<String,String> charCategory = new Hashtable<String,String>();
    private Hashtable<String,String> charNormalize = new Hashtable<String,String>();
    private Hashtable<String,String> charSplitter = new Hashtable<String,String>();
    private static Hashtable<String,Hashtable<String,String>> listAbbr = new Hashtable<String,Hashtable<String,String>>();

    //class of characters use to write the normal words
    private List typesOfWords = new ArrayList();

    public NormalizeText() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {

                String action = null;
                //String category2 = null;
                String hexcode = null;

                public void startElement(String uri, String localName,String qName,
                                         Attributes attributes) throws SAXException {
                    if (qName.equalsIgnoreCase("CHARS")) {
                        action = attributes.getValue("action");
                    }

                    if (qName.equalsIgnoreCase("CHAR")) {
                        if (attributes.getIndex("hexcode") >= 0) {
                            hexcode = attributes.getValue("hexcode");
                            if (hexcode != null) {
                                hexcode = hexcode.toLowerCase();
                                if (attributes.getIndex("category") >= 0) {
                                    charCategory.put(hexcode, attributes.getValue("category"));
                                }

                                String remove = "";
                                if (attributes.getIndex("removeit") >= 0 &&
                                        attributes.getValue("removeit").equalsIgnoreCase("yes")) {
                                    charNormalize.put(hexcode, "");
                                    remove = "remove";
                                }

                                if (action != null && action.equals("splittoken")) {
                                    charSplitter.put(hexcode, remove);
                                }
                            }

                        }
                    }

                }

                public void endElement(String uri, String localName,
                                       String qName) throws SAXException {
                    if (qName.equalsIgnoreCase("CHARS")) {
                        action = null;
                    } else {
                        hexcode = null;
                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {
                    if (hexcode != null) {
                        charNormalize.put(hexcode, new String(ch, start, length));
                    }
                }
            };


            //load normalization config file
            File overwrittenFile = new File(TEXTPROVARIABLES.getTEXTPROPATH() + "/conf/normalization.xml");
            if(overwrittenFile.exists() && overwrittenFile.isFile()){
                saxParser.parse(overwrittenFile, handler);
            } else {
                URL url = getClass().getResource("/conf/normalization.xml");

                if (url != null){
                    saxParser.parse(url.openStream(), handler);
                } else{
                    System.err.println("Error: normalization.xml file not found!");
                }
            }

	    listAbbr.put("fre", read_abbr_file(TEXTPROVARIABLES.getTEXTPROPATH() + "/conf/abbreviations-fr.lst"));
	    listAbbr.put("eng", read_abbr_file(TEXTPROVARIABLES.getTEXTPROPATH() + "/conf/abbreviations-en.lst"));


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        typesOfWords.add(Character.CURRENCY_SYMBOL);
        typesOfWords.add(Character.DECIMAL_DIGIT_NUMBER);
        typesOfWords.add(Character.FORMAT);
        typesOfWords.add(Character.LETTER_NUMBER);
        typesOfWords.add(Character.LOWERCASE_LETTER);
        typesOfWords.add(Character.UPPERCASE_LETTER);
        typesOfWords.add(Character.MODIFIER_SYMBOL);
        typesOfWords.add(Character.NON_SPACING_MARK);
        typesOfWords.add(Character.OTHER_LETTER);
        typesOfWords.add(Character.OTHER_NUMBER);
        typesOfWords.add(Character.OTHER_SYMBOL);
        typesOfWords.add(Character.PRIVATE_USE);
        typesOfWords.add(Character.SURROGATE);
        typesOfWords.add(Character.TITLECASE_LETTER);
        typesOfWords.add(Character.UNASSIGNED);

        //initialize the Pattern object
        pattern = new Pattern[3];
        pattern[0] = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
        pattern[1] = Pattern.compile(URL_REGEX, Pattern.CASE_INSENSITIVE);
        pattern[2] = Pattern.compile(PUNCTUATION_REGEX, Pattern.CASE_INSENSITIVE);

    }


    public boolean isAccent(int ch) {
        String chstr = charCategory.get(String.format("%04x", ch).toLowerCase());

        if (chstr != null && chstr.equals("accent")) {
            return true;
        }
        return false;
    }

    public boolean isApostrophe(int ch) {
        String chstr = charCategory.get(String.format("%04x", ch).toLowerCase());

        if (chstr != null && chstr.equals("apostrophe")) {
            return true;
        }
        return false;
    }

    public boolean isQuote(int ch) {
        String chstr = charCategory.get(String.format("%04x", ch).toLowerCase());

        if (chstr != null && chstr.equals("quote")) {
            return true;
        }
        return false;
    }

    public boolean isInterpunzione(String str) {
        //if (str.matches(PUNCTUATION_REGEX)) {
        if (Pattern.matches("\\p{Punct}", str) || isQuote(str.codePointAt(0)) ) {
            // if (charNormalize.containsKey(String.format("%04x", ch).toLowerCase())) {
            return true;
        }
        return false;
    }

    public boolean isCurrency(int ch) {
        String chstr = charCategory.get(String.format("%04x", ch).toLowerCase());

        if (chstr != null && chstr.equals("currency")) {
            return true;
        }
        return false;
    }


    public String normalize(int ch) {
        String chstr = String.format("%04x", ch).toLowerCase();
        if (charNormalize.containsKey(chstr)) {
            return charNormalize.get(chstr);
        }
        return hexToString(chstr);
    }


    /*
ABB_
CUR

CAP
PUN
UPP
DIG
LET
LOW

JLD
JLE
OTH

    OrthoFeature
    TokenType

     */
    public String getOrthoType (String str) {
        boolean isDigit = false;
        boolean isAlphaLow = false;
        boolean isAlphaUp = false;
        boolean isCurrency = false;

        for (int i=0;i<str.length();i++) {
            if (Character.isDigit(str.charAt(i))) {
                isDigit = true;
            } else if (Character.isUpperCase(str.charAt(i))) {
                isAlphaUp = true;
            } else if (Character.isLowerCase(str.charAt(i))) {
                isAlphaLow = true;
            } else if (isCurrency(str.codePointAt(i))) {
                isCurrency = true;
            }
        }

        //if (str.matches(EMAIL_REGEX))
        //    return "EMA";
        //else  if (str.matches(URL_REGEX))
        //    return "URL";
        //else
        //if (isCurrency)
        //    return "CUR";
        if (isAlphaUp || isAlphaLow) {
            //if (str.contains("."))
            //    return "ABB";
            //else
            if (isAlphaUp && !isAlphaLow)
                return "CAP";
            else if (!isAlphaUp && isAlphaLow)
                return "LOW";
            else
                return "UPP";

        } else if (str.matches(PUNCTUATION_REGEX))
            return "PUN";
        else if (isDigit)
            return "DIG";

        return "OTH";
    }

    //For French and English, get long form in case of abbreviation (e.g. jan for janvier) and add an apostroph if missing
    private String getLongForm (String nrml, String lang){
		nrml = convert_abbreviation(nrml, lang);
	
		if (nrml.matches("^((l)|(t)|(j)|(d)|(n)|(qu))$")){
			nrml += "'";
		}
		return nrml;
    }

    private String convert_abbreviation(String str, String lang){
		String norm="";
		if (listAbbr.containsKey(str.toLowerCase())){
		    norm = listAbbr.get(lang).get(str.toLowerCase());
		}
		else{
		    norm = str;
		}
		return norm;
    }

    private Hashtable<String,String> read_abbr_file (String fileName) throws IOException{
    	Hashtable<String,String> hash = new Hashtable<String,String>();
		FileReader fr = new FileReader(fileName);
		BufferedReader br=new BufferedReader(fr);
		String line;
		while ((line=br.readLine())!=null){
		    if (line.contains("\t")){
				String [] elts = line.split("\t");
				hash.put(elts[0], elts[1]);
		    }
		}
		return hash;
    }

    public String normalize(String str, String lang) {
        String nrml = "";
        
        lang = lang.substring(0,3);
        
        if(lang.equalsIgnoreCase("ita") || lang.equalsIgnoreCase("eng")){
        	nrml = Normalizer.normalize(str, Normalizer.Form.NFD);
        }
        else if(lang.equalsIgnoreCase("fre")){
        	nrml = Normalizer.normalize(str, Normalizer.Form.NFC);
        } 
        
        if (lang.equalsIgnoreCase("fre") || lang.equalsIgnoreCase("eng")) {
        	nrml = getLongForm(nrml, lang);
        }
	
        String strnorm = "";
        for (int i=0;i<nrml.length();i++) {
            if (isCurrency(nrml.codePointAt(i)))
                strnorm += "_";
            else
                strnorm += normalize(nrml.codePointAt(i));
            //System.err.println(nrml.charAt(i) + " - \\u" + String.format("%04x", nrml.codePointAt(i)));
        }

        return strnorm;
    }

    /**
     *   encodes a string of 4-digit hex numbers to unicode
     *   <br />
     *   @param hex string of 4-digit hex numbers
     *   @return normal java string
     */
    public static final String hexToString ( String hex ) {
        if ( hex == null || hex.length() > 4) return "";
        try {
            return String.valueOf((char) (Integer.parseInt( hex, 16)));
        }
        catch ( NumberFormatException NF_Ex) { /* dont care*/ }

        return "";
    }


    public static void main(String args[]) throws IOException {
        NormalizeText normalizer = new NormalizeText();
        InputStreamReader in = new InputStreamReader(new FileInputStream(args[0]), "UTF8");
        BufferedReader bin = new BufferedReader (in);
        String line;
        while ((line = bin.readLine()) != null) {
            System.out.println(normalizer.normalize(line,"ita"));
        }
        in.close();
        bin.close();
    }


}
