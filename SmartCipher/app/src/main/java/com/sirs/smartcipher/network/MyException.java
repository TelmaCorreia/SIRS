package com.sirs.smartcipher.network;

/**
 * Created by telma on 12/2/2017.
 */

class MyException extends Exception {
    private  String message;
    public MyException(String s) {
        message=s;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
