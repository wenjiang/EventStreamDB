package com.zwb.args.dbpratice.application;

import android.content.Context;

import com.zwb.args.dbpratice.cache.DatabaseCache;
import com.zwb.args.dbpratice.db.SharedPreferencesManager;
import com.zwb.args.dbpratice.exception.NoTableException;
import com.zwb.args.dbpratice.util.LogUtil;

import java.util.Set;

/**
 * Created by pc on 2015/4/14.
 */
public class CustomApplication extends BaseMockApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        init(this);
    }

    @Override
    public void init(Context context) {
        DatabaseCache cache = DatabaseCache.getInstance(context);
        SharedPreferencesManager.init(context);
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
