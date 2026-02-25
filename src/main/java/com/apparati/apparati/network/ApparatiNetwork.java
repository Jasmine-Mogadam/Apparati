package com.apparati.apparati.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import com.apparati.apparati.Constants;

public class ApparatiNetwork {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MOD_ID);

    public static void init() {
        INSTANCE.registerMessage(PacketPackageApparati.Handler.class, PacketPackageApparati.class, 0, Side.SERVER);
        INSTANCE.registerMessage(PacketApparatiTab.Handler.class, PacketApparatiTab.class, 1, Side.SERVER);
        INSTANCE.registerMessage(PacketUpdateCore.Handler.class, PacketUpdateCore.class, 2, Side.SERVER);
        INSTANCE.registerMessage(PacketCopyCore.Handler.class, PacketCopyCore.class, 3, Side.SERVER);
    }
}
