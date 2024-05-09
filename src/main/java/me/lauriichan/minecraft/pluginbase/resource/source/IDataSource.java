package me.lauriichan.minecraft.pluginbase.resource.source;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface IDataSource {

    /**
     * Check if the source exists
     * 
     * @return if the source exists or not
     */
    boolean exists();

    /**
     * Checks if the source is a resource
     * 
     * @return if the source is a resource
     */
    default boolean isResource() {
        return false;
    }

    /**
     * Get the source object
     * 
     * @return the source object
     */
    Object getSource();

    /**
     * Gets the time that the source was last modified at
     * 
     * @return the time in ms
     */
    default long lastModified() {
        return -1L;
    }

    /**
     * Checks if the data source can be written to
     * 
     * @return @{code true} if the source can be written to otherwise @{code false}
     */
    default boolean isWritable() {
        return false;
    }

    /**
     * Open a writable stream for the source
     * 
     * @return             the output stream
     * 
     * @throws IOException if an I/O error occurs
     */
    default OutputStream openWritableStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Open a buffered writer for the source
     * 
     * @return             the buffered writer
     * 
     * @throws IOException if an I/O error occurs
     */
    default BufferedWriter openWriter() throws IOException {
        return openWriter(StandardCharsets.UTF_8);
    }

    /**
     * Open a buffered writer for the source
     * 
     * @param  charset     the charset to use
     * 
     * @return             the buffered writer
     * 
     * @throws IOException if an I/O error occurs
     */
    default BufferedWriter openWriter(Charset charset) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(openWritableStream(), charset));
    }

    /**
     * Checks if the data source can be read from
     * 
     * @return @{code true} if the source can be read from otherwise @{code false}
     */
    default boolean isReadable() {
        return false;
    }

    /**
     * Open a readable stream for the source
     * 
     * @return             the input stream
     * 
     * @throws IOException if an I/O error occurs
     */
    default InputStream openReadableStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Open a buffered reader for the source
     * 
     * @return             the buffered reader
     * 
     * @throws IOException if an I/O error occurs
     */
    default BufferedReader openReader() throws IOException {
        return openReader(StandardCharsets.UTF_8);
    }

    /**
     * Open a buffered reader for the source
     * 
     * @param  charset     the charset to use
     * 
     * @return             the buffered reader
     * 
     * @throws IOException if an I/O error occurs
     */
    default BufferedReader openReader(Charset charset) throws IOException {
        return new BufferedReader(new InputStreamReader(openReadableStream(), charset));
    }

}