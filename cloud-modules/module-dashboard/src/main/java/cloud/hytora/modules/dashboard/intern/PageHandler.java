package cloud.hytora.modules.dashboard.intern;

import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.modules.dashboard.DashboardModule;
import cloud.hytora.modules.dashboard.intern.util.WebUser;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.marioslab.basis.template.Template;
import io.marioslab.basis.template.TemplateContext;
import io.marioslab.basis.template.TemplateLoader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Getter @RequiredArgsConstructor
public abstract class PageHandler implements Handler {

    /**
     * The provided template
     */
    private final Template template;

    /**
     * The user that performs actions
     */
    private WebUser webUser;


    /**
     * The current context instance
     */
    protected Context context;

    /**
     * The template loader
     */
    protected static final TemplateLoader loader = new TemplateLoader.ClasspathTemplateLoader();

    /**
     * Sub-Method after calling {@link PageHandler#handle(Context)}
     *
     * @param context the context
     * @param templateContext the template context
     */
    public abstract void populateTemplateContext(Context context, TemplateContext templateContext);

    @Override
    public final void handle(@NotNull Context ctx) throws Exception {

        TemplateContext templateContext = new TemplateContext();

        this.context = ctx;
        populateTemplateContext(ctx, templateContext);

        String sessionID = ctx.cookie("sessionID");

        if (sessionID == null) {
            webUser = null;
        } else {
            System.out.println("SessionId was not null => " + sessionID);
            this.webUser = DashboardModule.getInstance().getWebUserManager().getUserBySessionID(sessionID);
        }

        if (this.webUser == null) {
            if (isLoginPage()) {
                //avoid overflow of loading same site over and over again
                return;
            }
            ctx.redirect("/");
        } else {
            if (isLoginPage()) {
                ctx.redirect("/dashboard");
                System.out.println(2);
                return;
            }
        }

        String render = template.render(templateContext);

        ctx.status(200);
        ctx.html(handleVariableReplace().supply(render));
    }


    public abstract BiSupplier<String, String> handleVariableReplace();

    /**
     * Checks if this is the login page
     */
    public abstract boolean isLoginPage();

}
