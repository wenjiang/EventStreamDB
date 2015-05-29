package com.zwb.args.dbpratice.model;

import com.zwb.args.dbpratice.annotation.Column;
import com.zwb.args.dbpratice.annotation.Key;
import com.zwb.args.dbpratice.annotation.Table;

/**
 * Created by pc on 2015/4/14.
 */
@Table(table = "user")
public class User extends BaseTable {
    @Column
    private String name;
    @Column
    @Key
    private String userId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
