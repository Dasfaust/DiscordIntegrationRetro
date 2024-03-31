package me.dasfaust.discordintegration;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import dcshadow.net.kyori.adventure.text.Component;
import dcshadow.net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.storage.Localization;
import de.erdbeerbaerlp.dcintegration.common.storage.linking.LinkManager;
import de.erdbeerbaerlp.dcintegration.common.util.DiscordMessage;
import de.erdbeerbaerlp.dcintegration.common.util.MessageUtils;
import de.erdbeerbaerlp.dcintegration.common.util.TextColors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.ServerChatEvent;

import java.util.UUID;

public class EventListener {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // TODO
        DiscordIntegrationMod.LOG.info("PlayerLoggedInEvent: " + event.player.getDisplayName());
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        if (!Localization.instance().discordChatMessage.isBlank()) {
            return;
        }
        // TODO: permissions
        /*if (!DiscordIntegration.INSTANCE.getServerInterface().playerHasPermissions(event.player.getUniqueID(), MinecraftPermission.SEMD_MESSAGES, MinecraftPermission.USER)) {
            return;
        }*/
        if (LinkManager.isPlayerLinked(event.player.getUniqueID()) && LinkManager.getLink(null, event.player.getUniqueID()).settings.hideFromDiscord) {
            return;
        }

        // TODO: ItemStacks? Can you even link items in chat in 1.7.10?
        String text = MessageUtils.escapeMarkdown(event.message.replace("@everyone", "[at]everyone").replace("@here", "[at]here"));

        if (DiscordIntegration.INSTANCE != null) {
            GuildMessageChannel channel = DiscordIntegration.INSTANCE.getChannel(Configuration.instance().advanced.chatOutputChannelID);
            if (channel == null) return;

            if (!Localization.instance().discordChatMessage.isBlank()) {
                if (Configuration.instance().embedMode.enabled && Configuration.instance().embedMode.chatMessages.asEmbed) {
                    final String avatarURL = Configuration.instance().webhook.playerAvatarURL.replace("%uuid%", event.player.getUniqueID().toString()).replace("%uuid_dashless%", event.player.getUniqueID().toString().replace("-", "")).replace("%name%", event.player.getDisplayName()).replace("%randomUUID%", UUID.randomUUID().toString());
                    if (!Configuration.instance().embedMode.chatMessages.customJSON.isBlank()) {
                        final EmbedBuilder b = Configuration.instance().embedMode.chatMessages.toEmbedJson(Configuration.instance().embedMode.chatMessages.customJSON
                            .replace("%uuid%", event.player.getUniqueID().toString())
                            .replace("%uuid_dashless%", event.player.getUniqueID().toString().replace("-", ""))
                            .replace("%name%", event.player.getDisplayName())
                            .replace("%randomUUID%", UUID.randomUUID().toString())
                            .replace("%avatarURL%", avatarURL)
                            .replace("%msg%", text)
                            .replace("%playerColor%", "" + TextColors.generateFromUUID(event.player.getUniqueID()).getRGB())
                        );
                        DiscordIntegration.INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    } else {
                        EmbedBuilder b = Configuration.instance().embedMode.chatMessages.toEmbed();
                        if (Configuration.instance().embedMode.chatMessages.generateUniqueColors) {
                            b = b.setColor(TextColors.generateFromUUID(event.player.getUniqueID()));
                        }
                        b = b.setAuthor(event.player.getDisplayName(), null, avatarURL)
                            .setDescription(text);
                        DiscordIntegration.INSTANCE.sendMessage(new DiscordMessage(b.build()));
                    }
                } else {
                    DiscordIntegration.INSTANCE.sendMessage(event.player.getDisplayName(), event.player.getUniqueID().toString(), new DiscordMessage(null, text, true), channel);
                }
            }
        }
    }
}
