package cloud.hytora.modules.dashboard.intern.util;

import cloud.hytora.document.gson.GsonHelper;
import cloud.hytora.modules.dashboard.DashboardModule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WebUserManager {

    private List<WebUser> cachedUsers = new ArrayList<>();

    public WebUserManager(){
        cacheAllUsers();
    }

    public void cacheAllUsers(){
        File baseDir = new File(DashboardModule.getInstance().getController().getDataFolder().toFile(), "users/");

        for (File file : Objects.requireNonNull(baseDir.listFiles())) {
            if(file.isFile() && file.getName().endsWith(".json")){
                try {
                    WebUser webUser = GsonHelper.PRETTY_GSON.fromJson(new FileReader(file), WebUser.class);
                    cachedUsers.add(webUser);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    public WebUser loadUser(String username) {
        File file = new File(DashboardModule.getInstance().getController().getDataFolder().toFile(), "users/" + username + ".json");

        try {
            return GsonHelper.PRETTY_GSON.fromJson(new FileReader(file), WebUser.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public WebUser getUserBySessionID(String sessionID) {
        for (WebUser cachedUser : cachedUsers) {
            if(cachedUser.getSessionID().equals(sessionID)){
                return cachedUser;
            }
        }
        return null;
    }

    public WebUser getUserByUsername(String username) {
        for (WebUser cachedUser : cachedUsers) {
            if(cachedUser.getUsername().equalsIgnoreCase(username)){
                return cachedUser;
            }
        }
        return null;
    }
}
