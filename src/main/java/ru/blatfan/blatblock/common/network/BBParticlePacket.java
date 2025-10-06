package ru.blatfan.blatblock.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;
import ru.blatfan.blatapi.fluffy_fur.FluffyFur;
import ru.blatfan.blatapi.fluffy_fur.client.particle.ParticleBuilder;
import ru.blatfan.blatapi.fluffy_fur.client.particle.data.ColorParticleData;
import ru.blatfan.blatapi.fluffy_fur.client.particle.data.GenericParticleData;
import ru.blatfan.blatapi.fluffy_fur.client.particle.data.LightParticleData;
import ru.blatfan.blatapi.fluffy_fur.client.particle.data.SpriteParticleData;
import ru.blatfan.blatapi.fluffy_fur.client.particle.options.ItemParticleOptions;
import ru.blatfan.blatapi.fluffy_fur.common.easing.Easing;
import ru.blatfan.blatapi.fluffy_fur.common.network.ClientPacket;
import ru.blatfan.blatapi.fluffy_fur.common.network.PositionClientPacket;
import ru.blatfan.blatapi.fluffy_fur.registry.client.FluffyFurParticles;
import ru.blatfan.blatapi.fluffy_fur.registry.client.FluffyFurRenderTypes;

import java.awt.*;
import java.util.function.Supplier;

public class BBParticlePacket extends PositionClientPacket {
    private final ItemStack drop;
    
    @OnlyIn(Dist.CLIENT)
    public void execute(Supplier<NetworkEvent.Context> context) {
        Level level = FluffyFur.proxy.getLevel();
        ParticleBuilder.create(new ItemParticleOptions(FluffyFurParticles.ITEM.get(), drop))
            .setRenderType(FluffyFurRenderTypes.TRANSLUCENT_BLOCK_PARTICLE)
            .setColorData(ColorParticleData.create(Color.WHITE).build())
            .setSpriteData(SpriteParticleData.CRUMBS_RANDOM)
            .setTransparencyData(GenericParticleData.create(1, 0).setEasing(Easing.QUARTIC_OUT).build())
            .setScaleData(GenericParticleData.create(0.025f, 0.0125f, 0).setEasing(Easing.ELASTIC_OUT).build())
            .setLifetime(100).randomVelocity(0.035f, 0.035f, 0.035f).setLightData(LightParticleData.DEFAULT)
            .spawn(level, x, y, z);
    }
    
    public static void register(SimpleChannel instance, int index) {
        instance.registerMessage(index, BBParticlePacket.class, BBParticlePacket::encode, BBParticlePacket::new, ClientPacket::handle);
    }
    
    public BBParticlePacket(Vec3 vec, ItemStack drop) {
        super(vec);
        this.drop = drop;
    }
    
    public BBParticlePacket(FriendlyByteBuf buf) {
        super(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.drop = buf.readItem();
    }
    
    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeItem(drop);
    }
}
