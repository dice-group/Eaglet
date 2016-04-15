package org.aksw.simba.eaglet.eval;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.io.nif.DocumentListParser;
import org.aksw.gerbil.io.nif.DocumentListWriter;
import org.aksw.gerbil.io.nif.DocumentParser;
import org.aksw.gerbil.io.nif.utils.NIFUriHelper;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.NIFTransferPrefixMapping;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
import org.aksw.simba.eaglet.annotator.AdaptedAnnotationParser;
import org.aksw.simba.eaglet.annotator.AnnotatorResult;
import org.aksw.simba.eaglet.entitytypemodify.EntityCheck;
import org.aksw.simba.eaglet.vocab.EAGLET;
import org.hsqldb.lib.HashSet;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class MergeResults {
	private String USER1_FILES[];

	String USER2_FILES[];

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MergeResults.class);
	private static final String folder = "";
	private static final String folder2 = "";

	public void merge(String filename, String filename2) throws GerbilException, IOException {
		File folder1 = new File(filename);
		File folder2 = new File(filename);
		File[] listOfFiles = folder1.listFiles();
		File[] listOfFiles2 = folder2.listFiles();

		for (File file : listOfFiles) {
			for (File file2 : listOfFiles2) {

				if (file.getName().equals(file2.getName()))

				{
					USER1_FILES = new String[] { file.getAbsolutePath() };
					USER2_FILES = new String[] { file2.getAbsolutePath() };
					List<Document> list1 = loadDocuments(USER1_FILES);
					List<Document> list2 = loadDocuments(USER2_FILES);
					for (Document doc1 : list1) {
						List<Marking> acceptedlist = new ArrayList<Marking>();
						List<Marking> reviewList = new ArrayList<Marking>();
						for (Document doc2 : list2) {

							List<EntityCheck> mark1 = doc1.getMarkings(EntityCheck.class);
							List<EntityCheck> mark2 = doc2.getMarkings(EntityCheck.class);
							Collections.sort(mark1, new StartPosBasedComparator());
							Collections.sort(mark2, new StartPosBasedComparator());
							for (EntityCheck ec1 : mark1) {
								for (EntityCheck ec2 : mark2) {
									if (ec1.equals(ec2)) {
										acceptedlist.add(ec1);
									}

									else {
										reviewList.add(ec1);
										reviewList.add(ec2);
									}
								}
							}
						}
						Document newdoc = new DocumentImpl(doc1.getText(), doc1.getDocumentURI(), acceptedlist);
						for (Marking d : reviewList) {
							newdoc.addMarking(d);
						}
						Model nifModel = generateModifiedModel(newdoc);

						File resultfile = new File("eaglet_data/result_merged/" + file.getName() + "-nif.ttl");
						if (!resultfile.exists()) {
							resultfile.getParentFile().mkdirs();
							resultfile.createNewFile();
						}
						FileOutputStream fout = new FileOutputStream(resultfile);
						fout.flush();
						nifModel.write(fout, "TTL");
						fout.close();
					}

				}

			}
		}

	}

	protected static List<Document> loadDocuments(String[] DATASET_FILES) {
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

	public static Model generateModifiedModel(Document document) {
		Model nifModel = ModelFactory.createDefaultModel();
		nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
		DocumentListWriter writer = new DocumentListWriter();
		writer.writeDocumentsToModel(nifModel, Arrays.asList(document));
		Resource annotationResource;
		for (EntityCheck correction : document.getMarkings(EntityCheck.class)) {
			annotationResource = nifModel.getResource(NIFUriHelper.getNifUri(document.getDocumentURI(),
					correction.getStartPosition(), correction.getStartPosition() + correction.getLength()));
			System.out.println(correction.getUris().toString() + " -> " + correction.isNamedEntity());
			nifModel.add(annotationResource, EAGLET.isNamedEntity,
					nifModel.createTypedLiteral(correction.isNamedEntity()));
		}

		return nifModel;

	}

}
