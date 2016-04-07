package org.aksw.simba.eaglet.entitytypemodify;

import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.MeaningSpan;

public class EntityTypeChange {

    public static List<Marking> changeType(Document d) {
        List<MeaningSpan> original_list = d.getMarkings(MeaningSpan.class);
        List<Marking> new_list = new ArrayList<Marking>(original_list.size());
        changeListType(original_list, new_list,d);
        
        return new_list;
    }

    public static List<NamedEntityCorrections> changeListType(List<MeaningSpan> original_list) {
        List<NamedEntityCorrections> new_list = new ArrayList<NamedEntityCorrections>(original_list.size());
        changeListType(original_list, new_list, null);
        return new_list;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Marking> void changeListType(List<MeaningSpan> original_list, List<T> newList, Document d) {
        for (MeaningSpan entity : original_list) {
            newList.add(
                    (T) new NamedEntityCorrections(entity.getStartPosition(), entity.getLength(), entity.getUris(),d));
        }
    }
}
	