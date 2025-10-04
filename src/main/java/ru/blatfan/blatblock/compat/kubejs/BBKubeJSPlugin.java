package ru.blatfan.blatblock.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;
import ru.blatfan.blatblock.compat.kubejs.bbl.BBLEventJS;
import ru.blatfan.blatblock.compat.kubejs.items.*;

public class BBKubeJSPlugin extends KubeJSPlugin {
    public static final EventGroup GROUP = EventGroup.of("BBLRegistry");
    public static final EventHandler REGISTER = GROUP.server("register", ()-> BBLEventJS.class);
    @Override
    public void init() {
        RegistryInfo.ITEM.addType("blatblock:multitool", MultitoolItemBuilder.class, MultitoolItemBuilder::new);
    }
    
    public static void post(){
        BBKubeJSPlugin.REGISTER.post(new BBLEventJS());
    }
    
    @Override
    public void registerEvents() {
        GROUP.register();
    }
    
    @Override
    public void registerClasses(ScriptType type, ClassFilter filter) {
        filter.allow("ru.blatfan.blatblock.compat.kubejs.bbl");
        filter.allow("ru.blatfan.blatblock.common.data");
    }
    
    @Override
    public void registerBindings(BindingsEvent event) {
    }
}