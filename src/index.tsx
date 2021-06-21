import { NativeModules } from 'react-native';

type FaceDetectionType = {
  multiply(a: number, b: number): Promise<number>;
};

const { FaceDetection } = NativeModules;

export default FaceDetection as FaceDetectionType;
