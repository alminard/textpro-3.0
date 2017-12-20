package eu.fbk.textpro.modules.tokenpro;

/**
 * User: Mohammad Qwaider and Christian Girardi
 * Date: 20-mar-2013
 * Time: 12.19.36
 */

import eu.fbk.textpro.TextProModuleInterface;
import eu.fbk.textpro.modules.cleanpro.CleanPro;
import eu.fbk.textpro.toolbox.MYProperties;
import eu.fbk.textpro.toolbox.TEXTPROVARIABLES;
import eu.fbk.textpro.toolbox.toolbox;
import eu.fbk.textpro.wrapper.OBJECTDATA;
import org.apache.qpid.junit.extensions.util.CommandLineParser;
import org.apache.qpid.junit.extensions.util.ParsedProperties;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import javax.xml.bind.JAXBException;


public class TokenPro implements TextProModuleInterface {
    static private String globalLang = null;
    static private Vector fieldsout = new Vector();

    static private boolean disableTokenization = false;
    static private boolean disableSentenceSplitting = false;
    static private boolean htmlcleaner = false;

    static  NormalizeText normText;
       LexparsConfig lexpars;
    static Hashtable<String,LexparsConfig> lis = new Hashtable<String, LexparsConfig>();
    
    public TokenPro() {
        this.normText = new NormalizeText();

        fieldsout.clear();
        fieldsout.add("token");
        fieldsout.add("tokennorm");
        fieldsout.add("tokenid");
        fieldsout.add("tokenstart");
        fieldsout.add("tokenend");
        fieldsout.add("tokentype");
    }

    public String[] annotationFields () {
        return "token+tokennorm+tokenid+tokenstart+tokenend+tokentype".split("\\+");
    }

    public void init (String[] params,MYProperties prop) {
   
        //System.err.println("Tokenizer init objects="+params.length +  " " +fieldsout);
        for (int i=0; i<params.length; i++) {
            if (params[i] != null) {
                //System.out.println(params[i]+"=>"+params[i+1]);
                if (params[i].equals("-l")) {
                    if (params.length > i+1) {
                        //globalLang = params[++i];
                    	globalLang = params[++i];
                        try {
                        	LexparsConfig test = new LexparsConfig("ita");
                        	lis.put("ita", test);
                        	LexparsConfig test1 = new LexparsConfig("eng");
                        	lis.put("eng", test1);
                        	LexparsConfig test2 = new LexparsConfig("fre");
                        	lis.put("fre", test2);
                        	
						} catch (IOException e) {
							e.printStackTrace();
						} catch (JAXBException e) {
							e.printStackTrace();
						}
                    }
                } else if (params[i].equals("-c")) {
                    if (params.length > i+1) {
                        String item = params[++i];
                        //System.out.println(item);
                        fieldsout.clear();

                        List cols = Arrays.asList(item.split("\\+"));
                        for (String icol : annotationFields()) {
                            if (cols.contains(icol)) {
                                fieldsout.add(icol);
                            }
                        }

                    }
                } else if (params[i].equals("-dis")) {
                    String par = params[++i];
                    if (par.contains("tokenization")) {
                        disableTokenization = true;
                        //System.out.println("disable tok");
                    }
                    if (par.contains("sentence")) {
                        disableSentenceSplitting = true;
                    }
                } else if (params[i].equals("-html")) {
                    if (params[++i].equalsIgnoreCase("yes")) {
                        htmlcleaner = true;
                    }
                }
            }
        }
    }


    public static void main(String args[]) throws IOException, JAXBException{
        String columns = "token";
        String language = "english";

        // Use the command line parser to evaluate the command line.
        CommandLineParser commandLine =
                new CommandLineParser(
                        new String[][] {
                                { "v", "verbose mode;", null, "false"},
                                { "c", "the sequence of columns: token[+tokennorm][+tokenid][+tokenstart][+tokenend][+tokentype];", "<COLUMNS>","false"},
                                { "l", "the language: 'english','italian' or 'french' are possible;", "<LANGUAGE>","false"},
                                { "o", "the output filename;", "<FILEOUT_NAME>", "false"},
                                { "1", "input raw text", "<FILEIN_NAME>", "true" },
                        });

        // Capture the command line arguments or display errors and correct usage and then exit.
        ParsedProperties options = null;

        try {
            options = new ParsedProperties(commandLine.parseCommandLine(args));
        }
        catch (IllegalArgumentException e)
        {
            System.err.println(commandLine.getErrors());
            System.err.println(commandLine.getUsage());
            System.exit(-1);
        }

        // Extract the command line options.
        String inputfile = options.getProperty("1");
        String outfile = inputfile+".tok";

        if (options.getProperty("c") != null)
            columns = options.getProperty("c");
        if (options.getProperty("l") != null)
            language = options.getProperty("l");
        if (options.getProperty("o") != null)
            outfile = options.getProperty("o");

        boolean verbose = options.getPropertyAsBoolean("-v");

        TokenPro tok = new TokenPro();
        String[] params = new String[4];
        params[0] = "-l";
        params[1] = language;
        params[2] = "-c";
        params[3] = columns;

        //tok.init(params);
        Date date = new Date();
        long now = date.getTime();
        tok.analyze(inputfile, outfile);
        date = new Date();


        if (verbose)
            System.err.println("Saved " + inputfile +".tok [" + (date.getTime() - now) + " ms)");
    }


    private void cleanHtml(File path) throws Exception {
        if (path != null) {
            CleanPro cleanpro = new CleanPro(globalLang);
            StringBuffer st = cleanpro.getRelevantText(path);

            //save all remained entries in a file
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path), Charset.forName("UTF8"));

            writer.write(st.toString());
            writer.flush();
            writer.close();
        }
    }
    private void cleanHtml(PipedInputStream path,PipedOutputStream fileout) throws Exception {
        if (path != null) {
            CleanPro cleanpro = new CleanPro(globalLang);

            InputStream in = new DataInputStream(path);
            Reader reader = new InputStreamReader(in, "UTF8");
            BufferedReader br = new BufferedReader(reader);
            String stIn= br.readLine();
            StringBuffer st = cleanpro.getRelevantText(stIn);

            //save all remained entries in a file
            OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new DataOutputStream(fileout)), "UTF8");

            writer.write(st.toString());
            writer.flush();
            writer.close();
        }
    }

    public void analyze ( String filein, String fileout) throws IOException {
    	lexpars = lis.get(globalLang);
    //	init(params);
    	if(filein==null||fileout==null){
    		System.err.println("TokenPro: Input file or Output file is null!");
            System.exit(-1);
    	}
        if (htmlcleaner) {
            //call CleanPro
            try {
                cleanHtml(new File(filein));
            } catch (Exception e) {
                System.err.println("ERROR! Html cleaner failed.");
                e.printStackTrace();
                System.exit(-1);
            }
        }


        InputStream in = new FileInputStream(filein);
        Reader reader = new InputStreamReader(in, "UTF8");
        BufferedReader br = new BufferedReader(reader);
        File fileDir = new File(fileout);
        OutputStreamWriter out
                = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(fileDir)), "UTF8");

        out.write("# FIELDS: " + fieldsout.toString().replaceAll("\\[|\\]","").replaceAll(",\\s+","\t") + "\n");

        String line;

        char ch;
        boolean splittedword;
        boolean splittedchar = false;
        int position = -1;
        ArrayList<Integer> tokenposition = new ArrayList<Integer>();
        String tokenNorm;
        int tokenid=0;
        LinkedList<String> temp = new LinkedList<String>();

        StringBuffer wordTemp = new StringBuffer();
        String wTemp;
        ListIterator<String> wordlp;

        while((line = br.readLine()) != null) {
            wordTemp.setLength(0);
            temp.clear();
            tokenposition.clear();
            if (disableTokenization) {
                temp.add(line);
                //System.err.println(position + " " +line);
                tokenposition.add(position);
                position += line.length() + 1;
            } else {
                // do something with line.
                //start tokenizing depending on the spaces and other rules

                for(int chp=0; chp<line.length(); chp++) {
                    ch = line.charAt(chp);
                    splittedword = lexpars.generalSplittingRules(ch);

                    if (!splittedword) {
                        if (wordTemp.length() == 0)
                            tokenposition.add(position+chp+1);
                        //if (wrapper.DEBUG)
                        // System.err.println("CHAR: " +ch+ " " + (int) ch + " U+" + String.format("%04x", line.codePointAt(chp)) + " " +lexpars.charSplitter(ch) + " " + lexpars.generalSplittingRules(ch));
                        if (!splittedchar)
                            splittedchar = lexpars.charSplitter(ch);

                        if (lexpars.charSplitter(ch))
                            wordTemp.append(" ").append(String.valueOf(ch)).append(" ");
                            //wordTemp += " "+String.valueOf(ch)+" ";
                        else
                            wordTemp.append(String.valueOf(ch));
                        //wordTemp += String.valueOf(ch);

                    }

                    //if(splittedword&&lexpars.hasEndSentenceChar(String.valueOf(ch))){
                    //temp.addLast("\n");
                    //}

                    if (splittedword || chp == line.length() -1) {
                        wTemp = wordTemp.toString().replaceAll("\\s+"," ").trim();
                        tokenNorm = lexpars.checkSplitRules(wTemp);
                        //System.err.println(" {" +tokenNorm + "} " + wTemp);

                        if (tokenNorm.length()>0) {
                            /// this is to check the abb.list before reconstructing the wordl

                            if(!disableSentenceSplitting&&( lexpars.containsEndSentenceChar(wTemp)
                                    && lexpars.isAbbreviation(wTemp))) {
                                tokenNorm = tokenNorm.replaceAll("\\s+", "");
                            }

                            //System.err.println(tokenNorm);
                            // reconstruct the list after the spacing!!!
                            //System.out.println("=={"+tokenNorm+"}");
                            String[] teta = tokenNorm.split(" ");
                            if (teta.length > 1) {
                                for(int i=0;i<teta.length;i++) {
                                    if (teta[i].length()>0) {
                                        temp.addLast(teta[i]);
                                        if (i < teta.length -1) {
                                            tokenposition.add(tokenposition.get(tokenposition.size() - 1) + teta[i].trim().length());
                                        }
                                    }
                                    if(i==teta.length-1 &&
                                            !lexpars.isAbbreviation(teta[i]) &&
                                            lexpars.hasEndSentenceChar(String.valueOf(teta[i].charAt(teta[i].length()-1)))){
                                        temp.addLast("\n");
                                    }
                                }
                            } else {
                                temp.addLast(tokenNorm);
                            }

                        }
                        wordTemp.setLength(0);
                        splittedchar = false;
                    }
                }

                if(!disableTokenization && line.length() == 0){
                    ///empty line for the empty line
                    //CG:temp.addLast("\n");
                }
                position += line.length()+1;
            }
            ////////////
            //System.out.println("> "+line);

            /// write the tokenized line in the output file
            wordlp = temp.listIterator();
            int i = 0;
            int endSentenceCase = 0;
            int openParenthesis = 0;

            while(wordlp.hasNext()) {
                wTemp = wordlp.next();
                if(!wTemp.equals("\n")){
                    if (disableTokenization && wTemp.length() == 0) {
                        i++;
                        continue;
                    }

                    //manage the brakes at the beginning of the sentence
                    if ("([{".contains(wTemp.substring(0,1)))
                        openParenthesis++;
                    if (wTemp.contains(")") ||  wTemp.contains("]") || wTemp.contains("]")) {
                        openParenthesis = openParenthesis-1;
                        if (openParenthesis < 0)
                            openParenthesis=0;
                    }
                    if (!disableSentenceSplitting &&
                            endSentenceCase == 1 &&  openParenthesis == 0 &&
                            (openParenthesis > 0 || !normText.isInterpunzione(wTemp.substring(0,1)))) {
                        out.append("\n");
                    }
                    endSentenceCase = 0;
                    if (fieldsout.contains("token"))
                        out.append(wTemp);
                    if (fieldsout.contains("tokennorm"))
                    	out.append("\t").append(normText.normalize(wTemp,globalLang));
                    if (fieldsout.contains("tokenid"))
                        out.append("\t").append(String.valueOf(++tokenid));
                    if (fieldsout.contains("tokenstart"))
                        out.append("\t").append(String.valueOf(tokenposition.get(i)));
                    if (fieldsout.contains("tokenend"))
                        out.append("\t").append(String.valueOf(tokenposition.get(i) + wTemp.length()));
                    if (fieldsout.contains("tokentype"))
                        out.append("\t").append(normText.getOrthoType(wTemp));
                    if (!disableTokenization)
                        out.append("\n");
                    if (i < tokenposition.size()-1) {
                        i++;
                        //System.out.println("{"+wTemp+"}");
                        if(!disableSentenceSplitting && lexpars.hasEndSentenceChar(wTemp)) {
                            //out.append("\n");
                            endSentenceCase = 1;
                        }
                    }
                } else{
                    // EOS end of sentence empty line
                    endSentenceCase = 2;
                    out.append("\n");

                }
            }

            //EOL end of line empty line
            if(endSentenceCase == 0 && lexpars.hasEndSentenceChar(String.valueOf("\r"))){
                out.append("\n");
                //here you may find if this set as EOS char in the xml with char id = <char id="13" hexcode="0x0d" desc="CARRIAGE RETURN" />
                // EOS,EOL,emptyline,another EOL
            }
        }
        out.flush();
        out.close();

    }

    
    public void analyze (PipedInputStream filein, PipedOutputStream fileout) throws IOException {
    	lexpars = lis.get(globalLang);
    	//init(params);
    	if (htmlcleaner) {
            //call CleanPro
            try {
                cleanHtml(filein,fileout);
            } catch (Exception e) {
                System.err.println("ERROR! Html cleaner failed.");
                e.printStackTrace();
                System.exit(-1);
            }
        }


        InputStream in = new DataInputStream(filein);
        Reader reader = new InputStreamReader(in, "UTF8");
        BufferedReader br = new BufferedReader(reader);
        OutputStreamWriter out
                = new OutputStreamWriter(new BufferedOutputStream(new DataOutputStream(fileout)), "UTF8");

        out.write("# FIELDS: " + fieldsout.toString().replaceAll("\\[|\\]","").replaceAll(",\\s+","\t") + "\n");

        String line;

        char ch;
        boolean splittedword;
        boolean splittedchar = false;
        int position = -1;
        ArrayList<Integer> tokenposition = new ArrayList<Integer>();
        String tokenNorm;
        int tokenid=0;
        LinkedList<String> temp = new LinkedList<String>();

        StringBuffer wordTemp = new StringBuffer();
        String wTemp;
        ListIterator<String> wordlp;

        while((line = br.readLine()) != null) {
            wordTemp.setLength(0);
            temp.clear();
            tokenposition.clear();
            if (disableTokenization) {
                temp.add(line);
                //System.err.println(position + " " +line);
                tokenposition.add(position);
                position += line.length() + 1;
            } else {
                // do something with line.
                //start tokenizing depending on the spaces and other rules

                for(int chp=0; chp<line.length(); chp++) {
                    ch = line.charAt(chp);
                    splittedword = lexpars.generalSplittingRules(ch);

                    if (!splittedword) {
                        if (wordTemp.length() == 0)
                            tokenposition.add(position+chp+1);
                        //if (wrapper.DEBUG)
                        // System.err.println("CHAR: " +ch+ " " + (int) ch + " U+" + String.format("%04x", line.codePointAt(chp)) + " " +lexpars.charSplitter(ch) + " " + lexpars.generalSplittingRules(ch));
                        if (!splittedchar)
                            splittedchar = lexpars.charSplitter(ch);

                        if (lexpars.charSplitter(ch))
                            wordTemp.append(" ").append(String.valueOf(ch)).append(" ");
                            //wordTemp += " "+String.valueOf(ch)+" ";
                        else
                            wordTemp.append(String.valueOf(ch));
                        //wordTemp += String.valueOf(ch);

                    }

                    //if(splittedword&&lexpars.hasEndSentenceChar(String.valueOf(ch))){
                    //temp.addLast("\n");
                    //}

                    if (splittedword || chp == line.length() -1) {
                        wTemp = wordTemp.toString().replaceAll("\\s+"," ").trim();
                        tokenNorm = lexpars.checkSplitRules(wTemp);
                        //System.err.println(" {" +tokenNorm + "} " + wTemp);

                        if (tokenNorm.length()>0) {
                            /// this is to check the abb.list before reconstructing the wordl

                            if(!disableSentenceSplitting&&( lexpars.containsEndSentenceChar(wTemp)
                                    && lexpars.isAbbreviation(wTemp))) {
                                tokenNorm = tokenNorm.replaceAll("\\s+", "");
                            }

                            //System.err.println(tokenNorm);
                            // reconstruct the list after the spacing!!!
                            //System.out.println("=={"+tokenNorm+"}");
                            String[] teta = tokenNorm.split(" ");
                            if (teta.length > 1) {
                                for(int i=0;i<teta.length;i++) {
                                    if (teta[i].length()>0) {
                                        temp.addLast(teta[i]);
                                        if (i < teta.length -1) {
                                            tokenposition.add(tokenposition.get(tokenposition.size() - 1) + teta[i].trim().length());
                                        }
                                    }
                                    if(i==teta.length-1 &&
                                            !lexpars.isAbbreviation(teta[i]) &&
                                            lexpars.hasEndSentenceChar(String.valueOf(teta[i].charAt(teta[i].length()-1)))){
                                        temp.addLast("\n");
                                    }
                                }
                            } else {
                                temp.addLast(tokenNorm);
                            }

                        }
                        wordTemp.setLength(0);
                        splittedchar = false;
                    }
                }

                if(!disableTokenization && line.length() == 0){
                    ///empty line for the empty line
                    //CG:temp.addLast("\n");
                }
                position += line.length()+1;
            }
            ////////////
            //System.out.println("> "+line);

            /// write the tokenized line in the output file
            wordlp = temp.listIterator();
            int i = 0;
            int endSentenceCase = 0;
            int openParenthesis = 0;

            while(wordlp.hasNext()) {
                wTemp = wordlp.next();
                if(!wTemp.equals("\n")){
                    if (disableTokenization && wTemp.length() == 0) {
                        i++;
                        continue;
                    }

                    //manage the brakes at the beginning of the sentence
                    if ("([{".contains(wTemp.substring(0,1)))
                        openParenthesis++;
                    if (wTemp.contains(")") ||  wTemp.contains("]") || wTemp.contains("]")) {
                        openParenthesis = openParenthesis-1;
                        if (openParenthesis < 0)
                            openParenthesis=0;
                    }
                    if (!disableSentenceSplitting &&
                            endSentenceCase == 1 &&  openParenthesis == 0 &&
                            (openParenthesis > 0 || !normText.isInterpunzione(wTemp.substring(0,1)))) {
                        out.append("\n");
                    }
                    endSentenceCase = 0;
                    if (fieldsout.contains("token"))
                        out.append(wTemp);
                    if (fieldsout.contains("tokennorm"))
                    	out.append("\t").append(normText.normalize(wTemp,globalLang));
                    if (fieldsout.contains("tokenid"))
                        out.append("\t").append(String.valueOf(++tokenid));
                    if (fieldsout.contains("tokenstart"))
                        out.append("\t").append(String.valueOf(tokenposition.get(i)));
                    if (fieldsout.contains("tokenend"))
                        out.append("\t").append(String.valueOf(tokenposition.get(i) + wTemp.length()));
                    if (fieldsout.contains("tokentype"))
                        out.append("\t").append(normText.getOrthoType(wTemp));
                    if (!disableTokenization)
                        out.append("\n");
                    if (i < tokenposition.size()-1) {
                        i++;
                        //System.out.println("{"+wTemp+"}");
                        if(!disableSentenceSplitting && lexpars.hasEndSentenceChar(wTemp)) {
                            //out.append("\n");
                            endSentenceCase = 1;
                        }
                    }
                } else{
                    // EOS end of sentence empty line
                    endSentenceCase = 2;
                    out.append("\n");

                }
            }

            //EOL end of line empty line
            if(endSentenceCase == 0 && lexpars.hasEndSentenceChar(String.valueOf("\r"))){
                out.append("\n");
                //here you may find if this set as EOS char in the xml with char id = <char id="13" hexcode="0x0d" desc="CARRIAGE RETURN" />
                // EOS,EOL,emptyline,another EOL
            }
        }
        out.flush();
        out.close();

    }

    
    
    public OBJECTDATA analyze (OBJECTDATA filein,toolbox tools) throws IOException {
    	
    	
    	if (lis.size() == 0) {
    		
    		int nbPar = 4;
    		
    		if (tools.variables.isHtmlcleaner()){
    			nbPar += 2;
    		}
    		if (!tools.variables.getDisable().equals("")){
    			nbPar += 2;
    		}
    		
    		String [] params = new String [nbPar];
    		params[0] = "-l";
    		params[1] = tools.variables.getLanguage();
    		params[2] = "-c";
    		params[3] = tools.variables.getUserModelsToRun();
    		//System.err.println(params[3]);
    		
    		int j=4;
    	    		
    		if (tools.variables.isHtmlcleaner()){
    			params[j] = "-html";
    			params[j+1] = "yes";
    			j+=2;
    		}
    		if (!tools.variables.getDisable().equals("")){
    			params[j] = "-dis";
    			params[j+1] = tools.variables.getDisable();
    		}	
    				
    		init(params, tools.variables.getProp());
    		
    	}
    	
    	String lang = tools.variables.getLanguage().toLowerCase().substring(0, 3);
    	
    	lexpars = lis.get(tools.variables.getLanguage().toLowerCase().substring(0, 3));
    	
    	//System.err.println(tools.variables.getLanguage().toLowerCase().substring(0, 3));
    	
        OBJECTDATA outFile = new OBJECTDATA();
    	LinkedList<String> tokenValues = new LinkedList<String>();
    	LinkedList<String> tokennormValues = new LinkedList<String>();
    	LinkedList<String> tokenidValues = new LinkedList<String>();
    	LinkedList<String> tokenstartValues = new LinkedList<String>();
    	LinkedList<String> tokenendValues = new LinkedList<String>();
    	LinkedList<String> tokentypeValues = new LinkedList<String>();
    	
    	
    	
    	
    	if(filein==null){
    		System.err.println("TokenPro: Input file or Output file is null!");
            System.exit(-1);
    	}
        if (htmlcleaner) {
            //call CleanPro
            try {
               //TODO cleanHtml(new File(filein));
            } catch (Exception e) {
                System.err.println("ERROR! Html cleaner failed.");
                e.printStackTrace();
                System.exit(-1);
            }
        }

       // outFile.updateHeaderList("# FIELDS: " + fieldsout.toString().replaceAll("\\[|\\]","").replaceAll(",\\s+","\t") + "\n", false);


        char ch;
        boolean splittedword;
        boolean splittedchar = false;
        int position = -1;
        ArrayList<Integer> tokenposition = new ArrayList<Integer>();
        String tokenNorm;
        int tokenid=0;
        LinkedList<String> temp = new LinkedList<String>();

        StringBuffer wordTemp = new StringBuffer();
        String wTemp;
        ListIterator<String> wordlp;

        for(String line : filein.getFileLineByLine()) {
        	
            wordTemp.setLength(0);
            temp.clear();
            tokenposition.clear();
            if (disableTokenization) {
                temp.add(line);
                //System.err.println(position + " " +line);
                tokenposition.add(position);
                position += line.length() + 1;
            } else {
                // do something with line.
                //start tokenizing depending on the spaces and other rules

                for(int chp=0; chp<line.length(); chp++) {
                    ch = line.charAt(chp);
                    splittedword = lexpars.generalSplittingRules(ch);

                    if (!splittedword) {
                        if (wordTemp.length() == 0)
                            tokenposition.add(position+chp+1);
                        //if (wrapper.DEBUG)
                        // System.err.println("CHAR: " +ch+ " " + (int) ch + " U+" + String.format("%04x", line.codePointAt(chp)) + " " +lexpars.charSplitter(ch) + " " + lexpars.generalSplittingRules(ch));
                        if (!splittedchar)
                            splittedchar = lexpars.charSplitter(ch);

                        if (lexpars.charSplitter(ch))
                            wordTemp.append(" ").append(String.valueOf(ch)).append(" ");
                            //wordTemp += " "+String.valueOf(ch)+" ";
                        else
                            wordTemp.append(String.valueOf(ch));
                        //wordTemp += String.valueOf(ch);

                    }

                    //if(splittedword&&lexpars.hasEndSentenceChar(String.valueOf(ch))){
                    //temp.addLast("\n");
                    //}

                    if (splittedword || chp == line.length() -1) {
                        wTemp = wordTemp.toString().replaceAll("\\s+"," ").trim();
                        tokenNorm = lexpars.checkSplitRules(wTemp);
                        //System.err.println(" {" +tokenNorm + "} " + wTemp);

                        if (tokenNorm.length()>0) {
                            /// this is to check the abb.list before reconstructing the wordl

                            if(!disableSentenceSplitting&&( lexpars.containsEndSentenceChar(wTemp)
                                    && lexpars.isAbbreviation(wTemp))) {
                                tokenNorm = tokenNorm.replaceAll("\\s+", "");
                            }

                            //System.err.println(tokenNorm);
                            // reconstruct the list after the spacing!!!
                            //System.out.println("=={"+tokenNorm+"}");
                            String[] teta = tokenNorm.split(" ");
                            if (teta.length > 1) {
                                for(int i=0;i<teta.length;i++) {
                                    if (teta[i].length()>0) {
                                        temp.addLast(teta[i]);
                                        if (i < teta.length -1) {
                                            tokenposition.add(tokenposition.get(tokenposition.size() - 1) + teta[i].trim().length());
                                        }
                                    }
                                    if(i==teta.length-1 &&
                                            !lexpars.isAbbreviation(teta[i]) &&
                                            lexpars.hasEndSentenceChar(String.valueOf(teta[i].charAt(teta[i].length()-1)))){
                                        temp.addLast("\n");
                                    }
                                }
                            } else {
                                temp.addLast(tokenNorm);
                            }

                        }
                        wordTemp.setLength(0);
                        splittedchar = false;
                    }
                }

                if(!disableTokenization && line.length() == 0){
                    ///empty line for the empty line
                    //CG:temp.addLast("\n");
                }
                position += line.length()+1;
            }
            ////////////
            //System.out.println("> "+line);

            /// write the tokenized line in the output file
            wordlp = temp.listIterator();
            int i = 0;
            int endSentenceCase = 0;
            int openParenthesis = 0;

            while(wordlp.hasNext()) {
                wTemp = wordlp.next();
                if(!wTemp.equals("\n") && !wTemp.equals("")){
                    if (disableTokenization && wTemp.length() == 0) {
                        i++;
                        continue;
                    }

                    //manage the brakes at the beginning of the sentence
                    if ("([{".contains(wTemp.substring(0,1)))
                        openParenthesis++;
                    if (wTemp.contains(")") ||  wTemp.contains("]") || wTemp.contains("]")) {
                        openParenthesis = openParenthesis-1;
                        if (openParenthesis < 0)
                            openParenthesis=0;
                    }
                    if (!disableSentenceSplitting  &&
                            endSentenceCase == 1 &&  openParenthesis == 0 &&
                            (openParenthesis > 0 || !normText.isInterpunzione(wTemp.substring(0,1)))) {
                        tokenValues.add("");
                        tokennormValues.add("");
                    	tokenidValues.add("");
                    	tokenstartValues.add("");
                    	tokenendValues.add("");
                    	tokentypeValues.add("");
                    }
                    endSentenceCase = 0;
                    //if (fieldsout.contains("token"))
                    	tokenValues.add(wTemp);
                    //if (fieldsout.contains("tokennorm"))
                    	tokennormValues.add(normText.normalize(wTemp,lang));
                    //if (fieldsout.contains("tokenid"))
                    	tokenidValues.add(String.valueOf(++tokenid));
                    //if (fieldsout.contains("tokenstart"))
                    	tokenstartValues.add(String.valueOf(tokenposition.get(i)));
                    //if (fieldsout.contains("tokenend"))
                    	tokenendValues.add(String.valueOf(tokenposition.get(i) + wTemp.length()));
                    //if (fieldsout.contains("tokentype"))
                    	tokentypeValues.add(normText.getOrthoType(wTemp));
                    if (!disableTokenization){
                    	//This one when we create the file line by line printing it, but in our case it is not needed
                    	// as every record related to the returned list will be on it's own line.
                    	/* tokenValues.add("");
                         tokennormValues.add("");
                     	tokenidValues.add("");
                     	tokenstartValues.add("");
                     	tokenendValues.add("");
                     	tokentypeValues.add("");*/
                    }
                    if (i < tokenposition.size()-1) {
                        i++;
                        //System.out.println("{"+wTemp+"}");
                        if(!disableSentenceSplitting && lexpars.hasEndSentenceChar(wTemp)) {
                            //out.append("\n");
                            endSentenceCase = 1;
                        }
                    }
                } else{
                    // EOS end of sentence empty line
                    endSentenceCase = 2;
                    tokenValues.add("");
                    tokennormValues.add("");
                	tokenidValues.add("");
                	tokenstartValues.add("");
                	tokenendValues.add("");
                	tokentypeValues.add("");

                }
            }

            //EOL end of line empty line
            if(!disableTokenization && endSentenceCase == 0 && lexpars.hasEndSentenceChar(String.valueOf("\r"))){
            	tokenValues.add("");
                tokennormValues.add("");
             	tokenidValues.add("");
             	tokenstartValues.add("");
             	tokenendValues.add("");
             	tokentypeValues.add("");
                //here you may find if this set as EOS char in the xml with char id = <char id="13" hexcode="0x0d" desc="CARRIAGE RETURN" />
                // EOS,EOL,emptyline,another EOL
            }
        }
        
        
        
        
    	
    	outFile.addColumn("token", tokenValues );
    	outFile.addColumn("tokennorm", tokennormValues );
    	outFile.addColumn("tokenid", tokenidValues );
    	outFile.addColumn("tokenstart", tokenstartValues );
    	outFile.addColumn("tokenend", tokenendValues );
    	outFile.addColumn("tokentype", tokentypeValues );
    	
		return outFile;
    }

    
}
