package me.lauriichan.minecraft.pluginbase.config;

import me.lauriichan.minecraft.pluginbase.resource.source.IDataSource;

public interface IConfigHandler {

    void load(Configuration configuration, IDataSource source, boolean onlyRaw) throws Exception;

    void save(Configuration configuration, IDataSource source) throws Exception;

}
