package org.example;

import java.io.*;
import java.util.*;

/**
 * Hello world!
 *
 */
public class FlowLogParser
{

        // Store the lookup table
        private static Map<String, List<String>> lookupTable;

        // Store the results
        private static Map<String, Integer> tagCounts;
        private static Map<String, Integer> portProtocolCounts;

        public static void main(String[] args) {
            System.out.println( "Inside Main!" );
            if (args.length != 3) {
                System.out.println("The parameters expected are: java FlowLogParser <flow_log_file> <lookup_table_file> <output_file>");
                System.exit(1);
            }

            String flowLogFile = args[0];
            String lookupTableFile = args[1];
            String outputFile = args[2];

            // Initialize data structures
            initializeProtocolMap();
            lookupTable = new HashMap<>();
            tagCounts = new HashMap<>();
            portProtocolCounts = new HashMap<>();

            try {
                // Load the lookup table
                loadLookupTable(lookupTableFile);

                // Process the flow logs
                processFlowLogs(flowLogFile);

                // Write the results to the output file
                writeResultsToFile(outputFile);

                System.out.println("Processing complete. Results written to " + outputFile);
            } catch (IOException e) {
                System.err.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private static void initializeProtocolMap() {
            /* Store protocol mappings */
            Map<String, Integer> protocolMap = new HashMap<>();
            protocolMap.put("tcp", 6);
            protocolMap.put("udp", 17);
        }

        private static void loadLookupTable(String filename) throws IOException {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                String key = fields[0] + "," + fields[1].toLowerCase();
                String tag = fields[2].toLowerCase();

                if (!lookupTable.containsKey(key)) {
                    lookupTable.put(key, new ArrayList<String>());
                }
                lookupTable.get(key).add(tag);
            }
            reader.close();
        }

        private static void processFlowLogs(String filename) throws IOException {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] fields = line.trim().split("\\s+");
                if (fields.length == 15 && fields[0].equals("2")) {
                    String dstPort = fields[5];
                    String protocol = getProtocolName(fields[7]);
                    String key = dstPort + "," + protocol;

                    // Update port/protocol counts
                    incrementCount(portProtocolCounts, key);

                    // Update tag counts
                    List<String> tags = lookupTable.getOrDefault(key, Arrays.asList("UNKNOWN"));
                    for (String tag : tags) {
                        incrementCount(tagCounts, tag);
                    }
                }
            }
            reader.close();
        }

        private static String getProtocolName(String protocolNumber) {
            int protocol = Integer.parseInt(protocolNumber);
            if (protocol == 6) return "tcp";
            if (protocol == 17) return "udp";
            return protocolNumber;
        }

        private static void incrementCount(Map<String, Integer> map, String key) {
            map.put(key, map.getOrDefault(key, 0) + 1);
        }

        private static void writeResultsToFile(String filename) throws IOException {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

            // Write tag counts
            writer.write("Tag Counts:");
            writer.newLine();
            writer.write("Tag,Count");
            writer.newLine();
            for (Map.Entry<String, Integer> entry : tagCounts.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }

            writer.newLine();

            // Write port/protocol combination counts
            writer.write("Port/Protocol Combination Counts:");
            writer.newLine();
            writer.write("Port,Protocol,Count");
            writer.newLine();
            for (Map.Entry<String, Integer> entry : portProtocolCounts.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }

            writer.close();
        }
    //}
//    public static void main( String[] args )
//    {
//        System.out.println( "Hello World!" );
//    }
}
