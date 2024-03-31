package me.dasfaust.discordintegration;

import cpw.mods.fml.common.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;

@Mod(modid = DiscordIntegrationMod.MODID, version = DiscordIntegrationMod.VERSION, name = "DiscordIntegration", acceptedMinecraftVersions = "[1.7.10]", acceptableRemoteVersions = "*")
public class DiscordIntegrationMod {

    public static final String MODID = "discordintegration";
    public static final String VERSION = "1.0.0";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(serverSide = "me.dasfaust.discordintegration.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerAboutToStartEvent event) {
        proxy.serverStarting(event);
    }
}
