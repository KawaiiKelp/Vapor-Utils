package com.kawaiikelp.vapor_utils.datagen;

import com.kawaiikelp.vapor_utils.VaporUtils;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsBlocks;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import org.codehaus.plexus.util.StringUtils;

public class VaporUtilsLanguageProvider extends LanguageProvider {

    private final String locale;

    public VaporUtilsLanguageProvider(PackOutput output, String locale) {
        super(output, VaporUtils.MODID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        // [아이템 & 채팅 메시지 번역 키 등록]
        boolean isKo = locale.equals("ko_kr");

        // 크리에이티브 탭 이름
        add("itemGroup.vapor_utils", "Vapor Utils");

        // Jade 관련
        if (isKo) {
            add("config.jade.plugin_vapor_utils.drying_rack_info", "건조대 진행도");
            add("jade.vapor_utils.drying", "건조 중");
        } else {
            add("config.jade.plugin_vapor_utils.drying_rack_info", "Drying Rack Progress");
            add("jade.vapor_utils.drying", "Drying");
        }

        // 블록 이름 자동 생성!
        VaporUtilsBlocks.DRYING_RACKS.forEach((woodName, blockReg) -> {
            if (isKo) {
                // 한국어: oak -> 참나무
                String korName = convertToKorean(woodName);
                add(blockReg.get(), korName + " 건조대");
            } else {
                // 영어: oak_drying_rack -> Oak Drying Rack
                String engName = convertToEnglish(woodName);
                add(blockReg.get(), engName + " Drying Rack");
            }
        });

        add("item.vapor_utils.projector", isKo ? "청사진 투영기" : "Blueprint Projector");
        add("item.vapor_utils.blueprint_mapper", isKo ? "청사진 기록기" : "Blueprint Mapper");
        add("item.vapor_utils.blueprint", isKo ? "청사진" : "Blueprint");
        add("item.vapor_utils.healing_axe", isKo ? "회복의 도끼" : "Healing Axe");

        // 청사진 기록기 (Blueprint Mapper)
        addMsg("mapper.pos1", isKo ? "§a[기록기] 시작점 설정: %s" : "§a[Mapper] Pos1 set: %s");
        addMsg("mapper.pos2", isKo ? "§b[기록기] 끝점 설정: %s" : "§b[Mapper] Pos2 set: %s");
        addMsg("mapper.ready_to_work", isKo ? "§b영역 지정이 완료되었습니다! 작업대에서 청사진을 만드세요." : "§bArea set! Now create your blueprint at the workbench.");
        addMsg("mapper.no_pos", isKo ? "§c좌표가 제대로 설정되지 않았습니다!" : "§cCoordinates not fully set!");
        addMsg("mapper.error", isKo ? "§c파일에 문제가 발생했습니다! 콘솔을 확인하세요." : "§cFile Error! Check Console.");
        addMsg("mapper.save_success", isKo ? "§c청사진 인쇄 완료!" : "§cBlueprint Saved!");
        addTooltip("mapper.pos1_info", isKo ? "시작점: %s" : "Pos1: %s");
        addTooltip("mapper.pos2_info", isKo ? "끝점: %s" : "Pos2: %s");
        addTooltip("mapper.desc", isKo ? "우클릭으로 두 지점을 지정하세요." : "Right-click to select two points.");

        // 청사진 아이템 (Blueprint)
        addTooltip("blueprint.file", isKo ? "§7파일: §e%s" : "§7File: §e%s");
        addTooltip("blueprint.size", isKo ? "§7크기: §b%s 블록" : "§7Size: §b%s blocks");
        addTooltip("blueprint.empty", isKo ? "§c[빈 청사진]" : "§c[Empty Blueprint]");

        // 투영기 (Projector)
        addMsg("projector.anchor", isKo ? "§a[투영기] 기준점이 설정되었습니다: %s" : "§a[Projector] Anchor set to: %s");
        addMsg("projector.on", isKo ? "§b[투영기] 활성화" : "§b[Projector] Enabled");
        addMsg("projector.off", isKo ? "§7[투영기] 비활성화" : "§7[Projector] Disabled");
        addMsg("projector.complete", isKo ? "§6[투영기] 건설 완료!" : "§6[Projector] Construction Complete!");
        addTooltip("projector.no_file", isKo ? "§c[비어있음] 청사진과 조합하여 데이터를 복사하세요." : "§c[Empty] Combine with a blueprint to copy data.");
        addTooltip("projector.active", isKo ? "§b[작동 중]" : "§b[Active]");
        addTooltip("projector.anchor_pos", isKo ? "§7기준점: %s" : "§7Anchor: %s");
        addTooltip("projector.controls", isKo ? "§8Shift+우클릭: 기준점 설정 / 우클릭: GUI" : "§8Shift+RClick: Set Anchor / RClick: Open GUI");
        addTooltip("projector.rotate", isKo ? "§8Shift+스크롤: 회전" : "§8Shift+Scroll: Rotate");

        add(VaporUtilsBlocks.BLUEPRINT_WORKBENCH.get(), isKo ? "청사진 작업대" : "Blueprint Workbench");
        addTooltip("workbench.desc", isKo ? "§7청사진을 기록하고 출력하는 작업대입니다." : "§7Workbench for mapping and printing blueprints.");

        add("jei.vapor_utils.drying", isKo ? "건조대" : "Drying Rack");

        // GUI 관련
        add("gui.vapor_utils.projector.title", isKo ? "투영기 설정" : "Projector Configuration");
        add("gui.vapor_utils.btn_print", isKo ? "청사진 인쇄" : "Print Blueprint");
        add("gui.vapor_utils.blueprint_name", isKo ? "청사진 이름 입력" : "Name Blueprint");
        add("gui.vapor_utils.blueprint_workbench", isKo ? "청사진 작업대" : "Blueprint Workbench");

        add("gui.vapor_utils.files", isKo ? "파일 목록" : "Files");
        add("gui.vapor_utils.needed", isKo ? "필요 재료" : "Materials Needed");

    }

    // 도우미 메서드 (귀찮으니까♡)
    private void addMsg(String key, String value) { add("message.vapor_utils." + key, value); }
    private void addTooltip(String key, String value) { add("tooltip.vapor_utils." + key, value); }

    private String convertToEnglish(String wood) {
        // "dark_oak" -> "Dark Oak"
        String[] parts = wood.split("_");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = StringUtils.capitalise(parts[i]);
        }
        return String.join(" ", parts);
    }

    private String convertToKorean(String wood) {
        // 이거 일일이 하기 귀찮으면 switch 문 써야지 뭐.
        return switch (wood) {
            case "oak" -> "참나무";
            case "spruce" -> "가문비나무";
            case "birch" -> "자작나무";
            case "jungle" -> "정글나무";
            case "acacia" -> "아카시아";
            case "dark_oak" -> "짙은 참나무";
            case "mangrove" -> "맹그로브";
            case "cherry" -> "벚나무";
            case "bamboo" -> "대나무";
            default -> wood;
        };
    }
}