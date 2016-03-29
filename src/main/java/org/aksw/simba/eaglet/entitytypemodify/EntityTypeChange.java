package org.aksw.simba.eaglet.entitytypemodify;

import java.util.List;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;

public class EntityTypeChange {

	
	
	public static List<Marking> changeType(Document d) {
		List<NamedEntity> original_list = d.getMarkings(NamedEntity.class);
		
		List<Marking> new_list = null;
		for(NamedEntity  entity: original_list)
		{
			new_list.add(new NamedEntityCorrections(entity.getStartPosition(), entity.getLength(), entity.getUri()));
		}

		return new_list;
	}
}
