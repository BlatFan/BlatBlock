package ru.blatfan.blatblock;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.blatfan.blatapi.fluffy_fur.FluffyFurClient;
import ru.blatfan.blatapi.fluffy_fur.client.gui.screen.FluffyFurMod;
import ru.blatfan.blatapi.fluffy_fur.client.gui.screen.FluffyFurPanorama;
import ru.blatfan.blatblock.client.block_render.BlatGeneratorRenderer;
import ru.blatfan.blatblock.client.gui.AutoGeneratorScreen;
import ru.blatfan.blatblock.common.BBRegistry;
import ru.blatfan.blatblock.common.data.BBLayerManager;
import ru.blatfan.blatblock.common.events.GeneratorEvents;
import ru.blatfan.blatblock.common.network.BBHandler;

import java.awt.*;

@Mod(BlatBlock.MOD_ID)
public class BlatBlock {
    public static final String MOD_ID = "blatblock";
    public static final String MOD_NAME = "BlatBlock";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final String MOD_VERSION = "0.6";
    
    public BlatBlock() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::common);
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new BBLayerManager()));
        MinecraftForge.EVENT_BUS.register(GeneratorEvents.class);
        BBRegistry.init(bus);
        
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigClient.SPEC, "blatfan/"+MOD_ID+"-client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigCommon.SPEC, "blatfan/"+MOD_ID+"-common.toml");
    }
    
    public void common(FMLCommonSetupEvent event){
        BBHandler.init();
    }
    
    public static ResourceLocation loc(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
    
    public static class ConfigCommon {
        public static final ForgeConfigSpec SPEC;
        public static final ForgeConfigSpec.IntValue BASIC_TICK_RATE;
        public static final ForgeConfigSpec.IntValue IMPROVED_TICK_RATE;
        public static final ForgeConfigSpec.IntValue PERFECT_TICK_RATE;
        
        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            
            BASIC_TICK_RATE=builder.defineInRange("auto_generators.basic", 40, 0, Integer.MAX_VALUE);
            IMPROVED_TICK_RATE=builder.defineInRange("auto_generators.improved", 20, 0, Integer.MAX_VALUE);
            PERFECT_TICK_RATE=builder.defineInRange("auto_generators.perfect", 5, 0, Integer.MAX_VALUE);
            
            SPEC=builder.build();
        }
    }
    public static class ConfigClient {
        public static final ForgeConfigSpec SPEC;
        public static final ForgeConfigSpec.BooleanValue GENERATOR_HOLOGRAM;
        public static final ForgeConfigSpec.BooleanValue ITEM_OVERCOUNT_COLOR;
        public static final ConfigColor ITEM_COUNT_COLOR;
        
        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            
            GENERATOR_HOLOGRAM=builder.define("generator_hologram", true);
            ITEM_OVERCOUNT_COLOR=builder.define("item_overcount_color", true);
            ITEM_COUNT_COLOR=define(builder, "item_count_color", new Color(206, 92, 255));
            
            SPEC=builder.build();
        }
        
        public static ConfigColor define(ForgeConfigSpec.Builder builder, String id, Color defaultValue){
            return new ConfigColor(
                builder.defineInRange(id+".r", defaultValue.getRed(), 0, 255),
                builder.defineInRange(id+".g", defaultValue.getGreen(), 0, 255),
                builder.defineInRange(id+".b", defaultValue.getBlue(), 0, 255)
            );
        }
        
        public record ConfigColor(ForgeConfigSpec.IntValue r, ForgeConfigSpec.IntValue g, ForgeConfigSpec.IntValue b){
            public Color get(){
                return new Color(r.get(), g.get(), b.get());
            }
        }
    }
    
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            FluffyFurMod MOD = new FluffyFurMod(MOD_ID, MOD_NAME, MOD_VERSION).setDev("BlatFan")
                .setItem(new ItemStack(BBRegistry.ITEMS.DIAMOND_MULTITOOL.get())).setNameColor(new Color(142, 95, 239))
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
            
            MenuScreens.register(BBRegistry.AUTO_GENERATOR_MENU.get(), AutoGeneratorScreen::new);
        }
        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(BBRegistry.BLOCK_ENTITIES.BLAT_GENERATOR.get(),
                BlatGeneratorRenderer::new);
        }
    }
}