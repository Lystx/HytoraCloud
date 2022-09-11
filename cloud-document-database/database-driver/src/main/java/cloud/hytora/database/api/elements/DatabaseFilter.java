package cloud.hytora.database.api.elements;

import cloud.hytora.common.function.BiSupplier;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@AllArgsConstructor
public enum DatabaseFilter {


    /**
     * Needed params : key, value
     */
    MATCH(entry -> {
        Object[] input = entry.getInput();
        String key = (String) input[0];
        Object value = input[1];

        DatabaseEntry databaseEntry = entry.getEntry();
        if (databaseEntry.contains(key)) {
            try {
                if (databaseEntry.get(key).toInstance(value.getClass()).equals(value)) {
                //if (databaseEntry.get(key).toString().equalsIgnoreCase(value.toString())) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }),

    ID(entry -> {
        DatabaseEntry databaseEntry = entry.getEntry();
        Object[] input = entry.getInput();
        String id = (String) input[0];
        return databaseEntry.getId().equalsIgnoreCase(id);
    }),

    SORT_DESC(entry -> {
       return false;
    }),

    SORT_ASC(entry -> {

        return false;
    }),
    ALL(e -> true);

    private final BiSupplier<FilterEntry, Boolean> function;


    public boolean checkFilter(DatabaseEntry entry, Object... input) {
        FilterEntry e = new FilterEntry(entry, input);
        return function.supply(e);
    }

    @Getter
    public static class FilterEntry {

        private final DatabaseEntry entry;
        private final Object[] input;

        public FilterEntry(DatabaseEntry entry, Object... input) {
            this.entry = entry;
            this.input = input;
        }
    }
}
