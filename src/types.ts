export enum FaceDetectorClassificationMode {
  NONE = 1,
  ALL = 2,
}

export enum FaceDetectorContourMode {
  NONE = 1,
  ALL = 2,
}

export enum FaceDetectorLandmarkMode {
  NO = 1,
  ALL = 2,
}

export enum FaceDetectorPerformanceMode {
  FAST = 1,
  ACCURATE = 2,
}

export enum FaceContourType {
  FACE = 1,
  LEFT_EYEBROW_TOP = 2,
  LEFT_EYEBROW_BOTTOM = 3,
  RIGHT_EYEBROW_TOP = 4,
  RIGHT_EYEBROW_BOTTOM = 5,
  LEFT_EYE = 6,
  RIGHT_EYE = 7,
  UPPER_LIP_TOP = 8,
  UPPER_LIP_BOTTOM = 9,
  LOWER_LIP_TOP = 10,
  LOWER_LIP_BOTTOM = 11,
  NOSE_BRIDGE = 12,
  NOSE_BOTTOM = 13,
  LEFT_CHECK = 14,
  RIGHT_CHECK = 15,
}

export enum FaceLandmarkType {
  MOUTH_BOTTOM = 1,
  MOUTH_RIGHT = 2,
  MOUTH_LEFT = 3,
  LEFT_EAR = 4,
  RIGHT_EAR = 5,
  LEFT_EYE = 6,
  RIGHT_EYE = 7,
  LEFT_CHEEK = 8,
  RIGHT_CHEEK = 9,
  NOSE_BASE = 10,
}

export type FaceDetectorOptionsType = {
  classificationMode?:
    | FaceDetectorClassificationMode.NONE
    | FaceDetectorClassificationMode.ALL;

  contourMode?: FaceDetectorContourMode.NONE | FaceDetectorContourMode.ALL;

  landmarkMode?: FaceDetectorLandmarkMode.NO | FaceDetectorLandmarkMode.ALL;

  minFaceSize?: number;

  performanceMode?:
    | FaceDetectorPerformanceMode.FAST
    | FaceDetectorPerformanceMode.ACCURATE;
};

export type FaceResult = {
  boundingBox: [number, number, number, number];

  rightEyeOpenProbability: number;

  leftEyeOpenProbability: number;

  smilingProbability: number;

  trackingId: number;

  headEulerAngleX: number;

  headEulerAngleY: number;

  headEulerAngleZ: number;

  landmarks: [
    {
      type: number;

      position: number;
    }
  ];

  faceContours: [
    {
      type: number;

      points: [
        {
          x: number;
          y: number;
        }
      ];
    }
  ];
};
