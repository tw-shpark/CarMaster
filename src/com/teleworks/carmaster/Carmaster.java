package com.teleworks.carmaster;

import com.google.android.gcm.GCMRegistrar;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;
import android.content.pm.ActivityInfo;

public class Carmaster extends FragmentActivity {
	private static final String SENDER_ID = "1065006683277";

	@Override
	public void onResume() {
		super.onResume();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// ActivityManager am = (ActivityManager) getApplicationContext()
		// .getSystemService(Context.ACTIVITY_SERVICE);
		// am.killBackgroundProcesses(this.getPackageName());
		// GCMRegistrar.onDestroy(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		final ActionBar actionBar = getActionBar();
		final ViewPager mViewPager;
		CarmasterPagerAdapter mCarmasterPagerAdapter;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_carmaster);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		// GCM 등록여부=======================================
		String regId = GCMRegistrar.getRegistrationId(this);
		// 등록된 ID가 없으면 ID값을 얻어옵니다
		if (regId.equals("") || regId == null) {
			GCMRegistrar.register(this, SENDER_ID);
			regId = GCMRegistrar.getRegistrationId(this);
			Log.w("GCMRegistrar", "new regId : " + regId);
		} else {
			Log.w("GCMRegistrar", "Already Registered : " + regId);
		}

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.teleworks.carmaster");
		BroadcastReceiver receiver = new UtisBroadcastReceiver();
		registerReceiver(receiver, intentFilter);
		// ==================================================

		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mCarmasterPagerAdapter = new CarmasterPagerAdapter(
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mCarmasterPagerAdapter);
		mViewPager.setEnabled(false);
		mViewPager.setOffscreenPageLimit(5); // Keep all page alive
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						getActionBar().setSelectedNavigationItem(position);
					}
				});

		ActionBar.TabListener tabListener = new ActionBar.TabListener() {

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				// do nothing
			}

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				mViewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
				// do nothing
			}
		};
		actionBar.addTab(actionBar.newTab().setText("교통정보")
				.setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText("운행정보")
				.setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText("영상정보")
				.setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText("응급호출")
				.setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText("설정")
				.setTabListener(tabListener));
	}

	private class UtisBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Intent Intent = new Intent(Carmaster.this,
					CarmasterUtisMsgDialog.class);
			Intent.putExtra("gcm_msg", intent.getStringExtra("msg"));
			startActivity(Intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.carmaster, menu);
		return true;
	}
}
