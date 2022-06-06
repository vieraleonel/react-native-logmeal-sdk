package es.logmeal.sdk;

public class CameraInfo {
    private float[] focalLength;
    private float[] principalPoint;

    public CameraInfo(float[] focalLength, float[] principalPoint) {
        this.focalLength = focalLength;
        this.principalPoint = principalPoint;
    }

    public float[] getFocalLength() {
        return focalLength;
    }

    public float[] getPrincipalPoint() {
        return principalPoint;
    }

    public String focalLengthToString() {
        return String.format("%s,%s", focalLength[0], focalLength[1]);
    }

    public String principalPointToString() {
        return String.format("%s,%s", principalPoint[0], principalPoint[1]);
    }
}
