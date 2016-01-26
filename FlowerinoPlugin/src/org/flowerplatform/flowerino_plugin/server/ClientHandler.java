package org.flowerplatform.flowerino_plugin.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author Claudiu Matei
 *
 */
public class ClientHandler implements Runnable {

	private Socket socket;

	private HttpServer server;
	
	public ClientHandler(HttpServer server, Socket socket) {
		this.socket = socket;
		this.server = server;
	}
	
	@Override
	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			OutputStream out = socket.getOutputStream();
			String request = in.readLine();
//			System.out.println(request);
			String method = request.substring(0, request.indexOf(' '));
			if (!method.equals("POST")) {
				throw new RuntimeException(String.format("Unsupported method: %s", method));
			}
			int contentLength = 0;
			String s;
			while ((s = in.readLine()).length() > 0) {
//				System.out.println(s);
				if (s.toLowerCase().startsWith("content-length")) {
					contentLength = Integer.parseInt(s.substring(s.indexOf(' ') + 1));
				}
			}
			String command = request.substring(request.indexOf('/') + 1,  request.lastIndexOf(' '));
			StringBuilder sb = new StringBuilder();
			char[] buf = new char[256];
			while (contentLength > 0) {
				int k = in.read(buf);
				sb.append(buf, 0, k);
				contentLength -= k;
			}

			ObjectMapper mapper = new ObjectMapper();
			Class<? extends IHttpCommand> commandClass = server.commands.get(command);
			if (commandClass == null) {
				throw new RuntimeException(String.format("Invalid command: %s", command));
			}
			IHttpCommand commandInstance = mapper.readValue(sb.toString(), server.commands.get(command));
			Object result = commandInstance.run();
			
			out.write("HTTP/1.1 200 OK\r\n".getBytes());
			out.write("Content-type: text/plain\r\n".getBytes());
			out.write("Connection: close\r\n".getBytes());
			out.write("\r\n".getBytes());
			
			if (result != null) {
				out.write(mapper.writeValueAsString(result).getBytes());
			}
			
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			try { 
				socket.close(); 
			} 
			catch (Throwable t) { 
				t.printStackTrace(); 
			}
		}
	}
	
}
