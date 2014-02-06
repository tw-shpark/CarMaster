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

public class CarmasterUtisFragment extends Fragment implements
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

		mMapView.setMapCenterPointAndZoomLevel(
				MapPoint.mapPointWithGeoCoord(37.537229, 127.005515), 9, true);
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

		MapPolyline polyline2 = new MapPolyline(21);
		polyline2.setTag(2000);
		polyline2.setLineColor(Color.argb(128, 0, 0, 255));
		polyline2
				.addPoint(MapPoint.mapPointWithGeoCoord(37.537229, 127.005515));
		polyline2
				.addPoint(MapPoint.mapPointWithGeoCoord(37.537249, 127.005545));
		polyline2
				.addPoint(MapPoint.mapPointWithGeoCoord(37.537269, 127.005575));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(475334.0,1101210.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(474300.0,1104123.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(474300.0,1104123.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(473873.0,1105377.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(473302.0,1107097.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(473126.0,1109606.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(473063.0,1110548.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(473435.0,1111020.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(474068.0,1111714.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(475475.0,1112765.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(476938.0,1113532.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(478725.0,1114391.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(479453.0,1114785.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(480145.0,1115145.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(481280.0,1115237.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(481777.0,1115164.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(482322.0,1115923.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(482832.0,1116322.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(483384.0,1116754.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(484401.0,1117547.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(484893.0,1117930.0));
		// polyline2.addPoint(MapPoint.mapPointWithWCONGCoord(485016.0,1118034.0));
		mMapView.addPolyline(polyline2);

		mMapView.fitMapViewAreaToShowAllPolylines();
		// TRAFFIC REPORT END

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
