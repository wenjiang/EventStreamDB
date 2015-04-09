package com.zwb.args.dbpratice.exception;

/**
 * Created by pc on 2015/4/9.
 */
public class NoRecordException extends BaseException {
    public NoRecordException(String info) {
        this.message = info;
    }
}
