package org.uoc.kison.UMGA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.uoc.kison.UMGA.metrics.EdgeNeighbourhoodCentrality;
import org.uoc.kison.objects.SimpleIntGraph;

/**
 *
 * @author jcasasr
 */
public class GraphReconstructionByNC extends GraphReconstruction {

    public static final String PARAM = "NC";
    public static final String NAME = "Neighbourhood Centrality";

    public GraphReconstructionByNC(SimpleIntGraph g, int[] dk) {
        super(g, dk);

        logger.info(String.format("GraphReconstructionByNC: Edge selection method: '%s'", NAME));
    }
    
    /****************
     * Edge Deletion *
     ****************/
    
    /**
     * Select the best candidate based on their NC score
     *
     * @param 
     * @return True if edge deletion succeeded or false if not.
     */
    @Override
    protected boolean oneEdgeDeletion() {

        EdgeNeighbourhoodCentrality enc = new EdgeNeighbourhoodCentrality();
        HashMap<Float, String> map = new HashMap<Float, String>();

        /**
         * Delete duplicates from 'nodesToAddEdge'
         * Create a set and convert to ArrayList
         */
        ArrayList<Integer> nodesToDeleteEdge_withoutDuplicates = new ArrayList<Integer>(new TreeSet<Integer>(nodesToDeleteEdge));
        logger.debug(String.format("oneEdgeDeletion: nodesToDelEdge_withoutDuplicates=%s from %s", nodesToDeleteEdge_withoutDuplicates, nodesToDeleteEdge));

        // Iterate on 'vi'
        int i = 0;
        while (i < nodesToDeleteEdge_withoutDuplicates.size()) {

            // Get 'vi'
            int vi = nodesToDeleteEdge_withoutDuplicates.get(i);
            
            // Iterate on 'vj'
            int j = i + 1;
            while (j < nodesToDeleteEdge_withoutDuplicates.size()) {

                // Get 'vj'
                int vj = nodesToDeleteEdge_withoutDuplicates.get(j);
                logger.debug(String.format("oneEdgeDeletion: +++ considering vi=%d and vj=%d", vi, vj));

                // iterate on all vks / vls
                // (vi,vk) \in E
                ArrayList<Integer> vks = gk.getEdges(vi);
                logger.debug("vks=" + vks);
                // (vj,vl) \in E
                ArrayList<Integer> vls = gk.getEdges(vj);
                logger.debug("vls=" + vls);

                for (int vk : vks) {
                    for (int vl : vls) {
                        // (vk,vl) \not \in E
                        if ((vk != vl) && (!gk.getEdges(vk).contains(vl))) {
                            float enc_vivk = enc.getEdgeNeighbourhoodCentrality(gk, vi, vk);
                            float enc_vjvl = enc.getEdgeNeighbourhoodCentrality(gk, vj, vl);
                            float enc_vkvl = enc.getEdgeNeighbourhoodCentrality(gk, vk, vl);
                            float enc_total = enc_vivk + enc_vjvl + enc_vkvl;

                            logger.debug(String.format("oneEdgeDeletion: edge deletion: (%d,%d),(%d,%d) -> (%d,%d) [%f] ", vi, vk, vj, vl, vk, vl, enc_total));

                            // add to map
                            String name = vi + "-" + vj + "-" + vk + "-" + vl;
                            map.put(enc_total, name);

                            // number of tries
                            edgeDeletionNumTries++;
                        }
                    }
                }
                j++;
            }
            i++;
        }

        // select
        if (!map.isEmpty()) {
            TreeMap<Float, String> sortedHashMap = new TreeMap<Float, String>(map);
            float fk = sortedHashMap.firstKey();
            String fv = sortedHashMap.get(fk);
            String[] v = fv.split("-");
            Integer vi = Integer.parseInt(v[0]);
            Integer vj = Integer.parseInt(v[1]);
            Integer vk = Integer.parseInt(v[2]);
            Integer vl = Integer.parseInt(v[3]);

            // apply edge deletion
            doEdgeDeletion(vi, vj, vk, vl, -1);
            
            return true;

        } else {
            // warning, no edge has been deleted!
            return false;
        }
    }

    /*****************
     * Edge Addition *
     *****************/
    
    /**
     * Select the best candidate based on their NC score
     *
     * @param 
     * @return True if edge addition succeeded or false if not.
     */
    @Override
    protected boolean oneEdgeAddition() {
        EdgeNeighbourhoodCentrality enc = new EdgeNeighbourhoodCentrality();
        HashMap<Float, String> map = new HashMap<Float, String>();
        
        /**
         * Delete duplicates from 'nodesToAddEdge'
         * Create a set and convert to ArrayList
         */
        ArrayList<Integer> nodesToAddEdge_withoutDuplicates = new ArrayList<Integer>(new TreeSet<Integer>(nodesToAddEdge));
        logger.debug(String.format("oneEdgeAddition: nodesToAddEdge_withoutDuplicates=%s from %s", nodesToAddEdge_withoutDuplicates, nodesToAddEdge));

        int i = 0;
        while (i < nodesToAddEdge_withoutDuplicates.size()) {
            int vi = nodesToAddEdge_withoutDuplicates.get(i);

            int j = i + 1;
            while (j < nodesToAddEdge_withoutDuplicates.size()) {
                int vj = nodesToAddEdge_withoutDuplicates.get(j);

                if ((vi != vj) && (!gk.getEdges(vi).contains(vj))) {
                    float enc_vivj = enc.getEdgeNeighbourhoodCentrality(gk, vi, vj);

                    logger.debug(String.format("oneEdgeAddition: edge addition: (%d,%d) [%f] ", vi, vj, enc_vivj));

                    // add to map
                    String name = vi + "-" + vj;
                    map.put(enc_vivj, name);

                    // number of tries
                    edgeAdditionNumTries++;
                }
                j ++;
            }
            i ++;
        }

        // select
        if (!map.isEmpty()) {
           TreeMap<Float, String> sortedHashMap = new TreeMap<Float, String>(map);
           float fk = sortedHashMap.firstKey();
           String fv = sortedHashMap.get(fk);
           Integer vi = Integer.parseInt(fv.substring(0, fv.indexOf("-")));
           Integer vj = Integer.parseInt(fv.substring(fv.indexOf("-") + 1, fv.length()));

           // apply edge addition
           doEdgeAddition(vi, vj, fk);
           
           return true;

        } else {
           // warning, no new edge has been created!
           return false;
        }
    }

    /***************
     * Edge Switch *
     ***************/
    
    /**
     * Evaluate NC on all candidates and select the best one
     * @param vi source node
     * @return True if edge switch succeeded or false if not.
     */
    @Override
    protected boolean oneEdgeSwitch(Integer vi) {
        // create space to save the score for all possible edge switch
        HashMap<Float, String> map = new HashMap<Float, String>();
        EdgeNeighbourhoodCentrality enc = new EdgeNeighbourhoodCentrality();
        
        /**
         * Find all 'vk' (neighbors of 'vi') 
         * (vi,vk) \in E
         */
        ArrayList<Integer> vks = gk.getEdges(vi);
        logger.debug(String.format("oneEdgeSwitch: vks=%s", vks));

        /**
         * Iterate over all 'vj' 
         * Convert 'nodesToAddEdge' to a set in order to avoid duplicate elements
         */
        Set<Integer> vjs = new TreeSet<Integer>(nodesToAddEdge);
        logger.debug(String.format("oneEdgeSwitch: vjs=%s from %s", vjs, nodesToAddEdge));

        for (int vj : vjs) {

            logger.debug(String.format("oneEdgeSwitch: considering vj=%d...", vj));

            // iterate throught all vk's
            for (int vk : vks) {
                // (vk,vj) \not \in E
                if (vk != vj && (!gk.getEdges(vj).contains(vk))) {
                    float enc_vivk = enc.getEdgeNeighbourhoodCentrality(gk, vi, vk);
                    float enc_vkvj = enc.getEdgeNeighbourhoodCentrality(gk, vk, vj);
                    float enc_total = enc_vivk + enc_vkvj;
                    logger.debug(String.format("oneEdgeSwitch: candidate: (%d,%d) -> (%d,%d) [%f] ", vi, vk, vk, vj, enc_total));

                    // add to map
                    String name = vj + "-" + vk;
                    map.put(enc_total, name);
                    
                    // number of tries
                    edgeSwitchNumTries++;
                }
            }
        }

        // select
        if (!map.isEmpty()) {
            TreeMap<Float, String> sortedHashMap = new TreeMap<Float, String>(map);
            float fk = sortedHashMap.firstKey();
            String fv = sortedHashMap.get(fk);
            Integer vj = Integer.parseInt(fv.substring(0, fv.indexOf("-")));
            Integer vk = Integer.parseInt(fv.substring(fv.indexOf("-") + 1, fv.length()));

            // apply edge switch
            doEdgeSwitch(vi, vk, vj, fk);
            
        } else {
            // No edge switch could be found
            return false;
        }

        return true;
    }
}
