package dev.booky.packetloggermapper.mixin;
// Created by booky10 in PacketLoggerMapper (14:27 16.04.23)

import com.google.gson.JsonObject;
import de.ari24.packetlogger.packets.BasePacketHandler;
import de.ari24.packetlogger.packets.PacketHandler;
import dev.booky.stackdeobf.mappings.RemappingUtil;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

@Mixin(PacketHandler.class)
public class PacketHandlerMixin {

    @Inject(
            method = "handlePacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/ari24/packetlogger/web/WebsocketServer;sendAll(Lcom/google/gson/JsonObject;)V",
                    shift = At.Shift.BEFORE,
                    remap = false
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            remap = false
    )
    private static <T extends Packet<?>> void preObjectSend(T packet, CallbackInfo ci, BasePacketHandler<?> handler, BasePacketHandler<T> packetHandler, JsonObject jsonObject, ConnectionProtocol state, int packetId) {
        String className = getPacketClassName(packet.getClass());
        jsonObject.addProperty("name", RemappingUtil.remapClasses(className));
        jsonObject.addProperty("legacyName", className
                // looks better in the UI imo
                + " ");
    }

    @Inject(
            method = "getRegisteredPacketIds",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/ArrayList;add(Ljava/lang/Object;)Z",
                    shift = At.Shift.BEFORE,
                    remap = false
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            remap = false
    )
    private static void prePacketNameAdd(CallbackInfoReturnable<ArrayList<JsonObject>> cir, ArrayList<JsonObject> ids, Iterator<Map.Entry<Class<? extends Packet<?>>, BasePacketHandler<?>>> var1, Map.Entry<Class<? extends Packet<?>>, BasePacketHandler<?>> entry, Class<? extends Packet<?>> packetClass, BasePacketHandler<?> handler, JsonObject jsonObject, String id) {
        String remappedClassName = RemappingUtil.remapClasses(getPacketClassName(packetClass));
        jsonObject.addProperty("label", remappedClassName);
    }

    @Unique
    private static String getPacketClassName(Class<?> clazz) {
        StringBuilder className = new StringBuilder(clazz.getSimpleName());
        clazz = clazz.getSuperclass();
        while (clazz != Packet.class && clazz != Object.class && clazz != Record.class && clazz != null) {
            className.insert(0, '$');
            className.insert(0, clazz.getSimpleName());
            clazz = clazz.getSuperclass();
        }
        return className.toString();
    }
}
