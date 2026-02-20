package com.apparati.apparati.content;

import com.apparati.apparati.Constants;
import com.apparati.apparati.content.client.ApparatiRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class RegistrationHandler {

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        EntityEntry apparati = EntityEntryBuilder.create()
                .entity(ApparatiEntity.class)
                .id(new ResourceLocation(Constants.MOD_ID, "apparati"), 0)
                .name("apparati")
                .tracker(64, 3, true)
                .build();
        event.getRegistry().register(apparati);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(RegistryEvent.Register<net.minecraft.block.Block> event) {
        RenderingRegistry.registerEntityRenderingHandler(ApparatiEntity.class, ApparatiRenderer::new);
        // Ensure the part item renderer is initialized
        com.apparati.apparati.content.client.ApparatiPartItemRenderer.INSTANCE.getClass();
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<net.minecraft.block.Block> event) {
        event.getRegistry().register(new BlockApparatiAssembler());
        net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity(TileEntityApparatiAssembler.class, new ResourceLocation(Constants.MOD_ID, "apparati_assembler"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<net.minecraft.item.Item> event) {
        net.minecraft.block.Block assembler = net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Constants.MOD_ID, "apparati_assembler"));
        event.getRegistry().register(new net.minecraft.item.ItemBlock(assembler).setRegistryName("apparati_assembler").setCreativeTab(net.minecraft.creativetab.CreativeTabs.DECORATIONS));
        
        event.getRegistry().register(new ApparatiPartItem("core", ApparatiPartItem.PartType.CORE, 0).setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC));
        event.getRegistry().register(new ApparatiPartItem("head_redstone_antennae", ApparatiPartItem.PartType.HEAD_REDSTONE_ANTENNAE, 1).setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC));
        event.getRegistry().register(new ApparatiPartItem("head_camera_lens", ApparatiPartItem.PartType.HEAD_CAMERA_LENS, 1).setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC));
        event.getRegistry().register(new ApparatiPartItem("head_microphone", ApparatiPartItem.PartType.HEAD_MICROPHONE, 1).setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC));
        event.getRegistry().register(new ApparatiPartItem("arm_holder", ApparatiPartItem.PartType.ARM_HOLDER, 1).setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC));
        event.getRegistry().register(new ApparatiPartItem("arm_placer", ApparatiPartItem.PartType.ARM_PLACER, 1).setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC));
        event.getRegistry().register(new ApparatiPartItem("chassis_hollow", ApparatiPartItem.PartType.CHASSIS_HOLLOW, 1).setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC));
        event.getRegistry().register(new ApparatiPartItem("chassis_chest", ApparatiPartItem.PartType.CHASSIS_CHEST, 1).setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC));
        event.getRegistry().register(new ApparatiPartItem("chassis_solid", ApparatiPartItem.PartType.CHASSIS_SOLID, 1).setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC));
        event.getRegistry().register(new ApparatiPartItem("treads_wheelie", ApparatiPartItem.PartType.TREADS_WHEELIE, 1).setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC));
    }
}
