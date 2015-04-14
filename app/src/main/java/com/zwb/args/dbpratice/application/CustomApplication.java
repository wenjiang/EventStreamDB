package com.zwb.args.dbpratice.application;

import android.app.Application;

import com.zwb.args.dbpratice.cache.DatabaseCache;
import com.zwb.args.dbpratice.exception.NoTableException;
import com.zwb.args.dbpratice.util.LogUtil;

import java.util.Set;

/**
 * Created by pc on 2015/4/14.
 */
public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DatabaseCache cache = DatabaseCache.getInstance(this);
        Set<Class<?>> tableSet = cache.getTableSet();
        for (Class clazz : tableSet) {
            try {
                cache.readFromDb(clazz);
            } catch (NoTableException e) {
                LogUtil.e(e.toString());
            }
        }
    }
}
