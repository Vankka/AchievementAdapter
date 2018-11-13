package github.vankka.achievementadapter.mixin;

import github.vankka.achievementadapter.GrantAchievementEventImplementation;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = StatisticsManagerServer.class)
public class MixinStatisticsManagerServer extends StatisticsManager {
    @Final @Shadow private Set<StatBase> dirty;
    @Final @Shadow private MinecraftServer mcServer;
    @Shadow private boolean hasUnsentAchievement;

    @Inject(method = "unlockAchievement", at = @At("HEAD"), cancellable = true)
    private void onUnlockAchievement(EntityPlayer p_unlockAchievement_1_, StatBase p_unlockAchievement_2_, int p_unlockAchievement_3_, CallbackInfo callbackInfo) {
        if (p_unlockAchievement_2_.isAchievement() && (p_unlockAchievement_2_.isAchievement() ? this.readStat(p_unlockAchievement_2_) : 0) == 0 && p_unlockAchievement_3_ > 0) {
            TextComponentTranslation textComponentTranslation = new TextComponentTranslation("chat.type.achievement", p_unlockAchievement_1_.getDisplayName(), p_unlockAchievement_2_.createChatComponent());
            Text text = TextSerializers.JSON.deserialize(TextComponentTranslation.Serializer.componentToJson(textComponentTranslation));
            Player player = Sponge.getServer().getPlayer(p_unlockAchievement_1_.getUniqueID()).orElse(null);
            Achievement achievement = DummyObjectProvider.createFor(Achievement.class, p_unlockAchievement_2_.getStatName().getUnformattedText().toUpperCase().replace(' ', '_'));

            GrantAchievementEventImplementation event = new GrantAchievementEventImplementation(achievement, text, player);
            Sponge.getEventManager().post(event);

            if (event.isCancelled()) {
                callbackInfo.cancel();
                return;
            }

            if (this.mcServer.isAnnouncingPlayerAchievements() && !event.isMessageCancelled())
                event.getChannel().orElse(event.getOriginalChannel()).send(this, event.getMessage());

            super.unlockAchievement(p_unlockAchievement_1_, p_unlockAchievement_2_, p_unlockAchievement_3_);
            this.dirty.add(p_unlockAchievement_2_);
            this.hasUnsentAchievement = true;

            callbackInfo.cancel();
        }
    }
}
