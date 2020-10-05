package com.chenyixin.crud.test;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class Student   {

    private int age;
    private String name;

    @Override
    public String toString() {
        return "Student{" +
                "age=" + age +
                ", name='" + name + '\'' +
                '}';
    }

    public Student(String name, int age){


        this.age =age;
        this.name =name;

    }


}
