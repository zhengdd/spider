package com.dongdong.animal.animallibs;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.dongdong.animal.spider.SpiderBus;

public class TwoActivity extends Activity {

    Button mBtnSend;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two);

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                BusData data = new BusData();
                data.setCode(10086);
                data.setMsg("我的自定义总线框架");
                SpiderBus.getInstance().post(data);
            }
        });

        mBtnSend = (Button) findViewById(R.id.btn_send);
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                thread.start();
            }
        });

    }


    private void setmBtnSend() {
        BusData data = new BusData();
        data.setCode(10086);
        data.setMsg("我的自定义总线框架");
        SpiderBus.getInstance().post(data);
    }
}
