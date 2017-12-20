package eu.fbk.textpro.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.eclipse.jetty.http.HttpStatus;

public class uploadServlet extends HttpServlet {
	@Override
	    protected void doGet(HttpServletRequest request, HttpServletResponse resp)
	            throws ServletException, IOException {
		String description = request.getParameter("description"); // Retrieves <input type="text" name="description">
	    Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">
	    String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
	    InputStream fileContent = filePart.getInputStream();
	    // ... (do your job here)
	    resp.setStatus(HttpStatus.OK_200);
        resp.getWriter().println("file: "+fileName);
	}


}
