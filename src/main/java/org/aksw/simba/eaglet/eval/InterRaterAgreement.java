package org.aksw.simba.eaglet.eval;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.io.nif.DocumentListParser;
import org.aksw.gerbil.io.nif.DocumentParser;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.NIFTransferPrefixMapping;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.simba.eaglet.annotator.AdaptedAnnotationParser;
import org.aksw.simba.eaglet.entitytypemodify.EntityTypeChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import arq.load;

public class InterRaterAgreement {

	private static final Logger LOGGER = LoggerFactory.getLogger(InterRaterAgreement.class);

	private static final String DATASET_FILES[] = new String[] { "eaglet_data/result_pipe/kore50-nif-result-nif.ttl" };

	public static void main(String[] args) {
		loadsDocuments();
	}

	protected static List<Document> loadDocuments() {
		List<Document> loadedDocuments = new ArrayList<Document>();
		for (int i = 0; i < DATASET_FILES.length; ++i) {
			try {
				Model nifModel = ModelFactory.createDefaultModel();
				nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
				FileInputStream fin = new FileInputStream(new File(DATASET_FILES[i]));
				nifModel.read(fin, "", "TTL");
				fin.close();
				DocumentListParser parser = new DocumentListParser(new DocumentParser(new AdaptedAnnotationParser()));
				List<Document> documents = parser.parseDocuments(nifModel);
				for (Document document : documents) {
					loadedDocuments.add(new DocumentImpl(document.getText(), document.getDocumentURI(),
							EntityTypeChange.changeType(document)));
				}

				// loadedDocuments.addAll(CheckerPipeline.readDocuments(DATASET_FILES[i]));
			} catch (Exception e) {
				LOGGER.error("Couldn't load the dataset!", e);
			}
		}
		return loadedDocuments;
	}



	protected static List<Document> loadsDocuments() {
		List<Document> loadedDocuments = new ArrayList<Document>();
		DocumentListParser parser = new DocumentListParser(new DocumentParser(new AdaptedAnnotationParser()));
		for (int i = 0; i < DATASET_FILES.length; ++i) {
			try {
				Model nifModel = ModelFactory.createDefaultModel();
				nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
				FileInputStream fin = new FileInputStream(new File(DATASET_FILES[i]));
				nifModel.read(fin, "", "TTL");
				fin.close();
				loadedDocuments.addAll(parser.parseDocuments(nifModel));
			} catch (Exception e) {
				LOGGER.error("Couldn't load the dataset!", e);
			}
		}
		return loadedDocuments;
	}
}