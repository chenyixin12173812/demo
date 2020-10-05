package com.chenyixin.crud.test;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class ClassRoom extends WeakReference<Object> {

    public Student student;

    private String teacher;



    public ClassRoom(Student student, String teacher, ReferenceQueue<Object> queue){

        super(student,queue);
        this.student =student;
        this.teacher =teacher;

    }

    @Override
    public String toString() {
        return "ClassRoom{" +
                "student=" + student +
                ", teacher='" + teacher + '\'' +
                '}';
    }
}
