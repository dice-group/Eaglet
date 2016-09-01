package org.aksw.simba.eaglet.eval;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.gerbil.io.nif.DocumentListWriter;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.NIFTransferPrefixMapping;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class MergeResults {

	private static final Logger LOGGER = LoggerFactory.getLogger(MergeResults.class);

	private static final int USER_IDS[] = new int[] { 1, 6, 8 };
	private static final String USER_OUTPUT_FOLDER = "eaglet_data/result_user/KORE50/final";

	public static void main(String[] args) {
		MergeResults merger = new MergeResults();
		merger.run(USER_IDS, new File(USER_OUTPUT_FOLDER));
	}

	public void run(int[] userIds, File userOutputFolder) {
		List<Map<String, Document>> indexedDocumentsPerUser = loadDocuments(userIds, userOutputFolder);
		Map<String, Document> resultingCorpus = new HashMap<String, Document>();
		for (Map<String, Document> userDocuments : indexedDocumentsPerUser) {
			for (String uri : userDocuments.keySet()) {
				if (resultingCorpus.containsKey(uri)) {
					merge(resultingCorpus.get(uri), userDocuments.get(uri));
				} else {
					resultingCorpus.put(uri, userDocuments.get(uri));
				}
			}
		}

		Model nifModel = generateModifiedModel(new ArrayList<Document>(resultingCorpus.values()));
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(userOutputFolder.getAbsolutePath() + File.separator + "mergedCorpus.ttl");
			nifModel.write(fout, "TURTLE");
		} catch (Exception e) {
			LOGGER.error("Couldn't write result file.", e);
		} finally {
			IOUtils.closeQuietly(fout);
		}
	}

	private void merge(Document resultDocument, Document userDocument) {
		Set<NamedEntityCorrections> resultEntities = new HashSet<NamedEntityCorrections>(
				resultDocument.getMarkings(NamedEntityCorrections.class));
		List<NamedEntityCorrections> userEntities = userDocument.getMarkings(NamedEntityCorrections.class);
		for (NamedEntityCorrections e : userEntities) {
			if (!resultEntities.contains(e)) {
				resultDocument.addMarking(e);
				resultEntities.add(e);
			}
		}
	}

	protected static List<Map<String, Document>> loadDocuments(int[] userIds, File userOutputFolder) {
		List<List<Document>> documents = InterRaterAgreement.loadDocuments(userIds, userOutputFolder);
		List<Map<String, Document>> documentsPerUser = new ArrayList<Map<String, Document>>(documents.size());
		Map<String, Document> indexedDocuments;
		for (List<Document> userDocuments : documents) {
			indexedDocuments = new HashMap<String, Document>();
			for (Document document : userDocuments) {
				indexedDocuments.put(document.getDocumentURI(), document);
			}
			documentsPerUser.add(indexedDocuments);
		}
		return documentsPerUser;
	}

	public static Model generateModifiedModel(List<Document> documents) {
		Model nifModel = ModelFactory.createDefaultModel();
		nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
		DocumentListWriter writer = new DocumentListWriter();
		writer.writeDocumentsToModel(nifModel, documents);
		// Resource annotationResource;
		// for (Document document : documents) {
		// for (NamedEntityCorrections correction :
		// document.getMarkings(NamedEntityCorrections.class)) {
		// annotationResource =
		// nifModel.getResource(NIFUriHelper.getNifUri(document.getDocumentURI(),
		// correction.getStartPosition(), correction.getStartPosition() +
		// correction.getLength()));
		// nifModel.add(annotationResource, EAGLET.isNamedEntity,
		// nifModel.createTypedLiteral(correction.isNamedEntity()));
		// }
		// }
		return nifModel;
	}

}
