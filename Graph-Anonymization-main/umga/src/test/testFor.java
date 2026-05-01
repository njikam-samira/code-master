/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.ArrayList;

/**
 *
 * @author jcasasr
 */
public class testFor {
    public static void main(String[] args) {
        ArrayList<Integer> vks = new ArrayList<Integer>();
        vks.add(4);
        vks.add(2);
        vks.add(8);
        vks.add(19);
        
        for(int vk : vks) {
            System.out.println(vk);
        }
    }
}
