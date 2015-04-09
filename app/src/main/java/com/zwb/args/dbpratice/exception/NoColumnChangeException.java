package com.zwb.args.dbpratice.exception;

/**
 * Created by pc on 2015/4/9.
 */
public class NoColumnChangeException extends BaseException {
    public NoColumnChangeException(String info) {
        this.message = info;
    }
}
