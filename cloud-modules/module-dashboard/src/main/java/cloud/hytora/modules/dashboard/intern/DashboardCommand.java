package cloud.hytora.modules.dashboard.intern;

import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.modules.dashboard.DashboardModule;
import cloud.hytora.modules.dashboard.intern.util.WebConfig;
import cloud.hytora.modules.dashboard.intern.util.WebPermission;
import cloud.hytora.modules.dashboard.intern.util.WebUser;

import java.security.spec.InvalidKeySpecException;


@Command(
        value = {"dashboard", "dash"},
        description = "Command to manage the DashboardModule",
        executionScope = CommandScope.CONSOLE
)
@Command.AutoHelp
public class DashboardCommand {

    private WebConfig config;

    public DashboardCommand(WebConfig config) {
        this.config = config;
    }

    @Command("create")
    @Command.Syntax("<username> <password>")
    public void execute(CommandSender sender, @Command.Argument("username") String username, @Command.Argument("password") String unhashedPassword) {


        WebUser user;
        try {
            user = new WebUser(username, DashboardModule.getInstance().getPasswordManager().generateHash(unhashedPassword, this.config.getSalt()));
            user.enableAccount();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            return;
        }

        for (WebPermission.PermissionType value : WebPermission.PermissionType.values()) {
            user.addPermission(value);
        }

        user.save();
        sender.sendMessage("§aAccount created!");
    }
}
