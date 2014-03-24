package com.teleworks.carmaster;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
	private String serverIp = "169.254.2.254"; // utis
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

		if (null == sockThread) {
			sockThread = new Thread(new SocketListener(20000));
			sockThread.start();
		} else {
			sockThread.interrupt();
			sockThread = new Thread(new SocketListener(CNS_UDP_RX_PORT));
			sockThread.start();
			// new Thread(new UDP_Tx(true, serverIp, "0801", "0000", "0040",
			// new String[] { "F64100" })).start();
			tx_to_obe(true, serverIp, "0801", "0000", "0040",
					new String[] { "F64100" });
		}
		// new Thread(new SocketListener(20000)).start();

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
				new Thread(new UDP_Tx(true, serverIp, "0801", "0000", "0040",
						new String[] { "F64100" })).start();
			}
		});

		mButton = (Button) rootView.findViewById(R.id.btn_2);
		mButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// new Thread(new UDP_Tx(serverIp, buf2, (byte) 0x28)).start();
				// 0x0003
				new Thread(new UDP_Tx(true, serverIp, "0801", "0003", "0040",
						new String[] { "00480100000040000000" })).start();
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
				new Thread(new UDP_Tx(true, serverIp, "7001", "2002", "00C2",
						new String[] { "00480100000040000000",
								"01480000000000000000", "0A4400000000",
								"074400000000", "0C420004",
								"FB4954656C65776F726B73" })).start();
			}
		});

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
			}
		}
	};

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
						socket_reciever = new DatagramSocket(rx_port);
						socket_reciever.setSoTimeout(1000);
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

	public static String byteToString(byte[] bytebuf) {
		String result = EncodingUtils.getString(bytebuf, 0, bytebuf.length,
				"Latin-1");
		return result;
	}

	public static byte[] stringToByte(String $byteString) {
		byte[] byteArray = EncodingUtils.getBytes($byteString, "Latin-1");
		return byteArray;
	}

	public static String byteToHexString(byte[] bytebuf, int len) {
		String result = "";
		for (int i = 0; i < len; i++) {
			result += String.format("%02X ", bytebuf[i]);
		}
		return result;
	}

	class UDP_Tx extends Thread {
		String str;
		String s;
		String mServerIP;
		// int sendLen;
		byte[] mHeader = new byte[36];
		byte[] mPayLoad;
		int mPayLoadCounter = 0;
		byte[] mbuf;
		byte[] crcbuff = new byte[2];
		boolean mNeed_Req;

		UDP_Tx(boolean need_Req, String serverIP, String CODE, String OPCODE,
				String FROM_TO, String[] DATA) {
			byte[] bytes;
			mNeed_Req = need_Req;
			mServerIP = serverIP;

			mHeader[MAGIC_CODE_4] = (byte) 0x55;
			mHeader[MAGIC_CODE_4 + 1] = (byte) 0x54;
			mHeader[MAGIC_CODE_4 + 2] = (byte) 0x49;
			mHeader[MAGIC_CODE_4 + 3] = (byte) 0x53;
			mHeader[PROT_VER_1] = (byte) 0x01;
			mHeader[HDR_LEN_1] = (byte) mHeader.length;
			bytes = hexStringToByteArray(CODE);
			mHeader[CODE_2] = bytes[0];
			mHeader[CODE_2 + 1] = bytes[1];
			bytes = hexStringToByteArray(OPCODE);
			mHeader[OPCODE_2] = bytes[0];
			mHeader[OPCODE_2 + 1] = bytes[1];
			bytes = hexStringToByteArray(FROM_TO);
			mHeader[FROM_TO_2] = bytes[0];
			mHeader[FROM_TO_2 + 1] = bytes[1];

			if (null != DATA && 0 < DATA.length) { // have payload
				for (int i = 0; i < DATA.length; i++) {
					bytes = hexStringToByteArray(DATA[i]);
					mPayLoadCounter += bytes.length;
				}
				mPayLoad = new byte[mPayLoadCounter + 1];
				mPayLoad[0] = (byte) DATA.length;
				mPayLoadCounter = 1;
				for (int i = 0; i < DATA.length; i++) {
					bytes = hexStringToByteArray(DATA[i]);
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

			mbuf[TOT_LEN_4 + 3] = (byte) mbuf.length;
			crcbuff = genCrc16(mbuf, mHeader.length); /* hdr_crc16 */
			mbuf[HDR_CRC16_2] = crcbuff[0];
			mbuf[HDR_CRC16_2 + 1] = crcbuff[1];

			mMainHandler.obtainMessage(SET_TEXT_CONSOLE_SEND, -1, -1,
					byteToHexString(mbuf, mbuf.length)).sendToTarget();
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

				if (70 < temp.length && temp[47] == 0x09) {
					int portno = Byte2ToInt(temp[68], temp[69]);
					if (CNS_UDP_RX_PORT != portno) {
						mMainHandler.obtainMessage(SET_TEXT_STATUS, -1, -1,
								"UDP RECIEVE PORT : " + CNS_UDP_RX_PORT)
								.sendToTarget();
						CNS_UDP_RX_PORT = portno;
					}
				}

				s = byteToHexString(temp, temp.length);
				mMainHandler.obtainMessage(SET_TEXT_CONSOLE, -1, -1, s)
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
		String s;
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
		temp_bytes = hexStringToByteArray(CODE);
		mHeader[CODE_2] = temp_bytes[0];
		mHeader[CODE_2 + 1] = temp_bytes[1];
		temp_bytes = hexStringToByteArray(OPCODE);
		mHeader[OPCODE_2] = temp_bytes[0];
		mHeader[OPCODE_2 + 1] = temp_bytes[1];
		temp_bytes = hexStringToByteArray(FROM_TO);
		mHeader[FROM_TO_2] = temp_bytes[0];
		mHeader[FROM_TO_2 + 1] = temp_bytes[1];

		if (null != DATA && 0 < DATA.length) { // have payload
			for (int i = 0; i < DATA.length; i++) {
				temp_bytes = hexStringToByteArray(DATA[i]);
				mPayLoadCounter += temp_bytes.length;
			}
			mPayLoad = new byte[mPayLoadCounter + 1];
			mPayLoad[0] = (byte) DATA.length;
			mPayLoadCounter = 1;
			for (int i = 0; i < DATA.length; i++) {
				temp_bytes = hexStringToByteArray(DATA[i]);
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
				byteToHexString(mbuf, mbuf.length)).sendToTarget();

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

			if ((byte) 0x55 == temp[0] && (byte) 0x54 == temp[1]
					&& (byte) 0x49 == temp[2] && (byte) 0x53 == temp[3]) {
				rx_obe_data(temp, temp.length);
			}

			if (70 < temp.length && temp[47] == 0x09) {
				int portno = Byte2ToInt(temp[68], temp[69]);
				if (CNS_UDP_RX_PORT != portno) {
					CNS_UDP_RX_PORT = portno;
				}
				mMainHandler.obtainMessage(SET_TEXT_STATUS, -1, -1,
						"UDP RECIEVE PORT : " + CNS_UDP_RX_PORT).sendToTarget();
			}

			// s = byteToHexString(temp, temp.length);
			// mMainHandler.obtainMessage(SET_TEXT_CONSOLE, -1, -1, s)
			// .sendToTarget();
		} catch (Exception e) {
			Log.e("UDP", "C: Error", e);
			mMainHandler.obtainMessage(SET_TEXT_STATUS, -1, -1,
					"OBE UDP CONNECT ERROR").sendToTarget();
		}

	}

	static boolean data_complite = false;

	void rx_obe_data(byte[] data_in, int len) {

		if ((byte) 0x1C > len)
			return;

		int headerLen = data_in[HDR_LEN_1];
		int totLen = Byte4ToInt(data_in[TOT_LEN_4], data_in[TOT_LEN_4 + 1],
				data_in[TOT_LEN_4 + 2], data_in[TOT_LEN_4 + 3]);
		int req_pkt_id = Byte4ToInt(data_in[REQ_PKT_ID_4],
				data_in[REQ_PKT_ID_4 + 1], data_in[REQ_PKT_ID_4 + 2],
				data_in[REQ_PKT_ID_4 + 3]);

		String s;
		if (100 > len)
			s = byteToHexString(data_in, len);
		else
			s = byteToHexString(data_in, 100);
		mMainHandler.obtainMessage(SET_TEXT_CONSOLE_BC, -1, -1,
				len + " bytes \n" + s).sendToTarget();

		if (len < headerLen && totLen < headerLen) {
			// size error
			return;
		}

		if (totLen == headerLen) {
			// no payload

		} else if ((byte) 0x01 == (data_in[CODE_2 + 1] & (byte) 0x01)) {
			// have payload

			int fragmentLen = Byte2ToInt(data_in[FRAG_PLEN_2],
					data_in[FRAG_PLEN_2 + 1]);
			int fragment_offset = Byte4ToInt(
					(byte) (data_in[MORE_OFFSET_4] & (byte) 0x7F),
					data_in[MORE_OFFSET_4 + 1], data_in[MORE_OFFSET_4 + 2],
					data_in[MORE_OFFSET_4 + 3]);

			// Print Rx data
			// String s;
			// if (100 > len)
			// s = byteToHexString(data_in, len);
			// else
			// s = byteToHexString(data_in, 100);
			// mMainHandler.obtainMessage(SET_TEXT_CONSOLE_BC, -1, -1,
			// len + " bytes \n" + s).sendToTarget();

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

				// if ((byte) 0x01 != data_in[headerLen])
				// return;

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
					mMainHandler.obtainMessage(UDP_DATA_RECIEVE_FROM_OBE, -1,
							-1, file_buff).sendToTarget();
					last_complite_rx_pkt_id = req_pkt_id;
				}

			}
		}
	}

	void parse_obe_fragment(byte[] data_in) {
		int count = Byte1ToInt(data_in[0]);
		int countIeLenByte;
		int it = 1;
		byte[] buff;

		for (int i = 0; i < count; i++) {
			byte ed_size_type = (byte) (data_in[it + 1] & 0xC0);
			int ieLen = 0;

			if ((byte) 0x40 == ed_size_type) {
				// size 6bit
				countIeLenByte = 1;
				ieLen = Byte1ToInt((byte) (data_in[it + 1] & (byte) 0x3F));
			} else if ((byte) 0x80 == ed_size_type) {
				// size 14bit
				countIeLenByte = 2;
				ieLen = Byte2ToInt((byte) (data_in[it + 1] & (byte) 0x3F),
						(byte) data_in[it + 2]);
			} else if ((byte) 0x00 == ed_size_type) {
				// size 30bit
				countIeLenByte = 4;
				ieLen = Byte4ToInt(
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
			ieLen = Byte1ToInt((byte) (data_in[1] & (byte) 0x3F));
		} else if ((byte) 0x80 == ed_size_type) {
			// size 14bit
			ieLen = Byte2ToInt((byte) (data_in[1] & (byte) 0x3F),
					(byte) data_in[2]);
		} else if ((byte) 0x00 == ed_size_type) {
			// size 30bit
			ieLen = Byte4ToInt((byte) ((byte) data_in[1] & (byte) 0x3F),
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

			mMainHandler.obtainMessage(SET_TEXT_UTIS_FILE_INFO, -1, -1,
					CarmasterUtisUtill.toKor(byteToString(file_info)))
					.sendToTarget();

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
								+ CarmasterUtisUtill
										.toKor(byteToString(file_info)))
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
								+ CarmasterUtisUtill
										.toKor(byteToString(file_info)))
						.sendToTarget();
				break;
			case 3:
				new CarmasterUtisUtill.playWav(thiscontext, file_data);

				mMainHandler.obtainMessage(
						SET_TEXT_UTIS_FILE_INFO,
						-1,
						-1,
						"PCM 파일 정보 : "
								+ CarmasterUtisUtill
										.toKor(byteToString(file_info)))
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
								+ CarmasterUtisUtill
										.toKor(byteToString(file_info)))
						.sendToTarget();
				break;
			case 5:
				new CarmasterUtisUtill.playWav(thiscontext, file_data);

				mMainHandler.obtainMessage(
						SET_TEXT_UTIS_FILE_INFO,
						-1,
						-1,
						"MP3 파일 정보 : "
								+ CarmasterUtisUtill
										.toKor(byteToString(file_info)))
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
			String msg = CarmasterUtisUtill.toKor(byteToString(textMsg));
			mMainHandler.obtainMessage(SET_TEXT_UTIS_MESSAGE, -1, -1, msg)
					.sendToTarget();
			new CarmasterUtisUtill.TextToSpeecht(thiscontext, msg);

			break;

		case 0x09:
			// if (70 < temp.length && temp[47] == 0x09) {
			// int portno = Byte2ToInt(data_in[9], data_in[10]);
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

	public static native int shellCmd(String cmdStr);

	public static int Byte4ToInt(byte Byte_0, byte Byte_1, byte Byte_2,
			byte Byte_3) {
		byte[] byteV = { Byte_0, Byte_1, Byte_2, Byte_3 };
		ByteBuffer buff = ByteBuffer.allocate(4);
		buff = ByteBuffer.wrap(byteV);
		buff.order(ByteOrder.BIG_ENDIAN);
		return buff.getInt();
	}

	public static int Byte2ToInt(byte Byte_H, byte Byte_L) {
		byte[] byteV = { 0, 0, Byte_H, Byte_L };
		ByteBuffer buff = ByteBuffer.allocate(4);
		buff = ByteBuffer.wrap(byteV);
		buff.order(ByteOrder.BIG_ENDIAN);
		return buff.getInt();
	}

	public static int Byte1ToInt(byte Byte_L) {
		byte[] byteV = { 0, 0, 0, Byte_L };
		ByteBuffer buff = ByteBuffer.allocate(4);
		buff = ByteBuffer.wrap(byteV);
		buff.order(ByteOrder.BIG_ENDIAN);
		return buff.getInt();
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];

		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}

		return data;
	}

}
