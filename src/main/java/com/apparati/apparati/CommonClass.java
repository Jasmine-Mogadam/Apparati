package com.apparati.apparati;

import com.apparati.apparati.platform.Services;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

public class CommonClass {

    public static void init() {

        Constants.LOG.info("Hello from Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
        
        ResourceLocation diamondLocation = Items.DIAMOND.getRegistryName();
        if (diamondLocation != null) {
            Constants.LOG.info("The ID for diamonds is {}", diamondLocation.toString());
        }

        if (Loader.isModLoaded(Constants.MOD_ID)) {

            Constants.LOG.info("Hello to " + Constants.MOD_ID);
        }
    }
}
