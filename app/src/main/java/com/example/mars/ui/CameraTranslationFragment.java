package com.example.mars.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.mars.R;
import com.example.mars.detector.TextDetector;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRotatedRect;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;


public class CameraTranslationFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2 {
    public static final String TAG = "CameraTranslationFragment";
    private JavaCamera2View javaCameraView;
    private boolean isCameraTabSelected = false;
    private boolean isCameraPermissionGranted = true;
    private boolean isOpenCVLibraryLoad = false;
    private final int CAMERA_PERMISSION_REQUEST = 0;
    private int timesAttemptToLoadOpenCVLibrary = 0;
    private int timesAttemptToRequestCameraPermission = 0;
    private CameraBridgeViewBase.CvCameraViewFrame currentFrame;

    private long openCameraFrameDaley = 400;
    private long closeCameraFrameDaley = 200;

    private Timer timer;
    private boolean isFreezingFrame = false;
    private boolean isFrameFrozen = false;
    private Mat frozenFrame;

    private boolean isOpenCameraFrameTaskScheduled = false;
    private boolean isCloseCameraFrameTaskScheduled = false;

    private TimerTask openFrameStreamTask;

    private TimerTask closeFrameStreamTask;

    private ImageButton pauseStream;
    private ImageButton flash;
    private ImageButton file;
    private ImageButton capture;

    private TextDetector detector;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_camera_translation, container, false);
        javaCameraView = view.findViewById(R.id.main_camera_tab_java_camera_view);
        javaCameraView.setCvCameraViewListener(this);
        pauseStream = view.findViewById(R.id.main_camera_tab_stop_or_resume);
        flash = view.findViewById(R.id.main_camera_tab_flash);
        file = view.findViewById(R.id.main_camera_tab_load_image_from_file);
        capture = view.findViewById(R.id.main_camera_tab_save_image);
        pauseStream.setOnClickListener((v)->{
            isFreezingFrame = !isFreezingFrame;
            if(isFreezingFrame) {
                pauseCameraPreview();
                pauseStream.setImageResource(R.drawable.ic_play_light);
            } else {
                resumeCameraPreview();
                pauseStream.setImageResource(R.drawable.ic_pause_light);
            }
        });


        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        timer = new Timer();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "onCameraViewStarted: width = " + width + ", height = " + height);
        detector = new TextDetector();
    }

    @Override
    public void onCameraViewStopped() {
        Log.d(TAG, "onCameraViewStopped");

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d(TAG, "onCameraFrame: inputFrame = " + inputFrame);
        Mat frame = inputFrame.rgba();
        if(isFreezingFrame) {
            if(isFrameFrozen) {
                frame.release();
                frame =  frozenFrame;

            } else {
                frozenFrame = frame.clone();
                frame.release();
                frame = frozenFrame;
                isFrameFrozen = true;
            }
        }

        return frame;
    }


    public void onTabSelectionChanged(boolean isCameraTabSelected) {
        Log.d(TAG, "onTabSelectionChanged is called\nisCameraTabSelected = " + isCameraTabSelected);
        this.isCameraTabSelected = isCameraTabSelected;
        if(isCameraTabSelected) {
            new Thread(this::enableCameraPreview).start();
        } else {
            new Thread(this::disableCameraPreview).start();
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
                if(isCloseCameraFrameTaskScheduled) {
                    if(closeFrameStreamTask != null) {
                        closeFrameStreamTask.cancel();
                    }
                    isCloseCameraFrameTaskScheduled = false;
                }
                openFrameStreamTask = new TimerTask() {
                    @Override
                    public void run() {
                        getActivity().runOnUiThread(()->{
                            javaCameraView.enableView();
                            isOpenCameraFrameTaskScheduled = false;
                        });
                    };
                };
                timer.schedule(openFrameStreamTask, openCameraFrameDaley);
                isOpenCameraFrameTaskScheduled = true;
            } else {
                Log.d(TAG, "Camera permission is denied");
                new Thread(this::requestCameraPermission).start();
            }
        } else if(timesAttemptToLoadOpenCVLibrary < 4) {
            new Thread(this::requestLoadOpenCVLibrary).start();
        }
    }

    private void disableCameraPreview() {
        if(isOpenCameraFrameTaskScheduled) {
            if(openFrameStreamTask != null) {
                openFrameStreamTask.cancel();
            }
            isCloseCameraFrameTaskScheduled = false;
        }
        closeFrameStreamTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(()->{
                    javaCameraView.disableView();
                    isCloseCameraFrameTaskScheduled = false;
                });
            }
        };
        timer.schedule(closeFrameStreamTask, closeCameraFrameDaley);
        isCloseCameraFrameTaskScheduled = true;
    }

    private void pauseCameraPreview() {
        isFreezingFrame = true;
        isFrameFrozen = false;
    }

    private void resumeCameraPreview() {
        isFreezingFrame = false;
        if(frozenFrame != null) {
            frozenFrame.release();
        }
    }

    private void requestCameraPermission() {
        Log.d(TAG, "requestCameraPermission is called");
        if(timesAttemptToRequestCameraPermission < 3) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }

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

        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST: {
                switch (grantResults[0]) {
                    case PackageManager.PERMISSION_GRANTED: {
                        isCameraPermissionGranted = true;
                        timesAttemptToRequestCameraPermission = 0;
                    } break;

                    default: {
                        isCameraPermissionGranted = false;
                        ++timesAttemptToRequestCameraPermission;
                    }
                }
            }break;

            default: {

            }
        }
    }

    private void onLoadOpenCVLibraryResult(boolean result) {
        enableCameraPreview();
    }


}
