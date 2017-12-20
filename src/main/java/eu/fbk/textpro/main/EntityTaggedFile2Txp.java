package eu.fbk.textpro.main;

/**
 * User: qwaider, cgirardi [@fbk.eu]
 * Date: 5-set-2013
 * Time: 14.09.27
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import eu.fbk.textpro.api.TextProGate;
import net.htmlparser.jericho.Source;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class EntityTaggedFile2Txp {

    /**
     * @param args
     */
    static String textproHome = "./textpro.sh";
    static Hashtable<Integer, entityStructure> entityList = new Hashtable<Integer, entityStructure>();

    public static void main(String[] args) throws IOException,
            XPathExpressionException, SAXException,
            ParserConfigurationException {

        String encoding = "utf8";
        if (args.length == 2) {
            String language = args[0].toLowerCase();
            if (!language.equals("italian") && !language.equals("english")) {
                System.err.println("WARNING! The language '"+args[1]+"' is not valid. Use 'english' or 'italian'.");
                System.exit(0);
            }

            String filepath = args[1];
            String tmpOutput = filepath + ".txt";
            String finalMergedFile = filepath+".txp";
            File output = new File(finalMergedFile);
            if (output.exists())
                output.delete();

            prepareFile(filepath);
            // printHash(entityList);
            // System.out.println(entityList.size());
            File file = new File(filepath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "UTF8"));
            StringBuffer text = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("___BR___");
            }
            reader.close();
            Source source = new Source(text);
            String pureText = source.getTextExtractor().toString()
                    .replaceAll("___BR___", "\n");
            // System.err.println(pureText);

            File fileDir = new File(tmpOutput);
            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileDir), encoding));
            out.append(pureText);
            out.flush();
            out.close();
            tryIndexes(tmpOutput);
            //printHash(entityList);
            callTextPro(language, tmpOutput);
            File txpfile = new File(tmpOutput+".txp");
            if (txpfile.exists()) {
                mergeEntityWithTextProOutput(txpfile.getCanonicalPath(),finalMergedFile);
                //System.err.println("Removing temporarly files...");
                fileDir.delete();
                txpfile.delete();
                System.err.println("Saved " + finalMergedFile);
            }
        }
    }

    private static void mergeEntityWithTextProOutput(String filepath,
                                                     String finalMergedFile) throws IOException {
        FileInputStream in = new FileInputStream(filepath);
        String encoding = "utf8";
        Reader reader = new InputStreamReader(in, encoding);
        BufferedReader br = new BufferedReader(reader);

        File fileDir = new File(finalMergedFile);
        Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileDir), encoding));
        Hashtable<String,Integer> colsList = new Hashtable<String, Integer>();
        String line = "";
        boolean headerFound=false;
        while (((line = br.readLine()) != null)&&!headerFound){
            if(line.startsWith("# FIELDS: ")){
                line = line.trim(); //.replaceFirst("# FIELDS: ", "").trim();
                String[] cols = line.replaceFirst("# FIELDS: ", "").trim().split("\t");
                for(int i=0;i<cols.length;i++)
                    colsList.put(cols[i], i);

                out.append(line+"\tentity").append("\n");
                headerFound=true;
            }else{
                out.append(line).append("\n");
            }
        }
        if(headerFound){
            Integer tokenColIndex = colsList.get("token");
            Integer tokenstartColIndex = colsList.get("tokenstart");
            int entityListCounter=0,entityListSize=entityList.size();
            while((line = br.readLine()) != null){
                if(line.split("\t").length>tokenstartColIndex&&entityListCounter<entityListSize){
                    String[] tokCols = line.split("\t");
                    //System.out.println(tokCols[tokenstartColIndex]);
                    if((entityList.get(entityListCounter).approximateIndex.equals(tokCols[tokenstartColIndex]))){
                        //System.out.println(tokCols[tokenColIndex]+"="+tokCols[tokenstartColIndex]+"="+entityList.get(entityListCounter).token+"="+entityList.get(entityListCounter).approximateIndex);
                        if(entityList.get(entityListCounter).token.equals(tokCols[tokenColIndex])){
                            out.append(line+"\tB-"+entityList.get(entityListCounter).value).append("\n");
                        }else{
                            String full=entityList.get(entityListCounter).token;
                            boolean first=true;
                            while(full.length()>0){
                                full=full.trim();
                                tokCols[tokenColIndex]=tokCols[tokenColIndex].trim();

                                if(!full.startsWith(tokCols[tokenColIndex]))
                                    System.err.println("Issue 1:EntitylistToken="+full+" TextproToken="+tokCols[tokenColIndex]);
                                if(first){
                                    out.append(line+"\tB-"+entityList.get(entityListCounter).value).append("\n");
                                    full=full.replaceFirst(tokCols[tokenColIndex], "");
                                    first=false;
                                }else{
                                    out.append(line+"\tI-"+entityList.get(entityListCounter).value).append("\n");
                                    full=full.replaceFirst(tokCols[tokenColIndex], "");
                                }
                                ///read next line to continue reading
                                if((line = br.readLine()) != null)
                                    tokCols = line.split("\t");
                                else{
                                    System.err.println("Issue 1:EntitylistToken="+full+" while no other lines on Textpro Output");
                                }

                            }
                        }
                        entityListCounter++;
                    }else{
                        out.append(line+"\tO").append("\n");
                    }

                }else{
                    if(line.split("\t").length>1)
                        out.append(line+"\tO").append("\n");
                    else
                        out.append(line).append("\n");
                }
            }
        }
        out.flush();
        out.close();
    }

    private static void tryIndexes(String filepath) throws IOException {

        FileInputStream in = new FileInputStream(filepath);
        String encoding = "utf8";
        Reader reader = new InputStreamReader(in, encoding);
        BufferedReader br = new BufferedReader(reader);
        String line = "";
        Integer entitytmp = 0, sizeEntityList = entityList.size(), lineNumber = 0,linesLength=0;

        while (((line = br.readLine()) != null) && entitytmp < sizeEntityList) {
            lineNumber++;
            int lastIndexReached = 0;
            // System.out.println("=" + line);
            if (line.contains(entityList.get(entitytmp).token)) {
                int tokenIndex = line.indexOf(entityList.get(entitytmp).token,
                        lastIndexReached);
                entityList.get(entitytmp).approximateIndex = linesLength+tokenIndex + "";
                entityList.get(entitytmp).lineNumber = lineNumber + "";
                lastIndexReached = tokenIndex;
                entitytmp+=1;
                //System.out.println(lastIndexReached+entityList.get(entitytmp-1).token.length());
                while (entitytmp < sizeEntityList&&line.contains(entityList.get(entitytmp).token)
                        && (line.indexOf(entityList.get(entitytmp).token,
                        (lastIndexReached+entityList.get(entitytmp-1).token.length())) > lastIndexReached)
                        ) {
                    tokenIndex = line.indexOf(entityList.get(entitytmp).token,
                            lastIndexReached+entityList.get(entitytmp-1).token.length());
                    entityList.get(entitytmp).approximateIndex = linesLength+tokenIndex
                            + "";
                    entityList.get(entitytmp).lineNumber = lineNumber + "";
                    lastIndexReached = tokenIndex;
                    entitytmp++;
                    //System.out.println(lastIndexReached);

                }
            }
            linesLength+=line.length()+1;
        }

    }

    private static void callTextPro(String language, String filepath) {
        try {
            TextProGate textpro = new TextProGate();
            textpro.setLanguage(language.substring(0,3));
            textpro.overwriteOutput();
            textpro.activeVerboseMood();
            //textpro.setOutputFolder("/tmp/");
            //textpro.setOutputFileName("aa.txp");
            textpro.setInputFile(filepath);
            textpro.getTokenPro().active(TextProGate.TokenProType.token.name());
            textpro.getTokenPro().active(TextProGate.TokenProType.tokenstart.name());
            textpro.getTagPro().active(TextProGate.TagProType.pos.name());
            textpro.getLemmaPro().active(TextProGate.LemmaProType.lemma.name());
            textpro.runTextPro();

        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private static void printHash(Hashtable<Integer, entityStructure> tmpL) {
        Iterator<Integer> kyl = tmpL.keySet().iterator();
        while (kyl.hasNext()) {
            Integer ktmp = kyl.next();
            entityStructure valtmp = tmpL.get(ktmp);
            System.err.println(ktmp + "=line#" + valtmp.lineNumber + "="
                    + valtmp.token + "=>" + valtmp.value + "=tokenIndex="
                    + valtmp.approximateIndex);
        }

    }

    private static void prepareFile(String filepath) throws IOException,
            XPathExpressionException, SAXException,
            ParserConfigurationException {
        // File filtertmp = new File(tmpOutput);
        // FileInputStream in = new FileInputStream(filepath);
        // String encoding="utf8";
        // Reader reader = new InputStreamReader(in, encoding);
        // BufferedReader br = new BufferedReader(reader);
        String tagRegex = "//ent[@type]";
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document xmlDocument = builder.parse(new FileInputStream(filepath));
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xPath.compile(tagRegex).evaluate(
                xmlDocument, XPathConstants.NODESET);

        Object result = xPath.compile(tagRegex).evaluate(xmlDocument,
                XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            Node currentItem = nodes.item(i);
            String key = currentItem.getAttributes().getNamedItem("type")
                    .getNodeValue();
            String value = currentItem.getTextContent();



            entityStructure tmp = new entityStructure();
            tmp.token = value;
            tmp.value = key;
            entityList.put(entityList.size(), tmp);
            // System.out.printf(i+" %1s = %2s\n", key, value);
			/*String[] vals = value.split(" ");
            for (int q = 0; q < vals.length; q++) {
                if (q == 0) {
                    entityStructure tmp = new entityStructure();
                    tmp.token = vals[q];
                    tmp.value = "B-" + key;
                    entityList.put(entityList.size(), tmp);

                } else {
                    entityStructure tmp = new entityStructure();
                    tmp.token = vals[q];
                    tmp.value = "I-" + key;
                    entityList.put(entityList.size(), tmp);
                }
            }
			*/
        }
    }

}

class entityStructure {
    String token;
    String value;
    String approximateIndex;
    String lineNumber;
}

