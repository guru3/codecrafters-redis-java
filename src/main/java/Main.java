import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


public class Main {

	private static int REDIS_PORT = 6379;

	public static void main(String[] args){
		// You can use print statements as follows for debugging, they'll be visible when running tests.
		System.out.println("Logs from your program will appear here!");

		ServerSocket serverSocket = null;
		Socket clientSocket = null;
		BufferedReader clientRequestReader = null;
		OutputStreamWriter clientResponseWriter = null;

		try {
			// Create a socket
			serverSocket = new ServerSocket(REDIS_PORT);
			serverSocket.setReuseAddress(true);

			// Wait for connection from client.
			clientSocket = serverSocket.accept();

			// Prepare to read data sent by client
			clientRequestReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));

			// Prepare to send data back to client
			clientResponseWriter = new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8);

			while( true ) {
				// Read the data sent by client
				String line = clientRequestReader.readLine();

				// Send response back to client
				clientResponseWriter.write("+PONG\r\n");
				clientResponseWriter.flush();
			}

		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		} finally {
			try {
				// Gracefully close client socket
				if (clientSocket != null) {
					clientSocket.close();
			   	}
				if (clientRequestReader != null) {
					clientRequestReader.close();
				}
				if (clientResponseWriter != null) {
					clientResponseWriter.close();
				}
			} catch (IOException e) {
				System.out.println("IOException: " + e.getMessage());
			}
		}
	}
}
