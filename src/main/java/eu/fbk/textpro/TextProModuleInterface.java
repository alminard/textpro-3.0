package eu.fbk.textpro;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import javax.xml.bind.JAXBException;

import eu.fbk.textpro.toolbox.MYProperties;
import eu.fbk.textpro.toolbox.toolbox;
import eu.fbk.textpro.wrapper.OBJECTDATA;

/**
 * Created by IntelliJ IDEA.
 * User: cgirardi & Qwaider
 * Date: 18-Mar-2014
 * Time: 9.36.42
 */
public interface TextProModuleInterface {
    public void init (String[] params,MYProperties prop) throws FileNotFoundException, UnsupportedEncodingException, MalformedURLException;
    public void analyze (String filein, String fileout) throws IOException, JAXBException;
 //   public void analyze (PipedInputStream filein, PipedOutputStream fileout) throws IOException, JAXBException;
  //  public void analyze (PipedReader filein, PipedWriter fileout) throws IOException, JAXBException;

   // public OBJECTDATA analyze (OBJECTDATA filein,toolbox tools) throws IOException;
}