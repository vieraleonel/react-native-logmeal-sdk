
package com.reactnativelogmealsdk;


import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;

import com.facebook.react.uimanager.ThemedReactContext;
import com.google.ar.core.Config;

import com.facebook.react.bridge.LifecycleEventListener;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;

import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import es.logmeal.sdk.FrameDepthInfo;
import es.logmeal.sdk.FrameProcessor;
import es.logmeal.sdk.helpers.DepthTextureHandler;
import es.logmeal.sdk.rendering.BackgroundRenderer;

public class LogmealSdkDepthCameraView extends GLSurfaceView implements GLSurfaceView.Renderer, LifecycleEventListener {
  private static final String TAG = "RN_LOGMEAL_SDK_DEPTH_CAMERA_VIEW";

  private ThemedReactContext mContextModule;
  private Session mSession;
  private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
  private final DepthTextureHandler depthTexture = new DepthTextureHandler();

  private boolean viewportChanged;
  private int viewportWidth;
  private int viewportHeight;

  private Frame frame;
  private FrameDepthInfo frameDepthInfo;

  private boolean captureNextFrame;
  private String customDepthInfoStorePath;

  // ------------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------------

  // ------------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------------

  @Override
  protected void onAttachedToWindow(){
    super.onAttachedToWindow();
  }

  @Override
  protected void onDetachedFromWindow(){
    super.onDetachedFromWindow();
  }

  @Override
  public void onHostResume() {
    // Activity `onResume`
    Config config = mSession.getConfig();

    config.setDepthMode(Config.DepthMode.AUTOMATIC);
    config.setFocusMode(Config.FocusMode.AUTO);

    mSession.configure(config);

    try {
      if (mSession != null) {
        // Note that order matters - see the note in onPause(), the reverse applies here.
        mSession.resume();
      }
    }catch (CameraNotAvailableException e) {
      // In some cases (such as another camera app launching) the camera may be given to
      // a different app instead. Handle this properly by showing a message and recreate the
      // session at the next iteration.
      mSession = null;
      return;
    }
  }

  @Override
  public void onHostPause() {
    // Activity `onPause`
    if (mSession != null) {
      // Note that the order matters - GLSurfaceView is paused first so that it does not try
      // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
      // still call session.update() and get a SessionPausedException.
      mSession.pause();
    }
  }

  @Override
  public void onHostDestroy() {
    // Activity `onDestroy`
    mSession.close();
    mSession = null;
  }


  public LogmealSdkDepthCameraView(ThemedReactContext context) {
    super(context);
    mContextModule = context;
    mContextModule.addLifecycleEventListener(this);

    try {
      mSession = new Session(getContext());
    } catch (UnavailableArcoreNotInstalledException | UnavailableApkTooOldException | UnavailableSdkTooOldException | UnavailableDeviceNotCompatibleException e) {
      e.printStackTrace();
    }

    this.setPreserveEGLContextOnPause(true);
    this.setEGLContextClientVersion(2);
    this.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
    this.setRenderer(this);
    this.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    this.setWillNotDraw(false);
  }


  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

    try {
      depthTexture.createOnGlThread();
      backgroundRenderer.createOnGlThread(getContext());
      backgroundRenderer.createDepthShaders(getContext(), depthTexture.getDepthTexture());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    GLES20.glViewport(0, 0, width, height);

    viewportWidth = width;
    viewportHeight = height;
    viewportChanged = true;
  }

  // ------------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------------

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public void onDrawFrame(GL10 gl) {
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    if (mSession == null) {
      return;
    }

    if (viewportChanged) {
      int displayRotation = getDisplay().getRotation();
      mSession.setDisplayGeometry(displayRotation, viewportWidth, viewportHeight);
      viewportChanged = false;
    }

    mSession.setCameraTextureName(backgroundRenderer.getTextureId());

    try {
      frame = mSession.update();
    } catch (CameraNotAvailableException e) {
      e.printStackTrace();
    }

    depthTexture.update(frame);

    if (captureNextFrame){
      captureNextFrame = false;
      FrameProcessor fp = new FrameProcessor(frame);
      try {
        frameDepthInfo = fp.getDepthInfo();
        if (customDepthInfoStorePath != null) {
          fp.saveDepthInfo(customDepthInfoStorePath);
        }
      } catch (Exception e) {
        frameDepthInfo = null;
        e.printStackTrace();
      }
    }

    backgroundRenderer.draw(frame);
  }

  public FrameDepthInfo getFrameDepthInfo() throws NotYetAvailableException {
    if (frameDepthInfo == null) {
      throw new NotYetAvailableException();
    }
    FrameDepthInfo copy = new FrameDepthInfo(frameDepthInfo.getDepthData(), frameDepthInfo.getImageData(), frameDepthInfo.getCameraInfo());
    frameDepthInfo = null;
    return copy;
  }

  public void captureNextFrame() throws InterruptedException {
    captureNextFrame = true;
    TimeUnit.MILLISECONDS.sleep(600);
  }

  public String getCustomDepthInfoStorePath() {
    return customDepthInfoStorePath;
  }

  public void setCustomDepthInfoStorePath(String customDepthInfoStorePath) {
    this.customDepthInfoStorePath = customDepthInfoStorePath;
  }
}
