package com.apparati.apparati.content;

import com.apparati.apparati.network.PacketUpdateCore;
import com.apparati.apparati.network.PacketCopyCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiApparatiCoreTab extends Gui {
    private static final ResourceLocation CORE_TAB_TEXTURE = new ResourceLocation("apparati", "textures/gui/assembler_core_tab.png");
    private static final ResourceLocation DROPDOWN_BG = new ResourceLocation("apparati", "textures/gui/dropdown_bg.png");
    private static final ResourceLocation DROPDOWN_INPUT = new ResourceLocation("apparati", "textures/gui/dropdown_input.png");
    private static final ResourceLocation DROPDOWN_INPUT_ACTIVE = new ResourceLocation("apparati", "textures/gui/dropdown_input_active.png");
    private static final ResourceLocation TARGET_INPUT_TEXTURE = new ResourceLocation("apparati", "textures/gui/assembler_core_tab_target_input.png");
    private static final ResourceLocation FILTER_INPUT_TEXTURE = new ResourceLocation("apparati", "textures/gui/assembler_core_tab_filter_input.png");
    private static final ResourceLocation COPY_ICON = new ResourceLocation("apparati", "textures/gui/copy_icon.png");
    private static final ResourceLocation ITEM_SLOT = new ResourceLocation("apparati", "textures/gui/item_slot.png");

    private final GuiApparatiAssembler parent;
    private final TileEntityApparatiAssembler te;
    private final RenderItem itemRender;
    
    private int openDropdownIndex = -1; // -1 for none
    private int openSubDropdownIndex = -1; // -1 for none (for target input method, stores row index)
    
    private int draggingIndex = -1; // -1 if not dragging
    
    // Popup State: 0 = None, 1 = Naming, 2 = Copying
    private int currentPopup = 0;
    
    private net.minecraft.client.gui.GuiTextField nameField;
    
    // Copy Popup Data
    // Stores inventory slot indices of the target cores
    private final Integer[] copySlots = new Integer[7]; 
    
    // Dropdown instances
    private final Dropdown[] behaviorDropdowns = new Dropdown[4];
    private final Dropdown[] methodDropdowns = new Dropdown[4];

    public GuiApparatiCoreTab(GuiApparatiAssembler parent, TileEntityApparatiAssembler te) {
        this.parent = parent;
        this.te = te;
        this.itemRender = Minecraft.getMinecraft().getRenderItem();
        
        Arrays.fill(copySlots, -1);
        
        // Initialize dropdowns
        for (int i = 0; i < 4; i++) {
            final int index = i;
            behaviorDropdowns[i] = new Dropdown(Arrays.asList("None", "Move", "Obtain"), 50);
            behaviorDropdowns[i].setCallback((option) -> {
                setBehavior(index, option);
                openDropdownIndex = -1;
            });
            
            methodDropdowns[i] = new Dropdown(Arrays.asList("Area", "Patrol", "Near"), 50);
            methodDropdowns[i].setCallback((option) -> {
                setMovementMethod(index, option);
                openSubDropdownIndex = -1;
            });
        }
        
        this.nameField = new net.minecraft.client.gui.GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 0, 0, 100, 20);
        this.nameField.setMaxStringLength(32);
        this.nameField.setFocused(true);
    }

    public void drawBackground(int guiLeft, int guiTop, int xSize, int ySize, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(CORE_TAB_TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        
        drawContent(guiLeft, guiTop, mouseX, mouseY);
    }
    
    public void drawTooltips(int mouseX, int mouseY, int guiLeft, int guiTop) {
        if (currentPopup != 0) {
            // Add tooltip for copy screen interaction if needed, or check button tooltips in popup?
            // Currently standard button tooltips are not implemented, only main screen.
            return;
        }

        // Info Icon Tooltip
        if (mouseX >= guiLeft + 8 && mouseX < guiLeft + 24 && mouseY >= guiTop + 26 && mouseY < guiTop + 42) {
             List<String> list = new ArrayList<>();
             list.add("Core Configuration");
             list.add("Configure the behavior of your Apparati.");
             list.add("");
             list.add("\u00A77Order determines priority.");
             parent.drawHoveringText(list, mouseX, mouseY);
        }
        
        // Only show tooltip if button is visible
        ItemStack coreStack = te.programmingInv.getStackInSlot(0);
        if (coreStack.isEmpty()) return;

        // Naming Button Tooltip (Aa) - Y+44 to Y+60
        if (mouseX >= guiLeft + 8 && mouseX < guiLeft + 24 && mouseY >= guiTop + 44 && mouseY < guiTop + 60) {
             List<String> list = new ArrayList<>();
             list.add("Rename Core");
             list.add("Set the name.");
             parent.drawHoveringText(list, mouseX, mouseY);
        }
        
        // Copy Button Tooltip - Y+62 to Y+78
        if (mouseX >= guiLeft + 8 && mouseX < guiLeft + 24 && mouseY >= guiTop + 62 && mouseY < guiTop + 78) {
             List<String> list = new ArrayList<>();
             list.add("Copy Core Configuration");
             list.add("Copies behaviors and name to other cores.");
             parent.drawHoveringText(list, mouseX, mouseY);
        }
        
        // Check dropdown tooltips...
        int dropdownX = guiLeft + 31;
        int dropdownY = guiTop + 9;
        
        int tier = 4;
        if (coreStack.hasTagCompound() && coreStack.getTagCompound().hasKey("Tier")) {
            tier = coreStack.getTagCompound().getInteger("Tier");
        }
        if (tier < 1) tier = 1;
        if (tier > 4) tier = 4;
        
        // If a dropdown is open, don't show tooltips for underlying buttons
        if (openDropdownIndex != -1) return;
        
        for (int k = 0; k < tier; k++) {
             int y = dropdownY + k * 18;
             if (mouseX >= dropdownX && mouseX < dropdownX + 50 && mouseY >= y && mouseY < y + 14) {
                  List<String> list = new ArrayList<>();
                  list.add("Select Behavior");
                  parent.drawHoveringText(list, mouseX, mouseY);
             }
        }
    }

    private void drawContent(int guiLeft, int guiTop, int mouseX, int mouseY) {
        ItemStack coreStack = te.programmingInv.getStackInSlot(0);
        
        if (!coreStack.isEmpty()) {
            // Draw Naming Button (Aa) at Y+44 (moved up 2px)
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int btnX = guiLeft + 8;
            int btnY = guiTop + 44;
            drawRect(btnX, btnY, btnX + 16, btnY + 16, 0xFF888888); 
            drawRect(btnX + 1, btnY + 1, btnX + 15, btnY + 15, 0xFF666666); 
            drawCenteredString(parent.getFontRenderer(), "Aa", btnX + 8, btnY + 4, 0xFFFFFF);
            
            // Draw Copy Button at Y+62
            int copyY = guiTop + 62;
            drawRect(btnX, copyY, btnX + 16, copyY + 16, 0xFF888888);
            drawRect(btnX + 1, copyY + 1, btnX + 15, copyY + 15, 0xFF666666);
            
            Minecraft.getMinecraft().getTextureManager().bindTexture(COPY_ICON);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            drawModalRectWithCustomSizedTexture(btnX, copyY, 0, 0, 16, 16, 16, 16);
            GlStateManager.disableBlend();
        }
        
        if (currentPopup != 0) {
            drawPopup(mouseX, mouseY);
            return;
        }

        // If no core, don't draw anything else
        if (coreStack.isEmpty()) {
            openDropdownIndex = -1;
            openSubDropdownIndex = -1;
            return;
        }
        
        // Check for drag release (if mouse not down)
        if (draggingIndex != -1 && !org.lwjgl.input.Mouse.isButtonDown(0)) {
            // Drop logic
             int dropdownY = guiTop + 9;
             int dropY = mouseY;
             int newIndex = (dropY - dropdownY) / 18;
             
             int tier = 4;
             if (coreStack.hasTagCompound() && coreStack.getTagCompound().hasKey("Tier")) {
                 tier = coreStack.getTagCompound().getInteger("Tier");
             }
             if (tier > 4) tier = 4;
             
             if (newIndex < 0) newIndex = 0;
             if (newIndex >= tier) newIndex = tier - 1;
             
             if (newIndex != draggingIndex) {
                 moveBehavior(draggingIndex, newIndex);
             }
             draggingIndex = -1;
        }

        int dropdownX = guiLeft + 31;
        int dropdownY = guiTop + 9;
        
        NBTTagCompound behaviors = getBehaviorsTag(coreStack);
        
        int tier = 4;
        if (coreStack.hasTagCompound() && coreStack.getTagCompound().hasKey("Tier")) {
            tier = coreStack.getTagCompound().getInteger("Tier");
        }
        if (tier < 1) tier = 1;
        if (tier > 4) tier = 4;

        // Calculate visual order based on drag
        Integer[] visualOrder = new Integer[tier];
        for (int i = 0; i < tier; i++) visualOrder[i] = i;
        
        if (draggingIndex != -1) {
             int currentHoverIndex = (mouseY - dropdownY) / 18;
             if (currentHoverIndex < 0) currentHoverIndex = 0;
             if (currentHoverIndex >= tier) currentHoverIndex = tier - 1;
             
             // Visual reordering (move logic)
             if (currentHoverIndex != draggingIndex) {
                 List<Integer> orderList = new ArrayList<>(Arrays.asList(visualOrder));
                 Integer draggedItem = orderList.remove((int)draggingIndex); // remove by index
                 orderList.add(currentHoverIndex, draggedItem);
                 visualOrder = orderList.toArray(new Integer[0]);
             }
        }

        // Draw rows in visual order
        for (int k = 0; k < tier; k++) {
            int actualIndex = visualOrder[k];
            
            // If this is the row being dragged, we skip drawing it in the list
            if (actualIndex == draggingIndex && draggingIndex != -1) continue;
            
            int y = dropdownY + k * 18;
            drawBehaviorRow(guiLeft, guiTop, dropdownX, y, actualIndex, behaviors, mouseX, mouseY);
        }
        
        // Draw dragged row on top
        if (draggingIndex != -1) {
            int dragY = mouseY - 9;
            int minY = dropdownY;
            int maxY = dropdownY + (tier - 1) * 18;
            if (dragY < minY) dragY = minY;
            if (dragY > maxY) dragY = maxY;
            
            drawBehaviorRow(guiLeft, guiTop, dropdownX, dragY, draggingIndex, behaviors, mouseX, mouseY);
        }
        
        // Draw overlays on top
        if (openDropdownIndex != -1 && openDropdownIndex < tier) {
            behaviorDropdowns[openDropdownIndex].drawOverlay(dropdownX, dropdownY + openDropdownIndex * 18, mouseX, mouseY);
        }
        
        if (openSubDropdownIndex != -1 && openSubDropdownIndex < tier) {
            int k = openSubDropdownIndex;
            int y = dropdownY + k * 18;
            int inputX = dropdownX + 50 + 3;
            methodDropdowns[k].drawOverlay(inputX + 33, y, mouseX, mouseY); 
        }
    }

    private void drawPopup(int mouseX, int mouseY) {
        // Draw overlay
        drawRect(0, 0, parent.width, parent.height, 0x80000000);
        
        int centerX = parent.width / 2;
        int centerY = parent.height / 2;
        int offsetY = parent.height / 4;
        // Moved up 6px relative to old position (old was +30)
        int drawY = centerY - offsetY + 24; 
        
        if (currentPopup == 1) { // NAMING
            drawCenteredString(parent.getFontRenderer(), "Set Core Name", centerX, drawY - 30, 0xFFFFFF);
            
            nameField.x = centerX - 50;
            nameField.y = drawY - 10;
            nameField.drawTextBox();
            
            int btnY = drawY + 15;
            drawStandardButton(centerX - 30, btnY, "Confirm", mouseX, mouseY);
            drawStandardButton(centerX + 30, btnY, "Cancel", mouseX, mouseY);
            
        } else if (currentPopup == 2) { // COPYING
            drawCenteredString(parent.getFontRenderer(), "Copy Core", centerX, drawY - 30, 0xFFFFFF);
            
            int startX = centerX - (7 * 18) / 2;
            int slotsY = drawY - 10;
            
            // Draw 7 slots
            for (int i = 0; i < 7; i++) {
                int x = startX + i * 18;
                
                // Rebind texture for each slot to prevent glitching if drawItemStack changed it
                Minecraft.getMinecraft().getTextureManager().bindTexture(ITEM_SLOT);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                drawModalRectWithCustomSizedTexture(x, slotsY, 0, 0, 18, 18, 18, 18);
                
                // Draw item if set
                if (copySlots[i] != -1) {
                    // Try to get item from inventory
                    if (copySlots[i] >= 0 && copySlots[i] < parent.inventorySlots.inventorySlots.size()) {
                        ItemStack stack = parent.inventorySlots.inventorySlots.get(copySlots[i]).getStack();
                        if (!stack.isEmpty()) {
                            drawItemStack(stack, x + 1, slotsY + 1, null);
                        } else {
                            copySlots[i] = -1; // Item gone, remove ref
                        }
                    }
                }
                
                if (mouseX >= x && mouseX < x + 18 && mouseY >= slotsY && mouseY < slotsY + 18) {
                    drawRect(x + 1, slotsY + 1, x + 17, slotsY + 17, 0x80FFFFFF);
                }
            }
            
            int btnY = drawY + 25; // Adjusted Y for buttons below slots
            drawStandardButton(centerX - 30, btnY, "Copy", mouseX, mouseY);
            drawStandardButton(centerX + 30, btnY, "Cancel", mouseX, mouseY);
        }
    }
    
    private void drawStandardButton(int centerX, int y, String text, int mouseX, int mouseY) {
        int width = 50;
        int height = 20;
        int x = centerX - width / 2;
        
        int color = 0xFF888888;
        int innerColor = 0xFF555555;
        
        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
             color = 0xFF999999;
             innerColor = 0xFF666666;
        }
        
        drawRect(x, y, x + width, y + height, color);
        drawRect(x + 1, y + 1, x + width - 1, y + height - 1, innerColor);
        drawCenteredString(parent.getFontRenderer(), text, centerX, y + 6, 0xFFFFFF);
    }

    private void drawBehaviorRow(int guiLeft, int guiTop, int x, int y, int index, NBTTagCompound behaviors, int mouseX, int mouseY) {
        String behaviorType = behaviors.hasKey("Behavior" + index) ? behaviors.getString("Behavior" + index) : "None";
        if (behaviorType.isEmpty()) behaviorType = "None";

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        
        // Use Dropdown component for behavior selector
        Dropdown dropdown = behaviorDropdowns[index];
        dropdown.setSelected(behaviorType);
        dropdown.setOpen(openDropdownIndex == index);
        dropdown.drawButton(x, y, mouseX, mouseY);
        
        // Inputs start at offset
        int inputX = x + 53; 
        
        if ("Movement".equals(behaviorType) || "Move".equals(behaviorType)) {
             Minecraft.getMinecraft().getTextureManager().bindTexture(TARGET_INPUT_TEXTURE);
             
             GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); // Ensure color reset
             drawModalRectWithCustomSizedTexture(inputX - 2, y - 2, 0, 0, 32, 18, 32, 18); 
             
             int slotX = inputX + 10 + 3; 
             int slotY = y - 2 + 1;
             
             NBTTagCompound config = behaviors.getCompoundTag("Config" + index);
             if (config.hasKey("Waypoint")) {
                 ItemStack waypoint = new ItemStack(config.getCompoundTag("Waypoint"));
                 drawItemStack(waypoint, slotX, slotY, null);
             }
             if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                 drawRect(slotX, slotY, slotX + 16, slotY + 16, 0x80FFFFFF);
             }
             
             String method = config.hasKey("Method") ? config.getString("Method") : "Area";
             
             // Use Dropdown component for method selector
             Dropdown methodDrop = methodDropdowns[index];
             methodDrop.setSelected(method);
             methodDrop.setOpen(openSubDropdownIndex == index);
             
             // Ensure color is reset after drawRect might have changed it
             GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
             
             methodDrop.drawButton(inputX + 33, y, mouseX, mouseY);

        } else if ("Obtain".equals(behaviorType)) {
             Minecraft.getMinecraft().getTextureManager().bindTexture(FILTER_INPUT_TEXTURE);
             
             GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
             drawModalRectWithCustomSizedTexture(inputX - 1, y - 2, 0, 0, 85, 18, 85, 18);
             
             NBTTagCompound config = behaviors.getCompoundTag("Config" + index);
             for (int s = 0; s < 4; s++) {
                 int slotX = inputX + 11 + s * 18 + 2; 
                 int slotY = y - 2 + 1;
                 
                 if (config.hasKey("Filter" + s)) {
                     ItemStack filter = new ItemStack(config.getCompoundTag("Filter" + s));
                     drawItemStack(filter, slotX, slotY, null);
                 }
                 if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                     drawRect(slotX, slotY, slotX + 16, slotY + 16, 0x80FFFFFF);
                 }
             }
        }
        GlStateManager.disableBlend();
    }
    
    public boolean handleMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (currentPopup != 0) {
            int centerX = parent.width / 2;
            int centerY = parent.height / 2;
            int offsetY = parent.height / 4;
            int drawY = centerY - offsetY + 24;
            
            if (currentPopup == 1) { // NAMING
                nameField.mouseClicked(mouseX, mouseY, mouseButton);
                int btnY = drawY + 15;
                int btnWidth = 50;
                
                // Confirm: centerX - 30 (center) -> x = centerX - 30 - 25 = -55
                int confirmX = centerX - 30 - 25;
                if (mouseX >= confirmX && mouseX < confirmX + btnWidth && mouseY >= btnY && mouseY < btnY + 20) {
                    setCoreName(nameField.getText());
                    closePopup();
                    return true;
                }
                
                // Cancel: centerX + 30 -> x = centerX + 30 - 25 = +5
                int cancelX = centerX + 30 - 25;
                if (mouseX >= cancelX && mouseX < cancelX + btnWidth && mouseY >= btnY && mouseY < btnY + 20) {
                    closePopup();
                    return true;
                }
            } else if (currentPopup == 2) { // COPYING
                int startX = centerX - (7 * 18) / 2;
                int slotsY = drawY - 10;
                
                // Check slot clicks
                for (int i = 0; i < 7; i++) {
                    int x = startX + i * 18;
                    if (mouseX >= x && mouseX < x + 18 && mouseY >= slotsY && mouseY < slotsY + 18) {
                        handleCopySlotClick(i);
                        return true;
                    }
                }
                
                int btnY = drawY + 25;
                int btnWidth = 50;
                
                // Copy: centerX - 55
                int copyX = centerX - 30 - 25;
                if (mouseX >= copyX && mouseX < copyX + btnWidth && mouseY >= btnY && mouseY < btnY + 20) {
                    executeCopy();
                    closePopup();
                    return true;
                }
                
                // Cancel: centerX + 5
                int cancelX = centerX + 30 - 25;
                if (mouseX >= cancelX && mouseX < cancelX + btnWidth && mouseY >= btnY && mouseY < btnY + 20) {
                    closePopup();
                    return true;
                }
                
                // Check inventory clicks for "Shift-Click" or normal click toggle
                net.minecraft.inventory.Container container = parent.inventorySlots;
                for (net.minecraft.inventory.Slot s : container.inventorySlots) {
                    int guiLeft = parent.getGuiLeft();
                    int guiTop = parent.getGuiTop();
                    if (mouseX >= guiLeft + s.xPos && mouseX < guiLeft + s.xPos + 16 &&
                        mouseY >= guiTop + s.yPos && mouseY < guiTop + s.yPos + 16) {
                        
                        if (s.getStack().getItem() == ModItems.CORE) {
                            // If shift-click or just normal click?
                            // User requested "slots you have to drag or shift click".
                            // Dragging (picking up item) and clicking ghost slot is tricky without item tracking.
                            // We allow "Click to select" as it's the most robust given ghost slots.
                            // Also allow "Shift-Click" behavior (which is same as click here).
                            addToCopyList(s.slotNumber);
                            return true;
                        }
                    }
                }
            }
            
            return true;
        }
    
        int dropdownX = parent.getGuiLeft() + 31;
        int dropdownY = parent.getGuiTop() + 9;
        
        ItemStack coreStack = te.programmingInv.getStackInSlot(0);
        if (coreStack.isEmpty()) return false;
        
        // Naming Button Click (Y+44)
        int btnX = parent.getGuiLeft() + 8;
        int btnY = parent.getGuiTop() + 44;
        if (mouseX >= btnX && mouseX < btnX + 16 && mouseY >= btnY && mouseY < btnY + 16) {
            currentPopup = 1;
            isNamingCore = true; // Kept for legacy if referenced, but logic moved to currentPopup
            nameField.setText(coreStack.getDisplayName());
            nameField.setFocused(true);
            Keyboard.enableRepeatEvents(true);
            return true;
        }
        
        // Copy Button Click (Y+62)
        int copyY = parent.getGuiTop() + 62;
        if (mouseX >= btnX && mouseX < btnX + 16 && mouseY >= copyY && mouseY < copyY + 16) {
            currentPopup = 2;
            Arrays.fill(copySlots, -1);
            Keyboard.enableRepeatEvents(false);
            return true;
        }
        
        // Check open dropdowns first (overlays)
        if (openDropdownIndex != -1) {
            int y = dropdownY + openDropdownIndex * 18;
            if (behaviorDropdowns[openDropdownIndex].handleOverlayClick(dropdownX, y, mouseX, mouseY)) {
                return true;
            }
            openDropdownIndex = -1;
            return true;
        }
        
        if (openSubDropdownIndex != -1) {
            int k = openSubDropdownIndex;
            int y = dropdownY + k * 18;
            int inputX = dropdownX + 50 + 3;
            if (methodDropdowns[k].handleOverlayClick(inputX + 33, y, mouseX, mouseY)) { 
                return true;
            }
            openSubDropdownIndex = -1;
            return true;
        }
        
        int tier = 4;
        if (coreStack.hasTagCompound() && coreStack.getTagCompound().hasKey("Tier")) {
            tier = coreStack.getTagCompound().getInteger("Tier");
        }
        if (tier < 1) tier = 1;
        if (tier > 4) tier = 4;

        // Check buttons and inputs for drag start
        for (int k = 0; k < tier; k++) {
            int y = dropdownY + k * 18;
            int inputX = dropdownX + 53;
            
            NBTTagCompound behaviors = getBehaviorsTag(coreStack);
            String behaviorType = behaviors.getString("Behavior" + k);
            
            boolean hasInput = false;
            // Check drag area (start of input, 8px wide)
            
            if ("Movement".equals(behaviorType) || "Move".equals(behaviorType)) {
                 hasInput = true;
            } else if ("Obtain".equals(behaviorType)) {
                 hasInput = true;
            }
            
            if (hasInput) {
                if (mouseX >= inputX && mouseX < inputX + 8 && mouseY >= y && mouseY < y + 18) {
                    draggingIndex = k;
                    return true;
                }
            }
            
            // Behavior dropdown button
            if (behaviorDropdowns[k].handleButtonClick(dropdownX, y, mouseX, mouseY)) {
                openDropdownIndex = k;
                return true;
            }
            
            // Input Slots/Buttons
            if ("Movement".equals(behaviorType) || "Move".equals(behaviorType)) {
                if (mouseX >= inputX + 10 + 3 && mouseX < inputX + 10 + 3 + 18 && mouseY >= y - 1 && mouseY < y - 1 + 18) {
                    handleGhostSlotClick(k, "Waypoint", -1);
                    return true;
                }
                 // Method dropdown button
                 if (methodDropdowns[k].handleButtonClick(inputX + 33, y, mouseX, mouseY)) {
                     openSubDropdownIndex = k;
                     return true;
                 }
            } else if ("Obtain".equals(behaviorType)) {
                for (int s = 0; s < 4; s++) {
                    if (mouseX >= inputX + 11 + s * 18 + 2 && mouseX < inputX + 11 + s * 18 + 2 + 18 && mouseY >= y - 1 && mouseY < y - 1 + 18) {
                        handleGhostSlotClick(k, "Filter", s);
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    public boolean keyTyped(char typedChar, int keyCode) throws IOException {
        if (currentPopup == 1) { // NAMING
            if (keyCode == 1) { // Escape
                closePopup();
                return true;
            }
            if (keyCode == 28) { // Enter
                setCoreName(nameField.getText());
                closePopup();
                return true;
            }
            nameField.textboxKeyTyped(typedChar, keyCode);
            return true; // Always consume keys in Naming mode to prevent closing screen
        }
        if (currentPopup == 2) { // COPYING
            if (keyCode == 1) { // Escape
                closePopup();
                return true;
            }
            return true; // Consume other keys? or allow?
        }
        return false;
    }
    
    public boolean handleKeyInput(char typedChar, int keyCode) {
         if (currentPopup != 0) {
             try {
                return keyTyped(typedChar, keyCode);
            } catch (IOException e) {
                e.printStackTrace();
            }
         }
         return false;
    }
    
    private void closePopup() {
        currentPopup = 0;
        isNamingCore = false;
        Keyboard.enableRepeatEvents(false);
    }
    
    // Kept for compatibility if used elsewhere, though not recommended
    private boolean isNamingCore = false;
    
    private void setCoreName(String name) {
        ItemStack coreStack = te.programmingInv.getStackInSlot(0);
        NBTTagCompound data = new NBTTagCompound();
        
        // Use standard display name NBT so client renders it correctly
        NBTTagCompound display;
        if (coreStack.hasTagCompound() && coreStack.getTagCompound().hasKey("display")) {
             display = coreStack.getTagCompound().getCompoundTag("display").copy();
        } else {
             display = new NBTTagCompound();
        }
        display.setString("Name", name);
        data.setTag("display", display);
        
        // Custom tags as well if needed by internal logic
        data.setString("CoreName", name);
        data.setBoolean("CoreGlint", true);
        data.setTag("ench", new net.minecraft.nbt.NBTTagList());
        
        PacketUpdateCore pkt = new PacketUpdateCore(data);
        com.apparati.apparati.network.ApparatiNetwork.INSTANCE.sendToServer(pkt);
    }
        
    private void setBehavior(int index, String type) {
        NBTTagCompound data = new NBTTagCompound();
        data.setString("Behavior" + index, type);
        data.setTag("Config" + index, new NBTTagCompound());
        PacketUpdateCore pkt = new PacketUpdateCore(data);
        com.apparati.apparati.network.ApparatiNetwork.INSTANCE.sendToServer(pkt);
    }
    
    private static class BehaviorEntry {
        String type;
        NBTTagCompound config;
        BehaviorEntry(String type, NBTTagCompound config) { this.type = type; this.config = config; }
    }

    private void moveBehavior(int from, int to) {
        ItemStack coreStack = te.programmingInv.getStackInSlot(0);
        NBTTagCompound behaviors = getBehaviorsTag(coreStack);
        int tier = 4;
        if (coreStack.hasTagCompound() && coreStack.getTagCompound().hasKey("Tier")) {
            tier = coreStack.getTagCompound().getInteger("Tier");
        }
        if (tier > 4) tier = 4;
        
        List<BehaviorEntry> list = new ArrayList<>();
        for (int i = 0; i < tier; i++) {
            String b = behaviors.hasKey("Behavior" + i) ? behaviors.getString("Behavior" + i) : "";
            NBTTagCompound c = behaviors.hasKey("Config" + i) ? behaviors.getCompoundTag("Config" + i) : new NBTTagCompound();
            list.add(new BehaviorEntry(b, c));
        }
        
        if (from >= 0 && from < list.size() && to >= 0 && to < list.size()) {
            BehaviorEntry entry = list.remove(from);
            list.add(to, entry);
            
            NBTTagCompound newTag = new NBTTagCompound();
            
            // Apply immediately to local stack to prevent flicker
            NBTTagCompound stackTag = coreStack.hasTagCompound() ? coreStack.getTagCompound() : new NBTTagCompound();
            
            for (int i = 0; i < list.size(); i++) {
                newTag.setString("Behavior" + i, list.get(i).type);
                newTag.setTag("Config" + i, list.get(i).config);
                
                // Update local
                stackTag.setString("Behavior" + i, list.get(i).type);
                stackTag.setTag("Config" + i, list.get(i).config);
            }
            coreStack.setTagCompound(stackTag);
            
            PacketUpdateCore pkt = new PacketUpdateCore(newTag);
            com.apparati.apparati.network.ApparatiNetwork.INSTANCE.sendToServer(pkt);
        }
    }
    
    private void setMovementMethod(int index, String method) {
        ItemStack coreStack = te.programmingInv.getStackInSlot(0);
        NBTTagCompound behaviors = getBehaviorsTag(coreStack);
        NBTTagCompound config = behaviors.getCompoundTag("Config" + index);
        
        NBTTagCompound data = new NBTTagCompound();
        config.setString("Method", method);
        data.setTag("Config" + index, config);
        com.apparati.apparati.network.ApparatiNetwork.INSTANCE.sendToServer(new PacketUpdateCore(data));
    }
    
    private void handleGhostSlotClick(int index, String type, int subIndex) {
        ItemStack held = Minecraft.getMinecraft().player.inventory.getItemStack();
        ItemStack coreStack = te.programmingInv.getStackInSlot(0);
        NBTTagCompound behaviors = getBehaviorsTag(coreStack);
        NBTTagCompound config = behaviors.getCompoundTag("Config" + index);
        
        if ("Waypoint".equals(type)) {
            if (held.isEmpty()) {
                config.removeTag("Waypoint");
            } else {
                ItemStack copy = held.copy();
                copy.setCount(1);
                config.setTag("Waypoint", copy.writeToNBT(new NBTTagCompound()));
            }
        } else if ("Filter".equals(type)) {
            String key = "Filter" + subIndex;
            if (held.isEmpty()) {
                config.removeTag(key);
            } else {
                ItemStack copy = held.copy();
                copy.setCount(1);
                config.setTag(key, copy.writeToNBT(new NBTTagCompound()));
            }
        }
        
        NBTTagCompound data = new NBTTagCompound();
        data.setTag("Config" + index, config);
        com.apparati.apparati.network.ApparatiNetwork.INSTANCE.sendToServer(new PacketUpdateCore(data));
    }
    
    private void handleCopySlotClick(int index) {
        if (copySlots[index] != -1) {
            // Already set, click to clear
            copySlots[index] = -1;
        } 
        // Note: Adding items logic is handled in handleMouseClick via inventory interaction
    }

    private void addToCopyList(int slotId) {
        // Check if already present
        for (int i = 0; i < 7; i++) {
            if (copySlots[i] == slotId) return; // Already added
        }
        // Add to first empty
        for (int i = 0; i < 7; i++) {
            if (copySlots[i] == -1) {
                copySlots[i] = slotId;
                return;
            }
        }
    }
    
    private void executeCopy() {
        List<Integer> validSlots = new ArrayList<>();
        for (int id : copySlots) {
            if (id != -1) validSlots.add(id);
        }
        if (!validSlots.isEmpty()) {
            PacketCopyCore pkt = new PacketCopyCore(validSlots);
            com.apparati.apparati.network.ApparatiNetwork.INSTANCE.sendToServer(pkt);
        }
    }

    private NBTTagCompound getBehaviorsTag(ItemStack stack) {
        if (!stack.isEmpty() && stack.hasTagCompound()) {
            return stack.getTagCompound();
        }
        return new NBTTagCompound();
    }
    
    private void drawItemStack(ItemStack stack, int x, int y, String altText) {
        GlStateManager.translate(0.0F, 0.0F, 32.0F);
        this.zLevel = 200.0F;
        this.itemRender.zLevel = 200.0F;
        net.minecraft.client.gui.FontRenderer font = null;
        if (!stack.isEmpty()) font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = parent.getFontRenderer();
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        RenderHelper.enableGUIStandardItemLighting();
        this.itemRender.renderItemAndEffectIntoGUI(stack, x, y);
        this.itemRender.renderItemOverlayIntoGUI(font, stack, x, y, altText);
        RenderHelper.disableStandardItemLighting();
        
        this.zLevel = 0.0F;
        this.itemRender.zLevel = 0.0F;
        GlStateManager.translate(0.0F, 0.0F, -32.0F);
    }
    
    private class Dropdown {
        private final List<String> options;
        private final int width;
        private final int height = 14;
        private String selected = "";
        private boolean isOpen = false;
        private Callback callback;
        
        public Dropdown(List<String> options, int width) {
            this.options = options;
            this.width = width;
        }
        
        public void setCallback(Callback callback) {
            this.callback = callback;
        }
        
        public void setSelected(String selected) {
            this.selected = selected;
        }
        
        public void setOpen(boolean isOpen) {
            this.isOpen = isOpen;
        }
        
        public void drawButton(int x, int y, int mouseX, int mouseY) {
            if (isOpen) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(DROPDOWN_INPUT_ACTIVE);
            } else {
                Minecraft.getMinecraft().getTextureManager().bindTexture(DROPDOWN_INPUT);
            }
            
            GlStateManager.enableBlend();
            drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, 50, 14);
            
            parent.getFontRenderer().drawString(selected, x + 4, y + 3, 0xFFFFFF);
        }
        
        public void drawOverlay(int x, int y, int mouseX, int mouseY) {
            GlStateManager.translate(0, 0, 500);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            Minecraft.getMinecraft().getTextureManager().bindTexture(DROPDOWN_BG);
            
            // Trim 1px top and 1px bottom. Height depends on options.
            
            int overlayHeight = options.size() * 12 + 2;
            
            drawModalRectWithCustomSizedTexture(x, y + 13, 0, 1, width, overlayHeight, 50, 40); 
            
            for (int i = 0; i < options.size(); i++) {
                parent.getFontRenderer().drawString(options.get(i), x + 4, y + 13 + 3 + i * 12, 0xFFFFFF);
                
                // Highlight hover
                int optY = y + 13 + 2 + i * 12;
                if (mouseX >= x && mouseX < x + width && mouseY >= optY && mouseY < optY + 12) {
                     drawRect(x + 1, optY, x + width - 1, optY + 12, 0x40FFFFFF);
                }
            }
            GlStateManager.disableBlend();
            GlStateManager.translate(0, 0, -500);
        }
        
        public boolean handleButtonClick(int x, int y, int mouseX, int mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
        
        public boolean handleOverlayClick(int x, int y, int mouseX, int mouseY) {
             int overlayY = y + 13;
             int overlayHeight = options.size() * 12 + 2;
             
             if (mouseX >= x && mouseX < x + width && mouseY >= overlayY && mouseY < overlayY + overlayHeight) {
                 int idx = (mouseY - (overlayY + 2)) / 12;
                 if (idx >= 0 && idx < options.size()) {
                     if (callback != null) {
                         callback.onSelect(options.get(idx));
                     }
                     return true;
                 }
             }
             return false;
        }
    }
    
    @FunctionalInterface
    private interface Callback {
        void onSelect(String option);
    }
}
