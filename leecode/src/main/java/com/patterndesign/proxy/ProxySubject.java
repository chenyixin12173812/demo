package com.patterndesign.proxy;

public class ProxySubject implements Subject {

    private Subject subject;

    public ProxySubject(Subject subject) {
        this.subject = subject;
    }

    @Override
    public void request() {
        // do someThing
       subject.request();
       //do anther thing
    }
}
