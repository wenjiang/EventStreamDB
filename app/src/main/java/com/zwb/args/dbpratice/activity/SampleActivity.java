package com.zwb.args.dbpratice.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.zwb.args.dbpratice.R;
import com.zwb.args.dbpratice.event.InsertEvent;
import com.zwb.args.dbpratice.exception.NoTableException;
import com.zwb.args.dbpratice.model.Status;
import com.zwb.args.dbpratice.util.LogUtil;

import java.util.ArrayList;
import java.util.List;


public class SampleActivity extends ActionBarActivity {
    private Button btTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btTest = (Button) findViewById(R.id.bt_test);
        btTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData();
            }
        });
    }

    public void addData() {
        List<Status> statuses = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            Status status = new Status();
            status.setName("吸金好");
            status.setStatusId("01");
            statuses.add(status);
        }

        InsertEvent insertEvent = new InsertEvent();
        try {
            insertEvent.to(Status.class).insertAll(statuses);
        } catch (NoTableException e) {
            LogUtil.e(e.toString());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
