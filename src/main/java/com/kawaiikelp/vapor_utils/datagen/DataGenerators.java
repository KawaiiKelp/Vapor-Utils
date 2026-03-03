package com.kawaiikelp.vapor_utils.datagen;

import com.kawaiikelp.vapor_utils.VaporUtils;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VaporUtils.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        // 아까 VaporUtils.java에 있던 내용 그대로 복사해와!
        // generator.addProvider(...) 하던 거!

        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();
        var existingFileHelper = event.getExistingFileHelper();
        var lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new VaporUtilsBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new VaporUtilsItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new VaporUtilsLanguageProvider(packOutput, "en_us"));
        generator.addProvider(event.includeClient(), new VaporUtilsLanguageProvider(packOutput, "ko_kr"));

        generator.addProvider(event.includeServer(), VaporUtilsLootTableProvider.create(packOutput));
        generator.addProvider(event.includeServer(), new VaporUtilsBlockTagProvider(packOutput, lookupProvider, existingFileHelper));
    }
}