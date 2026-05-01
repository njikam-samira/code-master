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

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.uoc.kison.UMGA.utils.ConfigValues;
import org.uoc.kison.UMGA.utils.UtilsGraph;
import org.uoc.kison.objects.SimpleIntGraph;

abstract class GraphReconstruction {
    
    protected static final Logger logger = Logger.getLogger(GraphReconstruction.class);
    UtilsGraph utilsGraph;
    ConfigValues configValues;
    protected SimpleIntGraph g; // original graph
    protected SimpleIntGraph gk; // anonymized graph
    protected int[] dk;
    protected ArrayList<Integer> nodesToAddEdge;
    protected ArrayList<Integer> nodesToDeleteEdge;
    protected int edgeDeletionNumTries = 0;
    protected int edgeAdditionNumTries = 0;
    protected int edgeSwitchNumTries = 0;

    /**
     *
     * @param g: original graph
     * @param dk: k-degree anonymous sequence
     */
    public GraphReconstruction(SimpleIntGraph g, int[] dk) {
        this.utilsGraph = new UtilsGraph();
        this.configValues = new ConfigValues();
        this.g = utilsGraph.copyGraph(g);
        this.gk = utilsGraph.copyGraph(g);
        this.dk = dk;
    }

    /**
     * UMGA Step 2. Modification of graph structure in order to apply anonymized
     * degree sequence
     */
    public SimpleIntGraph UMGA_recons() {

        logger.info("Starting graph reconstruction...");

        // data from original graph
        int[] d0 = utilsGraph.degree(g);

        // change's vector
        int[] changesVector = new int[dk.length];
        for (int i = 0; i < changesVector.length; i++) {
            changesVector[i] = dk[i] - d0[i];
        }
        logger.debug("UMGA_recons: changesVector: " + Arrays.toString(changesVector));

        int lenChangesVector = changesVector.length;

        // vectors
        nodesToDeleteEdge = new ArrayList<Integer>(lenChangesVector);
        nodesToAddEdge = new ArrayList<Integer>(lenChangesVector);

        for (int i = 0; i < lenChangesVector; i++) {
            if (changesVector[i] < 0) {
                for (int j = 0; j < Math.abs(changesVector[i]); j++) {
                    nodesToDeleteEdge.add(i);
                }
            } else if (changesVector[i] > 0) {
                for (int j = 0; j < Math.abs(changesVector[i]); j++) {
                    nodesToAddEdge.add(i);
                }
            }
        }

        nodesToAddEdge.trimToSize();
        nodesToDeleteEdge.trimToSize();

        logger.debug(String.format("UMGA_recons: nodesToDeleteEdge (%d): %s", nodesToDeleteEdge.size(), Arrays.toString(nodesToDeleteEdge.toArray())));
        logger.debug(String.format("UMGA_recons: nodesToAddEdge (%d): %s", nodesToAddEdge.size(), Arrays.toString(nodesToAddEdge.toArray())));

        /**
         * Reconstruct Graph from dk
         */
        gk = reconstructGraph();
        
        logger.info(String.format("Original Graph  : %d nodes, %d edges and K=%d.", g.getNumNodes(), g.getNumEdges(), utilsGraph.getKAnonymityValueFromGraph(g)));
        logger.info(String.format("Anonimized Graph: %d nodes, %d edges and K=%d.", gk.getNumNodes(), gk.getNumEdges(), utilsGraph.getKAnonymityValueFromGraph(gk)));
        logger.info("Graph reconstruction done!");

        return gk;
    }
    
    public SimpleIntGraph reconstructGraph() {
        
        logger.info(String.format("reconstructGraph: Starting graph reconstruction... [numEdges=%d]", gk.getNumEdges()));
        
        int deleted = edgeDeletion();
        logger.info(String.format("reconstructGraph: Deleted %d edges [numEdges=%d]", deleted, gk.getNumEdges()));
        
        int added = edgeAddition();
        logger.info(String.format("reconstructGraph: Added %d edges [numEdges=%d]", added, gk.getNumEdges()));

        int changed = edgeSwitch();
        logger.info(String.format("reconstructGraph: Changed %d edges [numEdges=%d]", changed, gk.getNumEdges()));

        return gk;
    }
    
    /**
     * Step 1
     * Reduce 'nodesToDeleteEdge' if it is larger than 'nodesToAddEdge'
     */
    protected int edgeDeletion() {
        int removed = 0;
        
        logger.debug(String.format("edgeDeletion: *** Starting edge deletion..."));

        /** if 'nodesToDeleteEdge' > 'nodesToAddEdge' then add new edges */
        while (nodesToDeleteEdge.size() > nodesToAddEdge.size()) {

            if(!this.oneEdgeDeletion()) {
                /**
                 * TODO: create fake candidate
                 */
                
                logger.error("edgeDeletion: NO EDGE HAS BEEN REMOVED!");
                // exit on error?
                if(configValues.getExitOnEdgeModificationError()) {
                    System.exit(0);
                }
            } else {               
                removed ++;
            }
        }

        logger.info(String.format("edgeDeletion: number of tries = %d", edgeDeletionNumTries));

        return removed;
    }
    
    abstract boolean oneEdgeDeletion();
    
    protected void doEdgeDeletion(Integer vi, Integer vj, Integer vk, Integer vl, float score) {
        logger.debug(String.format("doEdgeDeletion: deleting edge: (%d,%d),(%d,%d) -> (%d,%d) [%f] ", vi, vk, vj, vl, vk, vl, score));
        
        int numEdgesBefore = gk.getNumEdges();
        
        gk.deleteEdge(vi, vk);
        gk.deleteEdge(vk, vi);

        gk.deleteEdge(vj, vl);
        gk.deleteEdge(vl, vj);

        gk.addEdge(vk, vl);
        gk.addEdge(vl, vk);
        
        // remove edges from 'nodesToDeleteEdge'
        nodesToDeleteEdge.remove(vi);
        nodesToDeleteEdge.remove(vj);
        
        int numEdgesAfter = gk.getNumEdges();
        
        logger.debug(String.format("doEdgeDeletion: done! number of edges %d -> %d", numEdgesBefore, numEdgesAfter));
        
        if((numEdgesBefore - 2) != numEdgesAfter) {
            // Wrong number of edges! Aborting...
            logger.error(String.format("doEdgeDeletion: WRONG NUMBER OF EDGES!"));
            // exit on error?
            if(configValues.getExitOnEdgeModificationError()) {
                System.exit(0);
            }
        }
    }
    
    /**
     * Step 2
     * Reduce 'nodesToAddEdge' if it is larger than 'nodesToDeleteEdge'
     */
    protected int edgeAddition() {
        int added = 0;
        
        logger.debug(String.format("edgeAddition: *** Starting edge addition..."));

        /** if 'nodesToAddEdge' > 'nodesToDeleteEdge' then add new edges */
        while (nodesToDeleteEdge.size() < nodesToAddEdge.size()) {

            // edge addition
            if(!this.oneEdgeAddition()) {
                /**
                 * TODO: create fake candidate
                 */
                
                logger.error("edgeAddition: NO EDGE HAS BEEN ADDED!");
                // exit on error?
                if(configValues.getExitOnEdgeModificationError()) {
                    System.exit(0);
                }
            } else {
                
                added ++;
            }
        }

        logger.info(String.format("edgeAddition: number of tries = %d", edgeAdditionNumTries));

        return added;
    }
    
    abstract boolean oneEdgeAddition();
    
    protected void doEdgeAddition(Integer vi, Integer vj, float score) {
        logger.debug(String.format("doEdgeAddition: adding edge: (%d,%d) [%f] ", vi, vj, score));
        
        // edge addition
        gk.addEdge(vi, vj);
        gk.addEdge(vj, vi);

        nodesToAddEdge.remove(vi);
        nodesToAddEdge.remove(vj);
    }
    
    /**
     * Step 3
     * Change edges to reduce/increase degree of nodes
     */
    protected int edgeSwitch() {
        
        int changed = 0;
        int n = gk.getNumEdges();
        
        logger.debug(String.format("edgeSwitch: *** Starting edge switch..."));

        /** Iterate over all 'vi' */
        while (nodesToDeleteEdge.size() > 0) {

            // select and remove 'vi'
            Integer vi = nodesToDeleteEdge.get(0);
            logger.debug(String.format("edgeSwitch: +++ processing vi=%d", vi));

            // edge switch on 'vi'
            if(!this.oneEdgeSwitch(vi)) {
                /**
                 * TODO: create fake candidate
                 */
                
                logger.error("edgeSwitch: NO EDGE HAS BEEN SWITCHED!");
                // exit on error?
                if(configValues.getExitOnEdgeModificationError()) {
                    System.exit(0);
                }
            } else {
                // Ok, edge switch applied! 
                // delete 'vi' from nodesToDeleteEdge
                nodesToDeleteEdge.remove(new Integer(vi));                
                changed ++;
            }
            
            // verification of number of edges
            if (n != gk.getNumEdges()) {
                logger.error(String.format("edgeSwitch: ERROR: number of edges has changed! [%d -> %d]", n, gk.getNumEdges()));
                // exit on error?
                if(configValues.getExitOnEdgeModificationError()) {
                    System.exit(0);
                }
            }
        }

        logger.info(String.format("edgeSwitch  : number of tries = %d", edgeSwitchNumTries));

        return changed;
    }
    
    abstract boolean oneEdgeSwitch(Integer vi);
    
    protected void doEdgeSwitch(Integer vi, Integer vk, Integer vj, float score) {
        logger.debug(String.format("doEdgeSwitch: edge switch: (%d,%d) -> (%d,%d) [%f] ", vi, vk, vk, vj, score));

        // edge switch
        gk.deleteEdge(vi, vk);
        gk.deleteEdge(vk, vi);
        gk.addEdge(vk, vj);
        gk.addEdge(vj, vk);

        nodesToAddEdge.remove(vj);
    }
}