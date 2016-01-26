package org.flowerplatform.flowerino.otaupload;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class OtaUpload {
	
	String ip, file;
	
	int port;
	
	public OtaUpload(String ip, int port, String file) {
		this.ip = ip;
		this.port = port;
		this.file = file;
	}
	
	public void start() throws Exception {
        byte[] binData = Files.readAllBytes(new File(file).toPath());
		ServerSocket serverSocket = new ServerSocket(0);
        new UploadThread(serverSocket, binData).start();
        System.out.println("local port: " + serverSocket.getLocalPort());

        // send upload command
		DatagramSocket sock = new DatagramSocket();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write("OTAUPLOAD".getBytes());
        baos.write(("" + serverSocket.getLocalPort()).getBytes());
        byte[] packetData = baos.toByteArray();
        sock.send(new DatagramPacket(packetData, packetData.length, new InetSocketAddress(ip, port)));
        sock.send(new DatagramPacket(packetData, packetData.length, new InetSocketAddress(ip, port)));
        try { Thread.sleep(100); } catch (Exception e) { e.printStackTrace(); }
        sock.send(new DatagramPacket(packetData, packetData.length, new InetSocketAddress(ip, port)));
        sock.send(new DatagramPacket(packetData, packetData.length, new InetSocketAddress(ip, port)));
        sock.close();
        
	}
	
	public static void main(String[] args) throws Exception{
//		if (args.length != 3) {
//			System.out.println("Usage:\n\n    otapuload <ip> <port> <file>\n");
//		}
		new OtaUpload(args[0], Integer.parseInt(args[1]), args[2]).start();
//		new OtaUpload("192.168.100.123", 9999, "d:/eboot.elf").start();
	}
	
}

class UploadThread extends Thread {
	
	private ServerSocket serverSocket;
	
	private byte[] binData;
	
	public UploadThread(ServerSocket serverSocket, byte[] binData) {
		this.binData = binData;
		this.serverSocket = serverSocket;
	}
	
	public void run() {
		try {
			serverSocket.setSoTimeout(10000);
			Socket soc = serverSocket.accept();
			soc.getOutputStream().write(binData);
			soc.getOutputStream().close();
			soc.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
}