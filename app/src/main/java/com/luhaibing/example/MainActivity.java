package com.luhaibing.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.luhaibing.statelayout.StateLayout;
import com.luhaibing.statelayout.StateListener;

public class MainActivity extends AppCompatActivity {

    private StateLayout stateLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        stateLayout= (StateLayout) findViewById(R.id.stateLayout);
        setContentView(R.layout.activity_main2);
        stateLayout = StateLayout.wrap(findViewById(R.id.ll));
        stateLayout
                .setOnClickEvent(StateLayout.ERROR, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "111111111111", Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnClickEvent(StateLayout.ERROR, R.id.error, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "222222222222", Toast.LENGTH_SHORT).show();
                    }
                })
                .setStateListener(new StateListener() {

                    @Override
                    public void onCancel() {
                        Log.e("TAG", "onCancel");
                    }

                    @Override
                    public void onPreper() {
                        Log.e("TAG", "onPreper");
                    }

                    @Override
                    public void onStart(int current, int target, boolean isanim) {
                        Log.e("TAG", "onStart current -->> " + current + " ; target -->> " + target + " ; isanim -->>> " + isanim);
                    }

                    @Override
                    public void onComplete() {
                        Log.e("TAG", "onComplete");
                    }
                });
    }

    public void content(View view) {
        //stateLayout.showView(StateLayout.CONTENT);
        stateLayout.showContent();
    }

    public void loading(View view) {
        //stateLayout.showView(StateLayout.LOADING);
        stateLayout.showLoading();
    }

    public void empty(View view) {
        //stateLayout.showView(StateLayout.EMPTY);
        stateLayout.showEmpty();
    }

    public void error(View view) {
        //stateLayout.showView(StateLayout.ERROR);
        stateLayout.showError();
    }

}