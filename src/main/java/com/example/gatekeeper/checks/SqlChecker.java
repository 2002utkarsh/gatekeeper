package com.example.gatekeeper.checks;

import com.example.gatekeeper.util.Http;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SqlChecker {
    public SqlChecker() {}

    private static final String[] PAYLOADS = new String[]{
            "' OR '1'='1' -- ",
            "' OR 1=1 -- ",
            "\" OR \"1\"=\"1\" -- ",
            "admin' -- "
    };

    private static final String[] ENDPOINTS = new String[]{"/search?q=", "/login?username=", "/users?name="};

    public boolean run(String baseUrl) {
        System.out.println("Running SQL injection checks...");
        try {
            for (String ep : ENDPOINTS) {
                for (String p : PAYLOADS) {
                    String encoded = URLEncoder.encode(p, StandardCharsets.UTF_8);
                    // ensure we don't produce a double slash when ep starts with '/'
                    String url;
                    if (baseUrl.endsWith("/")) {
                        // remove trailing slash so baseUrl + ep produces exactly one slash
                        url = baseUrl.substring(0, baseUrl.length() - 1) + ep + encoded;
                    } else {
                        url = baseUrl + ep + encoded;
                    }

                    try {
                        Http.Response r = Http.get(url);
                        int code = r.status;
                        String body = r.body != null ? r.body.toLowerCase() : "";
                        if (code == 200 && (body.contains("sql") || body.contains("syntax error")
                                || body.contains("exception") || body.contains("sqlsyntax"))) {
                            System.err.println("Potential SQLi at " + url + " — response snippet: "
                                    + (body.length() > 200 ? body.substring(0, 200) : body));
                            return true;
                        }
                    } catch (Exception ignored) {
                        // optionally log ignored exceptions during individual requests
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("SQL check error: " + e.getMessage());
        }
        System.out.println("SQL checks completed — no obvious signs found.");
        return false;
    }
}
