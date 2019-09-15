package com.example.mars.detector;

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

import java.util.ArrayList;
import java.util.List;

public class TextDetector {

    private float scoreThresh = 0.5f;
    private float nmsThresh = 0.4f;
    private Size size = new Size(320, 320);
    private int W = (int)(size.width / 4); // width of the output geometry  / score maps
    private int H = (int)(size.height / 4);
    private Scalar mean = new Scalar(123.68, 116.78, 103.94);
    private List<Mat> outs = new ArrayList<>(2);
    private List<String> outNames = new ArrayList<String>();
    List<Float> confidencesList = new ArrayList<>();
    List<RotatedRect> boxesList = new ArrayList<>();

    public TextDetector() {
        outNames.add("feature_fusion/Conv_7/Sigmoid");
        outNames.add("feature_fusion/concat_3");
    }

    private Net net;

    private void loadDnnModel(String name) {

    }

    public Mat processFrame(Mat frame, boolean isRGBA) {
        if(isRGBA) {
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
        }
        net.setInput(Dnn.blobFromImage(frame, 1.0,size, mean, true, false));
        outs.clear();
        net.forward(outs, outNames);

        Mat scores = outs.get(0).reshape(1, H);
        Mat geometry = outs.get(1).reshape(1, 5 * H);
        confidencesList.clear();
        boxesList.clear();
        decode(scores, geometry, scoreThresh);
        if(boxesList.isEmpty()) {
            return frame;
        }

        // Apply non-maximum suppression procedure.
        MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confidencesList));
        RotatedRect[] boxesArray = boxesList.toArray(new RotatedRect[0]);
        MatOfRotatedRect boxes = new MatOfRotatedRect(boxesArray);
        MatOfInt indices = new MatOfInt();
        Dnn.NMSBoxesRotated(boxes, confidences, scoreThresh, nmsThresh, indices);

        // Render detections
        Point ratio = new Point((float)frame.cols()/size.width, (float)frame.rows()/size.height);
        int[] indexes = indices.toArray();
        for(int i = 0; i<indexes.length;++i) {
            RotatedRect rot = boxesArray[indexes[i]];
            Point[] vertices = new Point[4];
            rot.points(vertices);
            for (int j = 0; j < 4; ++j) {
                vertices[j].x *= ratio.x;
                vertices[j].y *= ratio.y;
            }
            for (int j = 0; j < 4; ++j) {
                Imgproc.line(frame, vertices[j], vertices[(j + 1) % 4], new Scalar(0, 0,255), 1);
            }
        }
        return frame;
    }

    private void decode(Mat scores, Mat geometry, float scoreThresh) {
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
        }
    }
}
