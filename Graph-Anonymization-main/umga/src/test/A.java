/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

/**
 *
 * @author jcasasr
 */
abstract class A {
    private int a;
    
    public A (int a) {
        this.a = a;
    }
    
    public int getA() {
        return a;
    }
    
    public void doSomething() {
        doOther();
    }
    
    abstract void doOther();
}
