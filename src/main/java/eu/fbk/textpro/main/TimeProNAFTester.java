package eu.fbk.textpro.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

public class TimeProNAFTester {
	 static final private String encoding = "UTF8";

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		boolean stdout =false;
		//System.out.println(args[0]+"="+args[1]+"="+args[2]);
		if(args.length >= 3){
			stdout= Boolean.valueOf(args[2]);
		}else{
			System.err.println("TimeProTester error: $inputFile $outputFile $outputType(StdOut:true;false)");
			System.exit(0);
		}
		
		String nafFile = null;
		String outFile = null;
		if (args.length < 1) {
			System.err.println("TimeProTester error: No input or output file!");
			System.exit(0);
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
		
		
		
		
		final String inputF= nafFile;
		final File originalFile = new File(inputF);
		
		final String outputF=outFile;
		
		
		File out=new File(outputF);
		if(out!=null&&out.exists()&&out.isFile()){
			
		}else{
			System.err.println("There were problems while running TimeProNAF process, we will return the input file as the output of the process!");
			if(originalFile!=null&&originalFile.exists()&&originalFile.isFile())
				copyFile(originalFile,outputF);
			else
				System.err.println("TimeProNAF problem: No input file and No output File!!!");
		}
		
		
		if(stdout)
			stdOutput(out.getAbsolutePath());
	}
	
	static void stdOutput(String stdout) throws IOException{
		 FileInputStream in = new FileInputStream(stdout);
	        Reader reader = new InputStreamReader(in, encoding);
	        BufferedReader br = new BufferedReader(reader);
		String input;
		while ((input = br.readLine()) != null) {
			System.out.println(input);
		}
		in.close();
	}
	  static void copyFile(File source, String target) {
	        if (source == null || !source.exists())
	            return;
	        try{

	            InputStream inStream = new FileInputStream(source);
	            OutputStream outStream = new FileOutputStream(new File(target));

	            byte[] buffer = new byte[1024];

	            int length;
	            while ((length = inStream.read(buffer)) > 0) {
	                outStream.write(buffer, 0, length);
	            }

	            inStream.close();
	            outStream.close();

	            //System.out.println("File copied into " + target);
	        }catch(IOException e) {
	            e.printStackTrace();
	        }
	    }
}