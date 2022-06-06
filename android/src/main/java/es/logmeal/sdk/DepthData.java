package es.logmeal.sdk;

import java.io.File;

public class DepthData {
    private File depthFile;
    private int width;
    private int height;

    public DepthData(File depthFile, int width, int height) {
        this.depthFile = depthFile;
        this.width = width;
        this.height = height;
    }

    public File getDepthFile() {
        return depthFile;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
