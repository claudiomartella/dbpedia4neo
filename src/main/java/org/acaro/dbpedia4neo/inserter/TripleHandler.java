package org.acaro.dbpedia4neo.inserter;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import com.tinkerpop.blueprints.pgm.util.TransactionalGraphHelper.CommitManager;

public class TripleHandler implements RDFHandler {
	private SailConnection sc;
	private CommitManager manager;
	
	public TripleHandler(SailConnection sc, CommitManager manager) { 
		this.sc = sc;
		this.manager = manager;
	}

	public void handleComment(String arg0) throws RDFHandlerException {
	}

	public void handleNamespace(String arg0, String arg1)
			throws RDFHandlerException {
	}

	public void handleStatement(Statement arg0) {

		try {
			// avoid self-cycles
			if (arg0.getSubject().stringValue().equals(arg0.getObject().stringValue()))
				return;
			
			sc.addStatement(arg0.getSubject(), arg0.getPredicate(), arg0.getObject());
			manager.incrCounter();
			if (manager.atCommit())
				System.out.print(".");
		} catch (SailException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Subject: " + arg0.getSubject().toString() +
					" Predicate: " + arg0.getPredicate().toString() +
					" Object: " + arg0.getObject().toString());
		}
	}

	public void startRDF() throws RDFHandlerException {
	}
	
	public void endRDF() throws RDFHandlerException {
	}
}
