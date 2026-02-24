package com.apparati.apparati.content;

import com.apparati.apparati.Constants;
import com.apparati.apparati.content.client.ApparatiRenderer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientRegistrationHandler {

    @SubscribeEvent
    public static void registerEntityRenderers(net.minecraftforge.event.RegistryEvent.Register<EntityEntry> event) {
        RenderingRegistry.registerEntityRenderingHandler(ApparatiEntity.class, ApparatiRenderer::new);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        System.out.println("Apparati: Registering models...");
        // Register models for all items in our mod
        for (Item item : net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.getValuesCollection()) {
            if (item.getRegistryName().getResourceDomain().equals(Constants.MOD_ID)) {
                registerItemModel(item);
                
                // Assign TEISR here, where we know client systems are ready
                if (item instanceof ApparatiPartItem) {
                    ApparatiPartItem part = (ApparatiPartItem) item;
                    if (part.getPartType() != ApparatiPartItem.PartType.CORE) {
                        System.out.println("Apparati: Setting TEISR for " + item.getRegistryName());
                        item.setTileEntityItemStackRenderer(com.apparati.apparati.content.client.ApparatiPartItemRenderer.INSTANCE);
                    }
                }
            }
        }
    }

    private static void registerItemModel(Item item) {
        if (item != null && item.getRegistryName() != null) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }
}
