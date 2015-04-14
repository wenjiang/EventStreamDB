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
import com.zwb.args.dbpratice.event.UpdateEvent;
import com.zwb.args.dbpratice.exception.NoRecordException;
import com.zwb.args.dbpratice.exception.NoTableException;
import com.zwb.args.dbpratice.exception.NoTagException;
import com.zwb.args.dbpratice.model.Status;
import com.zwb.args.dbpratice.model.User;
import com.zwb.args.dbpratice.util.LogUtil;

import java.util.List;


public class SampleActivity extends ActionBarActivity {
    private TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int i = 0; i < 100; i++) {
            Status status = new Status();
            status.setName("转发");
            status.setStatusId("01");
            InsertEvent insertStatusEvent = new InsertEvent();
            User user = new User();
            user.setName("并送");
            user.setUserId("02");
            InsertEvent insertUserEvent = new InsertEvent();
            try {
                insertUserEvent.to(User.class).insert(user);
                insertStatusEvent.to(Status.class).insert(status);
            } catch (NoTableException e) {
                LogUtil.e(e.toString());
            }
        }

        UpdateEvent updateEvent = new UpdateEvent();
        updateEvent.to(Status.class).where("id", "01").update("name", "你好");
        DatabaseCache cache = DatabaseCache.getInstance(this);
        tvName = (TextView) findViewById(R.id.tv_name);
        List<Status> statusList = null;
        List<User> userList = null;
        try {
            long start = System.currentTimeMillis();
            statusList = cache.from(Status.class).where("statusId", "01").find();
            userList = cache.from(User.class).findAll();
            long end = System.currentTimeMillis();
            LogUtil.e("时间:" + (end - start) + ", 提取的长度:" + statusList.size());
            LogUtil.e("提取的user长度:" + userList.size() + ", 用户的名字:" + userList.get(0).getName());
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
