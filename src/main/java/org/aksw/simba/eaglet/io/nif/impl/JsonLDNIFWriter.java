/**
 * This file is part of NIF transfer library for the General Entity Annotator Benchmark.
 *
 * NIF transfer library for the General Entity Annotator Benchmark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NIF transfer library for the General Entity Annotator Benchmark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NIF transfer library for the General Entity Annotator Benchmark.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.aksw.simba.eaglet.io.nif.impl;

import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

import org.aksw.gerbil.io.nif.AbstractNIFWriter;
import org.aksw.gerbil.io.nif.NIFWriter;
import org.aksw.gerbil.transfer.nif.Document;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_JSONLD;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * This is a implementation of the {@link NIFWriter} interface that relies on
 * the Turtle (TTL) serialization of RDF.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class JsonLDNIFWriter extends AbstractNIFWriter {

    private static final String HTTP_CONTENT_TYPE = RDFFormat.JSONLD.getLang().getContentType().getContentType();
    private static final String LANGUAGE = RDFFormat.JSONLD.getLang().getName();

    private RDFWriterRIOT_JSONLD rdfWriter;

    public JsonLDNIFWriter() {
        super(HTTP_CONTENT_TYPE, LANGUAGE);
        rdfWriter = new RDFWriterRIOT_JSONLD();
    }

    @Override
    public void writeNIF(List<Document> document, OutputStream os) {
        Model nifModel = createNIFModel(document);
        rdfWriter.write(nifModel, os, null);
    }

    @Override
    public void writeNIF(List<Document> document, Writer writer) {
        Model nifModel = createNIFModel(document);
        rdfWriter.write(nifModel, writer, null);
    }

}
