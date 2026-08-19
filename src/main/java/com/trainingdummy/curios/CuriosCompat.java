package com.trainingdummy.curios;

import com.trainingdummy.entity.DummyEntity;
import com.trainingdummy.item.DummyCurioEntry;
import com.trainingdummy.menu.DummyMenu;
import com.trainingdummy.menu.SlotTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.SlotItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.List;

public final class CuriosCompat {

    private static final String CURIOS_MODID = "curios";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(CURIOS_MODID);
    }

    public static void addCurioSlots(DummyMenu menu, DummyEntity dummy, int x, int y, int columns,
                                      int pageStart, int pageLimit) {
        CuriosApi.getCuriosInventory(dummy).ifPresent(handler -> {
            int[] global = {0};
            int pageEnd = pageStart + pageLimit;
            for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                String identifier = stacksHandler.getIdentifier();
                Identifier icon = CuriosApi.getSlot(identifier, dummy.level())
                        .map(ISlotType::getIcon).orElse(null);
                Component name = Component.translatable("curios.identifier." + identifier);

                for (int i = 0; i < stacksHandler.getStacks().getSlots(); i++) {
                    int g = global[0]++;
                    if (g < pageStart || g >= pageEnd) {
                        continue;
                    }
                    int local = g - pageStart;
                    int col = local % columns;
                    int row = local / columns;
                    NamedCurioSlot slot = new NamedCurioSlot(stacksHandler.getStacks(), i,
                            x + col * 18, y + row * 18, name, icon);
                    menu.addCurioSlot(slot);
                }
            }
        });
    }

    public static int slotCount(DummyEntity dummy) {
        if (!isLoaded()) {
            return 0;
        }
        return CuriosApi.getCuriosInventory(dummy).map(ICuriosItemHandler::getSlots).orElse(0);
    }

    public static List<DummyCurioEntry> captureAll(DummyEntity dummy) {
        List<DummyCurioEntry> result = new ArrayList<>();
        CuriosApi.getCuriosInventory(dummy).ifPresent(handler -> {
            for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                IItemHandlerModifiable stacks = stacksHandler.getStacks();
                String identifier = stacksHandler.getIdentifier();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        result.add(new DummyCurioEntry(identifier, i, stack.copy()));
                    }
                }
            }
        });
        return result;
    }

    public static void restoreAll(DummyEntity dummy, List<DummyCurioEntry> entries, ServerLevel level) {
        if (entries.isEmpty()) {
            return;
        }
        CuriosApi.getCuriosInventory(dummy).ifPresent(handler -> {
            for (DummyCurioEntry entry : entries) {
                ICurioStacksHandler stacksHandler = handler.getCurios().get(entry.identifier());
                IItemHandlerModifiable stacks = stacksHandler != null ? stacksHandler.getStacks() : null;
                if (stacks != null && entry.slot() < stacks.getSlots()) {
                    stacks.setStackInSlot(entry.slot(), entry.stack().copy());
                } else {
                    dummy.spawnAtLocation(level, entry.stack().copy());
                }
            }
        });
    }

    private static final class NamedCurioSlot extends SlotItemHandler implements SlotTooltip {

        private final Component tooltipName;
        private final Identifier icon;

        private NamedCurioSlot(IItemHandlerModifiable handler, int index, int x, int y, Component tooltipName,
                                Identifier icon) {
            super(handler, index, x, y);
            this.tooltipName = tooltipName;
            this.icon = icon;
        }

        private boolean indexStillValid() {
            return this.index < this.getItemHandler().getSlots();
        }

        @Override
        public ItemStack getItem() {
            return this.indexStillValid() ? super.getItem() : ItemStack.EMPTY;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.indexStillValid() && super.mayPlace(stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            return this.indexStillValid() && super.mayPickup(player);
        }

        @Override
        public Identifier getNoItemIcon() {
            return this.icon;
        }

        @Override
        public Component getTooltipName() {
            return this.tooltipName;
        }
    }

    private CuriosCompat() {
    }
}
