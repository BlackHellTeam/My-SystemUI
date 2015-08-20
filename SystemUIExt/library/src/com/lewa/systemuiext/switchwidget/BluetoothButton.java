
package com.lewa.systemuiext.switchwidget;

import com.lewa.systemuiext.R;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.util.Log;

public class BluetoothButton extends ReceiverButton
{
    private static final class BluetoothStateTracker extends StateTracker {

        @Override
        public int getActualState(Context context) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                return STATE_UNKNOWN;
            }

            return bluetoothStateToFiveState(mBluetoothAdapter.getState());
        }

        @Override
        protected void requestStateChange(Context context,
                final boolean desiredState) {
            // Actually request the Bluetooth change and persistent
            // settings write off the UI thread, as it can take a
            // user-noticeable amount of time, especially if there's
            // disk contention.
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... args) {
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();
                    } else {
                        mBluetoothAdapter.enable();
                    }
                    return null;
                }
            }.execute();
        }

        @Override
        public void onActualStateChange(Context context, Intent intent) {
            int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            setCurrentState(context, bluetoothStateToFiveState(bluetoothState));
        }

        /**
         * Converts BluetoothAdapter's state values into our
         * Wifi/Bluetooth-common state values.
         */
        private static int bluetoothStateToFiveState(int bluetoothState) {
            switch (bluetoothState) {
                case BluetoothAdapter.STATE_OFF:
                    return STATE_DISABLED;
                case BluetoothAdapter.STATE_ON:
                    return STATE_ENABLED;
                case BluetoothAdapter.STATE_TURNING_ON:
                    return STATE_TURNING_ON;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    return STATE_TURNING_OFF;
                default:
                    return STATE_UNKNOWN;
            }
        }
    }

    public BluetoothButton() {
        super();
        mStateTracker = new BluetoothStateTracker();
        mFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        IntentFilter iflter = new IntentFilter();
        iflter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        sContext.registerReceiver(mReceiver, iflter);
        mLabel = R.string.title_toggle_bluetooth;
        mButtonName = R.string.title_toggle_bluetooth;
        mSettingsIcon = R.drawable.btn_setting_bluetooth;
    }

    @Override
    protected void updateState() {
        mState = mStateTracker.getTriState(sContext);
        switch (mState) {
            case STATE_DISABLED:
                mIcon = R.drawable.stat_bluetooth_off;
                mTextColor = sDisabledColor;
                break;
            case STATE_ENABLED:
                mIcon = R.drawable.stat_bluetooth_on;
                break;
            case STATE_INTERMEDIATE:
                mIcon = R.drawable.switch_bluetooth_inter;
                mTextColor = sEnabledColor;
                break;
        }
    }
    //added for bug:#64236
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                mStateTracker.onActualStateChange(context,intent);
                update();
            }
        }
    };

    @Override
    protected void toggleState() {
        mStateTracker.toggleState(sContext);
    }

    @Override
    protected boolean onLongClick() {
        startActivity("android.settings.BLUETOOTH_SETTINGS");
        return true;
    }
}
