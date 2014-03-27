package com.teleworks.carmaster;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.http.util.EncodingUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

/**
 * 
 * @author gunsoo 함수 사용방법 - 한글을 hex로 변환 String subject =
 *         request.getParameter("TITLE"); if(subject != null){ subject =
 *         CarmasterUtisUtill.hexstr2Str(subject); }
 * 
 *         hex를 한글로 변환 String subject = CarmasterUtisUtill.str2Hexstr(subject);
 */

public class CarmasterUtisUtill {

	public static void main(String[] args) {
		new CarmasterUtisUtill();
	}

	public static String getString(byte[] input) {
		StringBuffer rtn = new StringBuffer();

		for (int i = 0; i < input.length;) {
			if ((input[i] & 0x80) == 0x80) {
				byte[] hangle = new byte[2];
				hangle[0] = input[i];
				hangle[1] = input[++i];
				rtn.append(new String(hangle));
			} else
				rtn.append((char) input[i]);
			++i;
		}
		return rtn.toString();
	}

	public static String toKor(String src) {
		String returnStr = "";
		byte[] b;
		try {
			b = src.getBytes("8859_1");
			returnStr = new String(b, "EUC-KR");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnStr;
	}

	public byte[] bitmapToByteArray(Bitmap $bitmap) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		$bitmap.compress(CompressFormat.JPEG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		return byteArray;
	}

	public Bitmap byteArrayToBitmap(byte[] $byteArray) {
		Bitmap bitmap = BitmapFactory.decodeByteArray($byteArray, 0,
				$byteArray.length);
		return bitmap;
	}

	// new SavePhotoTask().execute("1", "2", "3");
	static class SavePhotoTask extends Thread {
		byte[] bJpeg;
		private RandomAccessFile raf;

		SavePhotoTask(byte[] data_in) {
			bJpeg = new byte[data_in.length];
			System.arraycopy(data_in, 0, bJpeg, 0, data_in.length);
		}

		public void run() {
			try {
				String sdPath = Environment.getExternalStorageDirectory()
						.getAbsolutePath();
				String name = sdPath + "/tw_test.jpeg";
				raf = new RandomAccessFile(name, "rw");
				raf.seek(raf.length());
				raf.write(bJpeg);
			} catch (IOException e) {
				System.out.println("Error opening file: " + e);
			}
		}
	}

	static class playWav extends Thread {
		byte[] mp3SoundByteArray;
		Context myContext;

		playWav(Context thiscontext, byte[] data_in) {
			mp3SoundByteArray = data_in;
			myContext = thiscontext;
		}

		public void run() {
			try {
				// create temp file that will hold byte array
				File tempMp3 = File.createTempFile("kurchina", "mp3",
						myContext.getCacheDir());
				tempMp3.deleteOnExit();
				FileOutputStream fos = new FileOutputStream(tempMp3);
				fos.write(mp3SoundByteArray);
				fos.close();

				// Tried reusing instance of media player
				// but that resulted in system crashes...
				MediaPlayer mediaPlayer = new MediaPlayer();

				// Tried passing path directly, but kept getting
				// "Prepare failed.: status=0x1"
				// so using file descriptor instead
				FileInputStream fis = new FileInputStream(tempMp3);
				mediaPlayer.setDataSource(fis.getFD());

				mediaPlayer.prepare();
				mediaPlayer.start();
			} catch (IOException ex) {
				String s = ex.toString();
				ex.printStackTrace();
			}
		}
	}

	public static class TextToSpeecht implements OnInitListener {

		private static final String TAG = "TextToSpeechDemo";
		private TextToSpeech mTts;
		private String string;

		TextToSpeecht(Context thiscontext, String comment) {
			mTts = new TextToSpeech(thiscontext, this);
			string = comment;
		}

		public void Destroy() {
			// Don't forget to shutdown!
			if (mTts != null) {
				mTts.stop();
				mTts.shutdown();
			}
		}

		public void onInit(int status) {
			if (status == TextToSpeech.SUCCESS) {
				if (true == mTts.isSpeaking())
					return;
				// mTts.stop();

				int result = mTts.setLanguage(Locale.KOREA);
				if (result == TextToSpeech.LANG_MISSING_DATA
						|| result == TextToSpeech.LANG_NOT_SUPPORTED) {

					Log.e(TAG, "Language is not available.");
				} else {
					sayHello();
				}
			} else {
				// Initialization failed.
				Log.e(TAG, "Could not initialize TextToSpeech.");
			}
		}

		private void sayHello() {
			mTts.speak(string, TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	/**
	 * @author shell cmd on root"
	 */

	public static void doCmds(String cmds) {
		Process process;
		try {
			process = Runtime.getRuntime().exec("su");

			DataOutputStream os = new DataOutputStream(
					process.getOutputStream());

			os.writeBytes(cmds + "\n");

			os.writeBytes("exit\n");
			os.flush();
			os.close();

			process.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @author gunsoo
	 * 
	 *         use in fragment "context = container.getContext();"
	 */
	public static class usb_tethering implements Runnable {
		Context myContext;

		public usb_tethering(Context thiscontext) {
			this.myContext = thiscontext;
		}

		public void run() {
			ConnectivityManager cman = (ConnectivityManager) myContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			Method[] methods = cman.getClass().getDeclaredMethods();
			for (Method method : methods) {
				if (method.getName().equals("getTetherableIfaces")) {
					try {
						String[] ifaces = (String[]) method.invoke(cman);
						for (String iface : ifaces) {
							Log.d("TETHER", "Tether available on " + iface);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (method.getName().equals("isTetheringSupported")) {
					try {
						boolean supported = (Boolean) method.invoke(cman);
						Log.d("TETHER", "Tether is supported: "
								+ (supported ? "yes" : "no"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (method.getName().equals("tether")) {
					Log.d("TETHER", "Starting tether rndis0");
					try {
						int result = (Integer) method.invoke(cman, "rndis0");
						Log.d("TETHER", "Tether rndis0 result: " + result);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static class switchOnTethering implements Runnable {
		String TAG = "namh";
		Context myContext;
		ConnectivityManager cm;

		String[] available = null;
		int code = -1;

		public switchOnTethering(Context thiscontext) {
			cm = (ConnectivityManager) thiscontext
					.getSystemService(Context.CONNECTIVITY_SERVICE);
		}

		public void run() {
			/**
			 * {@ref: 
			 * http://alvinalexander.com/java/jwarehouse/android/services/java
			 * /com/android/server/ConnectivityService.java.shtml} {@ref:
			 * http://stackoverflow
			 * .com/questions/7509924/detect-usb-tethering-on-android} {@ref:
			 * http:// stackoverflow.com/questions/9913645/android-enable-usb-
			 * tethering -programmatically-there-is-an-app-that-did-it-fo}
			 * 
			 * 
			 */
			Method[] wmMethods = cm.getClass().getDeclaredMethods();
			for (Method method : wmMethods) {
				if (method.getName().equals("getTetherableIfaces")) {
					try {
						Log.d(TAG, "before getTetherableIfaces()");
						available = (String[]) method.invoke(cm);
						break;
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
				}// end of 'if'
			}// end of 'for'

			for (Method method : wmMethods) {
				if (method.getName().equals("tether")) {
					try {
						for (String s : available) {
							Log.d(TAG, "available = " + s);
						}
						// Toast.makeText(thiscontext.getApplicationContext(),
						// available[0], Toast.LENGTH_LONG).show();
						// available[0] might be String 'usb0'
						// code = (Integer) method.invoke(cm, available[0]);
						code = (Integer) method.invoke(cm, "rndis0");
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
					break;
				}// end of 'if'
			}

			if (code == 0)
				Log.d(TAG, "Enable usb tethering successfully!");
			else
				Log.d(TAG, "Enable usb tethering failed!");
		}
	}

	public static class SimpleTest {

		static String myApiKey = "자신의 API KEY로 바꾸세요.";
		static String regId = "테스트할 단말의 등록 ID로 바꾸세요.";

		/**
		 * @param args
		 */
		public static void main(String[] args) {
			Sender sender = new Sender(myApiKey);
			// Message message = new Message.Builder(regId).build();
			// Result result = sender.send(message, 5);

			String registrationId = regId;
			// Message message = new Message.Builder().build();
			Message message = new Message.Builder()
					.collapseKey("collapseKey" + System.currentTimeMillis())
					.timeToLive(3).delayWhileIdle(true)
					.addData("message", "안녕하시유~ " + System.currentTimeMillis())
					.build();

			Result result;
			try {
				result = sender.send(message, registrationId, 5);

				System.out.println("======= Send ======");

				if (result.getMessageId() != null) {
					String canonicalRegId = result.getCanonicalRegistrationId();
					System.out.println("canonicalRegId : " + canonicalRegId);
					if (canonicalRegId != null) {
						// same device has more than on registration ID: update
						// database
						System.out
								.println("same device has more than on registration ID: update database");
					}
				} else {
					String error = result.getErrorCodeName();
					System.out.println("[ERROR]" + error);
					if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
						// application has been removed from device - unregister
						// database
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("======= END ======");
		}

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

	public static int Byte2ToInt(byte Byte_H, byte Byte_L) {
		byte[] byteV = { 0, 0, Byte_H, Byte_L };
		ByteBuffer buff = ByteBuffer.allocate(4);
		buff = ByteBuffer.wrap(byteV);
		buff.order(ByteOrder.BIG_ENDIAN);
		return buff.getInt();
	}

	public static int Byte4ToInt(byte Byte_0, byte Byte_1, byte Byte_2,
			byte Byte_3) {
		byte[] byteV = { Byte_0, Byte_1, Byte_2, Byte_3 };
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

	public static String byteToHexString_noSpace(byte[] bytebuf, int len) {
		String result = "";
		for (int i = 0; i < len; i++) {
			result += String.format("%02X", bytebuf[i]);
		}
		return result;
	}
}
