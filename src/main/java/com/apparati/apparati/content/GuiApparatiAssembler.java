package com.apparati.apparati.content;

import com.apparati.apparati.Constants;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.List;

public class GuiApparatiAssembler extends GuiContainer {
    private static final ResourceLocation CRAFTING_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/crafting_table.png");
    private static final ResourceLocation ASSEMBLER_TEXTURE = new ResourceLocation("apparati", "textures/gui/assembler.png");

    private final TileEntityApparatiAssembler te;
    private GuiApparatiCoreTab coreTab;

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
        
        this.coreTab = new GuiApparatiCoreTab(this, te);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        te.setActiveTab(button.id);
        com.apparati.apparati.network.ApparatiNetwork.INSTANCE.sendToServer(new com.apparati.apparati.network.PacketApparatiTab(button.id));
        
        if (this.inventorySlots instanceof ContainerApparatiAssembler) {
            ((ContainerApparatiAssembler) this.inventorySlots).updateSlots();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
        
        if (te.getActiveTab() == 2 && coreTab != null) {
            coreTab.drawTooltips(mouseX, mouseY, guiLeft, guiTop);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        if (te.getActiveTab() == 0) {
            this.mc.getTextureManager().bindTexture(CRAFTING_TEXTURE);
            int i = (this.width - this.xSize) / 2;
            int j = (this.height - this.ySize) / 2;
            this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        } else if (te.getActiveTab() == 2 && coreTab != null) {
            int i = (this.width - this.xSize) / 2;
            int j = (this.height - this.ySize) / 2;
            coreTab.drawBackground(i, j, this.xSize, this.ySize, mouseX, mouseY);
        } else {
            this.mc.getTextureManager().bindTexture(ASSEMBLER_TEXTURE);
            int i = (this.width - this.xSize) / 2;
            int j = (this.height - this.ySize) / 2;
            this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (te.getActiveTab() == 2 && coreTab != null) {
            if (coreTab.handleMouseClick(mouseX, mouseY, mouseButton)) {
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    // Custom JEI handling to switch tab on + click
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (te.getActiveTab() == 2 && coreTab != null) {
            if (coreTab.handleKeyInput(typedChar, keyCode)) {
                return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        // Check if there are items in the crafting matrix while NOT on the crafting tab
        if (te.getActiveTab() != 0) {
            boolean hasItems = false;
            for (int i = 0; i < te.craftingInv.getSlots(); i++) {
                if (!te.craftingInv.getStackInSlot(i).isEmpty()) {
                    hasItems = true;
                    break;
                }
            }
            if (hasItems) {
                te.setActiveTab(0);
                com.apparati.apparati.network.ApparatiNetwork.INSTANCE.sendToServer(new com.apparati.apparati.network.PacketApparatiTab(0));
                if (this.inventorySlots instanceof ContainerApparatiAssembler) {
                    ((ContainerApparatiAssembler) this.inventorySlots).updateSlots();
                }
            }
        }
        
        // Ensure result is updated if items are present
        if (te.getActiveTab() == 0 && this.inventorySlots instanceof ContainerApparatiAssembler) {
            ((ContainerApparatiAssembler)this.inventorySlots).onCraftMatrixChanged();
        }
    }
    
    public void onJEIAutofill() {
        if (te.getActiveTab() != 0) {
            te.setActiveTab(0);
            com.apparati.apparati.network.ApparatiNetwork.INSTANCE.sendToServer(new com.apparati.apparati.network.PacketApparatiTab(0));
            if (this.inventorySlots instanceof ContainerApparatiAssembler) {
                ((ContainerApparatiAssembler) this.inventorySlots).updateSlots();
            }
        }
    }
    
    // Expose helpers for CoreTab
    public net.minecraft.client.gui.FontRenderer getFontRenderer() {
        return this.fontRenderer;
    }
    
    public int getGuiLeft() {
        return this.guiLeft;
    }
    
    public int getGuiTop() {
        return this.guiTop;
    }
}
