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

import org.apache.log4j.Logger;
import org.uoc.kison.UMGA.utils.ElapsedTime;
import org.uoc.kison.UMGA.utils.UtilsGraph;
import org.uoc.kison.objects.SimpleIntGraph;

/////////////////////////////////////////////////////////
// Univariant Microaggregation for Graph Anonymization //
/////////////////////////////////////////////////////////
public class UMGA {

    private static final Logger logger = Logger.getLogger(UMGA.class);
    UtilsGraph utilsGraph;

    public UMGA() {
        utilsGraph = new UtilsGraph();
    }

    /**
     * UMGA Algorithm
     */ 
    public SimpleIntGraph umga(SimpleIntGraph g, int k, String type, String typeSP, String edgeSelection) {

        ElapsedTime et = new ElapsedTime();
        
        logger.info(String.format("Original Graph: %d nodes, %d edges and K=%d.", g.getNumNodes(), g.getNumEdges(), utilsGraph.getKAnonymityValueFromGraph(g)));
        
        // degree sequence of G
        int[] d = utilsGraph.degree(g);

        /**
         * Step 1. anonymize the degree sequence
         */
        DegreeAnonimization da = new DegreeAnonimization();
        int[] dk = da.UMGA_deganon(d, k, type, typeSP);
        while (dk == null && k < g.getNumNodes()){
        	logger.warn("There was no solution for K="+k+", trying with new K="+(k+1));
        	k++;
        	dk = da.UMGA_deganon(d, k, type, typeSP);
        }

        /**
         * Step 2. modify original graph to anonymize it
         * Methods
         * - Random: select edges at random
         * - NC: select edges based on neighbourhood centrality
         */
        GraphReconstruction gr;
        if (edgeSelection.compareToIgnoreCase(GraphReconstructionByNC.PARAM) == 0) {
            // GraphReconstructionByNC
            gr = new GraphReconstructionByNC(g, dk);
        } else if (edgeSelection.compareToIgnoreCase(GraphReconstructionByNCLog.PARAM) == 0) {
            // GraphReconstructionByNCLog
            gr = new GraphReconstructionByNCLog(g, dk);
        } else if (edgeSelection.compareToIgnoreCase(GraphReconstructionByCoreness.PARAM) == 0) {
            // GraphReconstructionByCoreness
            gr = new GraphReconstructionByCoreness(g, dk);
        } else {
            // GraphReconstructionByRandom (default)
            gr = new GraphReconstructionByRandom(g, dk);
        }
        SimpleIntGraph gk = gr.UMGA_recons();

        logger.info("Anonimization process has finished!");
        logger.info("************************************************");
        // show results
        utilsGraph.edgeIntersection(g, gk);
        logger.info(String.format("Modified graph K=%d", utilsGraph.getKAnonymityValueFromDegreeSequence(utilsGraph.degree(gk))));

        et.stop();
        logger.info(String.format("Total running time: %s", et.getElapsedTime()));
        logger.info("************************************************");
        
        return gk;
    }
}
