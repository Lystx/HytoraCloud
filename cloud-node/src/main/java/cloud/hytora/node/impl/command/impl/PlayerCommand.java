package cloud.hytora.node.impl.command.impl;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.annotation.CommandAutoHelp;
import cloud.hytora.driver.command.annotation.CommandDescription;
import cloud.hytora.driver.command.annotation.SubCommand;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.connection.DefaultPlayerConnection;
import cloud.hytora.driver.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.database.impl.SectionedDatabase;
import cloud.hytora.node.impl.database.impl.section.DatabaseSection;

import java.util.List;
import java.util.UUID;

@Command(
        name = {"player", "players", "ps", "p"},
        scope = CommandScope.CONSOLE_AND_INGAME,
        permission = "cloud.command.use"
)
@CommandAutoHelp
@CommandDescription("Manages all players")
public class PlayerCommand {


    @SubCommand("list")
    @CommandDescription("Lists all players")
    public void executeList(CommandSender sender) {

        List<CloudPlayer> players = CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers();

        if (players.isEmpty()) {
            sender.sendMessage("§cThere are currently no players online!");
            return;
        }

        sender.sendMessage("§8");
        sender.sendMessage("§7Players (" + players.size() + ")§8:");

        for (CloudPlayer player : players) {
            sender.sendMessage("§b" + player.getName() + " §8[§e" + player.getProxyServer() + " | " + player.getServer() + "§8]");
        }
        sender.sendMessage("§8");
    }


    @SubCommand("debugSet")
    @CommandDescription("debug command")
    public void executeDebug(CommandSender sender) {
        SectionedDatabase database = NodeDriver.getInstance().getDatabaseManager().getDatabase();

        CloudOfflinePlayer player = new DefaultCloudOfflinePlayer(
                UUID.fromString("82e8f5a2-4077-407b-af8b-e8325cad7191"),
                "Lystx",
                new DefaultPlayerConnection("Proxy-1", new ProtocolAddress("127.0.0.1", -1), -1, true, false),
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                DocumentFactory.newJsonDocument()
        );

        database.getSection(CloudOfflinePlayer.class).insert(player);
        sender.sendMessage("Done!");
    }

    @SubCommand("debugGet")
    @CommandDescription("debug command")
    public void executeDebug2(CommandSender sender) {
        SectionedDatabase database = NodeDriver.getInstance().getDatabaseManager().getDatabase();

        CloudOfflinePlayer offlinePlayer = database.getSection(CloudOfflinePlayer.class).get("82e8f5a2-4077-407b-af8b-e8325cad7191");

        System.out.println("FOUND : " + offlinePlayer.getName());
        System.out.println(offlinePlayer.toString());

    }

    @SubCommand("debugEdit")
    @CommandDescription("debug command")
    public void executeDebug3(CommandSender sender) {
        SectionedDatabase database = NodeDriver.getInstance().getDatabaseManager().getDatabase();
        DatabaseSection<CloudOfflinePlayer> db = database.getSection(CloudOfflinePlayer.class);

        CloudOfflinePlayer offlinePlayer = db.get("82e8f5a2-4077-407b-af8b-e8325cad7191");
        Document properties = offlinePlayer.getProperties();

        properties.set("rank", "ADMIN");
        properties.set("ts3Verified", true);

        offlinePlayer.setProperties(properties);
        db.insert(offlinePlayer);

        System.out.println("Done edit!");

    }
}
