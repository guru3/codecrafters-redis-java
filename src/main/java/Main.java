import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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

			// Wait for connection from client.
			clientSocket = serverSocket.accept();

			// Prepare to read data sent by client using ObjectInputStream object
            ObjectInputStream clientRequestStream = new ObjectInputStream(clientSocket.getInputStream());

			// Prepare to send data back to client using ObjectOutputStream
			ObjectOutputStream clientResponseStream = new ObjectOutputStream(clientSocket.getOutputStream());

			// Read the data sent by client
			String clientMessage = (String) clientRequestStream.readObject();

            // Send response back to client
            clientResponseStream.writeObject(b"+PONG\r\n")

		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		} finally {
			try {
				// Gracefully close client socket
				if (clientSocket != null) {
					clientSocket.close();
			   	}
				if (clientRequestStream != null) {
					clientRequestStream.close()
				}
				if (clientResponseStream != null) {
					clientResponseStream.close()
				}
			} catch (IOException e) {
				System.out.println("IOException: " + e.getMessage());
			}
		}
	}
}
