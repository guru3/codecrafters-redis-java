import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;


class ClientConnectionHandler implements Runnable {

	private final Socket clientSocket;

	ClientConnectionHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {

		OutputStreamWriter clientResponseWriter = null;
		RESPDecoder respDecoder = null;
		Storage storage = new Storage();

		try {
			// Prepare to send data back to client
			clientResponseWriter = new OutputStreamWriter(this.clientSocket.getOutputStream(), StandardCharsets.UTF_8);

			// Prepare to read data sent by client and decode the request
			respDecoder = new RESPDecoder(this.clientSocket);

			while( true ) {

				try {
					// Process request and generate response
					List<Object> decodedRequest = respDecoder.processClientRequest();
					if( decodedRequest.size() == 0 ) {
						continue;
					}

					// Send response back to client
					String command = String.valueOf(decodedRequest.get(0));

					if (command.equals("ping")) {
						clientResponseWriter.write("+PONG\r\n");
					} else if (command.equals("echo")) {
						String response = String.valueOf(decodedRequest.get(1));
						clientResponseWriter.write(String.format("$%d\r\n%s\r\n", response.length(), response));
					} else if (command.equals("set")) {
						String firstArg = String.valueOf(decodedRequest.get(1));
						String secondArg = String.valueOf(decodedRequest.get(2));
						if( decodedRequest.size() > 3 ) {
							String thirdArg = String.valueOf(decodedRequest.get(3));

							if( thirdArg.equals("px") ) {
								String fourthArg = String.valueOf(decodedRequest.get(4));
								long expiryInMilliseconds = 0;
								try {
									expiryInMilliseconds = Long.parseLong(fourthArg);
								} catch (Exception e) {
									clientResponseWriter.write(String.format("-ERR PX value (%s) is not an integer\r\n", fourthArg));
								}

								storage.setWithExpiry(firstArg, secondArg, Duration.ofMillis(expiryInMilliseconds));
							} else {
								clientResponseWriter.write(String.format("-ERR unknown option for set: %s\r\n", thirdArg));
							}

						} else {
							storage.set(firstArg, secondArg);
						}
						clientResponseWriter.write("+OK\r\n");

					} else if (command.equals("get")) {
						String key = String.valueOf(decodedRequest.get(1));
						Value value = storage.get(key);
						if( value.isValid ) {
							clientResponseWriter.write(String.format("+%s\r\n", value.value));
						} else {
							clientResponseWriter.write("$-1\r\n");
						}
					}else {
						clientResponseWriter.write("-ERR unknown command\r\n");
					}

					clientResponseWriter.flush();
				} catch (IOException e) {
					System.out.println("IOException: " + e.getMessage());
				} catch (Exception e) {
					System.out.println("Exception: " + e.getMessage());
				}
			}
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		} finally {
			try {
				// Gracefully close stream writer
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
