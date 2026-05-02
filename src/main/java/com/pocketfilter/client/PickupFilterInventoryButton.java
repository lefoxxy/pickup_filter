package com.pocketfilter.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class PickupFilterInventoryButton extends Button {
    private static final ItemStack ICON = new ItemStack(Items.PAPER);

    PickupFilterInventoryButton(Builder builder) {
        super(builder);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int background = this.isHoveredOrFocused() ? 0xFFE8E8E8 : 0xFFC6C6C6;
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF555555);
        graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, background);
        graphics.renderItem(ICON, this.getX() + 1, this.getY() + 1);
    }
}
