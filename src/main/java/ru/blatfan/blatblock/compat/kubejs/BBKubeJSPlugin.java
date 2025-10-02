package ru.blatfan.blatblock.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;
import ru.blatfan.blatblock.compat.kubejs.bbl.BBLRegistryJS;
import ru.blatfan.blatblock.compat.kubejs.items.*;

public class BBKubeJSPlugin extends KubeJSPlugin {
    @Override
    public void init() {
        RegistryInfo.ITEM.addType("blatblock:multitool", MultitoolItemBuilder.class, MultitoolItemBuilder::new);
    }
    
    @Override
    public void registerClasses(ScriptType type, ClassFilter filter) {
        filter.allow("ru.blatfan.blatblock.compat.kubejs.bbl");
        filter.allow("ru.blatfan.blatblock.common.data");
    }
    
    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("BBLRegistry", BBLRegistryJS.INSTANCE);
    }
}