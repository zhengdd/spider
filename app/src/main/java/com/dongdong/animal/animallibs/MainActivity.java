package com.dongdong.animal.animallibs;

import android.content.Intent;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dongdong.animal.spider.SpiderBus;
import com.dongdong.animal.spider.Subscribe;

public class MainActivity extends AppCompatActivity {
    TextView mtv1, mtv2;
    Button mbtnnext, mbtnun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mtv1 = (TextView) findViewById(R.id.tv_1);
        mtv2 = (TextView) findViewById(R.id.tv_2);
        mbtnnext = (Button) findViewById(R.id.btn_to_next);
        mbtnun = (Button) findViewById(R.id.btn_to_out);

        mbtnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TwoActivity.class);
                startActivity(intent);
            }
        });

        mbtnun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mtv1.setText("");
                mtv2.setText("");
                SpiderBus.getInstance().unregister(this);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        SpiderBus.getInstance().register(this);
    }


    @Subscribe
    public void updataMsg(BusData data) {
        Looper.myLooper();
        if (data != null) {
            mtv1.setText(data.code + "");
        }
    }

    @Subscribe
    public void busup(BusData data) {
        if (data != null) {
            mtv2.setText(data.msg);
        }
    }

}
