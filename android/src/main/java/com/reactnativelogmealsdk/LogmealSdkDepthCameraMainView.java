package com.reactnativelogmealsdk;

import android.app.Activity;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.facebook.react.uimanager.ThemedReactContext;
import com.google.ar.core.exceptions.NotYetAvailableException;

import es.logmeal.sdk.FrameDepthInfo;

public class LogmealSdkDepthCameraMainView extends LinearLayout {

  LogmealSdkDepthCameraView depthCameraView;
  Activity mActivity;
  int mOriginalOrientation;
  String viewMode = "portrait";

  public LogmealSdkDepthCameraMainView(ThemedReactContext context, Activity activity)
  {
    super(context);
    //  mOriginalOrientation = activity.getRequestedOrientation();
    mActivity = activity;
    this.setOrientation(LinearLayout.VERTICAL);
    // add the buttons and signature views
    this.setBackgroundColor(Color.TRANSPARENT);
    this.depthCameraView = new LogmealSdkDepthCameraView(context);
    this.addView(depthCameraView);
    setLayoutParams(new android.view.ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT));
  }

  public void setViewMode(String viewMode) {
    this.viewMode = viewMode;
    // GestureDetectingView.valuesTesting = 20;
    if (viewMode.equalsIgnoreCase("portrait")) {
      //  mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    } else if (viewMode.equalsIgnoreCase("landscape")) {
      // mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
  }

  public FrameDepthInfo takePhoto() throws InterruptedException, NotYetAvailableException {
      depthCameraView.captureNextFrame();
      return depthCameraView.getFrameDepthInfo();
  }
}
