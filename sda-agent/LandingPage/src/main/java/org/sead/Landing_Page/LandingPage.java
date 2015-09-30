
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Servlet implementation class LandingPage
 */
public class LandingPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String title;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LandingPage() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String propertiesPath = "/Users/yuluo/Documents/workspace/Landing_Page/src/org/sead/config.properties";
		if (request.getParameter("tag") != null){
			String tag = request.getParameter("tag");
			Shimcalls shim = new Shimcalls(propertiesPath);
			
			JSONObject cp = shim.getResearchObject_cp(tag);
			shim.getObjectID(cp, "@id");
			String ore = shim.getID();
			JSONObject ore_file = shim.getResearchObjectORE(ore);
			
			
			JSONObject describe = new JSONObject();
			String pubdate = null;
			try{
				describe = (JSONObject) ore_file.get("describes");
				title = describe.get("Title").toString();
			}catch(Exception e){
				e.printStackTrace();
				System.err.println("No Title");
			}

			
			
			String who = null;
			
			try{
				JSONArray creator = (JSONArray) describe.get("Creator");
				who = creator.get(0).toString().split(":")[0];
			}catch(Exception e){
				who = describe.get("Creator").toString().split(":")[0];
			}
			
			try{
				pubdate = describe.get("Publication Date").toString();
			}catch(Exception e){
				e.printStackTrace();
				System.err.println("No Publish Date");
			}

	        PrintWriter out = response.getWriter();
	        
	        out.println (
	                  "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" +" +
	                      "http://www.w3.org/TR/html4/loose.dtd\">\n" +
	                  "<html> \n" +
	                    "<head> \n" +
	                      "<meta http-equiv=\"Content-Type\" content=\"text/html; " +
	                        "charset=ISO-8859-1\"> \n" +
	                      "<title> SDA Agent Landing Page: Search Result  </title> \n" +
	                    "</head> \n" +
	                    "<body> <div align='center'> \n" +
	                      "<style= \"font-size=\"12px\" color='black'\"" + "\">" +
	                        "RO information:  <br> "+
	                        "Title : " + title + " <br> " +
	                        "Author: " + who + " <br> " +
	                        "Publised Date: " + pubdate + " <br> " +
	                       
						"<form action='LandingPage'>"+
	                        "<input type='submit' name = 'Downlaod' value='Download'>"+
	                    "</form>"+
	                    "</font></body> \n" +
	                  "</html>" 
	                );      
		}else{
			
			SFTP sftp = new SFTP(propertiesPath);
			String target = "/cos1/hpss/s/e/seadva/"+title+File.separator+title+".tar";
			System.out.println(target);
	        InputStream inStream = sftp.downloadFile(target);
    
	         

	        String mimeType = "application/octet-stream";
	 
	        response.setContentType(mimeType);
	        

	        String headerKey = "Content-Disposition";
	        String headerValue = String.format("attachment; filename=\"%s\"", target.substring(target.lastIndexOf("/")+1));
	        response.setHeader(headerKey, headerValue);

	        OutputStream outStream = response.getOutputStream();
	         
	        byte[] buffer = new byte[4096];
	        int bytesRead = -1;
	         
	        while ((bytesRead = inStream.read(buffer)) != -1) {
	            outStream.write(buffer, 0, bytesRead);
	        }
	         
	        inStream.close();
	        outStream.close(); 
	        sftp.disConnectSessionAndChannel();
		}
        
    } 
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
