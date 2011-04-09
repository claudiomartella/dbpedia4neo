package org.acaro.dbpedia4neo.inserter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.openrdf.model.ValueFactory;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.ntriples.NTriplesParser;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.pgm.oupls.sail.GraphSail;
import com.tinkerpop.blueprints.pgm.util.TransactionalGraphHelper;
import com.tinkerpop.blueprints.pgm.util.TransactionalGraphHelper.CommitManager;

public class DBpediaLoader 
{
    public static void main( String[] args ) 
    	throws SailException, RDFParseException, RDFHandlerException, FileNotFoundException, IOException
    {
    	Neo4jGraph neo = new Neo4jGraph("dbpedia4neo");
    	Sail sail = new GraphSail(neo);
    	CommitManager manager = TransactionalGraphHelper.createCommitManager(neo, 10000);

    	for (String file: args) {
    		System.out.println("Loading " + file + ": ");
    		loadFile(file, sail.getConnection(), sail.getValueFactory(), manager);
    		System.out.print('\n');
    	}
    	manager.close();
    	sail.shutDown();
    }

	private static void loadFile(final String file, SailConnection sc, ValueFactory vf, CommitManager manager) throws RDFParseException, RDFHandlerException, FileNotFoundException, IOException {
		NTriplesParser parser = new NTriplesParser(vf);
		TripleHandler handler = new TripleHandler(sc, manager);
		parser.setRDFHandler(handler);
		parser.setStopAtFirstError(false);
		parser.setParseErrorListener(new ParseErrorListener() {
			
			@Override
			public void warning(String msg, int lineNo, int colNo) {
				System.err.println("warning: " + msg);
				System.err.println("file: " + file + " line: " + lineNo + " column: " +colNo);
			}

			@Override
			public void error(String msg, int lineNo, int colNo) {
				System.err.println("error: " + msg);
				System.err.println("file: " + file + " line: " + lineNo + " column: " +colNo);
			}

			@Override
			public void fatalError(String msg, int lineNo, int colNo) {
				System.err.println("fatal: " + msg);
				System.err.println("file: " + file + " line: " + lineNo + " column: " +colNo);
			}
			
		});
		parser.parse(new BufferedInputStream(new FileInputStream(new File(file))), "http://dbpedia.org/");
	}
}
