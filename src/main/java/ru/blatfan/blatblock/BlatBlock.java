package ru.blatfan.blatblock;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.blatfan.blatapi.fluffy_fur.FluffyFurClient;
import ru.blatfan.blatapi.fluffy_fur.client.gui.screen.FluffyFurMod;
import ru.blatfan.blatapi.fluffy_fur.client.gui.screen.FluffyFurPanorama;
import ru.blatfan.blatblock.client.block_render.BlatGeneratorRenderer;
import ru.blatfan.blatblock.common.BBRegistry;
import ru.blatfan.blatblock.common.data.BlatBlockManager;
import ru.blatfan.blatblock.common.events.GeneratorEvents;
import ru.blatfan.blatblock.common.network.BBHandler;

import java.awt.*;

@Mod(BlatBlock.MOD_ID)
public class BlatBlock {
    public static final String MOD_ID = "blatblock";
    public static final String MOD_NAME = "BlatBlock";
    public static final String MOD_VERSION = "0.2";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final boolean DEBUG_MODE = true;
    
    public BlatBlock() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::common);
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new BlatBlockManager()));
        MinecraftForge.EVENT_BUS.register(GeneratorEvents.class);
        BBRegistry.init(bus);
    }
    
    public void common(FMLCommonSetupEvent event){
        BBHandler.init();
    }
    
    public static ResourceLocation loc(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
    
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            FluffyFurMod MOD = new FluffyFurMod(MOD_ID, MOD_NAME, MOD_VERSION).setDev("BlatFan")
                .setItem(new ItemStack(BBRegistry.ITEMS.NETHERITE_MULTITOOL.get())).setNameColor(new Color(142, 95, 239))
                .setVersionColor(new Color(65, 36, 138)).setDescription(Component.literal("New generation of OneBlock maps and modpacks"))
                .addCurseForgeLink("https://www.curseforge.com/minecraft/mc-mods/blatblock")
                .addModrinthLink("https://modrinth.com/project/blatblock")
                .addDiscordLink("https://discord.com/channels/1134588677121654925/1421902783522668657")
                .addGithubLink("https://github.com/BlatFan/BlatBlock");
            FluffyFurPanorama PANORAMA = new FluffyFurPanorama(MOD_ID + ":panorama", Component.literal("BlatBlock"))
                .setMod(MOD).setItem(new ItemStack(BBRegistry.ITEMS.WOODEN_MULTITOOL.get()))
                .setTexture(loc("textures/gui/blatapi/panorama"))
                .setLogo(loc("textures/gui/blatapi/logo.png"));
            
            FluffyFurClient.registerMod(MOD);
            FluffyFurClient.registerPanorama(PANORAMA);
        }
        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(BBRegistry.BLOCK_ENTITIES.BLAT_GENERATOR.get(),
                BlatGeneratorRenderer::new);
        }
    }
}