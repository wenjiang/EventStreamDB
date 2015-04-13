package com.zwb.args.dbpratice.exception;

/**
 * 该异常会在没有表的情况下抛出
 * Created by pc on 2015/4/8.
 */
public class NoTableException extends BaseException {
    public NoTableException(String info) {
        message = info;
    }
}
