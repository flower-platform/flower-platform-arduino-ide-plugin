package org.flowerplatform.flowerino_plugin.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.flowerplatform.flowerino_plugin.server.command.UpdateSourceFilesCommand;

/**
 * 
 * @author Claudiu Matei
 *
 */
public class HttpServer implements Runnable {

	Map<String, Class<? extends IHttpCommand>> commands;
	
	private ExecutorService threadPool;
	
	private ServerSocket serverSocket;

	boolean stopped = false;
	
	public HttpServer(int port) throws IOException {
		threadPool = Executors.newFixedThreadPool(2);
		serverSocket = new ServerSocket(port);
		
		commands = new HashMap<>();
		commands.put("updateSourceFiles", UpdateSourceFilesCommand.class);
		
		threadPool.submit(this);
	}
	
	public void run() {
		try {
			while (!stopped) {
				Socket clientSocket = serverSocket.accept();
				threadPool.submit(new ClientHandler(this, clientSocket));
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			try { 
				serverSocket.close(); 
			} catch (Throwable t) { 
				t.printStackTrace(); 
			} 
		}
	}
	
	public void stop() {
		stopped = true;
		try { 
			serverSocket.close(); 
		} catch (Throwable t) {
			t.printStackTrace();
		}
		threadPool.shutdownNow();
	}

	public static void main(String[] args) throws Exception {
		new HttpServer(9000);
	}

}

