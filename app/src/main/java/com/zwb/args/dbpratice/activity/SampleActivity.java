package com.zwb.args.dbpratice.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.zwb.args.dbpratice.R;
import com.zwb.args.dbpratice.cache.DatabaseCache;
import com.zwb.args.dbpratice.event.InsertEvent;
import com.zwb.args.dbpratice.event.UpdateEvent;
import com.zwb.args.dbpratice.exception.NoRecordException;
import com.zwb.args.dbpratice.exception.NoTableException;
import com.zwb.args.dbpratice.exception.NoTagException;
import com.zwb.args.dbpratice.model.Status;
import com.zwb.args.dbpratice.util.LogUtil;

import java.util.List;


public class SampleActivity extends ActionBarActivity {
    private TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int i = 0; i < 100; i++) {
            InsertEvent insertEvent = new InsertEvent();
            Status status = new Status();
            status.setName("张三");
            status.setId("01");
            try {
                insertEvent.to(Status.class).insert(status);
            } catch (NoTableException e) {
                LogUtil.e(e.toString());
            }
        }

        UpdateEvent updateEvent = new UpdateEvent();
        updateEvent.to(Status.class).where("id", "01").update("name", "李四");
        UpdateEvent updateEvent1 = new UpdateEvent();
        updateEvent1.to(Status.class).where("name", "李四").update("id", "02");

        DatabaseCache cache = DatabaseCache.getInstance();
        tvName = (TextView) findViewById(R.id.tv_name);
        List<Status> statusList = null;
        try {
            statusList = cache.from(Status.class).where("id", "02").find();
        } catch (NoTagException e) {
            LogUtil.e(e.toString());
        } catch (NoRecordException e) {
            LogUtil.e(e.toString());
        }

        tvName.setText(statusList.get(0).getName());
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
