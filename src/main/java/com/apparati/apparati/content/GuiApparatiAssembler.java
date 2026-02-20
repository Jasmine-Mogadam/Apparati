package com.apparati.apparati.content;

import com.apparati.apparati.Constants;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiApparatiAssembler extends GuiContainer {
    private static final ResourceLocation BG_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/crafting_table.png");
    private final TileEntityApparatiAssembler te;
    private GuiTextField programmingField;

    public GuiApparatiAssembler(ContainerApparatiAssembler container, TileEntityApparatiAssembler te) {
        super(container);
        this.te = te;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, guiLeft - 20, guiTop + 10, 20, 20, "C"));
        this.buttonList.add(new GuiButton(1, guiLeft - 20, guiTop + 35, 20, 20, "A"));
        this.buttonList.add(new GuiButton(2, guiLeft - 20, guiTop + 60, 20, 20, "P"));

        Keyboard.enableRepeatEvents(true);
        this.programmingField = new GuiTextField(0, fontRenderer, guiLeft + 20, guiTop + 60, 136, 12);
        this.programmingField.setTextColor(-1);
        this.programmingField.setDisabledTextColour(-1);
        this.programmingField.setEnableBackgroundDrawing(true);
        this.programmingField.setMaxStringLength(100);
        
        // Load text from core if present
        updateProgrammingField();
    }

    private void updateProgrammingField() {
        if (te.getActiveTab() == 2 && !te.programmingInv.getStackInSlot(0).isEmpty()) {
            this.programmingField.setEnabled(true);
            String text = te.programmingInv.getStackInSlot(0).getDisplayName(); // Placeholder
            if (te.programmingInv.getStackInSlot(0).hasTagCompound()) {
                text = te.programmingInv.getStackInSlot(0).getTagCompound().getString("Description");
            }
            this.programmingField.setText(text);
        } else {
            this.programmingField.setEnabled(false);
            this.programmingField.setText("");
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        te.setActiveTab(button.id);
        // We'd need a packet to sync tab change to server, then server updates container slots
        // For now, let's assume TE sync handles it or we'll add the packet later.
        updateProgrammingField();
        // Re-init to update slots (this is a bit hacky but works for demo)
        this.mc.player.openGui(com.apparati.apparati.ApparatiMod.instance, 0, te.getWorld(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.programmingField.textboxKeyTyped(typedChar, keyCode)) {
             // Send packet to server to update Core NBT
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.programmingField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(BG_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        
        if (te.getActiveTab() == 0) {
            this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        } else {
            // Use a fallback or custom background for other tabs if they don't fit the crafting table texture
            // For now, still using the crafting table base but maybe we want to keep some elements.
            this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        }
        
        if (te.getActiveTab() == 2) {
            this.programmingField.drawTextBox();
        }
    }
}
