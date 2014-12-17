package com.nizlumina.minori.android.utility;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;

/**
 * Modded version of source: http://stackoverflow.com/q/20973089/3939904
 */
public class AdvancedScaleAnimation extends ScaleAnimation
{

    private float fromHeight;
    private float heightDelta;
    private float fromWidth;
    private float widthDelta;
    private View viewToScale;

    private boolean animateHeight = true;
    private boolean animateWidth = true;

    public AdvancedScaleAnimation(View viewToScale, float fromX, float toX, float fromY, float toY)
    {
        super(fromX, toX, fromY, toY);
        init(viewToScale, fromX, toX, fromY, toY);
    }

    public AdvancedScaleAnimation(View viewToScale, float fromX, float toX, float fromY, float toY, float pivotX, float pivotY)
    {
        super(fromX, toX, fromY, toY, pivotX, pivotY);
        init(viewToScale, fromX, toX, fromY, toY);
    }

    public AdvancedScaleAnimation(View viewToScale, float fromX, float toX, float fromY, float toY, int pivotXType, float pivotXValue, int pivotYType, float pivotYValue)
    {
        super(fromX, toX, fromY, toY, pivotXType, pivotXValue, pivotYType, pivotYValue);
        init(viewToScale, fromX, toX, fromY, toY);
    }

    private void init(View viewToScale, float fromX, float toX, float fromY, float toY)
    {
        this.viewToScale = viewToScale;
        this.fromHeight = fromY * viewToScale.getMeasuredHeight();
        this.heightDelta = (toY - fromY) * viewToScale.getMeasuredHeight();

        this.fromWidth = fromX;
        this.widthDelta = toX - fromX;

        //if (heightDelta == 0) animateHeight = false;
        //if (widthDelta == 0) animateWidth = false;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, @NonNull Transformation t)
    {
        super.applyTransformation(interpolatedTime, t);
        if (animateHeight)
            viewToScale.getLayoutParams().height = (int) (fromHeight + heightDelta * interpolatedTime);

        if (animateWidth)
            viewToScale.getLayoutParams().width = (int) (fromWidth + widthDelta * interpolatedTime);

        viewToScale.requestLayout();
    }

    @Override
    public boolean willChangeBounds()
    {
        return true;
    }
}