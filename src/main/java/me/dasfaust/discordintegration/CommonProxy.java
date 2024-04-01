package me.dasfaust.discordintegration;

import java.io.IOException;

import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.storage.Localization;

public class CommonProxy {

    public EventListener listener = new EventListener();

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

        DiscordIntegrationMod.LOG.info("DiscordIntegration version " + Tags.VERSION);

        try {
            if (!DiscordIntegration.discordDataDir.exists()) {
                if (!DiscordIntegration.discordDataDir.mkdir()) {
                    DiscordIntegrationMod.LOG.error(
                        "Please create the folder '" + DiscordIntegration.discordDataDir.getAbsolutePath()
                            + "' manually");
                }
            }

            DiscordIntegration.loadConfigs();

            if (Configuration.instance().general.botToken.equals("INSERT BOT TOKEN HERE")) {
                DiscordIntegrationMod.LOG.error("Please check the config file and set a bot token!");
            } else {
                MinecraftForge.EVENT_BUS.register(listener);
                FMLCommonHandler.instance()
                    .bus()
                    .register(listener);
            }
        } catch (IOException | IllegalStateException e) {
            DiscordIntegrationMod.LOG.error("An error occurred while reading the config file:");
            DiscordIntegrationMod.LOG.error(e.getMessage());
            DiscordIntegrationMod.LOG.error(e.getCause());
        }
    }

    public void serverStarting(FMLServerStartingEvent event) {
        DiscordIntegration.INSTANCE = new DiscordIntegration(new ForgeServerInterface());

        try {
            // Wait a short time to allow JDA to get initialized
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

                if (!Localization.instance().serverStarting.isEmpty()) {
                    if (DiscordIntegration.INSTANCE.getChannel() != null) {
                        final MessageCreateData m;

                        if (Configuration.instance().embedMode.enabled
                            && Configuration.instance().embedMode.startMessages.asEmbed) {
                            m = new MessageCreateBuilder()
                                .setEmbeds(
                                    Configuration.instance().embedMode.startMessages.toEmbed()
                                        .setDescription(Localization.instance().serverStarting)
                                        .build())
                                .build();
                        } else {
                            m = new MessageCreateBuilder().addContent(Localization.instance().serverStarting)
                                .build();
                        }

                        DiscordIntegration.startingMsg = DiscordIntegration.INSTANCE.sendMessageReturns(
                            m,
                            DiscordIntegration.INSTANCE.getChannel(Configuration.instance().advanced.serverChannelID));
                    }
                }
            }
        } catch (InterruptedException | NullPointerException ignored) {}
    }

    public void serverStarted(FMLServerStartedEvent event) {
        listener.onServerStarted(event);
    }

    public void serverStopping(FMLServerStoppingEvent event) {
        listener.onServerStopping(event);
    }

    public void serverStopped(FMLServerStoppedEvent event) {
        listener.onServerStopped(event);
    }
}
