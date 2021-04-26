package com.patterndesign.observer;

public class ConcreteSubject extends Subject {

    @Override
    public void notify1() {
        for(Observer observer:observers) {
            observer.update();
        }
    }
}
