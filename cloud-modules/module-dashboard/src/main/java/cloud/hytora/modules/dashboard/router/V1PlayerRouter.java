package cloud.hytora.modules.dashboard.router;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.http.api.*;
import cloud.hytora.driver.common.objects.CloudJsonEntity;
import cloud.hytora.driver.entity.player.CloudPlayer;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.stream.Collectors;

@HttpRouter("player")
public class V1PlayerRouter {


    @HttpEndpoint(method = HttpMethod.GET, path = "online", permission = "web.player.online")
    public void getOnline(@Nonnull HttpContext context) {
        Collection<CloudPlayer> players = CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers();

        context.getResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(players.stream().map(CloudJsonEntity::toDocument).collect(Collectors.toList()))
                .setStatusCode(HttpCodes.OK)
                .getContext()
                .closeAfter(true)
                .cancelNext(true);
    }

    @HttpEndpoint(method = HttpMethod.GET, path = "online/count", permission = "web.player.online.count")
    public void getOnlineCount(@Nonnull HttpContext context) {
        context.getResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(Document.gson("count", CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers().size()))
                .setStatusCode(HttpCodes.OK)
                .getContext()
                .closeAfter(true)
                .cancelNext(true);
    }

    @HttpEndpoint(method = HttpMethod.GET, path = "registered/count", permission = "web.player.registered.count")
    public void getRegisteredCount(@Nonnull HttpContext context) {
        context.getResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(Document.gson("count", -1))
                .setStatusCode(HttpCodes.OK)
                .getContext()
                .closeAfter(true)
                .cancelNext(true);
    }

    @HttpEndpoint(method = HttpMethod.POST, path = "{player}/kick", permission = "web.player.action.kick")
    public void postPlayerKick(@Nonnull HttpContext context) {
    }

}
