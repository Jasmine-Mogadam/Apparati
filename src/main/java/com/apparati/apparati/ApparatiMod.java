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

    // Helper method to determine if a block should be skipped from OreDictionary "block" registration
    private boolean shouldSkipBlockRegistration(Block block, ResourceLocation blockRegName) {
        String regPath = blockRegName.getResourcePath();

        // --- Diagnostic Logging ---
        if (regPath.contains("acacia_planks")) {
            LOGGER.info("  DEBUG: Encountered acacia_planks. isFullCube: {}, isOpaqueCube: {}, regPath: {}",
                    block.getDefaultState().isFullCube(), block.getDefaultState().isOpaqueCube(), regPath);
        }
        if (regPath.contains("stained_glass")) {
            LOGGER.info("  DEBUG: Encountered stained_glass. isFullCube: {}, isOpaqueCube: {}, regPath: {}",
                    block.getDefaultState().isFullCube(), block.getDefaultState().isOpaqueCube(), regPath);
        }
        if (regPath.contains("polished_granite") || regPath.contains("polished_diorite") || regPath.contains("polished_andesite")) {
             LOGGER.info("  DEBUG: Encountered polished stone variant. isFullCube: {}, isOpaqueCube: {}, regPath: {}",
                    block.getDefaultState().isFullCube(), block.getDefaultState().isOpaqueCube(), regPath);
        }
        if (regPath.contains("honeycomb_block")) {
            LOGGER.info("  DEBUG: Encountered honeycomb_block. isFullCube: {}, isOpaqueCube: {}, regPath: {}",
                    block.getDefaultState().isFullCube(), block.getDefaultState().isOpaqueCube(), regPath);
        }
        // --- End Diagnostic Logging ---


        // Strong whitelist for known full block suffixes, overriding other checks if they are indeed full/opaque cubes.
        // This is for blocks like "mushroom_block" or custom modded blocks ending in "_block" that should always be considered.
        if (regPath.endsWith("_block") && block.getDefaultState().isFullCube() && block.getDefaultState().isOpaqueCube()) {
            return false;
        }

        // Whitelist for common full blocks that might fail isFullCube/isOpaqueCube but should be treated as blocks
        // Examples: Stained Glass (not opaque), Planks (sometimes not strictly full cube due to model intricacies).
        // We explicitly check for common 'full block' keywords that are known to sometimes fail the strict Forge API checks.
        if (regPath.contains("planks") ||
            (regPath.contains("glass") && !regPath.contains("pane")) || // Include stained glass, exclude glass panes
            (regPath.contains("stone") && (regPath.contains("polished") || regPath.contains("granite") || regPath.contains("diorite") || regPath.contains("andesite"))) // Polished stone variants
            ) {
            return false; // Do not skip these if they match known full block patterns
        }


        // General exclusions for non-full blocks and other non-standard block types
        // These are more definitive non-blocks or blocks that clearly don't fit the 'solid block' definition.
        boolean isExcludedByNamingConvention = regPath.contains("_slab") || regPath.contains("_stairs") ||
                                               regPath.contains("_fence") || regPath.contains("_gate") ||
                                               regPath.contains("_wall") || regPath.contains("_button") ||
                                               regPath.contains("_pressure_plate") || regPath.contains("_door") ||
                                               regPath.contains("_trapdoor") || regPath.contains("_flower") ||
                                               regPath.contains("_plant") || regPath.contains("_vine") ||
                                               regPath.contains("torch") || regPath.contains("sign") ||
                                               regPath.equals("web") || regPath.equals("deadbush");

        // Finally, if it wasn't whitelisted by suffix or keyword, and it matches general exclusions or isn't a full/opaque cube, then skip.
        return isExcludedByNamingConvention || !block.getDefaultState().isFullCube() || !block.getDefaultState().isOpaqueCube();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("Attempting to register all *true* ItemBlocks to ore dictionary \"block\" in postInit...");
        int blockOreId = OreDictionary.getOreID("block");

        for (Block block : ForgeRegistries.BLOCKS) {
            Item item = Item.getItemFromBlock(block);
            ResourceLocation blockRegName = block.getRegistryName();
            
            if (item instanceof ItemBlock && blockRegName != null && item.getRegistryName().equals(blockRegName)) {
                // Pass the current block, its registry name, and its corresponding item to the helper method
                if (shouldSkipBlockRegistration(block, blockRegName)) { // Removed 'item' parameter as it's not used in the helper now
                    LOGGER.info("  Skipping block {} ({}): identified as non-standard or non-full block type.", blockRegName, item.getRegistryName()); // Log skipped blocks at this point
                    continue;
                }

                ItemStack blockStack = new ItemStack(block);
                if (!blockStack.isEmpty()) { // Ensure the stack is not empty
                    boolean alreadyRegisteredAsBlock = false;
                    for (int id : OreDictionary.getOreIDs(blockStack)) {
                        if (id == blockOreId) {
                            alreadyRegisteredAsBlock = true;
                            break;
                        }
                    }
                    if (!alreadyRegisteredAsBlock) {
                        OreDictionary.registerOre("block", blockStack);
                        LOGGER.info("  Registered {} as ore:block", blockRegName);
                    } else {
                        LOGGER.info("  {} is already registered as ore:block", blockRegName);
                    }
                } else {
                    LOGGER.info("  Skipping block {} as its ItemStack is empty.", blockRegName); // Added this case for clarity
                }
            } else if (blockRegName != null) {
                LOGGER.info("  Skipping block {} as it does not have a matching ItemBlock or is a special item derived from block (Item: {}).", blockRegName, item != null ? item.getRegistryName() : "null");
            }
        }
        LOGGER.info("Finished ore dictionary registration for ItemBlocks in postInit.");
    }
}
