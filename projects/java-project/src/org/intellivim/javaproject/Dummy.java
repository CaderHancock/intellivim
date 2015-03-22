package org.intellivim.javaproject;

import java.util.ArrayList;

/**
 * Some dummy class for testing purposes
 * @author dhleong
 */
public class Dummy {
    public void boring() {
        System.out.println("Hi");
        new ArrayList<String>();
        new Dummy().fluid().boring();
        ArrayList<String> list = new ArrayList<String>();
        list.add("hi");
        notBoring(42);
    }

    /** I promise it's not boring */
    public void notBoring(int number) {
        Problematic problem;
    }

    void notBoring(int number, String foo) {}

    public Dummy fluid() {
        return this;
    }

    /* Constructors last so existing tests with offsets aren't broken */

    Dummy() {
    }
    Dummy(int number) {
    }
    Dummy(String string) {
    }
    Dummy(int number, String andString) {
    }

    /* New code for testing params */

    void moreBoring() {
        notBoring(42, "foo");
        notBoring(answerQuestion("life"), "universe");
    }

    static int answerQuestion(String question) {
        return 42;
    }

}
