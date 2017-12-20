package eu.fbk.textpro.api;

import java.util.Hashtable;
import java.util.Iterator;

import eu.fbk.textpro.api.column;
import eu.fbk.textpro.api.outputType;

public class module {
	 Hashtable<String, column> cols = new Hashtable<String, column>();

	public <E extends Enum<E>> module(Class<E> enumData) {

		for (Enum<E> enumVal : enumData.getEnumConstants()) {
			// System.out.println(enumVal.toString());
			createColumn(enumVal.toString(), enumVal.toString());
		}

		/*
		 * tokenizerTypes[] vals =
		 * eu.fbk.textpro.api.tokenizer.tokenizerTypes.values(); for (int
		 * i=0;i<vals.length;i++) { createColumn(vals[i],vals[i]); }
		 */

	}

	private void createColumn(String name, String value) {
		column col = new column();
		col.setName(name);
		col.setValue(value);
		if (!cols.contains(name))
			cols.put(name, col);
		else {
			System.out.println("The column name " + name.toString()
					+ " the old value " + cols.get(name.toString())
					+ " will be replaced to " + value.toString());
			cols.put(name.toString(), col);
		}
	}

	public column get(String columnType) {
		return cols.get(columnType);
	}

	public <E extends Enum<E>> void activateAll(Class<E> classType) {
		for (Enum<E> enumVal : classType.getEnumConstants()) {
			//printHash(cols);
			//System.out.println(cols.size()+"="+enumVal.toString()+"="+cols.containsKey(enumVal.toString()));
			active(enumVal.toString());
		}
	}

	private void printHash(Hashtable cols) {
		Iterator keys = cols.keySet().iterator();
		while(keys.hasNext()){
			System.out.println(keys.next());
		}
	}

	public <E extends Enum<E>> void deactivateAll(Class<E> classType) {
		for (Enum<E> enumVal : classType.getEnumConstants()) {
			deactive(enumVal.toString());
		}
	}

	public void active(String columnType) {
		if (cols.containsKey(columnType)){
			cols.get(columnType.toString()).active();
		}else {
			System.err
					.println("Please use a type related to the module which you need to active");
			System.exit(-1);
		}
	}

	public void deactive(String columnType) {
		if (cols.containsKey(columnType)){
			cols.get(columnType.toString()).deactive();
		}else {
			System.err
					.println("Please use a type related to the module which you need to active");
			System.exit(-1);
		}
	}

}
