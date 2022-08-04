package cloud.hytora.modules.sign.api;


import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates a Group of CloudSigns
 * with Ids
 *
 * Could look like:
 * Lobby:
 *    1 : Lobby-1
 *    2 : Lobby-2
 *    3 : Lobby-3
 * BedWars:
 *    1 : BedWars-1
 *    2 : BedWars-2
 *    3 : BedWars-3
 *
 * It's simple to understand the logic of this
 * To sort the Signs in the SignSelector in Bukkit
 * the Signs in the {@link CloudSignGroup} are already
 * declared with an ID to iterate through all the signs
 * easily
 */
@Getter
public class CloudSignGroup {

    /**
     * The name of the sign group
     */
    private final String name;

    /**
     * The cloud signs stored in cache
     */
    private Map<Integer, CloudSign> cloudSigns;

    public CloudSignGroup(String name) {
        this.name = name;
        this.cloudSigns = new HashMap<>();
    }

    public CloudSignGroup(String name, Collection<CloudSign> cloudSigns) {
        this.name = name;
        this.cloudSigns = new HashMap<>();

        Map<Integer, CloudSign> map = new HashMap<>();
        int count = 1;
        for (CloudSign cloudSign : cloudSigns) {
            if (cloudSign.getTask().equalsIgnoreCase(name)) {
                map.put(count, cloudSign);
                count++;
            }
        }
        this.setCloudSigns(map);
    }

    /**
     * Sets the CloudSigns for this group
     *
     * @param cloudSigns the cloudSigns
     */
    public void setCloudSigns(Map<Integer, CloudSign> cloudSigns) {
        this.cloudSigns = cloudSigns;
    }

}
