package com.luhaibing.statelayout;

import android.animation.ValueAnimator;
import android.view.View;

/**
 * Created by luhaibing
 * <p>
 * Date: 2017-02-10.
 * Time: 00:19
 * <p>
 * className: ViewAnims
 * classDescription: 视图切换动画提供者
 */
public interface ViewAnims {

    ValueAnimator providerDisappear(int state, View view);

    ValueAnimator providerAppear(int state, View view);

}