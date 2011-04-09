package org.acaro.dbpedia4neo.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
import com.tinkerpop.blueprints.pgm.oupls.sail.GraphSail;

public class Main {
	public static void main(String[] args) throws Exception {

		final Server server = new Server(8081);
		final Neo4jGraph neo  = new Neo4jGraph("/Users/hammer/UNIBZ/neodbpedia/");
		final GraphSail gsail = new GraphSail(neo);
		final SailGraph sail  = new SailGraph(gsail);
		
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
                
        // set the query handler
        context.addServlet(new ServletHolder(new QueryHandler(sail)), "/query");
        
		// we need a clean shutdown
        Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					System.out.println("Shutting down...");
					server.stop();
					sail.shutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
        });
        // start the server
        server.start();
        server.join();	
	}
}
