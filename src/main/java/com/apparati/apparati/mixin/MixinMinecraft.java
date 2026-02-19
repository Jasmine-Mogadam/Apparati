package com.apparati.apparati.mixin;

import com.apparati.apparati.Constants;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    
    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(CallbackInfo info) {
        
        Constants.LOG.info("This line is printed by an example mod common mixin!");
        // Minecraft.getMinecraft().getVersionType() doesn't exist in 1.12.2 in the same way, 
        // usually it's just a string or handled differently. 
        // We'll just print a static message or something else that exists.
        Constants.LOG.info("MC Version: 1.12.2");
    }
}
