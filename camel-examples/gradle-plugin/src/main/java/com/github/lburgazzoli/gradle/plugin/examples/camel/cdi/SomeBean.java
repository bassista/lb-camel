package com.github.lburgazzoli.gradle.plugin.examples.camel.cdi;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("counterBean")
public class SomeBean {

    private int counter;

    public String someMethod(String body) {
        return "Saying Hello World " + ++counter + " times";
    }

}