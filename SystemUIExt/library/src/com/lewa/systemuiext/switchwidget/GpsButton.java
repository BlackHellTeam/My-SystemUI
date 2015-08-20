
package com.lewa.systemuiext.switchwidget;

import com.lewa.systemuiext.R;

import android.content.Intent;
import android.provider.Settings;
import android.location.LocationManager;
import android.content.ContentResolver;

/**
 * @author xjzhang
 * @author krluo
 * @author jdsong
 */
public class GpsButton extends ObserveButton
{
    public GpsButton() {
        super();
        mObservedUris.add(Settings.Secure.getUriFor(Settings.Secure.LOCATION_PROVIDERS_ALLOWED));
        mLabel = R.string.title_toggle_gps;
        mButtonName = R.string.title_toggle_gps;
        mSettingsIcon = R.drawable.btn_setting_gps;
    }

    @Override
    protected void updateState() {
        if (getGpsState()) {
            mIcon = R.drawable.stat_gps_on;
            mState = STATE_ENABLED;
            mTextColor = sEnabledColor;
        } else {
            mIcon = R.drawable.stat_gps_off;
            mState = STATE_DISABLED;
            mTextColor = sDisabledColor;
        }
    }

    @Override
    protected void toggleState() {
        ContentResolver resolver = sContext.getContentResolver();
        boolean enabled = getGpsState();
        Settings.Secure.setLocationProviderEnabled(
                resolver, LocationManager.GPS_PROVIDER, !enabled);
        if (!enabled) {
            Intent intent = new Intent(POWERSAVING_ACTION_NOTIFY_ON);
            intent.putExtra(POWERSAVING_DEV_TYPE, DEV_GPS);
            sContext.sendBroadcast(intent);
        }
    }

    @Override
    protected boolean onLongClick() {
        startActivity("android.settings.LOCATION_SOURCE_SETTINGS");
        return true;
    }

    private boolean getGpsState() {
        ContentResolver resolver = sContext.getContentResolver();
        return Settings.Secure.isLocationProviderEnabled(
                resolver, LocationManager.GPS_PROVIDER);
    }
}
