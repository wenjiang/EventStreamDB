package com.zwb.args.dbpratice.exception;

/**
 * 该异常会在没有tag的情况下抛出
 * Created by pc on 2015/4/8.
 */
public class NoTagException extends BaseException {
    public NoTagException(String info) {
        message = info;
    }
}
