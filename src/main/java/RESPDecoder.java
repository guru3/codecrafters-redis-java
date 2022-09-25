import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;


class ConnectionBuffer{

	private final BufferedReader clientRequestReader;
	private final StringBuilder buffer;

	ConnectionBuffer(Socket clientSocket) throws IOException {
		// Initialize client request reader
		this.clientRequestReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
		this.buffer = new StringBuilder();
	}

	private void readClientRequestData() throws IOException {
		String nextLine = this.clientRequestReader.readLine();
		if( nextLine != null && nextLine.length() > 0) {
			this.buffer.append(nextLine + "\r\n");
		}
	}

	String readUntilDelimiter(String delimiter) throws IOException {
		while( this.buffer.indexOf(delimiter) == -1 ) {
			this.readClientRequestData();
		}

		int delimiterIndex = this.buffer.indexOf(delimiter);
		String dataFromDelimiter = this.buffer.substring(0, delimiterIndex);
		this.buffer.delete(0, delimiterIndex + delimiter.length());
		return dataFromDelimiter;
	}

	String read(int bufferSize) throws IOException {
		while( this.buffer.length() < bufferSize ) {
			this.readClientRequestData();
		}

		String data = this.buffer.substring(0, bufferSize);
		this.buffer.delete(0, bufferSize);
		return data;
	}
}


class RESPDecoder{

	private final ConnectionBuffer connectionBuffer;

	public RESPDecoder(Socket clientSocket) throws IOException {
		this.connectionBuffer = new ConnectionBuffer(clientSocket);
	}

	public List<Object> processClientRequest() throws Exception {
		String data_type_byte = this.connectionBuffer.read(1);

		if (data_type_byte.equals("+")) {
			return this.decodeSimpleString();
		} else if (data_type_byte.equals("$")) {
			return this.decodeBulkString();
		} else if (data_type_byte.equals("*")) {
			return this.decodeArray();
		} else {
			throw new Exception("Unknown data type byte : " + String.valueOf(data_type_byte));
		}
	}

	private List<Object> decodeSimpleString() throws Exception {
		String argument = this.connectionBuffer.readUntilDelimiter("\r\n");
		return Arrays.asList(argument);
	}

	private List<Object> decodeBulkString() throws Exception {
		int bulkStringLength = Integer.parseInt(this.connectionBuffer.readUntilDelimiter("\r\n"));
		String data = this.connectionBuffer.read(bulkStringLength);
		this.connectionBuffer.readUntilDelimiter("\r\n");
		return Arrays.asList(data);
	}

	private List<Object> decodeArray() throws Exception {
		List<Object> arguments = new ArrayList<>();
		int arrayLength = Integer.parseInt(this.connectionBuffer.readUntilDelimiter("\r\n"));

		for( int index = 0; index < arrayLength; index++ ) {
			arguments.addAll(this.processClientRequest());
		}
		return arguments;
	}
}
