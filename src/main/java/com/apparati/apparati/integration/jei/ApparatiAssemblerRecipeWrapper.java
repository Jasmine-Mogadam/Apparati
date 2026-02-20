package com.apparati.apparati.integration.jei;

import com.apparati.apparati.content.ApparatiPartItem;
import com.apparati.apparati.content.AssemblerShapedRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApparatiAssemblerRecipeWrapper implements IRecipeWrapper {
    private final AssemblerShapedRecipe recipe;
    private static final List<String> MATERIALS = Arrays.asList(
            "minecraft:iron_block",
            "minecraft:gold_block",
            "minecraft:diamond_block",
            "minecraft:emerald_block",
            "minecraft:redstone_block",
            "minecraft:lapis_block"
    );

    public ApparatiAssemblerRecipeWrapper(AssemblerShapedRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<Ingredient> recipeIngredients = recipe.getIngredients();
        List<List<ItemStack>> inputs = new ArrayList<>();

        for (Ingredient ingredient : recipeIngredients) {
            ItemStack[] matches = ingredient.getMatchingStacks();
            List<ItemStack> variations = new ArrayList<>(Arrays.asList(matches));
            
            // If it's a block-placeholder (usually iron block in recipes), we add cycling materials
            boolean isBlockPlaceholder = false;
            for (ItemStack stack : matches) {
                if (stack.getItem() instanceof ItemBlock) {
                    Block block = ((ItemBlock) stack.getItem()).getBlock();
                    if (block.getRegistryName() != null && block.getRegistryName().toString().equals("minecraft:iron_block")) {
                        isBlockPlaceholder = true;
                        break;
                    }
                }
            }

            if (isBlockPlaceholder) {
                variations.clear();
                for (String material : MATERIALS) {
                    Block block = Block.getBlockFromName(material);
                    if (block != null) {
                        variations.add(new ItemStack(block));
                    }
                }
            }
            inputs.add(variations);
        }

        ingredients.setInputLists(ItemStack.class, inputs);

        // For output, we show cycling versions based on the materials
        List<ItemStack> outputs = new ArrayList<>();
        ItemStack baseOutput = recipe.getRecipeOutput();
        if (baseOutput.getItem() instanceof ApparatiPartItem) {
            for (String material : MATERIALS) {
                Block block = Block.getBlockFromName(material);
                if (block != null) {
                    ItemStack stack = baseOutput.copy();
                    NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
                    ResourceLocation registryName = block.getRegistryName();
                    if (registryName != null) {
                        tag.setString("BlockEntity", registryName.toString());
                        String matName = registryName.getResourcePath();
                        if (matName.endsWith("_block")) {
                            matName = matName.substring(0, matName.length() - 6);
                        }
                        tag.setString("Material", matName);
                    }
                    stack.setTagCompound(tag);
                    outputs.add(stack);
                }
            }
        } else {
            outputs.add(baseOutput);
        }
        ingredients.setOutputs(ItemStack.class, outputs);
    }
}
