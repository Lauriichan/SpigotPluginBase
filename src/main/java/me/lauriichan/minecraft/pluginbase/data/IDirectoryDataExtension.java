package me.lauriichan.minecraft.pluginbase.data;

import java.io.File;
import java.util.function.BiFunction;

import org.bukkit.NamespacedKey;

import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.data.IDataHandler.Wrapper;
import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;

@ExtensionPoint
public interface IDirectoryDataExtension<T> extends IDataExtension<T> {

    public static final record FileKey(NamespacedKey location, String extension) {

        public String filePath() {
            if (extension == null) {
                return location.getKey();
            }
            return location.getKey() + '.' + extension;
        }
    }

    public static final class FileData<T> extends Wrapper<T> {

        private final File file;
        private final FileKey key;
        private volatile boolean delete = false;

        public FileData(File file, FileKey key) {
            this.file = file;
            this.key = key;
        }

        public File file() {
            return file;
        }

        public FileKey key() {
            return key;
        }

        public void delete() {
            delete = true;
        }

        public boolean shouldBeDeleted() {
            return delete;
        }

    }

    abstract String path();

    abstract void keyWrapper(BiFunction<String, String, FileKey> keyWrapper);

    abstract BiFunction<String, String, FileKey> keyWrapper();

    default FileKey keyOf(String path, String extension) {
        return keyWrapper().apply(path, extension);
    }

    default ObjectSet<FileKey> newData() {
        return ObjectSets.emptySet();
    }

    default void clearNewData() {}

    default boolean isSupported(File file, String name, String extension, boolean isFile) {
        return true;
    }

    default boolean saveNewDataAfterLoad() {
        return false;
    }

    default boolean removeNewDataOnReload() {
        return true;
    }

    default boolean saveKnownFiles() {
        return true;
    }

    default boolean searchSupportedDirectories() {
        return false;
    }

    default void onLoadStart(final ISimpleLogger logger) {}

    default void onLoad(final ISimpleLogger logger, final FileData<T> value) throws Exception {}

    default void onLoadEnd(final ISimpleLogger logger) {}

    default void onDeleteDone(final ISimpleLogger logger, final DirectoryDataWrapper<?, ?> wrapper) {}

    default void onDeleted(final ISimpleLogger logger, final FileKey key) {}

    default void onSaveStart(final ISimpleLogger logger) {}

    default void onSave(final ISimpleLogger logger, final FileData<T> value) throws Exception {}

    default void onSaveEnd(final ISimpleLogger logger) {}

}
