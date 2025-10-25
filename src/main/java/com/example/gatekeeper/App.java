package com.example.gatekeeper;

import com.example.gatekeeper.checks.ActuatorChecker;
import com.example.gatekeeper.checks.CorsChecker;
import com.example.gatekeeper.checks.SqlChecker;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class App {

    public static void main(String[] args) {
        if (args == null) args = new String[0];

        if (Arrays.asList(args).contains("--help") || Arrays.asList(args).contains("-h")) {
            printHelp();
            System.exit(0);
        }

        String url = null;
        boolean checkCors = false;
        boolean checkActuator = false;
        boolean checkSql = false;
        boolean verbose = false;
        String reportFile = null;

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if ("--url".equals(a) || "-u".equals(a)) {
                if (i + 1 < args.length) url = args[++i];
                else { System.err.println("Error: --url requires a value"); System.exit(2); }
            } else if (a.startsWith("--url=")) {
                url = a.substring("--url=".length());
            } else if ("--check-cors".equals(a)) {
                checkCors = true;
            } else if ("--check-actuator".equals(a)) {
                checkActuator = true;
            } else if ("--check-sql".equals(a)) {
                checkSql = true;
            } else if ("--verbose".equals(a) || "-v".equals(a)) {
                verbose = true;
            } else if (a.startsWith("--report=")) {
                reportFile = a.substring("--report=".length());
            } else {
                System.err.println("Unknown argument: " + a);
                printHelp();
                System.exit(2);
            }
        }

        if (url == null || url.isBlank()) {
            System.err.println("Error: --url is required");
            printHelp();
            System.exit(2);
        }

        // If no checks specified, run all by default
        if (!checkCors && !checkActuator && !checkSql) {
            checkCors = checkActuator = checkSql = true;
        }

        System.out.println("Scanning: " + url);

        List<String> issues = new ArrayList<>();

        if (checkCors) {
            try {
                CorsChecker c = new CorsChecker();
                boolean v = c.run(url);
                if (v) issues.add("Issue Detected: Insecure CORS|Severity: Critical|Recommendation: Restrict allowed origins to trusted domains only.");
                if (verbose) System.out.println("[INFO] CORS check result: " + v);
            } catch (Exception e) {
                System.err.println("[ERROR] CORS check failed: " + e.getMessage());
            }
        }

        if (checkActuator) {
            try {
                ActuatorChecker ac = new ActuatorChecker();
                boolean v = ac.run(url);
                if (v) issues.add("Issue Detected: Information Disclosure (Actuator)|Severity: High|Recommendation: Disable public actuator endpoints or apply authentication.");
                if (verbose) System.out.println("[INFO] Actuator check result: " + v);
            } catch (Exception e) {
                System.err.println("[ERROR] Actuator check failed: " + e.getMessage());
            }
        }

        if (checkSql) {
            try {
                SqlChecker s = new SqlChecker();
                boolean v = s.run(url);
                if (v) issues.add("Issue Detected: SQL Injection indicators|Severity: High|Recommendation: Sanitize inputs and use parameterized queries.");
                if (verbose) System.out.println("[INFO] SQL check result: " + v);
            } catch (Exception e) {
                System.err.println("[ERROR] SQL check failed: " + e.getMessage());
            }
        }

        // Print professional, monochrome report (Option 2)
        if (issues.isEmpty()) {
            System.out.println("\nScan Status: PASSED - no critical issues found.");
            if (reportFile != null) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(reportFile))) {
                    pw.println("Scan Status: PASSED - no critical issues found.");
                } catch (Exception ignored) {}
            }
            System.exit(0);
        } else {
            System.out.println();
            for (String s : issues) {
                String[] parts = s.split("\\|");
                System.out.println(parts[0]);
                if (parts.length > 1) System.out.println(parts[1]);
                if (parts.length > 2) System.out.println(parts[2]);
                System.out.println();
            }
            System.out.println("Scan Status: FAILED (" + issues.size() + " issues)"); 
            if (reportFile != null) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(reportFile))) {
                    for (String s : issues) {
                        String[] parts = s.split("\\|");
                        for (String p : parts) pw.println(p);
                        pw.println();
                    }
                    pw.println("Scan Status: FAILED (" + issues.size() + " issues)"); 
                } catch (Exception ignored) {}
            }
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("\nGatekeeper - Lightweight API security scanner");
        System.out.println("-------------------------------------------------");
        System.out.println("Usage:");
        System.out.println("  java -jar gatekeeper.jar --url <target> [options]"); 
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --url <url>           or --url=<url>   Target base URL (required)"); 
        System.out.println("  --check-cors                          Check for insecure CORS"); 
        System.out.println("  --check-actuator                      Check for exposed Spring actuator endpoints"); 
        System.out.println("  --check-sql                           Basic SQL injection probes"); 
        System.out.println("  --verbose, -v                         Print extra diagnostic info"); 
        System.out.println("  --report=<file>                       Write report to file"); 
        System.out.println("  --help, -h                            Print this help"); 
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar gatekeeper.jar --url https://example.com --check-cors");
        System.out.println("  java -jar gatekeeper.jar --url=https://staging --check-cors --check-actuator --verbose --report=scan.txt");
        System.out.println();
    }
}
