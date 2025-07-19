package cloud.hytora.modules.dashboard.intern.util;

import cloud.hytora.document.gson.GsonHelper;
import cloud.hytora.modules.dashboard.DashboardModule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WebUser  {

    private boolean active;
    private String username;
    private byte[] passwordHash;
    private String sessionID;

    private List<WebPermission.PermissionType> grantedPermissions = new ArrayList<>();

    public WebUser(String username, byte[] passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public void disableAccount(){
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void enableAccount(){
        this.active = true;
    }


    public void logOut() {
        this.sessionID = null;
        this.save();
    }

    public String getSessionID() {
        return sessionID;
    }

    public boolean verify(byte[] salt, String password) throws InvalidKeySpecException {
        return DashboardModule.getInstance().getPasswordManager().verify(password, salt, passwordHash);
    }

    public void addPermission(WebPermission.PermissionType permission) {
        grantedPermissions.add(permission);
    }

    public void save() {
        File file = new File(DashboardModule.getInstance().getController().getDataFolder().toFile(), "users/" + this.username + ".json");

        try {
            FileWriter writer = new FileWriter(file);
            GsonHelper.PRETTY_GSON.toJson(this, writer);
            writer.close();

            DashboardModule.getInstance().getWebUserManager().cacheAllUsers();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getUsername() {
        return username;
    }

    public String generateSessionID() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        int length = 16;
        char[] data = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345667890".toCharArray();
        sessionID = "";
        for (int i = 0; i < length; i++) {
            sessionID += data[random.nextInt(data.length)];
        }

        save();
        return sessionID;

    }
}
