package eu.fbk.textpro.main;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.ValidationEventCollector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import eu.fbk.textpro.toolbox.MYProperties;
import eu.fbk.textpro.toolbox.toolbox;
import eu.fbk.textpro.wrapper.Textpro;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.Attributes;
import eu.fbk.textpro.modules.tokenpro.NormalizeText;
import eu.fbk.textpro.wrapper.TextProPipeLine;
import eu.fbk.textpro.wrapper.utility.naf.NAF;
import eu.fbk.textpro.wrapper.utility.naf.NAF.Timex3;
import eu.fbk.textpro.wrapper.utility.naf.NAF.Chunks.Chunk;
import eu.fbk.textpro.wrapper.utility.naf.NAF.Terms.Term;
import eu.fbk.textpro.wrapper.utility.naf.NAF.Terms.Term.Span.Target;
import eu.fbk.textpro.wrapper.utility.naf.NAF.Text.Wf;

public class TimeProNAF {

    /**
     * @param args
     */
    // configure a validating SAX2.0 parser (Xerces2)
    static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static final String JAXP_SCHEMA_LOCATION = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    static NAF nafFile;
    static BufferedWriter buffout = null;
    static private String encodingOUT = "UTF8";
    // NAF POS is PennTreebank while for English TexTPro the tagset is BNC
    static boolean getPOSfromNAF = false;
    final private static boolean DEBUG = false;

    final private static String NULLVALUE = "__NULL__";
    final private static String textproShellHomepath = "./textpro.sh";

    public static void main(String[] args) throws JAXBException,
            ParserConfigurationException, SAXException, IOException,
            InstantiationException, IllegalAccessException,
            ClassNotFoundException, NoSuchMethodException, SecurityException,
            IllegalArgumentException, InvocationTargetException,
            TransformerException {
        boolean stdout =false;
        if(args.length >= 3){
            stdout= Boolean.valueOf(args[2]);
        }else{
            System.err.println("TimeProTester error: $inputFile $outputFile $outputType(StdOut:true;false) config.properties");
            System.exit(0);
        }
        Random rn = new Random(System.nanoTime());
        String currentPath = "/tmp/";
        String txpinFile = currentPath + File.separator + rn.nextLong() + ".in";
        String txpoutFile = txpinFile + ".txp";

        String nafFile = null;
        String outFile = null;
        //System.err.println("TimePro main:"+args.length+" input:"+args[0]);
        if (stdout) {
            nafFile = args[0];
            outFile = args[1];
            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(nafFile), "UTF8"));

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    System.in));
            String input;
            while ((input = br.readLine()) != null) {
                out.write(input + "\n");
            }
            out.close();
        } else {
            nafFile = args[0]; // NAF.xml

            if (args.length >= 3) {
                outFile = args[1];
            } else {
                System.err
                        .println("WARNING! No name specified for output file");
                System.exit(0);
            }
        }

        // /call textpro to generate the timex
        eu.fbk.textpro.wrapper.TextProPipeLine textpro = new TextProPipeLine();

        new TimeProNAF().NAF2TXP(nafFile, txpinFile, getPOSfromNAF);
        if (DEBUG) {
            System.err.println("after NAF2TXP=");
            calculateMemory();
            System.gc();
            Runtime.getRuntime().gc();
            System.err.println("after gc NAF2TXP before textpro=");
            calculateMemory();
        }
        String[] parms = { "-y", "-d", "tokenization", "-l", "eng", "-c",
                "token+tokenid+pos+chunk+timex", txpinFile
                // ,"-o",txpoutFile problem running this option, but we already know
                // that textpro give the inputName+".txp" as output filename
        };
        
        String conf = args[3];
		System.err.println("Conf file: " + conf);
		MYProperties prop = new MYProperties(conf);
		toolbox tools = new toolbox(prop);
        tools.variables.setVERBOSE(true);
        tools.inputLine(parms);		
        textpro.TextProPipeLine(tools);
        // callTextProBuilder(parms);

        if (DEBUG) {
            System.err.println("after textpro=");
            calculateMemory();
            System.gc();
            Runtime.getRuntime().gc();
            System.err.println("after gc textpro before TXP2NAF=");
            calculateMemory();
        }

        // / do the final transformation
        new TimeProNAF().TXP2NAF3(nafFile, txpoutFile, outFile);

        // Delete the processed files
        File as = new File(txpinFile);
        as.deleteOnExit();
        File as1 = new File(txpoutFile);
        as1.deleteOnExit();

        if (DEBUG) {
            System.err.println("Finally");
            calculateMemory();
        }
    }

    static void calculateMemory() {
        int mb = 1024 * 1024;

        // Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();

        System.err.println("##### Heap utilization statistics [MB] #####");

        // Print used memory
        System.err.println("Used Memory:"
                + (runtime.totalMemory() - runtime.freeMemory()) / mb);

        // Print free memory
        System.err.println("Free Memory:" + runtime.freeMemory() / mb);

        // Print total available memory
        System.err.println("Total Memory:" + runtime.totalMemory() / mb);

        // Print Maximum available memory
        System.err.println("Max Memory:" + runtime.maxMemory() / mb);
    }

    public static void callTextProBuilder(Object[] arg) throws IOException {
        System.out.println(TimeProNAF.textproShellHomepath + " "
                + arg[0].toString());
        ProcessBuilder pb = new ProcessBuilder(TimeProNAF.textproShellHomepath,
                arg[0].toString());
        //Map<String, String> env = pb.environment();
        // env.put("VAR1", "myValue");
        // env.remove("OTHERVAR");
        // env.put("VAR2", env.get("VAR1") + "suffix");

        pb.directory(new File("."));
        Process p = pb.start();
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void TXP2NAF3(String originalNAFFile, String infile, String outFile)
            throws TransformerException,
            TransformerFactoryConfigurationError, SAXException,
            ParserConfigurationException, IOException {
        final List<Timex3> timex = getTimex(infile);
        XMLFilterImpl xr = new XMLFilterImpl(XMLReaderFactory.createXMLReader()) {
            @Override
            public void startElement(String uri, String localName,
                                     String qName, Attributes atts) throws SAXException {
                super.startElement(uri, localName, qName, atts);
            }

            public void endElement(String uri, String localName, String qName)
                    throws SAXException {
                if (qName.equals("nafHeader")) {
                    Textpro.Modules.Module timemodule = new TextProPipeLine().getModule("timepro");

                    //String tmp = "<linguisticProcessors layer=\"timex3\">"
                    //        + "<lp name=\"TimePro\" timestamp=\"" + wrapper.getISODate()
                    //        + "\" version=\""+timemodule.getModulesVersionDetails()+\"></lp>"
                    //       + "</linguisticProcessors>";

                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addAttribute("", "", "layer", "", "timex3");
                    Attributes attr = attrs;
                    super.startElement("", "linguisticProcessors",
                            "linguisticProcessors", attr);
                    // super.characters("2004".toCharArray(), 0,
                    // "2004".length());
                    AttributesImpl attrs2 = new AttributesImpl();
                    attrs2.addAttribute("", "", "name", "", "TimePro");
                    attrs2.addAttribute("", "", "timestamp", "", toolbox.getISODate());
                    attrs2.addAttribute("", "", "version", "", timemodule.getModulesVersionDetails());
                    Attributes attr2 = attrs2;
                    super.startElement("", "lp", "lp", attr2);
                    // super.characters("2004".toCharArray(), 0,
                    // "2004".length());
                    super.endElement("", "lp", "lp");

                    super.endElement("", "linguisticProcessors", "linguisticProcessors");

                    // ch = tmp.toCharArray();
                    // start = 0;
                    // length = ch.length;
                } else if (qName.equals("NAF")) {
                    // add time3x to the original file
                    super.startElement("", "timeExpressions","timeExpressions",null);
                    Iterator<Timex3> tl = timex.iterator();
                    while (tl.hasNext()) {
                        Timex3 ttmp = tl.next();
                        AttributesImpl attrs3 = new AttributesImpl();
                        attrs3.addAttribute("", "", "id", "", ttmp.getTmx3Id());
                        attrs3.addAttribute("", "", "type", "", (ttmp.getType().replaceFirst("I-", "")
                                .replaceFirst("B-", "").trim()));
                        super.startElement("", "timex3","timex3",attrs3);
                        /*
                        String tmpTimex = "<timex3 id=\""
                                + ttmp.getTmx3Id()
                                + "\" type=\""
                                + ttmp.getType().replaceFirst("I-", "")
                                .replaceFirst("B-", "").trim() + "\" >";
                         */
                        super.startElement("", "span","span", null);
                        //tmpTimex += "<span>";

                        Iterator<eu.fbk.textpro.wrapper.utility.naf.NAF.Timex3.Span.Target> tarl = ttmp
                                .getSpan().getTarget().iterator();
                        while (tarl.hasNext()) {
                            eu.fbk.textpro.wrapper.utility.naf.NAF.Timex3.Span.Target tartmp = tarl
                                    .next();
                            AttributesImpl attrs4 = new AttributesImpl();
                            attrs4.addAttribute("", "", "id", "", tartmp.getId());
                            super.startElement("", "target","target", attrs4);
                            super.endElement("", "target", "target");
                            //tmpTimex += "<target id=\"" + tartmp.getId()
                            //        + "\"/></target>";
                        }
                        //tmpTimex += "</span>";
                        super.endElement("", "span", "span");
                        super.endElement("", "timex3", "timex3");
                        //tmpTimex += "</timex3>";
                        // end
                    }
                    super.endElement("", "timeExpressions", "timeExpressions");
                    // emit(tmp);
                    //ch = tmp.toCharArray();
                    //start = 0;
                    //length = ch.length;
                }
                super.endElement(uri, localName, qName);
            }


            @Override
            public void characters(char[] ch, int start, int length)
                    throws SAXException {
                super.characters(ch, start, length);
            }

        };

        File file = new File(originalNAFFile);
        InputStream inputStream = new FileInputStream(file);
        Reader reader = new InputStreamReader(inputStream, "UTF-8");
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");
        Source src = new SAXSource(xr, is);
        // Result res = new StreamResult(System.out);
        // Result res = new StreamResult(outFile);

        StreamResult res;
        if (outFile != null) {
            res = new StreamResult(outFile);
        } else {
            res = new StreamResult(System.out);
        }


        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "2");

        transformer.transform(src, res);

    }

    List<Timex3> getTimex(String infile) throws ParserConfigurationException,
            IOException {

        // the textpro output here should have atleast
        FileInputStream in = new FileInputStream(infile);
        Reader reader = new InputStreamReader(in, "utf8");
        BufferedReader br = new BufferedReader(reader);
        String line;
        boolean startReading = false;
        Hashtable<String, Integer> col = new Hashtable<String, Integer>();
        while (!startReading && ((line = br.readLine()) != null)) {

            if (line.startsWith("# FIELDS:")) {
                String[] cols = line.replace("# FIELDS: ", "").trim()
                        .split("\t");
                for (int i = 0; i < cols.length; i++) {
                    col.put(cols[i], i);
                }
                startReading = true;
            }
        }
        // printHash(col);
        if (startReading && col.containsKey("token") && col.containsKey("pos")
                && col.containsKey("chunk") && col.containsKey("timex")
                && col.containsKey("tokenid")) {
            NAF file = new NAF();
            // NAF file = nafFile;
            // file.setLang("en");
            // file.setVersion("v3");
            // Terms terms = new Terms();
            // Text toks = new Text();

            // Chunks chunks = new Chunks();
            //int sentenceNumber = 1;
            // int tokenCounter = 1;
            // int termCounter = 0;
            int timex3Counter = 1;
            String previousTimex = "";
            while ((line = br.readLine()) != null) {
                // System.out.println(line);
                if (!line.startsWith("# ")) {
                    if (line.split("\t").length == col.size()) {
                        String[] lineCols = line.split("\t");
                        //String token = lineCols[col.get("token")];
                        // String pos = lineCols[col.get("pos")];
                        // String chunkString = lineCols[col.get("chunk")];
                        String timex = lineCols[col.get("timex")];
                        String tokenid = lineCols[col.get("tokenid")];

                        // //add Token
                        // Wf wftmp = new Wf();
                        // wftmp.setId("w" + tokenid);
                        // wftmp.setLength(token.length());
                        // wftmp.setSent("s" + sentenceNumber);
                        // wftmp.setValue(token);
                        // toks.getWf().add(wftmp);
                        // // check to be in the same term in timex now and
                        // could be extended to be more
                        if (timex.length() > 0 && !timex.equals("O")
                                && !timex.equals("")) {

                            if (timex.startsWith("I-")) {
                                eu.fbk.textpro.wrapper.utility.naf.NAF.Timex3.Span.Target target = new eu.fbk.textpro.wrapper.utility.naf.NAF.Timex3.Span.Target();
                                target.setId("w" + tokenid);
                                // /check here if it is the same term of the
                                // target or not!!!
                                // System.err.println(timex3Counter+"="+file.getTimex3().size());
                                file.getTimex3().get(timex3Counter - 2)
                                        .getSpan().getTarget().add(target);
                            } else if (timex.startsWith("B-")) {

                                // add time3x for this term
                                NAF.Timex3.Span timeSpan = new NAF.Timex3.Span();
                                eu.fbk.textpro.wrapper.utility.naf.NAF.Timex3.Span.Target timtarget = new eu.fbk.textpro.wrapper.utility.naf.NAF.Timex3.Span.Target();
                                timtarget.setId("w" + tokenid);
                                timeSpan.getTarget().add(timtarget);
                                Timex3 timex3 = new Timex3();
                                timex3.setTmx3Id("tmx" + timex3Counter);
                                timex3.setType(timex.replaceFirst("I-", "")
                                        .replaceFirst("B-", "").trim());
                                timex3.setSpan(timeSpan);
                                file.getTimex3().add(timex3);

                                // termCounter++;
                                timex3Counter += 1;
                            } else if (!timex.equals("O")
                                    && (timex.length() > 0)) {
                                // it should be catched previously, error to be
                                // here
                                System.err
                                        .println("ERROR:Timex not catched: previous="
                                                + previousTimex
                                                + " current="
                                                + timex);
                            }

                        } //else {
                            // TODO
                            // it could be that this is not a porblem but the
                            // timex didnot provide any timex data!!! check it
                            // System.err.println("Problem: null termlist hashtable.");
                        //}
                    } else if (line.trim().length() == 0) {
                        //sentenceNumber++;
                    } else {
                        System.err.println("Error Input line:" + line);
                    }

                    // /Increase counters
                    // tokenCounter+=1;

                }

            }

            // file.setText(toks);

            // file.setChunks(chunks);
            // Marshalling to a javax.xml.transform.SAXResult:
            // JAXBContext jc =
            // JAXBContext.newInstance("eu.fbk.textpro.wrapper.utility.naf");
            // Marshaller marshaller = jc.createMarshaller();
            // marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
            // Boolean.TRUE);

            // assume MyContentHandler instanceof ContentHandler
            // SAXResult result = new SAXResult(new MyContentHandler());
            // marshaller.marshal(file, result);
            // marshaller.marshal(file, new FileOutputStream(new
            // File(outFile)));

            // create an element for marshalling

            // create a Marshaller and marshal to System.out
            // JAXB.marshal(file, new FileOutputStream(new File(outFile)));
            // //looop on the timex3 list

            return file.getTimex3();

        }
        return null;
    }

    private void TXP2NAF2(String originalNAFFile, String infile, String outFile) {
        try {

            OutputStream outputStream = new FileOutputStream(new File(outFile));
            //XMLStreamWriter out = XMLOutputFactory.newInstance()
            //        .createXMLStreamWriter(new OutputStreamWriter(outputStream, "utf-8"));

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {

                boolean nafHeader = false;
                boolean other = false;

                public void startElement(String uri, String localName,
                                         String qName, Attributes attributes)
                        throws SAXException {

                    // System.err.println("Start Element :" + qName);

                    if (qName.equalsIgnoreCase("nafHeader")) {
                        nafHeader = true;
                    } else {
                        other = true;
                    }
                }

                public void endElement(String uri, String localName,
                                       String qName) throws SAXException {
                    if (qName.equalsIgnoreCase("nafHeader")) {
                        System.err.println("End Element :" + qName);
                    }

                }

                public void characters(char ch[], int start, int length)
                        throws SAXException {

                    if (nafHeader) {
                        System.err.println("nafHeader data : "
                                + new String(ch, start, length));
                        nafHeader = false;
                    }

					/*
					 * if (other) { System.err.println("Last Name : " + new
					 * String(ch, start, length)); other = false; }
					 */
                }

            };
            File file = new File(originalNAFFile);
            InputStream inputStream = new FileInputStream(file);
            Reader reader = new InputStreamReader(inputStream, "UTF-8");
            InputSource is = new InputSource(reader);
            is.setEncoding("UTF-8");
            saxParser.parse(is, handler);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void TXP2NAF(String originalNAFFile, String infile, String outFile)
            throws IOException, JAXBException, SAXException,
            ParserConfigurationException, TransformerException {

        // /to add time3x to the existing file
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory
                .newDocumentBuilder();
        Document document = documentBuilder.parse(originalNAFFile);
        Element root = document.getDocumentElement();

        NodeList contacts = root.getElementsByTagName("nafHeader");
        Element linguisticProcessors = document
                .createElement("linguisticProcessors");
        linguisticProcessors.setAttribute("layer", "timex3");
        // <lp name="TimePro" timestamp="2013-10-14T12:15:18Z" version="2.0"/>
        Element lp = document.createElement("lp");
        lp.setAttribute("name", "TimePro");
        lp.setAttribute("timestamp", toolbox.getISODate());
        Textpro.Modules.Module timemodule = new TextProPipeLine().getModule("timepro");
        lp.setAttribute("version", timemodule.getModulesVersionDetails());
        linguisticProcessors.appendChild(lp);
        contacts.item(0).appendChild(linguisticProcessors);
        // /End

        // the textpro output here should have atleast
        FileInputStream in = new FileInputStream(infile);
        Reader reader = new InputStreamReader(in, "utf8");
        BufferedReader br = new BufferedReader(reader);
        String line;
        boolean startReading = false;
        Hashtable<String, Integer> col = new Hashtable<String, Integer>();
        while (!startReading && ((line = br.readLine()) != null)) {

            if (line.startsWith("# FIELDS:")) {
                String[] cols = line.replace("# FIELDS: ", "").trim()
                        .split("\t");
                for (int i = 0; i < cols.length; i++) {
                    col.put(cols[i], i);
                }
                startReading = true;
            }
        }
        // printHash(col);
        if (startReading && col.containsKey("token") && col.containsKey("pos")
                && col.containsKey("chunk") && col.containsKey("timex")
                && col.containsKey("tokenid")) {
            NAF file = new NAF();
            // NAF file = nafFile;
            // file.setLang("en");
            // file.setVersion("v3");
            // Terms terms = new Terms();
            // Text toks = new Text();

            // Chunks chunks = new Chunks();
            int sentenceNumber = 1;
            // int tokenCounter = 1;
            // int termCounter = 0;
            int timex3Counter = 1;
            String previousTimex = "";
            while ((line = br.readLine()) != null) {
                // System.err.println(line);
                if (!line.startsWith("# ")) {
                    if (line.length() < 1) {
                        sentenceNumber++;
                    } else if (line.split("\t").length == col.size()) {
                        String[] lineCols = line.split("\t");
                        String token = lineCols[col.get("token")];
                        // String pos = lineCols[col.get("pos")];
                        // String chunkString = lineCols[col.get("chunk")];
                        String timex = lineCols[col.get("timex")];
                        String tokenid = lineCols[col.get("tokenid")];

                        // //add Token
                        // Wf wftmp = new Wf();
                        // wftmp.setId("w" + tokenid);
                        // wftmp.setLength(token.length());
                        // wftmp.setSent("s" + sentenceNumber);
                        // wftmp.setValue(token);
                        // toks.getWf().add(wftmp);
                        // // check to be in the same term in timex now and
                        // could be extended to be more
                        if (timex.length() > 0 && !timex.equals("O")
                                && !timex.equals("")) {

                            if (timex.startsWith("I-")) {
                                eu.fbk.textpro.wrapper.utility.naf.NAF.Timex3.Span.Target target = new eu.fbk.textpro.wrapper.utility.naf.NAF.Timex3.Span.Target();
                                target.setId("w" + tokenid);
                                // /check here if it is the same term of the
                                // target or not!!!
                                // System.err.println(timex3Counter+"="+file.getTimex3().size());
                                file.getTimex3().get(timex3Counter - 2)
                                        .getSpan().getTarget().add(target);
                            } else if (timex.startsWith("B-")) {

                                // add time3x for this term
                                NAF.Timex3.Span timeSpan = new NAF.Timex3.Span();
                                eu.fbk.textpro.wrapper.utility.naf.NAF.Timex3.Span.Target timtarget = new eu.fbk.textpro.wrapper.utility.naf.NAF.Timex3.Span.Target();
                                timtarget.setId("w" + tokenid);
                                timeSpan.getTarget().add(timtarget);
                                Timex3 timex3 = new Timex3();
                                timex3.setTmx3Id("tmx" + timex3Counter);
                                timex3.setType(timex.replaceFirst("I-", "")
                                        .replaceFirst("B-", "").trim());
                                timex3.setSpan(timeSpan);
                                file.getTimex3().add(timex3);

                                // termCounter++;
                                timex3Counter += 1;
                            } else if (!timex.equals("O")
                                    && (timex.length() > 0)) {
                                // it should be catched previously, error to be
                                // here
                                System.err
                                        .println("ERROR:Timex not catched: previous="
                                                + previousTimex
                                                + " current="
                                                + timex);
                            }

                        } //else {
                            // TODO
                            // it could be that this is not a porblem but the
                            // timex didnot provide any timex data!!! check it
                            // System.err.println("Problem: null termlist hashtable.");
                        //}
                    } else {
                        System.err.println("Error Input line:" + line);
                    }

                    // /Increase counters
                    // tokenCounter+=1;

                }

            }

            // file.setText(toks);

            // file.setChunks(chunks);
            // Marshalling to a javax.xml.transform.SAXResult:
            // JAXBContext jc =
            // JAXBContext.newInstance("eu.fbk.textpro.wrapper.utility.naf");
            // Marshaller marshaller = jc.createMarshaller();
            // marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
            // Boolean.TRUE);

            // assume MyContentHandler instanceof ContentHandler
            // SAXResult result = new SAXResult(new MyContentHandler());
            // marshaller.marshal(file, result);
            // marshaller.marshal(file, new FileOutputStream(new
            // File(outFile)));

            // create an element for marshalling

            // create a Marshaller and marshal to System.out
            // JAXB.marshal(file, new FileOutputStream(new File(outFile)));
            // //looop on the timex3 list

            // add time3x to the original file
            Element timeExpressions = document.createElement("timeExpressions");

            Iterator<Timex3> tl = file.getTimex3().iterator();
            while (tl.hasNext()) {
                Timex3 ttmp = tl.next();
                Element timex3 = document.createElement("timex3");
                timex3.setAttribute("id", ttmp.getTmx3Id());
                timex3.setAttribute(
                        "type",
                        ttmp.getType().replaceFirst("I-", "")
                                .replaceFirst("B-", "").trim());

                Element name = document.createElement("span");
                Iterator<eu.fbk.textpro.wrapper.utility.naf.NAF.Timex3.Span.Target> tarl = ttmp
                        .getSpan().getTarget().iterator();
                while (tarl.hasNext()) {
                    eu.fbk.textpro.wrapper.utility.naf.NAF.Timex3.Span.Target tartmp = tarl
                            .next();
                    Element port = document.createElement("target");
                    port.setAttribute("id", tartmp.getId());
                    name.appendChild(port);

                }
                timex3.appendChild(name);
                timeExpressions.appendChild(timex3);

                // end
            }
            root.appendChild(timeExpressions);

            // /export original file
            DOMSource source = new DOMSource(document);

            TransformerFactory transformerFactory = TransformerFactory
                    .newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "2");

            StreamResult result;
            if (outFile != null) {
                result = new StreamResult(outFile);
            } else {
                result = new StreamResult(System.out);
            }
            transformer.transform(source, result);

        }
    }

	/*
	 * void printHash(Hashtable list){ Iterator ll = list.keySet().iterator();
	 * while(ll.hasNext()){ Object ltmp = ll.next();
	 * System.err.println(ltmp+"="+list.get(ltmp)); } }
	 */

    void NAF2TXP(String file, String outFile, boolean writePOS)
            throws JAXBException, ParserConfigurationException, SAXException,
            IOException {

        // Unmarshalling from a javax.xml.transform.sax.SAXSource using a client
        // specified validating SAX2.0 parser:

        // System.setProperty("javax.xml.parsers.SAXParserFactory","org.apache.xerces.jaxp.SAXParserFactoryImpl");

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setValidating(true);
        SAXParser saxParser = spf.newSAXParser();

        try {
            saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            saxParser.setProperty(JAXP_SCHEMA_LOCATION, "conf/schema/NAF.xsd");
        } catch (SAXNotRecognizedException x) {
            // exception handling omitted
        }

        XMLReader xmlReader = saxParser.getXMLReader();
        SAXSource source = new SAXSource(xmlReader, new InputSource(file));

        // Setup JAXB to unmarshal
        JAXBContext jc = JAXBContext
                .newInstance("eu.fbk.textpro.wrapper.utility.naf");
        Unmarshaller u = jc.createUnmarshaller();
        ValidationEventCollector vec = new ValidationEventCollector();
        u.setEventHandler(vec);

        // turn off the JAXB provider's default validation mechanism to
        // avoid duplicate validation

        // unmarshal
        nafFile = (NAF) u.unmarshal(source);

        // check for events
        //if (vec.hasEvents()) {
            // iterate over events
        //}
        //String language = nafFile.getLang();
        //String version = nafFile.getVersion();
        ListIterator<Wf> tokenList = nafFile.getText().getWf().listIterator();
        int lineNumber = -1;
        String output = "";
        // output += "# FIELDS: token\tpos\tchunk\ttokennorm\n";
        output += "# FIELDS: token" + (writePOS ? "\tpos" : "")
                + "\ttokennorm\ttokenid\n";

        NormalizeText normalizeme = new NormalizeText();

        while (tokenList.hasNext()) {
            Wf token = tokenList.next();

            // int sentenceNumber =
            // Integer.parseInt(token.getSent().substring(1)); //Sentence Number
            // 's100'
            int sentenceNumber = Integer.parseInt(token.getSent()); // sentence
            // number'100'
            if (lineNumber == -1) {
                lineNumber = sentenceNumber;
            } else if (lineNumber < sentenceNumber) {
                // add new empty line as this is new sentence and make
                // lineNumber=token sentence
                output += "\n";
                lineNumber = sentenceNumber;
            }
            Term termTmp = getTerm(token.getId());
            if (termTmp != null) {
                String pos;
                // String chunck = "";
                if (termTmp.getPos() != null && !termTmp.getPos().equals("")) {
                    pos = termTmp.getPos();
                } else {
                    pos = NULLVALUE;
                }
				/*
				 * if (termTmp.getId() != null && !termTmp.getId().equals("")) {
				 * Chunk chunckTmp = getChunck(termTmp.getId()); if
				 * (chunckTmp!=null&&chunckTmp.getPhrase() != null &&
				 * !chunckTmp.getPhrase().equals("")) { chunck =
				 * chunckTmp.getPhrase(); } else { chunck = NULLVALUE; } } else
				 * { System.err.println("asNo Term for:" + token.getId() + "=" +
				 * token.getValue()); }
				 */
                // /we need to generate the tokennorm here and include it to the
                // system
                String tokenNor = normalizeme.normalize(token.getValue(),"ita");
                // output += token.getValue() + "\t" + pos + "\t" + chunck
                // +"\t"+tokenNor+ "\n";
                output += token.getValue()
                        + (writePOS ? "\t" + Penn2BNC(token.getValue(), pos)
                        : "") + "\t" + tokenNor + "\t"
                        + token.getId().replaceFirst("w", "") + "\n";
            } /*
			 * else { System.err.println("No Term for:" + token.getId() + "=" +
			 * token.getValue()); }
			 */

        }

        buffout = new BufferedWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outFile, false), encodingOUT)));
        buffout.write(output);
        buffout.flush();
        buffout.close();

    }

    private Chunk getChunck(String wid) {
        ListIterator<Chunk> chunkl = nafFile.getChunks().getChunk()
                .listIterator();
        while (chunkl.hasNext()) {
            Chunk chunk = chunkl.next();
            ListIterator<eu.fbk.textpro.wrapper.utility.naf.NAF.Chunks.Chunk.Span.Target> tarl = chunk
                    .getSpan().getTarget().listIterator();
            while (tarl.hasNext()) {
                eu.fbk.textpro.wrapper.utility.naf.NAF.Chunks.Chunk.Span.Target tar = tarl
                        .next();
                if (tar.getId().equals(wid)) {
                    return chunk;
                }
            }
        }
        return null;
    }

    private static Term getTerm(String wid) {
        ListIterator<Term> terml = nafFile.getTerms().getTerm().listIterator();
        while (terml.hasNext()) {
            Term term = terml.next();
            ListIterator<Target> tarl = term.getSpan().getTarget()
                    .listIterator();
            while (tarl.hasNext()) {
                Target tar = tarl.next();
                // System.err.println(tar.getId()+","+wid+"="+tar.getId().equals(wid));
                if (tar.getId().equals(wid)) {
                    return term;
                }
            }
        }
        return null;
    }

    private static String Penn2BNC(String token, String POS) {
        if (token.equals("that")) {
            return "CJT";
        } else if (token.equals("of")) {
            return "PRF";
        } else if (token.equals("not") || token.equals("n't")) {
            return "XX0";
        }

        if (POS.equals("$") || POS.equals(",") || POS.contains("-")
                || POS.equals(".") || POS.equals(":")) {
            return "PUN";
        } else if (POS.equals("``") || POS.equals("\"")) {
            return "PUQ";
        } else if (POS.equals("(")) {
            return "PUL";
        } else if (POS.equals(")")) {
            return "PUR";
        } else if (POS.equals("CC")) {
            return "CJC";
        } else if (POS.equals("JJ")) {
            return "AJ0";
        } else if (POS.equals("JJR")) {
            return "AJC";
        } else if (POS.equals("DT")) {
            return "AT0";
        } else if (POS.equals("RB")) {
            return "AV0";
        } else if (POS.equals("RP")) {
            return "AVP";
        } else if (POS.equals("WRB")) {
            return "AVQ";
        } else if (POS.equals("CD")) {
            return "CRD";
        } else if (POS.equals("PRP$")) {
            return "DPS";
        } else if (POS.equals("WDT") || POS.equals("WP$")) {
            return "DTQ";
        } else if (POS.equals("EX")) {
            return "EX0";
        } else if (POS.equals("UH")) {
            return "ITJ";
        } else if (POS.equals("NN")) {
            return "NN1";
        } else if (POS.equals("NNS")) {
            return "NN2";
        } else if (POS.equals("NNP") || POS.equals("NNPS")) {
            return "NP0";
        } else if (POS.equals("PRP")) {
            return "PNP";
        } else if (POS.equals("WP")) {
            return "PNQ";
        } else if (POS.equals("POS")) {
            return "POS";
        } else if (POS.equals("IN")) {
            return "PRP";
        } else if (POS.equals("TO")) {
            return "TO0";
        } else if (POS.equals("FW")) {
            return "UNC";
        } else if (POS.equals("MD")) {
            return "VM0";
        } else if (POS.equals("VB")) {
            return "VVB";
        } else if (POS.equals("VBD")) {
            return "VVD";
        } else if (POS.equals("VBG")) {
            return "VVG";
        } else if (POS.equals("VBP")) {
            return "VVI";
        } else if (POS.equals("VBN")) {
            return "VVN";
        } else if (POS.equals("VBX")) {
            return "VVZ";
        } else if (POS.equals("SYM")) {
            return "ZZ0";
        }
        return "NN0";
    }
}
