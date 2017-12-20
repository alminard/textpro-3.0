package eu.fbk.textpro.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

public class ExampleServlet extends HttpServlet {
	@Override
	    protected void doGet(HttpServletRequest request, HttpServletResponse response)
	            throws ServletException, IOException {
	 
		response.setStatus(HttpStatus.OK_200);
	        response.getWriter().println("EmbeddedJetty");
	        response.setContentType("text/html");
	       // response.setStatus(HttpServletResponse.SC_OK);
	        response.getWriter().println("<h1>Hi</h1>");
	      //  response.getWriter().println("session=" + request.getSession(true).getId());
	    }


}
