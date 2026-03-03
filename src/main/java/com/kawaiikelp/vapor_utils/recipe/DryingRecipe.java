package com.kawaiikelp.vapor_utils.recipe;

import com.google.gson.JsonObject;
import com.kawaiikelp.vapor_utils.VaporUtils;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsRecipes; // 곧 만들 거야
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class DryingRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack output;
    private final int ticks; // 건조 시간

    public DryingRecipe(ResourceLocation id, Ingredient input, ItemStack output, int ticks) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.ticks = ticks;
    }

    // 레시피 재료가 맞는지 확인
    @Override
    public boolean matches(Container pContainer, Level pLevel) {
        // 첫 번째 슬롯(0번) 아이템이랑 레시피 재료랑 비교
        return this.input.test(pContainer.getItem(0));
    }

    @Override
    public ItemStack assemble(Container pContainer, RegistryAccess pRegistryAccess) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return this.output;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return VaporUtilsRecipes.DRYING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return VaporUtilsRecipes.DRYING_TYPE.get();
    }

    // 건조 시간 가져오기 (우리가 만든 커스텀 메서드)
    public int getTicks() {
        return this.ticks;
    }

    public Ingredient getInput() {
        return this.input;
    }

    // --- 시리얼라이저 (JSON 읽고 쓰기) ---
    public static class Serializer implements RecipeSerializer<DryingRecipe> {
        // JSON 파일에서 레시피 읽어오는 부분
        @Override
        public DryingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            // "input": { "item": "minecraft:apple" } 같은 거 읽기
            Ingredient input = Ingredient.fromJson(pSerializedRecipe.get("input"));

            // "result": { "item": "vapor_utils:dried_apple" } 같은 거 읽기
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "result"));

            // "ticks": 200 (시간) 읽기. 없으면 기본값 100틱(5초)으로 퉁침.
            int ticks = GsonHelper.getAsInt(pSerializedRecipe, "ticks", 100);

            return new DryingRecipe(pRecipeId, input, output, ticks);
        }

        // 네트워크로 레시피 보낼 때 (서버 -> 클라이언트)
        @Override
        public @Nullable DryingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            Ingredient input = Ingredient.fromNetwork(pBuffer);
            ItemStack output = pBuffer.readItem();
            int ticks = pBuffer.readInt();
            return new DryingRecipe(pRecipeId, input, output, ticks);
        }

        // 네트워크로 레시피 쓸 때
        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, DryingRecipe pRecipe) {
            pRecipe.input.toNetwork(pBuffer);
            pBuffer.writeItem(pRecipe.output);
            pBuffer.writeInt(pRecipe.ticks);
        }
    }
}
