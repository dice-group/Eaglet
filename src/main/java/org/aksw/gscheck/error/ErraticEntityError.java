package org.aksw.gscheck.error;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Span;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
import org.aksw.gscheck.corrections.NamedEntityCorrections;
import org.aksw.gscheck.corrections.NamedEntityCorrections.Check;
import org.aksw.gscheck.errorutils.Entity_LengthBasedComparator;
import org.aksw.gscheck.errorutils.Token_StartposbasedComparator;
import org.aksw.simba.gscheck.documentprocessor.StanfordParsedMarking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

public class ErraticEntityError implements ErrorChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErraticEntityError.class);

    Map<String, List<NamedEntityCorrections>> map = new HashMap<String, List<NamedEntityCorrections>>();

    public void ErraticEntityProb(List<Document> documents) throws GerbilException {
        LOGGER.info(" ERRATIC ENTITY MODULE RUNNING");
        // implement search
        generate_map(documents);
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
            
            Map<String,List<NamedEntitySurfaceForm>>
            
            class NamedEntitySurfaceForm {
                private String surfaceForm[];
                private List<NamedEntityCorrections> nes;
            }

            while (i < tokens.size())

            {
                if (map.containsKey(tokens.get(i))) {
                    NamedEntityCorrections match = null;
                    for (int j = 0; j < map.get(tokens.get(i)).size(); j++) {
                        NamedEntityCorrections dummy = map.get(tokens.get(i)).get(j);
                        int k = 0;
                        while (dummy.entity_text[k].equals(tokens.get(i + k)) && (k < dummy.entity_text.length)) {
                            k++;
                        }
                        if (k == dummy.entity_text.length) {
                            found = true;
                            match = dummy;
                            i += k;
                            break;
                        }
                    }
                    if (found == false) {
                        NamedEntityCorrections newentity = new NamedEntityCorrections(tokens.get(i).beginPosition(),
                                tokens.get(i).endPosition() - tokens.get(i).beginPosition() + 1, " ", Check.INSERTED);
                        doc.addMarking(newentity);

                    } else if ((found == true) && (match != null)) {

                        if (doc.getDocumentURI().equals(match.getDoc())) {
                            if (tokens.get(i).beginPosition() == match.getStartPosition()) {
                                break;
                            } else {
                                NamedEntityCorrections newentity = new NamedEntityCorrections(
                                        tokens.get(i).beginPosition(),
                                        tokens.get(i).endPosition() - tokens.get(i).beginPosition() + 1, " ",
                                        Check.INSERTED);
                                doc.addMarking(newentity);
                            }
                        } else {
                            NamedEntityCorrections newentity = new NamedEntityCorrections(tokens.get(i).beginPosition(),
                                    tokens.get(i).endPosition() - tokens.get(i).beginPosition() + 1, " ",
                                    Check.INSERTED);
                            doc.addMarking(newentity);
                        }

                    } else {
                        LOGGER.error(" Something is terribly wrong ");
                    }

                }

            }

        }

    }

    public void generate_map(List<Document> documents) {
        for (Document doc : documents) {
            List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);

            entities = nameEntity(doc, entities);

            for (NamedEntityCorrections entity : entities) {
                entity.setDoc(doc.getDocumentURI());
                if (map.containsKey(entity.getEntity_name())) {
                    map.get(entity.getEntity_name()).add(entity);
                }

                else {
                    List<NamedEntityCorrections> sub = new ArrayList<NamedEntityCorrections>();
                    sub.add(entity);
                    map.put(entity.getEntity_name(), sub);
                }
            }

        }

        // sorting the entries in descending order of the number od lemma ib the
        // text
        for (Map.Entry<String, List<NamedEntityCorrections>> list : map.entrySet()) {
            Collections.sort(list.getValue(), new Entity_LengthBasedComparator());
        }

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
            if (nePositions.get(token.beginPosition()) || nePositions.get(token.endPosition() - 1)) {
                // search for matching named entities
                int pos = 0;
                while ((pos < start.length) && (start[pos] <= token.endPosition())) {
                    // if the token and the
                    if ((start[pos] <= token.beginPosition()) && (end[pos] >= token.endPosition())) {
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
