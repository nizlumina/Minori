package com.nizlumina.minori.utility;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Simple utility methods.
 */
public class Util
{
    public static int dpToPx(Context context, int dp)
    {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public static int pxToDp(Context context, int px)
    {
        // Get the screen's density scale
        final float scale = context.getResources().getDisplayMetrics().density;

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

    /**
     * Short util method for simple expand/collapse height for layouts
     *
     * @param viewGroup The layout to be expand/collapsed
     * @param endHeight Target height
     * @return
     */
    public static ValueAnimator makeLayoutHeightAnimator(final ViewGroup viewGroup, int endHeight)
    {
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(viewGroup.getHeight(), endHeight);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                int val = (Integer) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = viewGroup.getLayoutParams();
                params.height = val;
                viewGroup.setLayoutParams(params);
            }
        });

        return valueAnimator;
    }

    /**
     * Get best terms for title search.
     * If the title have 1 - 2 terms, it will be leaft as it is (albeit more sanitized).
     * If more, then the order is,
     * 1: First word longer than 2.
     * 2: Longest word between #1 and last word
     * 3: Last word
     *
     * @param input Titles as input
     * @return String of best terms.
     */
    public static String getBestTerms(String input, boolean addLastWord)
    {
        final String[] terms = input.trim().replaceAll("[^a-zA-Z0-9@]+", "%").split("%");
        final StringBuilder builder = new StringBuilder(terms.length);
//        System.out.println("LENGTH " + terms.length);

        if (terms.length == 0)
        {
            return null;
        }

        if (terms.length < 3)
        {
            for (String term : terms)
            {
                builder.append(term);
                builder.append(' ');
            }
        }
        else
        {
            int longestTermLength = 0;
            String longestMiddleTerm = null;

            final int termsLength = terms.length;
            String firstTerm = null;
            String lastTerm = null;
            for (int i = 0; i < termsLength; ++i)
            {
                final String term = terms[i];

                if (i == termsLength - 1) //to make sure its the last word after two words have been added
                {
                    lastTerm = terms[i];
                }
                else if (term.length() > 2) //use else if since its an independent event
                {
                    //set first term then skip
                    if (firstTerm == null)
                    {
                        firstTerm = terms[i];
                        builder.append(firstTerm);
                        builder.append(' ');
                    }
                    else if (term.length() > longestTermLength)
                    {
                        longestTermLength = term.length();
                        longestMiddleTerm = term;
                        //keep reassigning till it gets the longest
                    }
                }
            }

            if (longestMiddleTerm != null)
            {
                builder.append(longestMiddleTerm);
                builder.append(' ');
            }

            if (lastTerm != null && addLastWord)
            {
                builder.append(lastTerm);
            }
        }

        return builder.toString().trim();
    }
}
