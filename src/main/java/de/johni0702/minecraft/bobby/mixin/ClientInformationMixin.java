package de.johni0702.minecraft.bobby.mixin;

import net.minecraft.server.level.ClientInformation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientInformation.class)
public abstract class ClientInformationMixin {
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeByte(I)Lnet/minecraft/network/FriendlyByteBuf;", ordinal = 0))
    private int clampMaxValue(int viewDistance) {
        return Math.min(viewDistance, Byte.MAX_VALUE);
    }
}
