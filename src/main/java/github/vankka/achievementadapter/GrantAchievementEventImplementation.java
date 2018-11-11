package github.vankka.achievementadapter;

import java.util.Optional;
import javax.annotation.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.achievement.GrantAchievementEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public class GrantAchievementEventImplementation extends AbstractEvent implements GrantAchievementEvent.TargetPlayer {
    private final Achievement achievement;
    private final MessageChannel originalChannel = Sponge.getServer().getBroadcastChannel();
    private final Text originalMessage;
    @Nullable private final Player player;
    @Nullable private MessageChannel messageChannel;
    private boolean cancelled = false;
    private boolean messageCancelled = false;

    public GrantAchievementEventImplementation(Achievement achievement, Text originalMessage, @Nullable Player player) {
        this.achievement = achievement;
        this.originalMessage = originalMessage;
        this.player = player;
    }

    @Override
    public Achievement getAchievement() {
        return achievement;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public MessageChannel getOriginalChannel() {
        return originalChannel;
    }

    @Override
    public Optional<MessageChannel> getChannel() {
        return messageChannel != null ? Optional.of(messageChannel) : Optional.empty();
    }

    @Override
    public void setChannel(@Nullable MessageChannel channel) {
        messageChannel = channel;
    }

    @Override
    public Text getOriginalMessage() {
        return originalMessage;
    }

    @Override
    public boolean isMessageCancelled() {
        return messageCancelled;
    }

    @Override
    public void setMessageCancelled(boolean cancelled) {
        messageCancelled = cancelled;
    }

    @Override
    public MessageFormatter getFormatter() {
        return new MessageFormatter(getOriginalMessage());
    }

    @Override
    public Cause getCause() {
        if (player != null)
            return Cause.source(player).build();
        else
            return Cause.source(this).build();
    }

    @SuppressWarnings("ConstantConditions") // *may* not be a TargetPlayer
    @Override
    public Player getTargetEntity() {
        return player;
    }
}
