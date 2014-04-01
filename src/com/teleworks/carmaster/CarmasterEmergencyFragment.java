package com.teleworks.carmaster;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.teleworks.carmaster.CarmasterUtisFragment.UDP_V2V;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.Fragment;

public class CarmasterEmergencyFragment extends Fragment {
	public static final String ARG_OBJECT = "object";
	private Button mButton;
	private Result gcmResult; // GCM Result(단일 전송)
	private Message gcmMessage; // GCM Message
	private Sender gcmSender; // GCM Sender
	// 개발자 콘솔에서 발급받은 API Key
	private static String API_KEY = "AIzaSyCKm5x-70QSdmg6yM1njsRq2Ef0gUXQI8w";
	// 메세지의 고유 ID(?)정도로 생각하면 됩니다. 메세지의 중복수신을 막기 위해 랜덤값을 지정합니다
	private static String COLLAPSE_KEY = String
			.valueOf(Math.random() % 100 + 1);
	// 기기가 활성화 상태일 때 보여줄 것인지.
	private static boolean DELAY_WHILE_IDLE = true;
	// 기기가 비활성화 상태일 때 GCM Storage에 보관되는 시간
	private static int TIME_TO_LIVE = 3;
	// 메세지 전송 실패시 재시도할 횟수
	private static int RETRY = 3;

	// teleworks
	// private String registrationId =
	// "APA91bGj2FpmthOi6q-B_ZL2nEGRe80jIsAHEv9s1VI39gRnTSb1wkodTjBEwWRC6G2zK8eb3zp-M3hiB_2BUpV0Hho7rVVLkyA6uN9Zsv6NsAM8Txnx13bapT_U8B6EPXqaF7ZO1Y7QBY8HiZl5X8UP3l4OBFSuZg";

	// lg g pro
	// private String registrationId =
	// "APA91bHUQ55h8DwDM5DgImjEkVjkWaz10KM7FG-nFU62jIHjgiGoeTLiRoZ2H-mZBtx4jdpiLJzeANGLey41E57yOW2BBufBLlxhJGXiz8zzwdYgm3Kd9di8h51RwGqPWxjfD0kWFZ69iBh_xQdPqqy3QyNmOrq1TA";

	// lg g pro
	private String registrationId = "APA91bHU4mYu_A3nFeW-vLTd9IwIAskzRVGx9MWj9VMLsr5BMclM_q8mHFw1HO0X3Vi8NqejwmBmYQkuPqpPTCgl9-6HcujFmC_4vq7Tcumnsocjl50WOmFaYO1CgVl61BYboJBQ_NAkjoHDR_sm4ZPpVGLLy6APqg";

	// UTIS

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

	private EditText mEdit_number, mEdit_location;
	private CheckBox mCheckbox[] = new CheckBox[9];

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_emergency,
				container, false);

		// mEdit = (EditText) rootView.findViewById(R.id.edit_member_name);
		// mEdit.requestFocus();
		// mEdit = (EditText)
		// rootView.findViewById(R.id.edit_accident_location);
		// mEdit.requestFocus();

		mCheckbox[0] = (CheckBox) rootView.findViewById(R.id.chk_1);
		mCheckbox[1] = (CheckBox) rootView.findViewById(R.id.chk_2);
		mCheckbox[2] = (CheckBox) rootView.findViewById(R.id.chk_3);
		mCheckbox[3] = (CheckBox) rootView.findViewById(R.id.chk_4);
		mCheckbox[4] = (CheckBox) rootView.findViewById(R.id.chk_5);
		mCheckbox[5] = (CheckBox) rootView.findViewById(R.id.chk_6);
		mCheckbox[6] = (CheckBox) rootView.findViewById(R.id.chk_7);
		mCheckbox[7] = (CheckBox) rootView.findViewById(R.id.chk_8);
		mCheckbox[8] = (CheckBox) rootView.findViewById(R.id.chk_9);
		mEdit_number = (EditText) rootView
				.findViewById(R.id.edit_member_mobile);
		mEdit_location = (EditText) rootView
				.findViewById(R.id.edit_accident_location);

		mButton = (Button) rootView.findViewById(R.id.btn_emg_location);
		mButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

			}
		});
		mButton = (Button) rootView.findViewById(R.id.btn_emg_ok_send);
		mButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// new Thread(new GsmSendThread()).start();

				String s_type = "";

				byte type_act[] = { 0, 0 };
				// check box
				if (mCheckbox[0].isChecked()) {
					type_act[1] += (byte) 0x04;
					s_type += "차량사고 ";
				}
				if (mCheckbox[1].isChecked()) {
					type_act[1] += (byte) 0x08;
					s_type += "기상관련 ";
				}
				if (mCheckbox[2].isChecked()) {
					type_act[1] += (byte) 0x10;
					s_type += "기후/고장 ";
				}
				if (mCheckbox[3].isChecked()) {
					type_act[1] += (byte) 0x20;
					s_type += "차량화재 ";
				}
				if (mCheckbox[4].isChecked()) {
					type_act[1] += (byte) 0x40;
					s_type += "장애물 ";
				}
				if (mCheckbox[5].isChecked()) {
					type_act[1] += (byte) 0x80;
					s_type += "위험물 ";
				}
				if (mCheckbox[6].isChecked()) {
					type_act[0] += (byte) 0x01;
					s_type += "지진 ";
				}
				if (mCheckbox[7].isChecked()) {
					type_act[0] += (byte) 0x02;
					s_type += "산사태 ";
				}
				if (mCheckbox[8].isChecked()) {
					type_act[0] += (byte) 0x04;
					s_type += "홍수 ";
				}

				if ((byte) 0x00 == type_act[0] && (byte) 0x00 == type_act[1]) {
					Toast.makeText(getActivity(),
							String.format("응급 상황 사고 유형을 선택하십시오."),
							Toast.LENGTH_SHORT).show();
					return;
				}

				String s_type_act = CarmasterUtisUtill.byteToHexString_noSpace(
						type_act, 2);

				String s_obe_id = CarmasterUtisUtill.byteToHexString_noSpace(
						CarmasterUtisFragment.obe_id, 8);

				new Thread(new UDP_V2V(32, CarmasterUtisFragment.serverIp,
						"0101", "2002", "00C2", new String[] {
								"0048" + s_obe_id, "01480100640080000002",
								"0A4400120000", "06443B9AD108",
								"07487B109D4B56D03B16", "0C42" + s_type_act,
								"F75074657374206465736372697074696F6E" }))
						.start();

				new Thread(new GsmSendThread("돌발상황발생 [" + s_type + "]",
						"돌발상황발생 [" + s_type + "]", "차량번호 : "
								+ mEdit_number.getText().toString()
								+ ", 사고위치 : "
								+ mEdit_location.getText().toString())).start();

				Toast.makeText(
						getActivity(),
						String.format("응급 상황 코드 %02X%02X 전송되었습니다.",
								type_act[0], type_act[1]), Toast.LENGTH_SHORT)
						.show();
			}
		});
		return rootView;
	}

	class GsmSendThread extends Thread {
		String ticker = "";
		String title = "";
		String msg = "";

		GsmSendThread(String s_ticker, String s_title, String s_msg) {
			ticker = s_ticker;
			title = s_title;
			msg = s_msg;
		}

		public void run() {
			setMessage(ticker, title, msg);
			sendMessage();
		}
	}

	public void setMessage(String s_ticker, String s_title, String s_msg) {
		gcmSender = new Sender(API_KEY);
		gcmMessage = new Message.Builder().collapseKey(COLLAPSE_KEY)
				.delayWhileIdle(DELAY_WHILE_IDLE).timeToLive(TIME_TO_LIVE)
				.addData("ticker", s_ticker).addData("title", s_title)
				.addData("msg", s_msg).build();
	}

	public void sendMessage() {
		// 일괄전송시에 사용
		// 단일전송시에 사용
		try {
			gcmResult = gcmSender.send(gcmMessage, registrationId, RETRY);
		} catch (IOException e) {
			Log.w("sendMessage", "IOException " + e.getMessage());
		}
		Log.w("sendMessage",
				"getCanonicalIds : " + gcmResult.getCanonicalRegistrationId()
						+ "\n" + "getMessageId : " + gcmResult.getMessageId());
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

		UDP_V2V(int headLen, String serverIP, String CODE, String OPCODE,
				String FROM_TO, String[] DATA) {
			mHeader = new byte[headLen];
			byte[] bytes;

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
				byte[] crc32 = CarmasterUtisFragment.genCrc32(mPayLoad,
						mPayLoad.length);
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
			crcbuff = CarmasterUtisFragment.genCrc16(mbuf, mHeader.length); /* hdr_crc16 */
			mbuf[HDR_CRC16_2] = crcbuff[0];
			mbuf[HDR_CRC16_2 + 1] = crcbuff[1];

		}

		public void run() {
			DatagramSocket socket_udp;
			DatagramPacket packet_udp;
			// TODO Auto-generated method stub
			try {

				socket_udp = new DatagramSocket();
				packet_udp = new DatagramPacket(mbuf, mbuf.length,
						InetAddress.getByName(mServerIP), 20000);

				/* Send out the packet */
				packet_udp.setData(mbuf);
				socket_udp.send(packet_udp);
				Log.d("UDP", "C: Sent.");

				// change receive buff size
				// mbuf = new byte[1024];
				// packet_udp.setData(mbuf);
				// socket_udp.receive(packet_udp);
				// byte[] temp = new byte[packet_udp.getLength()];
				//
				// System.arraycopy(packet_udp.getData(), 0, temp, 0,
				// packet_udp.getLength());

			} catch (Exception e) {
				Log.e("UDP", "C: Error", e);
			}
		}
	}
}