package webServer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketImpl;
import java.util.StringTokenizer;

/**
 * This is a small simulation of a webserver. It works on the port 50505.
 * Some of the code pieces are copied from the UOPeople assignment.
 * 
 * **********************************************************************
 *    Title: Lab 11: A Web Server
 *    Author: University of The People
 *    Date: 13 May 2020
 *    Availability: https://my.uopeople.edu/mod/page/view.php?id=268287&inpopup=1
 * **********************************************************************
 *
 */
public class WebServer {

	private static final int LISTENING_PORT = 50505; // Port number
	private static Socket client;
	private static DataInputStream in;
	private static PrintStream out;
	private static String requestedFile;

	public static void main(String[] args) { // main() begin

		ServerSocket serverSocket;

		try {
			serverSocket = new ServerSocket(LISTENING_PORT);
		}
		catch (Exception e) {
			System.out.println("Failed to create listening socket.");
			return;
		}

		System.out.println("Listening on port " + LISTENING_PORT);

		try {
			while (true) {
				Socket connection = serverSocket.accept();
				System.out.println("\nConnection from "
						+ connection.getRemoteSocketAddress());
				ConnectionThread thread = new ConnectionThread(connection);
				thread.start();
			}
		}
		catch (Exception e) {
			System.out.println("Server socket shut down unexpectedly!");
			System.out.println("Error: " + e);
			System.out.println("Exiting.");
		}

		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	} // main() end


	/**
	 * The method handles the already connected socket.
	 * 
	 * @param connection already connected socket
	 */
	@SuppressWarnings("deprecation")
	private static void handleConnection(Socket connection) { // handleConnection() begin

		String rootDirectory = "/F:/rootDirectory/";
		client = connection; // the connection to handle

		try {
			in = new DataInputStream(client.getInputStream());
			out = new PrintStream(client.getOutputStream());

			String ln = null;
			String request = null;

			request= in.readLine();
			ln = request;
			
			StringTokenizer str = new StringTokenizer(request);

			if(!str.nextToken().equals("GET")) { // unknown command
				errMsg(501);
				return;
			}

			while(ln.length() > 0) {
				ln = in.readLine();
			}
			
			requestedFile = str.nextToken();
			File file = new File(rootDirectory + requestedFile);

			if(!file.canRead()) { //unknown type
				errMsg(404);
				return;
			}

			sendResHeader(getMimeType(requestedFile), (int) file.length());
			sendFile(file, client.getOutputStream());
		}
		catch(Exception e) {
			System.out.println("Error while communicating with client: " + e);
		}
		finally {
			try {
				connection.close(); // closing the connection
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	} //handleConnection() end


	/**
	 * This method takes a file and a socket to send the file through it.
	 * 
	 * @param f file to be sent
	 * @param soc the socket for sending
	 * @throws IOException
	 */
	private static void sendFile(File f, OutputStream soc) throws IOException { // sendFile() begin

		InputStream in = new BufferedInputStream(new FileInputStream(f));
		OutputStream out = new BufferedOutputStream(soc);

		while(true) {
			int byt = in.read(); //reading one byte

			if(byt < 0) {
				break;
			}
			out.write(byt); // sending one byte
		}

		in.close();  // closing
		out.flush(); // the
		out.close(); // connections

	} // sendFile() end


	/**
	 * this method sends a header to indicate a successful connection.
	 * 
	 * @param type the content type
	 * @param length the size of the content
	 */
	private static void sendResHeader(String type, int length) { // sendResHeader() begin
		out.println("HTTP/1.1 200 OK");
		out.println("Connection: close " + "\r\n");
		out.println("Content-type: " + type);
		out.println("Content-Length: " + length);        
	} // sendResHeader() end

	/**
	 * This method returns a string representation of the MIMEtype of the file.
	 * 
	 * @param fileName the name of the file to be sent
	 * @return the MIME type of the file
	 */
	private static String getMimeType(String fileName) { // getMimeType() begin

		int pos = fileName.lastIndexOf('.');

		if (pos < 0)  // no file extension in name
			return "x-application/x-unknown";

		String ext = fileName.substring(pos+1).toLowerCase();

		if (ext.equals("txt")) return "text/plain";
		else if (ext.equals("html")) return "text/html";
		else if (ext.equals("htm")) return "text/html";
		else if (ext.equals("css")) return "text/css";
		else if (ext.equals("js")) return "text/javascript";
		else if (ext.equals("java")) return "text/x-java";
		else if (ext.equals("jpeg")) return "image/jpeg";
		else if (ext.equals("jpg")) return "image/jpeg";
		else if (ext.equals("png")) return "image/png";
		else if (ext.equals("gif")) return "image/gif";
		else if (ext.equals("ico")) return "image/x-icon";
		else if (ext.equals("class")) return "application/java-vm";
		else if (ext.equals("jar")) return "application/java-archive";
		else if (ext.equals("zip")) return "application/zip";
		else if (ext.equals("xml")) return "application/xml";
		else if (ext.equals("xhtml")) return"application/xhtml+xml";
		else return "x-application/x-unknown";

		// Note:  x-application/x-unknown  is something made up;
		// it will probably make the browser offer to save the file.
	} // getMimeType() end


	/**
	 * This method takes an error code to print out the proper error message.
	 * 
	 * @param errCode the code of the error
	 */
	private static void errMsg(int errCode) { //errMsg() begin 

		switch(errCode) {

		case 404 -> {
			out.print("HTTP/1.1 404 Not Found"); 
			out.println("Connection: close " );       
			out.println("Content-type: text/plain" +"\r\n");    
			out.println("<html><head><title>Error</title></head><body> <h2>Error: 404 Not Found</h2> <p>The resource that you requested does not exist on this server.</p> </body></html>");
			break;
		}

		case 400 -> {
			out.print("HTTP/1.1 400 Bad Request"); 
			out.println("Connection: close " );       
			out.println("Content-type: text/plain" +"\r\n");    
			out.println("<html><head><title>Error</title></head><body> <h2>Error: 400 Bad Request</h2> <p>The connection was interrupted due to bad request.</p> </body></html>");
			break;

		}

		case 501 ->{
			out.print("HTTP/1.1 501 Not Implemented"); 
			out.println("Connection: close " );       
			out.println("Content-type: text/plain" +"\r\n");    
			out.println("<html><head><title>Error</title></head><body> <h2>Error: 501 Not Implemented</h2> <p>The method isn't implemented.</p> </body></html>");
			break;
		}
		
		default -> {
			out.print("HTTP/1.1 500 Internal Server Error"); 
			out.println("Connection: close " );       
			out.println("Content-type: text/plain" +"\r\n");    
			out.println("<html><head><title>Error</title></head><body> <h2>Error: 500 Internal Server Error</h2> <p>The connection was interrupted due to unexpected error.</p> </body></html>");
			break;
		}
		
		}

	} // errMsg() end


	/**
	 * This method provides a thread to handle separate sockets simultaneously.
	 * 
	 *
	 */
	private static class ConnectionThread extends Thread {

		Socket connection;

		ConnectionThread(Socket connection) {
			this.connection = connection;
		}

		public void run() {
			handleConnection(connection);
		}
	}

}
