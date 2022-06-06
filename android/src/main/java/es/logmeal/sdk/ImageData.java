package es.logmeal.sdk;

import java.io.File;

public class ImageData {
    private File imageRGBFile;
    private int width;
    private int height;
    private int scalingFactor;
    private int cropDifference;

    public ImageData(File imageRGBFile, int width, int height, int scalingFactor, int cropDifference) {
        this.imageRGBFile = imageRGBFile;
        this.width = width;
        this.height = height;
        this.scalingFactor = scalingFactor;
        this.cropDifference = cropDifference;
    }

    public File getImageRGBFile() {
        return imageRGBFile;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getScalingFactor() {
        return scalingFactor;
    }

    public int getCropDifference() {
        return cropDifference;
    }
}
