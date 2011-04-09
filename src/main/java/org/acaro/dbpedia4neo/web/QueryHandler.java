package org.acaro.dbpedia4neo.web;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public class QueryHandler extends HttpServlet {
    private static final String FORM =  "<h2>Type in your query:</h2><br/>" +
    									"<form name=\"input\" action=\"%s\" method=\"post\">" +
    									"<textarea name=\"query\" cols=80 rows=15></textarea>" +
    									"<input type=\"Submit\" name=\"submit\" value=\"Submit\">" +
    									"</form>";
    private static final String QUERY = "This was your query: %s<br/>";
    private static final String HEADER = "<html><head><title>%s</title></head>";
    private static final String FOOTER = "</body></html>";
	private SailGraph sail;
    
    protected QueryHandler(SailGraph sail) {
    	this.sail = sail;
    }
    
    // get will just return the input form
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		PrintWriter writer = resp.getWriter();
		
		printHeader(writer, "DBPediaWebShell");
		printForm(writer, req.getRequestURI());
		printFooter(writer);
	}
    
	// post will parse the query, run it and return its results
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	PrintWriter writer = resp.getWriter();
    	
    	printHeader(writer, "DBPediaWebShell");
    	String query;
    	if ((query = req.getParameter("query")) != null)
    		printQuery(writer, query);
    	
    	try {
    		double start = System.currentTimeMillis();
    		List<Map<String, Vertex>> results = sail.executeSparql(query);
    		printResults(writer, results, System.currentTimeMillis()-start);
    	} catch (RuntimeException e) {
    		writer.write(e.getMessage());
    		e.printStackTrace(writer);
    	}
    	printForm(writer, req.getRequestURI());
    	printFooter(writer);
    }
    
    private void printQuery(PrintWriter writer, String query) {
    	writer.write(String.format(QUERY, escapeHtml(query)));
    }
    
    private void printResults(PrintWriter writer, List<Map<String, Vertex>> results, double time) {
    	writer.write(results.size() + " results in " + time + "ms.<br/>");
    	for (Map<String, Vertex> result: results) {
			for (Entry<String, Vertex> r: result.entrySet()) {
				writer.write(escapeHtml(r.getKey()) + " => ");
				writer.write(escapeHtml((String) r.getValue().getId()));
				writer.write("<br/>");
			}
		}
    }

	private void printHeader(PrintWriter writer, String title) {
    	writer.write(String.format(HEADER, title));
    }
    
    private void printFooter(PrintWriter writer) {
    	writer.write(FOOTER);
    }
    
    private void printForm(PrintWriter writer, String action) {
    	writer.write(String.format(FORM, action));
    }
    
    private String escapeHtml(String str) {
    	return StringEscapeUtils.escapeHtml(str);
    }
}
