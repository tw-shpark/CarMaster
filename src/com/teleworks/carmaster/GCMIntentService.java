package com.teleworks.carmaster;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;

@SuppressLint("HandlerLeak")
public class GCMIntentService extends GCMBaseIntentService {
	private static final String TAG = "GCM";
	// private static final String INSERT_PAGE = "";
	private static final String SENDER_ID = "1065006683277";

	public GCMIntentService() {
		super(SENDER_ID);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
			showMessage(context, intent);
		}
	}

	@Override
	protected void onError(Context context, String msg) {
		// TODO Auto-generated method stub
		Log.w(TAG, "onError!! " + msg);
	}

	@Override
	protected void onRegistered(Context context, String regID) {
		// TODO Auto-generated method stub
		if (!regID.equals("") || regID != null) {
			Log.w(TAG, "onRegistered!! " + regID);
			// 단일전송일때 주석처리
			// insertRegistrationID(regID);
		}
	}

	@Override
	protected void onUnregistered(Context context, String regID) {
		// TODO Auto-generated method stub
		Log.w(TAG, "onUnregistered!! " + regID);
	}

	public void showToast() {
		Toast.makeText(this, "RegID 등록 완료", Toast.LENGTH_LONG).show();
	}

	@SuppressWarnings("deprecation")
	private void showMessage(Context context, Intent intent) {
		String title = intent.getStringExtra("title");
		String msg = intent.getStringExtra("msg");
		String ticker = intent.getStringExtra("ticker");

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Activity.NOTIFICATION_SERVICE);

		// 해당 어플을 실행하는 이벤트를 하고싶을 때 아래 주석을 풀어주세요
		// PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
		// new Intent(context, 어플이 처음 시작되는
		// 클래스명.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				new Intent(), 0);
		// PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
		// new Intent(context, GCMSendMessage.class)
		// .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = ticker;
		notification.when = System.currentTimeMillis();
		notification.setLatestEventInfo(context, title, msg, pendingIntent);
		notificationManager.notify(0, notification);

		handlerDlog.obtainMessage(1, -1, -1, msg).sendToTarget();
	}

	private Handler handlerDlog = new Handler() {
		public void handleMessage(Message msg) {
			Intent intent = new Intent();
			intent.setAction("com.teleworks.carmaster");
			intent.putExtra("msg", (String) msg.obj);
			sendBroadcast(intent);
		}
	};

	// public void insertRegistrationID(String id) {
	// httpConnect = new GCMHttpConnect(INSERT_PAGE + "?regID=" + id,
	// httpRequest);
	// httpConnect.start();
	// }
}
