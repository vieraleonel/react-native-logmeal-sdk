package es.logmeal.sdk;

public class FrameDepthInfo {
    private DepthData depthData;
    private ImageData imageData;
    private CameraInfo cameraInfo;

    public FrameDepthInfo(DepthData depthData, ImageData imageData, CameraInfo cameraInfo) {
        this.depthData = depthData;
        this.imageData = imageData;
        this.cameraInfo = cameraInfo;
    }

    public DepthData getDepthData() {
        return depthData;
    }

    public ImageData getImageData() {
        return imageData;
    }

    public CameraInfo getCameraInfo() {
        return cameraInfo;
    }
}
