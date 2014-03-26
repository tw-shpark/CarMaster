package com.teleworks.carmaster;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.WindowManager;

public class Carmaster extends FragmentActivity {

	@Override
	public void onResume(){
		super.onResume();
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	final ActionBar actionBar = getActionBar();
    	final ViewPager mViewPager;
    	CarmasterPagerAdapter mCarmasterPagerAdapter;
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carmaster);
        
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE );
        
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        mCarmasterPagerAdapter = new CarmasterPagerAdapter(getSupportFragmentManager());
        
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCarmasterPagerAdapter);
        mViewPager.setEnabled(false);
        mViewPager.setOffscreenPageLimit(5); // Keep all page alive
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
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
        actionBar.addTab(actionBar.newTab().setText("교통정보").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("운행정보").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("영상정보").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("응급호출").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("설정").setTabListener(tabListener));
        
        //actionBar.set
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.carmaster, menu);
        return true;
    }
    
}
