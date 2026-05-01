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
package org.uoc.kison.UMGA.search;

import java.util.Arrays;
import java.util.Random;
import org.apache.log4j.Logger;
import org.uoc.kison.UMGA.objects.Group;
import org.uoc.kison.UMGA.objects.OpsValue;

/**
 *
 * @author jcasasr
 */
public class GreedySearch {
    
    public static final String PARAM = "G";
    public static final String NAME = "Greedy search";
    private static final Logger logger = Logger.getLogger(GreedySearch.class);
    private final double SOLUTION_MAX_ITER_NO_CHANGE = 0.1;
    private long numCombinations = 0;
    private long maxCombinations = 0;
    private long maxIterationNoImproSol = 0;
    
    public GreedySearch(long iOps) {
        super();
        
        maxCombinations = iOps<=63?Math.round(Math.pow(2, iOps)):Long.MAX_VALUE;
        maxIterationNoImproSol = Math.round(Math.min(SOLUTION_MAX_ITER_NO_CHANGE * maxCombinations, Math.pow(2,20)));
        
        logger.info(GreedySearch.NAME+" is selected.");
        logger.info(String.format("There are %d groups [2^%d = %d combinations].", iOps, iOps, maxCombinations));
        logger.info(String.format("Max number of iterations without improving the solution is %d", maxIterationNoImproSol));
    }
    
    public long getNumCombinations() {
        return numCombinations;
    }
    
    /**
     * Exhaustive method
     */
    public OpsValue getOpsValue(Group[] groups, int[] ops) {
        boolean minFound = false;
        boolean maxIterationReached = false;
        long iteration = 0; // number of total iterations
        long iterationNoImproSol = 0; // number of iterations without improving the solution
        OpsValue minOps = null;
        
        while(!minFound && !maxIterationReached) {
            OpsValue tmpOps = getGreedySearch(groups, Arrays.copyOf(ops, ops.length), 0);
            
            // counter 
            iteration ++;
            iterationNoImproSol ++;
            
            if(minOps == null || tmpOps.getAbsValue() < minOps.getAbsValue()) {
                // new value is better than the old one
                minOps = tmpOps;
                // reset counter 
                iterationNoImproSol = 0;
            }
            
            // verification of optimal value
            if(minOps.getAbsValue()==0) {
                // optimal value found!
                minFound = true;
            }
            
            // verification of number of iterations
            if(iterationNoImproSol >= maxIterationNoImproSol) {
                maxIterationReached = true;
            }
        }
        
        if(minFound) {
            logger.info(String.format("The optimal solution has been found after %d iterations [%.2f %%]", iteration, ((double) iteration / (double) maxCombinations) * 100));
        } else {
            logger.info(String.format("%d iterations have been tested... last %d iterarions without improving the soluton!", iteration, iterationNoImproSol));
        }
        
        return minOps;
    }
            
    private OpsValue getGreedySearch(Group[] groups, int[] ops, int currentValue) {
        int indexToChange = -1;
        
        /** is there any '-1' in the 'ops' vector? */
        for (int i = 0; i < ops.length; i++) {
            if (ops[i] == -1) {
                indexToChange = i;
                break;
            }
        }
        
        /**
         * if 'no'  -> compute the final value and return it
         * if 'yes' -> prepare the a new 'ops' and call recursive method again. 
         */
        if (indexToChange == -1) { // No -1's found
            int value = 0;
            for (int i = 0; i < ops.length; i++) {
                if (ops[i] == Group.ceilDiff) {
                    value += groups[i].getCeilDiffs();
                } else if (ops[i] == Group.floorDiff) {
                    value += groups[i].getFloorDiffs();
                } else if (ops[i] != 0) {
                    logger.error("GreedySearch::getGreedySearch: Error on ops[i] value = "+ops[i]);
                    logger.error("Execution will be stopped!");
                    System.exit(-1);
                }
            }
            if (value % 2 != 0) { //odd numbers are not allowed (cost = +Inf)
                value = Integer.MAX_VALUE;
            }
            
            // count called times
            numCombinations ++;

            return new OpsValue(ops, value);
            
        } else {
            // option A - 'floor'
            int[] aOps = Arrays.copyOf(ops, ops.length);
            aOps[indexToChange] = Group.floorDiff;
            int aVal = currentValue + groups[indexToChange].getFloorDiffs();
            
            // option B - 'ceil'
            int[] bOps = ops;
            bOps[indexToChange] = Group.ceilDiff;
            int bVal = currentValue + groups[indexToChange].getCeilDiffs();

            /**
             * Compute the probability of select 'aOps' or 'bOps' based on their values (greedy aprox.)
             * Ex: va = 2, vb = 10
             * qa = 12/2=6
             * qb = 12/10=1.2
             * p = 1/(7.2)=0.1388
             * cutPoint = 6*p = 6*0.1388 = 0.8333
             * 
             * That is, between [0, 0.8333] the selected value will be 'aVal' (83%). And between [0.8334, 1] the selected value will be 'bVal' (16.6%)
             */ 
            double va = Math.abs(aVal);
            double vb = Math.abs(bVal);
            double t = va + vb;
            double qa = t / va;
            double qb = t / vb;
            double p = 1 / (qa + qb);
            double cutPoint = qa * p;
            
            OpsValue ret;
            Random rand = new Random();
            
            if(rand.nextDouble() <= cutPoint) {
                ret = getGreedySearch(groups, aOps, aVal);
            } else {
                ret = getGreedySearch(groups, bOps, bVal);
            }
            
            return(ret);
        }
    }
}
