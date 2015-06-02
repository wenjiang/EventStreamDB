package com.zwb.args.dbpratice;

import android.test.AndroidTestCase;

import com.zwb.args.dbpratice.application.CustomApplication;

/**
 * Created by pc on 2015/6/1.
 */
public class DbTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ＭockApplication.init(CustomApplication.class, getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDbCreate() {

    }
}
