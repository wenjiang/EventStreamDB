package com.zwb.args.dbpratice.application;

import android.app.Application;
import android.content.Context;

/**
 * Created by pc on 2015/6/2.
 */
public abstract class BaseMockApplication extends Application {

    public abstract void init(Context context);

    @Override
    public void onCreate() {
        super.onCreate();

        init(this);
    }
}
