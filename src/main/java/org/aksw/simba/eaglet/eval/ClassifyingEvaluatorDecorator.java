/**
 * This file is part of General Entity Annotator Benchmark.
 *
 * General Entity Annotator Benchmark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * General Entity Annotator Benchmark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with General Entity Annotator Benchmark.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.aksw.simba.eaglet.eval;

import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.datatypes.marking.ClassifiedMarking;
import org.aksw.gerbil.evaluate.AbstractTypeTransformingEvaluatorDecorator;
import org.aksw.gerbil.evaluate.Evaluator;
import org.aksw.gerbil.evaluate.TypeTransformingEvaluatorDecorator;
import org.aksw.gerbil.matching.impl.clas.MarkingClassifier;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.simba.eaglet.entitytypemodify.ClassifiedEntityCheck;
import org.aksw.simba.eaglet.entitytypemodify.EntityCheck;

/**
 * This {@link TypeTransformingEvaluatorDecorator} transforms {@link Marking}
 * instances into {@link ClassifiedMarking} instances based on the given
 * {@link MarkingClassifier} instances.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 * @param <U>
 *            The {@link Marking} class
 * @param <V>
 *            The {@link ClassifiedMarking} class
 */
public class ClassifyingEvaluatorDecorator
        extends AbstractTypeTransformingEvaluatorDecorator<EntityCheck, ClassifiedEntityCheck> {

    protected MarkingClassifier<ClassifiedEntityCheck> classifiers[];

    public ClassifyingEvaluatorDecorator(Evaluator<ClassifiedEntityCheck> evaluator,
            @SuppressWarnings("unchecked") MarkingClassifier<ClassifiedEntityCheck>... classifiers) {
        super(evaluator);
        this.classifiers = classifiers;
    }

    @Override
    protected List<ClassifiedEntityCheck> changeType(List<EntityCheck> markings) {
        List<ClassifiedEntityCheck> classifiedMarkings = new ArrayList<ClassifiedEntityCheck>(markings.size());
        ClassifiedEntityCheck classifiedMarking;
        for (EntityCheck marking : markings) {
            classifiedMarking = new ClassifiedEntityCheck(marking);
            for (int i = 0; i < classifiers.length; ++i) {
                classifiers[i].classify(classifiedMarking);
            }
            classifiedMarkings.add(classifiedMarking);
        }
        return classifiedMarkings;
    }

}
