package eu.fbk.textpro.toolbox;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import eu.fbk.textpro.wrapper.Textpro;
import eu.fbk.textpro.wrapper.Textpro.Modules.Module;
import eu.fbk.textpro.wrapper.Textpro.Modules.Module.Params;

public class initiateAllModules {
	boolean VERBOSE = true;
	String language = "english";
	public void initiate(List<Module> modules, MYProperties prop){
		try{
		for (Module modt : modules) {
			long nowtime = new Date().getTime();
			String[] parms = getParms(modt.getParams());
		String className = modt.getCmd().getValue();
		// Load the Class. Must use fully qualified name here!
		Class clazz = Class.forName(className);
		// I need an array as follows to describe the signature
		Class[] parameters = new Class[] { String[].class,MYProperties.class };
		Method method = clazz.getMethod("init", parameters);
		Object obj = clazz.newInstance();
		method.invoke(obj, new Object[] { parms ,prop});
		System.out.println(className+" module has been initiated."+((VERBOSE)?("("+(new Date().getTime() - nowtime ) + "ms)"):""));
		
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public initiateAllModules() {
	}


	String[] getParms(Params params){
		String[] parms = null;
		if (params != null) {
			parms = new String[params.getParam().size() * 2 + 2];
			Iterator<Textpro.Modules.Module.Params.Param> parml = params
					.getParam().iterator();
			int q = 0;
			while (parml.hasNext()) {
				Textpro.Modules.Module.Params.Param parmtmp = parml.next();
				String ff = "";
				String tt = "";
				if (parmtmp.getValue().contains("$inputFile")) {
					
				} else if (parmtmp.getValue().contains("$outputFile")) {
					
				} else if (parmtmp.getValue().contains("$language")) {
					// cut the string from the start till reaching {$language}
					String gg = parmtmp.getValue()
							.replaceAll("\\$language", "");
					gg = gg.replaceAll(" ", "");
					tt = gg;
					ff = language;
				} else if (parmtmp.getValue().contains("$kxparams")) {
					
				} else if (parmtmp.getValue().contains("$disable")) {
					
				} else if (parmtmp.getValue().contains("-html")) {
					
				} else {
					tt = parmtmp.getValue();
					/*
					 * if (tt.contains(" ")) { tt = tt.replaceAll("\\s+", " ");
					 * ff = tt.substring(tt.indexOf(" ")+1); tt =
					 * tt.substring(0,tt.indexOf(" ")); }
					 */
				}
				// cmd+=tt+" ";
				if (tt.length() > 0) {
					parms[q] = tt;
					q++;
				}

				if (ff != null && ff.length() > 0) {
					// if (ff.length()>0) {
					parms[q] = ff;
					q++;
					// Arrays.fill(parms, ff);
					// cmd+=ff+" ";
				}
			}
			
		}
		
		return parms;
	}
}
