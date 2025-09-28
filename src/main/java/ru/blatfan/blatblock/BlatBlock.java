package ru.blatfan.blatskills;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(BlatSkills.MOD_ID)
public class BlatSkills {
    public static final String MOD_ID = "blatskills";
    public static final Logger LOGGER = LoggerFactory.getLogger("BlatSkills");
    public static final ;
    
    public BlatSkills() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        
        
        
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SkillsConfig.Common.SPEC, "blatfan/"+ MOD_ID +"-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SkillsConfig.Client.SPEC, "blatfan/"+ MOD_ID +"-client.toml");
    }
    
    public static class Handler extends {

    }
}