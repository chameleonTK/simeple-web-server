import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A connection handler.
 * @author 180008901
 *
 */
public class WebServer implements Runnable {
	private String root;
	private Socket conn;
	private InputStream is;	
	private OutputStream os;
	private BufferedReader br;
	private PrintWriter pw;
	private BufferedOutputStream bw;
	private final String DEFAULT_FILE = "index.html";
	private final String NAME = "CS5001-p3-networking";
	/**
	 * Constructor.
	 * @param root root directory of the web site
	 * @param conn connection from client
	 */
	public WebServer(String root, Socket conn) {
		this.root = root;
		this.conn = conn;
		try {
			is = conn.getInputStream();
			os = conn.getOutputStream();
			br = new BufferedReader(new InputStreamReader(is));
			pw = new PrintWriter(os); // send binary output stream to client (for requested data)
			bw = new BufferedOutputStream(os); // send character output stream to client (for headers
			System.out.println("Initialized Thread");
		} catch (IOException ioe) {
			System.err.println("ConnectionHandler: " + ioe.getMessage());
		}
	}
	
	/**
	 * Read content from input stream line by line
	 * @return tokens from input stream
	 * @throws IOException
	 */
	public List<String> readHTTPRequest() throws IOException {
		List<String> tokens = new ArrayList<String>();	
		String line = this.br.readLine();
		if (line == null || line.equals("")) {
			return tokens;
		}
		StringTokenizer parse = new StringTokenizer(line);
        while(parse.hasMoreTokens()) {
        	tokens.add(parse.nextToken());
        }
		return tokens;
	}
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			System.out.println("Run");
			System.out.println("Request content:");
			List<String> tokens = this.readHTTPRequest();
			if (tokens.size() < 2) {
				return;
			}
			String method = tokens.get(0).toUpperCase();
			String url = tokens.get(1).toLowerCase();
			String file = this.parseURL(url);
			String queryParams = this.parseQueryParams(url);
			System.out.println("DateTime: " + new Date());
			System.out.println("Request: " + method + " " + file);
			if (queryParams!= null) {
				System.out.println("Query Params: " + queryParams);
			}
			switch (method) {
				case "POST":
					int contentLength = 0;
					String body = "";
					//Get Content-Length
					while (tokens.size() > 0) {
						tokens = this.readHTTPRequest();
						if (tokens.size() > 1) {
							String key = tokens.get(0);
							if (key.toLowerCase().equals("content-length:")) {
								contentLength = Integer.parseInt(tokens.get(1));
							}
						}
					}
					//Get Request body
					for (int i = 0; i < contentLength; i++) {
						int c = this.br.read();
						body += (char)c;
			        }
					if (!body.equals("")) {
						System.out.println("Body: " + body);
					}
				case "GET":
				case "HEAD":
					if (file.endsWith("/")) {
						file += DEFAULT_FILE;
					}
					File fileData = new File(this.root, file);
					InputStream content = new FileInputStream(fileData);
					int fileLength = (int)fileData.length();
					String contentType = this.getContentType(file);
					//HTTP Headers
					System.out.println("Response: 200 OK");
					this.pw.println("HTTP/1.1 200 OK");
					this.pw.println("Server: " + NAME);
					this.pw.println("Date: " + new Date());
					this.pw.println("Content-Type: " + contentType);
					this.pw.println("Content-Length: " + fileLength);
					System.out.println("length " + fileLength);
					this.pw.println();
					this.pw.flush();
					// Return requested content
					if (method.equals("GET") || method.equals("POST")) {
						byte[] byteData = this.readFileData(content, fileLength);
				        this.bw.write(byteData, 0, byteData.length);
						this.bw.flush();
					}
					break;
				default:
					System.out.println("Response: 501 Not Implemented");
					System.err.println("Error 501");
					this.sendError(501, "Not Implemented", "<h1>501 Not implemented</h1>");
			}
		} catch (FileNotFoundException fnfe) {
			try {
				System.out.println("Response: 404 Not Found");
				System.err.println("Error 404");
				this.sendError(404, "Not Found", "<h1>404 Not found</h1>");
			} catch (IOException e) {
				System.err.println("Error with file not found exception ");
				e.printStackTrace();
			}
		} catch (IOException ioe) {
			System.err.println("Server error : " + ioe);
			try {
				this.sendError(500, "Internal Server Error", "<h1>500 Internal Server Error</h1>");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			try {
				System.out.println("Close connection ...");
				this.cleanup();
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			}
		}
	}
	
	/**
	 * Get only path from uri by split query string.
	 * @param url 
	 * @return path without query string
	 */
	private String parseURL(String url) {
		return url.split("\\?")[0];
	}
	
	/**
	 * Get query string.
	 * @param url 
	 * @return query string
	 */
	private String parseQueryParams(String url) {
		String[] s = url.split("\\?");
		if (s.length >= 2) {
			return s[1];
		}
		return null;
	}

	/**
	 * Clean up streams after close the connection.
	 * @throws IOException
	 */
	public void cleanup() throws IOException {
		if (this.br != null) {
			this.br.close();
		}
		if (this.bw != null) {
		    this.bw.close();
		}
		if (this.pw != null) {
		    this.pw.close();
		}
		if (this.is != null) {
		    this.is.close();
		}
		if (this.os != null) {
		    this.os.close();
		}
		if (this.conn != null) {
		    this.conn.close();
		}
	}
	/**
	 * HTTP error handler.
	 * @param httpStatus HTTP status
	 * @param error Short Description of HTTP error
	 * @param message Detail Description of HTTP error
	 * @throws IOException
	 */
	private void sendError(int httpStatus, String error, String message) throws IOException {
		byte[] data = message.getBytes();
		this.pw.println(String.format("HTTP/1.1 %d %s", httpStatus, error));
		this.pw.println("Server: " + NAME);
		this.pw.println("Date: " + new Date());
		this.pw.println("Content-type: text/html");
		this.pw.println("Content-length: " + data.length);
		this.pw.println();
		this.pw.flush();
		// file
		this.bw.write(data, 0, data.length);
		this.bw.flush();
	}
	/**
	 * Get byte data from a input stream and convert into byte array.
	 * @param content target stream
	 * @param fileLength buffer length
	 * @return byte array that contains data from input stream
	 * @throws IOException
	 */
	private byte[] readFileData(InputStream content, int fileLength) throws IOException {
		byte[] getBytes = new byte[fileLength];
		content.read(getBytes);
		content.close();
		return getBytes;
	}
	/**
	 * Determine Content-type of file.
	 * @param file filename
	 * @return correct content-type
	 */
	private String getContentType(String file) {
		if (file.endsWith(".htm")  ||  file.endsWith(".html")) {
			return "text/html";
		} else if (file.endsWith(".jpg")  ||  file.endsWith(".jpeg")) {
			return "image/jpeg";
		} else {
			return "text/plain";
		}
	}
}
