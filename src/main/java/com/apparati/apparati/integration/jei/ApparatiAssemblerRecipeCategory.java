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
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/apparati_assembler.png");
    private final IDrawable background;
    private final String localizedName;

    public ApparatiAssemblerRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(GUI_TEXTURE, 5, 5, 166, 74);
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
        recipeLayout.getItemStacks().init(0, false, 89, 35); // Output
        recipeLayout.getItemStacks().set(0, ingredients.getOutputs(net.minecraft.item.ItemStack.class).get(0));

        // 3x3 grid
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int index = 1 + x + (y * 3);
                recipeLayout.getItemStacks().init(index, true, 25 + x * 18, 12 + y * 18);
                java.util.List<java.util.List<net.minecraft.item.ItemStack>> inputs = ingredients.getInputs(net.minecraft.item.ItemStack.class);
                if (x + (y * 3) < inputs.size()) {
                    recipeLayout.getItemStacks().set(index, inputs.get(x + (y * 3)));
                }
            }
        }
    }
}
