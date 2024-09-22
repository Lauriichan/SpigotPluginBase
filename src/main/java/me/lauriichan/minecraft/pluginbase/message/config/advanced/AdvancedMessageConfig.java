package me.lauriichan.minecraft.pluginbase.message.config.advanced;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.laylib.localization.MessageProvider;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.ConditionConstant;
import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.config.IConfigHandler;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.extension.ExtensionCondition;
import me.lauriichan.minecraft.pluginbase.message.config.MessageConfig;

@Extension
@ExtensionCondition(name = ConditionConstant.USE_MULTILANG_CONFIG, condition = true)
public class AdvancedMessageConfig extends MessageConfig {

    private final MessageManager messageManager;

    public AdvancedMessageConfig(final BasePlugin<?> plugin) {
        this.messageManager = plugin.messageManager();
    }

    @Override
    public String path() {
        return "data://language/";
    }

    @Override
    public IConfigHandler handler() {
        return LanguageConfigHandler.LANGUAGE;
    }

    @Override
    public void onLoad(ISimpleLogger logger, final Configuration configuration) throws Exception {
        if (!configuration.contains(Actor.DEFAULT_LANGUAGE)) {
            configuration.getConfiguration(Actor.DEFAULT_LANGUAGE, true);
        }
        final MessageProvider[] providers = messageManager.getProviders();
        for (final String key : configuration.keySet()) {
            loadMessages(configuration.getConfiguration(key), key, providers);
        }
    }

    @Override
    public void onSave(ISimpleLogger logger, final Configuration configuration) throws Exception {
        if (!configuration.contains(Actor.DEFAULT_LANGUAGE)) {
            configuration.getConfiguration(Actor.DEFAULT_LANGUAGE, true);
        }
        final MessageProvider[] providers = messageManager.getProviders();
        for (final String key : configuration.keySet()) {
            saveMessages(configuration.getConfiguration(key), key, providers);
        }
    }

}
