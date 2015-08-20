/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lewa.systemuiext;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import android.util.Log;
/**
 * Contains phone signal strength related information.
 */
public class LewaSignalStrength extends SignalStrength {

    private static final String LOG_TAG = "LewaSignalStrength";
    private static final boolean DBG = true;
    public static final int SIGNAL_STRENGTH_SUPER_GREAT = 5;


    public LewaSignalStrength(SignalStrength s) {
        copyFrom(s);
    }

    @Override 
    public int getGsmLevel() {
        int level;
        //lewa decrease 2asu for all
        // ASU ranges from 0 to 31 - TS 27.007 Sec 8.5
        // asu = 0 (-113dB or less) is very weak
        // signal, its better to show 0 bars to the user in such cases.
        // asu = 99 is a special case, where the signal strength is unknown.
/*        int asu = getGsmSignalStrength();
        if (asu <= 2 || asu == 99) level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        else if (asu >= 12) level = SIGNAL_STRENGTH_SUPER_GREAT;
        else if (asu >= 10) level = SIGNAL_STRENGTH_GREAT;
        else if (asu >= 5)  level = SIGNAL_STRENGTH_GOOD;
        else if (asu >= 3)  level = SIGNAL_STRENGTH_MODERATE;
        else level = SIGNAL_STRENGTH_POOR;
        if (DBG) log("getGsmLevel=" + level);*/
        int dbm = getGsmDbm();
        if (dbm == -1) level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        else if (dbm >= -80) level = SIGNAL_STRENGTH_SUPER_GREAT;
        else if (dbm >= -87) level = SIGNAL_STRENGTH_GREAT;
        else if (dbm >= -93) level = SIGNAL_STRENGTH_GOOD;
        else if (dbm >= -99)  level = SIGNAL_STRENGTH_MODERATE;
        else if (dbm >= -105)  level = SIGNAL_STRENGTH_POOR;
        else level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        Log.i(LOG_TAG,"gsm-->level :"+level+"gsm-->dbm :"+dbm);
        return level;
    }

    @Override 
    public int getLteLevel() {
    /*
     * TS 36.214 Physical Layer Section 5.1.3 TS 36.331 RRC RSSI = received
     * signal + noise RSRP = reference signal dBm RSRQ = quality of signal
     * dB= Number of Resource blocksxRSRP/RSSI SNR = gain=signal/noise ratio
     * = -10log P1/P2 dB
     */
     //lewa decrease 5db for all
    int rssiIconLevel = SIGNAL_STRENGTH_NONE_OR_UNKNOWN, rsrpIconLevel = -1, snrIconLevel = -1;
    int lteRsrp = getLteRsrp();
    if (lteRsrp > -44) rsrpIconLevel = -1;
    else if (lteRsrp >= -97) rsrpIconLevel = SIGNAL_STRENGTH_SUPER_GREAT;
    else if (lteRsrp >= -105) rsrpIconLevel = SIGNAL_STRENGTH_GREAT;
    else if (lteRsrp >= -110) rsrpIconLevel = SIGNAL_STRENGTH_GOOD;
    else if (lteRsrp >= -115) rsrpIconLevel = SIGNAL_STRENGTH_MODERATE;
    else if (lteRsrp >= -120) rsrpIconLevel = SIGNAL_STRENGTH_POOR;
    else rsrpIconLevel = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

    /*
     * Values are -200 dB to +300 (SNR*10dB) RS_SNR >= 13.0 dB =>4 bars 4.5
     * dB <= RS_SNR < 13.0 dB => 3 bars 1.0 dB <= RS_SNR < 4.5 dB => 2 bars
     * -3.0 dB <= RS_SNR < 1.0 dB 1 bar RS_SNR < -3.0 dB/No Service Antenna
     * Icon Only
     */
    
    int lteRssnr = getLteRssnr();
    if (lteRssnr > 300) snrIconLevel = -1;
    else if (lteRssnr >= 130) snrIconLevel = SIGNAL_STRENGTH_SUPER_GREAT;
    else if (lteRssnr >= 100) snrIconLevel = SIGNAL_STRENGTH_GREAT; //lewa decrease 30db
    else if (lteRssnr >= 40) snrIconLevel = SIGNAL_STRENGTH_GOOD;//lewa decrease 5db
    else if (lteRssnr >= 10) snrIconLevel = SIGNAL_STRENGTH_MODERATE;
    else if (lteRssnr >= -30) snrIconLevel = SIGNAL_STRENGTH_POOR;
    else if (lteRssnr >= -200)
        snrIconLevel = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

    if (DBG) Log.i(LOG_TAG,"getLTELevel - rsrp:" + lteRsrp + " snr:" + lteRssnr + " rsrpIconLevel:"
            + rsrpIconLevel + " snrIconLevel:" + snrIconLevel);

    /* Choose a measurement type to use for notification */
    if (snrIconLevel != -1 && rsrpIconLevel != -1) {
        /*
         * The number of bars displayed shall be the smaller of the bars
         * associated with LTE RSRP and the bars associated with the LTE
         * RS_SNR
         */
       /* return (rsrpIconLevel < snrIconLevel ? rsrpIconLevel : snrIconLevel);*/
        return rsrpIconLevel;
    }
    
    if (snrIconLevel != -1) return snrIconLevel;

    if (rsrpIconLevel != -1) return rsrpIconLevel;
    
    /* Valid values are (0-63, 99) as defined in TS 36.331 */
    //lewa decrease 2db
    int lteSignalStrength = getLteSignalStrength();
    if (lteSignalStrength > 63) rssiIconLevel = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
    else if (lteSignalStrength >= 12) rssiIconLevel = SIGNAL_STRENGTH_SUPER_GREAT;
    else if (lteSignalStrength >= 10) rssiIconLevel = SIGNAL_STRENGTH_GREAT;
    else if (lteSignalStrength >= 6) rssiIconLevel = SIGNAL_STRENGTH_GOOD;
    else if (lteSignalStrength >= 3) rssiIconLevel = SIGNAL_STRENGTH_MODERATE;
    else if (lteSignalStrength >= 0) rssiIconLevel = SIGNAL_STRENGTH_POOR;
    if (DBG) Log.i(LOG_TAG,"getLTELevel - rssi:" + lteSignalStrength + " rssiIconLevel:"
            + rssiIconLevel);
    return rssiIconLevel;

    }
    @Override
    public int getTdScdmaLevel() {
        final int tdScdmaDbm = getTdScdmaDbm();
        int level;

        //lewa decrease 5db
/*        if ((tdScdmaDbm > -25) || (tdScdmaDbm == SignalStrength.INVALID))
                level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;*/
        if (tdScdmaDbm == -1) level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        else if (tdScdmaDbm >= -85) level = SIGNAL_STRENGTH_SUPER_GREAT;
        else if (tdScdmaDbm >= -90) level = SIGNAL_STRENGTH_GREAT;
        else if (tdScdmaDbm >= -95) level = SIGNAL_STRENGTH_GOOD;
        else if (tdScdmaDbm >= -99) level = SIGNAL_STRENGTH_MODERATE;
        else if (tdScdmaDbm >= -105) level = SIGNAL_STRENGTH_POOR;
        else level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        if (DBG)
            Log.i(LOG_TAG,"getTdScdmaLevel = " + level + "tdScdmaDbm :" + tdScdmaDbm);
        return level;
     }

     @Override  
     public int getCdmaLevel() {
        final int cdmaDbm = getCdmaDbm();
        final int cdmaEcio = getCdmaEcio();
        int levelDbm;
        int levelEcio;

        //lewa decrease 5db
        if (cdmaDbm >= -75) levelDbm = SIGNAL_STRENGTH_SUPER_GREAT;
        else if (cdmaDbm >= -80) levelDbm = SIGNAL_STRENGTH_GREAT;
        else if (cdmaDbm >= -90) levelDbm = SIGNAL_STRENGTH_GOOD;
        else if (cdmaDbm >= -100) levelDbm = SIGNAL_STRENGTH_MODERATE;
        else if (cdmaDbm >= -105) levelDbm = SIGNAL_STRENGTH_POOR;
        else levelDbm = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

        //lewa decrease 10db
        // Ec/Io are in dB*10
        if (cdmaEcio >= -90) levelEcio = SIGNAL_STRENGTH_SUPER_GREAT;
        else if (cdmaEcio >= -100) levelEcio = SIGNAL_STRENGTH_GREAT;
        else if (cdmaEcio >= -120) levelEcio = SIGNAL_STRENGTH_GOOD;
        else if (cdmaEcio >= -140) levelEcio = SIGNAL_STRENGTH_MODERATE;
        else if (cdmaEcio >= -160) levelEcio = SIGNAL_STRENGTH_POOR;
        else levelEcio = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

        int level = (levelDbm < levelEcio) ? levelDbm : levelEcio;
        if (DBG)
            Log.i(LOG_TAG,"getCdmaLevel=" + level + "cdmaDbm :" + cdmaDbm + "cdmaEcio:"
                    + cdmaEcio + "levelEcio:" + levelEcio + "levelDbm :"
                    + levelDbm);
        return level;
    }

    @Override 
    public int getEvdoLevel() {
        int evdoDbm = getEvdoDbm();
        int evdoSnr = getEvdoSnr();
        int levelEvdoDbm;
        int levelEvdoSnr;
        //lewa decrease 5db
        if (evdoDbm >= -65) levelEvdoDbm = SIGNAL_STRENGTH_SUPER_GREAT;
        else if (evdoDbm >= -70) levelEvdoDbm = SIGNAL_STRENGTH_GREAT;
        else if (evdoDbm >= -80) levelEvdoDbm = SIGNAL_STRENGTH_GOOD;
        else if (evdoDbm >= -95) levelEvdoDbm = SIGNAL_STRENGTH_MODERATE;
        else if (evdoDbm >= -110) levelEvdoDbm = SIGNAL_STRENGTH_POOR;
        else levelEvdoDbm = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

        if (evdoSnr >= 7) levelEvdoSnr = SIGNAL_STRENGTH_SUPER_GREAT;//lewa decrease 1
        else if (evdoSnr >= 6) levelEvdoSnr = SIGNAL_STRENGTH_GREAT;
        else if (evdoSnr >= 5) levelEvdoSnr = SIGNAL_STRENGTH_GOOD;
        else if (evdoSnr >= 3) levelEvdoSnr = SIGNAL_STRENGTH_MODERATE;
        else if (evdoSnr >= 1) levelEvdoSnr = SIGNAL_STRENGTH_POOR;
        else levelEvdoSnr = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

        int level = (levelEvdoDbm < levelEvdoSnr) ? levelEvdoDbm : levelEvdoSnr;
        if (DBG)
            Log.i(LOG_TAG,"getEvdoLevel=" + level + "evdoDbm :" + evdoDbm + "evdoSnr :"
                    + evdoSnr + "levelEvdoDbm :" + levelEvdoDbm + "levelEvdoSnr :"
                    + levelEvdoSnr);
        return level;
    }

     /**
     * log
     */
    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
   }
}
