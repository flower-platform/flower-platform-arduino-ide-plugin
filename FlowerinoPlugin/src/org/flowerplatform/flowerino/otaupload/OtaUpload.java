package org.flowerplatform.flowerino.otaupload;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

import com.microsoft.azure.iot.service.sdk.Message;
import com.microsoft.azure.iot.service.sdk.ServiceClient;

public class OtaUpload {
	
	String file;
	
	public OtaUpload(String file) {
		this.file = file;
	}

	public void localUpload(String boardIp, String serverSignature) throws Exception {
        byte[] binData = Files.readAllBytes(new File(file).toPath());

		String downloadKey = new String(Base64.getEncoder().encode(SecureRandom.getInstanceStrong().generateSeed(32)));
		UploadThread uploadThread = new UploadThread(binData, downloadKey, serverSignature);

		// send upload command
        String downloadUrl = "http://" + InetAddress.getLocalHost().getHostAddress() + "/?downloadKey=" + downloadKey;
		sendUdpUploadCommand(boardIp, downloadUrl);

        while (uploadThread.started);
	}
	
	public void dispatcherUpload(String boardIp, String dispatcherUrl,String uploadKey, String boardName, String rAppGroup) throws Exception {
        byte[] binData = Files.readAllBytes(new File(file).toPath());

		// generate download key
        String downloadKey = new String(Base64.getEncoder().encode(SecureRandom.getInstanceStrong().generateSeed(32)));

		// upload file to dispatcher
        String uploadUrl = String.format("%s/upload.php?uploadKey=%s&board=%s&rAppGroup=%s&downloadKey=%s", dispatcherUrl, uploadKey, boardName, rAppGroup, downloadKey);
		URL obj = new URL(uploadUrl);
		HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-type", "application/binary");
		OutputStream out = conn.getOutputStream();
		out.write(binData);
		out.close();
		conn.getInputStream().close();
		
        String downloadUrl = String.format("%s/download.php?board=%s&rAppGroup=%s&downloadKey=%s", dispatcherUrl, boardName, rAppGroup, downloadKey);
		sendUdpUploadCommand(boardIp, downloadUrl);
	}

	private void sendUdpUploadCommand(String ip, String url) throws SocketException, IOException, UnknownHostException {
		DatagramSocket sock = new DatagramSocket();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write("OTAUPLOAD".getBytes());
        baos.write(url.getBytes());

        byte[] packetData = baos.toByteArray();
        System.out.println("Sending update command: " + new String(packetData));
        sock.send(new DatagramPacket(packetData, packetData.length, new InetSocketAddress(ip, 65500)));
        try { Thread.sleep(100); } catch (Exception e) { e.printStackTrace(); }
        sock.close();
	}
	
	public void azureUpload(String azureConnectionString, String deviceId, String dispatcherUrl, String uploadKey, String boardName, String rAppGroup) throws Exception {
        byte[] binData = Files.readAllBytes(new File(file).toPath());

		// generate download key
        String downloadKey = new String(Base64.getEncoder().encode(SecureRandom.getInstanceStrong().generateSeed(32)));

		// upload file to dispatcher
        String uploadUrl = String.format("%s/upload.php?uploadKey=%s&board=%s&rAppGroup=%s&downloadKey=%s", dispatcherUrl, uploadKey, boardName, rAppGroup, downloadKey);
		URL obj = new URL(uploadUrl);
		HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-type", "application/binary");
		OutputStream out = conn.getOutputStream();
		out.write(binData);
		out.close();
		conn.getInputStream().close();
		
        String downloadUrl = String.format("%s/download.php?board=%s&rAppGroup=%s&downloadKey=%s\0", dispatcherUrl, boardName, rAppGroup, downloadKey);
        sendAzureUploadCommand(azureConnectionString, deviceId, downloadUrl);
	}

	private void sendAzureUploadCommand(String azureConnectionString, String deviceId, String downloadUrl) throws Exception, IOException, UnsupportedEncodingException {
		ServiceClient client = ServiceClient.createFromConnectionString(azureConnectionString);
        client.open();
        Message message = new Message("OTAUPLOAD" + downloadUrl);
        message.setMessageId(java.util.UUID.randomUUID().toString());
        Date now = new Date();
        message.setExpiryTimeUtc(new Date(now.getTime() + 60 * 1000));
        message.setCorrelationId(java.util.UUID.randomUUID().toString());
        message.setUserId(java.util.UUID.randomUUID().toString());
        client.send(deviceId, message);
        client.close();
        System.out.println(downloadUrl);
	}
	
	public static void main(String[] args) throws Exception {
//		if (args.length != 3) {
//			System.out.println("Usage:\n\n    otapuload <ip> <port> <file>\n");
//		}
//		new OtaUpload(args[0], Integer.parseInt(args[1]), args[2]).start();
//		new OtaUpload("f:/track.txt").localUpload("192.168.100.251", "YKLdXKfs6VW9BiHewutvhiUCvB2gnf0KUOpry7qJM4g="); 
		new OtaUpload("f:/track.txt").dispatcherUpload("192.168.100.251", "http://testcr.azurewebsites.net", "upkey", "board1", "newg1"); 
//		new OtaUpload("f:/track.txt").sendAzureUploadCommand("HostName=arduino-mkr1000.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=atQwsAzJOGBo6A1tV2ZN55BizfzPVRv2ZIc1XmSN3Lo=",
//				"mkr1000", "http://asdlkajdlkasdj/");
		
	}
	
}

class UploadThread extends Thread {
	
	private ServerSocket serverSocket;
	
	private byte[] binData;
	
	private String downloadKey;
	
	private String serverSignature;
	
	boolean started;
	
	public UploadThread(byte[] binData, String downloadKey, String serverSignature) {
		this.binData = binData;
		this.downloadKey = downloadKey;
		this.serverSignature = serverSignature;
		this.start();
	}
	
	public void run() {
		started = true;
		Socket soc = null;
		try {
	        serverSocket = new ServerSocket(80);
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
			started = false;
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}