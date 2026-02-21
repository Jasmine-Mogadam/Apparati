package com.apparati.apparati.integration.jei;

import com.apparati.apparati.Constants;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class ApparatiAssemblerRecipeCategory implements IRecipeCategory<ApparatiAssemblerRecipeWrapper> {
    private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation("minecraft", "textures/gui/container/crafting_table.png");
    private final IDrawable background;
    private final String localizedName;

    public ApparatiAssemblerRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(CRAFTING_TABLE_GUI_TEXTURES, 29, 16, 116, 54);
        this.localizedName = "Apparati Assembler"; // Should use I18n
    }

    @Override
    public String getUid() {
        return ApparatiJEIPlugin.ASSEMBLER_CATEGORY;
    }

    @Override
    public String getTitle() {
        return localizedName;
    }

    @Override
    public String getModName() {
        return Constants.MOD_NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ApparatiAssemblerRecipeWrapper recipeWrapper, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, false, 94, 18); // Output
        
        // Pass the full list of outputs to allow JEI to cycle through them
        java.util.List<java.util.List<net.minecraft.item.ItemStack>> outputs = ingredients.getOutputs(net.minecraft.item.ItemStack.class);
        if (!outputs.isEmpty()) {
            recipeLayout.getItemStacks().set(0, outputs.get(0));
        }

        // 3x3 grid
        java.util.List<java.util.List<net.minecraft.item.ItemStack>> inputs = ingredients.getInputs(net.minecraft.item.ItemStack.class);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int index = 1 + x + (y * 3);
                recipeLayout.getItemStacks().init(index, true, x * 18, y * 18);
                if (index - 1 < inputs.size()) {
                    recipeLayout.getItemStacks().set(index, inputs.get(index - 1));
                }
            }
        }
    }
}
