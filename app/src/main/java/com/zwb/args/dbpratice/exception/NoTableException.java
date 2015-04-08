package com.zwb.args.dbpratice.exception;

/**
 * Created by pc on 2015/4/8.
 */
public class NoTableException extends BaseException {
    public NoTableException(String info) {
        message = info;
    }
}
