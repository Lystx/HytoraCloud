package cloud.hytora.modules.sign.api.config;

import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.modules.sign.api.ICloudSign;
import cloud.hytora.modules.sign.api.SignState;
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
    private final SignAnimation onlineLayout;

    /**
     * The full sign layout
     */
    private final SignAnimation fullLayout;

    /**
     * The maintenance sign layout
     */
    private final SignAnimation maintenanceLayout;

    /**
     * The starting sign layout
     */
    private final SignAnimation startingLayOut;


    public SignConfiguration() {

        //The default layouts
        SignLayout full = new SignLayout(new String[]{"&8│ &b{server.name} &8│", "&6VIP", "{server.motd}", "&8× &7{server.online}}&8/&7{server.max} &8×"}, "STAINED_CLAY", 1);
        SignLayout maintenance = new SignLayout(new String[]{"", "&8│ &b{task.name} &8│", "&8× &cMaintenance &8×", ""}, "STAINED_CLAY", 3);

        //The loading animation
        SignLayout loading1 = new SignLayout(new String[]{"", "&8│ &bLoading... &8│", "&7{task.name} &8x &a⬛&7⬛⬛", ""}, "STAINED_CLAY", 14);
        SignLayout loading2 = new SignLayout(new String[]{"", "&8│ &bLoading... &8│", "&7{task.name} &8x &a⬛⬛&7⬛", ""}, "STAINED_CLAY", 14);
        SignLayout loading3 = new SignLayout(new String[]{"", "&8│ &bLoading... &8│", "&7{task.name} &8x &a⬛⬛⬛", ""}, "STAINED_CLAY", 14);
        SignLayout loading4 = new SignLayout(new String[]{"", "&8│ &bLoading... &8│", "&7{task.name} &8x &7⬛⬛⬛", ""}, "STAINED_CLAY", 14);

        //The starting animation
        SignLayout loading5 = new SignLayout(new String[]{"", "&8│ &e{server.name} &8│", "&8× &0Starting. &8×", ""}, "STAINED_CLAY", 4);
        SignLayout loading6 = new SignLayout(new String[]{"", "&8│ &e{server.name} &8│", "&8× &0Starting.. &8×", ""}, "STAINED_CLAY", 4);
        SignLayout loading7 = new SignLayout(new String[]{"", "&8│ &e{server.name} &8│", "&8× &0Starting... &8×", ""}, "STAINED_CLAY", 4);
        SignLayout loading8 = new SignLayout(new String[]{"", "&8│ &e{server.name} &8│", "&8× &0Starting.. &8×", ""}, "STAINED_CLAY", 4);
        SignLayout loading9 = new SignLayout(new String[]{"", "&8│ &e{server.name} &8│", "&8× &0Starting. &8×", ""}, "STAINED_CLAY", 4);
        SignLayout loading10 = new SignLayout(new String[]{"", "&8│ &e{server.name} &8│", "&8× &0Starting &8×", ""}, "STAINED_CLAY", 4);

        this.knockBackConfig = new SignKnockbackConfig(true, 0.7, 0.5, "cloud.hytora.module.signs.knockback.bypass");
        this.loadingLayout = new SignAnimation(20, loading1, loading2, loading3, loading4);
        this.onlineLayout = new SignAnimation(
                20,
                new SignLayout(new String[]{"&8│ &b{server.name} &8│", "&aAvailable", "{server.motd}", "&8× &7{server.online}&8/&7{server.max} &8×"}, "STAINED_CLAY", 5),
                new SignLayout(new String[]{"&8│ &b{server.name} &8│", "&aUptime:", "{server.uptime}", "&8× &7{server.online}&8/&7{server.max} &8×"}, "STAINED_CLAY", 5),
                new SignLayout(new String[]{"&8│ &b{server.name} &8│", "&aUptime:", "{server.uptime}", "&8× &7{server.online}&8/&7{server.max} &8×"}, "STAINED_CLAY", 5),
                new SignLayout(new String[]{"&8│ &b{server.name} &8│", "&aAvailable", "{server.motd}", "&8× &7{server.online}&8/&7{server.max} &8×"}, "STAINED_CLAY", 5)

        );
        this.fullLayout = new SignAnimation(20, full);
        this.maintenanceLayout = new SignAnimation(20, maintenance);
        this.startingLayOut = new SignAnimation(20, loading5, loading6, loading7, loading8, loading9, loading10);

    }


    public SignAnimation getAnimationByState(SignState state) {
        switch (state) {
            case STARTING:
                return startingLayOut;
            case ONLINE:
                return onlineLayout;
            case FULL:
                return fullLayout;
            case OFFLINE:
                return loadingLayout;
            case MAINTENANCE:
                return maintenanceLayout;
            default:
                return loadingLayout;
        }
    }
}
