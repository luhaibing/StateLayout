package com.luhaibing.example;

import android.app.Application;

import com.luhaibing.statelayout.StateLayout;

/**
 * Created by luhaibing
 * <p>
 * Date: 2017-02-10.
 * Time: 00:49
 * <p>
 * className: App
 * classDescription: ...
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StateLayout.Builder
                .newBuilder()
                .setLoadingId(R.layout.layout_loading)
                .setEmptyId(R.layout.layout_empty)
                .setErrorId(R.layout.layout_error)
                .setUseAnim(true)
                .setType(StateLayout.TOGETHER)
                .build();
    }

}