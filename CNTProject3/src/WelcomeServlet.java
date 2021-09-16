// A simple servlet to process get requests.
// Main servlet in first-example web-app

//import javax.servlet.*;
//import javax.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

public class WelcomeServlet extends HttpServlet {   
   // process "get" requests from clients
   @Override
protected void doGet( HttpServletRequest request, 
                      HttpServletResponse response ) throws ServletException, IOException  {

      response.setContentType( "text/html" );
      PrintWriter out = response.getWriter();  
      // send HTML5 page to client
      // start HTML5 document
      out.println( "<meta charset=\"utf-8\">" );
           // head section of document
      out.println( "<head>" );
	   out.println( "<style type='text/css'>");
	   out.println( "<!--  body{background-color:blue; color:white; font-family:calibri;}");
	   out.println( " h1{font-size:100pt; text-align:center;} h2{font-size:60pt;} ");
	   out.println( " #one{color:magenta;} #two{color:yellow;} #three{color:red;} #four{color:lime;} #five{color:cyan;}");
	   out.println( "-->");
	   out.println( "</style>");
      out.println( "<title>Welcome to Servlets!</title>" );
      out.println( "</head>" );
      // body section of document
      out.println( "<body>" );
      out.println( "<h1><span id=\"one\">H</span><span id=\"two\">e</span><span id=\"three\">l</span>"
      		+ "<span id=\"four\">l</span><span id=\"five\">o</span>!!</h1>");
      out.println( "<h2>Welcome To The Exciting World Of Servlet Technology!</h2>" );
      out.println( "</body>" );
      // end HTML5 document

      out.println( "</html>" );
      out.close();  // close stream to complete the page
   } //end doGet() method
} //end WelcomeServlet class