package org.aksw.simba.eaglet.error;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
import org.aksw.simba.eaglet.documentprocessor.StanfordParsedMarking;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.simba.eaglet.errorutils.NamedEntitySurfaceForm;
import org.aksw.simba.eaglet.errorutils.Token_StartposbasedComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

public class ErraticEntityError implements ErrorChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErraticEntityError.class);

    public void ErraticEntityProb(List<Document> documents) throws GerbilException {
        LOGGER.info(" ERRATIC ENTITY MODULE RUNNING");
        // implement search
        Map<String, List<NamedEntitySurfaceForm>> map = generate_map(documents);
        for (Document doc : documents) {
            List<StanfordParsedMarking> stanfordAnns = doc.getMarkings(StanfordParsedMarking.class);
            StanfordParsedMarking stanfordAnn = stanfordAnns.get(0);
            List<CoreLabel> tokens = stanfordAnn.getAnnotation().get(TokensAnnotation.class);
            if (stanfordAnns.size() != 1) {
                // TODO PANIC!!!!
                LOGGER.error(" Parser not working ");
            }
            boolean found = false;
            // actual search
            int i = 0;

            int k, matchedSurfaceFormLength;
            NamedEntityCorrections ne;
            CoreLabel currentToken;
            // iterate over all tokens
            while (i < tokens.size()) {
                currentToken = tokens.get(i);
                // if this token matches a first token of an entity
                if (map.containsKey(currentToken.lemma())) {
                    matchedSurfaceFormLength = 0;
                    // get the list of possible entities
                    List<NamedEntitySurfaceForm> current_list = map.get(currentToken.lemma());
                    // iterate over the list of possible entities
                    for (int j = 0; (j < current_list.size()) && (matchedSurfaceFormLength == 0); j++) {
                        NamedEntitySurfaceForm surfaceformvar = current_list.get(j);
                        k = 0;
                        // check whether the complete surface form is matching
                        while ((k < surfaceformvar.surfaceForm.length) && ((i + k) < tokens.size())
                                && surfaceformvar.surfaceForm[k].equals(tokens.get(i + k).lemma())) {
                            ++k;
                        }
                        // if the surface form is matching
                        if (k == surfaceformvar.surfaceForm.length) {
                            // check whether one of the already known named
                            // entities having this surface form is matching the
                            // position
                            found = false;
                            for (int n = 0; (!found) && (n < surfaceformvar.nes.size()); ++n) {
                                ne = surfaceformvar.nes.get(n);
                                if (doc.getDocumentURI().equals(ne.getDoc())) {
                                    if (currentToken.beginPosition() == ne.getStartPosition()) {
                                        found = true;
                                    }
                                }
                            }
                            // if there is no matching named entity, we have
                            // found a new one
                            if (found == false) {
                                NamedEntityCorrections newentity = new NamedEntityCorrections(
                                        currentToken.beginPosition(),
                                        tokens.get(i + surfaceformvar.surfaceForm.length - 1).endPosition()
                                                - currentToken.beginPosition(),
                                        new HashSet<String>(surfaceformvar.nes.get(0).getUris()), Check.INSERTED);
                                doc.addMarking(newentity);
                            }
                            matchedSurfaceFormLength = surfaceformvar.surfaceForm.length;
                        }
                    }
                    // if we have found something that matched increase our
                    // current position with the length of the matching surface
                    // form
                    if (matchedSurfaceFormLength > 0) {
                        i += matchedSurfaceFormLength;
                    } else {
                        ++i;
                    }
                } else {
                    ++i;
                }
            }
        }
    }

    public Map<String, List<NamedEntitySurfaceForm>> generate_map(List<Document> documents) {
        Map<String, List<NamedEntitySurfaceForm>> map = new HashMap<String, List<NamedEntitySurfaceForm>>();
        for (Document doc : documents) {
            List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);

            entities = nameEntity(doc, entities);

            for (NamedEntityCorrections entity : entities) {
                entity.setDoc(doc.getDocumentURI());
                if (map.containsKey(entity.getEntity_name())) {
                    for (NamedEntitySurfaceForm ns : map.get(entity.getEntity_name())) {
                        if (Arrays.equals(ns.surfaceForm, entity.entity_text)) {
                            ns.nes.add(entity);
                        }
                    }

                }

                else {
                    List<NamedEntitySurfaceForm> sub = new ArrayList<NamedEntitySurfaceForm>();
                    List<NamedEntityCorrections> sub_entity = new ArrayList<NamedEntityCorrections>();
                    NamedEntitySurfaceForm nesf = new NamedEntitySurfaceForm();
                    nesf.surfaceForm = entity.entity_text;
                    sub_entity.add(entity);
                    nesf.nes = sub_entity;
                    sub.add(nesf);
                    map.put(entity.getEntity_name(), sub);
                }
            }

        }

        // sorting the entries in descending order of the number od lemma ib the
        // text
        /*
         * for (Map.Entry<String, List<NamedEntityCorrections>> list :
         * map.entrySet()) { Collections.sort(list.getValue(), new
         * Entity_LengthBasedComparator()); }
         */
        return map;
    }

    public List<NamedEntityCorrections> nameEntity(Document doc, List<NamedEntityCorrections> entities) {

        List<StanfordParsedMarking> stanfordAnns = doc.getMarkings(StanfordParsedMarking.class);

        StanfordParsedMarking stanfordAnn = stanfordAnns.get(0);

        List<CoreLabel> tokens = stanfordAnn.getAnnotation().get(TokensAnnotation.class);
        if (stanfordAnns.size() != 1) {
            // TODO PANIC!!!!
            LOGGER.error(" Parser not working ");
        }
        String text = doc.getText();
        // 1. create Bitset containing the ne positions and gather ne positions
        BitSet nePositions = new BitSet(text.length());
        int start[] = new int[entities.size()];
        int end[] = new int[entities.size()];

        Collections.sort(entities, new StartPosBasedComparator());
        for (int i = 0; i < entities.size(); ++i) {
            start[i] = entities.get(i).getStartPosition();
            end[i] = entities.get(i).getStartPosition() + entities.get(i).getLength();
            nePositions.set(start[i], end[i]);
        }
        // 2. Iterate over the tokens and search for tokens that are inside of
        // ne boarders
        @SuppressWarnings("unchecked")
        List<CoreLabel> entityTokens[] = new List[entities.size()];
        for (CoreLabel token : tokens) {
           
            if (nePositions.get(token.beginPosition(), token.endPosition()).cardinality() > 0) {
                // search for matching named entities
                int pos = 0;
                while ((pos < start.length) && (start[pos] <= token.endPosition())) {
                    // if the token and the ne are overlapping
                    if ((start[pos] < token.endPosition()) && (end[pos] > token.beginPosition())) {
                        // nes.get(pos) is the matching ne
                        // add the token to the list of tokens of ne
                        if (entityTokens[pos] == null) {
                            entityTokens[pos] = new ArrayList<CoreLabel>();
                        }
                        entityTokens[pos].add(token);
                    }
                    ++pos;
                }
            }
        }
        Token_StartposbasedComparator comparator = new Token_StartposbasedComparator();

        // setting text for entity
        NamedEntityCorrections currentNamedEntity;
        for (int i = 0; i < entities.size(); i++) {
            if (entityTokens[i] == null) {
                // TODO print error because the entities has got no tokens :(
            } else {
                // sorting the token list
                Collections.sort(entityTokens[i], comparator);
                currentNamedEntity = entities.get(i);
                currentNamedEntity.setEntity_name(entityTokens[i].get(0).get(LemmaAnnotation.class));
                currentNamedEntity.entity_text = new String[entityTokens[i].size()];
                for (int j = 0; j < entityTokens[i].size(); ++j) {
                    currentNamedEntity.entity_text[j] = entityTokens[i].get(j).get(LemmaAnnotation.class);
                }
                currentNamedEntity.setNumber_of_lemma(entityTokens[i].size());
            }
        }

        return entities;
    }

    @Override
    public void check(List<Document> documents) throws GerbilException {
        // TODO Auto-generated method stub
        this.ErraticEntityProb(documents);
    }

}
