package com.teleworks.carmaster;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;

public class Carmaster extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	final ActionBar actionBar = getActionBar();
    	final ViewPager mViewPager;
    	CarmasterPagerAdapter mCarmasterPagerAdapter;
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carmaster);
        
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        mCarmasterPagerAdapter = new CarmasterPagerAdapter(getSupportFragmentManager());
        
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCarmasterPagerAdapter);
        mViewPager.setEnabled(false);
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
        actionBar.addTab(actionBar.newTab().setText("��������").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("��������").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("��������").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("����ȣ��").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("����").setTabListener(tabListener));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.carmaster, menu);
        return true;
    }
    
}