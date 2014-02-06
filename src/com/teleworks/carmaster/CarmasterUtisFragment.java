package com.teleworks.carmaster;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class CarmasterUtisFragment extends Fragment 
		implements MapView.OpenAPIKeyAuthenticationResultListener, 
			MapView.MapViewEventListener, MapView.CurrentLocationEventListener, 
			MapView.POIItemEventListener/*, MapReverseGeoCoder.ReverseGeoCodingResultListener*/ {
	public static final String ARG_OBJECT = "object";
	private static final String LOG_TAG = "UtisMapView";

	private RelativeLayout mRelativeLayout;
	private MapView mMapView;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView;
		Activity mActivity = getActivity();
		
		rootView = inflater.inflate(R.layout.fragment_utisinfo, container, false);
		
		mRelativeLayout = (RelativeLayout) rootView.findViewById(R.id.utisRelativeLayout);
		
		MapView.setMapTilePersistentCacheEnabled(true);
		
		mMapView = new MapView(mActivity);
		
		mMapView.setDaumMapApiKey("d4910fcb5bbb000957a0b7adf458dd51a2c2cff6");
		mMapView.setOpenAPIKeyAuthenticationResultListener(this);
        mMapView.setMapViewEventListener(this);
        mMapView.setCurrentLocationEventListener(this);
        mMapView.setPOIItemEventListener(this);
		mMapView.setCurrentLocationEventListener(this);
		mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);

		mMapView.setMapType(MapView.MapType.Standard);

		mRelativeLayout.addView(mMapView);
		
		return rootView;
	}


	@Override
	public void onCalloutBalloonOfPOIItemTouched(MapView arg0, MapPOIItem arg1) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onDraggablePOIItemMoved(MapView arg0, MapPOIItem arg1,
			MapPoint arg2) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onPOIItemSelected(MapView arg0, MapPOIItem arg1) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onCurrentLocationDeviceHeadingUpdate(MapView arg0, float arg1) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onCurrentLocationUpdate(MapView arg0, MapPoint arg1, float arg2) {
		//MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
		//Log.i(LOG_TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", 
		//mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
		
	}


	@Override
	public void onCurrentLocationUpdateCancelled(MapView arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onCurrentLocationUpdateFailed(MapView arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onMapViewCenterPointMoved(MapView arg0, MapPoint arg1) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onMapViewDoubleTapped(MapView arg0, MapPoint arg1) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onMapViewInitialized(MapView arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onMapViewLongPressed(MapView arg0, MapPoint arg1) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onMapViewSingleTapped(MapView arg0, MapPoint arg1) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onMapViewZoomLevelChanged(MapView arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onDaumMapOpenAPIKeyAuthenticationResult(MapView arg0, int arg1,
			String arg2) {
		// TODO Auto-generated method stub
		
	}
}
