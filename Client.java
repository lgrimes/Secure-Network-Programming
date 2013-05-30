import java.net.*;
import java.io.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Client  extends CryptSys{
	
	public static void main(String[] args) {
		
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		String username = args[2];
		String line;
		boolean accepted = false;
		boolean authenticated = false;
		boolean handshaking = true;
		boolean exit = false;
		
		try {
			Socket socket = new Socket(host, port);
			
			// get streams
			BufferedReader sBR = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			BufferedReader cBR = new BufferedReader(
					new InputStreamReader(System.in));
			PrintStream ps = new PrintStream(socket.getOutputStream());
			
			System.out.println("Waiting for connection to " + host +
					" on port " + port);
			while (!accepted) {
				if (sBR.readLine().compareToIgnoreCase("accept") == 0) {
					System.out.println("Connected to " + host + " on port " + port);
					accepted = true;
				}
			}
			
			// Send username
			System.out.println("Authenticating user: " + username);
			ps.println(username);
			while (!authenticated) {
				if ((line = sBR.readLine()).compareToIgnoreCase("auth") == 0) {
					System.out.println("User Authenticated");
					authenticated = true;
				}
				else if (line.compareToIgnoreCase("nonauth") == 0) {
					System.out.println("User Not Authorised!");
					socket.close();
					return;
				}
			}
			
			// Handshake: request SSL socket
			ps.println("SSL");
			while (handshaking) {
				if ((line = sBR.readLine()).compareToIgnoreCase("accept") == 0) {
					handshaking = false;
					System.out.println("SSL Connection request accepted");
				}
				else if (line.compareToIgnoreCase("reject") == 0) {
					System.out.println("SSL Connection not supported, exiting");
					socket.close();
					return;
				}
			}
			
			// Convert socket to SSL socket
			SSLSocketFactory sslsf = (SSLSocketFactory)SSLSocketFactory.getDefault();
			SSLSocket sslSocket = (SSLSocket)sslsf.createSocket(socket, null, port,
					false);
			System.out.println("SSL Connection request established\n");
			
			// re-get streams
			sBR = new BufferedReader(
					new InputStreamReader(sslSocket.getInputStream()));
			ps = new PrintStream(sslSocket.getOutputStream());
			
			// accept welcome screen
			while ((line = sBR.readLine()).compareToIgnoreCase("endwelcome") != 0)
				System.out.println(line);
			
			// read and send messages
			while (!exit) {
				System.out.print("\nEnter message (\"Exit\" to quit): ");
				// check for exit input
				if ((line = cBR.readLine()).compareToIgnoreCase("exit") == 0) {
					ps.println(line);
					exit = true;
				}
				else {
					ps.println(line);
					System.out.println("Sent to server: " + line);
				}
			}
			
			// close connections
			System.out.println("Client closing.");
			sslSocket.close();
			socket.close();
			
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
