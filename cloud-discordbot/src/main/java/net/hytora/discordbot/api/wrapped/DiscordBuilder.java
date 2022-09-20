package net.hytora.discordbot.api.wrapped;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@Getter
public class DiscordBuilder {

    @Getter @Setter
    private static Processor processor;

    public static DiscordBuilder newBuilder() {
        return new DiscordBuilder();
    }

    private final Collection<GatewayIntent> intents;
    private final Collection<DiscordListener<?>> listeners;

    private String token;
    private OnlineStatus status;
    private MemberCachePolicy memberCachePolicy;
    private Activity activity;

    private DiscordBuilder() {
        this.intents = new ArrayList<>();
        this.listeners = new ArrayList<>();

        this.memberCachePolicy = MemberCachePolicy.ONLINE;
        this.status = OnlineStatus.ONLINE;
        this.activity = Activity.of(Activity.ActivityType.WATCHING, "Coding tutorials");
    }

    public DiscordBuilder token(String token) {
        this.token = token;
        return this;
    }

    public DiscordBuilder status(OnlineStatus status) {
        this.status = status;
        return this;
    }

    public DiscordBuilder activity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public DiscordBuilder cachePolicy(MemberCachePolicy policy) {
        this.memberCachePolicy = policy;
        return this;
    }

    public DiscordBuilder appendIntent(GatewayIntent intent) {
        this.intents.add(intent);
        return this;
    }

    public <T extends GenericEvent> DiscordBuilder appendListener(DiscordListener<T> listener) {
        this.listeners.add(listener);
        return this;
    }

    public DiscordBuilder listeners(DiscordListener<?>... listeners) {
        this.listeners.addAll(Arrays.asList(listeners));
        return this;
    }


    public Discord build() {
        return processor.process(this);
    }

    public interface Processor {

        Discord process(DiscordBuilder builder);
    }
}
