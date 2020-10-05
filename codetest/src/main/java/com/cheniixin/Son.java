package com.cheniixin;

public class Son extends Parent {

    public   String age = "28";

    private   String  className = "2班";



    public Son( String age,String className){


        this.age =age;
        this.className=className;
    }



    public void printAge() {



        System.out.println("printAge" + age);
    }



}
