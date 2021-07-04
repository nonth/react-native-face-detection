# react-native-face-detection

react-native face detection using [Google MLKit](https://developers.google.com/ml-kit).

Some part of the code is from react-native-firebase ml-vision package ([version 6.4.0](https://github.com/invertase/react-native-firebase/tree/v6.4.0/packages/ml-vision)) but due to Google separate Firebase ML Kit into [Google ML Kit](https://developers.google.com/ml-kit) and [Firebase Machine Learning](https://firebase.google.com/docs/ml) then the ml-vision will no longer available on react-native-firebase.

This package bring back face detection feature from ml-vision package.

## Installation

You can use npm or yarn to install the latest version:

**npm**
```sh
npm install --save react-native-face-detection
```

**yarn**
```sh
yarn add react-native-face-detection
```

### iOS

Run `npx pod-install` after installing the package

## Usage

```js
import FaceDetection, { FaceDetectorContourMode, FaceDetectorLandmarkMode, FaceContourType } from "react-native-face-detection";

async function processFaces(imagePath) {
  const options = {
    landmarkMode: FaceDetectorLandmarkMode.ALL,
    contourMode: FaceDetectorContourMode.ALL
  };

  const faces = await FaceDetection.processImage(imagePath, options);

  faces.forEach(face => {
    console.log('Head rotation on X axis: ', face.headEulerAngleX);
    console.log('Head rotation on Y axis: ', face.headEulerAngleY);
    console.log('Head rotation on Z axis: ', face.headEulerAngleZ);

    console.log('Left eye open probability: ', face.leftEyeOpenProbability);
    console.log('Right eye open probability: ', face.rightEyeOpenProbability);
    console.log('Smiling probability: ', face.smilingProbability);

    face.faceContours.forEach(contour => {
      if (contour.type === FaceContourType.FACE) {
        console.log('Face outline points: ', contour.points);
      }
    });

    face.landmarks.forEach(landmark => {
      if (landmark.type === FaceLandmarkType.LEFT_EYE) {
        console.log('Left eye outline points: ', landmark.points);
      } else if (landmark.type === FaceLandmarkType.RIGHT_EYE) {
        console.log('Right eye outline points: ', landmark.points);
      }
    });
  });
}

// Local path to file on the device
const imagePath = '/path/to/face-image.png';

processFaces(imagePath).then(() => console.log('Finished processing file.'));
```

## Example

Check `example` foler.

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
