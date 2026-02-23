package com.apparati.apparati.util;

import com.apparati.apparati.Constants;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlockOreDictionaryHelper {

    private static final Logger LOGGER = LogManager.getLogger("ApparatiOreDictHelper"); // Use a specific logger name for the helper

    public static void registerApparatiBlocks() {
        LOGGER.info("Attempting to register all *true* ItemBlocks to ore dictionary \"{}\" in registerApparatiBlocks...", Constants.ORE_DICT_BLOCK_TAG);
        int blockOreId = OreDictionary.getOreID(Constants.ORE_DICT_BLOCK_TAG);

        for (Block block : ForgeRegistries.BLOCKS) {
            ResourceLocation blockRegName = block.getRegistryName();
            Item item = Item.getItemFromBlock(block);

            // Ensure the item corresponds to an ItemBlock.
            // Note: We intentionally allow items with different registry names than their blocks
            // (e.g., "standing_sign" block vs "sign" item, or modded variants like BOP honeycomb).
            if (item instanceof ItemBlock && blockRegName != null) {
                
                // Scan all possible metadata values (0-32767) to ensure we find every valid variant,
                // even if mods don't report them correctly via getSubBlocks().
                // To prevent registering thousands of duplicates for blocks that ignore metadata or wrap it (like vanilla planks),
                // we filter by unique unlocalized names.
                java.util.Set<String> seenUnlocalizedNames = new java.util.HashSet<>();

                for (int meta = 0; meta <= Short.MAX_VALUE; meta++) {
                    ItemStack blockStack = new ItemStack(block, 1, meta);
                    if (!blockStack.isEmpty()) {
                        try {
                            // Validity Check: Try to fetch the unlocalized name.
                            // If this throws or returns null, the item variant is likely invalid.
                            String unlocalizedName = blockStack.getUnlocalizedName();
                            
                            // Duplicate Filter: Only register if we haven't seen this name before for this block.
                            if (unlocalizedName != null && !seenUnlocalizedNames.contains(unlocalizedName)) {
                                seenUnlocalizedNames.add(unlocalizedName);
                                registerBlockToOreDict(block, blockRegName, meta, blockStack, blockOreId);
                            }
                        } catch (Exception e) {
                            // Ignore invalid metadata states that cause crashes/errors
                        }
                    }
                }
            }
        }
        LOGGER.info("Finished ore dictionary registration for ItemBlocks in registerApparatiBlocks.");
    }

    private static void registerBlockToOreDict(Block block, ResourceLocation blockRegName, int meta, ItemStack stack, int blockOreId) {
        if (shouldSkipBlockRegistration(block, blockRegName, meta)) {
            LOGGER.info("  Skipping block {} (meta {}) because of shouldSkipBlockRegistration.", blockRegName, meta);
            return;
        }
        
        boolean alreadyRegisteredAsBlock = false;
        for (int id : OreDictionary.getOreIDs(stack)) {
            if (id == blockOreId) {
                alreadyRegisteredAsBlock = true;
                break;
            }
        }
        
        if (!alreadyRegisteredAsBlock) {
            OreDictionary.registerOre(Constants.ORE_DICT_BLOCK_TAG, stack);
            LOGGER.info("  Registered {} (meta {}) as ore:{}", blockRegName, meta, Constants.ORE_DICT_BLOCK_TAG);
        } else {
            LOGGER.info("  {} (meta {}) is already registered as ore:{}", blockRegName, meta, Constants.ORE_DICT_BLOCK_TAG);
        }
    }

    public static boolean shouldSkipBlockRegistration(Block block, ResourceLocation blockRegName, int metadata) {
        String regPath = blockRegName.getResourcePath();
        net.minecraft.block.state.IBlockState blockState = block.getStateFromMeta(metadata);

        if (regPath.endsWith("_block")) {
            return false;
        }

        if (regPath.contains("planks") ||
            (regPath.contains("glass") && !regPath.contains("pane")) ||
            (regPath.contains("stone") && (regPath.contains("polished") || regPath.contains("granite") || regPath.contains("diorite") || regPath.contains("andesite")))) {
            return false;
        }

        boolean isExcludedByNamingConvention = regPath.contains("_slab") || regPath.contains("_stairs") ||
                                               regPath.contains("_fence") || regPath.contains("_gate") ||
                                               regPath.contains("_wall") || regPath.contains("_button") ||
                                               regPath.contains("_pressure_plate") || regPath.contains("_door") ||
                                               regPath.contains("_trapdoor") || regPath.contains("_flower") ||
                                               regPath.contains("_plant") || regPath.contains("_vine") ||
                                               regPath.contains("torch") || regPath.contains("sign") ||
                                               regPath.equals("web") || regPath.equals("deadbush");

        // Use isNormalCube() to check for solid, redstone-conducting blocks.
        // Also check isFullCube() because the user wants to include things like glass which are full cubes but not "normal" (not opaque/conductive).
        // Fences and chests are NOT full cubes, so they will still be excluded.
        // So we keep the block if it's either a normal cube OR a full cube.
        // Skip if NEITHER normal NOR full.
        return isExcludedByNamingConvention || (!block.isNormalCube(blockState) && !blockState.isFullCube());
    }
}
