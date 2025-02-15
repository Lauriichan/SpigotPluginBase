package me.lauriichan.minecraft.pluginbase.resource.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class PathDataSource implements IDataSource {

    private final Path path;

    public PathDataSource(final Path path) {
        this.path = path;
    }

    @Override
    public boolean exists() {
        return Files.exists(path);
    }

    @Override
    public boolean isResource() {
        return !Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
    }

    @Override
    public Path getSource() {
        return path;
    }
    
    @Override
    public String getPath() {
        return path.toAbsolutePath().toString();
    }

    @Override
    public long lastModified() {
        try {
            return Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis();
        } catch (final IOException e) {
            return -1L;
        }
    }

    @Override
    public boolean isWritable() {
        return Files.isWritable(path);
    }

    @Override
    public OutputStream openWritableStream() throws IOException {
        if (!isWritable()) {
            throw new UnsupportedOperationException("Path can not be written to");
        }
        ensureCreated();
        return path.getFileSystem().provider().newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    @Override
    public boolean isReadable() {
        return Files.isReadable(path);
    }

    @Override
    public InputStream openReadableStream() throws IOException {
        return path.getFileSystem().provider().newInputStream(path, StandardOpenOption.READ);
    }
    
    private void ensureCreated() throws IOException {
        if (!Files.exists(path)) {
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
        }
    }

}