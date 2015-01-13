package com.nizlumina.minori.android.utility;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.ImageButton;

/**
 * Simple utility methods.
 */
public class Util
{
    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        // Get the screen's density scale
        final float scale = Resources.getSystem().getDisplayMetrics().density;

        // Convert the dps to pixels, based on density scale
        return (int) (px * scale + 0.5f);
    }

    public static String formatSize(long bytes) //Source: http://stackoverflow.com/a/24805871/3939904
    {
        if (bytes < 1024) return bytes + " B";
        int z = (63 - Long.numberOfLeadingZeros(bytes)) / 10;
        return String.format("%.1f %sB", (double) bytes / (1L << (z * 10)), " KMGTPE".charAt(z));
    }

    //Net Utils
    public static boolean internetAvailable(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
    }

    /**
     * Set the tint for both foreground and background drawable.
     *
     * @param button         ImageButton to be colorized.
     * @param foregroundTint RGB color integer. Mode: SRC_ATOP
     * @param backgroundTint RGB color integer. Mode: MULTIPLY (due to black shadows)
     */
    public static void tintImageButton(ImageButton button, int foregroundTint, int backgroundTint)
    {
        if (button != null)
        {
            Drawable fg = button.getDrawable();
            if (fg != null) fg.setColorFilter(foregroundTint, PorterDuff.Mode.SRC_ATOP);

            Drawable bg = button.getBackground();
            if (bg != null) bg.setColorFilter(backgroundTint, PorterDuff.Mode.MULTIPLY);
        }
    }

    public static void logThread(String identifier)
    {
        Log.v(identifier, "Thread ID:" + Thread.currentThread().getId());
    }

    public static long logTime(long startTimeInNano)
    {
        return logTime(startTimeInNano, null);
    }


    public static long logTime(long startTimeInNano, String extras)
    {
        return logTime(startTimeInNano, extras, false);
    }

    public static long logTime(long startTimeInNano, String extras, boolean alsoShowNanos)
    {
        long s = System.nanoTime();
        long d = (s - startTimeInNano);
        if (alsoShowNanos)
            Log.v(Util.class.getSimpleName(), String.format("Time taken: %d ms, %d ns, %s", d / (1000 * 1000), d, extras));
        else
            Log.v(Util.class.getSimpleName(), String.format("Time taken: %d ms, %s", d / (1000 * 1000), extras));
        return s;
    }
// Unused
//
//    public static int firstDigit(int integer)
//    {
//        while (integer > 9) //Example for number 123 => 123/10, 12.3 => 12.3/10, 1.23 => 1
//            integer /= 10;
//        return integer;
//    }
//


//    public static void animateBack(ActionBarActivity currentActivity)
//    {
//        currentActivity.overridePendingTransition(R.anim.act_slide_from_left, R.anim.act_slide_to_right);
//    }
//
//    public static void animateIn(ActionBarActivity currentActivity)
//    {
//        currentActivity.overridePendingTransition(R.anim.act_slide_from_right, R.anim.act_slide_to_left);
//    }

// Just some fun stuffs
//
//    public <T> T callMethod(Callable<T> callable) throws Exception
//    {
//        return callable.call();
//    }
}
