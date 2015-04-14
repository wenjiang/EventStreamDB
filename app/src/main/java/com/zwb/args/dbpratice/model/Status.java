package com.zwb.args.dbpratice.model;

import com.zwb.args.dbpratice.annotation.Column;
import com.zwb.args.dbpratice.annotation.Table;

/**
 * Created by pc on 2015/4/8.
 */
@Table(table = "status")
public class Status extends BaseTable {
    @Column
    private String name;
    @Column
    private String statusId;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setStatusId(String id) {
        this.statusId = id;
    }

    public String getStatusId() {
        return statusId;
    }
}
