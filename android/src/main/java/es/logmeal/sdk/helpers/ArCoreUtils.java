package es.logmeal.sdk.helpers;

import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;

public final class ArCoreUtils {
    private static final String TAG = "LogMealSDK.ArCoreUtils";

    public static ArCoreSupport isDeviceSupported(AppCompatActivity context) {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(context);

        if (availability.isTransient()) {
            // Continue to query availability at 5Hz while compatibility is checked in the background.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ArCoreUtils.isDeviceSupported(context);
                }
            }, 200);
        }

        ArCoreSupport arCoreSupport = new ArCoreSupport();
        arCoreSupport.setArCoreAvailability(availability);

        switch (availability) {
            case SUPPORTED_INSTALLED:
                arCoreSupport.setArCoreSupported(true);
                break;

            case SUPPORTED_APK_TOO_OLD:
            case SUPPORTED_NOT_INSTALLED:
                try {
                    // Request ARCore installation or update if needed.
                    ArCoreApk.InstallStatus installStatus = ArCoreApk.getInstance().requestInstall(context, true);
                    switch (installStatus) {
                        case INSTALL_REQUESTED:
                            Log.i(TAG, "ARCore installation requested.");
                            arCoreSupport.setArCoreSupported(false);
                            break;
                        case INSTALLED:
                            arCoreSupport.setArCoreSupported(true);
                            break;
                    }
                } catch (UnavailableException e) {
                    Log.e(TAG, "ARCore not installed", e);
                }
                arCoreSupport.setArCoreSupported(false);

            case UNSUPPORTED_DEVICE_NOT_CAPABLE:
                Log.d(TAG, "This device is not supported for A");
                arCoreSupport.setArCoreSupported(false);
                break;

            case UNKNOWN_CHECKING:
                // ARCore is checking the availability with a remote query.
                // This function should be called again after waiting 200 ms to determine the query result.

            case UNKNOWN_ERROR:
            case UNKNOWN_TIMED_OUT:
                Log.d(TAG, "There was an error checking for AR availability. This may be due to the device being offline.");
                // throw new UnknownError("There was an error checking for AR availability. This may be due to the device being offline.");
                // There was an error checking for AR availability. This may be due to the device being offline.
                // Handle the error appropriately.
        }

        if (arCoreSupport.isArCoreSupported()) {
            try {
                Session session = new Session(context);
                arCoreSupport.setDepthSupported(session.isDepthModeSupported(Config.DepthMode.AUTOMATIC));
            } catch (UnavailableArcoreNotInstalledException | UnavailableApkTooOldException | UnavailableSdkTooOldException | UnavailableDeviceNotCompatibleException e) {
                Log.d(TAG, "Depth not supported");
                arCoreSupport.setDepthSupported(false);
            }
        }

        return arCoreSupport;
    }


}
