package com.example.gatekeeper.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Http {

    public static class Response {
        public int status;
        public Map<String, List<String>> headers;
        public String body;
    }

    public static Response get(String url) throws Exception {
        return get(url, Collections.emptyMap());
    }

    public static Response get(String url, Map<String,String> headers) throws Exception {
        URL u = new URL(url);
        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(10000);
        con.setReadTimeout(10000);
        if (headers != null) {
            for (var e : headers.entrySet()) {
                con.setRequestProperty(e.getKey(), e.getValue());
            }
        }
        int code = con.getResponseCode();
        InputStream is = code >= 200 && code < 400 ? con.getInputStream() : con.getErrorStream();
        StringBuilder sb = new StringBuilder();
        if (is != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append('\n');
            }
        }
        Http.Response r = new Http.Response();
        r.status = code;
        r.headers = con.getHeaderFields();
        r.body = sb.toString();
        return r;
    }
}
