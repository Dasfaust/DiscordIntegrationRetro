package me.dasfaust.discordintegration;

import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.storage.Localization;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.minecraftforge.common.MinecraftForge;

import java.io.IOException;

public class CommonProxy {

    final EventListener listener = new EventListener();

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

        DiscordIntegrationMod.LOG.info("DiscordIntegration version " + DiscordIntegrationMod.VERSION);

        try {
            if (!DiscordIntegration.discordDataDir.exists()) {
                if (!DiscordIntegration.discordDataDir.mkdir()) {
                    DiscordIntegrationMod.LOG.error("Please create the folder '" + DiscordIntegration.discordDataDir.getAbsolutePath() + "' manually");
                }
            }

            DiscordIntegration.loadConfigs();

            if (Configuration.instance().general.botToken.equals("INSERT BOT TOKEN HERE")) {
                DiscordIntegrationMod.LOG.error("Please check the config file and set a bot token!");
            } else {
                MinecraftForge.EVENT_BUS.register(listener);
            }
        } catch (IOException | IllegalStateException e) {
            DiscordIntegrationMod.LOG.error("An error occurred while reading the config file:");
            DiscordIntegrationMod.LOG.error(e.getMessage());
            DiscordIntegrationMod.LOG.error(e.getCause());
        }
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {}

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {}

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerAboutToStartEvent event) {
        DiscordIntegration.INSTANCE = new DiscordIntegration(new ForgeServerInterface());

        try {
            //Wait a short time to allow JDA to get initialized
            DiscordIntegrationMod.LOG.info("Waiting for JDA to initialize...");
            for (int i = 0; i <= 5; i++) {
                if (DiscordIntegration.INSTANCE.getJDA() == null) {
                    Thread.sleep(1000);
                } else {
                    break;
                }
            }

            if (DiscordIntegration.INSTANCE.getJDA() != null) {
                Thread.sleep(2000);

                if (!Localization.instance().serverStarting.isBlank())
                    if (DiscordIntegration.INSTANCE.getChannel() != null) {
                        final MessageCreateData m;

                        if (Configuration.instance().embedMode.enabled && Configuration.instance().embedMode.startMessages.asEmbed)
                        {
                            m = new MessageCreateBuilder().setEmbeds(Configuration.instance().embedMode.startMessages.toEmbed().setDescription(Localization.instance().serverStarting).build()).build();
                        } else {
                            m = new MessageCreateBuilder().addContent(Localization.instance().serverStarting).build();
                        }

                        DiscordIntegration.startingMsg = DiscordIntegration.INSTANCE.sendMessageReturns(m, DiscordIntegration.INSTANCE.getChannel(Configuration.instance().advanced.serverChannelID));
                    }
            }
        } catch (InterruptedException | NullPointerException ignored) {
        }
    }
}
