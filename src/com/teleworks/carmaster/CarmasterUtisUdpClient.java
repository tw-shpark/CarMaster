package com.teleworks.carmaster;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.util.Log;

public class CarmasterUtisUdpClient implements Runnable {

	String serverIp = "192.168.1.11";
	int serverPort = 8888;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			// Retrieve the ServerName
			InetAddress serverAddr = InetAddress.getByName(serverIp);

			Log.d("UDP", "C: Connecting...");
			/* Create new UDP-Socket */
			DatagramSocket socket = new DatagramSocket();

			/* Prepare some data to be sent. */
			byte[] buf = ("Hello from Client").getBytes();

			/*
			 * Create UDP-packet with data & destination(url+port)
			 */
			DatagramPacket packet = new DatagramPacket(buf, buf.length,
					serverAddr, serverPort);
			Log.d("UDP", "C: Sending: '" + new String(buf) + "'");

			/* Send out the packet */
			socket.send(packet);
			Log.d("UDP", "C: Sent.");
			Log.d("UDP", "C: Done.");

			socket.receive(packet);
			Log.d("UDP", "C: Received: '" + new String(packet.getData()) + "'");

		} catch (Exception e) {
			Log.e("UDP", "C: Error", e);
		}
	}
}