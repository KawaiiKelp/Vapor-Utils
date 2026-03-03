package com.kawaiikelp.vapor_utils.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VaporUtilsKubeJSPlugin extends KubeJSPlugin {

    // 로거 하나 장만해.
    private static final Logger LOGGER = LoggerFactory.getLogger("VaporUtils KubeJS");

    public VaporUtilsKubeJSPlugin() {
        // 생성자 로그 (클래스 로딩 확인용)
        LOGGER.info("!!! [VaporUtils] KubeJS 플러그인 생성자 호출됨 !!!");
    }

    @Override
    public void init() {
        // 초기화 로그 (KubeJS 로딩 확인용)
        LOGGER.info("!!! [VaporUtils] KubeJS 플러그인 init() 성공 !!!");
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        // 나중에 여기서 '건조대(Drying Rack)' 레시피 타입을 KubeJS에 알려줄 거야.
        // 지금은 일단 비워둬. 김칫국 마시지 말고♡

        event.register(new ResourceLocation("vapor_utils", "drying"), new RecipeSchema(

                // 1. 재료 (Input)
                // JSON에서 "input"이라는 키로 찾을 거야.
                ItemComponents.INPUT.key("input"),

                // 2. 결과 (Output)
                // JSON에서 "result"라는 키로 찾을 거야.
                ItemComponents.OUTPUT.key("result"),

                // 3. 시간 (Ticks)
                // JSON에서 "ticks"라는 키로 찾을 거야.
                // .optional(100)은 스크립트에서 안 적으면 기본값 100틱(5초)으로 하겠다는 뜻♡
                TimeComponent.TICKS.key("ticks").optional(100L)
        ));
    }
}
