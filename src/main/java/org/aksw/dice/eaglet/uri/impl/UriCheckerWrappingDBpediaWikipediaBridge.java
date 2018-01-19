package org.aksw.dice.eaglet.uri.impl;

import java.util.Set;

import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.Correction;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.ErrorType;
import org.aksw.dice.eaglet.uri.UriChecker;
import org.aksw.gerbil.semantic.sameas.impl.wiki.WikiDbPediaBridgingSameAsRetriever;

public class UriCheckerWrappingDBpediaWikipediaBridge implements UriChecker {

	protected UriChecker decorated;
	protected WikiDbPediaBridgingSameAsRetriever bridge = new WikiDbPediaBridgingSameAsRetriever();

	public UriCheckerWrappingDBpediaWikipediaBridge(UriChecker decorated) {
		this.decorated = decorated;
	}

	@Override
	public ErrorType checkUri(String uri) {
		Set<String> retrievedUris = bridge.retrieveSameURIs(uri);
		ErrorType result;
		for (String retrievedUri : retrievedUris) {
			result = decorated.checkUri(retrievedUri);
			if (result != ErrorType.NOERROR) {
				return result;
			}
		}
		return ErrorType.NOERROR;
	}

}
