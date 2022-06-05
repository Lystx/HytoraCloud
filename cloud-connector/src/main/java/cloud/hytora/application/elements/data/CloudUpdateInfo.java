package cloud.hytora.application.elements.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CloudUpdateInfo {

    private String date;
    private String committer;
    private String type;
    private String message;
    private String versionNow;


    public String[] toArray() {
        return new String[]{date, committer, type, message, versionNow};
    }
}
