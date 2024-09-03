# FlowLogParser
This Java program parses AWS VPC flow logs(Version 2), maps each log entry to tags based on a lookup table, and generates summary statistics

Program Structure

main: The entry point of the program.
initializeProtocolMap: Sets up a map to convert between protocol names and numbers.
loadLookupTable: Reads the lookup table file and stores the mappings in memory.
processFlowLogs: Reads the flow log file, processes each line, and updates the count maps.
getProtocolName: Converts a protocol number to its corresponding name.
incrementCount: A helper method to increment counts in a map.
writeResultsToFile: Writes the final results to the output file.

Assumptions

The flow log file and the lookup table file are plain text (ASCII) files.
The program assumes that the input files are well-formed and contain valid data.
Each sample log lines starts with "2" and has 14 fields separated by spaces.
"dstPort" defined in field 5 and "protocol" in field 7 both of string value.

Requirements

Java Development Kit (JDK) 8 or higher

No additional libraries or packages are required beyond the Java standard library.
Compilation
To compile the program, navigate to the directory containing the FlowLogParser.java file and run:
    javac FlowLogParser.java
Running the Program
To run the program, use the following command:
    java FlowLogParser <flow_log_file> <lookup_table_file> <output_file>
Where:
    <flow_log_file> is the path to the input VPC flow log file
    <lookup_table_file> is the path to the CSV file containing the lookup table
    <output_file> is the path where the output will be written

Example:
Output
The program generates an output file containing two sections:
Tag Counts: The count of matches for each tag.
Port/Protocol Combination Counts: The count of matches for each port/protocol combination.

Limitations and Potential Improvements

The program loads the entire lookup table into memory, which may not be efficient for very large lookup tables.
Error handling is minimal and could be improved.
The program processes files sequentially, which may be slow for very large flow log files.