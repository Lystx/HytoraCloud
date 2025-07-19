package cloud.hytora.modules.dashboard.intern.base.get;

import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.modules.dashboard.intern.PageHandler;
import io.javalin.http.Context;
import io.marioslab.basis.template.TemplateContext;
import lombok.SneakyThrows;

public class LoginPageHandler extends PageHandler {

    public LoginPageHandler() {
        super(loader.load("/dashboard/login.html"));
    }

    @Override
    @SneakyThrows
    public void populateTemplateContext(Context ctx, TemplateContext templateContext) {
        String error = ctx.pathParamMap().get("error");
        if (error != null) {
            System.out.println("Error occured whilst trying to view login.html!");
            System.out.println("Error: " + error);
        }
    }

    @Override
    public BiSupplier<String, String> handleVariableReplace() {
        return null;
    }


    @Override
    public boolean isLoginPage() {
        return true;
    }
}
