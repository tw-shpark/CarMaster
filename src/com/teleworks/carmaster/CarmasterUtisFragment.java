package com.teleworks.carmaster;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.apache.http.util.EncodingUtils;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
public class CarmasterUtisFragment extends Fragment {
	public static final String ARG_OBJECT = "object";
	private static final String LOG_TAG = "UtisFragment";
	private TextView mTextView;
	private String serverIp = "169.254.2.254";
	private int serverPort = 20000;
	private Thread thread;
	private static final int SET_TEXT_VIEW = 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView;
		rootView = inflater.inflate(R.layout.fragment_utis_test, container,
				false);

		mTextView = (TextView) rootView.findViewById(R.id.text_utis_console);
		mTextView.setText(String.format("UTIS OBE TEST"));

		thread = new Thread(UTIS_OBE_UDP_CLIENT);
		thread.start();
		thread = new Thread(UTIS_OBE_UDP_READ);
		thread.start();

		return rootView;
	}

	// main Handler
	private final Handler mMainHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SET_TEXT_VIEW:
				mTextView.setText((String) msg.obj);
				break;
			}
		}
	};

	Runnable UTIS_OBE_UDP_READ = new Runnable() {
		public void run() {
			runUdpServer();
			Log.d("UDP", "C: UTIS_OBE_UDP_READ End");
		}
	};

	Runnable UTIS_OBE_UDP_CLIENT = new Runnable() {
		public void run() {
			// TODO Auto-generated method stub
			try {
				// Retrieve the ServerName
				InetAddress serverAddr = InetAddress.getByName(serverIp);

				Log.d("UDP", "C: Connecting...");
				mMainHandler.obtainMessage(SET_TEXT_VIEW, -1, -1,
						"C: Connecting...").sendToTarget();
				/* Create new UDP-Socket */
				DatagramSocket socket = new DatagramSocket();

				/* Prepare some data to be sent. */
				// byte[] buf = ("Hello from Client").getBytes();
				byte[] buf = { 0x00, 0x00 };

				/*
				 * Create UDP-packet with data & destination(url+port)
				 */
				DatagramPacket packet = new DatagramPacket(buf, buf.length,
						serverAddr, serverPort);
				// Log.d("UDP", "C: Sending: '" + new String(buf) + "'");
				Log.d("UDP", "C: Sending: '" + byteToHexString(buf) + "'");
				/* Send out the packet */
				socket.send(packet);
				Log.d("UDP", "C: Sent.");
				Log.d("UDP", "C: Done.");

				socket.receive(packet);
				// Log.d("UDP", "C: Received: '" + new String(packet.getData())
				// + "'");
				mMainHandler.obtainMessage(SET_TEXT_VIEW, -1, -1,
						byteToHexString(packet.getData())).sendToTarget();
				Log.d("UDP", "C: End");

			} catch (Exception e) {
				Log.e("UDP", "C: Error", e);
				mMainHandler.obtainMessage(SET_TEXT_VIEW, -1, -1,
						"UTIS_OBE_UDP connect Error").sendToTarget();
			}
		}
	};

	private void runUdpServer() {
		// String lText;
		byte[] lMsg = new byte[256];
		DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
		DatagramSocket ds = null;
		int len;
		try {
			ds = new DatagramSocket(serverPort);
			// disable timeout for testing
			// ds.setSoTimeout(100000);
			while (true) {
				ds.receive(dp);
				len = dp.getLength();
				// lText = new String(lMsg, 0, dp.getLength());
				// Log.i("UDP packet received", lText);

				byte[] comm = new byte[len];
				System.arraycopy(lMsg, 0, comm, 0, len); // ´Ü¸»±â
				Log.i("UDP packet received", byteToHexString(comm));
				mMainHandler.obtainMessage(SET_TEXT_VIEW, -1, -1,
						byteToHexString(comm)).sendToTarget();
			}
			// textView.setText(lText);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ds != null) {
				ds.close();
			}
		}
	}

	public static String byteToString(byte[] bytebuf) {
		String result = EncodingUtils.getString(bytebuf, 0, bytebuf.length,
				"Latin-1");
		return result;
	}

	public static byte[] stringToByte(String $byteString) {
		byte[] byteArray = EncodingUtils.getBytes($byteString, "Latin-1");
		return byteArray;
	}

	public static String byteToHexString(byte[] bytebuf) {
		String result = "";
		for (int i = 0; i < bytebuf.length; i++) {
			result += String.format("%02X ", bytebuf[i]);
		}
		return result;
	}
}
