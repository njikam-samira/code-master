package org.uoc.kison.UMGA;

import org.uoc.kison.objects.SimpleIntGraph;
import interviews.graphs.Graph;
import interviews.graphs.Edge;
import interviews.graphs.KCore;
import java.util.ArrayList;

/**
 *
 * @author jcasasr
 */
public class GraphReconstructionByCoreness extends GraphReconstruction {

    private Graph graph;
    private KCore kCore;
    
    public static final String PARAM = "C";
    public static final String NAME = "Coreness";

    public GraphReconstructionByCoreness(SimpleIntGraph g, int[] dk) {
        super(g, dk);
        
        // Convert 'SimpleIntGraph' to 'Graph'
        this.convertSimpleIntGraphToGraph();
        // initialize 'KCore' as unweighted graph
        this.kCore = new KCore(this.graph);
        this.kCore.computeUnweighted();
        
        logger.info(String.format("GraphReconstructionByCoreness: Using method '%s'", NAME));
    }
    
    /**
     * convert 'g' to 'graph'
     */
    private void convertSimpleIntGraphToGraph() {
        // create an empty graph
        this.graph = new Graph(this.g.getNumNodes());
        
        // add edges
        for(int i=0; i<g.getNumNodes(); i++) {
            for(int j: g.getEdges(i)) {
                /**
                 * TODO: pot donar problemes quan el graf comença al node 0?
                 * En principi aquesta condició no hauria de provocar problemes...
                 */
                if(j < g.getNumNodes()) {
                    graph.addEdge(i, j);
                }
            }
        }
    }
    
    @Override
    public SimpleIntGraph reconstructGraph() {
        
        // random shuffle
        this.utilsGraph.shuffleArray(this.nodesToAddEdge);
        this.utilsGraph.shuffleArray(this.nodesToDeleteEdge);
        
        // edge modification process
        logger.info(String.format("reconstructGraph: Starting graph reconstruction... [numEdges=%d]", gk.getNumEdges()));
        
        int deleted = edgeDeletion();
        logger.info(String.format("reconstructGraph: Deleted %d edges [numEdges=%d]", deleted, gk.getNumEdges()));
        
        int added = edgeAddition();
        logger.info(String.format("reconstructGraph: Added %d edges [numEdges=%d]", added, gk.getNumEdges()));

        int changed = edgeSwitch();
        logger.info(String.format("reconstructGraph: Changed %d edges [numEdges=%d]", changed, gk.getNumEdges()));

        // convert 'Graph' to 'SimpleIntGraph'
        convertGraphToSimpleIntGraph();
        
        return gk;
    } 
    
    /**
     * convert 'graph' to 'gk'
     */
    private void convertGraphToSimpleIntGraph() {
        // create an empty SimpleIntGraph
        this.gk = new SimpleIntGraph(graph.V);
        
        // add edges
        for(Edge e: graph.edges()) {
            this.gk.addEdge(e.v, e.w);
            this.gk.addEdge(e.w, e.v);
        }
    }
    
    /**
     * Step 1
     * Select the first random candidate which fulfills the coreness-preserving
     *
     * @param 
     * @return True if edge deletion succeeded or false if not.
     */
    @Override
    protected boolean oneEdgeDeletion() {

        for(int i=0; i<nodesToDeleteEdge.size(); i++) {
            int vi = nodesToDeleteEdge.get(i);
            
            for(Edge e : graph.adjE(vi)) {
                // e=(vi, vj)
                if((e.v == vi && nodesToDeleteEdge.contains(e.w)) 
                        || (e.w == vi && nodesToDeleteEdge.contains(e.v))) {

                    // number of tries
                    edgeDeletionNumTries++;

                    // test coreness
                    if(this.graph.removeEdge(e)) {
                        // remove edges from 'nodesToDeleteEdge'
                        nodesToDeleteEdge.remove(new Integer(e.v));
                        nodesToDeleteEdge.remove(new Integer(e.w));
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Step 2
     * Reduce 'nodesToAddEdge' if it is larger than 'nodesToDeleteEdge'
     */
    @Override
    protected boolean oneEdgeAddition() {
        
        for(int i=0; i<nodesToAddEdge.size(); i++) {
            int vi = nodesToAddEdge.get(i);
            
            for(int j=i+1; j<nodesToAddEdge.size(); j++) {
                int vj = nodesToAddEdge.get(j);
                
                logger.info(String.format("Trying edge (%s,%s)...", vi, vj));
                
                // no self-loops
                if(vi != vj) {
                    Edge e = new Edge(vi, vj);

                    // e=(vi, vj) exist?
                    if(!this.graph.contains(e)) {

                        // number of tries
                        edgeAdditionNumTries++;

                        // test coreness
                        if(this.kCore.addEdge(e)) {
                            // remove edges from 'nodesToAddEdge'
                            nodesToAddEdge.remove(new Integer(e.v));
                            nodesToAddEdge.remove(new Integer(e.w));

                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Step 3
     * Change edges to reduce/increase degree of nodes
     */
    @Override
    protected boolean oneEdgeSwitch(Integer vi) {
        
        // Find all 'vk' (neighbors of 'vi') (vi,vk) \in E
        ArrayList<Integer> vks = new ArrayList<Integer>();
        for(int v : this.graph.adjV(vi)) {
            vks.add(v);
        }
        logger.debug(String.format("oneEdgeSwitch: vks=%s", vks));

        for(int k=0; k<vks.size(); k++) {
            int vk = vks.get(k);
            
            for(int j=0; j<nodesToAddEdge.size(); j++) {
                int vj = nodesToAddEdge.get(j);
                if (vi != vj && vk != vj && (!this.graph.containsEdge(vj, vk))) {
                    
                    // number of tries
                    edgeSwitchNumTries++;
                    
                    // edge switch
                    // (v_k, v_i) -> (v_k, v_j)
                    if(this.kCore.switchEdge(vi, vj, vk)) {
                        
                        nodesToAddEdge.remove(new Integer(vj));
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
