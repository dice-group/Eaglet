package org.aksw.gscheck.errorcheckpipeline;

import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gscheck.error.CombinedTaggingError;
import org.aksw.gscheck.error.ErraticEntityError;
import org.aksw.gscheck.error.LongDescriptionError;
import org.aksw.gscheck.error.OverLappingError;
import org.aksw.gscheck.error.SubsetMarkingError;

public class CheckerPipeline {

	

	public static void PiPeStructure () throws GerbilException
	{
		//CombinedTaggingError .CombinedTagger();
		//ErraticEntityError.ErraticEntityProb();
		//LongDescriptionError.LongDescription();
		//OverLappingError.overlapcheck();
		SubsetMarkingError.subsetmark();
	}
	public static void main(String[] args) throws GerbilException 
	{
		PiPeStructure();
	}

}
