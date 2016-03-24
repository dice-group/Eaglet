package org.aksw.simba.eaglet.errorutils;
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


import java.util.concurrent.Semaphore;

import org.aksw.gerbil.database.ExperimentDAO;
import org.aksw.gerbil.datatypes.ExperimentTaskConfiguration;
import org.aksw.gerbil.evaluate.EvaluatorFactory;
import org.aksw.gerbil.execute.ExperimentTask;
import org.aksw.gerbil.semantic.sameas.SameAsRetriever;
import org.aksw.gerbil.web.config.RootConfig;
import org.aksw.simba.topicmodeling.concurrent.overseers.Overseer;
import org.aksw.simba.topicmodeling.concurrent.overseers.simple.SimpleOverseer;
import org.aksw.simba.topicmodeling.concurrent.reporter.LogReporter;
import org.aksw.simba.topicmodeling.concurrent.reporter.Reporter;
import org.aksw.simba.topicmodeling.concurrent.tasks.Task;
import org.aksw.simba.topicmodeling.concurrent.tasks.TaskObserver;
import org.apache.commons.lang3.text.translate.JavaUnicodeEscaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractExperimentTaskTest {

    private Throwable testError = null;
    private Semaphore mutex = new Semaphore(0);

    public void runTest(int experimentTaskId, ExperimentDAO experimentDAO, EvaluatorFactory evFactory,
            ExperimentTaskConfiguration configuration) {
        runTest(experimentTaskId, experimentDAO, RootConfig.createSameAsRetriever(), evFactory, configuration);
    }

    public void runTest(int experimentTaskId, ExperimentDAO experimentDAO, SameAsRetriever sameAsRetriever,
            EvaluatorFactory evFactory, ExperimentTaskConfiguration configuration) {
        ExperimentTask task = new ExperimentTask(experimentTaskId, experimentDAO, sameAsRetriever, evFactory,
                configuration);
        Overseer overseer = new SimpleOverseer();
        @SuppressWarnings("unused")
        Reporter reporter = new LogReporter(overseer);
        overseer.addObserver(new JUnitTestTaskObserver(this));
        overseer.startTask(task);
        // wait for the task to end
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
       
    }

    protected static class JUnitTestTaskObserver implements TaskObserver {

        private static final Logger LOGGER = LoggerFactory
                .getLogger(JUnitTestTaskObserver.class);

        private AbstractExperimentTaskTest testInstance;

        public JUnitTestTaskObserver(AbstractExperimentTaskTest testInstance) {
            this.testInstance = testInstance;
        }

        @Override
        public void reportTaskThrowedException(Task task, Throwable t) {
            testInstance.testError = t;
            LOGGER.error("Got an unexpected exception.", t);
            // If there was an error we have to release the mutex here
            testInstance.mutex.release();
        }

        @Override
        public void reportTaskFinished(Task task) {
            // If there was no error we have to release the mutex here
            testInstance.mutex.release();
        }

    }

   

        
    }

