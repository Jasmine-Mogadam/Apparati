package com.apparati.apparati;

import com.apparati.apparati.Tags;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.NonNullList;

import java.util.List;

import com.apparati.apparati.util.BlockOreDictionaryHelper;

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
        com.apparati.apparati.network.ApparatiNetwork.init();

        if (event.getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(com.apparati.apparati.content.ClientRegistrationHandler.class);
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        BlockOreDictionaryHelper.registerApparatiBlocks();
    }
}
