package me.lauriichan.minecraft.pluginbase.message.config;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.IMessage;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.laylib.localization.MessageProvider;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.ConditionConstant;
import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.config.IConfigHandler;
import me.lauriichan.minecraft.pluginbase.config.ISingleConfigExtension;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.extension.ExtensionCondition;
import me.lauriichan.minecraft.pluginbase.message.provider.ISimpleFallback;
import me.lauriichan.minecraft.pluginbase.message.provider.ISimpleMessage;

@Extension
@ExtensionCondition(name = ConditionConstant.USE_MULTILANG_CONFIG, condition = false, activeByDefault = true)
public final class MessageConfig implements ISingleConfigExtension {

    private final MessageManager messageManager;

    public MessageConfig(final BasePlugin<?> plugin) {
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

    final void loadMessages(final Configuration configuration, final String language, final MessageProvider[] providers) {
        for (final MessageProvider provider : providers) {
            IMessage message = provider.getMessage(language);
            if (!(message instanceof ISimpleMessage)) {
                continue;
            }
            ((ISimpleMessage) message).setTranslation(configuration.get(provider.getId(), String.class));
        }
    }

    final void saveMessages(final Configuration configuration, final String language, final MessageProvider[] providers) {
        IMessage message;
        for (final MessageProvider provider : providers) {
            message = provider.getMessage(language);
            if (message == null) {
                if (provider instanceof ISimpleFallback) {
                    configuration.set(provider.getId(), ((ISimpleFallback) provider).fallback());
                }
                continue;
            }
            configuration.set(message.id(), message.value());
        }
    }

}
