package com.example.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;


public class MainJettyServer {

	public static void main(String[] args) throws Exception{
		String webPort = System.getenv("PORT");
		System.out.println("port = " + webPort);
		if(webPort == null || webPort.isEmpty()){
			webPort = "8080";
		}
		Server server = new Server();
		
		ServerConnector connector = new ServerConnector(server);
		connector.setIdleTimeout(30);
		connector.setPort(Integer.valueOf(webPort));
		server.setConnectors(new Connector[]{connector});
		
//		System.out.println("server.getConnectors()[1].getIdleTimeout()" + server.getConnectors()[1].getIdleTimeout());
		
		final WebAppContext root = new WebAppContext();
		
		root.setContextPath("/");
		root.setParentLoaderPriority(true);
		final String webappDirLocation = "../src/main/webapp/"; //"src/main/webapp/" to run in IDE use this
		root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
		root.setResourceBase(webappDirLocation);
		System.out.println("root.getResourceBase() = " + root.getResourceBase());
		
		server.setHandler((Handler) root);
		server.start();
		server.join();
	}

}
