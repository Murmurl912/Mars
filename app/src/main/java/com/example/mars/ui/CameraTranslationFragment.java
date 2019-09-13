package com.example.mars.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mars.R;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.Objects;


public class CameraTranslationFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2 {
    public static final String TAG = "CameraTranslationFragment";
    private CameraBridgeViewBase javaCameraView;
    private boolean isCameraTabSelected = false;
    private boolean isCameraPermissionGranted = true;
    private boolean isOpenCVLibraryLoad = false;
    private final int CAMERA_PERMISSION_REQUEST = 0;
    private int timesAttemptToLoadOpenCVLibrary = 0;
    private int timesAttemptToRequestCameraPermission = 0;
    private CameraBridgeViewBase.CvCameraViewFrame currentFrame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_camera_translation, container, false);
        javaCameraView = view.findViewById(R.id.main_camera_tab_java_camera_view);
        javaCameraView.setCvCameraViewListener(this);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "onCameraViewStarted: width = " + width + ", height = " + height);
    }

    @Override
    public void onCameraViewStopped() {
        Log.d(TAG, "onCameraViewStopped");

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d(TAG, "onCameraFrame: inputFrame = " + inputFrame);

        return inputFrame.rgba();
    }

    public void onTabSelectionChanged(boolean isCameraTabSelected) {
        Log.d(TAG, "onTabSelectionChanged is called\nisCameraTabSelected = " + isCameraTabSelected);
        this.isCameraTabSelected = isCameraTabSelected;
        if(isCameraTabSelected) {
            enableCameraPreview();
        } else {
            disableCameraPreview();
        }
    }

    private void enableCameraPreview() {
        Log.d(TAG, "enableCameraPreview is called\n");
        if(isOpenCVLibraryLoad) {
            Log.d(TAG, "OpenCV library is already loaded\n");
            checkCameraPermission();
            if(isCameraPermissionGranted) {
                Log.d(TAG, "Camera permission is already granted\n");
                javaCameraView.setCameraPermissionGranted();
                javaCameraView.enableView();
            } else {
                Log.d(TAG, "Camera permission is denied");
                new Thread(this::requestCameraPermission).start();
            }
        } else if(timesAttemptToLoadOpenCVLibrary < 4) {
            requestLoadOpenCVLibrary();
        }
    }

    private void disableCameraPreview() {
        javaCameraView.disableView();
    }

    private void pauseCameraPreview() {

    }

    private void resumeCameraPreview() {

    }

    private void requestCameraPermission() {
        Log.d(TAG, "requestCameraPermission is called");
    }

    private void checkCameraPermission() {
        Log.d(TAG, "checkCameraPermission is called");
        int permissionCheck = ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                Manifest.permission.CAMERA);
        isCameraPermissionGranted = PackageManager.PERMISSION_GRANTED == permissionCheck;
    }

    private void requestLoadOpenCVLibrary() {
        isOpenCVLibraryLoad = OpenCVLoader.initDebug();
        timesAttemptToLoadOpenCVLibrary = isOpenCVLibraryLoad ? 0 : timesAttemptToLoadOpenCVLibrary + 1;
        onLoadOpenCVLibraryResult(isOpenCVLibraryLoad);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isCameraTabSelected) {
            enableCameraPreview();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isCameraTabSelected) {
            disableCameraPreview();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if(isCameraTabSelected) {
            enableCameraPreview();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isCameraTabSelected) {
            disableCameraPreview();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void onLoadOpenCVLibraryResult(boolean result) {
        enableCameraPreview();
    }

}
