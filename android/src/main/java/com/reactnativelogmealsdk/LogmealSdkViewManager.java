package com.reactnativelogmealsdk;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

public class LogmealSdkViewManager extends ViewGroupManager<LogmealSdkDepthCameraMainView> {
  public static final String REACT_CLASS = "DepthCamera";

  private LogmealSdkRNModule mContextModule;


  public LogmealSdkViewManager(ReactApplicationContext reactContext) {
    mContextModule = new LogmealSdkRNModule(reactContext);
  }

  @Override
  @NonNull
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  protected LogmealSdkDepthCameraMainView createViewInstance(ThemedReactContext reactContext) {
    return new LogmealSdkDepthCameraMainView(reactContext, mContextModule.getActivity());
  }
}
