/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uoc.kison.UMGA.metrics;

import java.util.HashSet;
import java.util.Set;
import org.uoc.kison.objects.SimpleIntGraph;

/**
 *
 * @author jcasasr
 */
public class EdgeNeighbourhoodCentrality {
    
    public float getEdgeNeighbourhoodCentrality(SimpleIntGraph gk, int i, int j) {
        Set<Integer> Ni = new HashSet<Integer>(gk.getEdges(i));
        Set<Integer> Nj = new HashSet<Integer>(gk.getEdges(j));
        
        int orSet = getSetCardinalityUnion(Ni, Nj);
        int andSet = getSetCardinalityIntersection(Ni, Nj);
        
        return (orSet - andSet) / (float) (2 * gk.getDegreeMax());
    }
    
    private int getSetCardinalityUnion(Set<Integer> set1, Set<Integer> set2) {
        // initiate 's' with elements of 'set1'
        Set<Integer> s = new HashSet<Integer>(set1);
        // add elements from 'set2'
        for(Integer i : set2) {
            s.add(i);
        }
        return s.size();
    }
    
    /**
     * Intersection = size(A)+size(B)-Union(A,B)
     * @param set1
     * @param set2
     * @return cardinality of the intersection A,B
     */
    private int getSetCardinalityIntersection(Set<Integer> set1, Set<Integer> set2) {
        int total = set1.size() + set2.size();
        int union = getSetCardinalityUnion(set1, set2);
        return total - union;
    }
}
