import cloud.hytora.database.DatabaseDriver;
import cloud.hytora.database.api.elements.Database;
import cloud.hytora.database.api.elements.DatabaseCollection;
import cloud.hytora.database.api.elements.DatabaseEntry;
import cloud.hytora.database.api.elements.DatabaseFilter;
import cloud.hytora.http.HttpAddress;

import java.util.Collection;
import java.util.function.Consumer;

public class DatabaseTest {

    public static void main(String[] args) {

        DatabaseDriver driver = new DatabaseDriver("Node-2");
        driver.connect(new HttpAddress("127.0.0.1", 8890), "UA5pKMFR5U")
                .onTaskFailed(Throwable::printStackTrace)
                .onTaskSucess(e -> {

                    Database database = driver.getDatabase("cloudsystem");
                    DatabaseCollection collection = database.getCollectionOrCreate("players");
                    System.out.println("Collection found");

                    collection.filterEntriesAsync(DatabaseFilter.MATCH, "rank", "ADMIN")
                            .onTaskFailed(Throwable::printStackTrace)
                            .onTaskSucess(new Consumer<Collection<DatabaseEntry>>() {
                                @Override
                                public void accept(Collection<DatabaseEntry> databaseEntries) {
                                    System.out.println("FOUND : " + databaseEntries.size());
                                    for (DatabaseEntry databaseEntry : databaseEntries) {
                                        System.out.println(databaseEntry);
                                    }
                                }
                            });
                });

    }

}
