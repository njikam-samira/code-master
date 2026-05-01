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
package org.uoc.kison.UMGA;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.uoc.kison.UMGA.objects.Group;
import org.uoc.kison.UMGA.objects.OpsValue;
import org.uoc.kison.UMGA.partitions.GknByWeightedGraph;
import org.uoc.kison.UMGA.search.ExhaustiveSearch;
import org.uoc.kison.UMGA.search.GreedySearch;
import org.uoc.kison.UMGA.utils.Memory;
import org.uoc.kison.UMGA.utils.UtilsGraph;

/**
 *
 * @author jcasasr
 */
public class DegreeAnonimization {

    private static final Logger logger = Logger.getLogger(DegreeAnonimization.class);
    UtilsGraph utilsGraph;

    public DegreeAnonimization() {
        super();
        this.utilsGraph = new UtilsGraph();
    }

    /**
     * UMGA Step 1. Anonymization of degree sequence
     */
    public int[] UMGA_deganon(int[] d0, int k, String search, String partitions) {

        // memory usage
        Memory mem = new Memory();

        int[] permut = utilsGraph.orderPermutation(d0);
        int[] V = new int[d0.length + 1];
        V[0] = 0;
        System.arraycopy(d0, 0, V, 1, d0.length);
        Arrays.sort(V);

        int n = d0.length;

        logger.info("Starting degree anonymization...");
        logger.debug("d0: " + Arrays.toString(d0));
        logger.debug("permut: " + Arrays.toString(permut));
        logger.debug("V: " + Arrays.toString(V));
        logger.info(String.format("n=%d; k=%d", n, k));

        // show memory usage
        mem.test();

        /**
         * Compute groups of k nodes in order to minimize the cost within group
         */
        Group[] groups;

        /**
         * GknByWeightedGraph
         */
        GknByWeightedGraph gknSIG = new GknByWeightedGraph(k, n, V);

        // show memory usage
        mem.test();

        gknSIG.getShortestPath(0, n);
        groups = gknSIG.getGroups(V);

        // show memory usage
        mem.test();

        /**
         * OPS to apply
         */
        logger.info("Computing best operations to apply...");
        int[] ops = new int[groups.length];
        int iOps = 0;
        for (int i = 0; i < groups.length; i++) {
            if (groups[i].hasOption()) {
                ops[i] = -1;
                iOps++;
            } else {
                ops[i] = 0;
            }
        }

        OpsValue oOpsToApply = null;
        if (search.compareToIgnoreCase(GreedySearch.PARAM) == 0) {
            /** Greedy search */
            GreedySearch gs = new GreedySearch(iOps);
            oOpsToApply = gs.getOpsValue(groups, ops);

        } else {
            /** Exhaustive search (default method) */
            ExhaustiveSearch es = new ExhaustiveSearch(iOps);
            oOpsToApply = es.getOpsValue(groups, ops);
        }

        // show memory usage
        mem.test();
        
        // There is no solution for this pair {Graph,k}
        if (oOpsToApply.getValue() == Integer.MAX_VALUE) return null;
        
        logger.info("Value of the best operation to apply is: " + oOpsToApply.getValue());
        ops = Arrays.copyOf(oOpsToApply.getOps(), oOpsToApply.getOps().length);
        logger.debug(String.format("oOpsToApply [value=%d]: %s", oOpsToApply.getValue(), Arrays.toString(ops)));

        // apply OPS to ordered degree sequence
        logger.info("Computing ordered degree sequence...");
        int[] orderedDegreeSequence = new int[n];
        int numGrups = groups.length;
        int position = 0;
        for (int i = 0; i < numGrups; i++) {
            int newValue = 0;
            if (ops[i] == Group.ceilDiff) {
                newValue = groups[i].getCeilMean();
            } else if (ops[i] == Group.floorDiff) {
                newValue = groups[i].getFloorMean();
            } else if (ops[i] == 0) {
                if (groups[i].getFloorMean() == groups[i].getCeilMean()) {
                    newValue = groups[i].getCeilMean();
                } else {
                    logger.error("Error processing 'opsToApply[" + i + "]==0'");
                }
            } else {
                logger.error("Unknown ops value: " + ops[i]);
            }
            int[] values = groups[i].getValues();
            for (int j = 0; j < values.length; j++, position++) {
                orderedDegreeSequence[position] = newValue;
            }
        }
        logger.debug("orderedDegreeSequence: " + Arrays.toString(orderedDegreeSequence));

        // re-order degree sequence
        logger.info("Computing degree sequence...");
        int[] degreeSequence = new int[n];
        for (int i = 0; i < n; i++) {
            degreeSequence[permut[i]] = orderedDegreeSequence[i];
        }
        logger.debug("degreeSequence: " + Arrays.toString(degreeSequence));

        // Print results
        int d0NumEdges = 0;
        for (int i = 0; i < d0.length; i++) {
            d0NumEdges += d0[i];
        }
        d0NumEdges /= 2;
        logger.info(String.format("D original [%d edges]", d0NumEdges));
        logger.debug(String.format("D original: %s", Arrays.toString(d0)));

        int dkNumEdges = 0;
        for (int i = 0; i < d0.length; i++) {
            dkNumEdges += degreeSequence[i];
        }
        dkNumEdges /= 2;
        logger.info(String.format("D Anonymized [%d edges]", dkNumEdges));
        logger.debug(String.format("D Anonymized: %s", Arrays.toString(degreeSequence)));

        int size = Math.max(d0.length, degreeSequence.length);
        int difs = 0;
        for (int i = 0; i < size; i++) {
            if (i < d0.length && i < degreeSequence.length) {
                difs += Math.abs(d0[i] - degreeSequence[i]);
            } else if (i >= d0.length) {
                difs += degreeSequence[i];
            } else if (i >= degreeSequence.length) {
                difs += d0[i];
            }
        }
        logger.info(String.format("Difs: %d [%.2f %%]", difs, (Double.valueOf(difs / 2) / d0NumEdges * 100)));

        int difsEdges = (dkNumEdges - d0NumEdges);
        logger.info(String.format("Difs of number of edges: %d edges [%s]", difsEdges, (difsEdges - Math.round(difsEdges) == 0) ? "OK" : "ERROR"));
        int kValue = utilsGraph.getKAnonymityValueFromDegreeSequence(degreeSequence);
        logger.info(String.format("k value = %d [%s]", kValue, kValue == k ? "OK" : "ERROR"));
        logger.info("Degree anonymization done!");

        return degreeSequence;
    }
}
