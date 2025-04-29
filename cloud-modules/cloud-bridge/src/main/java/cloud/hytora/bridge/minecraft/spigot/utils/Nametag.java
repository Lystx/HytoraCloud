package cloud.hytora.bridge.minecraft.spigot.utils;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class Nametag {


    
    public void updateNameTags(Player player) {
        this.updateNameTags(player, null);
    }

    public void updateNameTags(Player player, Function<Player, PermissionGroup> playerPermissionGroupFunction) {
        this.updateNameTags(player, playerPermissionGroupFunction, null);
    }

    public void updateNameTags(Player player,
                               Function<Player, PermissionGroup> playerPermissionGroupFunction,
                               Function<Player, PermissionGroup> allOtherPlayerPermissionGroupFunction) {
        PermissionManager pm = CloudDriver.getInstance().getProvider(PermissionManager.class);
        if (pm == null) {
            return;
        }


        PermissionPlayer permissionPlayer = pm.getPlayerByUniqueIdOrNull(player.getUniqueId());
        PermissionGroup playerPermissionGroup = playerPermissionGroupFunction != null ? playerPermissionGroupFunction.apply(player) : permissionPlayer.getHighestGroup();

        initScoreboard(player);

        for (Player all : player.getServer().getOnlinePlayers()) {
            initScoreboard(all);

            if (playerPermissionGroup != null) {
                addTeamEntry(player, all, playerPermissionGroup);
            }

            PermissionGroup targetPermissionGroup = allOtherPlayerPermissionGroupFunction != null ? allOtherPlayerPermissionGroupFunction.apply(
                    all) : null;

            if (targetPermissionGroup == null) {
                targetPermissionGroup = pm.getPlayerByUniqueIdOrNull(all.getUniqueId()).getHighestGroup();
            }

            if (targetPermissionGroup != null) {
                addTeamEntry(all, player, targetPermissionGroup);
            }

        }
    }

   
    private void initScoreboard(Player all) {
        if (all.getScoreboard() == null) {
            all.setScoreboard(all.getServer().getScoreboardManager().getNewScoreboard());
        }
    }

    private void addTeamEntry(Player target, Player all, PermissionGroup permissionGroup) {
        String teamName = permissionGroup.getSortId() + permissionGroup.getName();
        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16);
            CloudDriver.getInstance()
                    .getLogger().debug("In order to prevent issues, the name (+ tagID) of the group " + permissionGroup.getName() + " was temporarily shortened to 16 characters!");
            
        }
        Team team = all.getScoreboard().getTeam(teamName);
        if (team == null) {
            team = all.getScoreboard().registerNewTeam(teamName);
        }

        if (permissionGroup.getPrefix().length() > 16) {
            permissionGroup.setPrefix(permissionGroup.getPrefix().substring(0, 16));
            CloudDriver.getInstance().getLogger().debug("In order to prevent issues, the prefix of the group " + permissionGroup.getName() + " was temporarily shortened to 16 characters!");
            CloudDriver.getInstance().getLogger().debug("Please fix this issue by changing the prefix in your perms.yml");

        }
        if (permissionGroup.getSuffix().length() > 16) {
            permissionGroup.setSuffix(permissionGroup.getSuffix().substring(0, 16));
            CloudDriver.getInstance().getLogger().debug("In order to prevent issues, the suffix of the group " + permissionGroup.getName() + " was temporarily shortened to 16 characters!");
            CloudDriver.getInstance().getLogger().debug("Please fix this issue by changing the suffix in your perms.yml");
        }

        try {
            Method setColor = team.getClass().getDeclaredMethod("setColor", ChatColor.class);
            setColor.setAccessible(true);
            if (permissionGroup.getChatColor().length() != 0) {
                setColor.invoke(team, ChatColor.getByChar(permissionGroup.getChatColor().replaceAll("&", "").replaceAll("§", "")));
            } else {
                setColor.invoke(team, ChatColor.getByChar(ChatColor.getLastColors(permissionGroup.getPrefix().replace('&', '§'))
                        .replaceAll("&", "")
                        .replaceAll("§", "")));
            }
        } catch (NoSuchMethodException ignored) {
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }


        team.setPrefix(ChatColor.translateAlternateColorCodes('&', permissionGroup.getPrefix()));
        team.setSuffix(ChatColor.translateAlternateColorCodes('&', permissionGroup.getSuffix()));

        team.addEntry(target.getName());

      //target.setDisplayName(ChatColor.translateAlternateColorCodes('&', "DD" + target.getName()));
    }

}
