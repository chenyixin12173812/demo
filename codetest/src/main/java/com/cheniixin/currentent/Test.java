package com.cheniixin.currentent;

public class Test {

    public void excute(int i){

        try {
            Thread.sleep(1000*i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }




}
