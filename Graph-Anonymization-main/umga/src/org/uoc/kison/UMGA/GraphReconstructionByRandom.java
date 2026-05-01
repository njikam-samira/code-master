package org.uoc.kison.UMGA;

import java.util.ArrayList;
import org.uoc.kison.objects.SimpleIntGraph;

/**
 *
 * @author jcasasr
 */
public class GraphReconstructionByRandom extends GraphReconstruction {

    public static final String PARAM = "R";
    public static final String NAME = "Random";

    public GraphReconstructionByRandom(SimpleIntGraph g, int[] dk) {
        super(g, dk);

        logger.info(String.format("GraphReconstructionByRandom: Using method '%s'", NAME));
    }

    /****************
     * Edge Deletion *
     ****************/
    
    /**
     * Select the first random candidate
     *
     * @param 
     * @return True if edge deletion succeeded or false if not.
     */
    @Override
    protected boolean oneEdgeDeletion() {

        int i = 0;
        while (i < nodesToDeleteEdge.size()) {
            int vi = nodesToDeleteEdge.get(i);
            
            ArrayList<Integer> vks = gk.getEdges(vi);
            
            int j = i + 1;
            while (j < nodesToDeleteEdge.size()) {
                int vj = nodesToDeleteEdge.get(j);

                ArrayList<Integer> vls = gk.getEdges(vj);

                logger.debug(String.format("oneEdgeDeletion: deleting edges from nodes %d and %d", vi, vj));

                for (int vk : vks) {
                    for (int vl : vls) {
                        
                        if (vk != vl && !gk.getEdges(vk).contains(vl)) {
                            // delete edge
                            doEdgeDeletion(vi, vj, vk, vl, -1);

                            // number of tries
                            edgeDeletionNumTries++;
                    
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }

    /*****************
     * Edge Addition *
     *****************/
    
    /**
     * Select the first random candidate
     *
     * @param 
     * @return True if edge addition succeeded or false if not.
     */
    @Override
    protected boolean oneEdgeAddition() {
        
        int i = 0;
        while (i < nodesToAddEdge.size()) {
            
            int vi = nodesToAddEdge.get(i);
            
            int j = i + 1;
            while (j < nodesToAddEdge.size()) {
                
                int vj = nodesToAddEdge.get(j);

                if ((vi != vj) && (!gk.getEdges(vi).contains(vj))) {
                    // add new edge
                    doEdgeAddition(vi, vj, -1);

                    // number of tries
                    edgeAdditionNumTries++;
                    
                    return true;
                }
                j++;
            }
            i++;
        }

        return false;
    }

    /***************
     * Edge Switch *
     ***************/
    
    /**
     * Select the first random candidate
     *
     * @param vi source node
     * @return True if edge switch succeeded or false if not.
     */
    @Override
    protected boolean oneEdgeSwitch(Integer vi) {
        /**
         * Find all 'vk' (neighbors of 'vi') (vi,vk) \in E
         */
        ArrayList<Integer> vks = gk.getEdges(vi);
        logger.debug(String.format("oneEdgeSwitch: vks=%s", vks));

        for(int vk : vks) {
            for(int vj : nodesToAddEdge) {
                if (vi != vj && vk != vj && (!gk.getEdges(vj).contains(vk))) {
                    
                    // edge switch
                    doEdgeSwitch(vi, vk, vj, -1);

                    // number of tries
                    edgeSwitchNumTries++;
                    
                    return true;
                }
            }
        }

        return false;
    }
}
