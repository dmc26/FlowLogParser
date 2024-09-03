package org.example;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Unit test for simple App.
 */




import java.nio.file.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FlowLogParserTest{

    private FlowLogParser parser;
    private static final String TEST_LOOKUP_TABLE = "test_lookup_table.csv";
    private static final String TEST_FLOW_LOG = "test_flow_log.txt";
    private static final String TEST_OUTPUT = "test_output.txt";

    @Before
    public void setUp() throws IOException {
        parser = new FlowLogParser();

        // Create test lookup table
        List<String> lookupLines = Arrays.asList(
                "dstport,protocol,tag",
                "80,tcp,web",
                "443,tcp,ssl",
                "53,udp,dns"
        );
        Files.write(Paths.get(TEST_LOOKUP_TABLE), lookupLines);

        // Create test flow log
        List<String> flowLogLines = Arrays.asList(
                "2 123456789012 eni-1234567890abcdef0 10.0.1.4 10.0.0.61 80 443 6 1 40 1566261600 1566261660 ACCEPT OK",
                "2 123456789012 eni-1234567890abcdef0 10.0.0.61 10.0.1.4 443 80 6 1 40 1566261600 1566261660 ACCEPT OK",
                "2 123456789012 eni-1234567890abcdef0 10.0.1.5 10.0.0.62 53 12345 17 1 40 1566261600 1566261660 ACCEPT OK"
        );
        Files.write(Paths.get(TEST_FLOW_LOG), flowLogLines);
    }

    @After
    public void tearDown() throws IOException {
        // Clean up test files
        Files.deleteIfExists(Paths.get(TEST_LOOKUP_TABLE));
        Files.deleteIfExists(Paths.get(TEST_FLOW_LOG));
        Files.deleteIfExists(Paths.get(TEST_OUTPUT));
    }

    @Test
    public void testLoadLookupTable() throws IOException {
        parser.loadLookupTable(TEST_LOOKUP_TABLE);
        Map<String, List<String>> lookupTable = parser.getLookupTable();

        assertEquals(3, lookupTable.size());
        assertTrue(lookupTable.get("80,tcp").contains("web"));
        assertTrue(lookupTable.get("443,tcp").contains("ssl"));
        assertTrue(lookupTable.get("53,udp").contains("dns"));
    }

    @Test
    public void testProcessFlowLogs() throws IOException {
        parser.loadLookupTable(TEST_LOOKUP_TABLE);
        parser.processFlowLogs(TEST_FLOW_LOG);

        Map<String, Integer> tagCounts = parser.getTagCounts();
        Map<String, Integer> portProtocolCounts = parser.getPortProtocolCounts();

        assertEquals(3, tagCounts.size());
        assertEquals(Integer.valueOf(1), tagCounts.get("web"));
        assertEquals(Integer.valueOf(1), tagCounts.get("ssl"));
        assertEquals(Integer.valueOf(1), tagCounts.get("dns"));

        assertEquals(3, portProtocolCounts.size());
        assertEquals(Integer.valueOf(1), portProtocolCounts.get("80,tcp"));
        assertEquals(Integer.valueOf(1), portProtocolCounts.get("443,tcp"));
        assertEquals(Integer.valueOf(1), portProtocolCounts.get("53,udp"));
    }

    @Test
    public void testGetProtocolName() {
        assertEquals("tcp", FlowLogParser.getProtocolName("6"));
        assertEquals("udp", FlowLogParser.getProtocolName("17"));
        assertEquals("123", FlowLogParser.getProtocolName("123"));
    }

    @Test
    public void testWriteResultsToFile() throws IOException {
        parser.loadLookupTable(TEST_LOOKUP_TABLE);
        parser.processFlowLogs(TEST_FLOW_LOG);
        parser.writeResultsToFile(TEST_OUTPUT);

        List<String> outputLines = Files.readAllLines(Paths.get(TEST_OUTPUT));
        assertTrue(outputLines.contains("Tag Counts:"));
        assertTrue(outputLines.contains("Port/Protocol Combination Counts:"));
        assertTrue(outputLines.contains("web,1"));
        assertTrue(outputLines.contains("ssl,1"));
        assertTrue(outputLines.contains("dns,1"));
        assertTrue(outputLines.contains("80,tcp,1"));
        assertTrue(outputLines.contains("443,tcp,1"));
        assertTrue(outputLines.contains("53,udp,1"));
    }

    @Test
    public void testMainMethod() throws IOException {
        String[] args = {TEST_FLOW_LOG, TEST_LOOKUP_TABLE, TEST_OUTPUT};
        FlowLogParser.main(args);

        assertTrue(Files.exists(Paths.get(TEST_OUTPUT)));
        List<String> outputLines = Files.readAllLines(Paths.get(TEST_OUTPUT));
        assertTrue(outputLines.contains("Tag Counts:"));
        assertTrue(outputLines.contains("Port/Protocol Combination Counts:"));
    }
}