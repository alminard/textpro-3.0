package eu.fbk.textpro.wrapper;

import java.io.File;
import java.io.PipedOutputStream;
import java.util.LinkedList;

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*FileData test = new FileData();
		test.readData(new File("inputTest.txt"), "utf-8");*/
		//test.printHeaderList();
		//System.err.println("=========");
		//test.printTokenIndexs();
		/*LinkedList<String> as = new LinkedList<String>();
		as.add("token");
		as.add("pos");
		as.add("full_morpho");*/
		/*for(String l:test.getFileLineByLine(as)){
			System.out.println(l);
		}*/
		//test.saveInFile("inT2.txt", "utf-8",as);
		/*
		FileData test = new FileData();
		test.readData(new File("inT.txt"), "utf-8");
		
		FileData test2 = new FileData();
		test2.readData(new File("inT2.txt"), "utf-8");

		FileDataUtil merged = new FileDataUtil();
		FileData done = merged.merge(test, test2);
		done.printHeaderList();
		done.printTokenIndexs();*/
		/*
		FileData test = new FileData();
		test.readData(new File("inT.txt"), "utf-8");
		
		FileData test2 = new FileData();
		test2.readDataFromPipeStream(test.getFileAsPipeStreaming(), "utf-8");
		test2.printHeaderList();*/
		/*FileData tt = new FileData();
		LinkedList<String> columValues=new LinkedList<String>();
		columValues.add("my");
		tt.addColumn("token", columValues);
		tt.addColumn("xx", columValues);
		tt.printHeaderList();
		System.out.println("=====");
		tt.printTokenIndexs();
		for(String l:tt.getFileLineByLine()){
			System.out.println(l);
		}
		System.out.println("######");
		tt.deleteColumn("xx");
		tt.printHeaderList();
		System.out.println("=====");
		tt.printTokenIndexs();
		for(String l:tt.getFileLineByLine()){
			System.out.println(l);
		}
		tt.deleteHeader("asd");*/
		OBJECTDATA tt = new OBJECTDATA();
		tt.readData(new File("Facebook-testset.txt"), "utf8");
		tt.saveInFile("Fa.txt", "utf8",true);
	}

}
