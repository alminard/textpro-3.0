package eu.fbk.textpro.modules.lemmapro;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import eu.fbk.textpro.TextProModuleInterface;
import eu.fbk.textpro.toolbox.MYProperties;
import eu.fbk.textpro.toolbox.TEXTPROCONSTANT;
import eu.fbk.textpro.toolbox.toolbox;
import eu.fbk.textpro.wrapper.OBJECTDATA;
import eu.fbk.textpro.wrapper.TextProPipeLine;


/**
 * @author qwaider
 *
 */

public class LemmaPro implements TextProModuleInterface {
    String language = "eng";
    lemmaConfig lemmaPosMatcher = null;
    static Hashtable<String,lemmaConfig> lis= new Hashtable<String,lemmaConfig>();
    
    public void init(String[] params,MYProperties prop) throws FileNotFoundException, UnsupportedEncodingException {
        for (int i=0; i<params.length; i++) {
            if (params[i] != null) {
                /*if (params[i].toString().equalsIgnoreCase("-f")) {
                 file= params[i+1].toString();
                 System.out.println("lemmapro file to read ="+file);
                 in = new FileInputStream(file);
                }else*/
                /*if (params[i].toString().equalsIgnoreCase("-s")) {
                    String myString = params[i+1].toString();
                     in = new ByteArrayInputStream( myString.getBytes( "UTF8" ) );
                }*/
                /*else if (params[i].toString().equalsIgnoreCase("-stdin")) {
                     in = (System.in) ;

                }else
                */
                if (params[i].equalsIgnoreCase("-l")) {
                    language = params[i+1];
                }
                /*else if (file.length()==0&& i == params.length-1) {
                    System.out.println("Error of calling lemmapro! "+params[i].toString());
                    System.exit(-1);
                }*/
            }
        }
        try {
        	
            lemmaConfig test = new lemmaConfig();
            test.readConfigFile("ita");
            lis.put("ita", test);

            lemmaConfig test2 = new lemmaConfig();
            test2.readConfigFile("eng");
            lis.put("eng", test2);
            
            lemmaConfig test3 = new lemmaConfig();
            test3.readConfigFile("fre");
            lis.put("fre", test3);
            
            System.out.println("INIT lemmapro");
            
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void analyze (String filein, String fileout) throws IOException, JAXBException{
    	lemmaPosMatcher=lis.get(language);
        InputStream in = new FileInputStream(filein);
        Reader reader = new InputStreamReader(in, "UTF8");
        BufferedReader br = new BufferedReader(reader);
        File fileDir = new File(fileout);

        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), "UTF8"));
        String line;

        String[] lineTokens;
        StringBuffer comp_morpho = new StringBuffer();

        while((line = br.readLine()) != null) {
            comp_morpho.setLength(0);
            String lemmas = " ";
            lineTokens = Pattern.compile("\t").split(line);
            if (lineTokens.length>2) {
                //System.out.println(i+"line="+lineTokens[2]);
                String[] full_morpho = Pattern.compile(" ").split(lineTokens[2]);
                //confirm+v+indic+past confirm+v+part+past confirmed+adj+zero i should check one by one.
                for (String morpho : full_morpho) {
                	//System.err.println("lineTokens[1]:"+lineTokens[1]);
                	//System.err.println("morpho:"+morpho);
                    if (lemmaPosMatcher.morphoMatcher(lineTokens[1], morpho)) {
                        String lemma = "";
                        comp_morpho.append(" ").append(morpho);
                        if (morpho.contains("~") && morpho.contains("/")) {
                            String[] morphos = morpho.split("/");
                            for (int i = 0; i<morphos.length; i++) {
                                if (i>0) {
                                    lemma+="/";
                                }
                                lemma+= morphos[i].replaceFirst("\\+.*","").replaceFirst(".*~","");
                            }
                        } else {
                            lemma = morpho.replaceFirst("\\+.*","");
                        }
                        if (!lemmas.contains(" "+lemma+" "))
                            lemmas += lemma+ " ";
                    }
                }

                out.append(lineTokens[0]).append("\t").append(lineTokens[1]).append("\t").append(lineTokens[2]).append("\t");
                if (comp_morpho.length() == 0) {
                    out.append(TEXTPROCONSTANT.NULL);
                    if (lineTokens[0].matches("^[^\\w]+$"))
                        out.append("\t").append(TEXTPROCONSTANT.NULL);
                    else if (lineTokens[0].substring(0,1).matches("[A-Z]"))
                        out.append("\t").append(lineTokens[0]);
                    else
                        out.append("\t").append(lineTokens[0].toLowerCase());
                } else {
                    out.append(comp_morpho.toString().trim()).append("\t").append(lemmas.trim());
                }
                out.append("\t").append(lemmaPosMatcher.getWNpos(lineTokens[1])).append("\n");
            } else {
                out.append(line).append("\n");
            }
        }
        out.flush();
        out.close();
    }

    public void analyze (String filein, String fileout,toolbox tools) throws IOException, JAXBException{
    	lemmaPosMatcher=lis.get(tools.variables.getLanguage().toLowerCase().substring(0, 3));
        InputStream in = new FileInputStream(filein);
        Reader reader = new InputStreamReader(in, "UTF8");
        BufferedReader br = new BufferedReader(reader);
        File fileDir = new File(fileout);

        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), "UTF8"));
        String line;

        String[] lineTokens;
        StringBuffer comp_morpho = new StringBuffer();

        while((line = br.readLine()) != null) {
            comp_morpho.setLength(0);
            String lemmas = " ";
            lineTokens = Pattern.compile("\t").split(line);
            if (lineTokens.length>2) {
                //System.out.println(i+"line="+lineTokens[2]);
                String[] full_morpho = Pattern.compile(" ").split(lineTokens[2]);
                //confirm+v+indic+past confirm+v+part+past confirmed+adj+zero i should check one by one.
                for (String morpho : full_morpho) {
                	//System.err.println("lineTokens[1]:"+lineTokens[1]);
                	//System.err.println("morpho:"+morpho);
                    if (lemmaPosMatcher.morphoMatcher(lineTokens[1], morpho)) {
                        String lemma = "";
                        comp_morpho.append(" ").append(morpho);
                        if (morpho.contains("~") && morpho.contains("/")) {
                            String[] morphos = morpho.split("/");
                            for (int i = 0; i<morphos.length; i++) {
                                if (i>0) {
                                    lemma+="/";
                                }
                                lemma+= morphos[i].replaceFirst("\\+.*","").replaceFirst(".*~","");
                            }
                        } else {
                            lemma = morpho.replaceFirst("\\+.*","");
                        }
                        if (!lemmas.contains(" "+lemma+" "))
                            lemmas += lemma+ " ";
                    }
                }

                out.append(lineTokens[0]).append("\t").append(lineTokens[1]).append("\t").append(lineTokens[2]).append("\t");
                if (comp_morpho.length() == 0) {
                    out.append(TEXTPROCONSTANT.NULL);
                    if (lineTokens[0].matches("^[^\\w]+$"))
                        out.append("\t").append(TEXTPROCONSTANT.NULL);
                    else if (lineTokens[0].substring(0,1).matches("[A-Z]"))
                        out.append("\t").append(lineTokens[0]);
                    else
                        out.append("\t").append(lineTokens[0].toLowerCase());
                } else {
                    out.append(comp_morpho.toString().trim()).append("\t").append(lemmas.trim());
                }
                out.append("\t").append(lemmaPosMatcher.getWNpos(lineTokens[1])).append("\n");
            } else {
                out.append(line).append("\n");
            }
        }
        out.flush();
        out.close();
    }
    public OBJECTDATA analyze(OBJECTDATA filein, toolbox tools) throws UnsupportedEncodingException, FileNotFoundException, JAXBException{

    	if(lis.size() == 0) {
    		String [] params = {"-l", tools.variables.getLanguage()};
    		init(params,tools.variables.getProp());
    	}

    	lemmaPosMatcher=lis.get(tools.variables.getLanguage().toLowerCase().substring(0, 3));

    	
        String[] lineTokens;
        StringBuffer comp_morpho = new StringBuffer();
        
		LinkedList<String> comp_morpho_values = new LinkedList<String>();
		LinkedList<String> wnpos_values = new LinkedList<String>();
		LinkedList<String> lemma_values = new LinkedList<String>();		
		
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("token",1);
		copyThisTokens.put("pos",2);
		copyThisTokens.put("full_morpho",3);
		
		Iterable<String> lines = filein.getFileLineByLine(copyThisTokens,true);
		
		Iterator it = lines.iterator();
		int cpt = 0;
		while(it.hasNext()){
			String ltmp=(String) it.next();
			if(ltmp.trim().startsWith("# FIELDS: ")){
				continue;
			}
			
			if (ltmp.contains("\t")) {
				//for( String line:filein.getFileAsList()) {
	            comp_morpho.setLength(0);
	            String lemmas = " ";
	            //lineTokens = Pattern.compile("\t").split(line);
	            lineTokens = Pattern.compile("\t").split(ltmp);
	            if (lineTokens.length>2) {
	                //System.out.println(i+"line="+lineTokens[2]);
	                String[] full_morpho = Pattern.compile(" ").split(lineTokens[2]);
	                //confirm+v+indic+past confirm+v+part+past confirmed+adj+zero i should check one by one.
	                for (String morpho : full_morpho) {
	                	//System.err.println("lineTokens[1]:"+lineTokens[1]);
	                	//System.err.println("morpho:"+morpho);
	                    if (lemmaPosMatcher.morphoMatcher(lineTokens[1], morpho)) {
	                        String lemma = "";
	                        comp_morpho.append(" ").append(morpho);
	                        if (morpho.contains("~") && morpho.contains("/")) {
	                            String[] morphos = morpho.split("/");
	                            for (int i = 0; i<morphos.length; i++) {
	                                if (i>0) {
	                                    lemma+="/";
	                                }
	                                lemma+= morphos[i].replaceFirst("\\+.*","").replaceFirst(".*~","");
	                            }
	                        } else {
	                            lemma = morpho.replaceFirst("\\+.*","");
	                        }
	                        if (!lemmas.contains(" "+lemma+" "))
	                            lemmas += lemma+ " ";
	                    }
	                }
	
	                if (comp_morpho.length() == 0) {
	                	comp_morpho_values.addLast(TEXTPROCONSTANT.NULL);
	                    if (lineTokens[0].matches("^[^\\w]+$"))
	                      lemma_values.addLast(TEXTPROCONSTANT.NULL);
	                    else if (lineTokens[0].substring(0,1).matches("[A-Z]"))
	                    	lemma_values.addLast(lineTokens[0]);
	                    else
	                    	lemma_values.addLast(lineTokens[0].toLowerCase());
	                } else {
	                	comp_morpho_values.addLast(comp_morpho.toString().trim());
	                	if (lemmas.contains(" ")) {
	                		String [] lemmaList = lemmas.split(" ");
	                		for (String l : lemmaList) {
	                			if (l.equals(lineTokens[0].toLowerCase())) {
	                				lemmas = l;
	                				break;
	                			}
	                		}
	                	}
	                    lemma_values.addLast(lemmas.trim());
	                }
	                wnpos_values.addLast(lemmaPosMatcher.getWNpos(lineTokens[1]));
	            }
	            else {
	            	comp_morpho_values.addLast("");
	            	lemma_values.addLast("");
	            	wnpos_values.addLast("");
	            }
			}
			else {
            	comp_morpho_values.addLast("");
            	lemma_values.addLast("");
            	wnpos_values.addLast("");
            }
        }
        filein.addColumn("comp_morpho", comp_morpho_values);
        filein.addColumn("lemma", lemma_values);
        filein.addColumn("wnpos", wnpos_values);
        return filein;
    }
}
