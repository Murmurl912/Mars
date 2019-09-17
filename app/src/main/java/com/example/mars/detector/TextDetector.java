package com.example.mars.detector;

import android.content.Context;
import android.util.Log;

import com.example.mars.R;

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
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TextDetector {

    private float scoreThresh = 0.5f;
    private float nmsThresh = 0.4f;
    private Size size = new Size(320, 320);
    private int W = (int)(size.width / 4); // width of the output geometry  / score maps
    private int H = (int)(size.height / 4);

    private Scalar mean = new Scalar(123.68, 116.78, 103.94);
    private List<Mat> outputBlobs = new ArrayList<>(2);
    private List<String> outputNames = new ArrayList<String>(2);

    List<Float> confidencesList = new ArrayList<>();
    List<RotatedRect> boxesList = new ArrayList<>();
    private Net net;

    private Context context;

    public TextDetector(Context context) {
        this.context = context;
        loadDnnModel("model.pb");
        outputNames.add("feature_fusion/Conv_7/Sigmoid");
        outputNames.add("feature_fusion/concat_3");
    }

    private void loadDnnModel(String name) {
        try {
            File files = context.getFilesDir();
            File model = new File(files, "model.pb");
            if(model.exists()) {
                net = Dnn.readNet(model.getCanonicalPath());
            } else {
                FileOutputStream out = context.openFileOutput("model.pb", Context.MODE_PRIVATE);
                InputStream in = context.getResources().openRawResource(R.raw.model);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(out);
                byte[] buffer = new byte[1024];
                while(bufferedInputStream.read(buffer) != -1) {
                    bufferedOutputStream.write(buffer);
                }
                bufferedOutputStream.flush();
                bufferedInputStream.close();
                bufferedOutputStream.close();
                out.close();
                in.close();
                Dnn.readNet(model.getCanonicalPath());
            }

        } catch (IOException e) {
            Log.d("IO", e.toString());
        }

    }

    public List<TextBox> detect(Mat frame) {
        Size frameSize = frame.size();
        List<TextBox> detections = new ArrayList<>();
        if(net.empty()) {
            return detections;
        }
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
        Mat blob = Dnn.blobFromImage(frame, 1.0, size, mean, true, false);

        outputBlobs.clear();
        net.setInput(blob);
        long a = System.currentTimeMillis();
        net.forward(outputBlobs, outputNames);
        long b = System.currentTimeMillis();
        Log.d("Networking Time", " = " + (b - a));

        Mat scores = outputBlobs.get(0).reshape(1, W);
        Mat geometry = outputBlobs.get(1).reshape(1, 5 * H);

        decode(scores, geometry, scoreThresh);

        if(confidencesList.isEmpty()) {
            scores.release();
            // frame.release();
            blob.release();
            geometry.release();

            return detections;
        }

        MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confidencesList));
        RotatedRect[] boxesArray = boxesList.toArray(new RotatedRect[0]);
        MatOfRotatedRect boxes = new MatOfRotatedRect(boxesArray);
        MatOfInt indices = new MatOfInt();
        Dnn.NMSBoxesRotated(boxes, confidences, scoreThresh, nmsThresh, indices);

        // Render detections
        Point ratio = new Point((float)frame.cols()/size.width, (float)frame.rows()/size.height);
        int[] indexes = indices.toArray();
        for (int index : indexes) {
            RotatedRect rot = boxesArray[index];
            Point[] vertices = new Point[4];
            rot.points(vertices);
            for (int j = 0; j < 4; ++j) {
                vertices[j].x *= ratio.x;
                vertices[j].y *= ratio.y;
            }
            Imgproc.line(frame, vertices[0], vertices[(0 + 1) % 4], new Scalar(0, 0,255), 1);
            Imgproc.line(frame, vertices[1], vertices[(1 + 1) % 4], new Scalar(0, 255, 0), 1);
            Imgproc.line(frame, vertices[2], vertices[(2 + 1) % 4], new Scalar(255, 0, 0), 1);
            Imgproc.line(frame, vertices[3], vertices[(3 + 1) % 4], new Scalar(255, 0,255), 1);

            detections.add(new TextBox(vertices, frameSize));
        }

        // frame.release();
        scores.release();
        geometry.release();
        confidences.release();
        boxes.release();
        indices.release();

        return detections;
    }

    private void decode(Mat scores, Mat geometry, float scoreThresh) {
        confidencesList.clear();
        boxesList.clear();

        // size of 1 geometry plane
        int W = geometry.cols();
        int H = geometry.rows() / 5;
        //System.out.println(geometry);
        //System.out.println(scores);

        for (int y = 0; y < H; ++y) {
            Mat scoresData = scores.row(y);
            Mat x0Data = geometry.submat(0, H, 0, W).row(y);
            Mat x1Data = geometry.submat(H, 2 * H, 0, W).row(y);
            Mat x2Data = geometry.submat(2 * H, 3 * H, 0, W).row(y);
            Mat x3Data = geometry.submat(3 * H, 4 * H, 0, W).row(y);
            Mat anglesData = geometry.submat(4 * H, 5 * H, 0, W).row(y);

            for (int x = 0; x < W; ++x) {
                double score = scoresData.get(0, x)[0];
                if (score >= scoreThresh) {
                    double offsetX = x * 4.0;
                    double offsetY = y * 4.0;
                    double angle = anglesData.get(0, x)[0];
                    double cosA = Math.cos(angle);
                    double sinA = Math.sin(angle);
                    double x0 = x0Data.get(0, x)[0];
                    double x1 = x1Data.get(0, x)[0];
                    double x2 = x2Data.get(0, x)[0];
                    double x3 = x3Data.get(0, x)[0];
                    double h = x0 + x2;
                    double w = x1 + x3;
                    Point offset = new Point(offsetX + cosA * x1 + sinA * x2, offsetY - sinA * x1 + cosA * x2);
                    Point p1 = new Point(-1 * sinA * h + offset.x, -1 * cosA * h + offset.y);
                    Point p3 = new Point(-1 * cosA * w + offset.x,      sinA * w + offset.y); // original trouble here !
                    RotatedRect r = new RotatedRect(new Point(0.5 * (p1.x + p3.x), 0.5 * (p1.y + p3.y)), new Size(w, h), -1 * angle * 180 / Math.PI);

                    boxesList.add(r);
                    confidencesList.add((float) score);
                }
            }

            scoresData.release();
            x0Data.release();
            x1Data.release();
            x2Data.release();
            x3Data.release();
            anglesData.release();
        }
    }

    public void onFrameArrive(Mat frame, OnDetectComplete callback) {
//        new Thread(()->{
//            callback.onDetectComplete(detect(frame));
//        }).start();
        callback.onDetectComplete(detect(frame));

    }

    public interface OnDetectComplete {
        void onDetectComplete(List<TextBox> boxes);
    }

}
