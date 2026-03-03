package com.kawaiikelp.vapor_utils.registry;

import com.kawaiikelp.vapor_utils.VaporUtils;
import com.kawaiikelp.vapor_utils.client.gui.menu.BlueprintWorkbenchMenu;
import com.kawaiikelp.vapor_utils.client.gui.menu.ProjectorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class VaporUtilsMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, VaporUtils.MODID);

    // IForgeMenuType.create를 써야 패킷 데이터를 받을 수 있어!
    public static final RegistryObject<MenuType<ProjectorMenu>> PROJECTOR_MENU =
            MENUS.register("projector_menu", () -> IForgeMenuType.create(ProjectorMenu::new));

    // [추가] 작업대 메뉴 등록♡
    public static final RegistryObject<MenuType<BlueprintWorkbenchMenu>> BLUEPRINT_WORKBENCH_MENU =
            MENUS.register("blueprint_workbench_menu", () -> IForgeMenuType.create(BlueprintWorkbenchMenu::new));
}