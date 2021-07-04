import * as React from 'react';

import {
  StyleSheet,
  View,
  Button,
  Dimensions,
  ImageBackground,
} from 'react-native';
import {
  launchImageLibrary,
  ImagePickerResponse,
  Asset,
} from 'react-native-image-picker';
import FaceDetection, {
  FaceDetectorContourMode,
  FaceDetectorLandmarkMode,
} from 'react-native-face-detection';

type ImageSizeType = {
  width: number;
  height: number;
  originalWidth: number;
  originalHeight: number;
};

type BoundingBoxType = [number, number, number, number];

type FaceRectType = {
  x: number;
  y: number;
  width: number;
  height: number;
};

const { width: screenWidth } = Dimensions.get('window');

function calculateImageSize(
  originalWidth: number,
  originalHeight: number
): ImageSizeType {
  const ratio = originalWidth / originalHeight;

  const w = originalWidth > screenWidth ? screenWidth : originalWidth;
  const h = w / ratio;

  return {
    width: w,
    height: h,
    originalWidth,
    originalHeight,
  };
}

function calculateFaceRectInsideImage(
  boundingBox: BoundingBoxType,
  imageSize: ImageSizeType
): FaceRectType {
  const wRatio = imageSize.originalWidth / imageSize.width;
  const hRatio = imageSize.originalHeight / imageSize.height;

  const faceX = boundingBox[0] / wRatio;
  const faceY = boundingBox[1] / hRatio;

  const faceWidth = boundingBox[2] / wRatio - faceX;
  const faceHeight = boundingBox[3] / hRatio - faceY;

  return {
    x: faceX,
    y: faceY,
    width: Math.ceil(faceWidth),
    height: Math.ceil(faceHeight),
  };
}

export default function App() {
  const [imageObject, setImageObject] = React.useState<Asset | undefined>();
  const [imageSize, setImageSize] = React.useState<ImageSizeType | undefined>();
  const [faceRects, setFaceRects] = React.useState<
    FaceRectType[] | undefined
  >();
  const [isLoading, setIsLoading] = React.useState<boolean>(false);

  React.useEffect(() => {
    const processImage = async () => {
      if (imageObject && imageObject.uri) {
        const options = {
          landmarkMode: FaceDetectorLandmarkMode.ALL,
          contourMode: FaceDetectorContourMode.ALL,
        };

        const faces = await FaceDetection.processImage(
          imageObject.uri,
          options
        );

        console.log(faces);

        const imageSizeResult = calculateImageSize(
          imageObject.width as number,
          imageObject.height as number
        );

        const faceRectResults: FaceRectType[] = [];

        faces.forEach((face) => {
          const faceRect = calculateFaceRectInsideImage(
            face.boundingBox,
            imageSizeResult
          );

          faceRectResults.push(faceRect);
        });

        setImageSize(imageSizeResult);
        setFaceRects(faceRectResults);

        setIsLoading(false);
      }
    };

    processImage();
  }, [imageObject]);

  return (
    <View style={styles.container}>
      {!isLoading && imageObject && imageSize?.width && imageSize.height && (
        <ImageBackground
          source={{ uri: imageObject.uri }}
          style={[
            {
              width: imageSize.width,
              height: imageSize.height,
            },
          ]}
        >
          {faceRects &&
            faceRects.map((rect: FaceRectType, index: number) => (
              <View
                key={`${index}`}
                style={[
                  styles.box,
                  {
                    width: rect.width,
                    height: rect.height,
                    left: rect.x,
                    top: rect.y,
                  },
                ]}
              />
            ))}
        </ImageBackground>
      )}
      <View style={styles.buttonContainer}>
        <Button
          title="Select Image"
          onPress={async () => {
            launchImageLibrary(
              {
                mediaType: 'photo',
              },
              (response: ImagePickerResponse) => {
                if (response.didCancel) {
                  return;
                }

                setFaceRects(undefined);

                setIsLoading(true);

                setImageObject(response.assets[0]);
              }
            );
          }}
          disabled={isLoading}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    borderWidth: 1,
    borderColor: 'green',
  },
  buttonContainer: {
    marginTop: 8,
  },
});
