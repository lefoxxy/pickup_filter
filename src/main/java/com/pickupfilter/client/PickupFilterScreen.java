package com.pickupfilter.client;

import com.pickupfilter.data.FilterMode;
import com.pickupfilter.menu.PickupFilterMenu;
import com.pickupfilter.network.PickupFilterNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class PickupFilterScreen extends AbstractContainerScreen<PickupFilterMenu> {
    private Button modeButton;
    private Button feedbackButton;

    public PickupFilterScreen(PickupFilterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 190;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 97;
    }

    @Override
    protected void init() {
        super.init();

        this.modeButton = Button.builder(modeButtonText(), button -> {
                    FilterMode nextMode = this.menu.getMode() == FilterMode.WHITELIST ? FilterMode.BLACKLIST : FilterMode.WHITELIST;
                    this.menu.setLocalMode(nextMode);
                    updateButtonText();
                    PickupFilterNetwork.sendToggleMode();
                })
                .bounds(this.leftPos + 8, this.topPos + 76, 78, 20)
                .build();

        this.feedbackButton = Button.builder(feedbackButtonText(), button -> {
                    boolean nextFeedbackValue = !this.menu.isFeedbackEnabled();
                    this.menu.setLocalFeedbackEnabled(nextFeedbackValue);
                    updateButtonText();
                    PickupFilterNetwork.sendToggleFeedback();
                })
                .bounds(this.leftPos + 90, this.topPos + 76, 78, 20)
                .build();

        this.addRenderableWidget(this.modeButton);
        this.addRenderableWidget(this.feedbackButton);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtonText();
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType clickType) {
        if (slotId >= 0 && slotId < PickupFilterMenu.FILTER_SLOT_COUNT) {
            handleFilterSlotClick(slotId, mouseButton, clickType);
            return;
        }

        super.slotClicked(slot, slotId, mouseButton, clickType);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFFC6C6C6);
        graphics.fill(left + 4, top + 4, left + this.imageWidth - 4, top + this.imageHeight - 4, 0xFFD9D9D9);
        graphics.renderOutline(left, top, this.imageWidth, this.imageHeight, 0xFF555555);

        drawSlotGrid(graphics, left + 43, top + 17, 5, 3);
        drawSlotGrid(graphics, left + 7, top + 107, 9, 3);
        drawSlotGrid(graphics, left + 7, top + 165, 9, 1);
    }

    private void drawSlotGrid(GuiGraphics graphics, int x, int y, int columns, int rows) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int slotX = x + column * 18;
                int slotY = y + row * 18;
                graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF373737);
                graphics.fill(slotX + 1, slotY + 1, slotX + 18, slotY + 18, 0xFFFFFFFF);
                graphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0xFF8B8B8B);
                graphics.fill(slotX + 2, slotY + 2, slotX + 17, slotY + 17, 0xFFB8B8B8);
                graphics.fill(slotX + 2, slotY + 2, slotX + 16, slotY + 16, 0xFF8B8B8B);
            }
        }
    }

    private void updateButtonText() {
        if (this.modeButton != null) {
            this.modeButton.setMessage(modeButtonText());
        }

        if (this.feedbackButton != null) {
            this.feedbackButton.setMessage(feedbackButtonText());
        }
    }

    private Component modeButtonText() {
        return this.menu.getMode() == FilterMode.WHITELIST
                ? Component.translatable("button.pickupfilter.mode.whitelist")
                : Component.translatable("button.pickupfilter.mode.blacklist");
    }

    private Component feedbackButtonText() {
        return this.menu.isFeedbackEnabled()
                ? Component.translatable("button.pickupfilter.feedback.on")
                : Component.translatable("button.pickupfilter.feedback.off");
    }

    private void handleFilterSlotClick(int slotId, int mouseButton, ClickType clickType) {
        ItemStack reference = switch (clickType) {
            case PICKUP, QUICK_MOVE -> this.menu.getCarried();
            case SWAP -> this.menu.getPlayerInventoryItem(mouseButton);
            case CLONE -> this.minecraft != null && this.minecraft.player != null && this.minecraft.player.getAbilities().instabuild
                    ? this.menu.getCarried()
                    : ItemStack.EMPTY;
            default -> ItemStack.EMPTY;
        };

        if (reference.isEmpty()) {
            this.menu.setLocalFilterReference(slotId, ItemStack.EMPTY);
            PickupFilterNetwork.sendClearSlot(slotId);
            return;
        }

        ItemStack normalizedReference = reference.copy();
        normalizedReference.setCount(1);
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(normalizedReference.getItem());
        if (itemId == null) {
            return;
        }

        this.menu.setLocalFilterReference(slotId, normalizedReference);
        PickupFilterNetwork.sendUpdateSlot(slotId, itemId);
    }
}
