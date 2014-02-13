package com.teleworks.carmaster;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CarmasterPagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter {
	public static final int UTIS_INFO	= 0;
	public static final int CAR_INFO	= 1;
	public static final int VIDEO_INFO	= 2;
	public static final int EMERGENCY	= 3;
	public static final int SETTINGS	= 4;
	
	public CarmasterPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int arg0) {
		Fragment fragment;
		
		switch(arg0){
		case UTIS_INFO : 
			fragment = new CarmasterUtisFragment();
			break;
		case CAR_INFO :
			fragment = new CarmasterCarFragment();
			break;
		case VIDEO_INFO :
			fragment = new CarmasterVideoFragment();
			break;
		case EMERGENCY :
			fragment = new CarmasterEmergencyFragment();
			break;
		case SETTINGS :
			fragment = new CarmasterSettingFragment();
			break;
		default :
			fragment = new CarmasterFragment();
			Log.e("CarMaster", "Unexpected Error : Cannot found fragment corresponding to argument(" + arg0 + ")");
			break;
		}
		
		Bundle args = new Bundle();
		
		args.putInt(CarmasterFragment.ARG_OBJECT,  arg0 + 1);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public int getCount() {
		return 5;
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		return "OBJECT " + (position + 1);
	}

	public static class CarmasterFragment extends Fragment {
		public static final String ARG_OBJECT = "object";

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView;
			
			Bundle args = getArguments();
			rootView = inflater.inflate(R.layout.fragment_textonly, container, false);
			((TextView) rootView.findViewById(android.R.id.text1))
					.setText(Integer.toString(args.getInt(ARG_OBJECT)));

			return rootView;
		}
	}

}