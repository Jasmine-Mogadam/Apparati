package com.apparati.apparati.content;

import com.apparati.apparati.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class AssemblerShapedRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    private final ShapedRecipes recipe;
    private final char blockChar;
    private final Ingredient placeholderIngredient;

    public AssemblerShapedRecipe(ShapedRecipes recipe, char blockChar, Ingredient placeholderIngredient) {
        this.recipe = recipe;
        this.blockChar = blockChar;
        this.placeholderIngredient = placeholderIngredient;
    }

    public Ingredient getPlaceholderIngredient() {
        return placeholderIngredient;
    }

    public int getRecipeWidth() {
        return recipe.recipeWidth;
    }

    public int getRecipeHeight() {
        return recipe.recipeHeight;
    }

    @Override
    public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World worldIn) {
        int width = recipe.recipeWidth;
        int height = recipe.recipeHeight;
        
        for (int i = 0; i <= 3 - width; ++i) {
            for (int j = 0; j <= 3 - height; ++j) {
                if (this.checkMatch(inv, i, j, true)) {
                    return true;
                }
                if (this.checkMatch(inv, i, j, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkMatch(InventoryCrafting inv, int startX, int startY, boolean reverse) {
        int width = recipe.recipeWidth;
        int height = recipe.recipeHeight;
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        ItemStack firstBlockFound = ItemStack.EMPTY;

        for (int x = 0; x < 3; ++x) {
            for (int y = 0; y < 3; ++y) {
                int relX = x - startX;
                int relY = y - startY;
                Ingredient target = Ingredient.EMPTY;

                if (relX >= 0 && relY >= 0 && relX < width && relY < height) {
                    if (reverse) {
                        target = ingredients.get(width - relX - 1 + relY * width);
                    } else {
                        target = ingredients.get(relX + relY * width);
                    }
                }

                ItemStack stack = inv.getStackInRowAndColumn(x, y);

                // Rely on Ingredient.apply() now that OreDictionary is reliably populated in ApparatiMod.postInit
                if (!target.apply(stack)) {
                    return false;
                }

                // If this slot is the placeholder ingredient (the "block"), verify consistency
                if (target == this.placeholderIngredient) {
                    if (firstBlockFound.isEmpty()) {
                        firstBlockFound = stack;
                    } else {
                        if (!ItemStack.areItemsEqual(firstBlockFound, stack) || !ItemStack.areItemStackTagsEqual(firstBlockFound, stack)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
        ItemStack result = recipe.getCraftingResult(inv).copy();
        
        // Find the block used in the recipe to transfer its texture/material properties to the result.
        // The placeholderIngredient corresponds to the 'block_char' defined in the recipe JSON.
        // This ingredient represents the main structural component (like a chassis) whose material
        // should be reflected in the final assembled item.
        ItemStack blockStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            // Check if the item in this slot matches the placeholder ingredient (the structural block)
            if (!stack.isEmpty() && placeholderIngredient.apply(stack)) {
                blockStack = stack;
                break;
            }
        }

        // If the structural block was found, write its information to the result's NBT.
        // This allows the client-side renderer to know which texture to apply to the model.
        if (!blockStack.isEmpty()) {
            NBTTagCompound tag = result.hasTagCompound() ? result.getTagCompound() : new NBTTagCompound();
            Block block = ((ItemBlock) blockStack.getItem()).getBlock();
            ResourceLocation registryName = block.getRegistryName();
            if (registryName != null) {
                tag.setString("BlockEntity", registryName.toString());
                
                // Also set material based on the block name for texture selection
                String material = registryName.getResourcePath();
                if (material.endsWith("_block")) {
                    material = material.substring(0, material.length() - 6);
                }
                tag.setString("Material", material);
            }
            tag.setInteger("BlockMeta", blockStack.getMetadata());
            result.setTagCompound(tag);
        }

        return result;
    }

    @Override
    public boolean canFit(int width, int height) {
        return recipe.canFit(width, height);
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        return recipe.getRecipeOutput();
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        return recipe.getRemainingItems(inv);
    }

    @Nonnull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return recipe.getIngredients();
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public String getGroup() {
        return recipe.getGroup();
    }

    public static class Factory implements IRecipeFactory {
        @Override
        public IRecipe parse(JsonContext context, JsonObject json) {
            String group = JsonUtils.getString(json, "group", "");
            char blockChar = JsonUtils.getString(json, "block_char", "I").charAt(0);
            
            Map<String, Ingredient> key = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : JsonUtils.getJsonObject(json, "key").entrySet()) {
                key.put(entry.getKey(), CraftingHelper.getIngredient(entry.getValue(), context));
            }
            
            JsonArray patternJson = JsonUtils.getJsonArray(json, "pattern");
            String[] pattern = new String[patternJson.size()];
            for (int i = 0; i < pattern.length; i++) {
                pattern[i] = patternJson.get(i).getAsString();
            }

            int width = pattern[0].length();
            int height = pattern.length;
            
            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    String row = pattern[i];
                    String s = row.substring(j, j + 1);
                    Ingredient ingredient = key.get(s);
                    if (ingredient == null && !s.equals(" ")) {
                        throw new com.google.gson.JsonSyntaxException("Pattern references symbol \"" + s + "\" but it\"s not defined in the key");
                    }
                    ingredients.set(i * width + j, ingredient == null ? Ingredient.EMPTY : ingredient);
                }
            }

            ItemStack result = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), context);

            // Use the actual ingredient defined in the recipe key for the block character
            // This ensures that the Ingredient object we use for checking consistency in checkMatch
            // is the exact same object instance as the one in the recipe's ingredient list.
            Ingredient blockIngredient = key.get(String.valueOf(blockChar));
            
            if (blockIngredient == null) {
                 // Fallback: If for some reason the blockChar isn't in the key (e.g. malformed recipe or not used), create a default one.
                 // Ideally this case shouldn't be hit for valid recipes.
                 blockIngredient = Ingredient.fromStacks(OreDictionary.getOres(Constants.ORE_DICT_BLOCK_TAG).toArray(new ItemStack[0]));
            }

            return new AssemblerShapedRecipe(new ShapedRecipes(group, width, height, ingredients, result), blockChar, blockIngredient);
        }
    }
}
