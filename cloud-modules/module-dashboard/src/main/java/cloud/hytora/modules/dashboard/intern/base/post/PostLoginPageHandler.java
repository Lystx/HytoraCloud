package cloud.hytora.modules.dashboard.intern.base.post;

import cloud.hytora.modules.dashboard.DashboardModule;
import cloud.hytora.modules.dashboard.intern.util.WebUser;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class PostLoginPageHandler implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");

        if (username == null || password == null) {
            ctx.redirect("/");
        }

        System.out.println("Logging in -> " + username + " with " + password);

        WebUser webUser = DashboardModule.getInstance().getWebUserManager().getUserByUsername(username);
        boolean verify = webUser.verify(DashboardModule.getInstance().getConfig().getSalt(), password);
        if (verify) {
            webUser.generateSessionID();
            ctx.cookie("sessionID", webUser.getSessionID());
            ctx.cookie("user", webUser.getUsername());
            ctx.redirect("/dashboard");
        } else {
            ctx.redirect("/?error=wrongPassword");
        }

    }
}
