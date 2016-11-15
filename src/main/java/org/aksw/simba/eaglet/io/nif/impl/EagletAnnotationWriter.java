package org.aksw.simba.eaglet.io.nif.impl;

import java.util.Set;

import org.aksw.gerbil.io.nif.AnnotationWriter;
import org.aksw.gerbil.io.nif.utils.NIFUriHelper;
import org.aksw.gerbil.transfer.nif.Meaning;
import org.aksw.gerbil.transfer.nif.ScoredMarking;
import org.aksw.gerbil.transfer.nif.Span;
import org.aksw.gerbil.transfer.nif.TypedMarking;
import org.aksw.gerbil.transfer.nif.vocabulary.ITSRDF;
import org.aksw.gerbil.transfer.nif.vocabulary.NIF;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.ErrorType;
import org.aksw.simba.eaglet.vocab.EAGLET;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class EagletAnnotationWriter extends AnnotationWriter {

    public void addSpan(final Model nifModel, final Resource documentAsResource, final String text,
            final String documentURI, final Span span) {
        int startInJavaText = span.getStartPosition();
        int endInJavaText = startInJavaText + span.getLength();
        int start = text.codePointCount(0, startInJavaText);
        int end = start + text.codePointCount(startInJavaText, endInJavaText);

        String spanUri = NIFUriHelper.getNifUri(documentURI, start, end);
        Resource spanAsResource = nifModel.createResource(spanUri);
        nifModel.add(spanAsResource, RDF.type, NIF.Phrase);
        nifModel.add(spanAsResource, RDF.type, NIF.String);
        nifModel.add(spanAsResource, RDF.type, NIF.RFC5147String);
        if (span.getIsWord()) {
            nifModel.add(spanAsResource, RDF.type, NIF.Word);
        }
        // TODO add language to String
        nifModel.add(spanAsResource, NIF.anchorOf,
                nifModel.createTypedLiteral(text.substring(startInJavaText, endInJavaText), XSDDatatype.XSDstring));
        nifModel.add(spanAsResource, NIF.beginIndex,
                nifModel.createTypedLiteral(start, XSDDatatype.XSDnonNegativeInteger));
        nifModel.add(spanAsResource, NIF.endIndex, nifModel.createTypedLiteral(end, XSDDatatype.XSDnonNegativeInteger));
        nifModel.add(spanAsResource, NIF.referenceContext, documentAsResource);

        if (span instanceof Meaning) {
            for (String meainingUri : ((Meaning) span).getUris()) {
                nifModel.add(spanAsResource, ITSRDF.taIdentRef, nifModel.createResource(meainingUri));
            }
        }
        if (span instanceof ScoredMarking) {
            nifModel.add(spanAsResource, ITSRDF.taConfidence,
                    nifModel.createTypedLiteral(((ScoredMarking) span).getConfidence(), XSDDatatype.XSDdouble));
        }
        if (span instanceof TypedMarking) {
            Set<String> types = ((TypedMarking) span).getTypes();
            for (String type : types) {
                nifModel.add(spanAsResource, ITSRDF.taClassRef, nifModel.createResource(type));
            }
        }
        if (span instanceof NamedEntityCorrections) {
            NamedEntityCorrections correction = (NamedEntityCorrections) span;
            if (correction.getError() != null) {
                for (ErrorType error : correction.getError()) {
                    nifModel.add(spanAsResource, EAGLET.hasErrorType, EAGLET.getErrorType(error));
                }
            }
            if (correction.getResult() != null) {
                nifModel.add(spanAsResource, EAGLET.hasCheckResult, EAGLET.getCheckResult(correction.getResult()));
            }
            if (correction.getUserDecision() != null) {
                nifModel.add(spanAsResource, EAGLET.hasUserDecision,
                        EAGLET.getUserDecision(correction.getUserDecision()));
            }
            if (correction.getPartner() != null) {
                NamedEntityCorrections partner = correction.getPartner();
                Resource partnerResource = nifModel.getResource(
                        NIFUriHelper.getNifUri(NIFUriHelper.getDocumentUriFromNifUri(spanAsResource.getURI()),
                                partner.getStartPosition(), partner.getStartPosition() + partner.getLength()));
                nifModel.add(spanAsResource, EAGLET.hasPairPartner, partnerResource);
            }
        }
    }
}
