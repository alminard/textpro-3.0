package eu.fbk.textpro.toolbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class FileChecker {
	 boolean isBigFile (File file) {
        // the big file is a file contains at least two files with two # FILE: or two # FIELDS:
        boolean foundHeader = false;
        try{
            String line;
            Reader reader = new InputStreamReader(new FileInputStream(file), TEXTPROCONSTANT.encoding);
            @SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(reader);
            boolean foundFirst=false;
            while((line = br.readLine()) != null) {
                if (line.length() > 0 && (line.startsWith(TEXTPROCONSTANT.headerFields) || line.startsWith(TEXTPROCONSTANT.headerFile))) {
                    //System.err.println(file.getName() + " -- " +line);
                    if (foundFirst) {
                        foundHeader = true;
                        return foundHeader;
                    } else {
                        foundFirst=true;
                    }
                }
            }
            reader.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
        return foundHeader;
    }
}
