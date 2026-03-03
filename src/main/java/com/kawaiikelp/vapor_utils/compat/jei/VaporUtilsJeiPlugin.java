package com.kawaiikelp.vapor_utils.compat.jei;

import com.kawaiikelp.vapor_utils.VaporUtils;
import com.kawaiikelp.vapor_utils.recipe.DryingRecipe;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsBlocks;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@JeiPlugin
public class VaporUtilsJeiPlugin implements IModPlugin {
    public static final RecipeType<DryingRecipe> DRYING_TYPE = RecipeType.create(VaporUtils.MODID, "drying", DryingRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(VaporUtils.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new DryingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var manager = Minecraft.getInstance().level.getRecipeManager();
        List<DryingRecipe> recipes = manager.getAllRecipesFor(VaporUtilsRecipes.DRYING_TYPE.get());
        registration.addRecipes(DRYING_TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // 건조대를 눌렀을 때 이 레시피 카테고리가 보이도록 설정!
        registration.addRecipeCatalyst(new ItemStack(VaporUtilsBlocks.getDefault().get()), DRYING_TYPE);
    }
}