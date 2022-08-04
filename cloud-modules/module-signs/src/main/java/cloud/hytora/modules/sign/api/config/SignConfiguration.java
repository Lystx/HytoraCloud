package cloud.hytora.modules.sign.api.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignConfiguration {

    /**
     * The knockback config
     */
    private final SignKnockbackConfig knockBackConfig;

    /**
     * The loading animation
     */
    private final SignAnimation loadingLayout;

    /**
     * The online sign layout
     */
    private final SignLayout onlineLayout;

    /**
     * The full sign layout
     */
    private final SignLayout fullLayout;

    /**
     * The maintenance sign layout
     */
    private final SignLayout maintenanceLayout;

    /**
     * The starting sign layout
     */
    private final SignLayout startingLayOut;


    public SignConfiguration() {

        //The default layouts
        SignLayout online = new SignLayout("ONLINE", new String[]{"&8│ &b{server.name} &8│", "&aAvailable", "{server.motd}", "&8× &7{server.online}&8/&7{server.max} &8×"}, "STAINED_CLAY", 5);
        SignLayout full = new SignLayout("FULL", new String[]{"&8│ &b{server.name} &8│", "&6VIP", "{server.motd}", "&8× &7{server.online}}&8/&7{server.max} &8×"}, "STAINED_CLAY", 1);
        SignLayout maintenance = new SignLayout("MAINTENANCE", new String[]{"", "&8│ &b{task.name} &8│", "&8× &cMaintenance &8×", ""}, "STAINED_CLAY", 3);
        SignLayout starting = new SignLayout("STARTING", new String[]{"", "&8│ &e{server.name} &8│", "&8× &0Starting... &8×", ""}, "STAINED_CLAY", 4);

        //The loading animation
        SignLayout loading1 = new SignLayout("LOADING", new String[]{"", "&8│ &bLoading... &8│", "&7{task.name} &8x &a⬛&7⬛⬛", ""}, "STAINED_CLAY", 14);
        SignLayout loading2 = new SignLayout("LOADING", new String[]{"", "&8│ &bLoading... &8│", "&7{task.name} &8x &a⬛⬛&7⬛", ""}, "STAINED_CLAY", 14);
        SignLayout loading3 = new SignLayout("LOADING", new String[]{"", "&8│ &bLoading... &8│", "&7{task.name} &8x &a⬛⬛⬛", ""}, "STAINED_CLAY", 14);
        SignLayout loading4 = new SignLayout("LOADING", new String[]{"", "&8│ &bLoading... &8│", "&7{task.name} &8x &7⬛⬛⬛", ""}, "STAINED_CLAY", 14);


        this.knockBackConfig = new SignKnockbackConfig(true, 0.7, 0.5, "cloud.hytora.module.signs.knockback.bypass");
        this.loadingLayout = new SignAnimation(20, loading1, loading2, loading3, loading4);
        this.onlineLayout = online;
        this.fullLayout = full;
        this.maintenanceLayout = maintenance;
        this.startingLayOut = starting;

    }
}
