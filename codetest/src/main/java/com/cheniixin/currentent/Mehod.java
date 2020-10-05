package com.cheniixin.currentent;

import java.util.concurrent.*;

public class Mehod {




    public static void main(String[] args) throws ExecutionException, InterruptedException {


        ExecutorService executor=Executors.newFixedThreadPool(10);

        Test test = new Test();

        CompletableFuture future = CompletableFuture.runAsync(()->{

                System.out.println("1 start");

                test.excute(2);

                System.out.println("1 end");



        },executor);

        future.runAsync(()->{

                System.out.println("2 start");

                test.excute(4);

                System.out.println("2 end");



        },executor);






    }








}
