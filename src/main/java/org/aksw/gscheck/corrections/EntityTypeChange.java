package org.aksw.gscheck.corrections;

import java.util.List;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.transfer.nif.Document;

public class EntityTypeChange {

	
	
	public static List<NamedEntityCorrections> changeType(Document d) {
		List<NamedEntity> original_list = d.getMarkings(NamedEntity.class);
		
		List<NamedEntityCorrections> new_list = null;
		for(NamedEntity  entity: original_list)
		{
			new_list.add(new NamedEntityCorrections(entity.getStartPosition(), entity.getLength(), entity.getUris()));
		}

		return new_list;
	}
}
