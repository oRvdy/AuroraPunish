package com.auroramc.punish.listener;

import com.auroramc.punish.Main;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.Set;

public abstract class ListenersManager implements Listener {

    public static void setupListeners(Plugin plugin) {
        Reflections reflections = new Reflections(plugin.getClass().getPackage().getName());
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(com.auroramc.punish.listener.Listener.class);
        for (Class<?> clazz : annotatedClasses) {
            try {
                Constructor<?> constructor = clazz.getConstructor();
                Listener listener = (Listener) constructor.newInstance();
                Main.getInstance().getProxy().getPluginManager().registerListener(plugin, listener);
            } catch (ReflectiveOperationException e) {
                Main.getInstance().sendMessage("Failed to register listener: " + clazz.getName(), 'c');
                throw new RuntimeException(e);
            }
        }
    }

    public ListenersManager() {
        Main.getInstance().getProxy().getPluginManager().registerListener(Main.getInstance(), this);
    }
}
