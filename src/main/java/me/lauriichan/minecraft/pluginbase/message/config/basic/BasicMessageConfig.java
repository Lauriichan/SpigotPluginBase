package me.lauriichan.minecraft.pluginbase.message.config.basic;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.ConditionConstant;
import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.config.IConfigHandler;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.extension.ExtensionCondition;
import me.lauriichan.minecraft.pluginbase.message.config.MessageConfig;

@Extension
@ExtensionCondition(name = ConditionConstant.USE_MULTILANG_CONFIG, condition = false, activeByDefault = true)
public class BasicMessageConfig extends MessageConfig {

    private final MessageManager messageManager;

    public BasicMessageConfig(final BasePlugin<?> plugin) {
        this.messageManager = plugin.messageManager();
    }

    @Override
    public String path() {
        return "data://message.json";
    }

    @Override
    public IConfigHandler handler() {
        return MessageConfigHandler.MESSAGE;
    }

    @Override
    public void onLoad(ISimpleLogger logger, final Configuration configuration) throws Exception {
        loadMessages(configuration, Actor.DEFAULT_LANGUAGE, messageManager.getProviders());
    }

    @Override
    public void onSave(ISimpleLogger logger, final Configuration configuration) throws Exception {
        saveMessages(configuration, Actor.DEFAULT_LANGUAGE, messageManager.getProviders());
    }

}
