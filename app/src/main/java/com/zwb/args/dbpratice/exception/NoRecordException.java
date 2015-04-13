package com.zwb.args.dbpratice.exception;

/**
 * 该异常会在没有数据的时候抛出
 * Created by pc on 2015/4/9.
 */
public class NoRecordException extends BaseException {
    public NoRecordException(String info) {
        this.message = info;
    }
}
