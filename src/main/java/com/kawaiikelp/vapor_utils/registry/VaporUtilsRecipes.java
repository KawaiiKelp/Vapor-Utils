package com.kawaiikelp.vapor_utils.registry;

import com.kawaiikelp.vapor_utils.VaporUtils;
import com.kawaiikelp.vapor_utils.recipe.DryingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class VaporUtilsRecipes {
    // 1. 시리얼라이저 등록기
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, VaporUtils.MODID);

    // 2. 레시피 타입 등록기 (이건 Forge 레지스트리가 아니라 일반 변수로 만들어도 돼)
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, VaporUtils.MODID);

    // 3. 실제 등록
    public static final RegistryObject<RecipeSerializer<DryingRecipe>> DRYING_SERIALIZER =
            SERIALIZERS.register("drying", DryingRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<DryingRecipe>> DRYING_TYPE =
            RECIPE_TYPES.register("drying", () -> new RecipeType<DryingRecipe>() {
                @Override
                public String toString() {
                    return "drying";
                }
            });
}