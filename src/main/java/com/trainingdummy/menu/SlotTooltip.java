package com.trainingdummy.menu;

import net.minecraft.network.chat.Component;

/** Implemented by slots that should show a name tooltip on hover while empty, like Curios does. */
public interface SlotTooltip {

    Component getTooltipName();
}
