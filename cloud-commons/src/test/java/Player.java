import cloud.hytora.context.annotations.*;

@ApplicationParticipant
@Configuration
public class Player {

    @CacheContext
    private Test testInstance;

    @JsonContext(file = "application.json", key = "version")
    private String version;

    private String name;

    @Constructor
    public void ignored() {
        System.out.println(testInstance);
        System.out.println(version);
    }


}
