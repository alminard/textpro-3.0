/**
 * 
 */
package eu.fbk.textpro.toplevel;

import eu.fbk.textpro.wrapper.Textpro.Modules.Module.Output;

/**
 * @author qwaider
 *
 */
 public class textproModule {
	String moduleName="";
	Output optionValue=null;
	boolean status=false;
	 void activate(){
		 status=true;
	 }
	 void deactivate(){
		 status=false;
	 }
}
