package com.smsmitra.dmz.Config;

//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Component
//@ConfigurationProperties(prefix = "backend")
//public class BackendRouteConfig {
//
//    private Map<String, String> routes = new HashMap<>();
//
//    public Map<String, String> getRoutes() {
//        return routes;
//    }
//
//    public void setRoutes(Map<String, String> routes) {
//        this.routes = routes;
//    }
//}




import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "backend")
public class BackendRouteConfig {
	
	  private Map<String, String> routes = new HashMap<>();
	    private String defaultRoute;   // frontend; gets every path that matches no service key

	    public Map<String, String> getRoutes() { return routes; }
	    public void setRoutes(Map<String, String> routes) { this.routes = routes; }

	    public String getDefaultRoute() { return defaultRoute; }
	    public void setDefaultRoute(String defaultRoute) { this.defaultRoute = defaultRoute; }

	/*
	 * private Map<String, String> routes = new HashMap<>();
	 * 
	 * public Map<String, String> getRoutes() { return routes; }
	 * 
	 * public void setRoutes(Map<String, String> routes) { this.routes = routes; }
	 */
}
