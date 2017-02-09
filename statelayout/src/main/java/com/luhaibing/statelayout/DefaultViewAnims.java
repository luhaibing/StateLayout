package com.luhaibing.statelayout;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;

/**
 * Created by luhaibing
 * <p>
 * Date: 2017-02-10.
 * Time: 00:22
 * <p>
 * className: DefaultViewAnims
 * classDescription: 默认的切换动画
 */
public class DefaultViewAnims implements ViewAnims {

    @Override
    public ValueAnimator providerDisappear(int state, View view) {
        return ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0f)
                .setDuration(250);
    }

    @Override
    public ValueAnimator providerAppear(int state, View view) {
        return ObjectAnimator.ofFloat(view, "alpha", 0f, 1.0f)
                .setDuration(250);
    }

}