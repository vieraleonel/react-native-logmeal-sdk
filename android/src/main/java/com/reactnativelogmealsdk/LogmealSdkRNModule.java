package com.reactnativelogmealsdk;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.google.ar.core.exceptions.NotYetAvailableException;

import es.logmeal.sdk.FrameDepthInfo;
import es.logmeal.sdk.helpers.ArCoreSupport;
import es.logmeal.sdk.helpers.ArCoreUtils;

public class LogmealSdkRNModule extends ReactContextBaseJavaModule {
  public static final String REACT_CLASS = "LogmealSdkModule";
  private final ReactApplicationContext reactContext;

  public LogmealSdkRNModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {

    }
  };

  @ReactMethod
  void checkDeviceSupport(Promise promise) {

    ArCoreSupport arCoreSupport = ArCoreUtils.isDeviceSupported((AppCompatActivity) getActivity());

    if (arCoreSupport.isArCoreSupported()) {
      if (arCoreSupport.isDepthSupported()) {
        promise.resolve(true);
      } else {
        promise.resolve(false);
      }
    } else {
      promise.resolve(false);
    }
  }

  private LogmealSdkDepthCameraMainView findDepthCameraMainView(int id) {
    return getReactApplicationContext().getCurrentActivity().findViewById(id);
  }

  @ReactMethod
  void takePhoto(int viewTag, Promise promise) {
    LogmealSdkDepthCameraMainView mainView = findDepthCameraMainView(viewTag);
    try {
      FrameDepthInfo fdi = mainView.takePhoto();

      WritableMap responseMap = Arguments.createMap();
      responseMap.putDouble("focalLengthX", fdi.getCameraInfo().getFocalLength()[0]);
      responseMap.putDouble("focalLengthY", fdi.getCameraInfo().getFocalLength()[1]);

      responseMap.putDouble("principalPointX", fdi.getCameraInfo().getPrincipalPoint()[0]);
      responseMap.putDouble("principalPointY", fdi.getCameraInfo().getPrincipalPoint()[1]);

      responseMap.putDouble("depthImageWidth", fdi.getDepthData().getWidth());
      responseMap.putDouble("depthImageHeight", fdi.getDepthData().getHeight());
      responseMap.putString("depthImageUri", fdi.getDepthData().getDepthFile().getAbsolutePath());

      responseMap.putString("imageUri", fdi.getImageData().getImageRGBFile().getAbsolutePath());

      promise.resolve(responseMap);

    } catch (InterruptedException | NotYetAvailableException e) {
      e.printStackTrace();
      promise.reject(e);
    }
  }

  @Override
  @NonNull
  public String getName() {
    return REACT_CLASS;
  }

  public Activity getActivity() {
    return this.getCurrentActivity();
  }
}
