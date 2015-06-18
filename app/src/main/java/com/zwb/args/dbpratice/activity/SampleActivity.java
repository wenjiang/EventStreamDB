package com.zwb.args.dbpratice.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.zwb.args.dbpratice.R;
import com.zwb.args.dbpratice.cache.DatabaseCache;
import com.zwb.args.dbpratice.event.InsertEvent;
import com.zwb.args.dbpratice.exception.NoRecordException;
import com.zwb.args.dbpratice.exception.NoSuchTableException;
import com.zwb.args.dbpratice.exception.NoTableException;
import com.zwb.args.dbpratice.exception.NoTagException;
import com.zwb.args.dbpratice.model.Status;
import com.zwb.args.dbpratice.util.LogUtil;

import java.util.ArrayList;
import java.util.List;


public class SampleActivity extends ActionBarActivity {
    private TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Status> statuses = new ArrayList<Status>();
        for (int i = 0; i < 10; i++) {
            Status status = new Status();
            status.setName("吸金好");
            status.setStatusId("01");
            statuses.add(status);
        }

        Status status = new Status();
        status.setName("你好");
        status.setStatusId("02");

        InsertEvent insertEvent1 = new InsertEvent();
        InsertEvent insertEvent = new InsertEvent();
        try {
            insertEvent.to(Status.class).insertAll(statuses);
            insertEvent1.to(Status.class).insert(status);
        } catch (NoTableException e) {
            LogUtil.e(e.toString());
        }

        DatabaseCache cache = DatabaseCache.getInstance();
        try {
            cache.insertToDb(Status.class);
            cache.insertToDb(Status.class);
        } catch (NoTagException e) {
            LogUtil.e(e.toString());
        } catch (NoRecordException e) {
            LogUtil.e(e.toString());
        } catch (NoSuchTableException e) {
            LogUtil.e(e.toString());
        } catch (NoTableException e) {
            LogUtil.e(e.toString());
        }
        tvName = (TextView) findViewById(R.id.tv_name);
        List<Status> statusList = null;
        try {
            long start = System.currentTimeMillis();
            statusList = cache.from(Status.class).findAll();
            long end = System.currentTimeMillis();
            LogUtil.e("时间:" + (end - start) + ", 提取的长度:" + statusList.size());
            for (Status s : statusList) {
                LogUtil.e("id:" + s.getStatusId() + ", name:" + s.getName());
            }
        } catch (NoTagException e) {
            LogUtil.e(e.toString());
        } catch (NoRecordException e) {
            LogUtil.e(e.toString());
        } catch (NoTableException e) {
            LogUtil.e(e.toString());
        }

        tvName.setText(statusList.get(0).getName());
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
