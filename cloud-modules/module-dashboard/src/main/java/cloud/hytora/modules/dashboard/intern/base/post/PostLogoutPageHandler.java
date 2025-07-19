package cloud.hytora.modules.dashboard.intern.base.post;

import cloud.hytora.modules.dashboard.DashboardModule;
import cloud.hytora.modules.dashboard.intern.util.WebUser;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class PostLogoutPageHandler implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        System.out.println("Handle logout");
        String sessionID = ctx.cookie("sessionID");
        if (sessionID == null) {
            System.out.println("Tried to log out nulled WebUser");
            return;
        }

        WebUser webUser = DashboardModule.getInstance().getWebUserManager().getUserBySessionID(sessionID);
        webUser.logOut();
        ctx.clearCookieStore();
        ctx.redirect("/");
        System.out.println("WebUser " + webUser.getUsername() + " has logged out!");
    }
}
