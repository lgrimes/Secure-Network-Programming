import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public class Server extends CryptSys implements Runnable {

	static final int maxClients = 100;
	static final String[] users = {"Ed", "Laura", "Arthur"};

	static SSLServerSocket sslss;
	static ExecutorService exec;
	static int numClients = 0;
	static int port;
	Socket socket;
	SSLSocket sslSocket;
	int clientNumber;
	
	// constructor
	Server(Socket s) {
		socket = s;
	}
	
	public static void main(String[] args) {

		port = Integer.parseInt(args[0]);
		Socket  s;
		boolean clientConnected = false;
		
		try {
			ServerSocket ss = new ServerSocket(port);
			System.out.println(ss.toString() + " is listening...");
			
			// create thread pool
			exec = Executors.newCachedThreadPool();
			
			// accept connections from clients
			while (!clientConnected || numClients > 0) {
				s = ss.accept();
				clientConnected = true;
				numClients++;
				System.out.println("\nClient connected, total clients: " +
						numClients);
				exec.execute(new Server(s));
			}
			while (!exec.isTerminated())
				System.out.println("waiting");
			sslss.close();
			ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return;
	}
	
	public void run() {
		try {
			boolean exit = false;
			String user, line;
			
			// get socket streams an console input
			PrintStream ps = new PrintStream(socket.getOutputStream());
			BufferedReader sBR = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			
			// acknowledge connection
			ps.println("accept");
			
			// authenticate user
			user = sBR.readLine();
			if (authenticateUser(user))
				ps.println("auth");
			else {
				ps.println("nonauth");
				socket.close();
				return;
			}
			
			// Handshake: accept connection type request
			if (sBR.readLine().compareToIgnoreCase("SSL") == 0) {
				// Convert socket to SSL socket
				SSLSocketFactory sslsf = (SSLSocketFactory)SSLSocketFactory.getDefault();
				sslSocket = (SSLSocket)sslsf.createSocket(socket, null, port,
						false);
				sslSocket.setUseClientMode(false);
				ps.println("accept");
			}
			else {
				ps.println("reject");
				System.out.println("Requested connection type unsupported," +
						"dropping connection.");
			}
			
			// re-get SSL socket streams an console input
			ps = new PrintStream(sslSocket.getOutputStream());
			sBR = new BufferedReader(
					new InputStreamReader(sslSocket.getInputStream()));
			
			// send welcome message
			ps.print("**************************************************\n");
			ps.print("**                    Welcome                   **\n");
			ps.print("**************************************************\n");
			ps.print("**  Welcome to the Secure Network Programming   **\n");
			ps.print("**   Semester 1, 2013 Assignment 2 submission   **\n");
			ps.print("**  for Edward Booth, Lauren Grimes and Arthur  **\n");
			ps.print("** Papadakis. This is a simple messaging system **\n");
			ps.print("**     so please enter your message(s) now      **\n");
			ps.print("**************************************************\n");
			ps.println("endwelcome");
			
			// receive messages and prompt for decoding
			while (!exit) {
				line = sBR.readLine();
				if (line.compareToIgnoreCase("exit") == 0) {
					exit = true;
				}
				else
					System.out.println("Recieved from client: " + line + "\n");
			}
			
			// close socket
			socket.close();
			sslSocket.close();
			numClients--;
			if (numClients > 0) {
				System.out.println("\nClient disconnected, total clients: " +
						numClients);
			}
			else {
				// if no other connected clients, close ServerSocket and exit
				System.out.println("\nLast client disconnected, exiting.");
				exec.shutdown();
				System.exit(0);
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Checks if a username is on record
	private boolean authenticateUser(String u) {
		for (int i = 0; i < users.length; i++) {
			if (users[i].compareTo(u) == 0)
				return true;
		}
		return false;
	}
}
