package github.vankka.achievementadapter.mixin;

import github.vankka.achievementadapter.AchievementAdapter;
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

@Mixin(value = StatisticsManagerServer.class)
public class MixinStatisticsManagerServer extends StatisticsManager {
    @Final @Shadow private Set<StatBase> dirty;
    @Final @Shadow private MinecraftServer mcServer;
    @Shadow private boolean hasUnsentAchievement;

    /**
     * @author AchievementAdapter
     * @reason GrantAchievementEvent isn't triggered normally
     */
    @Overwrite
    public void unlockAchievement(EntityPlayer p_unlockAchievement_1_, StatBase p_unlockAchievement_2_, int p_unlockAchievement_3_) {
        int lvt_4_1_ = p_unlockAchievement_2_.isAchievement() ? this.readStat(p_unlockAchievement_2_) : 0;

        // AchievementAdapter start
        if (p_unlockAchievement_2_.isAchievement() && lvt_4_1_ == 0 && p_unlockAchievement_3_ > 0) {
            TextComponentTranslation textComponentTranslation = new TextComponentTranslation("chat.type.achievement", p_unlockAchievement_1_.getDisplayName(), p_unlockAchievement_2_.createChatComponent());
            Text text = TextSerializers.JSON.deserialize(TextComponentTranslation.Serializer.componentToJson(textComponentTranslation));
            Player player = Sponge.getServer().getPlayer(p_unlockAchievement_1_.getUniqueID()).orElse(null);
            String achievement = p_unlockAchievement_2_.getStatName().getUnformattedText().toUpperCase().replace(' ', '_');

            GrantAchievementEventImplementation event = new GrantAchievementEventImplementation(DummyObjectProvider.createFor(Achievement.class, achievement), text, player);
            Sponge.getEventManager().post(event);

            if (event.isCancelled())
                return;

            if (this.mcServer.isAnnouncingPlayerAchievements() && !event.isCancelled() && !event.isMessageCancelled())
                event.getChannel().orElse(event.getOriginalChannel()).send(AchievementAdapter.class, event.getMessage());
        }
        // AchievementAdapter end

        super.unlockAchievement(p_unlockAchievement_1_, p_unlockAchievement_2_, p_unlockAchievement_3_);
        this.dirty.add(p_unlockAchievement_2_);
        if (p_unlockAchievement_2_.isAchievement() && lvt_4_1_ > 0 && p_unlockAchievement_3_ == 0) {
            this.hasUnsentAchievement = true;
            if (this.mcServer.isAnnouncingPlayerAchievements()) {
                this.mcServer.getPlayerList().sendChatMsg(new TextComponentTranslation("chat.type.achievement.taken", p_unlockAchievement_1_.getDisplayName(), p_unlockAchievement_2_.createChatComponent()));
            }
        }

        if (p_unlockAchievement_2_.isAchievement() && lvt_4_1_ == 0 && p_unlockAchievement_3_ > 0) {
            this.hasUnsentAchievement = true;

            // AchievementAdapter
//            if (this.mcServer.isAnnouncingPlayerAchievements()) {
//                this.mcServer.getPlayerList().sendChatMsg(new TextComponentTranslation("chat.type.achievement", p_unlockAchievement_1_.getDisplayName(), p_unlockAchievement_2_.createChatComponent()));
//            }
        }
    }
}
