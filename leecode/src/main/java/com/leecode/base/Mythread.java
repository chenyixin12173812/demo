package com.leecode.base;

import java.util.concurrent.Callable;

public class Mythread implements Runnable {




    @Override
    public void run() {

        throw new  RuntimeException("my exception");
    }






    private void deal(Throwable thrown) {
    }
}
