package eu.fbk.textpro.wrapper.utility;

import eu.fbk.textpro.toolbox.TEXTPROCONSTANT;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.Element;

import java.util.*;
import java.io.*;

/**
 * User: cgirardi@fbk.eu
 * Date: 02-apr-2011
 * Time: 10.06.45
 *
 * ex: /research/hlt/cgirardi/bin/java/TextPro/txp2xml.sh -tokens -ein "ISO-8859-1" -eout "UTF8" -in /media/disk/hlt/Corpora/Adige/xmlFTP_clean/2008/ corpus/adige/2008/ corpus_xml/adige/2008/ > & /tmp/2008.log

 */
public class Txp2XML {
    static private String encodingOUT = "UTF8";
    static private String encodingIN = "UTF8";
    private HashMap<String, Integer> fieldsposition = new HashMap();
    private Vector<String> loccoords = new Vector();
    private static boolean saveTokens = true;
    static BufferedWriter buffout = null;
    static Format format = Format.getPrettyFormat();
    static XMLOutputter xml = null;


    static Map<String, Character> escapeStrings;

    static {
        // HTML character entity references as defined in HTML 4
        // see http://www.w3.org/TR/REC-html40/sgml/entities.html
        escapeStrings = new HashMap<String, Character>(252);

        escapeStrings.put("&nbsp;", new Character('\u00A0'));
        escapeStrings.put("&iexcl;", new Character('\u00A1'));
        escapeStrings.put("&cent;", new Character('\u00A2'));
        escapeStrings.put("&pound;", new Character('\u00A3'));
        escapeStrings.put("&curren;", new Character('\u00A4'));
        escapeStrings.put("&yen;", new Character('\u00A5'));
        escapeStrings.put("&brvbar;", new Character('\u00A6'));
        escapeStrings.put("&sect;", new Character('\u00A7'));
        escapeStrings.put("&uml;", new Character('\u00A8'));
        escapeStrings.put("&copy;", new Character('\u00A9'));
        escapeStrings.put("&ordf;", new Character('\u00AA'));
        escapeStrings.put("&laquo;", new Character('\u00AB'));
        escapeStrings.put("&not;", new Character('\u00AC'));
        escapeStrings.put("&shy;", new Character('\u00AD'));
        escapeStrings.put("&reg;", new Character('\u00AE'));
        escapeStrings.put("&macr;", new Character('\u00AF'));
        escapeStrings.put("&deg;", new Character('\u00B0'));
        escapeStrings.put("&plusmn;", new Character('\u00B1'));
        escapeStrings.put("&sup2;", new Character('\u00B2'));
        escapeStrings.put("&sup3;", new Character('\u00B3'));
        escapeStrings.put("&acute;", new Character('\u00B4'));
        escapeStrings.put("&micro;", new Character('\u00B5'));
        escapeStrings.put("&para;", new Character('\u00B6'));
        escapeStrings.put("&middot;", new Character('\u00B7'));
        escapeStrings.put("&cedil;", new Character('\u00B8'));
        escapeStrings.put("&sup1;", new Character('\u00B9'));
        escapeStrings.put("&ordm;", new Character('\u00BA'));
        escapeStrings.put("&raquo;", new Character('\u00BB'));
        escapeStrings.put("&frac14;", new Character('\u00BC'));
        escapeStrings.put("&frac12;", new Character('\u00BD'));
        escapeStrings.put("&frac34;", new Character('\u00BE'));
        escapeStrings.put("&iquest;", new Character('\u00BF'));
        escapeStrings.put("&Agrave;", new Character('\u00C0'));
        escapeStrings.put("&Aacute;", new Character('\u00C1'));
        escapeStrings.put("&Acirc;", new Character('\u00C2'));
        escapeStrings.put("&Atilde;", new Character('\u00C3'));
        escapeStrings.put("&Auml;", new Character('\u00C4'));
        escapeStrings.put("&Aring;", new Character('\u00C5'));
        escapeStrings.put("&AElig;", new Character('\u00C6'));
        escapeStrings.put("&Ccedil;", new Character('\u00C7'));
        escapeStrings.put("&Egrave;", new Character('\u00C8'));
        escapeStrings.put("&Eacute;", new Character('\u00C9'));
        escapeStrings.put("&Ecirc;", new Character('\u00CA'));
        escapeStrings.put("&Euml;", new Character('\u00CB'));
        escapeStrings.put("&Igrave;", new Character('\u00CC'));
        escapeStrings.put("&Iacute;", new Character('\u00CD'));
        escapeStrings.put("&Icirc;", new Character('\u00CE'));
        escapeStrings.put("&Iuml;", new Character('\u00CF'));
        escapeStrings.put("&ETH;", new Character('\u00D0'));
        escapeStrings.put("&Ntilde;", new Character('\u00D1'));
        escapeStrings.put("&Ograve;", new Character('\u00D2'));
        escapeStrings.put("&Oacute;", new Character('\u00D3'));
        escapeStrings.put("&Ocirc;", new Character('\u00D4'));
        escapeStrings.put("&Otilde;", new Character('\u00D5'));
        escapeStrings.put("&Ouml;", new Character('\u00D6'));
        escapeStrings.put("&times;", new Character('\u00D7'));
        escapeStrings.put("&Oslash;", new Character('\u00D8'));
        escapeStrings.put("&Ugrave;", new Character('\u00D9'));
        escapeStrings.put("&Uacute;", new Character('\u00DA'));
        escapeStrings.put("&Ucirc;", new Character('\u00DB'));
        escapeStrings.put("&Uuml;", new Character('\u00DC'));
        escapeStrings.put("&Yacute;", new Character('\u00DD'));
        escapeStrings.put("&THORN;", new Character('\u00DE'));
        escapeStrings.put("&szlig;", new Character('\u00DF'));
        escapeStrings.put("&agrave;", new Character('\u00E0'));
        escapeStrings.put("&aacute;", new Character('\u00E1'));
        escapeStrings.put("&acirc;", new Character('\u00E2'));
        escapeStrings.put("&atilde;", new Character('\u00E3'));
        escapeStrings.put("&auml;", new Character('\u00E4'));
        escapeStrings.put("&aring;", new Character('\u00E5'));
        escapeStrings.put("&aelig;", new Character('\u00E6'));
        escapeStrings.put("&ccedil;", new Character('\u00E7'));
        escapeStrings.put("&egrave;", new Character('\u00E8'));
        escapeStrings.put("&eacute;", new Character('\u00E9'));
        escapeStrings.put("&ecirc;", new Character('\u00EA'));
        escapeStrings.put("&euml;", new Character('\u00EB'));
        escapeStrings.put("&igrave;", new Character('\u00EC'));
        escapeStrings.put("&iacute;", new Character('\u00ED'));
        escapeStrings.put("&icirc;", new Character('\u00EE'));
        escapeStrings.put("&iuml;", new Character('\u00EF'));
        escapeStrings.put("&eth;", new Character('\u00F0'));
        escapeStrings.put("&ntilde;", new Character('\u00F1'));
        escapeStrings.put("&ograve;", new Character('\u00F2'));
        escapeStrings.put("&oacute;", new Character('\u00F3'));
        escapeStrings.put("&ocirc;", new Character('\u00F4'));
        escapeStrings.put("&otilde;", new Character('\u00F5'));
        escapeStrings.put("&ouml;", new Character('\u00F6'));
        escapeStrings.put("&divide;", new Character('\u00F7'));
        escapeStrings.put("&oslash;", new Character('\u00F8'));
        escapeStrings.put("&ugrave;", new Character('\u00F9'));
        escapeStrings.put("&uacute;", new Character('\u00FA'));
        escapeStrings.put("&ucirc;", new Character('\u00FB'));
        escapeStrings.put("&uuml;", new Character('\u00FC'));
        escapeStrings.put("&yacute;", new Character('\u00FD'));
        escapeStrings.put("&thorn;", new Character('\u00FE'));
        escapeStrings.put("&yuml;", new Character('\u00FF'));
        escapeStrings.put("&fnof;", new Character('\u0192'));
        escapeStrings.put("&Alpha;", new Character('\u0391'));
        escapeStrings.put("&Beta;", new Character('\u0392'));
        escapeStrings.put("&Gamma;", new Character('\u0393'));
        escapeStrings.put("&Delta;", new Character('\u0394'));
        escapeStrings.put("&Epsilon;", new Character('\u0395'));
        escapeStrings.put("&Zeta;", new Character('\u0396'));
        escapeStrings.put("&Eta;", new Character('\u0397'));
        escapeStrings.put("&Theta;", new Character('\u0398'));
        escapeStrings.put("&Iota;", new Character('\u0399'));
        escapeStrings.put("&Kappa;", new Character('\u039A'));
        escapeStrings.put("&Lambda;", new Character('\u039B'));
        escapeStrings.put("&Mu;", new Character('\u039C'));
        escapeStrings.put("&Nu;", new Character('\u039D'));
        escapeStrings.put("&Xi;", new Character('\u039E'));
        escapeStrings.put("&Omicron;", new Character('\u039F'));
        escapeStrings.put("&Pi;", new Character('\u03A0'));
        escapeStrings.put("&Rho;", new Character('\u03A1'));
        escapeStrings.put("&Sigma;", new Character('\u03A3'));
        escapeStrings.put("&Tau;", new Character('\u03A4'));
        escapeStrings.put("&Upsilon;", new Character('\u03A5'));
        escapeStrings.put("&Phi;", new Character('\u03A6'));
        escapeStrings.put("&Chi;", new Character('\u03A7'));
        escapeStrings.put("&Psi;", new Character('\u03A8'));
        escapeStrings.put("&Omega;", new Character('\u03A9'));
        escapeStrings.put("&alpha;", new Character('\u03B1'));
        escapeStrings.put("&beta;", new Character('\u03B2'));
        escapeStrings.put("&gamma;", new Character('\u03B3'));
        escapeStrings.put("&delta;", new Character('\u03B4'));
        escapeStrings.put("&epsilon;", new Character('\u03B5'));
        escapeStrings.put("&zeta;", new Character('\u03B6'));
        escapeStrings.put("&eta;", new Character('\u03B7'));
        escapeStrings.put("&theta;", new Character('\u03B8'));
        escapeStrings.put("&iota;", new Character('\u03B9'));
        escapeStrings.put("&kappa;", new Character('\u03BA'));
        escapeStrings.put("&lambda;", new Character('\u03BB'));
        escapeStrings.put("&mu;", new Character('\u03BC'));
        escapeStrings.put("&nu;", new Character('\u03BD'));
        escapeStrings.put("&xi;", new Character('\u03BE'));
        escapeStrings.put("&omicron;", new Character('\u03BF'));
        escapeStrings.put("&pi;", new Character('\u03C0'));
        escapeStrings.put("&rho;", new Character('\u03C1'));
        escapeStrings.put("&sigmaf;", new Character('\u03C2'));
        escapeStrings.put("&sigma;", new Character('\u03C3'));
        escapeStrings.put("&tau;", new Character('\u03C4'));
        escapeStrings.put("&upsilon;", new Character('\u03C5'));
        escapeStrings.put("&phi;", new Character('\u03C6'));
        escapeStrings.put("&chi;", new Character('\u03C7'));
        escapeStrings.put("&psi;", new Character('\u03C8'));
        escapeStrings.put("&omega;", new Character('\u03C9'));
        escapeStrings.put("&thetasym;", new Character('\u03D1'));
        escapeStrings.put("&upsih;", new Character('\u03D2'));
        escapeStrings.put("&piv;", new Character('\u03D6'));
        escapeStrings.put("&bull;", new Character('\u2022'));
        escapeStrings.put("&hellip;", new Character('\u2026'));
        escapeStrings.put("&prime;", new Character('\u2032'));
        escapeStrings.put("&Prime;", new Character('\u2033'));
        escapeStrings.put("&oline;", new Character('\u203E'));
        escapeStrings.put("&frasl;", new Character('\u2044'));
        escapeStrings.put("&weierp;", new Character('\u2118'));
        escapeStrings.put("&image;", new Character('\u2111'));
        escapeStrings.put("&real;", new Character('\u211C'));
        escapeStrings.put("&trade;", new Character('\u2122'));
        escapeStrings.put("&alefsym;", new Character('\u2135'));
        escapeStrings.put("&larr;", new Character('\u2190'));
        escapeStrings.put("&uarr;", new Character('\u2191'));
        escapeStrings.put("&rarr;", new Character('\u2192'));
        escapeStrings.put("&darr;", new Character('\u2193'));
        escapeStrings.put("&harr;", new Character('\u2194'));
        escapeStrings.put("&crarr;", new Character('\u21B5'));
        escapeStrings.put("&lArr;", new Character('\u21D0'));
        escapeStrings.put("&uArr;", new Character('\u21D1'));
        escapeStrings.put("&rArr;", new Character('\u21D2'));
        escapeStrings.put("&dArr;", new Character('\u21D3'));
        escapeStrings.put("&hArr;", new Character('\u21D4'));
        escapeStrings.put("&forall;", new Character('\u2200'));
        escapeStrings.put("&part;", new Character('\u2202'));
        escapeStrings.put("&exist;", new Character('\u2203'));
        escapeStrings.put("&empty;", new Character('\u2205'));
        escapeStrings.put("&nabla;", new Character('\u2207'));
        escapeStrings.put("&isin;", new Character('\u2208'));
        escapeStrings.put("&notin;", new Character('\u2209'));
        escapeStrings.put("&ni;", new Character('\u220B'));
        escapeStrings.put("&prod;", new Character('\u220F'));
        escapeStrings.put("&sum;", new Character('\u2211'));
        escapeStrings.put("&minus;", new Character('\u2212'));
        escapeStrings.put("&lowast;", new Character('\u2217'));
        escapeStrings.put("&radic;", new Character('\u221A'));
        escapeStrings.put("&prop;", new Character('\u221D'));
        escapeStrings.put("&infin;", new Character('\u221E'));
        escapeStrings.put("&ang;", new Character('\u2220'));
        escapeStrings.put("&and;", new Character('\u2227'));
        escapeStrings.put("&or;", new Character('\u2228'));
        escapeStrings.put("&cap;", new Character('\u2229'));
        escapeStrings.put("&cup;", new Character('\u222A'));
        escapeStrings.put("&int;", new Character('\u222B'));
        escapeStrings.put("&there4;", new Character('\u2234'));
        escapeStrings.put("&sim;", new Character('\u223C'));
        escapeStrings.put("&cong;", new Character('\u2245'));
        escapeStrings.put("&asymp;", new Character('\u2248'));
        escapeStrings.put("&ne;", new Character('\u2260'));
        escapeStrings.put("&equiv;", new Character('\u2261'));
        escapeStrings.put("&le;", new Character('\u2264'));
        escapeStrings.put("&ge;", new Character('\u2265'));
        escapeStrings.put("&sub;", new Character('\u2282'));
        escapeStrings.put("&sup;", new Character('\u2283'));
        escapeStrings.put("&nsub;", new Character('\u2284'));
        escapeStrings.put("&sube;", new Character('\u2286'));
        escapeStrings.put("&supe;", new Character('\u2287'));
        escapeStrings.put("&oplus;", new Character('\u2295'));
        escapeStrings.put("&otimes;", new Character('\u2297'));
        escapeStrings.put("&perp;", new Character('\u22A5'));
        escapeStrings.put("&sdot;", new Character('\u22C5'));
        escapeStrings.put("&lceil;", new Character('\u2308'));
        escapeStrings.put("&rceil;", new Character('\u2309'));
        escapeStrings.put("&lfloor;", new Character('\u230A'));
        escapeStrings.put("&rfloor;", new Character('\u230B'));
        escapeStrings.put("&lang;", new Character('\u2329'));
        escapeStrings.put("&rang;", new Character('\u232A'));
        escapeStrings.put("&loz;", new Character('\u25CA'));
        escapeStrings.put("&spades;", new Character('\u2660'));
        escapeStrings.put("&clubs;", new Character('\u2663'));
        escapeStrings.put("&hearts;", new Character('\u2665'));
        escapeStrings.put("&diams;", new Character('\u2666'));
        escapeStrings.put("&quot;", new Character('\u0022'));
        escapeStrings.put("&amp;", new Character('\u0026'));
        escapeStrings.put("&lt;", new Character('\u003C'));
        escapeStrings.put("&gt;", new Character('\u003E'));
        escapeStrings.put("&OElig;", new Character('\u0152'));
        escapeStrings.put("&oelig;", new Character('\u0153'));
        escapeStrings.put("&Scaron;", new Character('\u0160'));
        escapeStrings.put("&scaron;", new Character('\u0161'));
        escapeStrings.put("&Yuml;", new Character('\u0178'));
        escapeStrings.put("&circ;", new Character('\u02C6'));
        escapeStrings.put("&tilde;", new Character('\u02DC'));
        escapeStrings.put("&ensp;", new Character('\u2002'));
        escapeStrings.put("&emsp;", new Character('\u2003'));
        escapeStrings.put("&thinsp;", new Character('\u2009'));
        escapeStrings.put("&zwnj;", new Character('\u200C'));
        escapeStrings.put("&zwj;", new Character('\u200D'));
        escapeStrings.put("&lrm;", new Character('\u200E'));
        escapeStrings.put("&rlm;", new Character('\u200F'));
        escapeStrings.put("&ndash;", new Character('\u2013'));
        escapeStrings.put("&mdash;", new Character('\u2014'));
        escapeStrings.put("&lsquo;", new Character('\u2018'));
        escapeStrings.put("&rsquo;", new Character('\u2019'));
        escapeStrings.put("&sbquo;", new Character('\u201A'));
        escapeStrings.put("&ldquo;", new Character('\u201C'));
        escapeStrings.put("&rdquo;", new Character('\u201D'));
        escapeStrings.put("&bdquo;", new Character('\u201E'));
        escapeStrings.put("&dagger;", new Character('\u2020'));
        escapeStrings.put("&Dagger;", new Character('\u2021'));
        escapeStrings.put("&permil;", new Character('\u2030'));
        escapeStrings.put("&lsaquo;", new Character('\u2039'));
        escapeStrings.put("&rsaquo;", new Character('\u203A'));
        escapeStrings.put("&euro;", new Character('\u20AC'));
    }

    static List escapeCharacters;
    static {escapeCharacters = new ArrayList(escapeStrings.values()); }
    static List escapeEntities;
    static {escapeEntities = new ArrayList(escapeStrings.keySet()); }

    public static void main(String[] args) throws Exception {

        Txp2XML txp2xml = new Txp2XML();
        if (args.length == 0) {
            System.err.println("java  eu.fbk.textpro.output.Txp2XML [-tokens] [-ein encoding] [-eout encoding] [-in TextPro input file or dir] <txp file or dir> [XML output dir]\n\nFor default:\n-ein is "+encodingIN+"\n-eout is "+encodingOUT);
        } else if (args.length > 0) {
            File txp_IN = null;
            File xml_OUT = null;

            for (int i=0; i<args.length; i++) {
                if (args[i].equalsIgnoreCase("-tokens"))
                    saveTokens = false;
                else if (args[i].equals("-ein"))
                    encodingIN =  args[++i];
                else if (args[i].equals("-eout"))
                    encodingOUT =  args[++i];
                else if (txp_IN == null)
                    txp_IN = new File(args[i]);
                else {
                    xml_OUT = new File(args[i]);
                    if (!xml_OUT.isDirectory())
                        xml_OUT.mkdirs();
                }

            }

            format.setEncoding(encodingOUT);
            xml = new XMLOutputter(format);

            if (xml_OUT == null)
                xml_OUT = txp_IN.getParentFile();

            File out;
            if (txp_IN.isDirectory()) {
                File[] files = txp_IN.listFiles();
                for (int i=0; i<files.length; i++) {
                    if (files[i].isFile()) {
                        out = new File(xml_OUT,files[i].getName() +".xml");

                        System.err.println("Input file: " + txp_IN);
                        System.err.println("Saved els: " +txp2xml.getXML (files[i], out) + " ("+out+")");
                    }
                }
            } else {
                out = new File(xml_OUT,txp_IN.getName() +".xml");

                System.err.println("Input file: " + txp_IN);
                System.err.println("Saved els: " + txp2xml.getXML(txp_IN, out)+ " ("+out +")");
            }
        }
    }

    //txp_IN è il file che è stato elaborato da TextPro (se passo questo file voglio che il body sia soltanto il testo originale
    //txp è l'output di TextPro
    public int getXML (File txp_IN, File xml_OUT) throws Exception {

        String header = "<?xml version=\"1.0\" encoding=\"" + encodingOUT +"\"?>\n<xml>\n";
        if (xml_OUT != null && xml_OUT.exists()) {
            System.err.println("WARNING! The output "+xml_OUT +" is already exists.");
            return 0;
        }

        if (xml_OUT == null)
            buffout = new BufferedWriter(new OutputStreamWriter(System.out));
        else
            buffout = new BufferedWriter(new OutputStreamWriter
                    (new FileOutputStream(xml_OUT,false),encodingOUT));
        buffout.write(header);


        String lang;

        String[] posloc;
        String url= null, urlIN = null;
        StringBuffer text = new StringBuffer();
        String[] fields = null;
        String[] cols;
        int currentPosition = 0;
        int prev_token_lenght = 0;
        boolean prev_is_eos = false;

        Element doc = new org.jdom2.Element("doc");
        Element elText = new org.jdom2.Element("text");
        Element elTokens = new org.jdom2.Element("tokens");
        Element mentions = new org.jdom2.Element("mentions");
        Element elChunks = new org.jdom2.Element("chunks");
        Element elMention = null, elTmxMention =null, elChunk=null;
        Element el;
        int counter = 0;
        InputStreamReader inputStream = new InputStreamReader(new FileInputStream (txp_IN), encodingIN);
        BufferedReader buff = new BufferedReader(inputStream);
        String line = null;



        int linenum = -1;
        try {
            while ((line = buff.readLine()) != null) {
                linenum++;
                if (line.startsWith("# ")) {
                    if (line.startsWith("# LANG:")) {
                        //add doc to xml
                        //List langs = WebParser.languageDetection(text.toString().getBytes("UTF8"));
                        //if (langs.size() > 0)
                        //  doc.setAttribute("lang",((String) langs.get(0)).replaceFirst("[-|\\.].*$",""));
                        lang = line.replaceFirst("# LANG:\\s*","").trim();
                        doc.setAttribute("lang",lang);

                    } else if (line.startsWith("# FILE:")) {
                        currentPosition = 0;
                        if (url != null) {
                            //add last mention
                            if (elMention != null) {
                                mentions.addContent(elMention);
                                elMention = null;
                            }
                            if (elTmxMention != null) {
                                //add last time mention
                                mentions.addContent(elTmxMention);
                                elTmxMention = null;
                            }
                            if (elChunk != null) {
                                //add last time mention
                                elChunks.addContent(elChunk);
                                elChunk = null;
                            }


                            MentionsLocalCoref(mentions);
                            doc.addContent(mentions);

                            mentions = new org.jdom2.Element("mentions");
                            if (text.length() > 0) {
                                elText.setText(text.toString());
                                text.setLength(0);
                            }
                            doc.addContent(elText);
                            if (elChunks.getChildren().size() > 0) {
                                doc.addContent(elChunks);
                            }
                            if (saveTokens && elTokens.getChildren().size() > 0)
                                doc.addContent(elTokens);

                            //xmlresult.addContent(doc);
                            buffout.write(xml.outputString(doc)+"\n");
                        }

                        doc = new org.jdom2.Element("doc");
                        elText = new org.jdom2.Element("text");
                        elTokens = new org.jdom2.Element("tokens");
                        elChunks = new org.jdom2.Element("chunks");
                        url = line.replaceFirst("# FILE:\\s*","").trim();
                        counter++;
                        //System.err.println("# "+ counter +". " +url);
                        doc.setAttribute("url",url);

                        text.setLength(0);

                    } else if (line.startsWith("# KEYWORDS:")) {
                        String[] keys = line.replaceFirst("# KEYWORDS:\\s*","").split(" *[<|>] *");
                        Element elkeys = new org.jdom2.Element("keywords");
                        for (int i=0; i<keys.length; i=i+2) {
                            el = new org.jdom2.Element("keyword");
                            el.setAttribute("score",keys[i+1].replaceAll(" .*",""));
                            el.setAttribute("freq",keys[i+1].replaceAll(".* ",""));
                            el.setText(keys[i]);
                            elkeys.addContent(el);

                        }
                        doc.addContent(elkeys);


                    } else if (line.startsWith("# FIELDS:")) {
                        fieldsposition.clear();
                        fields = line.replaceFirst("# FIELDS:\\s*","").split("\t");
                        for (int i=0; i< fields.length; i++) {
                            //if (mntposition == 0) {  se c'è mention prendo quella al posto di entity
                            //System.err.println(fields[i] + "\n");
                            fieldsposition.put(fields[i], i);
                        }
                        fields = line.replaceFirst("^[^\t]+\t","").split("\t");

                    } else {
                        //other header fields
                        if (line.matches("^# .+:.*$")) {
                            String elname = line.substring(0,line.indexOf(":")).replaceFirst("#\\s*","").toLowerCase();
                            if (elname.length() > 0) {
                                Element elheader = new Element(elname);
                                elheader.setText(line.substring(line.indexOf(":")+1).trim());

                                doc.addContent(elheader);
                            } else {
                                System.err.println("WARN! Meta info is missed in (line:"+linenum+", file:"+url+")");
                            }
                        } else {
                            System.err.println("WARN! Meta info is missed in (line:"+linenum+", file:"+url+")");
                        }
                    }
                } else if (line.length() > 0) {
                    cols = line.split("\t");
                    if (fieldsposition.get("tokenstart") != null) {
                        //System.err.println((currentPosition + prev_token_lenght) + " != " + cols[fieldsposition.get("tokenstart")] + " " +cols[fieldsposition.get("token")]);

                        if ((currentPosition + prev_token_lenght) < Integer.valueOf(cols[fieldsposition.get("tokenstart")])) {
                            if (!prev_is_eos)
                                text.append(" ");
                            else {
                                if ((Integer.valueOf(cols[fieldsposition.get("tokenstart")]) - currentPosition - prev_token_lenght) >= 2)
                                    text.append("\n");
                                else
                                    //while ((currentPosition + prev_token_lenght) <= Integer.valueOf(cols[fieldsposition.get("tokenstart")])) {
                                    //    currentPosition = currentPosition+2;
                                    text.append(" ");
                                //}
                            }
                            prev_is_eos = false;
                        }
                        text.append(cols[fieldsposition.get("token")]);
                        currentPosition = Integer.valueOf(cols[fieldsposition.get("tokenstart")]);
                        prev_token_lenght = cols[fieldsposition.get("token")].length();
                        if (line.contains("\t<eos>")) {
                            prev_is_eos = true;
                        }

                    }

                    if (fieldsposition.get("entity") != null) {
                        //if (!cols[fieldsposition.get("entity")].contains("_NOM")
                        //       && !cols[fieldsposition.get("entity")].contains("_PRO"))
                        if (cols[fieldsposition.get("entity")].startsWith("B-")) {
                            if (elMention != null) {
                                //add previuos mention
                                mentions.addContent(elMention);
                            }
                            elMention = new org.jdom2.Element("extent");

                            String type=cols[fieldsposition.get("entity")].replaceFirst("B-","").replaceAll("@.*","").toLowerCase();

                            elMention.setText(cols[0]);

                            if (type.length() > 1) {
                                elMention.setAttribute("type",type.replaceFirst("[@|_].*",""));
                            }
                            if (cols[fieldsposition.get("entity")].contains("_NOM"))
                                elMention.setAttribute("level","NOM");
                            else if (cols[fieldsposition.get("entity")].contains("_PRO"))
                                elMention.setAttribute("level","PRO");
                            else
                                elMention.setAttribute("level","NAM");

                            if (fieldsposition.get("tokenstart") != null) {
                                elMention.setAttribute("start",cols[fieldsposition.get("tokenstart")]);
                                elMention.setAttribute("end",String.valueOf(Integer.parseInt(cols[fieldsposition.get("tokenstart")]) + cols[0].length()));
                            }

                            if (fieldsposition.get("trigger") != null && cols.length>fieldsposition.get("trigger")) {
                                //System.err.println(fieldsposition.get("trigger")+","+cols.length+" "+line);
                                String[] triggers = cols[fieldsposition.get("trigger")].split("<.>");
                                String trigger = "";
                                for (String tr : triggers) {
                                    if (tr.length() > 1 && tr.trim().length() > trigger.length()) {
                                        trigger = tr.trim();
                                    }
                                }
                                if (trigger.length() > 0)
                                    elMention.setAttribute("triggers",trigger);
                            }

                            if (fieldsposition.get("starttime") != null && cols.length>fieldsposition.get("starttime")) {
                                elMention.setAttribute("starttime",cols[fieldsposition.get("starttime")]);
                            }

                        } else if (elMention != null && cols[fieldsposition.get("entity")].startsWith("I-")) {
                            elMention.setText(elMention.getText() +" " + cols[0]);
                            if (fieldsposition.get("tokenstart") != null) {
                                elMention.setAttribute("end", String.valueOf(Integer.parseInt(cols[fieldsposition.get("tokenstart")]) + cols[0].length()));
                            }

                        }
                        if (elMention != null && fieldsposition.get("endtime") != null && cols.length>fieldsposition.get("endtime")) {
                            elMention.setAttribute("endtime",cols[fieldsposition.get("endtime")]);
                        }

                        if (fieldsposition.get("geoinfo") != null && cols.length>fieldsposition.get("geoinfo")) {
                            posloc = cols[fieldsposition.get("geoinfo")].split("@@");
                            if (posloc.length > 1) {
                                String locent = posloc[1];
                                String clustID = posloc[0];

                                posloc = clustID.split(",");
                                if (posloc[0].equalsIgnoreCase("0")) {
                                    if (loccoords.contains(locent))
                                        loccoords.add(locent);
                                    clustID = "locID" +locent.hashCode();
                                } else
                                    clustID = posloc[0];

                                if (locent.length() > 0) {
                                    elMention.setAttribute("geoinfo",locent);
                                    elMention.setAttribute("globalcoref",clustID);
                                }
                                //DEBUG
                                //System.err.println(line +"\nloc"+posloc[0]+"] "+  posloc[1].replaceFirst(";.+$","") +" ["+locent+"]");
//getMentionIDS
                            }
                        }

                        if (fieldsposition.get("wikiinfo") != null &&
                                cols.length>fieldsposition.get("wikiinfo")) {
                            //System.err.println(cols[fieldsposition.get("wikiinfo")]);
                            if (cols[fieldsposition.get("wikiinfo")].length() > 1) {
                                elMention.setAttribute("wikiurl",cols[fieldsposition.get("wikiinfo")]);
                            }
                        }
                    }
                    if (fieldsposition.get("chunk") != null) {
                        if (cols[fieldsposition.get("chunk")].startsWith("B-NP")) {
                            if (elChunk != null) {
                                //add previuos mention
                                elChunks.addContent(elChunk);
                            }
                            elChunk = new org.jdom2.Element("chunk");
                            elChunk.setText(cols[0]);
                            elChunk.setAttribute("type","NP");

                        } else if (elChunk != null && cols[fieldsposition.get("chunk")].startsWith("I-NP")) {
                            elChunk.setText(elChunk.getText() +" " + cols[0]);
                        }
                    }
                    if (fieldsposition.get("timex") != null) {
                        if (cols[fieldsposition.get("timex")].startsWith("B-")) {
                            if (elTmxMention != null) {
                                //add previuos mention
                                mentions.addContent(elTmxMention);
                            }

                            elTmxMention = new org.jdom2.Element("extent");
                            elTmxMention.setText(cols[0]);
                            elTmxMention.setAttribute("type","time");

                            if (fieldsposition.get("tokenstart") != null) {
                                elTmxMention.setAttribute("start",cols[fieldsposition.get("tokenstart")]);
                                elTmxMention.setAttribute("end",String.valueOf(Integer.parseInt(cols[fieldsposition.get("tokenstart")]) + cols[0].length()));
                            }
                            if (fieldsposition.get("starttime") != null && cols.length>fieldsposition.get("starttime")) {
                                elTmxMention.setAttribute("starttime",cols[fieldsposition.get("starttime")]);
                            }

                            elTmxMention.setAttribute("value",cols[fieldsposition.get("timex")].replaceFirst(".+@","").trim());

                        } else if (elTmxMention != null && cols[fieldsposition.get("timex")].startsWith("I-")) {
                            elTmxMention.setText(elTmxMention.getText() +" " + cols[0]);
                            if (fieldsposition.get("tokenstart") != null) {
                                elTmxMention.setAttribute("end", String.valueOf(Integer.parseInt(cols[fieldsposition.get("tokenstart")]) + cols[0].length()));
                            }
                        }
                        if (elTmxMention != null && fieldsposition.get("endtime") != null && cols.length>fieldsposition.get("endtime")) {
                            elTmxMention.setAttribute("endtime",cols[fieldsposition.get("endtime")]);
                        }
                    }


                    //mancano "chunck" e parser
                    if (saveTokens && fields != null) {
                        el = new org.jdom2.Element("w");
                        Element elOrtho = new org.jdom2.Element("token");
                        Element elMorpho = null;

                        elOrtho.setText(cols[0]);
                        for (String field : fields) {
                            //System.err.println(field);

                            if (fieldsposition.get(field) >= cols.length ||
                                    cols[fieldsposition.get(field)].length() <= 0 ||
                                    cols[fieldsposition.get(field)].equals(TEXTPROCONSTANT.NULL))
                                continue;
                            if (field.contains("sentence") && cols[fieldsposition.get("sentence")].contains("eos"))
                                el.setAttribute("eos","true");
                            else if (" starttime endtime ".contains(field)) {
                                elOrtho.setAttribute(field,cols[fieldsposition.get(field)]);
                            } else if (" tokenid tokenstart tokenend ".contains(field)) {
                                //System.err.println("# " + field.replaceFirst("token","") + " " +fieldsposition.get(field));
                                elOrtho.setAttribute(field.replaceFirst("token",""),cols[fieldsposition.get(field)]);
                            } else if (" lemma pos wnpos comp_morpho ".contains(field)) {
                                if (elMorpho == null)
                                    elMorpho = new org.jdom2.Element("morpho");

                                if (field.equalsIgnoreCase("lemma")) {
                                    Element lemma = new org.jdom2.Element("lemma");
                                    lemma.setText(cols[fieldsposition.get(field)]);
                                    elMorpho.addContent(lemma);
                                } else if (field.equalsIgnoreCase("comp_morpho"))
                                    addMorphoFeatures(elMorpho,cols[fieldsposition.get(field)]);
                                else
                                    elMorpho.setAttribute(field,cols[fieldsposition.get(field)]);
                            } else if (field.equals("full_morpho")) {
                                Element elTmp = new org.jdom2.Element(field);
                                elTmp.setText(cols[fieldsposition.get(field)]);
                                el.addContent(elTmp);
                            }

                        }
                        el.addContent(elOrtho);

                        if (elMorpho != null)
                            el.addContent(elMorpho);

                        elTokens.addContent(el);
                    }
                } else {
                    text.append("\n");
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR! "+e.getMessage() + " ("+linenum+ ") line: " +line);
            e.printStackTrace();
        }

        inputStream.close();
        if (text.length() > 0) {
            elText.setText(text.toString());
            text.setLength(0);
        }
        //add last mention
        if (elMention != null) {
            mentions.addContent(elMention);
        }
        if (elTmxMention != null) {
            //add last time mention
            mentions.addContent(elTmxMention);
        }
        if (elChunk != null) {
            //add last time mention
            elChunks.addContent(elChunk);
        }

        if (url != null) {
            MentionsLocalCoref(mentions);
            doc.addContent(mentions);
            doc.addContent(elText);
            if (elChunks.getChildren().size() > 0)
                doc.addContent(elChunks);
            if (saveTokens && elTokens.getChildren().size() > 0)
                doc.addContent(elTokens);

            //xmlresult.addContent(doc);
            if (xml == null) {
                format.setEncoding(encodingOUT);
                xml = new XMLOutputter(format);

            }
            buffout.write(xml.outputString(doc));
        }

        buffout.write("\n</xml>\n");
        buffout.close();
        return counter;
        //"<?xml version=\"1.0\" encoding=\"" + encoding +"\"?>\n" +
        //        xml.outputString(xmlresult);
    }

    private static void MentionsLocalCoref (Element mentions) {
        List<Element> imention = mentions.getChildren();
        Vector<String> mmentions = new Vector();
        for (Element mnt : imention) {

            if (!mnt.getAttributeValue("type").startsWith("time")) {
                //System.err.println(mnt.getAttributeValue("type") + " MNT: " + mnt.getText());
                mmentions.add(mnt.getAttributeValue("type") + " " +mnt.getText());
            }
        }
        Vector result = getLocalCoreference(mmentions);
        //System.err.println(result);
        int i = 0;
        for (Element mnt : imention) {
            if (!mnt.getAttributeValue("type").startsWith("time")) {
                mnt.setAttribute("localcoref",String.valueOf((Integer) result.get(i) +1));
                i++;
            }
        }
    }


    public static Vector getLocalCoreference (Vector mentions) {
        Vector clusters = new Vector();
        for (int i=0; i<mentions.size(); i++) {
            if (i == 0) {
                clusters.add(new Integer(0));
                continue;
            }

            //Tengo traccia della prima mention compatibile, nel caso ci fosse notCompCluster che rimuove tutti i compCluster
            boolean found = false;
            List notCompCluster = new ArrayList();
            List compCluster = new ArrayList();
            for (int m=i-1; m>=0; --m) {
                //se c'� compatibilit� faccio backtrack per vedere se � l'unico assegnamento altrimenti
                int tokenComp = tokenComparable(getMentionWordsReference((String) mentions.get(m)), getMentionWordsReference(((String) mentions.get(i))));
                //System.err.println(tokenComp + " " +m+". " + mentions.get(m) + " -- " +i+". "+ mentions.get(i));
                if (tokenComp == 1) {
                    if (!compCluster.contains(clusters.get(m)) )
                        compCluster.add(clusters.get(m));
                } else if (!notCompCluster.contains(clusters.get(m))) {
                    notCompCluster.add(clusters.get(m));
                }

            }
            compCluster.removeAll(notCompCluster);
            if (compCluster.size() > 0) {
                clusters.add((Integer) compCluster.get(0));
            } else {
                clusters.add(new Integer(i));

            }
        }

        if (clusters.size() == mentions.size()) {
            Integer clusterId;
            List other_clusters = new ArrayList();
            for (int i=0; i<mentions.size(); i++) {
                other_clusters.clear();
                clusterId = (Integer) clusters.get(i);
                for (int c=0; c<mentions.size(); c++) {
                    if (tokenComparable(getMentionWordsReference((String) mentions.get(c)),getMentionWordsReference(((String) mentions.get(i)))) == 1) {
                        //System.err.println("\ntokenComparable: " + Reference.getMentionWordsReference((String) mentions.get(c)) +" -- "+Reference.getMentionWordsReference(((String) mentions.get(i))));
                        if (clusters.get(c) != clusterId && !other_clusters.contains(clusters.get(c))) {
                            other_clusters.add(clusters.get(c));
                        }
                    }

                }
                //System.err.println(mentions.get(i) + " " + other_clusters);
                if (other_clusters.size() == 1) {
                    clusters.set(i, other_clusters.get(0));
                }
            }

        }
        //printCluster(doCluster(mentions,clusters));
        //System.err.println(mentions+"\n"+clusters);

        return clusters;
    }


    static public String getMentionReference (String text) {
        return getMentionWordsReference(text).replaceAll("\\s+","_");
    }

    static public String getMentionWordsReference (String text) {
        return toLowercaseASCIINoAccent(text);
    }

    public static String toLowercaseASCIINoAccent(String txt) {
        if (txt == null) {
            return null;
        }

        String txtLower = txt.trim().toLowerCase();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < txtLower.length(); i++) {
            char c = txtLower.charAt(i);
            int charpos = escapeCharacters.indexOf(c);
            if (charpos > 0){
                String entchar = (String) escapeEntities.get(charpos);
                if (" grave; acute; uml; tilde; circ; ring; szlig; slash; cedil; elig;".contains(" " +entchar.substring(2)))
                    sb.append(entchar.substring(1,2).toLowerCase());
                else
                    sb.append("x");

            }
            else {
                sb.append(c);
            }
        }
        return sb.toString();
    }


    private static String[] tokenized (String str) {
        if (str == null)
            return "".split("");
        str = str.replaceAll("[\\.|']","");
        return str.split("\\s+");

//return str.split("[\\s|\\-|\'|\\.]");
    }

    //controllo che tutti i token della stringa pi? piccola siano contenuti nella string pi? grande
    public static int tokenComparable (String str1, String str2) {
        String[] s1 = tokenized(str1);
        String[] s2 = tokenized(str2);


/*
for (int i=0; i<s1.length; i++) {
    System.err.println("##1 " + s1[i]);
}
for (int i=0; i<s2.length; i++) {
    System.err.println("##2 " + s2[i]);
}
*/
        int matching = 0;
        if (s1.length > s2.length) {
            String[] tmp = s1;
            s1 = s2;
            s2 = tmp;
        }
        for (int i=0; i<s1.length; i++) {
            //  && noMntToken.indexOf(" " + s1[i].toLowerCase() + " ") < 0
            if (s1[i].length() > 0) {
                int countmatch = 0;
                int countfound = 0;
                boolean found = false;
                for (int j=0; j<s2.length; j++) {
                    //inserire solo quelle che non sono stopword
                    // && noMntToken.indexOf(" " + s2[j].toLowerCase() + " ") < 0
                    if (s2[j].length() > 0) {
                        countmatch++;
//System.err.print("tC ->" + s1[i] + " - " + s2[j]);
//confronto due token solo se uno o ? di un carattere
                        if (s2[j].endsWith("&NAM") && s1[i].endsWith("&NAM")) {
                            //s2[j].length() == 1 || s1[i].length() == 1 ||
                            if (s1[i].replaceAll("&NAM","").equalsIgnoreCase(s2[j].replaceAll("&NAM",""))) {
                                //if (s2[j].indexOf(s1[i].replaceAll("&NAM","")) == 0 || s1[i].indexOf(s2[j].replaceAll("&NAM","")) == 0) {
                                countfound++;
                                found=true;
                            }
                            //}
                        } else {
                            if (//s2[j].indexOf(s1[i]) == 0 ||
                                //  s1[i].indexOf(s2[j]) == 0 ||
                                    s1[i].replaceAll("&.*$","").equalsIgnoreCase(s2[j].replaceAll("&.*$",""))) {
                                //System.err.println(str1+"/"+str2+" : "+s1[i] + " -- " +s2[j]);
                                countfound++;
                                found=true;

                            }
                        }
                        //s2[j].endsWith(s1[i].substring(s1[i].lastIndexOf("&"))
                        //System.err.println(" " + found);
                    }
                    if (str2.startsWith("org ") && s2.length > 2) {
                        String initials = "";
                        for (String word : s2) {
                            initials += word.charAt(0);
                        }
                        if (initials.substring(1).equalsIgnoreCase(str1.replaceFirst("org ","").replaceAll("[^\\d|\\w]",""))) {
                            found = true;
                        }
                    }
                    //if (s2[j].startsWith(s1[i]))
                    //  found = true;



                }
                if (countmatch > 0 && !found) {
                    return 0;
                }

            }

        }

        return 1;

    }



    private static void addMorphoFeatures (Element el, String morphoAnalisy) {
        String[] split_analize = morphoAnalisy.split("[\\+|\\~]");
        int start_parser = 0;
        if (morphoAnalisy.contains("~")) {
            start_parser = 1;
            //form = split_analize[0];
        }
        String value, pos = "";
        for (int i=start_parser; i<split_analize.length; i++) {
            value = split_analize[i];
            if (value.equalsIgnoreCase("nil")) {
                value = "";
            }
            if (value.length() > 0 && !value.equalsIgnoreCase("_")) {
                if (i == start_parser) {
                    //lemma = value;
                } else if (i == (start_parser + 1)) {
                    pos = value;
                    if (value.equals("adj")) {
                        pos = "a";
                    } else if (value.equals("adv")) {
                        pos = "r";
                    }
                    el.setAttribute("tag", pos);
                }


                if (i > (start_parser + 1)) {
                    if (pos.equals("v")) {
                        if (i == (start_parser + 2)) {
                            el.setAttribute("mood", value);
                        } else if (i == (start_parser + 3)) {
                            el.setAttribute("tense", value);
                        } else if (i == (start_parser + 4)) {
                            el.setAttribute("gender", value);
                        } else if (i == (start_parser + 5)) {
                            el.setAttribute("person", value);
                        } else if (i == (start_parser + 6)) {
                            el.setAttribute("number", value);
                        }
                    } else {
                        if (i == (start_parser + 2)) {
                            el.setAttribute("gender",value);
                        } else if (i == (start_parser + 3)) {
                            el.setAttribute("number", value);
                        }
                    }
                }
            }
        }
    }

}
