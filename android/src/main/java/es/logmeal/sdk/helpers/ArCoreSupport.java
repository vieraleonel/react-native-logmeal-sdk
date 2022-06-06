package es.logmeal.sdk.helpers;

import com.google.ar.core.ArCoreApk;

public class ArCoreSupport {
    private boolean isArCoreSupported = false;
    private boolean isDepthSupported = false;
    private ArCoreApk.Availability arCoreAvailability = null;

    public boolean isArCoreSupported() {
        return isArCoreSupported;
    }

    public void setArCoreSupported(boolean arCoreSupported) {
        isArCoreSupported = arCoreSupported;
    }

    public boolean isDepthSupported() {
        return isDepthSupported;
    }

    public void setDepthSupported(boolean depthSupported) {
        isDepthSupported = depthSupported;
    }

    public ArCoreApk.Availability getArCoreAvailability() {
        return arCoreAvailability;
    }

    public void setArCoreAvailability(ArCoreApk.Availability arCoreAvailability) {
        this.arCoreAvailability = arCoreAvailability;
    }
}
