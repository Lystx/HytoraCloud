package cloud.hytora.modules.npc.spigot.entity.skin;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.http.HttpClient;
import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

public class DefaultSkinFetcher implements SkinFetcher {

    private final HttpClient httpClient;
    private final Collection<SkinFetcherServer> skinFetcherServers;

    public DefaultSkinFetcher(HttpClient httpClient, SkinFetcherServer... servers) {
        this.httpClient = httpClient;
        this.skinFetcherServers = Arrays.asList(servers);
    }


    @Override
    public Task<GameProfile> fetchGameProfile(String name, @Nullable SkinFetcherListener listener) {

        Task<GameProfile> task = Task.empty();

        getProfiles(name)
                .onTaskSucess(gameProfiles -> {

                    List<GameProfile> retrievedGameProfiles = new ArrayList<>();
                    for (GameProfile gameProfile : gameProfiles) {
                        if (gameProfile == null) {
                            continue;
                        }
                        retrievedGameProfiles.add(gameProfile);
                    }


                    if (!retrievedGameProfiles.isEmpty()) {
                        GameProfile gameProfile = retrievedGameProfiles.get(0);
                        if (listener != null) {
                            listener.onComplete(gameProfile);
                        }
                        task.setResult(gameProfile);
                    } else {
                        task.setFailure(new SkinException("No Skin found for '" + name + "'"));
                    }


                }).onTaskFailed(task::setFailure);


        return task;
    }

    private Task<List<GameProfile>> getProfiles(String name) {
        Task<List<GameProfile>> task =  Task.empty();
        List<GameProfile> list = new ArrayList<>();


        for (SkinFetcherServer skinServer : skinFetcherServers) {
            try {
                HttpClient.Response httpResponse = httpClient.send(
                        skinServer.prepareRequest(name));

                GameProfile gameProfile = skinServer.readProfile(name, httpResponse);
                list.add(gameProfile);
            } catch (Exception e) {
                task.setFailure(new SkinException(name, e));
            }
        }
        task.setResult(list);
        return task;
    }
}
