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

    public AssemblerShapedRecipe(ShapedRecipes recipe, char blockChar) {
        this.recipe = recipe;
        this.blockChar = blockChar;
    }

    @Override
    public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World worldIn) {
        return recipe.matches(inv, worldIn);
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
        ItemStack result = recipe.getCraftingResult(inv).copy();
        
        // Find the block used
        ItemStack blockStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock) {
                blockStack = stack;
                break;
            }
        }

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
                        throw new com.google.gson.JsonSyntaxException("Pattern references symbol '" + s + "' but it's not defined in the key");
                    }
                    ingredients.set(i * width + j, ingredient == null ? Ingredient.EMPTY : ingredient);
                }
            }

            ItemStack result = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), context);
            return new AssemblerShapedRecipe(new ShapedRecipes(group, width, height, ingredients, result), blockChar);
        }
    }
}
