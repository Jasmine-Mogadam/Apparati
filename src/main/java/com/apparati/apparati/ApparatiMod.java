package com.apparati.apparati;

import com.apparati.apparati.Tags;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Tags.VERSION)
public class ApparatiMod {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    /**
     * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
     *     Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
     * </a>
     */
    @Mod.Instance(Constants.MOD_ID)
    public static ApparatiMod instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Hello From {}!", Tags.MOD_NAME);
        software.bernie.geckolib3.GeckoLib.initialize();
        net.minecraftforge.fml.common.network.NetworkRegistry.INSTANCE.registerGuiHandler(this, new com.apparati.apparati.content.GuiHandler());
    }

}
