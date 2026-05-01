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
import org.apache.log4j.Logger;
import org.uoc.kison.UMGA.objects.Group;
import org.uoc.kison.UMGA.objects.OpsValue;

/**
 *
 * @author jcasasr
 */
public class ExhaustiveSearch {
    
    public static final String PARAM = "E";
    public static final String NAME = "Exhaustive search";
    private static final Logger logger = Logger.getLogger(ExhaustiveSearch.class);
    private long numCombinations = 0;
    private long maxCombinations = 0;
    private final long minNumCombinationsToShowPercen = Math.round(Math.pow(2, 20));
    
    public ExhaustiveSearch(long _iOps) {
        super();
        maxCombinations = Math.round(Math.pow(2, _iOps));
        
        logger.info(ExhaustiveSearch.NAME+" is selected.");
        logger.info(String.format("There are %d groups [2^%d = %d combinations].", _iOps, _iOps, maxCombinations));
    }
    
    public long getNumCombinations() {
        return numCombinations;
    }
    
    public long getMaxCombinations() {
        return maxCombinations;
    }
    
    /**
     * Exhaustive method
     */
    public OpsValue getOpsValue(Group[] groups, int[] ops) {
        OpsValue ret = getExhaustiveSearch(groups, ops, 0);
        
        logger.info(String.format("The solution has been found after %d iterations [%.2f %%]", numCombinations, ((double) numCombinations / (double) maxCombinations) * 100));
        
        return ret;
    }
    
    private OpsValue getExhaustiveSearch(Group[] groups, int[] ops, int currentValue) {
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
         * if 'yes' -> prepare the two new 'ops' and call recursive method again. 
         */
        if (indexToChange == -1) { // No -1's found
            int value = 0;
            for (int i = 0; i < ops.length; i++) {
                if (ops[i] == Group.ceilDiff) {
                    value += groups[i].getCeilDiffs();
                } else if (ops[i] == Group.floorDiff) {
                    value += groups[i].getFloorDiffs();
                }
            }
            if (value % 2 != 0) { //odd numbers are not allowed (cost = +Inf)
                value = Integer.MAX_VALUE;
            }
            
            // count called times
            numCombinations++;
            
            // print if it is larger than 'minNumCombinationsToShowPercen'
            if(maxCombinations >= minNumCombinationsToShowPercen) {
                for(double i=0.1; i <= 1; i=i+0.1) {
                    if(Math.round(maxCombinations*i)==numCombinations) 
                        logger.info(String.format("ExhaustiveSearch:: %d %% done!", Math.round(i*100)));
                }
            }

            if(value==0) {
                logger.info("The optimal solution was found!");
            }
            
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

            // compute the smallest value (greedy aprox.)
            OpsValue ret1;
            if(Math.abs(aVal) <= Math.abs(bVal)) {
                ret1 = getExhaustiveSearch(groups, aOps, aVal);
            } else {
                ret1 = getExhaustiveSearch(groups, bOps, bVal);
            }

            if(ret1.getValue()==0) {
                // if there is an optimal solution, we don't continue searching...
                return(ret1);
                
            } else {
                // if not, we continue searching...
                OpsValue ret2;
                if(Math.abs(aVal) <= Math.abs(bVal)) {
                    ret2 = getExhaustiveSearch(groups, bOps, bVal);
                } else {
                    ret2 = getExhaustiveSearch(groups, aOps, aVal);
                }

                // return the best
                if (Math.abs(ret1.getValue()) <= Math.abs(ret2.getValue())) {
                    // A is smaller or equal
                    return ret1;
                } else {
                    return ret2;
                }
            }
        }
    }
}
