package com.kawaiikelp.vapor_utils.compat.jei;

import com.kawaiikelp.vapor_utils.VaporUtils;
import com.kawaiikelp.vapor_utils.recipe.DryingRecipe;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class DryingRecipeCategory implements IRecipeCategory<DryingRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    public DryingRecipeCategory(IGuiHelper helper) {
        // 배경은 깔끔하게 빈 공간으로! (필요하면 텍스처 그려♡)
        this.background = helper.createBlankDrawable(100, 40);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(VaporUtilsBlocks.getDefault().get()));
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<DryingRecipe> getRecipeType() {
        return VaporUtilsJeiPlugin.DRYING_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.vapor_utils.drying");
    }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DryingRecipe recipe, IFocusGroup focuses) {
        // 왼쪽 입력 슬롯
        builder.addSlot(RecipeIngredientRole.INPUT, 15, 11).addIngredients(recipe.getInput());
        // 오른쪽 출력 슬롯
        builder.addSlot(RecipeIngredientRole.OUTPUT, 70, 11).addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(DryingRecipe recipe, mezz.jei.api.gui.ingredient.IRecipeSlotsView view, GuiGraphics graphics, double mouseX, double mouseY) {
        // 가운데 화살표랑 시간(Tick) 그려주기♡
        graphics.drawString(Minecraft.getInstance().font, "->", 42, 15, 0x404040, false);
        Component time = Component.literal(recipe.getTicks() + " Ticks");
        graphics.drawString(Minecraft.getInstance().font, time, 35, 28, 0x888888, false);
    }
}