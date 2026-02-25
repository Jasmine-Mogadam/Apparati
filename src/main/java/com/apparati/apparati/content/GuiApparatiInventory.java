package com.apparati.apparati.content;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import java.io.IOException;

public class GuiApparatiInventory extends GuiContainer {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("apparati", "textures/gui/inventory.png");
    private final IInventory playerInventory;
    private final IInventory apparatiInventory;
    private final ApparatiEntity apparati;
    private float mousePosx;
    private float mousePosY;
    
    private float currentScroll;
    private boolean isScrolling;
    private boolean wasClicking;
    private static final int VISIBLE_ROWS = 3;

    public GuiApparatiInventory(IInventory playerInv, IInventory apparatiInv, ApparatiEntity apparati) {
        super(new ContainerApparatiInventory(playerInv, apparatiInv, apparati, net.minecraft.client.Minecraft.getMinecraft().player));
        this.playerInventory = playerInv;
        this.apparatiInventory = apparatiInv;
        this.apparati = apparati;
        this.allowUserInput = false;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        System.out.println("DEBUG: Init GUI Apparati Inventory at " + i + ", " + j);
        this.buttonList.clear();
        // Positioned at top-left of the entity preview area (approx 26, 18 based on texture usually)
        // Adjusting to overlap with typical GUI elements
        this.buttonList.add(new GuiButtonPackage(0, i + 26, j + 18));
    }

    private static class GuiButtonPackage extends GuiButton {
        private static final ResourceLocation ICON_TEXTURE = new ResourceLocation("apparati", "textures/gui/package_icon.png");

        public GuiButtonPackage(int buttonId, int x, int y) {
            super(buttonId, x, y, 16, 16, "");
        }

        @Override
        public void drawButton(net.minecraft.client.Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(ICON_TEXTURE);
                int i = 0; // Texture X
                int j = 0; // Texture Y
                // Draw the icon. Assuming 16x16 icon in the texture file.
                // Draw complete texture scaling to button size if needed, or just 16x16
                drawModalRectWithCustomSizedTexture(this.x, this.y, 0, 0, this.width, this.height, 16, 16);
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        System.out.println("DEBUG: Button pressed: " + button.id);
        if (button.id == 0) {
            com.apparati.apparati.network.ApparatiNetwork.INSTANCE.sendToServer(new com.apparati.apparati.network.PacketPackageApparati(apparati.getEntityId()));
            this.mc.player.closeScreen();
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.apparatiInventory.getDisplayName().getUnformattedText(), 8, 6, 4210752);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        
        GuiInventory.drawEntityOnScreen(i + 53, j + 55, 30, (float)(i + 53) - this.mousePosx, (float)(j + 75 - 50) - this.mousePosY, this.apparati);
        
        int k = (int)(41.0F * this.currentScroll); 
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        this.drawTexturedModalRect(i + 155, j + 18 + k, 232 + (this.needsScrollBars() ? 0 : 12), 0, 12, 15);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        boolean flag = org.lwjgl.input.Mouse.isButtonDown(0);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        int k = i + 155; 
        int l = j + 16;  
        int i1 = k + 14; 
        int j1 = l + 54; 

        if (!this.wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1) {
            this.isScrolling = this.needsScrollBars();
        }
        if (!flag) this.isScrolling = false;
        this.wasClicking = flag;

        if (this.isScrolling) {
            this.currentScroll = ((float)(mouseY - l) - 7.5F) / ((float)(j1 - l) - 15.0F);
            this.currentScroll = net.minecraft.util.math.MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
            ((ContainerApparatiInventory)this.inventorySlots).scrollTo(this.currentScroll);
        }

        this.mousePosx = (float)mouseX;
        this.mousePosY = (float)mouseY;
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public void handleMouseInput() throws java.io.IOException {
        super.handleMouseInput();
        int i = org.lwjgl.input.Mouse.getEventDWheel();
        if (i != 0 && this.needsScrollBars()) {
            int j = ((ContainerApparatiInventory)this.inventorySlots).getInventoryRows() - VISIBLE_ROWS;
            if (i > 0) i = 1;
            if (i < 0) i = -1;
            this.currentScroll = (float)((double)this.currentScroll - (double)i / (double)j);
            this.currentScroll = net.minecraft.util.math.MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
            ((ContainerApparatiInventory)this.inventorySlots).scrollTo(this.currentScroll);
        }
    }

    private boolean needsScrollBars() {
        return ((ContainerApparatiInventory)this.inventorySlots).getInventoryRows() > VISIBLE_ROWS;
    }
}
