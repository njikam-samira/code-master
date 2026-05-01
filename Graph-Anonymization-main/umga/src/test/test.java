package test;

import java.util.Arrays;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.uoc.kison.UMGA.partitions.GknByWeightedGraph;
import org.uoc.kison.UMGA.search.ExhaustiveSearch;
import org.uoc.kison.UMGA.search.GreedySearch;
import org.uoc.kison.UMGA.utils.UtilsGraph;
import org.uoc.kison.objects.SimpleIntGraph;
import org.uoc.kison.parsers.GmlParser;
import org.uoc.kison.parsers.TxtParser;

/**
 *
 * @author jcasasr
 */
public class test {
    
    private static final Logger logger = Logger.getLogger(test.class);
    private static final String version = "1.2.0";
    
    public static void main(String[] args) {
        PropertyConfigurator.configure("log4j.properties");

        String inputFileName = null;
        String extension = null;

        if (args.length >= 1) {
            inputFileName = args[0];

            String fullPath = FilenameUtils.getFullPath(inputFileName);
            String baseName = FilenameUtils.getBaseName(inputFileName);
            extension = FilenameUtils.getExtension(inputFileName);

            logger.info(String.format("Version %s", version));
            logger.info("************************************************");
            logger.info(String.format("Input filname   : %s", inputFileName));
            logger.info("************************************************");

        } else {
            System.out.println("UMGA Version " + version);
            System.out.println("Usage: java UMGA <input filename>");
            System.exit(-1);
        }

        // import GML 
        SimpleIntGraph g = null;

        if (extension.compareToIgnoreCase("GML") == 0) {
            GmlParser gmlParser = new GmlParser();
            g = gmlParser.parseFile(inputFileName);

        } else if (extension.compareToIgnoreCase("TXT") == 0) {
            TxtParser txtParser = new TxtParser();
            g = txtParser.parseFile(inputFileName);

        } else {
            logger.error(String.format("Unknown filetype (extension %s)!", extension));
            System.exit(0);
        }

        // degree sequence of G
        UtilsGraph utilsGraph = new UtilsGraph();
        
        logger.info(String.format("%d nodes, %d edges", g.getNumNodes(), g.getNumEdges()));
        logger.info(String.format("K=%d", utilsGraph.getKAnonymityValueFromGraph(g)));
        
        int[] d = utilsGraph.degree(g);
        logger.info(String.format("Degree sequence=%s", Arrays.toString(d)));
        
        int[] h = utilsGraph.getDegreeHistogramFromDegreeSequence(d);
        logger.info(String.format("Degree histogram=%s", Arrays.toString(h)));
    }
}
