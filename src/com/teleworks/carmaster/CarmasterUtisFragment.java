package com.teleworks.carmaster;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.http.util.EncodingUtils;

import com.google.android.gcm.GCMRegistrar;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class CarmasterUtisFragment extends Fragment {
	public static final String ARG_OBJECT = "object";
	private static final String LOG_TAG = "UtisFragment";

	Context thiscontext;

	private TextView mTextView_status, mTextView_console, mTextView_console_bc,
			mTextView_console_send, mTextView_utis_massage,
			mTextView_utis_file_info;
	private ImageView mCCTV_img;
	private Button mButton;

	// private String serverIp_brodcast = "169.254.2.255"; // utis
	public static final String serverIp = "169.254.2.254"; // utis
	// public static final String serverIp = "192.168.2.68";
	public static final String rndisIp = "169.254.2.253";
	private int serverPort = 20000;
	private int CNS_UDP_RX_PORT = 20000;
	private static final int UDP_DATA_RECIEVE_FROM_OBE = 0;
	private static final int SET_TEXT_STATUS = 1;
	private static final int SET_TEXT_CONSOLE = 2;
	private static final int SET_TEXT_CONSOLE_BC = 3;
	private static final int SET_TEXT_CONSOLE_SEND = 4;
	private static final int SET_TEXT_UTIS_MESSAGE = 5;
	private static final int SET_TEXT_UTIS_FILE_INFO = 6;
	private static final int SET_TOAST_MESSAGE = 7;

	// Packet opset
	private static final int MAGIC_CODE_4 = 0;
	private static final int PROT_VER_1 = 4;
	private static final int HDR_LEN_1 = 5;
	private static final int HDR_CRC16_2 = 6;
	private static final int CODE_2 = 8;
	private static final int OPCODE_2 = 10;
	private static final int FROM_TO_2 = 12;
	private static final int PKT_ID_4 = 14;
	private static final int TOT_LEN_4 = 18;
	private static final int MORE_OFFSET_4 = 22;
	private static final int FRAG_PLEN_2 = 26;
	private static final int REQ_PKT_ID_4 = 28;
	private static final int BCAST_ID_4 = 32;
	private static final int CHECKSUM_4 = 36;
	private static final int CNT_1 = 36;
	private static final int EID_1 = 37;
	private static final int SIZE_N = 38;

	private static boolean parsing_pending = false;
	private static byte[] file_buff;
	private static boolean file_rx_mode = false;
	private static int file_rx_pkt_id = 1;
	private static int last_complite_rx_pkt_id = 0;

	private static final byte eid_multimedia = (byte) 0xFA;
	private static final byte eid_visibleString = (byte) 0xFB;
	public static byte[] obe_id = new byte[8];
	private static Thread sockThread = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView;
		rootView = inflater.inflate(R.layout.fragment_utis_test, container,
				false);

		thiscontext = container.getContext();

		mTextView_status = (TextView) rootView
				.findViewById(R.id.text_utis_status);
		mTextView_status.setText(String.format("[State] "));

		mTextView_console = (TextView) rootView
				.findViewById(R.id.text_utis_console);
		mTextView_console.setText(String.format("[Cmd] "));

		mTextView_console_bc = (TextView) rootView
				.findViewById(R.id.text_utis_console2);
		mTextView_console_bc.setText(String.format("[Receive] "));

		mTextView_console_send = (TextView) rootView
				.findViewById(R.id.text_utis_console_send);
		mTextView_console_send.setText(String.format("[Send] "));

		mTextView_utis_massage = (TextView) rootView
				.findViewById(R.id.text_utis_message);

		mTextView_utis_file_info = (TextView) rootView
				.findViewById(R.id.text_utis_file_info);
		mTextView_utis_file_info.setText(String.format("파일정보 : "));

		mCCTV_img = (ImageView) rootView.findViewById(R.id.img_cctv);

		new Thread(new SocketListener(CNS_UDP_RX_PORT)).start();

		mButton = (Button) rootView.findViewById(R.id.btn_0);
		mButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 0x0000
				CarmasterUtisUtill.doCmds("ifconfig rndis0 " + rndisIp);
				new Thread(req_register).start();
			}
		});

		mButton = (Button) rootView.findViewById(R.id.btn_1);
		mButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 0x0000
				// new Thread(new UDP_Tx(true, serverIp, "0801", "0000", "0040",
				// new String[] { "FB4954656C65776F726B73" })).start();
				new Thread(new UDP_Tx(true, 36, serverIp, "0801", "0000",
						"0040", new String[] { "F64100" })).start();
			}
		});

		mButton = (Button) rootView.findViewById(R.id.btn_2);
		mButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// new Thread(new UDP_Tx(serverIp, buf2, (byte) 0x28)).start();
				// 0x0003
				new Thread(new UDP_Tx(true, 36, serverIp, "0801", "0003",
						"0040", new String[] { "00480100000040000000" }))
						.start();
			}
		});

		mButton = (Button) rootView.findViewById(R.id.btn_3);
		mButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final Intent intent = new Intent(Intent.ACTION_MAIN, null);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				final ComponentName cn = new ComponentName(
						"com.android.settings",
						"com.android.settings.TetherSettings");
				intent.setComponent(cn);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});

		mButton = (Button) rootView.findViewById(R.id.btn_4);
		mButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				String s_obe_id = CarmasterUtisUtill.byteToHexString_noSpace(
						obe_id, 8);

				new Thread(new UDP_V2V(true, 32, serverIp, "0101", "2002",
						"00C2", new String[] { "0048" + s_obe_id,
								"01480100640080000002", "0A4400120000",
								"06443B9AD108", "07487B109D4B56D03B16",
								"0C420010",
								"F75074657374206465736372697074696F6E" }))
						.start();
			}
		});

		// 0x0000
		CarmasterUtisUtill.doCmds("ifconfig rndis0 " + rndisIp);
		new Thread(req_register).start();

		return rootView;
	}

	// main Handler
	private final Handler mMainHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SET_TEXT_STATUS:
				mTextView_status.setText("[State] " + (String) msg.obj);
				break;
			case SET_TEXT_CONSOLE:
				mTextView_console.setText("[Cmd] " + (String) msg.obj);
				break;
			case SET_TEXT_CONSOLE_BC:
				mTextView_console_bc.setText("[Receive] " + (String) msg.obj);
				break;
			case SET_TEXT_CONSOLE_SEND:
				mTextView_console_send.setText("[Send] " + (String) msg.obj);
				break;
			case SET_TEXT_UTIS_MESSAGE:
				mTextView_utis_massage.setText((String) msg.obj);
				mTextView_utis_massage.setSelected(false);
				mTextView_utis_massage.setSelected(true);
				break;
			case SET_TEXT_UTIS_FILE_INFO:
				mTextView_utis_file_info.setText((String) msg.obj);
				break;
			case UDP_DATA_RECIEVE_FROM_OBE:
				parsing_pending = true;
				parse_obe_fragment((byte[]) msg.obj);
				parsing_pending = false;
				break;
			case SET_TOAST_MESSAGE:
				Toast.makeText(getActivity(), (String) msg.obj,
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	class UDP_Tx extends Thread {
		String str;

		String mServerIP;
		// int sendLen;
		byte[] mHeader;// = new byte[36];
		byte[] mPayLoad;
		int mPayLoadCounter = 0;
		byte[] mbuf;
		byte[] crcbuff = new byte[2];
		boolean mNeed_Req;

		UDP_Tx(boolean need_Req, int headLen, String serverIP, String CODE,
				String OPCODE, String FROM_TO, String[] DATA) {
			mHeader = new byte[headLen];
			byte[] bytes;
			mNeed_Req = need_Req;
			mServerIP = serverIP;

			mHeader[MAGIC_CODE_4] = (byte) 0x55;
			mHeader[MAGIC_CODE_4 + 1] = (byte) 0x54;
			mHeader[MAGIC_CODE_4 + 2] = (byte) 0x49;
			mHeader[MAGIC_CODE_4 + 3] = (byte) 0x53;
			mHeader[PROT_VER_1] = (byte) 0x01;
			mHeader[HDR_LEN_1] = (byte) mHeader.length;
			bytes = CarmasterUtisUtill.hexStringToByteArray(CODE);
			mHeader[CODE_2] = bytes[0];
			mHeader[CODE_2 + 1] = bytes[1];
			bytes = CarmasterUtisUtill.hexStringToByteArray(OPCODE);
			mHeader[OPCODE_2] = bytes[0];
			mHeader[OPCODE_2 + 1] = bytes[1];
			bytes = CarmasterUtisUtill.hexStringToByteArray(FROM_TO);
			mHeader[FROM_TO_2] = bytes[0];
			mHeader[FROM_TO_2 + 1] = bytes[1];

			mHeader[PKT_ID_4 + 3] = 0x05;
			// mHeader[REQ_PKT_ID_4 + 0] = (byte) 0x0E;
			// mHeader[REQ_PKT_ID_4 + 1] = (byte) 0x1C;
			// mHeader[REQ_PKT_ID_4 + 2] = (byte) 0x8E;
			// mHeader[REQ_PKT_ID_4 + 3] = (byte) 0x77;

			if (null != DATA && 0 < DATA.length) { // have payload
				for (int i = 0; i < DATA.length; i++) {
					bytes = CarmasterUtisUtill.hexStringToByteArray(DATA[i]);
					mPayLoadCounter += bytes.length;
				}
				mPayLoad = new byte[mPayLoadCounter + 1];
				mPayLoad[0] = (byte) DATA.length;
				mPayLoadCounter = 1;
				for (int i = 0; i < DATA.length; i++) {
					bytes = CarmasterUtisUtill.hexStringToByteArray(DATA[i]);
					System.arraycopy(bytes, 0, mPayLoad, mPayLoadCounter,
							bytes.length);
					mPayLoadCounter += bytes.length;
				}

				Log.d("UDP", String.format("DATA.length : %d ", DATA.length));
				mbuf = new byte[mHeader.length + mPayLoad.length];
				System.arraycopy(mHeader, 0, mbuf, 0, mHeader.length);
				System.arraycopy(mPayLoad, 0, mbuf, mHeader.length,
						mPayLoad.length);
			} else {
				mbuf = new byte[mHeader.length];
				System.arraycopy(mHeader, 0, mbuf, 0, mHeader.length);
			}

			// // payload crc32
			// byte[] crc32 = genCrc32(mPayLoad, mPayLoad.length);
			// mbuf[BCAST_ID_4 + 0] = (byte) crc32[0];
			// mbuf[BCAST_ID_4 + 1] = (byte) crc32[1];
			// mbuf[BCAST_ID_4 + 2] = (byte) crc32[2];
			// mbuf[BCAST_ID_4 + 3] = (byte) crc32[3];

			mbuf[TOT_LEN_4 + 3] = (byte) mbuf.length;
			crcbuff = genCrc16(mbuf, mHeader.length); /* hdr_crc16 */
			mbuf[HDR_CRC16_2] = crcbuff[0];
			mbuf[HDR_CRC16_2 + 1] = crcbuff[1];

			mMainHandler.obtainMessage(SET_TEXT_CONSOLE_SEND, -1, -1,
					CarmasterUtisUtill.byteToHexString(mbuf, mbuf.length))
					.sendToTarget();
		}

		public void run() {
			DatagramSocket socket_udp;
			DatagramPacket packet_udp;
			// TODO Auto-generated method stub
			try {

				socket_udp = new DatagramSocket();
				packet_udp = new DatagramPacket(mbuf, mbuf.length,
						InetAddress.getByName(mServerIP), serverPort);

				/* Send out the packet */
				packet_udp.setData(mbuf);
				socket_udp.send(packet_udp);
				Log.d("UDP", "C: Sent.");

				if (false == mNeed_Req) {
					socket_udp.close();
					return;
				}

				// change receive buff size
				mbuf = new byte[1024];
				packet_udp.setData(mbuf);
				socket_udp.receive(packet_udp);
				byte[] temp = new byte[packet_udp.getLength()];

				System.arraycopy(packet_udp.getData(), 0, temp, 0,
						packet_udp.getLength());

				// if ((byte) 0x55 == temp[0] && (byte) 0x54 == temp[1]
				// && (byte) 0x49 == temp[2] && (byte) 0x53 == temp[3]) {
				// rx_obe_data(temp, temp.length);
				// }

				// set info
				if (70 < temp.length && temp[47] == 0x09) {
					System.arraycopy(temp, 39, obe_id, 0, 8);

					int portno = CarmasterUtisUtill.Byte2ToInt(temp[68],
							temp[69]);
					if (CNS_UDP_RX_PORT != portno) {
						mMainHandler.obtainMessage(
								SET_TEXT_STATUS,
								-1,
								-1,
								"UDP RECIEVE PORT : "
										+ portno
										+ "\nOBE ID : "
										+ CarmasterUtisUtill
												.byteToHexString_noSpace(
														obe_id, 8))
								.sendToTarget();
						CNS_UDP_RX_PORT = portno;
					}
				}

				mMainHandler.obtainMessage(SET_TEXT_CONSOLE, -1, -1,
						CarmasterUtisUtill.byteToHexString(temp, temp.length))
						.sendToTarget();
			} catch (Exception e) {
				Log.e("UDP", "C: Error", e);
				mMainHandler.obtainMessage(SET_TEXT_STATUS, -1, -1,
						"OBE UDP CONNECT ERROR").sendToTarget();
			}
		}
	}

	class UDP_V2V extends Thread {
		String str;

		String mServerIP;
		// int sendLen;
		byte[] mHeader;// = new byte[32];
		byte[] mPayLoad;
		int mPayLoadCounter = 0;
		byte[] mbuf;
		byte[] crcbuff = new byte[2];
		boolean mNeed_Req;

		UDP_V2V(boolean need_Req, int headLen, String serverIP, String CODE,
				String OPCODE, String FROM_TO, String[] DATA) {
			mHeader = new byte[headLen];
			byte[] bytes;
			mNeed_Req = need_Req;
			mServerIP = serverIP;

			mHeader[MAGIC_CODE_4] = (byte) 0x55;
			mHeader[MAGIC_CODE_4 + 1] = (byte) 0x54;
			mHeader[MAGIC_CODE_4 + 2] = (byte) 0x49;
			mHeader[MAGIC_CODE_4 + 3] = (byte) 0x53;
			mHeader[PROT_VER_1] = (byte) 0x01;
			mHeader[HDR_LEN_1] = (byte) mHeader.length;
			bytes = CarmasterUtisUtill.hexStringToByteArray(CODE);
			mHeader[CODE_2] = bytes[0];
			mHeader[CODE_2 + 1] = bytes[1];
			bytes = CarmasterUtisUtill.hexStringToByteArray(OPCODE);
			mHeader[OPCODE_2] = bytes[0];
			mHeader[OPCODE_2 + 1] = bytes[1];
			bytes = CarmasterUtisUtill.hexStringToByteArray(FROM_TO);
			mHeader[FROM_TO_2] = bytes[0];
			mHeader[FROM_TO_2 + 1] = bytes[1];

			mHeader[PKT_ID_4 + 3] = 0x05;

			if (null != DATA && 0 < DATA.length) { // have payload
				for (int i = 0; i < DATA.length; i++) {
					bytes = CarmasterUtisUtill.hexStringToByteArray(DATA[i]);
					mPayLoadCounter += bytes.length;
				}
				mPayLoad = new byte[mPayLoadCounter + 1];
				mPayLoad[0] = (byte) DATA.length;
				mPayLoadCounter = 1;
				for (int i = 0; i < DATA.length; i++) {
					bytes = CarmasterUtisUtill.hexStringToByteArray(DATA[i]);
					System.arraycopy(bytes, 0, mPayLoad, mPayLoadCounter,
							bytes.length);
					mPayLoadCounter += bytes.length;
				}

				Log.d("UDP", String.format("DATA.length : %d ", DATA.length));
				mbuf = new byte[mHeader.length + mPayLoad.length];

				// // payload crc32
				byte[] crc32 = genCrc32(mPayLoad, mPayLoad.length);
				mHeader[mHeader.length - 4] = (byte) crc32[0];
				mHeader[mHeader.length - 3] = (byte) crc32[1];
				mHeader[mHeader.length - 2] = (byte) crc32[2];
				mHeader[mHeader.length - 1] = (byte) crc32[3];

				System.arraycopy(mHeader, 0, mbuf, 0, mHeader.length);
				System.arraycopy(mPayLoad, 0, mbuf, mHeader.length,
						mPayLoad.length);
			} else {
				mbuf = new byte[mHeader.length];
				System.arraycopy(mHeader, 0, mbuf, 0, mHeader.length);
			}

			mbuf[TOT_LEN_4 + 3] = (byte) mbuf.length;
			crcbuff = genCrc16(mbuf, mHeader.length); /* hdr_crc16 */
			mbuf[HDR_CRC16_2] = crcbuff[0];
			mbuf[HDR_CRC16_2 + 1] = crcbuff[1];

			mMainHandler.obtainMessage(SET_TEXT_CONSOLE_SEND, -1, -1,
					CarmasterUtisUtill.byteToHexString(mbuf, mbuf.length))
					.sendToTarget();
		}

		public void run() {
			DatagramSocket socket_udp;
			DatagramPacket packet_udp;
			// TODO Auto-generated method stub
			try {

				socket_udp = new DatagramSocket();
				packet_udp = new DatagramPacket(mbuf, mbuf.length,
						InetAddress.getByName(mServerIP), serverPort);

				/* Send out the packet */
				packet_udp.setData(mbuf);
				socket_udp.send(packet_udp);
				Log.d("UDP", "C: Sent.");

				if (false == mNeed_Req) {
					socket_udp.close();
					return;
				}

				// change receive buff size
				mbuf = new byte[1024];
				packet_udp.setData(mbuf);
				socket_udp.receive(packet_udp);
				byte[] temp = new byte[packet_udp.getLength()];

				System.arraycopy(packet_udp.getData(), 0, temp, 0,
						packet_udp.getLength());

				// if ((byte) 0x55 == temp[0] && (byte) 0x54 == temp[1]
				// && (byte) 0x49 == temp[2] && (byte) 0x53 == temp[3]) {
				// rx_obe_data(temp, temp.length);
				// }

				// set info
				if (70 < temp.length && temp[47] == 0x09) {
					System.arraycopy(temp, 39, obe_id, 0, 8);

					int portno = CarmasterUtisUtill.Byte2ToInt(temp[68],
							temp[69]);
					if (CNS_UDP_RX_PORT != portno) {
						mMainHandler.obtainMessage(
								SET_TEXT_STATUS,
								-1,
								-1,
								"UDP RECIEVE PORT : "
										+ portno
										+ "\nOBE ID : "
										+ CarmasterUtisUtill
												.byteToHexString_noSpace(
														obe_id, 8))
								.sendToTarget();
						CNS_UDP_RX_PORT = portno;
					}
				}

				mMainHandler.obtainMessage(SET_TEXT_CONSOLE, -1, -1,
						CarmasterUtisUtill.byteToHexString(temp, temp.length))
						.sendToTarget();
			} catch (Exception e) {
				Log.e("UDP", "C: Error", e);
				mMainHandler.obtainMessage(SET_TEXT_STATUS, -1, -1,
						"OBE UDP CONNECT ERROR").sendToTarget();
			}
		}
	}

	private Runnable rndis_ipset(final String ip) {
		Runnable run = new Runnable() {
			public void run() {
				CarmasterUtisUtill.doCmds("ifconfig rndis0 " + ip);
			}
		};
		return run;
	}

	Runnable req_register = new Runnable() {
		public void run() {
			// CarmasterUtisUtill.doCmds("ifconfig rndis0 " + rndisIp);
			thread_sleep(500);
			tx_to_obe(true, serverIp, "0801", "0000", "0040",
					new String[] { "F64100" });
			thread_sleep(500);
			tx_to_obe(true, serverIp, "0801", "0003", "0040",
					new String[] { "00480100000040000000" });
			thread_sleep(500);
			tx_to_obe(true, serverIp, "0801", "0000", "0040",
					new String[] { "FB4954656C65776F726B73" });
		}
	};

	void thread_sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};

	void tx_to_obe(boolean need_Req, String serverIP, String CODE,
			String OPCODE, String FROM_TO, String[] DATA) {
		// String mServerIP;
		// int sendLen;
		byte[] mHeader = new byte[36];
		byte[] mPayLoad;
		int mPayLoadCounter = 0;
		byte[] mbuf;
		byte[] crcbuff = new byte[2];
		byte[] temp_bytes;
		// mNeed_Req = need_Req;
		// mServerIP = serverIP;

		mHeader[MAGIC_CODE_4] = (byte) 0x55;
		mHeader[MAGIC_CODE_4 + 1] = (byte) 0x54;
		mHeader[MAGIC_CODE_4 + 2] = (byte) 0x49;
		mHeader[MAGIC_CODE_4 + 3] = (byte) 0x53;
		mHeader[PROT_VER_1] = (byte) 0x01;
		mHeader[HDR_LEN_1] = (byte) mHeader.length;
		temp_bytes = CarmasterUtisUtill.hexStringToByteArray(CODE);
		mHeader[CODE_2] = temp_bytes[0];
		mHeader[CODE_2 + 1] = temp_bytes[1];
		temp_bytes = CarmasterUtisUtill.hexStringToByteArray(OPCODE);
		mHeader[OPCODE_2] = temp_bytes[0];
		mHeader[OPCODE_2 + 1] = temp_bytes[1];
		temp_bytes = CarmasterUtisUtill.hexStringToByteArray(FROM_TO);
		mHeader[FROM_TO_2] = temp_bytes[0];
		mHeader[FROM_TO_2 + 1] = temp_bytes[1];

		if (null != DATA && 0 < DATA.length) { // have payload
			for (int i = 0; i < DATA.length; i++) {
				temp_bytes = CarmasterUtisUtill.hexStringToByteArray(DATA[i]);
				mPayLoadCounter += temp_bytes.length;
			}
			mPayLoad = new byte[mPayLoadCounter + 1];
			mPayLoad[0] = (byte) DATA.length;
			mPayLoadCounter = 1;
			for (int i = 0; i < DATA.length; i++) {
				temp_bytes = CarmasterUtisUtill.hexStringToByteArray(DATA[i]);
				System.arraycopy(temp_bytes, 0, mPayLoad, mPayLoadCounter,
						temp_bytes.length);
				mPayLoadCounter += temp_bytes.length;
			}

			Log.d("UDP", String.format("DATA.length : %d ", DATA.length));
			mbuf = new byte[mHeader.length + mPayLoad.length];
			System.arraycopy(mHeader, 0, mbuf, 0, mHeader.length);
			System.arraycopy(mPayLoad, 0, mbuf, mHeader.length, mPayLoad.length);
		} else {
			mbuf = new byte[mHeader.length];
			System.arraycopy(mHeader, 0, mbuf, 0, mHeader.length);
		}

		mbuf[TOT_LEN_4 + 3] = (byte) mbuf.length;
		crcbuff = genCrc16(mbuf, mHeader.length); /* hdr_crc16 */
		mbuf[HDR_CRC16_2] = crcbuff[0];
		mbuf[HDR_CRC16_2 + 1] = crcbuff[1];

		mMainHandler.obtainMessage(SET_TEXT_CONSOLE_SEND, -1, -1,
				CarmasterUtisUtill.byteToHexString(mbuf, mbuf.length))
				.sendToTarget();

		DatagramSocket socket_udp;
		DatagramPacket packet_udp;
		// TODO Auto-generated method stub
		try {
			socket_udp = new DatagramSocket();
			packet_udp = new DatagramPacket(mbuf, mbuf.length,
					InetAddress.getByName(serverIP), serverPort);
			// socket_udp.setSoTimeout(500);

			/* Send out the packet */
			packet_udp.setData(mbuf);
			socket_udp.send(packet_udp);
			Log.d("UDP", "C: Sent.");

			// change receive buff size
			mbuf = new byte[1024];
			packet_udp.setData(mbuf);
			socket_udp.receive(packet_udp);
			byte[] temp = new byte[packet_udp.getLength()];

			System.arraycopy(packet_udp.getData(), 0, temp, 0,
					packet_udp.getLength());

			// if ((byte) 0x55 == temp[0] && (byte) 0x54 == temp[1]
			// && (byte) 0x49 == temp[2] && (byte) 0x53 == temp[3]) {
			// rx_obe_data(temp, temp.length);
			// }

			// set info
			if (70 < temp.length && temp[47] == 0x09) {
				System.arraycopy(temp, 39, obe_id, 0, 8);

				int portno = CarmasterUtisUtill.Byte2ToInt(temp[68], temp[69]);
				if (CNS_UDP_RX_PORT != portno) {
					mMainHandler
							.obtainMessage(
									SET_TEXT_STATUS,
									-1,
									-1,
									"UDP RECIEVE PORT : "
											+ portno
											+ " OBE ID : "
											+ CarmasterUtisUtill
													.byteToHexString_noSpace(
															obe_id, 8))
							.sendToTarget();
					CNS_UDP_RX_PORT = portno;
				}
			}

			mMainHandler.obtainMessage(SET_TEXT_CONSOLE, -1, -1,
					CarmasterUtisUtill.byteToHexString(temp, temp.length))
					.sendToTarget();
		} catch (Exception e) {
			Log.e("UDP", "C: Error", e);
			mMainHandler.obtainMessage(SET_TEXT_STATUS, -1, -1,
					"OBE UDP CONNECT ERROR").sendToTarget();
		}
	}

	class SocketListener extends Thread {
		DatagramSocket socket_reciever = null;
		DatagramPacket rx_packet;
		int rx_port;

		SocketListener(int port) {
			rx_port = port;
		}

		public void run() {
			byte[] buf = new byte[5000];
			while (!Thread.interrupted()) {
				try {
					if (null == socket_reciever) {
						Log.d("UDP Rx", "new DatagramSocket");
						socket_reciever = new DatagramSocket(CNS_UDP_RX_PORT);
						socket_reciever.setSoTimeout(2000);
					}
					rx_packet = new DatagramPacket(buf, buf.length);
					socket_reciever.receive(rx_packet);
					Log.d("UDP Rx", "Received packet");

					byte[] temp = null;
					temp = rx_packet.getData();

					if (null != temp && 0 < temp.length) {
						if (true == parsing_pending)
							continue;
						if ((byte) 0x55 == temp[0] && (byte) 0x54 == temp[1]
								&& (byte) 0x49 == temp[2]
								&& (byte) 0x53 == temp[3]) {
							rx_obe_data(temp, rx_packet.getLength());
						}
					}

				} catch (SocketTimeoutException e) {
					socket_reciever.close();
					socket_reciever = null;
				} catch (IOException e) {
					Log.e(getClass().getName(), e.getMessage());
				}
			}
		}
	}

	static boolean data_complite = false;

	void rx_obe_data(byte[] data_in, int len) {

		// printf
		String s;
		if (200 > len)
			s = CarmasterUtisUtill.byteToHexString(data_in, len);
		else
			s = CarmasterUtisUtill.byteToHexString(data_in, 100);
		mMainHandler.obtainMessage(
				SET_TEXT_CONSOLE_BC,
				-1,
				-1,
				String.format("OPCODE [%02X%02X], ", data_in[OPCODE_2],
						data_in[OPCODE_2 + 1]) + len + " bytes \n" + s)
				.sendToTarget();

		if ((byte) 0x24 > len)
			return;

		int headerLen = data_in[HDR_LEN_1];
		int totLen = CarmasterUtisUtill.Byte4ToInt(data_in[TOT_LEN_4],
				data_in[TOT_LEN_4 + 1], data_in[TOT_LEN_4 + 2],
				data_in[TOT_LEN_4 + 3]);

		if (len < headerLen && totLen < headerLen) {
			// size error
			return;
		}

		// opcode - V2V
		if (len > 142 && (byte) 0x20 == data_in[OPCODE_2]
				&& (byte) 0x02 == data_in[OPCODE_2 + 1]) {
			byte[] temp_obeid = new byte[8];
			System.arraycopy(data_in, 99, temp_obeid, 0, 8);

			byte[] temp_type_act = { data_in[141], data_in[142] };
			String s_type_act = "";

			if ((byte) 0x04 == (temp_type_act[1] & (byte) 0x04)) {
				s_type_act = s_type_act + "차량사고, ";
			}
			if ((byte) 0x08 == (temp_type_act[1] & (byte) 0x08)) {
				s_type_act = s_type_act + "기상관련, ";
			}
			if ((byte) 0x10 == (temp_type_act[1] & (byte) 0x10)) {
				s_type_act = s_type_act + "기후/고장, ";
			}
			if ((byte) 0x20 == (temp_type_act[1] & (byte) 0x20)) {
				s_type_act = s_type_act + "화재, ";
			}
			if ((byte) 0x40 == (temp_type_act[1] & (byte) 0x40)) {
				s_type_act = s_type_act + "장애물, ";
			}
			if ((byte) 0x80 == (temp_type_act[1] & (byte) 0x80)) {
				s_type_act = s_type_act + "위험물, ";
			}
			if ((byte) 0x01 == (temp_type_act[0] & (byte) 0x01)) {
				s_type_act = s_type_act + "지진, ";
			}
			if ((byte) 0x02 == (temp_type_act[0] & (byte) 0x02)) {
				s_type_act = s_type_act + "산사태, ";
			}
			if ((byte) 0x04 == (temp_type_act[0] & (byte) 0x04)) {
				s_type_act = s_type_act + "홍수";
			}

			String format = new String("yyyyMMddHHmmss");
			SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.KOREA);
			String time = sdf.format(new Date());

			mMainHandler.obtainMessage(
					SET_TOAST_MESSAGE,
					-1,
					-1,
					String.format(
							"응급상황[%s] \n차량ID[%s]\n사고위치[36.3652189,127.3379818]\n사고시간["
									+ time + "]", s_type_act,
							CarmasterUtisUtill.byteToHexString_noSpace(
									temp_obeid, 8))).sendToTarget();

		}
		// opcode - media data
		else if ((byte) 0xF0 == data_in[OPCODE_2]
				&& (byte) 0x00 <= data_in[OPCODE_2 + 1]
				&& (byte) 0x06 >= data_in[OPCODE_2 + 1]) {

			if (totLen == headerLen) {
				// no payload

			} else if ((byte) 0x01 == (data_in[CODE_2 + 1] & (byte) 0x01)) {
				// have payload
				int req_pkt_id = CarmasterUtisUtill.Byte4ToInt(
						data_in[REQ_PKT_ID_4], data_in[REQ_PKT_ID_4 + 1],
						data_in[REQ_PKT_ID_4 + 2], data_in[REQ_PKT_ID_4 + 3]);

				int fragmentLen = CarmasterUtisUtill.Byte2ToInt(
						data_in[FRAG_PLEN_2], data_in[FRAG_PLEN_2 + 1]);
				int fragment_offset = CarmasterUtisUtill.Byte4ToInt(
						(byte) (data_in[MORE_OFFSET_4] & (byte) 0x7F),
						data_in[MORE_OFFSET_4 + 1], data_in[MORE_OFFSET_4 + 2],
						data_in[MORE_OFFSET_4 + 3]);

				if (last_complite_rx_pkt_id == req_pkt_id) {
					return;
				}

				if (true == file_rx_mode) {
					if (req_pkt_id == file_rx_pkt_id) {
						if ((byte) 0x00 == (data_in[MORE_OFFSET_4] & (byte) 0x80)) {
							if (true == data_complite) {
								mMainHandler.obtainMessage(
										UDP_DATA_RECIEVE_FROM_OBE, -1, -1,
										file_buff).sendToTarget();
								file_rx_mode = false;
								data_complite = false;
								last_complite_rx_pkt_id = req_pkt_id;

							} else {
								data_complite = true;
							}
						}
						if (eid_multimedia != data_in[headerLen + 1]) // eid
							System.arraycopy(data_in, headerLen, file_buff,
									fragment_offset, fragmentLen);
					} else {
						file_rx_mode = false;
						data_complite = false;
					}
				} else { // fragment file receive continuous
					// more data
					if ((byte) 0x80 == (data_in[MORE_OFFSET_4] & (byte) 0x80)) {
						file_buff = new byte[totLen - headerLen];
						System.arraycopy(data_in, headerLen, file_buff, 0,
								fragmentLen);
						file_rx_pkt_id = req_pkt_id;
						file_rx_mode = true;
					} else {

						file_buff = new byte[totLen - headerLen];
						System.arraycopy(data_in, headerLen, file_buff, 0,
								file_buff.length);
						mMainHandler.obtainMessage(UDP_DATA_RECIEVE_FROM_OBE,
								-1, -1, file_buff).sendToTarget();
						last_complite_rx_pkt_id = req_pkt_id;
					}
				}
			}
		}
	}

	void parse_obe_fragment(byte[] data_in) {
		int count = CarmasterUtisUtill.Byte1ToInt(data_in[0]);
		int countIeLenByte;
		int it = 1;
		byte[] buff;

		for (int i = 0; i < count; i++) {
			byte ed_size_type = (byte) (data_in[it + 1] & 0xC0);
			int ieLen = 0;

			if ((byte) 0x40 == ed_size_type) {
				// size 6bit
				countIeLenByte = 1;
				ieLen = CarmasterUtisUtill
						.Byte1ToInt((byte) (data_in[it + 1] & (byte) 0x3F));
			} else if ((byte) 0x80 == ed_size_type) {
				// size 14bit
				countIeLenByte = 2;
				ieLen = CarmasterUtisUtill.Byte2ToInt(
						(byte) (data_in[it + 1] & (byte) 0x3F),
						(byte) data_in[it + 2]);
			} else if ((byte) 0x00 == ed_size_type) {
				// size 30bit
				countIeLenByte = 4;
				ieLen = CarmasterUtisUtill.Byte4ToInt(
						(byte) ((byte) data_in[it + 1] & (byte) 0x3F),
						(byte) data_in[it + 2], (byte) data_in[it + 3],
						(byte) data_in[it + 4]);
			} else {
				// length error
				return;
			}
			buff = new byte[1 + countIeLenByte + ieLen];
			System.arraycopy(data_in, it, buff, 0, buff.length);
			it += buff.length;
			parse_obe_eid(buff);
		}
	}

	void parse_obe_eid(byte[] data_in) {

		byte ed_size_type = (byte) (data_in[1] & 0xC0);
		byte eid = data_in[0]; // eid
		int ieLen = 0;

		if ((byte) 0x40 == ed_size_type) {
			// size 6bit
			ieLen = CarmasterUtisUtill
					.Byte1ToInt((byte) (data_in[1] & (byte) 0x3F));
		} else if ((byte) 0x80 == ed_size_type) {
			// size 14bit
			ieLen = CarmasterUtisUtill.Byte2ToInt(
					(byte) (data_in[1] & (byte) 0x3F), (byte) data_in[2]);
		} else if ((byte) 0x00 == ed_size_type) {
			// size 30bit
			ieLen = CarmasterUtisUtill.Byte4ToInt(
					(byte) ((byte) data_in[1] & (byte) 0x3F),
					(byte) data_in[2], (byte) data_in[3], (byte) data_in[4]);
		} else {
			// length error
			return;
		}

		switch (eid) {
		case eid_multimedia:
			Bitmap bmp;
			int info_len = 1 + 16 + 64;
			int media_type = data_in[data_in.length - ieLen];

			byte[] file_info = new byte[64];
			byte[] file_data = new byte[ieLen - info_len];

			// file info
			System.arraycopy(data_in, data_in.length - ieLen + info_len - 64,
					file_info, 0, 64);

			// file data
			System.arraycopy(data_in, data_in.length - ieLen + info_len,
					file_data, 0, ieLen - info_len);

			mMainHandler.obtainMessage(
					SET_TEXT_UTIS_FILE_INFO,
					-1,
					-1,
					CarmasterUtisUtill.toKor(CarmasterUtisUtill
							.byteToString(file_info))).sendToTarget();

			switch (media_type) {
			case 1:
				bmp = BitmapFactory.decodeByteArray(file_data, 0,
						file_data.length);
				mCCTV_img.setImageBitmap(bmp);

				mMainHandler.obtainMessage(
						SET_TEXT_UTIS_FILE_INFO,
						-1,
						-1,
						"GIF 파일 정보 : "
								+ CarmasterUtisUtill.toKor(CarmasterUtisUtill
										.byteToString(file_info)))
						.sendToTarget();
				break;
			case 2:
				bmp = BitmapFactory.decodeByteArray(file_data, 0,
						file_data.length);
				mCCTV_img.setImageBitmap(bmp);

				mMainHandler.obtainMessage(
						SET_TEXT_UTIS_FILE_INFO,
						-1,
						-1,
						"JPEG 파일 정보 : "
								+ CarmasterUtisUtill.toKor(CarmasterUtisUtill
										.byteToString(file_info)))
						.sendToTarget();
				break;
			case 3:
				new CarmasterUtisUtill.playWav(thiscontext, file_data);

				mMainHandler.obtainMessage(
						SET_TEXT_UTIS_FILE_INFO,
						-1,
						-1,
						"PCM 파일 정보 : "
								+ CarmasterUtisUtill.toKor(CarmasterUtisUtill
										.byteToString(file_info)))
						.sendToTarget();
				break;
			case 4:
				bmp = BitmapFactory.decodeByteArray(file_data, 0,
						file_data.length);
				mCCTV_img.setImageBitmap(bmp);

				mMainHandler.obtainMessage(
						SET_TEXT_UTIS_FILE_INFO,
						-1,
						-1,
						"CCTV 정지영상 파일 정보 : "
								+ CarmasterUtisUtill.toKor(CarmasterUtisUtill
										.byteToString(file_info)))
						.sendToTarget();
				break;
			case 5:
				new CarmasterUtisUtill.playWav(thiscontext, file_data);

				mMainHandler.obtainMessage(
						SET_TEXT_UTIS_FILE_INFO,
						-1,
						-1,
						"MP3 파일 정보 : "
								+ CarmasterUtisUtill.toKor(CarmasterUtisUtill
										.byteToString(file_info)))
						.sendToTarget();
				break;
			}
			break;

		case eid_visibleString:

			Log.d("UDP", "TEXT MSG RECIEVED FROM_OBE.");

			// ACK CODE HERE
			//

			byte[] textMsg = new byte[ieLen];
			System.arraycopy(data_in, data_in.length - ieLen, textMsg, 0, ieLen);
			String msg = CarmasterUtisUtill.toKor(CarmasterUtisUtill
					.byteToString(textMsg));
			mMainHandler.obtainMessage(SET_TEXT_UTIS_MESSAGE, -1, -1, msg)
					.sendToTarget();
			new CarmasterUtisUtill.TextToSpeecht(thiscontext, msg);

			break;

		case 0x09:
			// if (70 < temp.length && temp[47] == 0x09) {
			// int portno = CarmasterUtisUtill.Byte2ToInt(data_in[9],
			// data_in[10]);
			// if (CNS_UDP_RX_PORT != portno) {
			// mMainHandler.obtainMessage(SET_TEXT_STATUS, -1, -1,
			// "UDP RECIEVE PORT : " + CNS_UDP_RX_PORT).sendToTarget();
			// CNS_UDP_RX_PORT = portno;
			// }
			// }
			break;
		}
	}

	static {
		System.loadLibrary("Utis");
	}

	public static native byte[] genCrc16(byte[] src, int len);

	public static native byte[] genCrc32(byte[] src, int len);

	public static native int shellCmd(String cmdStr);

}
