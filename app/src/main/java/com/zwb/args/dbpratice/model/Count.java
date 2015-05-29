package com.zwb.args.dbpratice.model;

import com.zwb.args.dbpratice.annotation.Column;
import com.zwb.args.dbpratice.annotation.Table;

/**
 * Created by pc on 2015/5/17.
 */
@Table(table = "count")
public class Count extends BaseTable {
    @Column
    private String name;
}
