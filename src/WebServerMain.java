import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The entry class is used to parse arguments from cli and start a web server.
 * Which each connection will be handle by a new thread
 * @author 180008901
 *
 */
public class WebServerMain {

	/**
	 * The main function.
	 * @param args arguments from command line interface
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.out.println("Usage: java WebServerMain <document_root> <port>\n");
			return;
		}
		String documentRoot = args[0];
		int port = Integer.parseInt(args[1]);
		FileOutputStream ferror = null, faccess = null;
		if (args.length >= 3 && args[2].equals("-v")) {
			ferror = new FileOutputStream("error_log.txt");
			faccess = new FileOutputStream("access_log.txt");
			System.setOut(new PrintStream(faccess));
			System.setErr(new PrintStream(ferror));
		}
		System.out.println("Web server runs on localhost: " + port);
		System.out.println("Document root path: " + documentRoot);
		ServerSocket serverConnect = null;
		try {
			serverConnect = new ServerSocket(port);
			System.out.println("New connection start .... ");
			while (true) {
				Socket conn = serverConnect.accept();
				WebServer server = new WebServer(documentRoot, conn);
				Thread thread = new Thread(server);
				thread.start();
			}
		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		} finally {
			if (serverConnect != null) {
				serverConnect.close();
			}
		}
		if (ferror != null) {
			ferror.close();
		}
		if (faccess != null) {
			faccess.close();
		}
	}

}
