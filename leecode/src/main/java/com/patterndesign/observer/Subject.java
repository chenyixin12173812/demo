package com.patterndesign.observer;

import java.util.ArrayList;
import java.util.List;

public abstract class Subject {

    protected List<Observer> observers= new ArrayList<>();
    public  void  attach(Observer observer) {
         observers.add(observer);
    }
    public  void  detach(Observer observer) {
        observers.remove(observer);
    }

    public  abstract void notify1();

}
