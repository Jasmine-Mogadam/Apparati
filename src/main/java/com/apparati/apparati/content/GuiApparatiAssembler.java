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
    private static final ResourceLocation BG_TEXTURE = new ResourceLocation("apparati", "textures/gui/assembler.png");
    private final TileEntityApparatiAssembler te;

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
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        te.setActiveTab(button.id);
        // We'd need a packet to sync tab change to server, then server updates container slots
        // For now, let's assume TE sync handles it or we'll add the packet later.
        
        // Re-init to update slots (this is a bit hacky but works for demo)
        this.mc.player.openGui(com.apparati.apparati.ApparatiMod.instance, 0, te.getWorld(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    private static final ResourceLocation CRAFTING_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/crafting_table.png");
    private static final ResourceLocation ASSEMBLER_TEXTURE = new ResourceLocation("apparati", "textures/gui/assembler.png");

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        if (te.getActiveTab() == 0) {
            this.mc.getTextureManager().bindTexture(CRAFTING_TEXTURE);
        } else {
            this.mc.getTextureManager().bindTexture(ASSEMBLER_TEXTURE);
        }

        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    }

    // Custom JEI handling to switch tab on + click
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        // Intercept JEI '+' click which usually sends a key or mouse event
        // In 1.12 JEI, the '+' button click area is often handled by JEI itself,
        // but we can try to detect if we're on the wrong tab when the GUI loses focus or updates.
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        // Check if there are items in the crafting matrix while NOT on the crafting tab
        // This is a sign that JEI just autofilled (since JEI bypasses tab restrictions on the container level if slots are just moved)
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
                // Re-init to update slots
                this.mc.player.openGui(com.apparati.apparati.ApparatiMod.instance, 0, te.getWorld(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
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
            // Re-init to update slots
            this.mc.player.openGui(com.apparati.apparati.ApparatiMod.instance, 0, te.getWorld(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
        }
    }
}
