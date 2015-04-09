package com.zwb.args.dbpratice.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.zwb.args.dbpratice.R;
import com.zwb.args.dbpratice.cache.DatabaseCache;
import com.zwb.args.dbpratice.event.EventStream;
import com.zwb.args.dbpratice.event.InsertEvent;
import com.zwb.args.dbpratice.exception.NoColumnChangeException;
import com.zwb.args.dbpratice.exception.NoRecordException;
import com.zwb.args.dbpratice.exception.NoTableException;
import com.zwb.args.dbpratice.exception.NoTagException;
import com.zwb.args.dbpratice.model.Status;
import com.zwb.args.dbpratice.util.LogUtil;


public class SampleActivity extends ActionBarActivity {
    private TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InsertEvent event = new InsertEvent();
        Status status = new Status();
        status.setName("张三");
        status.setId("01");
        try {
            event.to(Status.class).insert(status).commit("status_insert");
        } catch (NoTableException e) {
            LogUtil.e(e.toString());
        } catch (NoTagException e) {
            LogUtil.e(e.toString());
        }

        EventStream.getInstance().register(event);
        DatabaseCache cache = DatabaseCache.getInstance();

        tvName = (TextView) findViewById(R.id.tv_name);
        String name = null;
        try {
            name = cache.from(Status.class).where("id", "01").find("name", String.class);
        } catch (NoTagException e) {
            LogUtil.e(e.toString());
        } catch (NoColumnChangeException e) {
            LogUtil.e(e.toString());
        } catch (NoRecordException e) {
            LogUtil.e(e.toString());
        }
        tvName.setText(name);
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
