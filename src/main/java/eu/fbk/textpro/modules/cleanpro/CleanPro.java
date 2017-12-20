package eu.fbk.textpro.modules.cleanpro;

import net.htmlparser.jericho.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import eu.fbk.textpro.TextProModuleInterface;
import eu.fbk.textpro.toolbox.MYProperties;

/**
 * User: cgirardi@fbk.eu
 * Date: 28-giu-2011
 * Time: 14.18.44
 *
 * la strategia è quella di considerare il path dei tag: memorizzo il path che ha il
 * testo + lungo e cerco altri testi che hanno lo stesso path (questo dovrebbe funzionare per
 * pulire i blog
 */
public class CleanPro implements TextProModuleInterface {
    static String globalLang = "eng";

    private static final boolean DEBUG = true;

    //elimino commenti, tag sconosciuti, tag prog server (php, ..) e tutti i tag specificati
    //nella lista removeTag;
    private static final boolean FILTERTAG = true;
    private static final boolean SHOWESSENTIALTAG = false;

    //per il cinese uso ISO
    //static final Charset charset = Charset.forName("ISO-8859-1");
    //per l'inglese uso utf8
    private String encoding = "UTF8";

    private static StopWord stopword = new StopWord();
    
    private static File logFile = null;

    //lista dei tags che non creano separazione del testo (tipo p,td,div,...)
    private ArrayList styleTags = new ArrayList();

    //le strutture seguenti servono per calcolare la zona pi promettente
    private ArrayList borderTags = new ArrayList();

    //mantengo una lista dei tags da non considerare. Questo viene usato quado viene memorizzato il content
    // di un elemento che ha solo figli che modificano solo la visualizzazione del testo ma non lo scope
    private ArrayList avoidTags = new ArrayList();

    //remove these tags
    private ArrayList removeTag = new ArrayList();
    private Hashtable html = new Hashtable();


    public CleanPro(String lang) {
        String[] params = new String[1];
        params[0] = lang;

        init(params,null);
    }

    public void setEncoding(String encoding) {
        try {
            Charset ch = Charset.forName(encoding);

            if (ch != null)
                this.encoding = encoding;
        } catch (Exception e) {
            System.err.println("WARNING! " + e.getMessage() + " is an unsupported encoding.");
        }
    }

    public void init(String[] params,MYProperties prop) {
        globalLang = params[0];

        if (logFile != null && logFile.exists()) {
            try {
                logFile =  new File(logFile, "log.txt");
                logFile.delete();
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } else {
            logFile= null;
        }

        styleTags.add(HTMLElementName.B);
        styleTags.add(HTMLElementName.I);
        styleTags.add(HTMLElementName.U);
        styleTags.add(HTMLElementName.S);
        styleTags.add(HTMLElementName.FONT);
        styleTags.add(HTMLElementName.A);
        styleTags.add(HTMLElementName.IMG);
        styleTags.add(HTMLElementName.CENTER);
        styleTags.add(HTMLElementName.CITE);
        //styleTags.add(HTMLElementName.CODE);
        styleTags.add(HTMLElementName.EM);
        //styleTags.add(HTMLElementName.PRE);
        styleTags.add(HTMLElementName.STRONG);
        styleTags.add(HTMLElementName.SMALL);
        styleTags.add(HTMLElementName.BIG);
        styleTags.add(HTMLElementName.Q);

        borderTags.add(HTMLElementName.TABLE);
        borderTags.add(HTMLElementName.TT);
        borderTags.add(HTMLElementName.TD);
        borderTags.add(HTMLElementName.TH);
        borderTags.add(HTMLElementName.TR);
        borderTags.add(HTMLElementName.BODY);
        borderTags.add(HTMLElementName.DIV);
        borderTags.add(HTMLElementName.SPAN);
        borderTags.add(HTMLElementName.DD);
        borderTags.add(HTMLElementName.DL);
        borderTags.add(HTMLElementName.DT);
        borderTags.add(HTMLElementName.HR);
        borderTags.add(HTMLElementName.P);
        borderTags.add(HTMLElementName.LI);
        borderTags.add(HTMLElementName.UL);
        borderTags.add(HTMLElementName.OL);
        //borderTags.add(HTMLElementName.BR);
        borderTags.add(HTMLElementName.DIR);

        //il tag form può essere messo troppo all'esterno così si cancecllerebbero anche tag buoni
        // removeTag.add(HTMLElementName.FORM);
        removeTag.add(HTMLElementName.OPTION);
        removeTag.add(HTMLElementName.SELECT);
        removeTag.add(HTMLElementName.TITLE);
        removeTag.add(HTMLElementName.META);
        removeTag.add(HTMLElementName.HEAD);
        removeTag.add(HTMLElementName.SCRIPT);
        removeTag.add(HTMLElementName.PARAM);
        removeTag.add(HTMLElementName.NOSCRIPT);
        removeTag.add(HTMLElementName.APPLET);
        removeTag.add(HTMLElementName.AREA);
        removeTag.add(HTMLElementName.IMG);
        removeTag.add(HTMLElementName.SUP);
        removeTag.add(HTMLElementName.STYLE);
        //removeTag.add(HTMLElementName.BR);
        removeTag.add("nobr");

    }

    public StringBuffer getRelevantText (File file) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file),Charset.forName(encoding)));
        String line;
        StringBuffer sb = new StringBuffer();
        while ((line = input.readLine()) != null) {
            sb.append(line+"\n");
        }
        input.close();
        return getRelevantText(sb.toString());
    }

    public StringBuffer getRelevantText (String htmltext) {
        avoidTags.clear();
        html.clear();


        CharSequence stripfile = null;
        try {
            if (!htmltext.startsWith("<html")) {
                htmltext = "<html>\n" + htmltext+"\n</html>\n";
            }

            byte[] utf8Bytes = htmltext.replaceAll("<<+","<").replaceAll(">>+",">").trim().getBytes(encoding);
            stripfile = htmlPreProcessor(new String(utf8Bytes,encoding));

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Source source=new Source(stripfile);
        source.setLogger(null);

        List<Element> elementList = source.getAllElements();

        StringBuffer result = new StringBuffer();

        int count = 0;
        Element bestElement = null;
        //to find the best sentence
        for (Element el : elementList) {
            String content =el.getTextExtractor().toString();
            int contentlen =content.length();


            List<Element> children = null;
            if (el.getName().equalsIgnoreCase("A"))
                contentlen = 0;
            else if (contentlen > 0) {
                //remove the content of the children elements
                children = el.getChildElements();
                for (Element ch : children) {
                    contentlen = contentlen-ch.getTextExtractor().toString().length();
                    if (ch.getName().equalsIgnoreCase("A"))
                        contentlen = contentlen-10*ch.getTextExtractor().toString().length();
                }
            }

            if (contentlen > count) {
                count = contentlen;
                bestElement = el;
            }


        }

        String bestpath = getParentPath(bestElement);


        for (Element el : elementList) {
            if (el.getName().equalsIgnoreCase("h1") ||
                    getParentPath(el).equals(bestpath)) {
                //goodSentences.add(el);
                int count2 = el.getTextExtractor().toString().length();
                List<Element> children = el.getChildElements();
                for (Element ch : children) {
                    if (ch.getName().equalsIgnoreCase("A")) {
                        count2 = count2-ch.getTextExtractor().toString().length();
                    }

                }
                if (count2 > 0)
                    result.append(el.getTextExtractor().toString()).append("\n");
            }
        }


        return result;

    }

    private String getParentPath (Element el) {
        String path = "";
        while((el.getParentElement() != null)) {
            path = el.getName() +":"+path;
            el = el.getParentElement();
        }
        return path;
    }

    private CharSequence htmlPreProcessor (CharSequence texthtml)  {
        Source source=new Source(texthtml);
        source.setLogger(null);

        List elementList= source.getAllElements();

        StringBuffer sb = new StringBuffer();

        //+13R
        if (FILTERTAG) {
            HashMap rmTagList = new HashMap();
            List remTags=new ArrayList();
            remTags.addAll(source.getAllTags(StartTagType.SERVER_COMMON));
            remTags.addAll(source.getAllTags(StartTagType.COMMENT));
            remTags.addAll(source.getAllTags(StartTagType.DOCTYPE_DECLARATION));


            //aggiungo le posizione dei tag sconosciuti, commenti e linguaggi server
            for (int r=0; r<remTags.size();r++) {
                addBadInterval(rmTagList,
                        ((Tag) remTags.get(r)).getBegin(),
                        ((Tag) remTags.get(r)).getEnd());
                //System.err.println("RM1 " +(Tag) remTags.get(r));
            }


            Element element;

            for (Iterator i=elementList.iterator(); i.hasNext();) {
                element=(Element)i.next();
                if (removeTag.contains(element.getName().toLowerCase()) ||
                        (element.getEndTag() != null &&
                                getNormContext(element).length() == 0 &&
                                hasStructureChild(element) == false)) {
                    addBadInterval(rmTagList,element);
                    //System.err.println("== " + element.getBegin() + " " + getNormContext(element).length() + " " + element);

                } else if (element.getName().equalsIgnoreCase("text")) {
                    //se element non e' un styleTags mantengo la lista dei tag con lo stesso testo
                    //che verrano eliminati  (anche i tag con testo vuoto)
                    //if (styleTags.contains(element.getName()) && !element.getName().equals(HTMLElementName.A)) {
                    addBadInterval(rmTagList,element.getStartTag().getBegin(),element.getStartTag().getEnd());
                    if (getNormContext(element).length() > 0 && element.getEndTag() != null) {
                        addBadInterval(rmTagList,element.getEndTag().getBegin(),element.getEndTag().getEnd());
                    }
                }

            }


            Integer position;
            Integer currposition =0;

            int charpos = 0;

            if (rmTagList.size() > 0) {
                Vector v = new Vector(rmTagList.keySet());
                Collections.sort(v);
                //System.err.println(file + " " + v);


                for (int i=0; i<v.size(); i++) {
                    position = (Integer) v.get(i);
                    //System.out.println("^^" + currposition +","+position);
                    while(currposition < position) {
                        //if ((ch = isStream.read()) != -1) {
                        if (charpos < texthtml.length()) {
                            sb.append(texthtml.charAt(charpos));
                            charpos++;
                            currposition++;

                            //System.out.print((char) ch);
                        } else {
                            break;
                        }
                    }
                    sb.append(" ");
                    currposition = (Integer) rmTagList.get(position);
                    //sb.append("#"+position+"-"+currposition + "("+(currposition-position)+")");
                    if (currposition < position-1) {
                        System.err.println("Error 2! File seek (" + position + " - " + currposition + ") " );
                        System.exit(0);
                    }
                    //isStream.skip(currposition-position);
                    charpos = currposition;

                }


            }


            while(charpos < texthtml.length()) {
                sb.append(texthtml.charAt(charpos));
                charpos++;
                currposition++;
            }

            String html = sb.toString();

            //html = html.replaceAll("<[b|B][r|R]>","<p>");
            //html = html.replaceAll("<[h|H][r|R]>","<p>");
            html = html.replaceAll("</[p|P]>","<p>");
            html = html.replaceAll("<[p|P]/>","<p>");
            //html = html.replaceAll("<br\\s*/>","\n");

            //HTML Entity
            html = html.replaceAll("&nbsp;*"," ");
            //html = html.replaceAll("&#\\d+;","");


            //elimino cose del tipo <ASC http://animal-rights.net/ar-faq/ /ASC>
            rmTagList.clear();
            remTags.clear();

            //Source source=new Source(new URL("file:///" + file.getCanonicalPath()));
            source=new Source(html);
            source.setLogger(null);
            remTags.addAll(source.getAllTags(StartTagType.MARKUP_DECLARATION));
            remTags.addAll(source.getAllTags(StartTagType.UNREGISTERED));

            elementList= source.getAllElements();
            sb.setLength(0);

            //aggiungo le posizione dei tag sconosciuti, commenti e linguaggi server
            for (int r=0; r<remTags.size();r++) {
                addBadInterval(rmTagList,
                        ((Tag) remTags.get(r)).getBegin(),
                        ((Tag) remTags.get(r)).getEnd());
            }

            int c=0;
            if (rmTagList.size() > 0) {
                Vector v = new Vector(rmTagList.keySet());
                Collections.sort(v);
                //System.err.println(file + " ORDER" + v);
                //currposition=0;
                for (int i=0; i<v.size(); i++) {
                    position = (Integer) v.get(i);
                    while(currposition < position) {
                        if (c < html.length()) {
                            currposition++;
                            sb.append(html.charAt(c));
                            c++;
                        } else {
                            break;
                        }
                    }
                    sb.append(" ");
                    currposition = (Integer) rmTagList.get(position);
                    //sb.append("#"+position+"-"+currposition + "("+(currposition-position)+")");
                    if (currposition < position-1) {
                        System.err.println("Error 3! File seek (" + position + " - " + currposition + ") " );
                        System.exit(0);
                    }
                    c = currposition;

                }

                sb.append(html.substring(c,html.length()));
            } else {
                sb = new StringBuffer(html);
            }





        }
        return sb;
    }

    private void addBadInterval (HashMap hash,Element element) {
        if (element.getEndTag() != null) {
            addBadInterval(hash,element.getStartTag().getBegin(),element.getEndTag().getEnd());
        } else {
            addBadInterval(hash,element.getBegin(),element.getEnd());

        }
    }

    private void addBadInterval (HashMap hash,int begin,int end) {
        //System.err.println(">> " + begin + " " + end);
        boolean saveInput = true;
        Set keys = hash.keySet();
        Vector sortkeys = new Vector(keys);
        Collections.sort(sortkeys);
        for (int i=0; i<sortkeys.size(); i++) {
            Integer start=(Integer) sortkeys.get(i);

            if (start.intValue() <= begin && ((Integer) hash.get(start)).intValue() >= end) {
                saveInput = false;
                break;
            } else
            if (start.intValue() > begin && ((Integer) hash.get(start)).intValue() < end) {
                hash.remove(start);
            }
        }
        if (saveInput) {
            hash.put(new Integer(begin),new Integer(end));
        }

    }


    private String getNormContext(Element element) {
        return getNormContext(element.getContent().getTextExtractor().toString());
    }

    private static String getNormContext(String text) {
        if (text != null) {
            text = text.replaceAll("\\&nbsp;","");
            text = text.replaceAll("\\s+","");

        } else {
            text = "";
        }
        return text.trim();
    }


    public String getAllText (File file) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(file));
        String line;
        StringBuffer sb = new StringBuffer();
        while ((line = input.readLine()) != null) {
            sb.append(line).append("\n");
        }
        input.close();
        return getAllText(sb.toString());
    }

    public String getAllText (String htmltext) {
        try {
            String[] splittedhtml = htmltext.split("<");
            for (int t=0; t<borderTags.size(); t++) {
                String tag = (borderTags.get(t)).toString();
                for (int e=0; e<splittedhtml.length; e++) {
                    if (splittedhtml[e].toLowerCase().matches("^"+tag+"[ |>].*")) {
                        splittedhtml[e] = "_#:_<" + splittedhtml[e];
                        //System.err.println(">" +splittedhtml[e]);
                    }
                }
            }
            StringBuffer sb = new StringBuffer();
            sb.append("<html>\n");
            for (int e=0; e<splittedhtml.length; e++) {
                if (splittedhtml[e].matches("^_#:_.*")) {
                    sb.append(splittedhtml[e]);
                } else {
                    sb.append("<").append(splittedhtml[e]);
                }
            }
            sb.append("\n</html>\n");


            byte[] utf8Bytes = sb.toString().getBytes(encoding);
            Source source = new Source(htmlPreProcessor(new String(utf8Bytes, "UTF8")));
            source.setLogger(null);

            htmltext = source.getTextExtractor().toString().replaceAll("_#:_","\n");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return htmltext.replaceAll("\n\\s*\n+","\n");
    }

    private boolean hasStructureChild (Element element) {
        String htmlstr = element.toString().toLowerCase();
        for (int i=0; i<borderTags.size(); i++) {
            if (htmlstr.matches(".*<\\s*" + borderTags.get(i) + "[>|\\s].*")) {
                return true;
            }
        }
        return false;

    }


    public void analyze(String filein, String fileout) throws IOException {
        File input = new File(filein);
        StringBuffer st = getRelevantText(input);

        //save all remained entries in a file
        OutputStreamWriter writer = new OutputStreamWriter(System.out,Charset.forName(encoding));
        if (fileout != null) {
            File target = new File(fileout);
            if (target.getCanonicalPath().equals(input.getCanonicalPath())) {
                System.err.println("Error! Input and output file are identical (" + filein +")");
                return;
            }

            if (target.isDirectory())
                target = new File(target,input.getName());

            //System.err.println("#Writing... " + target);
            writer = new OutputStreamWriter(new FileOutputStream(target),Charset.forName(encoding));
        }
        writer.write(st.toString());
        writer.flush();
        if (fileout != null) {
            writer.close();
        }

    }


    public static void main(String[] args) throws Exception {
        String outfile = null;
        String lang = "english";
        if (args.length < 2) {
            System.err.println("Usage:\n   java -cp jericho-html-3.2.jar eu.fbk.textpro.modules.cleanpro.CleanPro <lang> <input file or directory> [output directory]\n\n- <lang>\t\tthe language of the input page can be 'italian' or 'english'. Put 'null' if you want to use both language settings.\n- <input file or dir>\tif the input file is a directory the recorsive parsing will be done.\n- [output directory]\t(optional) if a output dir is passed the result files will be written into it. The result puts on STDOUT otherwise.");
            System.exit(0);
        } else {
            if (args.length > 2)
                outfile = args[2];
            lang = args[0].toLowerCase();
        }
        //System.err.println("# CleanPro... " +  args[1]);

        CleanPro hc = new CleanPro(lang);
        //hc.setEncoding("ISO-8859-1");

        File pathin = new File(args[1]);
        if (pathin.isDirectory()) {
            File[] files = pathin.listFiles();

            for (int i = 0; i < files.length; i++) {
                hc.analyze(files[i].getCanonicalPath(), outfile);
            }
        } else {
            hc.analyze(args[1], outfile);
        }
    }
}