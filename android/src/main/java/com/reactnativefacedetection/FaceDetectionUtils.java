package com.reactnativefacedetection;

import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.reactnativefacedetection.FaceDetectionCommon.*;

public class FaceDetectionUtils {
    /**
     * Create a Uri from the path, defaulting to file when there is no supplied scheme
     */
    public static Uri getUri(String uri) {
        Uri parsed = Uri.parse(uri);

        if (parsed.getScheme() == null || parsed.getScheme().isEmpty()) {
            return Uri.fromFile(new File(uri));
        }

        return parsed;
    }

    public static int[] rectToIntArray(@Nullable Rect rect) {
        if (rect == null || rect.isEmpty()) {
          return new int[]{};
        }

        return new int[]{
          rect.left,
          rect.top,
          rect.right,
          rect.bottom
        };
    }

    public static Map<String, Object> getLandmarkMap(FaceLandmark faceLandmark) {
        Map<String, Object> faceLandmarkMap = new HashMap<>();
        faceLandmarkMap.put(KEY_TYPE, faceLandmark.getLandmarkType());
        faceLandmarkMap.put(KEY_POSITION, getPointMap(faceLandmark.getPosition()));

        return faceLandmarkMap;
    }

    public static float[] getPointMap(PointF point) {
        return new float[]{point.x, point.y};
    }

    public static Map<String, Object> getContourMap(FaceContour faceContour) {
        Map<String, Object> faceContourMap = new HashMap<>();

        List<PointF> pointsListRaw = faceContour.getPoints();
        List<float[]> pointsListFormatted = new ArrayList<>(pointsListRaw.size());

        for (PointF pointRaw : pointsListRaw) {
            pointsListFormatted.add(getPointMap(pointRaw));
        }

        faceContourMap.put(KEY_TYPE, faceContour.getFaceContourType());
        faceContourMap.put(KEY_POINTS, pointsListFormatted);

        return faceContourMap;
    }

    //
    public static FaceDetectorOptions getFaceDetectorOptions(Bundle faceDetectorOptionsBundle) {
        FaceDetectorOptions.Builder builder = new FaceDetectorOptions.Builder();

        if (faceDetectorOptionsBundle.getBoolean(KEY_ENABLE_TRACKING)) {
          builder.enableTracking();
        }

        if (faceDetectorOptionsBundle.containsKey(KEY_CLASSIFICATION_MODE)) {
            int classificationMode = (int) faceDetectorOptionsBundle.getDouble(KEY_CLASSIFICATION_MODE);
            switch (classificationMode) {
                case FaceDetectorOptions.CLASSIFICATION_MODE_NONE:
                    builder.setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE);
                    break;
              case FaceDetectorOptions.CLASSIFICATION_MODE_ALL:
                    builder.setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL);
                    break;
              default:
                    throw new IllegalArgumentException("Invalid 'classificationMode' Face Detector option, must be either 1 or 2.");
            }
        }

        if (faceDetectorOptionsBundle.containsKey(KEY_CONTOUR_MODE)) {
            int contourMode = (int) faceDetectorOptionsBundle.getDouble(KEY_CONTOUR_MODE);
            switch (contourMode) {
                case FaceDetectorOptions.CONTOUR_MODE_NONE:
                    builder.setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE);
                    break;
                case FaceDetectorOptions.CONTOUR_MODE_ALL:
                    builder.setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL);
                    break;
            default:
                throw new IllegalArgumentException("Invalid 'contourMode' Face Detector option, must be either 1 or 2.");
            }
        }

        if (faceDetectorOptionsBundle.containsKey(KEY_LANDMARK_MODE)) {
            int landmarkMode = (int) faceDetectorOptionsBundle.getDouble(KEY_LANDMARK_MODE);
            switch (landmarkMode) {
                case FaceDetectorOptions.LANDMARK_MODE_NONE:
                    builder.setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE);
                    break;
                case FaceDetectorOptions.LANDMARK_MODE_ALL:
                    builder.setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid 'landmarkMode' Face Detector option, must be either 1 or 2.");
            }
        }

        if (faceDetectorOptionsBundle.containsKey(KEY_MIN_FACE_SIZE)) {
            float minFaceSize = (float) faceDetectorOptionsBundle.getDouble(KEY_MIN_FACE_SIZE);
            builder.setMinFaceSize(minFaceSize);
        }

        if (faceDetectorOptionsBundle.containsKey(KEY_PERFORMANCE_MODE)) {
            int performanceMode = (int) faceDetectorOptionsBundle.getDouble(KEY_PERFORMANCE_MODE);
            switch (performanceMode) {
                case FaceDetectorOptions.PERFORMANCE_MODE_FAST:
                    builder.setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST);
                    break;
                case FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE:
                    builder.setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid 'performanceMode' Face Detector option, must be either 1 or 2.");
      }
        }

        return builder.build();
    }
}
