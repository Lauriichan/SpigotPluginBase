package me.lauriichan.minecraft.pluginbase.message.config;

import java.io.File;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.ConditionConstant;
import me.lauriichan.minecraft.pluginbase.config.IMultiConfigExtension;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.extension.ExtensionCondition;

@Extension
@ExtensionCondition(name = ConditionConstant.USE_MULTILANG_CONFIG, condition = true, activeByDefault = false)
public final class MultiMessageConfig implements IMultiConfigExtension<String, String, MessageConfig> {

    private final BasePlugin<?> plugin;
    private final File folder;

    public MultiMessageConfig(final BasePlugin<?> plugin) {
        this.plugin = plugin;
        this.folder = (File) plugin.resource("data://language").getSource();
    }

    @Override
    public Class<MessageConfig> type() {
        return MessageConfig.class;
    }

    @Override
    public String getConfigKey(String element) {
        return element;
    }

    @Override
    public String path(String element) {
        return String.format("data://language/%s.json", element);
    }

    @Override
    public MessageConfig create() {
        return new MessageConfig(plugin);
    }
    
    @Override
    public void onLoad(ISimpleLogger logger) {
        if (!folder.exists()) {
            return;
        }
        for (File file : folder.listFiles()) {
            if (file.getName().endsWith(".json")) {
                continue;
            }
            String langName = file.getName().substring(0, file.getName().length() - 5);
            plugin.configManager().multiWrapperOrCreate(getClass(), langName).reload(false);
        }
    }

}
