import React, { createRef, PureComponent } from 'react';
import {
  requireNativeComponent,
  UIManager,
  Platform,
  ViewStyle,
  NativeModules,
  findNodeHandle,
  NativeMethods,
} from 'react-native';

const { LogmealSdkModule } = NativeModules;

const LINKING_ERROR =
  `The package 'react-native-logmeal-sdk' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const ComponentName = 'DepthCamera';

const NativeDepthCamera =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<LogmealSdkDepthCameraProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };

class DepthCamera extends PureComponent<LogmealSdkDepthCameraProps> {
  private readonly ref: React.RefObject<DepthCameraRefType>;

  constructor(props: LogmealSdkDepthCameraProps) {
    super(props);
    this.ref = createRef();
  }

  private get _handle(): number | null {
    const nodeHandle = findNodeHandle(this.ref.current);

    if (nodeHandle == null || nodeHandle === -1) {
      throw new Error(
        "Could not get the Camera's native view tag! Does the Camera View exist in the native view-tree?"
      );
    }

    return nodeHandle;
  }

  public async takePhoto(): Promise<TakePhotoResponse> {
    const result = await LogmealSdkModule.takePhoto(this._handle);

    return {
      focalLength: {
        x: result.focalLengthX,
        y: result.focalLengthY,
      },
      principalPoint: {
        x: result.principalPointX,
        y: result.principalPointY,
      },
      depthImageSize: {
        width: result.depthImageWidth,
        height: result.depthImageHeight,
      },
      depthImageUri: result.depthImageUri,
      imageUri: result.imageUri,
    };
  }

  public render(): React.ReactNode {
    // @ts-ignore
    return <NativeDepthCamera {...this.props} ref={this.ref} />;
  }
}

/**
 * Module methods
 */
const { checkDeviceSupport } = LogmealSdkModule;

export { checkDeviceSupport, DepthCamera };

export interface LogmealSdkDepthCameraProps {
  style: ViewStyle;
}
export type DepthCameraRefType = PureComponent<LogmealSdkDepthCameraProps> &
  Readonly<NativeMethods> & Readonly<{takePhoto: () => Promise<TakePhotoResponse>}>;

export interface TakePhotoResponse {
  focalLength: {
    x: number;
    y: number;
  };
  principalPoint: {
    x: number;
    y: number;
  };
  depthImageSize: {
    width: number;
    height: number;
  };
  depthImageUri: string;
  imageUri: string;
}
