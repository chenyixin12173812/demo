package com.patterndesign.observer;

public class Client {

    public static void main(String[] args) {
        Subject  subject= new ConcreteSubject();
        subject.attach(new ConcreteObserver1());
        subject.attach(new ConcreteObserver2());
        subject.notify1();
    }

}
