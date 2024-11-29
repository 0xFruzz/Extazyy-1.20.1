package ru.fruzz.extazyy.misc.util.funtime;

import lombok.Getter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum FuntimeItems {
    //Spheres
    OSIRIS("[★] Сфера Осириса", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDgxMzYzNWJkODZiMTcxYmJlMTQzYWQ3MWUwOTAyMjkyNjQ5Y2IzYWI4NDQwZWQwMGY4NWNhNmNhMzgyOTkzNiJ9fX0", true, Items.PLAYER_HEAD),
    ASTREA("[★] Сфера Астрея", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWE1YWFkZDUyYTVmYWI5NzA4ODE0NTFhZGY1NmZiYjQ5M2EzNTg1NmVhOTZmNTRlMzJlZWE2NjJkNzg3ZWQyMCJ9fX0", true, Items.PLAYER_HEAD),
    TITAN("[★] Сфера Титана", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTAzN2JiYmViNjJlMTAyMGRmOWEwNmM0ZWRkNjAzMzBlNzA2MzBkMDkwZjA5NGQ4Nzc2YzJiZDEzNWRlYzIyIn19fQ", true, Items.PLAYER_HEAD),
    APOLLO("[★] Сфера Аполлона", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjQxMTdiNjAxOGZlZjBkNTE1NjcyMTczZTNiMjZlNjYwZDY1MWU1ODc2YmE2ZDAzZTUzNDIyNzBjNDliZWM4MCJ9fX0", true, Items.PLAYER_HEAD),
    PANDORA("[★] Сфера Пандоры", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGU1MWU2NWViNDA1Mjc3MjM4MmM5ZTUwN2E1NGJkZWQ0M2UzOWY3NTViNWRkZjU1YjNmMzk0NDNjZWQ0NjdmNCJ9fX0", true, Items.PLAYER_HEAD),
    ANDROMEDA("[★] Сфера Андромеды", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDRmZmUzZjM1OGYyMDliYWQ4ZmZmNGRjNDgyNDVkOWJhZjBhMDMxYjNjMWVlNmI3NTg0NjBhMzM5YjE1MTllMiJ9fX0", true, Items.PLAYER_HEAD),
    CHIMERA("[★] Сфера Химеры", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWZhYmVlZDQyNGIyNTJhODk0NWE2NDQyYjQ2MmQ1ZjMxNDcwMWE4MTZkYTJkMGE2OWNjZGZjZmQ3NDZlNTg4ZSJ9fX0", true, Items.PLAYER_HEAD),
    //Talismans
    GRANI("[★] Талисман Грани", "", false, Items.TOTEM_OF_UNDYING),
    TRITON("[★] Талисман Тритона", "", false, Items.TOTEM_OF_UNDYING),
    ECHIDNA("[★] Талисман Ехидны", "", false, Items.TOTEM_OF_UNDYING),
    HARMONY("[★] Талисман Гармонии", "", false, Items.TOTEM_OF_UNDYING),
    DAEDALUS("[★] Талисман Дедала", "", false, Items.TOTEM_OF_UNDYING),
    PHOENIX("[★] Талисман Феникса", "", false, Items.TOTEM_OF_UNDYING),
    //Other
    HOOK_SPHERES("[★] Отмычка к Сферам", "", false, Items.TRIPWIRE_HOOK),
    NETHERITE_SCRAP("[★] Трапка", "", false, Items.NETHERITE_SCRAP),
    PHANTOM_MEMBRANE("[★] Божья аура", "", false, Items.PHANTOM_MEMBRANE),
    //Potions
    WINNER_POTION("[★] Зелье Победителя", "", false, Items.SPLASH_POTION),
    MEDIC_POTION("[★] Зелье Медика", "", false, Items.SPLASH_POTION),
    KILLER_POTION("[★] Зелье Киллера", "", false, Items.SPLASH_POTION),
    BURP_POTION("[★] Зелье Отрыжки", "", false, Items.SPLASH_POTION),
    SULFURIC_ACID("[★] Серная кислота", "", false, Items.SPLASH_POTION),
    //
    ENCHANTED_GOLDEN_APPLE("Зачарованное золотое яблоко", "", false, Items.ENCHANTED_GOLDEN_APPLE),
    DRAGON_HEAD("Голова дракона", "", false, Items.DRAGON_HEAD),
    NETHERITE_INGOT("Незеритовый слиток", "", false, Items.NETHERITE_INGOT),
    SILVER("[★] Серебро", "", false, Items.IRON_NUGGET),
    ICE_ARROW("[★] Ледяная стрела", "", false, Items.TIPPED_ARROW),
    TIER_BLACK("[★] TNT - TIER BLACK", "", false, Items.TNT),
    TIER_WHITE("[★] TNT - TIER WHITE", "", false, Items.TNT),
    PARANOIA_ARROW("[★] Стрела паранойи", "", false, Items.TIPPED_ARROW);

    @Getter
    private final String name;
    @Getter
    private final String textureValue;
    @Getter
    private final boolean isHead;
    @Getter
    private final Item item;

    FuntimeItems(String name, String textureValue, boolean isHead, Item item) {
        this.name = name;
        this.textureValue = textureValue;
        this.isHead = isHead;
        this.item = item;
    }

}
