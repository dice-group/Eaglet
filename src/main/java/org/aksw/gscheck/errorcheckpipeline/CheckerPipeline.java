package org.aksw.gscheck.errorcheckpipeline;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.io.nif.DocumentListWriter;
import org.aksw.gerbil.io.nif.impl.TurtleNIFWriter;
import org.aksw.gerbil.io.nif.utils.NIFUriHelper;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.NIFTransferPrefixMapping;
import org.aksw.gscheck.corrections.EntityTypeChange;
import org.aksw.gscheck.corrections.NamedEntityCorrections;
import org.aksw.gscheck.error.CombinedTaggingError;
import org.aksw.gscheck.error.ErraticEntityError;
import org.aksw.gscheck.error.ErrorChecker;
import org.aksw.gscheck.error.LongDescriptionError;
import org.aksw.gscheck.error.OverLappingError;
import org.aksw.gscheck.error.SubsetMarkingError;
import org.aksw.gscheck.vocab.EAGLET;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class CheckerPipeline {

    private static final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
            "gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false, ExperimentType.A2KB);

    public static void PiPeStructure() throws GerbilException {
        CombinedTaggingError.CombinedTagger();
        ErraticEntityError.ErraticEntityProb();
        LongDescriptionError.LongDescription();
        OverLappingError.overlapcheck();
        SubsetMarkingError.subsetmark();
    }

    public void PreProcessor() throws GerbilException {
        List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();

        for (Document doc : documents) {
            List<Marking> list = EntityTypeChange.changeType(doc);
            doc.setMarkings(list);
        }

    }

    public static void main(String[] args) throws GerbilException {
        // TODO init pipeline
        List<ErrorChecker> checkers = new ArrayList<ErrorChecker>();
        checkers.add(new CombinedTaggingError());
        // PiPeStructure();

        List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();
        for (Document doc : documents) {
            List<Marking> list = EntityTypeChange.changeType(doc);
            doc.setMarkings(list);
        }
        // TODO preprocess documents

        for (ErrorChecker checker : checkers) {
            checker.check(documents);
        }

        // TODO write documents
        Model nifModel = generateModel(documents);
        FileOutputStream fout = new FileOutputStream("TODO.ttl");
        nifModel.write(fout, "TTL");
        fout.close();
    }

    public static Model generateModel(List<Document> documents) {
        Model nifModel = ModelFactory.createDefaultModel();
        nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
        DocumentListWriter writer = new DocumentListWriter();
        writer.writeDocumentsToModel(nifModel, documents);
        Resource annotationResource;
        List<NamedEntityCorrections> partners;
        for (Document document : documents) {
            for (NamedEntityCorrections correction : document.getMarkings(NamedEntityCorrections.class)) {
                annotationResource = nifModel.getResource(NIFUriHelper.getNifUri(document.getDocumentURI(),
                        correction.getStartPosition(), correction.getStartPosition() + correction.getLength()));
                nifModel.add(annotationResource, EAGLET.hasCheckResult,
                        EAGLET.getCheckResult(correction.getResult()));
                partners = correction.getPartner();
                if ((partners != null) && (partners.size() > 0)) {
                    for (NamedEntityCorrections partner : partners) {
                        nifModel.add(annotationResource, EAGLET.hasPairPartner,
                                nifModel.getResource(NIFUriHelper.getNifUri(document.getDocumentURI(),
                                        partner.getStartPosition(), partner.getStartPosition() + partner.getLength())));
                    }
                }
            }
        }
        return nifModel;
    }

}
