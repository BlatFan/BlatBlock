package ru.blatfan.blatblock.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.common.block.blatgenerator.BlatGeneratorBlockEntity;
import ru.blatfan.blatblock.common.data.BBLayerManager;

import java.util.function.Supplier;

public class BlatGeneratorPacket {
    private final int x;
    private final int y;
    private final int z;
    private final ResourceLocation bbl;
    private final Type type;
    
    public BlatGeneratorPacket(int x, int y, int z, ResourceLocation bbl, Type type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.bbl = bbl;
        this.type = type;
    }
    
    public BlatGeneratorPacket(BlockPos pos, ResourceLocation bbl, Type type) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.bbl = bbl;
        this.type = type;
    }
    
    public static void register(SimpleChannel instance, int index) {
        instance.registerMessage(index, BlatGeneratorPacket.class, BlatGeneratorPacket::encode, BlatGeneratorPacket::decode, BlatGeneratorPacket::handle);
    }
    
    public final void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (context.get().getDirection().getReceptionSide().equals(LogicalSide.SERVER)) {
                try {
                    Level level = context.get().getSender().level();
                    if (level.getBlockEntity(new BlockPos(x, y, z)) instanceof BlatGeneratorBlockEntity generator && !level.isClientSide) {
                        if(generator.getAllMinedBlock()< BBLayerManager.get(bbl).getBlockCost()) return;
                        if (type == Type.BUY) generator.setMinedBlock(bbl, 1);
                        generator.setCurrentLayer(bbl);
                        generator.setChanged();
                    }
                } catch (Exception e) {
                    BlatBlock.LOGGER.error("Error handling BlatGenerator packet: {}", e.getMessage());
                }
            }
        });
        context.get().setPacketHandled(true);
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeUtf(this.bbl.toString());
        buf.writeEnum(this.type);
    }
    
    public static BlatGeneratorPacket decode(FriendlyByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        ResourceLocation bbl = ResourceLocation.tryParse(buf.readUtf());
        Type type = buf.readEnum(Type.class);
        return new BlatGeneratorPacket(x, y, z, bbl, type);
    }
    
    public enum Type{SET, BUY}
}