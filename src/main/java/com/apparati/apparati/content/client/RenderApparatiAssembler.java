package com.apparati.apparati.content.client;

import com.apparati.apparati.content.TileEntityApparatiAssembler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;

public class RenderApparatiAssembler extends TileEntitySpecialRenderer<TileEntityApparatiAssembler> {
    @Override
    public void render(TileEntityApparatiAssembler te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te.getActiveTab() != 1) return; // Only show assembly animation in assembly tab? 
        // Or maybe always show it if items are present.
        
        long time = te.getWorld().getTotalWorldTime();
        float bob = (float) Math.sin((time + partialTicks) * 0.1f) * 0.05f;

        // Cross pattern slots: 0: Head, 1: Chassis, 2: Treads, 3: Left Arm, 4: Right Arm
        renderItem(te.assemblyInv.getStackInSlot(0), x + 0.5, y + 1.6 + bob, z + 0.5); // Top
        renderItem(te.assemblyInv.getStackInSlot(1), x + 0.5, y + 1.2 + bob, z + 0.5); // Middle
        renderItem(te.assemblyInv.getStackInSlot(2), x + 0.5, y + 0.8 + bob, z + 0.5); // Bottom
        renderItem(te.assemblyInv.getStackInSlot(3), x + 0.2, y + 1.2 + bob, z + 0.5); // Left
        renderItem(te.assemblyInv.getStackInSlot(4), x + 0.8, y + 1.2 + bob, z + 0.5); // Right
    }

    private void renderItem(ItemStack stack, double x, double y, double z) {
        if (stack.isEmpty()) return;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.scale(0.5, 0.5, 0.5);
        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
        GlStateManager.popMatrix();
    }
}
