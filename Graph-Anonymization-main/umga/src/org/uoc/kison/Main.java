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
package org.uoc.kison;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.uoc.kison.UMGA.GraphReconstructionByNC;
import org.uoc.kison.UMGA.GraphReconstructionByNCLog;
import org.uoc.kison.UMGA.GraphReconstructionByRandom;
import org.uoc.kison.UMGA.GraphReconstructionByCoreness;
import org.uoc.kison.UMGA.UMGA;
import org.uoc.kison.UMGA.partitions.GknByWeightedGraph;
import org.uoc.kison.UMGA.search.ExhaustiveSearch;
import org.uoc.kison.UMGA.search.GreedySearch;
import org.uoc.kison.exporters.GmlExporter;
import org.uoc.kison.objects.SimpleIntGraph;
import org.uoc.kison.parsers.GmlParser;
import org.uoc.kison.parsers.TxtParser;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);
    private static final String version = "2.0";

    public static void main(String[] args) {
        PropertyConfigurator.configure("log4j.properties");

        String inputFileName = null;
        String extension = null;
        String outputFileName = null;
        int k = 1;
        String partitions = GknByWeightedGraph.PARAM;
        String search = null;
        String edgeSelection = null;

        if (args.length >= 4) {
            inputFileName = args[0];
            k = Integer.parseInt(args[1]);
            search = args[2];
            edgeSelection = args[3];
            
            String fullPath = FilenameUtils.getFullPath(inputFileName);
            String baseName = FilenameUtils.getBaseName(inputFileName);
            extension = FilenameUtils.getExtension(inputFileName);
            outputFileName = fullPath + baseName + "-k" + k + "-" + search + "-"+ edgeSelection +"." + extension;

            logger.info("**************************************************************");
            logger.info("* UMGA - Univariant Microaggregation for Graph Anonymization *");
            logger.info("* Jordi Casas-Roma (jcasasr@uoc.edu)                         *");
            logger.info("* Alexandre Dotor Casals (adotorc@uoc.edu)                   *");
            logger.info("* Universitat Oberta de Catalunya (www.uoc.edu)              *");
            logger.info("*                                                            *");
            logger.info("* Casas-Roma, J., Herrera-Joancomarti, J., Torra,V. (2013).  *");
            logger.info("*                                                            *");
            logger.info("*                                                            *");
            logger.info("*                                                            *");
            logger.info("**************************************************************");
            logger.info("");
            logger.info(String.format("Version %s", version));
            logger.info(String.format("Input filname   : %s", inputFileName));
            logger.info(String.format("k value         : %d", k));
            logger.info(String.format("Search method   : %s - %s", search, search.equalsIgnoreCase(ExhaustiveSearch.PARAM) ? ExhaustiveSearch.NAME : search.equalsIgnoreCase(GreedySearch.PARAM) ? GreedySearch.NAME : ""));
            logger.info(String.format("Edge selection  : %s - %s", edgeSelection, edgeSelection.equalsIgnoreCase(GraphReconstructionByRandom.PARAM) ? GraphReconstructionByRandom.NAME : edgeSelection.equalsIgnoreCase(GraphReconstructionByNC.PARAM) ? GraphReconstructionByNC.NAME : ""));
            logger.info(String.format("Output filename : %s", outputFileName));
            logger.info("");
            logger.info("---------------------------------------------------------------");

        } else {
            System.out.println("UMGA Version " + version);
            System.out.println("Usage: java UMGA <input filename> <k value> <search> <edge selection>");
            System.out.println("    - <search>        : '" + GreedySearch.PARAM + "' for Greedy search");
            System.out.println("                        '" + ExhaustiveSearch.PARAM + "' for Exhaustive search");
            System.out.println("    - <edge selection>: '" + GraphReconstructionByRandom.PARAM + "' for Random edge selection");
            System.out.println("                        '" + GraphReconstructionByNC.PARAM + "' for Neighbourhood Centrality edge selection");
            System.out.println("                        '" + GraphReconstructionByNCLog.PARAM + "' for Neighbourhood Centrality Log edge selection");
            System.out.println("                        '" + GraphReconstructionByCoreness.PARAM + "' for Coreness-preserving edge selection");
            System.exit(-1);
        }

        // import GML 
        SimpleIntGraph graph = null;

        if (extension.compareToIgnoreCase("GML") == 0) {
            GmlParser gmlParser = new GmlParser();
            graph = gmlParser.parseFile(inputFileName);

        } else if (extension.compareToIgnoreCase("TXT") == 0) {
            TxtParser txtParser = new TxtParser();
            graph = txtParser.parseFile(inputFileName);

        } else {
            logger.error(String.format("Unknown filetype (extension %s)!", extension));
            System.exit(0);
        }

        // apply UMGA algorithm
        UMGA umga = new UMGA();
        SimpleIntGraph gk = umga.umga(graph, k, search, partitions, edgeSelection);

        // export result to GML
        logger.info(String.format("Saving anonymized graph to: %s", outputFileName));
        GmlExporter gmlExporter = new GmlExporter();
        gmlExporter.exportToFile(gk, outputFileName);
    }
}
