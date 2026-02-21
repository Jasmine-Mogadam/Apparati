package com.apparati.apparati;

import com.apparati.apparati.Tags;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Tags.VERSION)
public class ApparatiMod {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.Instance(Constants.MOD_ID)
    public static ApparatiMod instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Initializing {}...", Tags.MOD_NAME);
        software.bernie.geckolib3.GeckoLib.initialize();
        net.minecraftforge.fml.common.network.NetworkRegistry.INSTANCE.registerGuiHandler(this, new com.apparati.apparati.content.GuiHandler());

        if (event.getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(com.apparati.apparati.content.ClientRegistrationHandler.class);
        }
    }

}
