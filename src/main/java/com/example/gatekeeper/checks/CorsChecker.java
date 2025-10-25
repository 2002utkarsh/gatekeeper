package com.example.gatekeeper.checks;

import com.example.gatekeeper.util.Http;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CorsChecker {
    public CorsChecker() {}

    public boolean run(String baseUrl) {
        System.out.println("Running CORS check...");
        try {
            // send a malicious origin header to test reflection
            Map<String,String> headers = Collections.singletonMap("Origin","http://evil.example.com");
            Http.Response res = Http.get(baseUrl, headers);
            int code = res.status;
            Map<String, List<String>> h = res.headers;
            List<String> allow = h.getOrDefault("Access-Control-Allow-Origin", h.getOrDefault("access-control-allow-origin", null));

            String allowVal = allow != null && !allow.isEmpty() ? allow.get(0) : null;

            if (allowVal == null) {
                System.out.println("CORS check: Access-Control-Allow-Origin header not present (status " + code + ")");
                return false;
            }

            String a = allowVal.trim();
            if ("*".equals(a) || a.contains("evil.example.com")) {
                System.err.println("CORS header suspicious: " + a);
                return true;
            }

            System.out.println("CORS check passed: Access-Control-Allow-Origin=" + a + " (status " + code + ")");
            return false;
        } catch (Exception e) {
            System.err.println("CORS check error: " + e.getMessage());
            return false;
        }
    }
}
