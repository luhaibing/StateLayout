package com.luhaibing.statelayout;

/**
 * Created by luhaibing
 * <p>
 * Date: 2017-02-10.
 * Time: 00:24
 * <p>
 * className: StateListener
 * classDescription: 状态监听
 */
public interface StateListener {

    /**
     * 取消
     * 因为状态重合情况/当前动画未结束
     */
    void onCancel();

    /**
     * 准备
     */
    void onPreper();

    /**
     * 状态切换中
     *
     * @param current 当前状态
     * @param target  目标状态
     * @param isanim  切换时是否使用动画
     */
    void onStart(@StateLayout.ViewKey int current, @StateLayout.ViewKey int target, boolean isanim);

    /**
     * 完成
     */
    void onComplete();

}
