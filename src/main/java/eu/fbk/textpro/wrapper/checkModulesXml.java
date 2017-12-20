package eu.fbk.textpro.wrapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBException;

import eu.fbk.textpro.toolbox.MYProperties;
import eu.fbk.textpro.toolbox.TEXTPROCONSTANT;
import eu.fbk.textpro.toolbox.toolbox;
import eu.fbk.textpro.wrapper.Textpro.Modules.Module;
import eu.fbk.textpro.wrapper.Textpro.Modules.Module.Input.Field;


public class checkModulesXml {

    static Textpro myFile;
    static LinkedHashMap<String, String> moduleslist = new LinkedHashMap<String, String>();
    static LinkedHashMap<String, String> inputColumns = new LinkedHashMap<String, String>();
    public static LinkedHashMap<String, String> outputColumns = new LinkedHashMap<String, String>();
    public static LinkedHashMap<String, String> outputHeaders = new LinkedHashMap<String, String>();

    public checkModulesXml(toolbox toolbox) throws IOException {
        try {
            checkModules(toolbox);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        
       
        getInputColumns();
        getOutputColumns();
        getOutputHeaders();
        
    }

      public void main(String[] args) throws JAXBException, IOException {
    	  
    	  String conf = args[0];
			System.err.println("Conf file: " + conf);
			MYProperties prop = new MYProperties(conf);
			toolbox tools = new toolbox(prop);        //check existence of modules and sort them depending on the dependence on each other.
        // if a module doesnot exsit exit!
        checkModules(tools);
        System.out.println("======================");
        System.out.println("===Existing Modules===");
        System.out.println("==(column => module)==");
        System.out.println("======================");
        printHash(moduleslist);
        System.out.println("======================");
        System.out.println("===Required Columns===");
        System.out.println("==(column => module)==");
        System.out.println("======================");
        getInputColumns();
        printHash(inputColumns);
        System.out.println("======================");
        System.out.println("===Produced Columns===");
        System.out.println("==(column => module)==");
        System.out.println("======================");
        getOutputColumns();
        printHash(outputColumns);

        //printColumnsWhichCouldnotProduce();

        System.out.println("Checking is finished successfully!");

    }



    private static void printColumnsWhichCouldnotProduce() {
        LinkedHashMap<String, String> minus = new LinkedHashMap<String, String>();
        boolean foundOne = false;
        for(String inptmp :inputColumns.keySet()){
            //System.err.println(inptmp+"="+outputColumns.containsKey(inptmp));
            if (!outputColumns.containsKey(inptmp)) {
                minus.put(inptmp, inputColumns.get(inptmp));
                foundOne=true;
            }
        }

        if (foundOne) {
            System.out.println("===================================");
            System.out.println("=====Columns couldn't produce======");
            System.out.println("==(required column=> module name)==");
            System.out.println("===================================");
            printHash(minus);

        }

    }



    private static void getOutputColumns() {
        outputColumns.clear();
        ListIterator<Module> modulesl = myFile.getModules().getModule().listIterator();
        while(modulesl.hasNext()) {
            Module moduletmp = modulesl.next();
            ListIterator<eu.fbk.textpro.wrapper.Textpro.Modules.Module.Output.Field> inpl = moduletmp.getOutput().getField().listIterator();
            
            while(inpl.hasNext()) {
                eu.fbk.textpro.wrapper.Textpro.Modules.Module.Output.Field inptmp = inpl.next();
                //System.err.println("COLUMN: " + inptmp.getName() +", " + moduletmp.getName());
                outputColumns.put(inptmp.getName(), moduletmp.getName());
            }
        }
    }

    private static void getOutputHeaders() {
        outputHeaders.clear();
        ListIterator<Module> modulesl = myFile.getModules().getModule().listIterator();
        while(modulesl.hasNext()) {
            Module moduletmp = modulesl.next();
            ListIterator<eu.fbk.textpro.wrapper.Textpro.Modules.Module.Output.Header> inpl = moduletmp.getOutput().getHeader().listIterator();
            while(inpl.hasNext()) {
                eu.fbk.textpro.wrapper.Textpro.Modules.Module.Output.Header inptmp = inpl.next();
                //System.err.println("HEADER: " + inptmp.getName() +", " + moduletmp.getName());
                outputHeaders.put(inptmp.getName(), moduletmp.getName());
            }
        }
    }

    private static void getInputColumns() {
        inputColumns.clear();
        ListIterator<Module> modulesl = myFile.getModules().getModule().listIterator();
        while(modulesl.hasNext()) {
            Module moduletmp = modulesl.next();
            ListIterator<Field> inpl = moduletmp.getInput().getField().listIterator();
            
            while(inpl.hasNext()) {
                Field inptmp = inpl.next();
                inputColumns.put(inptmp.getName(), moduletmp.getName());
            }
        }
    }

    private void checkModules(toolbox tools) throws JAXBException, IOException {
		myFile=(Textpro) tools.getConfigFileReader().read("modules.xml","eu.fbk.textpro.wrapper");
        moduleslist.clear();
        ListIterator<Module> modulesl = myFile.getModules().getModule().listIterator();
        boolean errorModules = false;
        int i=1;
        while(modulesl.hasNext()) {
            Module moduletmp = modulesl.next();
            Object classObject = createObjectOfAModuleClass(moduletmp.getCmd().getValue());
            if (classObject == null) {
                errorModules = true;
            }else{
            	moduleslist.put(moduletmp.getName(), String.valueOf(i));
                i++;
            }
        }
        if (errorModules) {
            System.err.println("# Check is failed, there are one or more modules definitions wrong.");
            System.exit(-1);
        }
    }


    static void printHash(LinkedHashMap<String, String> hashmap) {
    	for (Object lltmp : hashmap.keySet()) {
    		System.out.println(lltmp.toString() + "=>" + hashmap.get(lltmp));
    	}
    }

    private  Object createObjectOfAModuleClass(String className) {
        Object obj = null;
        try {
        	Class  clazz = Class.forName(className);
            //Class[] parameters = new Class[] {Object[].class};
            obj = clazz.newInstance();
        } catch (ClassNotFoundException e) {
            System.err.println("The "+className+" module classes couldn't be found!");
            //e.printStackTrace();
        } catch (InstantiationException e) {
            System.err.println("We couldn't intiate an object of "+className+" module, please check that you inherate the module interface class!");
            //e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.err.println("The "+className+" module classes are protected, we couldn't access them!");
            //e.printStackTrace();
        }

        return obj;
    }






    public int checkUserTaskAccomplishable(String userModelsToRun) throws UnsupportedEncodingException, FileNotFoundException, JAXBException {
        String[] modlist = Pattern.compile("\\+").split(userModelsToRun);
        //Set outl = outputColumns.keySet();
        Hashtable<String, Integer> notFound = new Hashtable<String, Integer>();

        for(int i =0;i<modlist.length;i++) {
            if (!outputColumns.containsKey(modlist[i]) && !outputHeaders.containsKey(modlist[i])) {
                notFound.put(modlist[i], 0);
            }
        }

        if (notFound.size() > 0) {
            System.out.println("=====================================");
            System.out.println("=== Columns could not be produced ===");
            
            for(   Object tmp :notFound.keySet()){
                System.out.println("{"+tmp+"} " + outputColumns.size());
            }
            System.out.println("=====================================");
            System.err.println("We could not run the system as the above columns could not be produced by TextPro, please check modules.xml file!");
            //System.exit(-1);
            return TEXTPROCONSTANT.process_error_continue;
        }
        return TEXTPROCONSTANT.process_ok;

    }

}
