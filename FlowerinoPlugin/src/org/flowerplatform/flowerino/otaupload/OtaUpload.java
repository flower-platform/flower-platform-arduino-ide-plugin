package org.flowerplatform.flowerino.otaupload;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Base64;

public class OtaUpload {
	
	String ip, file, serverSignature;
	
	int port;
	
	public OtaUpload(String ip, int port, String file, String serverSignature) {
		this.ip = ip;
		this.port = port;
		this.file = file;
		this.serverSignature = serverSignature;
	}
	
	public void start() throws Exception {
        byte[] binData = Files.readAllBytes(new File(file).toPath());
		ServerSocket serverSocket = new ServerSocket(80);
		String downloadKey = new String(Base64.getEncoder().encode(SecureRandom.getInstanceStrong().generateSeed(32)));
		new UploadThread(serverSocket, binData, downloadKey, serverSignature).start();
        System.out.println("local port: " + serverSocket.getLocalPort());

        // send upload command
		DatagramSocket sock = new DatagramSocket();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write("OTAUPLOAD".getBytes());
        String url = "http://" + InetAddress.getLocalHost().getHostAddress() + "/?downloadKey=" + downloadKey;
        baos.write(url.getBytes());
        
//        int localPort = serverSocket.getLocalPort();
//        baos.write((localPort & 0xFF00) >> 8);
//        baos.write(localPort & 0xFF);
        
//        baos.write((binData.length & 0xFF000000) >> 24);
//        baos.write((binData.length & 0xFF0000) >> 16);
//        baos.write((binData.length & 0xFF00) >> 8);
//        baos.write(binData.length & 0xFF);
        
        // compute check sequence
//        for (int i = 0; i < binData.length; i++) {
//        	
//        }
        
        byte[] packetData = baos.toByteArray();
        System.out.println("Sending update command");
        sock.send(new DatagramPacket(packetData, packetData.length, new InetSocketAddress(ip, port)));
        try { Thread.sleep(100); } catch (Exception e) { e.printStackTrace(); }
//        sock.send(new DatagramPacket(packetData, packetData.length, new InetSocketAddress(ip, port)));
//        try { Thread.sleep(100); } catch (Exception e) { e.printStackTrace(); }
//        sock.send(new DatagramPacket(packetData, packetData.length, new InetSocketAddress(ip, port)));
        sock.close();
	}
	
	public static void main(String[] args) throws Exception {
//		if (args.length != 3) {
//			System.out.println("Usage:\n\n    otapuload <ip> <port> <file>\n");
//		}
//		new OtaUpload(args[0], Integer.parseInt(args[1]), args[2]).start();
		new OtaUpload("192.168.100.251", 65500, "f:/track.txt", "YKLdXKfs6VW9BiHewutvhiUCvB2gnf0KUOpry7qJM4g=").start();
	}
	
}

class UploadThread extends Thread {
	
	private ServerSocket serverSocket;
	
	private byte[] binData;
	
	private String downloadKey;
	
	private String serverSignature;
	
	public UploadThread(ServerSocket serverSocket, byte[] binData, String downloadKey, String serverSignature) {
		this.serverSocket = serverSocket;
		this.binData = binData;
		this.downloadKey = downloadKey;
		this.serverSignature = serverSignature;
	}
	
	public void run() {
		Socket soc = null;
		try {
			serverSocket.setSoTimeout(10000);
			soc = serverSocket.accept();
			System.out.println("Board connected.");
			soc.setSoTimeout(5000);
			BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			String s;
			String clientDownloadKey = null;
			while ((s = in.readLine()).length() > 0) {
				if (s.startsWith("GET")) {
					clientDownloadKey = s.substring(s.indexOf("downloadKey=") + 12, s.lastIndexOf(' '));
				}
			}
			PrintStream out = new PrintStream(soc.getOutputStream());
			if (downloadKey.equals(clientDownloadKey)) {
				System.out.println("Sending update...");
				out.println("HTTP/1.1 200 OK");
				out.println("Content-type: application/binary");
				out.println("Connection: close");
				out.println("Content-length: " + binData.length);
				out.println("FP-serverSignature: " + serverSignature);
				out.println();
				out.write(binData);
				System.out.println("Update sent.");
			} else {
				System.out.println("Client sent incorrect download key. Rejecting...");
				out.println("HTTP/1.1 403 Forbidden");
				out.println("Content-type: text/plain");
				out.println("Connection: close");
				out.println();
				out.println("Access denied");
			}
			soc.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Update failed.");
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}