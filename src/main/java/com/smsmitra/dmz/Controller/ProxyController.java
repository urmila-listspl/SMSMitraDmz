//package com.smsmitra.dmz.Controller;
//
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.http.RequestEntity;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;
//
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.Enumeration;
//
//@RestController
//public class ProxyController {
//
//    @Value("${internal.server.url}")
//    private String internalServerUrl;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    @RequestMapping("/**")
//    public ResponseEntity<String> proxyRequest(HttpServletRequest request) throws URISyntaxException {
//        // Build the full URL for the internal server, including the path and query parameters
//        String fullPath = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
//        URI internalUri = new URI(internalServerUrl + fullPath);
//
//        // Create headers from the incoming request
//        HttpHeaders headers = new HttpHeaders();
//        Enumeration<String> headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            headers.add(headerName, request.getHeader(headerName));
//        }
//
//        // Forward the request body if present
//        String requestBody = null;
//        try {
//            if ("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod())) {
//                requestBody = request.getReader().lines().reduce("", (accumulator, line) -> accumulator + line);
//            }
//        } catch (Exception e) {
//            // Log the error but don't stop the process
//            System.err.println("Error reading request body: " + e.getMessage());
//        }
//
//        // Create a RequestEntity to forward the entire request
//        RequestEntity<String> requestEntity = new RequestEntity<>(
//                requestBody,
//                headers,
//                HttpMethod.valueOf(request.getMethod()),
//                internalUri
//        );
//
//        // Forward the request to the internal server
//        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
//
//        // Return the response from the internal server back to the original client
//        return ResponseEntity
//                .status(responseEntity.getStatusCode())
//                .headers(responseEntity.getHeaders())
//                .body(responseEntity.getBody());
//    }
//}

//--------------------CURRENT WORKING---------------///////

//package com.smsmitra.dmz.Controller;
//
//import com.smsmitra.dmz.Config.BackendRouteConfig;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.nio.charset.StandardCharsets;
//import java.util.Enumeration;
//
//@RestController
//public class ProxyController {
//
//    private final BackendRouteConfig routeConfig;
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    public ProxyController(BackendRouteConfig routeConfig) {
//        this.routeConfig = routeConfig;
//    }
//
//    @RequestMapping("/**")
//    public ResponseEntity<String> proxyRequest(HttpServletRequest request)
//           throws Exception {
//        System.out.println("🔥 DMZ HIT → " + request.getMethod() + " " + request.getRequestURI());
//
//        String requestUri = request.getRequestURI();   // /smsmitra/api/send
//        String query = request.getQueryString() != null
//                ? "?" + request.getQueryString()
//                : "";
//
//        // 🔥 Extract first path segment
//        String[] parts = requestUri.split("/");
//        if (parts.length < 2) {
//            return ResponseEntity.badRequest().body("Invalid request path");
//        }
//
//        String serviceKey = parts[1]; // smsmitra / lvf / newapp
//        String targetBaseUrl = routeConfig.getRoutes().get(serviceKey);
//
//        if (targetBaseUrl == null) {
//            return ResponseEntity
//                    .status(HttpStatus.NOT_FOUND)
//                    .body("Service not configured in DMZ");
//        }
//
//        // Remove service prefix before forwarding
//        String contextPrefix = "/" + serviceKey;
//        String forwardPath;
//
//        if (requestUri.equals(contextPrefix)) {
//            forwardPath = "/";
//        }
//        else if (requestUri.startsWith(contextPrefix + "/")) {
//            forwardPath = requestUri.substring(contextPrefix.length());
//        }
//        else {
//      forwardPath = requestUri;
//        }
//        URI internalUri = new URI(targetBaseUrl + forwardPath + query);
//
//        // ---- headers ----
//        HttpHeaders headers = new HttpHeaders();
//        Enumeration<String> headerNames = request.getHeaderNames();
//
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            if (!headerName.equalsIgnoreCase("host")
//                    && !headerName.equalsIgnoreCase("content-length")) {
//                headers.add(headerName, request.getHeader(headerName));
//            }
//        }
//
//        // ---- body ----
//        byte[] body = request.getInputStream().readAllBytes();
//        HttpMethod method = HttpMethod.valueOf(request.getMethod());
//        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);
//
//        ResponseEntity<byte[]> response =
//                restTemplate.exchange(internalUri, method, entity, byte[].class);
//
//        return ResponseEntity
//                .status(response.getStatusCode())
//                .headers(response.getHeaders())
//                .body(response.getBody() != null
//                        ? new String(response.getBody(), StandardCharsets.UTF_8)
//                        : null);
//    }
//}


package com.smsmitra.dmz.Controller;

import com.smsmitra.dmz.Config.BackendRouteConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

@RestController
public class ProxyController {

    // Headers that belong to one TCP connection and must never be copied by a proxy
    private static final Set<String> HOP_BY_HOP = Set.of(
            "host", "content-length", "connection", "keep-alive",
            "proxy-authenticate", "proxy-authorization", "te",
            "trailer", "transfer-encoding", "upgrade");

    private final BackendRouteConfig routeConfig;
    private final RestTemplate restTemplate;

    public ProxyController(BackendRouteConfig routeConfig) {
        this.routeConfig = routeConfig;

        // Without timeouts a slow backend blocks DMZ threads forever
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);   // 5 sec to connect
        factory.setReadTimeout(30000);     // 30 sec to get the response

        this.restTemplate = new RestTemplate(factory);

        // KEY FIX: do not throw on 4xx/5xx. Return the backend's response as-is.
        this.restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }
        });
    }

    @RequestMapping("/**")
    public ResponseEntity<byte[]> proxyRequest(HttpServletRequest request) {

        System.out.println("🔥 DMZ HIT → " + request.getMethod() + " " + request.getRequestURI());

        String requestUri = request.getRequestURI();   // /WhatsAppBanking/wabanking/verifymobile/xxx
        String query = request.getQueryString() != null
                ? "?" + request.getQueryString()
                : "";

        // ---- Extract first path segment ----
        String[] parts = requestUri.split("/");
        if (parts.length < 2) {
            return gatewayError(HttpStatus.BAD_REQUEST, "Invalid request path");
        }

        String serviceKey = parts[1];
        String targetBaseUrl = routeConfig.getRoutes().get(serviceKey);

        if (targetBaseUrl == null) {
            return gatewayError(HttpStatus.NOT_FOUND, "Service not configured in DMZ");
        }

        // ---- Remove service prefix before forwarding ----
        String contextPrefix = "/" + serviceKey;
        String forwardPath;

        if (requestUri.equals(contextPrefix)) {
            forwardPath = "/";
        } else if (requestUri.startsWith(contextPrefix + "/")) {
            forwardPath = requestUri.substring(contextPrefix.length());
        } else {
            forwardPath = requestUri;
        }

        URI internalUri;
        try {
            internalUri = new URI(targetBaseUrl + forwardPath + query);
        } catch (URISyntaxException e) {
            System.out.println("❌ DMZ: malformed URL → " + e.getMessage());
            return gatewayError(HttpStatus.BAD_REQUEST, "Malformed request URL");
        }

        // ---- Request headers ----
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!HOP_BY_HOP.contains(headerName.toLowerCase())) {
                // getHeaders keeps multi-valued headers intact
                for (String value : Collections.list(request.getHeaders(headerName))) {
                    headers.add(headerName, value);
                }
            }
        }

        try {
            // ---- Request body ----
            byte[] body = request.getInputStream().readAllBytes();
            HttpMethod method = HttpMethod.valueOf(request.getMethod());
            HttpEntity<byte[]> entity = new HttpEntity<>(body.length > 0 ? body : null, headers);

            // ---- Forward ----
            ResponseEntity<byte[]> response =
                    restTemplate.exchange(internalUri, method, entity, byte[].class);

            // ---- Return backend response exactly as received (any status) ----
            return new ResponseEntity<>(
                    response.getBody(),
                    copyResponseHeaders(response.getHeaders()),
                    response.getStatusCode());

        } catch (HttpStatusCodeException e) {
            // Safety net: only reached if the error handler above is ever changed back to throwing.
            return new ResponseEntity<>(
                    e.getResponseBodyAsByteArray(),
                    copyResponseHeaders(e.getResponseHeaders()),
                    e.getStatusCode());

        } catch (ResourceAccessException e) {
            // Backend did not answer at all: down, refused, DNS, SSL, timeout
            System.out.println("❌ DMZ: backend not reachable → " + internalUri + " : " + e.getMessage());
            if (e.getCause() instanceof SocketTimeoutException) {
                return gatewayError(HttpStatus.GATEWAY_TIMEOUT, "Upstream service timed out");
            }
            return gatewayError(HttpStatus.BAD_GATEWAY, "Upstream service unavailable");

        } catch (IOException e) {
            System.out.println("❌ DMZ: cannot read request body → " + e.getMessage());
            return gatewayError(HttpStatus.BAD_REQUEST, "Unable to read request body");

        } catch (Exception e) {
            e.printStackTrace();
            return gatewayError(HttpStatus.BAD_GATEWAY, "Gateway error");
        }
    }

    // Copy backend response headers except hop-by-hop ones
    private HttpHeaders copyResponseHeaders(HttpHeaders source) {
        HttpHeaders out = new HttpHeaders();
        if (source != null) {
            source.forEach((name, values) -> {
                if (!HOP_BY_HOP.contains(name.toLowerCase())) {
                    out.addAll(name, values);
                }
            });
        }
        return out;
    }

    // Errors generated by the DMZ itself (NOT backend errors). Generic text only, no internal IPs.
    private ResponseEntity<byte[]> gatewayError(HttpStatus status, String message) {
        String json = "{\"status\":\"ERROR\",\"message\":\"" + message + "\",\"data\":null}";
        return ResponseEntity
                .status(status)
                .header("X-Gateway-Error", "true")   // lets you tell DMZ errors apart from backend errors
                .contentType(MediaType.APPLICATION_JSON)
                .body(json.getBytes(StandardCharsets.UTF_8));
    }
}