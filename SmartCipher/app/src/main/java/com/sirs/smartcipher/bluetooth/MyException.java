package com.sirs.smartcipher.bluetooth;

/**
 * Created by telma on 12/4/2017.
 */

public class MyException extends Exception {
    String message;

    public MyException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
