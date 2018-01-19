package org.aksw.dice.eaglet.main;

import java.io.IOException;

import org.aksw.dice.eaglet.errorcheckpipeline.InputforPipeline;
import org.aksw.gerbil.exceptions.GerbilException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunPipeLine {
	/**
	 * The main class to execute the pipeline.
	 *
	 * @param args
	 * @throws GerbilException
	 * @throws IOException
	 */
	public static void main(String[] args) throws GerbilException, IOException {
		Logger LOGGER = LoggerFactory.getLogger(RunPipeLine.class);
		String filename = null;
		if (args.length == 0) {
			LOGGER.error("No Filename provided as input. The program will terminate!!!!");
			System.exit(0);
		} else if (args[0].equals("-n") && args[2].equals("-f")) {
			new InputforPipeline(args[1], args[3]);
		}

	}
}
