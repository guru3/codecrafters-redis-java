import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


class ClientConnectionHandler implements Runnable {

	private final Socket clientSocket;

	ClientConnectionHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {

		BufferedReader clientRequestReader = null;
		OutputStreamWriter clientResponseWriter = null;

		try {
			// Prepare to read data sent by client
			clientRequestReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream(), StandardCharsets.UTF_8));

			// Prepare to send data back to client
			clientResponseWriter = new OutputStreamWriter(this.clientSocket.getOutputStream(), StandardCharsets.UTF_8);

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
				// Gracefully close stream reader/writer
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


public class Main {

	private static int REDIS_PORT = 6379;

	public static void main(String[] args){
		// You can use print statements as follows for debugging, they'll be visible when running tests.
		System.out.println("Logs from your program will appear here!");

		ServerSocket serverSocket = null;
		Socket clientSocket = null;

		try {
			// Create a socket
			serverSocket = new ServerSocket(REDIS_PORT);
			serverSocket.setReuseAddress(true);

			while( true ) {
				// Wait for connection from a new client
				clientSocket = serverSocket.accept();

				// Use a new thread to handle connection with this client
				Thread newConnectionHandler = new Thread(new ClientConnectionHandler(clientSocket));
				newConnectionHandler.start();
			}
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		} finally {
			try {
				// Gracefully close client socket
				if (clientSocket != null) {
					clientSocket.close();
				}
			} catch (IOException e) {
				System.out.println("IOException: " + e.getMessage());
			}
		}
	}
}
