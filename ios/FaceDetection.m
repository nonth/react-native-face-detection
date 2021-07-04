#import "FaceDetection.h"
#import <React/RCTConvert.h>
#import <MLKitVision/MLKitVision.h>
#import <MLKitFaceDetection/MLKitFaceDetection.h>

@implementation FaceDetection

static NSString *const RNFDErrorDomain = @"RNFDErrorDomain";

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

RCT_EXPORT_MODULE()

RCT_REMAP_METHOD(processImage,
                  processImageWithFilePath:(NSString *)filePath
                  faceDetectorOptions:(NSDictionary *)faceDetectorOptions
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)
{
    [self UIImageForFilePath:filePath completion:^(NSArray *errorCodeMessageArray, UIImage *image) {
        if (errorCodeMessageArray != nil) {
            [self rejectPromiseWithUserInfo:reject userInfo:(NSMutableDictionary *)@{
                @"code": errorCodeMessageArray[0],
                @"message": errorCodeMessageArray[1],
            }];
            return;
        }
        
        MLKVisionImage *visionImage = [[MLKVisionImage alloc] initWithImage:image];
        visionImage.orientation = image.imageOrientation;
        
        MLKFaceDetectorOptions *options = [[MLKFaceDetectorOptions alloc] init];
        
        NSInteger *classificationMode = [faceDetectorOptions[@"classificationMode"] pointerValue];
        if (classificationMode == (NSInteger *) 1) {
          options.classificationMode = MLKFaceDetectorClassificationModeNone;
        } else if (classificationMode == (NSInteger *) 2) {
          options.classificationMode = MLKFaceDetectorClassificationModeAll;
        }

        NSInteger *contourMode = [faceDetectorOptions[@"contourMode"] pointerValue];
        if (contourMode == (NSInteger *) 1) {
          options.contourMode = MLKFaceDetectorContourModeNone;
        } else if (contourMode == (NSInteger *) 2) {
          options.contourMode = MLKFaceDetectorContourModeAll;
        }

        NSInteger *landmarkMode = [faceDetectorOptions[@"landmarkMode"] pointerValue];
        if (landmarkMode == (NSInteger *) 1) {
          options.landmarkMode = MLKFaceDetectorLandmarkModeNone;
        } else if (landmarkMode == (NSInteger *) 2) {
          options.landmarkMode = MLKFaceDetectorLandmarkModeAll;
        }

        NSInteger *performanceMode = [faceDetectorOptions[@"performanceMode"] pointerValue];
        if (performanceMode == (NSInteger *) 1) {
          options.performanceMode = MLKFaceDetectorPerformanceModeFast;
        } else if (performanceMode == (NSInteger *) 2) {
          options.performanceMode = MLKFaceDetectorPerformanceModeAccurate;
        }

        options.minFaceSize = (CGFloat) [faceDetectorOptions[@"minFaceSize"] doubleValue];
        
        MLKFaceDetector *faceDetector = [MLKFaceDetector faceDetectorWithOptions:options];
        
        [faceDetector processImage:visionImage completion:^(NSArray<MLKFace *> * _Nullable faces, NSError * _Nullable error) {
            if (error != nil) {
                [self rejectPromiseWithUserInfo:reject userInfo:(NSMutableDictionary *)@{
                    @"code": @"unknown",
                    @"message": [error localizedDescription]
                }];
                return;
            }
            
            NSMutableArray *facesFormatted = [[NSMutableArray alloc] init];
            
            for (MLKFace *face in faces) {
                NSMutableDictionary *visionFace = [[NSMutableDictionary alloc] init];
                
                visionFace[@"boundingBox"] = [self rectToIntArray:face.frame];

                visionFace[@"headEulerAngleX"] = face.hasHeadEulerAngleX ? @(face.headEulerAngleX) : @(-1);
                visionFace[@"headEulerAngleY"] = face.hasHeadEulerAngleY ? @(face.headEulerAngleY) : @(-1);
                visionFace[@"headEulerAngleZ"] = face.hasHeadEulerAngleZ ? @(face.headEulerAngleZ) : @(-1);
                visionFace[@"leftEyeOpenProbability"] = face.hasLeftEyeOpenProbability ? @(face.leftEyeOpenProbability) : @(-1);
                visionFace[@"rightEyeOpenProbability"] = face.hasRightEyeOpenProbability ? @(face.rightEyeOpenProbability) : @(-1);
                visionFace[@"smilingProbability"] = face.hasSmilingProbability ? @(face.smilingProbability) : @(-1);
                visionFace[@"trackingId"] = face.hasTrackingID ? @(face.trackingID) : @(-1);

                
                // Contours
                NSMutableArray *faceContours = [[NSMutableArray alloc] init];
                if (contourMode == (NSInteger *) 2) {
                    [faceContours addObject:[self contourToDict:[face contourOfType:MLKFaceContourTypeFace]]];
                    [faceContours addObject:[self contourToDict:[face contourOfType:MLKFaceContourTypeLeftEyebrowTop]]];
                    [faceContours addObject:[self contourToDict:[face contourOfType:MLKFaceContourTypeLeftEyebrowBottom]]];
                    [faceContours addObject:[self contourToDict:[face contourOfType:MLKFaceContourTypeRightEyebrowTop]]];
                    [faceContours addObject:[self contourToDict:[face contourOfType:MLKFaceContourTypeRightEyebrowBottom]]];
                    [faceContours addObject:[self contourToDict:[face contourOfType:MLKFaceContourTypeLeftEye]]];
                    [faceContours addObject:[self contourToDict:[face contourOfType:MLKFaceContourTypeRightEye]]];
                    [faceContours addObject:[self contourToDict:[face contourOfType:MLKFaceContourTypeUpperLipTop]]];
                    [faceContours addObject:[self contourToDict:[face contourOfType:MLKFaceContourTypeUpperLipBottom]]];
                    [faceContours addObject:[self contourToDict:[face contourOfType:MLKFaceContourTypeLowerLipTop]]];
                    [faceContours addObject:[self contourToDict:[face contourOfType:MLKFaceContourTypeLowerLipBottom]]];
                    [faceContours addObject:[self contourToDict:[face contourOfType:MLKFaceContourTypeNoseBridge]]];
                    [faceContours addObject:[self contourToDict:[face contourOfType:MLKFaceContourTypeNoseBottom]]];
                    [faceContours addObject:[self contourToDict:[face contourOfType:MLKFaceContourTypeLeftCheek]]];
                    [faceContours addObject:[self contourToDict:[face contourOfType:MLKFaceContourTypeRightCheek]]];
                }
                visionFace[@"faceContours"] = faceContours;
                
                // Face Landmarks
                NSMutableArray *faceLandmarks = [[NSMutableArray alloc] init];
                if (landmarkMode == (NSInteger *) 2) {
                    [faceLandmarks addObject:[self landmarkToDict:[face landmarkOfType:MLKFaceLandmarkTypeMouthBottom]]];
                    [faceLandmarks addObject:[self landmarkToDict:[face landmarkOfType:MLKFaceLandmarkTypeMouthRight]]];
                    [faceLandmarks addObject:[self landmarkToDict:[face landmarkOfType:MLKFaceLandmarkTypeMouthLeft]]];
                    [faceLandmarks addObject:[self landmarkToDict:[face landmarkOfType:MLKFaceLandmarkTypeLeftEar]]];
                    [faceLandmarks addObject:[self landmarkToDict:[face landmarkOfType:MLKFaceLandmarkTypeRightEar]]];
                    [faceLandmarks addObject:[self landmarkToDict:[face landmarkOfType:MLKFaceLandmarkTypeLeftEye]]];
                    [faceLandmarks addObject:[self landmarkToDict:[face landmarkOfType:MLKFaceLandmarkTypeRightEye]]];
                    [faceLandmarks addObject:[self landmarkToDict:[face landmarkOfType:MLKFaceLandmarkTypeLeftCheek]]];
                    [faceLandmarks addObject:[self landmarkToDict:[face landmarkOfType:MLKFaceLandmarkTypeRightCheek]]];
                    [faceLandmarks addObject:[self landmarkToDict:[face landmarkOfType:MLKFaceLandmarkTypeNoseBase]]];
                }
                visionFace[@"landmarks"] = faceLandmarks;
                
                [facesFormatted addObject:visionFace];
            }
            
            resolve(facesFormatted);
        }];

    }];
}

#pragma mark -

- (void)UIImageForFilePath:(NSString *)filePath completion:(void (^)(NSArray *errorCodeMessageArray, UIImage *image))completion
{
    NSURL *url = [NSURL URLWithString:filePath];
    
    if (url == nil) {
        completion(@[@"file-not-found", @"The local file specified does not exist on the device."], nil);
        return;
    }
    
    BOOL isExists = [[NSFileManager defaultManager] fileExistsAtPath:[url path]];
        
    if (!isExists) {
        completion(@[@"file-not-found", @"The local file specified does not exist on the device."], nil);
    } else {
        dispatch_async(dispatch_get_main_queue(), ^{
            UIImage *image = [RCTConvert UIImage:filePath];
            completion(nil, image);
        });
    }
}

- (NSArray *)rectToIntArray:(CGRect)rect {
    CGSize size = rect.size;
    CGPoint point = rect.origin;
    
    return @[
        @(point.x),
        @(point.y),
        @(point.x + size.width),
        @(point.y + size.height)
    ];
}

- (NSDictionary *)contourToDict:(MLKFaceContour *)visionFaceContour {
    NSMutableDictionary *visionFaceContourDict = [[NSMutableDictionary alloc] init];

    if (visionFaceContour == nil) {
        return visionFaceContourDict;
    }

    NSMutableArray *pointsFormatted = [[NSMutableArray alloc] init];
    for (MLKVisionPoint *point in visionFaceContour.points) {
        [pointsFormatted addObject:[self arrayForMLKVisionPoint:point]];
    }

    visionFaceContourDict[@"type"] = [self contourTypeToInt:visionFaceContour.type];
    visionFaceContourDict[@"points"] = pointsFormatted;

    return visionFaceContourDict;
}

- (NSNumber *)contourTypeToInt:(NSString *)faceContourType {
    if ([@"Face" isEqualToString:faceContourType]) {
      return @(1);
    }
    if ([@"LeftEyebrowTop" isEqualToString:faceContourType]) {
      return @(2);
    }
    if ([@"LeftEyebrowBottom" isEqualToString:faceContourType]) {
      return @(3);
    }
    if ([@"RightEyebrowTop" isEqualToString:faceContourType]) {
      return @(4);
    }
    if ([@"RightEyebrowBottom" isEqualToString:faceContourType]) {
      return @(5);
    }
    if ([@"LeftEye" isEqualToString:faceContourType]) {
      return @(6);
    }
    if ([@"RightEye" isEqualToString:faceContourType]) {
      return @(7);
    }
    if ([@"UpperLipTop" isEqualToString:faceContourType]) {
      return @(8);
    }
    if ([@"UpperLipBottom" isEqualToString:faceContourType]) {
      return @(9);
    }
    if ([@"LowerLipTop" isEqualToString:faceContourType]) {
      return @(10);
    }
    if ([@"LowerLipBottom" isEqualToString:faceContourType]) {
      return @(11);
    }
    if ([@"NoseBridge" isEqualToString:faceContourType]) {
      return @(12);
    }
    if ([@"NoseBottom" isEqualToString:faceContourType]) {
      return @(13);
    }
    if ([@"LeftCheek" isEqualToString:faceContourType]) {
      return @(14);
    }
    if ([@"RightCheek" isEqualToString:faceContourType]) {
      return @(15);
    }
    return @(-1);
}

- (NSDictionary *)landmarkToDict:(MLKFaceLandmark *)visionFaceLandmark {
    NSMutableDictionary *visionFaceLandmarkDict = [[NSMutableDictionary alloc] init];

    if (visionFaceLandmark == nil) {
        return visionFaceLandmarkDict;
    }

    visionFaceLandmarkDict[@"type"] = [self landmarkTypeToInt:visionFaceLandmark.type];
    visionFaceLandmarkDict[@"position"] = [self arrayForMLKVisionPoint:visionFaceLandmark.position];
    
    return visionFaceLandmarkDict;
}

- (NSNumber *)landmarkTypeToInt:(NSString *)faceLandmarkType {
    if ([@"MouthBottom" isEqualToString:faceLandmarkType]) {
      return @(1);
    }
    if ([@"MouthRight" isEqualToString:faceLandmarkType]) {
      return @(2);
    }
    if ([@"MouthLeft" isEqualToString:faceLandmarkType]) {
      return @(3);
    }
    if ([@"LeftEar" isEqualToString:faceLandmarkType]) {
      return @(4);
    }
    if ([@"RightEar" isEqualToString:faceLandmarkType]) {
      return @(5);
    }
    if ([@"LeftEye" isEqualToString:faceLandmarkType]) {
      return @(6);
    }
    if ([@"RightEye" isEqualToString:faceLandmarkType]) {
      return @(7);
    }
    if ([@"LeftCheek" isEqualToString:faceLandmarkType]) {
      return @(8);
    }
    if ([@"RightCheek" isEqualToString:faceLandmarkType]) {
      return @(9);
    }
    if ([@"NoseBase" isEqualToString:faceLandmarkType]) {
      return @(10);
    }
    return @(-1);
}

- (NSArray *)arrayForMLKVisionPoint:(MLKVisionPoint *)point {
    return @[@(point.x), @(point.y)];
}

- (void)rejectPromiseWithUserInfo:(RCTPromiseRejectBlock)reject userInfo:(NSMutableDictionary *)userInfo {
    NSError *error = [NSError errorWithDomain:RNFDErrorDomain code:666 userInfo:userInfo];
    reject(userInfo[@"code"], userInfo[@"message"], error);
}

@end
