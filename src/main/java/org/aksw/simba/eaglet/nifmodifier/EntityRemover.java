package org.aksw.simba.eaglet.nifmodifier;

import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.io.nif.DocumentWriter;

import org.aksw.gerbil.io.nif.utils.NIFUriHelper;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.NIFTransferPrefixMapping;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class EntityRemover  {
	public static void main(String[] args) {
		Document document = new DocumentImpl("This is a test.", "http://example.org/1");
		document.addMarking(new NamedEntity(0, 10, "http://example.org/Barack_Obama"));

		Model nifModel = ModelFactory.createDefaultModel();
		nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
		DocumentWriter writer = new DocumentWriter();
		writer.writeDocumentToModel(nifModel, document);
		// System.out.println(document.getMarkings());
		String annotationUri = NIFUriHelper.getNifUri(document.getDocumentURI(), 0, 10);
		Resource annotation = nifModel.getResource(annotationUri);
		nifModel.add(annotation, nifModel.createProperty("http://aksw.org/anotationCheckClass"),
				nifModel.createProperty("http://aksw.org/IWouldDeleteIt"));

		
		
		StmtIterator iterator = nifModel.listStatements(annotation, null, (RDFNode) null);
		List<Statement> stmtsToBeRemoved = new ArrayList<>();
		while (iterator.hasNext()) {
			stmtsToBeRemoved.add(iterator.next());
		}

		nifModel.remove(stmtsToBeRemoved);
		//document.getMarkings().remove(0);
		//System.out.println(document.getMarkings());
	}
	public void removeEntity(Document doc)
	{
		Model nifModel = ModelFactory.createDefaultModel();
		nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
		DocumentWriter writer = new DocumentWriter();
		writer.writeDocumentToModel(nifModel, doc);
		
		
	}

}
