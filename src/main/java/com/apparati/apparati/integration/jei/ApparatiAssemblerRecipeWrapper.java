package com.apparati.apparati.integration.jei;

import com.apparati.apparati.Constants;
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
import net.minecraftforge.oredict.OreDictionary;

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

        int width = recipe.getRecipeWidth();
        int height = recipe.getRecipeHeight();

        // JEI Category expects a list of 9 lists for the 3x3 grid
        for (int i = 0; i < 9; i++) {
            inputs.add(new ArrayList<>());
        }

        int blockOreId = OreDictionary.getOreID("block");

        // The recipeIngredients list in ShapedRecipes is row-major and size width*height
        for (int i = 0; i < recipeIngredients.size(); i++) {
            Ingredient ingredient = recipeIngredients.get(i);
            if (ingredient == Ingredient.EMPTY) continue;
            
            ItemStack[] matches = ingredient.getMatchingStacks();
            if (matches.length == 0) continue;

            // Determine if this ingredient is the block placeholder
            boolean isBlockPlaceholder = false;
            for (ItemStack stack : matches) {
                for (int id : OreDictionary.getOreIDs(stack)) {
                    if (id == blockOreId) {
                        isBlockPlaceholder = true;
                        break;
                    }
                }
                if (isBlockPlaceholder) break;
                
                if (stack.getItem() instanceof ItemBlock) {
                    Block b = ((ItemBlock)stack.getItem()).getBlock();
                    if (b.getRegistryName() != null && b.getRegistryName().toString().equals("minecraft:iron_block")) {
                        isBlockPlaceholder = true;
                        break;
                    }
                }
            }

            List<ItemStack> finalVariations;
            if (isBlockPlaceholder) {
                finalVariations = new ArrayList<>();
                for (String material : MATERIALS) {
                    Block block = Block.getBlockFromName(material);
                    if (block != null) {
                        finalVariations.add(new ItemStack(block));
                    }
                }
            } else {
                finalVariations = new ArrayList<>(Arrays.asList(matches));
            }
            
            // Map the recipe-local index (0 to width*height-1) to the 3x3 grid index (0 to 8)
            int row = i / width;
            int col = i % width;
            int gridIndex = col + (row * 3);
            
            if (gridIndex < 9) {
                inputs.set(gridIndex, finalVariations);
            }
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
