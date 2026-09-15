package dose.killeffect.mixin.client;

import dose.killeffect.client.PacketTestClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public abstract class ClientCommonNetworkHandlerMixin {
    @Inject(method = "sendPacket", at = @At("HEAD"))
    private void killeffect$captureOutgoingPacket(Packet<?> packet, CallbackInfo ci) {
        PacketTestClient.onOutgoingPacket(packet);
    }
}
