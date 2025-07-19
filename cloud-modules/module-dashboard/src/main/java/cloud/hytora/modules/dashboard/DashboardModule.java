package cloud.hytora.modules.dashboard;

import cloud.hytora.document.wrapped.StorableDocument;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.http.api.HttpServer;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.AbstractModule;
import cloud.hytora.driver.module.controller.base.ModuleConfiguration;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.module.controller.base.ModuleEnvironment;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.modules.dashboard.handler.PlayerAuthCommand;
import cloud.hytora.modules.dashboard.handler.PlayerAuthHandler;
import cloud.hytora.modules.dashboard.intern.DashboardCommand;
import cloud.hytora.modules.dashboard.intern.base.get.DashboardPageHandler;
import cloud.hytora.modules.dashboard.intern.base.get.LoginPageHandler;
import cloud.hytora.modules.dashboard.intern.base.PasswordManager;
import cloud.hytora.modules.dashboard.intern.base.post.PostLoginPageHandler;
import cloud.hytora.modules.dashboard.intern.base.post.PostLogoutPageHandler;
import cloud.hytora.modules.dashboard.intern.util.WebConfig;
import cloud.hytora.modules.dashboard.intern.util.WebUserManager;
import cloud.hytora.modules.dashboard.router.*;
import io.javalin.Javalin;
import lombok.Getter;

import java.io.File;

@ModuleConfiguration(
        name = "module-dashboard",
        main = DashboardModule.class,
        author = "Lystx",
        version = "ALPHA-.01",
        copyType = ModuleCopyType.NONE,
        environment = ModuleEnvironment.NODE
)
@Getter
public class DashboardModule extends AbstractModule {


    @Getter
    private static DashboardModule instance;

    private Javalin javalin;
    private PasswordManager passwordManager;
    private WebUserManager webUserManager;
    private WebConfig config;

    public DashboardModule(ModuleController controller) {
        super(controller);
    }

    @ModuleTask(id = 0, state = ModuleState.LOADED)
    public void load() {
        File baseDir = new File(this.controller.getDataFolder().toFile(), "users/");
        baseDir.mkdirs();

        StorableDocument config = this.controller.getConfig();
        if (config.isEmpty()) {
            config.set(new WebConfig());
            config.save();
            this.config = new WebConfig();
        } else {
            this.config = config.toInstance(WebConfig.class);
        }

        if (this.config.getSalt() == null) {
            this.config.generateNewSalt();
            config.set(this.config);
            config.save();
        }
    }

    @ModuleTask(id = 1, state = ModuleState.ENABLED)
    public void enable() {
        instance = this;

        this.passwordManager = new PasswordManager();
        this.webUserManager = new WebUserManager();

        CloudDriver.getInstance().getCommandManager().registerCommand(new PlayerAuthCommand());

        HttpServer httpServer = CloudDriver.getInstance().getProvider(HttpServer.class);


        httpServer.getAuthRegistry().registerAuthMethodHandler("player", new PlayerAuthHandler());
        httpServer.getHandlerRegistry()
                .registerHandlers("v1", new V1PingRouter(), new V1HomeRouter(), new V1StatusRouter(), new V1UpgradeRouter(), new V1PlayerRouter());


        CloudDriver.getInstance().getLogger().info("§2Enabling %1HytoraCloud§8-%2Dashboard§8...");
        //change context ClassLoader to prevent Dependency-Collision
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(DashboardModule.class.getClassLoader());


        this.javalin = Javalin.create(config -> config.addStaticFiles("dashboard/"));
        this.javalin.get("/", new LoginPageHandler());

        this.javalin.get("/dashboard", new DashboardPageHandler());
        this.javalin.post("/post/login", new PostLoginPageHandler());
        this.javalin.post("/post/logout", new PostLogoutPageHandler());

        this.javalin.start(config.getPort());

        //change back to original ContextClassLoader
        Thread.currentThread().setContextClassLoader(classLoader);
        CloudDriver.getInstance().getCommandManager().registerCommand(new DashboardCommand(config));
    }

    @ModuleTask(id = 2, state = ModuleState.DISABLED)
    public void disable() {
        this.javalin.stop();
    }
}

 