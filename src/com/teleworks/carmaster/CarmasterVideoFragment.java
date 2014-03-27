package com.teleworks.carmaster;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;
import android.support.v4.app.Fragment;

public class CarmasterVideoFragment extends Fragment implements CvCameraViewListener2, OnCheckedChangeListener {
	public static final String ARG_OBJECT = "object";
	protected static final String TAG = "VidioFragment";
	
	private CameraBridgeViewBase mOpenCvCameraView;
	private Mat mRgba;
	private Mat mGray;
	private long selectAlgorithm;

	private final static long ALGORITHM_NONE = 0;
	private final static long ALGORITHM_LDWS = 1;
	private final static long ALGORITHM_TOPV = 2;
	
	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(getActivity()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("TopView");

                    mOpenCvCameraView.enableView();
                    Toast.makeText(getActivity(), "OpenCV Manager Connected.", Toast.LENGTH_SHORT).show();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
	
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, getActivity(), mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView;
		rootView = inflater.inflate(R.layout.fragment_videoinfo, container, false);
		
		mOpenCvCameraView = (CameraBridgeViewBase) rootView.findViewById(R.id.OpenCvCameraView);
		mOpenCvCameraView.setCvCameraViewListener(this);
		
		RadioGroup algorithmGroup;
		algorithmGroup = (RadioGroup) rootView.findViewById(R.id.algoritmSel);
		algorithmGroup.setOnCheckedChangeListener(this);
		
		selectAlgorithm = ALGORITHM_NONE;
		
		return rootView;
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mGray = new Mat(height, width, CvType.CV_8UC1);
	}

	@Override
	public void onCameraViewStopped() {
		mRgba.release();
		mGray.release();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();
		VideoProcessing(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(), selectAlgorithm);
		return mRgba;
	}
	
	public native void VideoProcessing(long matAddrGr, long matAddrRgba, long flags);

	@Override
	public void onCheckedChanged(RadioGroup arg0, int arg1) {
		if(arg0.getCheckedRadioButtonId() == R.id.algo_none) {
			selectAlgorithm = ALGORITHM_NONE;
		} else if(arg0.getCheckedRadioButtonId() == R.id.algo_ldws) {
			selectAlgorithm = ALGORITHM_LDWS;
		} else if (arg0.getCheckedRadioButtonId() == R. id.algo_topview) {
			selectAlgorithm = ALGORITHM_TOPV;
		}
		
	}
}