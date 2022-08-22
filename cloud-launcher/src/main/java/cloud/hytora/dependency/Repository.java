package cloud.hytora.dependency;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class Repository {


    private final String name;
    private final String url;
}