package com.teleworks.carmaster;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class CarmasterUtisMapFragment extends Fragment implements
		MapView.OpenAPIKeyAuthenticationResultListener,
		MapView.MapViewEventListener, MapView.CurrentLocationEventListener,
		MapView.POIItemEventListener/*
									 * , MapReverseGeoCoder.
									 * ReverseGeoCodingResultListener
									 */{
	public static final String ARG_OBJECT = "object";
	private static final String LOG_TAG = "UtisMapView";

	private RelativeLayout mRelativeLayout;
	private MapView mMapView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView;
		Activity mActivity = getActivity();

		rootView = inflater.inflate(R.layout.fragment_utisinfo, container,
				false);

		mRelativeLayout = (RelativeLayout) rootView
				.findViewById(R.id.utisRelativeLayout);

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

		// TRAFFIC REPORT START
		mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(
				36.3641707229209, 127.33106224909565), true);
		// mMapView.setZoomLevel(4, true);
		// mMapView.zoomIn(true);
		// mMapView.zoomIn(true);
		// mMapView.zoomIn(true);

		MapPOIItem existingPOIItemStart = mMapView.findPOIItemByTag(10001);
		if (existingPOIItemStart != null) {
			mMapView.removePOIItem(existingPOIItemStart);
		}

		MapPOIItem existingPOIItemEnd = mMapView.findPOIItemByTag(10002);
		if (existingPOIItemEnd != null) {
			mMapView.removePOIItem(existingPOIItemEnd);
		}

		MapPolyline existingPolyline = mMapView.findPolylineByTag(2000);
		if (existingPolyline != null) {
			mMapView.removePolyline(existingPolyline);
		}

		traffic_road(mMapView, Color.argb(128, 255, 0, 0),
				CarmasterUtisGPSMap.national_highway_251);

		return rootView;
	}

	private void traffic_road(MapView mapView, int lineColor, double[] location) {

		if (4 > location.length && (0 != (location.length / 2)))
			return;

		MapPolyline polyline = new MapPolyline(location.length / 2);
		polyline.setTag(2000);

		for (int i = 0; i < location.length - 1; i += 2) {
			polyline.addPoint(MapPoint.mapPointWithGeoCoord(location[i],
					location[i + 1]));
		}

		mapView.addPolyline(polyline);
		mapView.fitMapViewAreaToShowAllPolylines();
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
		// MapPoint.GeoCoordinate mapPointGeo =
		// currentLocation.getMapPointGeoCoord();
		// Log.i(LOG_TAG,
		// String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)",
		// mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));

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
