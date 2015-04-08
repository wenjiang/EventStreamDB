package com.zwb.args.dbpratice.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.zwb.args.dbpratice.R;
import com.zwb.args.dbpratice.cache.DatabaseCache;
import com.zwb.args.dbpratice.event.EventStream;
import com.zwb.args.dbpratice.event.UpdateEvent;
import com.zwb.args.dbpratice.exception.NoTableException;
import com.zwb.args.dbpratice.exception.NoTagException;
import com.zwb.args.dbpratice.model.Status;

import java.util.List;


public class SampleActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UpdateEvent event = new UpdateEvent();
        try {
            event.to(Status.class).update("name", "张三").commit("status_name_update");
        } catch (NoTableException e) {
            Log.e("SampleActivity", e.toString());
        } catch (NoTagException e) {
            Log.e("SampleActivity", e.toString());
        }

        EventStream.getInstance().register(event);
        DatabaseCache cache = DatabaseCache.getInstance();
        try {
            List<Status> statusList = cache.from(Status.class).find("name");
            for (Status status : statusList) {
                Log.e("SampleActivity", status.getName());
            }
        } catch (NoTableException e) {
            Log.e("DatabaseCache", e.toString());
        }
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
