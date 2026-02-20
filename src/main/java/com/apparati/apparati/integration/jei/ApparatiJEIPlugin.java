package com.apparati.apparati.integration.jei;

import com.apparati.apparati.Constants;
import com.apparati.apparati.content.ApparatiPartItem;
import com.apparati.apparati.content.AssemblerShapedRecipe;
import com.apparati.apparati.content.BlockApparatiAssembler;
import com.apparati.apparati.content.GuiApparatiAssembler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import java.util.ArrayList;
import java.util.List;

@JEIPlugin
public class ApparatiJEIPlugin implements IModPlugin {
    public static final String ASSEMBLER_CATEGORY = Constants.MOD_ID + ".assembler";

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new ApparatiAssemblerRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(IModRegistry registry) {
        registry.handleRecipes(AssemblerShapedRecipe.class, ApparatiAssemblerRecipeWrapper::new, ASSEMBLER_CATEGORY);

        List<AssemblerShapedRecipe> recipes = new ArrayList<>();
        for (IRecipe recipe : CraftingManager.REGISTRY) {
            if (recipe instanceof AssemblerShapedRecipe) {
                recipes.add((AssemblerShapedRecipe) recipe);
            }
        }

        registry.addRecipes(recipes, ASSEMBLER_CATEGORY);
        
        registry.addRecipeCatalyst(new ItemStack(net.minecraft.item.Item.getItemFromBlock(net.minecraft.block.Block.getBlockFromName(Constants.MOD_ID + ":apparati_assembler"))), ASSEMBLER_CATEGORY);
        
        registry.addRecipeClickArea(GuiApparatiAssembler.class, 84, 40, 24, 18, ASSEMBLER_CATEGORY);

        // This is the important part for recipe lookup on items
        for (AssemblerShapedRecipe recipe : recipes) {
            registry.addRecipeCatalyst(recipe.getRecipeOutput(), ASSEMBLER_CATEGORY);
        }
    }
}
