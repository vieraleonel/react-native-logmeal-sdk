package es.logmeal.sdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.ExifInterface;
import android.media.Image;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.ar.core.Frame;
import com.google.ar.core.exceptions.NotYetAvailableException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FrameProcessor {
    private Frame frame;
    private long identifier;

    public FrameProcessor(Frame frame) {
        this.frame = frame;
        this.identifier = System.currentTimeMillis();
    }

    private DepthData getDepthData() throws NotYetAvailableException, IOException {
        Image image = frame.acquireDepthImage();
        int height = image.getHeight();
        int width = image.getWidth();

        Image.Plane plane = image.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer().order(ByteOrder.nativeOrder());
        buffer.position(0);
        File depthFile = File.createTempFile(this.identifier + "_depth16", ".png");
        FileChannel fc = new FileOutputStream(depthFile).getChannel();
        fc.write(buffer);
        fc.close();
        image.close();

        return new DepthData(depthFile, width, height);
    }

    private ImageData getImageData(int cropWidth, int cropHeight) throws Exception {
        Image originalImage = frame.acquireCameraImage();
        int originalImageWidth = originalImage.getWidth();
        int originalImageHeight = originalImage.getHeight();

        int format = originalImage.getFormat();
        if (format != ImageFormat.YUV_420_888) {
            throw new Exception("Incompatible imageType");
        }

        YuvImage yuvImage = this.toYuvImage(originalImage);

        byte[] jpegImage = null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            yuvImage.compressToJpeg(new Rect(0, 0, originalImageWidth, originalImageHeight), 100, out);
            jpegImage = out.toByteArray();
        }

        File originalImageFile = File.createTempFile(this.identifier + "_original_RGB", ".jpeg");
        FileChannel fc = new FileOutputStream(originalImageFile).getChannel();
        fc.write(ByteBuffer.wrap(jpegImage));
        fc.close();
        originalImage.close();

        // cropping dimensions
        int scalingFactor = originalImageWidth / cropWidth;
        int desiredWidth = cropWidth * scalingFactor;
        int desiredHeight = cropHeight * scalingFactor;
        int xOffset = (originalImageWidth - desiredWidth) / 2;
        int yOffset = (originalImageHeight - desiredHeight) / 2;

        // no cropping
        if (xOffset == 0 && yOffset == 0) {
            if (originalImageWidth > originalImageHeight) {
                ExifInterface exifInterface = new ExifInterface(originalImageFile.getPath());
                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
                exifInterface.saveAttributes();
            }
            return new ImageData(originalImageFile, originalImageWidth, originalImageHeight, 0, 0);
        }

        Bitmap originalImageBitmap = BitmapFactory.decodeFile(originalImageFile.getAbsolutePath());
        Bitmap croppedBitmap = Bitmap.createBitmap(originalImageBitmap, xOffset, yOffset, desiredWidth, desiredHeight);
        File croppedImageFile = File.createTempFile(this.identifier + "_cropped_RGB", ".jpeg");
        FileOutputStream fos = new FileOutputStream(croppedImageFile);
        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

        if (desiredWidth > desiredHeight) {
            ExifInterface exifInterface = new ExifInterface(croppedImageFile.getPath());
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
            exifInterface.saveAttributes();
        }
        return new ImageData(croppedImageFile, desiredWidth, desiredHeight, scalingFactor, yOffset);
    }

    private CameraInfo getCameraInfo(ImageData imageData) {
        float[] focalLength = frame.getCamera().getImageIntrinsics().getFocalLength();
        float[] principalPoint = frame.getCamera().getImageIntrinsics().getPrincipalPoint();

        if (imageData.getCropDifference() > 0) {
            principalPoint[0] -= imageData.getCropDifference();
        }

        return new CameraInfo(focalLength, principalPoint);
    }

    public FrameDepthInfo getDepthInfo() throws Exception {
        DepthData depthData = this.getDepthData();
        ImageData imageData = this.getImageData(depthData.getWidth(), depthData.getHeight());
        CameraInfo ci = this.getCameraInfo(imageData);

        return new FrameDepthInfo(depthData, imageData, ci);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void saveDepthInfo(String folderPath) throws Exception {
        FrameDepthInfo frameDepthInfo = this.getDepthInfo();

        File depthFile = new File(folderPath, this.identifier + "_depth.png");
        Files.copy(frameDepthInfo.getDepthData().getDepthFile().toPath(), depthFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        File imageFile = new File(folderPath, this.identifier + "_RGP_cropped.png");
        Files.copy(frameDepthInfo.getImageData().getImageRGBFile().toPath(), imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        int[] originalImageDimensions = this.frame.getCamera().getImageIntrinsics().getImageDimensions();
        ImageData originalImageData = this.getImageData(originalImageDimensions[0], originalImageDimensions[1]);
        File originalImageFile = new File(folderPath, this.identifier + "_RGP_original.png");
        Files.copy(originalImageData.getImageRGBFile().toPath(), originalImageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        CameraInfo originalCameraInfo = this.getCameraInfo(originalImageData);

        String info = "";
        info += "focal_length:" + frameDepthInfo.getCameraInfo().focalLengthToString();
        info += System.lineSeparator() + "principal_point:" + frameDepthInfo.getCameraInfo().principalPointToString();
        info += System.lineSeparator() + String.format("image_rgb_cropped_size:%s,%s", frameDepthInfo.getImageData().getWidth(), frameDepthInfo.getImageData().getHeight());
        info += System.lineSeparator() + String.format("image_depth_size:%s,%s", frameDepthInfo.getDepthData().getWidth(), frameDepthInfo.getDepthData().getHeight());
        info += System.lineSeparator() + String.format("scaling_factor:%s", frameDepthInfo.getImageData().getScalingFactor());
        info += System.lineSeparator();
        info += System.lineSeparator() + String.format("original_image_rgb_size:%s,%s", originalImageData.getWidth(), originalImageData.getHeight());
        info += System.lineSeparator() + "original_focal_length:" + originalCameraInfo.focalLengthToString();
        info += System.lineSeparator() + "original_principal_point:" + originalCameraInfo.principalPointToString();
        File infoFile = new File(folderPath, this.identifier + "_info.txt");
        FileOutputStream stream = new FileOutputStream(infoFile);
        stream.write(info.getBytes());
        stream.close();
    }

    private YuvImage toYuvImage(Image image) {
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            throw new IllegalArgumentException("Invalid image format");
        }

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        // U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        int width = image.getWidth();
        int height = image.getHeight();
        return new YuvImage(nv21, ImageFormat.NV21, width, height, /* strides= */ null);
    }
}
