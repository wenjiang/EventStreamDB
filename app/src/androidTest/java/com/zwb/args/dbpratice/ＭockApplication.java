package com.zwb.args.dbpratice;

import android.content.Context;

import com.zwb.args.dbpratice.application.BaseMockApplication;
import com.zwb.args.dbpratice.util.LogUtil;

/**
 * Created by pc on 2015/6/1.
 */
public class ＭockApplication {
    private ＭockApplication() {
    }

    public static void init(Class<?> clazz, Context context) {
        try {
            BaseMockApplication application = (BaseMockApplication) clazz.newInstance();
            application.init(context);
        } catch (InstantiationException e) {
            LogUtil.e(e.toString());
        } catch (IllegalAccessException e) {
            LogUtil.e(e.toString());
        }
    }
}
