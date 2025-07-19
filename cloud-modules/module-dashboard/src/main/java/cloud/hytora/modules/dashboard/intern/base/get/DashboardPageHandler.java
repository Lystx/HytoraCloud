package cloud.hytora.modules.dashboard.intern.base.get;

import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.modules.dashboard.intern.PageHandler;
import io.javalin.http.Context;
import io.marioslab.basis.template.TemplateContext;

public class DashboardPageHandler extends PageHandler {

    public DashboardPageHandler() {
        super(loader.load("/dashboard/index.html"));
    }

    @Override
    public void populateTemplateContext(Context context, TemplateContext templateContext) {
    }

    @Override
    public BiSupplier<String, String> handleVariableReplace() {
        return new BiSupplier<String, String>() {
            @Override
            public String supply(String s) {
                return s.replace("%sessionID%", context.cookie("sessionID"))
                        .replace("{dashboard.user}", context.cookie("user"))
                        ;
            }
        };
    }

    @Override
    public boolean isLoginPage() {
        return false;
    }
}
