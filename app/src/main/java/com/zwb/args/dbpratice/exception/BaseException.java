package com.zwb.args.dbpratice.exception;

/**
 * Created by pc on 2015/4/8.
 */
public class BaseException extends Exception {
    protected String message;

    public String toString() {
        return message;
    }
}
