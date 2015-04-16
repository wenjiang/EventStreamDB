package com.zwb.args.dbpratice.exception;

/**
 * Created by pc on 2015/4/14.
 */
public class NoSuchValueException extends BaseException {

    public NoSuchValueException(String info) {
        this.message = info;
    }
}
