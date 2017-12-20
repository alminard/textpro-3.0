package eu.fbk.textpro.tester;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

public interface TesterInterface {
	@Test
    public void run (Object method, Object input, Object output, Object moduleName) throws IOException;

}
