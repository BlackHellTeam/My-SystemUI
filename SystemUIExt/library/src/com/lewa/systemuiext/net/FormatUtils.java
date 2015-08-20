package com.lewa.systemuiext.net;

import java.util.Locale;

import com.lewa.systemuiext.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.NetworkPolicy;

@SuppressLint("DefaultLocale")
public class FormatUtils {
    public static final long KB_IN_BYTES = 1024;
    public static final long MB_IN_BYTES = KB_IN_BYTES * 1024;
    public static final long GB_IN_BYTES = MB_IN_BYTES * 1024;
    public static final long TB_IN_BYTES = GB_IN_BYTES * 1024;
    public static final int MAX_SIZE = 999 * 1024;
    
    public static String combindString(Context context, int resId, String value){
        return context.getString(resId) + ":" + value;
    }
    
    public static String formatSize(Context context, long length) {
        return length == NetworkPolicy.LIMIT_DISABLED ? context.getString(R.string.not_set)
                : (length == 0 ? context.getString(R.string.zero) : formatFileSize(length, false));
    }
    
    @SuppressLint("DefaultLocale")
    private static String formatFileSize(long number, boolean shorter) {
        float result = number;
        String suffix = "B";
        if (result > 900) {
            suffix = "KB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = "MB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = "GB";
            result = result / 1024;
        }
        String value;
        if (result < 1) {
            value = String.format("%.2f", result);
        }
        else if (result < 10) {
            if (shorter) {
                value = String.format("%.1f", result);
            }
            else {
                value = String.format("%.2f", result);
            }
        }
        else if (result < 100) {
            if (shorter) {
                value = String.format("%.0f", result);
            }
            else {
                value = String.format("%.2f", result);
            }
        }
        else {
            value = String.format("%.0f", result);
        }
        return value + suffix;
    }
    public static String formatShorterSize(long size, long total) {
        float totalF = total;
        float sizeF = size;
        String suffix = "B";
        if (totalF > 900) {
            suffix = "KB";
            totalF /= 1024;
            sizeF /= 1024;
        }
        if (totalF > 900) {
            suffix = "MB";
            totalF /= 1024;
            sizeF /= 1024;
        }
        if (totalF > 900) {
            suffix = "GB";
            totalF /= 1024;
            sizeF /= 1024;
        }
        String value;
        if (totalF < 10) {
            value = String.format(Locale.getDefault(), "%.2f / %.2f%s", sizeF, totalF, suffix);
        }
        else if (totalF < 100) {
            value = String.format(Locale.getDefault(), "%.2f / %.1f%s", sizeF, totalF, suffix);
        }
        else {
            value = String.format(Locale.getDefault(), "%.2f / %.0f%s", sizeF, totalF, suffix);
        }
        return value;
    }
    
    public static int calcGradientColor(int c1, int c2, float progress) {
        int r1 = Color.red(c1);
        int g1 = Color.green(c1);
        int b1 = Color.blue(c1);
        int r2 = Color.red(c2);
        int g2 = Color.green(c2);
        int b2 = Color.blue(c2);
        int rS = (int) (Math.abs(r2 - r1) * progress);
        int gS = (int) (Math.abs(g2 - g1) * progress);
        int bS = (int) (Math.abs(b2 - b1) * progress);
        r2 = (int) (r1 > r2 ? r1 - rS : r1 + rS);
        g2 = (int) (g1 > g2 ? g1 - gS : g1 + gS);
        b2 = (int) (b1 > b2 ? b1 - bS : b1 + bS);
        return Color.argb(Color.alpha(c1), r2, g2, b2);
    }
    
    public static String formatShorterSize(long size) {
        String str;
        if (size < KB_IN_BYTES)
            str = size + "B";
        else if (size < MB_IN_BYTES)
            str = (int) (size / KB_IN_BYTES) + "KB";
        else if (size < GB_IN_BYTES)
            str = (int) (size / MB_IN_BYTES) + "MB";
        else
            str = (int) (size / GB_IN_BYTES) + "GB";
        return str;
    }
}
