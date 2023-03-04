package com.reactnativefacedetection;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.reactnativefacedetection.FaceDetectionCommon.*;
import static com.reactnativefacedetection.FaceDetectionUtils.*;


@ReactModule(name = FaceDetectionModule.NAME)
public class FaceDetectionModule extends ReactContextBaseJavaModule {
    public static final String NAME = "FaceDetection";

    public FaceDetectionModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void processImage(String filePath, ReadableMap faceDetectorOptions, Promise promise) {
        FaceDetectorOptions options = getFaceDetectorOptions(Arguments.toBundle(faceDetectorOptions));

        InputImage image = null;
        try {
            image = InputImage.fromFilePath(
                this.getReactApplicationContext(),
                getUri(filePath)
            );
        } catch (IOException e) {
            e.printStackTrace();

            String code = "io-exception";
            String message = e.getMessage();

            WritableMap userInfoMap = Arguments.createMap();
            userInfoMap.putString("code", code);
            userInfoMap.putString("message", message);

            promise.reject(code, message, userInfoMap);
            return;
        }

        FaceDetector detector = FaceDetection.getClient(options);

        Task<List<Face>> result =detector.process(image);
        result.addOnSuccessListener(
            new OnSuccessListener<List<Face>>() {
                @Override
                public void onSuccess(@NonNull List<Face> faces) {
                    List<Map<String, Object>> facesFormatted = new ArrayList<>(faces.size());

                    for (Face face : faces) {
                        Map<String, Object> faceFormatted = new HashMap<>();

                        faceFormatted.put(KEY_BOUNDING_BOX,
                          FaceDetectionUtils.rectToIntArray(face.getBoundingBox())
                        );

                        faceFormatted.put(KEY_HEAD_EULER_ANGLE_X, face.getHeadEulerAngleX());
                        faceFormatted.put(KEY_HEAD_EULER_ANGLE_Y, face.getHeadEulerAngleY());
                        faceFormatted.put(KEY_HEAD_EULER_ANGLE_Z, face.getHeadEulerAngleZ());
                        faceFormatted.put(KEY_LEFT_EYE_OPEN_PROBABILITY, face.getLeftEyeOpenProbability());
                        faceFormatted.put(KEY_RIGHT_EYE_OPEN_PROBABILITY, face.getRightEyeOpenProbability());

                        faceFormatted.put(KEY_SMILING_PROBABILITY, face.getSmilingProbability());
                        faceFormatted.put(KEY_TRACKING_ID, face.getTrackingId());

                        List<Map<String, Object>> faceContoursFormatted;

                        int classificationMode = (int) faceDetectorOptions.getDouble(KEY_CLASSIFICATION_MODE);
                        // Get flag for the contour mode
                        int contourMode = (int) faceDetectorOptions.getDouble(KEY_CONTOUR_MODE);

                        if (contourMode == FaceDetectorOptions.CONTOUR_MODE_NONE) {
                            faceContoursFormatted = new ArrayList<>(0);
                        } else {
                            faceContoursFormatted = new ArrayList<>(14);
                            faceContoursFormatted.add(getContourMap(face.getContour(FaceContour.FACE)));
                            faceContoursFormatted.add(getContourMap(face.getContour(FaceContour.LEFT_EYEBROW_TOP)));
                            faceContoursFormatted.add(getContourMap(face.getContour(FaceContour.LEFT_EYEBROW_BOTTOM)));
                            faceContoursFormatted.add(getContourMap(face.getContour(FaceContour.RIGHT_EYEBROW_TOP)));
                            faceContoursFormatted.add(getContourMap(face.getContour(FaceContour.RIGHT_EYEBROW_BOTTOM)));
                            faceContoursFormatted.add(getContourMap(face.getContour(FaceContour.LEFT_EYE)));
                            faceContoursFormatted.add(getContourMap(face.getContour(FaceContour.RIGHT_EYE)));
                            faceContoursFormatted.add(getContourMap(face.getContour(FaceContour.UPPER_LIP_TOP)));
                            faceContoursFormatted.add(getContourMap(face.getContour(FaceContour.UPPER_LIP_BOTTOM)));
                            faceContoursFormatted.add(getContourMap(face.getContour(FaceContour.LOWER_LIP_TOP)));
                            faceContoursFormatted.add(getContourMap(face.getContour(FaceContour.LOWER_LIP_BOTTOM)));
                            faceContoursFormatted.add(getContourMap(face.getContour(FaceContour.NOSE_BRIDGE)));
                            faceContoursFormatted.add(getContourMap(face.getContour(FaceContour.NOSE_BOTTOM)));
                        }

                        faceFormatted.put(KEY_FACE_CONTOURS, faceContoursFormatted);

                        List<Map<String, Object>> faceLandmarksFormatted;

                        int landmarkMode = (int) faceDetectorOptions.getDouble(KEY_LANDMARK_MODE);

                        if (landmarkMode == FaceDetectorOptions.LANDMARK_MODE_NONE) {
                            faceLandmarksFormatted = new ArrayList<>(0);
                        } else {
                            faceLandmarksFormatted = new ArrayList<>(14);

                            if (face.getLandmark(FaceLandmark.MOUTH_BOTTOM) != null) {
                                faceLandmarksFormatted.add(getLandmarkMap(
                                  face.getLandmark(FaceLandmark.MOUTH_BOTTOM)
                                ));
                            }

                            if (face.getLandmark(FaceLandmark.MOUTH_RIGHT) != null) {
                                faceLandmarksFormatted.add(getLandmarkMap(
                                  face.getLandmark(FaceLandmark.MOUTH_RIGHT)
                                ));
                            }

                            if (face.getLandmark(FaceLandmark.MOUTH_LEFT) != null) {
                                faceLandmarksFormatted.add(getLandmarkMap(
                                  face.getLandmark(FaceLandmark.MOUTH_LEFT)
                                ));
                            }

                            if (face.getLandmark(FaceLandmark.RIGHT_EYE) != null) {
                                faceLandmarksFormatted.add(getLandmarkMap(
                                  face.getLandmark(FaceLandmark.RIGHT_EYE)
                                ));
                            }

                            if (face.getLandmark(FaceLandmark.LEFT_EYE) != null) {
                                faceLandmarksFormatted.add(getLandmarkMap(
                                  face.getLandmark(FaceLandmark.LEFT_EYE)
                                ));
                            }

                            if (face.getLandmark(FaceLandmark.RIGHT_EAR) != null) {
                                faceLandmarksFormatted.add(getLandmarkMap(
                                  face.getLandmark(FaceLandmark.RIGHT_EAR)
                                ));
                            }

                            if (face.getLandmark(FaceLandmark.LEFT_EAR) != null) {
                                faceLandmarksFormatted.add(getLandmarkMap(
                                  face.getLandmark(FaceLandmark.LEFT_EAR)
                                ));
                            }

                            if (face.getLandmark(FaceLandmark.RIGHT_CHEEK) != null) {
                                faceLandmarksFormatted.add(getLandmarkMap(
                                  face.getLandmark(FaceLandmark.RIGHT_CHEEK)
                                ));
                            }

                            if (face.getLandmark(FaceLandmark.LEFT_CHEEK) != null) {
                                faceLandmarksFormatted.add(getLandmarkMap(
                                  face.getLandmark(FaceLandmark.LEFT_CHEEK)
                                ));
                            }

                            if (face.getLandmark(FaceLandmark.NOSE_BASE) != null) {
                                faceLandmarksFormatted.add(getLandmarkMap(
                                  face.getLandmark(FaceLandmark.NOSE_BASE)
                                ));
                            }
                        }

                        faceFormatted.put(KEY_LANDMARKS, faceLandmarksFormatted);
                        facesFormatted.add(faceFormatted);
                    }

                    promise.resolve(
                      Arguments.makeNativeArray(facesFormatted)
                    );
                }
            }
        );
        result.addOnFailureListener(
            new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String[] errorCodeAndMessage = FaceDetectionCommon.getErrorCodeAndMessageFromException(e);

                    String code = errorCodeAndMessage[0];
                    String message = errorCodeAndMessage[1];
                    String nativeErrorMessage = errorCodeAndMessage[2];

                    WritableMap userInfoMap = Arguments.createMap();
                    userInfoMap.putString("code", code);
                    userInfoMap.putString("message", message);
                    userInfoMap.putString("nativeErrorMessage", nativeErrorMessage);

                    promise.reject(code, message, userInfoMap);
                }
            }
        );
    }
}
