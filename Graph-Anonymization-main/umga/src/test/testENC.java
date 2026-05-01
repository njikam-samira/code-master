package test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.uoc.kison.UMGA.metrics.EdgeNeighbourhoodCentrality;
import org.uoc.kison.objects.SimpleIntGraph;
import org.uoc.kison.parsers.GmlParser;

/**
 *
 * @author jcasasr
 */
public class testENC {
    
    public static void main(String[] args) {
        String file = "/Users/jcasasr/Dropbox/UAB-TESI/tesi/datasets/karate/karate.gml";
        GmlParser gmlParser = new GmlParser();
        SimpleIntGraph g = gmlParser.parseFile(file);
        
        System.out.println("Number of nodes: "+ g.getNumNodes());
        System.out.println("Max degree: "+ g.getDegreeMax());
        int n = g.getNumNodes();
        EdgeNeighbourhoodCentrality enc = new EdgeNeighbourhoodCentrality();
        for(int i=0; i<n; i++) {
            for(int j=i+1; j<n; j++) {
                float res = enc.getEdgeNeighbourhoodCentrality(g, i, j);
                System.out.println(" ENB ("+ i +","+ j +") = "+ res);
            }
        }
        
//        Set<Integer> A = new HashSet<Integer>(Arrays.asList(1, 2, 3, 4, 5));
//        Set<Integer> B = new HashSet<Integer>(Arrays.asList(4, 5, 6, 7));
//        System.out.println("U: "+ enc.getSetCardinalityUnion(A, B));
//        System.out.println("I: "+ enc.getSetCardinalityIntersection(A, B));
    }
}
