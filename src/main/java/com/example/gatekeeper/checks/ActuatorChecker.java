package com.example.gatekeeper.checks;

import com.example.gatekeeper.util.Http;

import java.util.Arrays;
import java.util.List;

public class ActuatorChecker {
    public ActuatorChecker() {}

    public boolean run(String baseUrl) {
        System.out.println("Running Actuator checks...");
        try {
            List<String> endpoints = Arrays.asList("/actuator/env","/actuator/metrics","/actuator/beans","/actuator/mappings","/actuator/health");
            for (String ep : endpoints) {
                String u = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) + ep : baseUrl + ep;
                try {
                    Http.Response r = Http.get(u);
                    if (r.status == 200 && r.body != null && r.body.trim().length() > 0) {
                        System.err.println("Actuator endpoint exposed: " + u + " (status 200)"); 
                        return true;
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            System.err.println("Actuator check error: " + e.getMessage());
        }
        System.out.println("Actuator checks passed (no exposed endpoints detected).");
        return false;
    }
}
