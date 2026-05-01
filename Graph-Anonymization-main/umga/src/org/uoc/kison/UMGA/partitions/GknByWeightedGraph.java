/*
 * Copyright 2013 Jordi Casas-Roma, Alexandre Dotor Casals
 * 
 * This file is part of UMGA. 
 * 
 * UMGA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * UMGA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with UMGA.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.uoc.kison.UMGA.partitions;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.uoc.kison.UMGA.objects.Group;
import org.uoc.kison.UMGA.utils.Memory;

/**
 *
 * @author jcasasr
 */
public class GknByWeightedGraph {
    
    public static final String PARAM = "S";
    public static final String NAME = "WeightedGraph";
    private static final Logger logger = Logger.getLogger(GknByWeightedGraph.class);
    private WeightedGraph sig;
    private List<Integer> sp;
    
    public GknByWeightedGraph(int k, int n, int[] V) {
        
        logger.info(GknByWeightedGraph.NAME+" is selected!");
        
        //////////////////////////////
        // create G(k,n)
        logger.info(String.format("Creating graph G(%d,%d)...", k, n));
        sig = new WeightedGraph(n+1, k);
        
        // show memory usage
        Memory mem = new Memory();
        mem.test();

        ////////////////////////////////////////
        // create edges (i,j)
        logger.info(String.format("Adding edges to G(%d,%d)...", k, n));
        
        for (int i = 0; i < n; i++) {
            for (int j = (i + k); j < (i + (2 * k)); j++) {
                if (j < n + 1) {
                    ////////////////////////////////
                    // compute L(i,j)

                    // C(i,j)
                    int[] C = new int[j - i];
                    for (int m = i + 1; m <= j; m++) {
                        C[m - (i + 1)] = m;
                    }

                    // M(i,j)
                    int[] M = new int[j - i];
                    for (int m = i + 1; m <= j; m++) {
                        M[m - (i + 1)] = V[m];
                    }

                    int average = M[Math.round(M.length / 2)];

                    // L(i,j)
                    int L = 0;
                    for (int m = i + 1; m <= j; m++) {
                        L += Math.abs(V[m] - average);
                    }
                    
                    // add edge
                    sig.setValue(i, j, L);
                }
            }
        }
    }
    
    public List<Integer> getShortestPath(int source, int target) {
        
        logger.info("Computing shortest path...");
        sp = sig.getShortestPath(source, target);
        logger.debug(String.format("Shortest path: %s - Value = %.2f", Arrays.toString(sp.toArray()), sig.getShortestPathLength()));
        
        return sp;
    }
    
    public Group[] getGroups(int[] V) {
        
        logger.info("Computing groups...");
        
        // nodes groups
        int[] Vreal = new int[V.length - 1];
        System.arraycopy(V, 1, Vreal, 0, V.length - 1); // removing first node (fake node = 0)
        
        Group[] groups = new Group[sp.size() - 1];
        
        for (int i = 0; i < sp.size() - 1; i++) {
            int a = sp.get(i);
            int b = sp.get(i+1);

            int[] values = new int[b - a];
            for (int j = 0; j < values.length; j++) {
                values[j] = Vreal[a + j];
            }

            groups[i] = new Group(values);
        }
        
        logger.debug("Groups: " + Arrays.toString(groups));
        
        return groups;
    }
}
