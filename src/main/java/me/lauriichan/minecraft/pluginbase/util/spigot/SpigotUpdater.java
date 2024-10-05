package me.lauriichan.minecraft.pluginbase.util.spigot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.laylib.json.io.JsonParser;
import me.lauriichan.laylib.json.io.JsonWriter;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.laylib.logger.ISimpleLogger;

public final class SpigotUpdater<V extends Comparable<V>> {

    public static class SpigotUpdaterException extends Exception {

        private static final long serialVersionUID = -1486270645493382143L;

        public SpigotUpdaterException(String message) {
            super(message);
        }

        public SpigotUpdaterException(Throwable cause) {
            super(cause);
        }

    }

    private static final JsonWriter WRITER = new JsonWriter().setPretty(false);

    private static final String API_URL = "https://api.spigotmc.org/simple/0.2/index.php";
    private static final String DOWNLOAD_URL_FORMAT = "https://www.spigotmc.org/resources/%s/download?version=%s";

    private static String buildUrl(Key... params) {
        if (params.length == 0) {
            throw new IllegalStateException();
        }
        StringBuilder builder = new StringBuilder(API_URL).append('?');
        for (int i = 0; i < params.length; i++) {
            if (i != 0) {
                builder.append('&');
            }
            builder.append(params[i].getKey()).append('=').append(params[i].getValueOrDefault("").toString());
        }
        return builder.toString();
    }

    public static record ResourceInfo<V>(int resourceId, String title, String tag, V version, int authorId, String authorName,
        int downloadCount, int updateCount) {}

    private final int resourceId;
    private final V version;

    private final Function<String, V> versionParser;

    private volatile ResourceInfo<V> latest;

    public SpigotUpdater(int resourceId, String version, Function<String, V> versionParser) {
        this.resourceId = resourceId;
        this.version = versionParser.apply(version);
        this.versionParser = versionParser;
    }

    public boolean isUp2Date() throws SpigotUpdaterException {
        return version.compareTo(getLatestVersion()) >= 0;
    }

    public void downloadLatestUpdate(ISimpleLogger logger, File jarFile) throws SpigotUpdaterException {
        if (jarFile == null || !jarFile.getName().endsWith(".jar")) {
            throw new SpigotUpdaterException("Jar file required");
        }
        updateInfo();
        JsonArray array = performGetRequest(buildUrl(Key.of("action", "getResourceUpdates"), Key.of("id", resourceId),
            Key.of("page", (int) Math.ceil(latest.updateCount() / 10d)))).asJsonArray();
        JsonObject object = array.get(array.size() - 1).asJsonObject();
        int updateId = object.getAsInt("id");
        downloadUpdate(jarFile, updateId);
    }

    public void downloadUpdate(File jarFile, String version) throws SpigotUpdaterException {
        downloadUpdate(jarFile, versionParser.apply(version));
    }

    public void downloadUpdate(File jarFile, V version) throws SpigotUpdaterException {
        if (jarFile == null || !jarFile.getName().endsWith(".jar")) {
            throw new SpigotUpdaterException("Jar file required");
        }
        updateInfo();
        int pageCount = (int) Math.ceil(latest.updateCount() / 10d);
        for (int page = 1; page <= pageCount; page++) {
            JsonArray array = performGetRequest(
                buildUrl(Key.of("action", "getResourceUpdates"), Key.of("id", resourceId), Key.of("page", page))).asJsonArray();
            for (IJson<?> entry : array) {
                JsonObject object = entry.asJsonObject();
                V otherVersion = versionParser.apply(object.getAsString("resource_version"));
                if (otherVersion.compareTo(version) == 0) {
                    downloadUpdate(jarFile, object.getAsInt("id"));
                    return;
                }
            }
        }
        throw new SpigotUpdaterException("Couldn't find download url for version '" + version + "'");
    }

    private void downloadUpdate(File jarFile, int updateId) throws SpigotUpdaterException {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(DOWNLOAD_URL_FORMAT.formatted(resourceId, updateId))
                .openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(false);
            connection.setDoInput(true);

            connection.setConnectTimeout(7500);
            connection.setReadTimeout(12500);

            connection.connect();

            int code = connection.getResponseCode();
            if (code != 200) {
                try (InputStream errorStream = stream(connection)) {
                    throw new SpigotUpdaterException(
                        "Received response with code " + code + ": " + new String(errorStream.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
            if (jarFile.exists()) {
                jarFile.delete();
            } else if (!jarFile.getParentFile().exists()) {
                jarFile.getParentFile().mkdirs();
            }
            try (FileOutputStream outputStream = new FileOutputStream(jarFile)) {
                connection.getInputStream().transferTo(outputStream);
            } finally {
                connection.disconnect();
            }
        } catch (SpigotUpdaterException exp) {
            throw exp;
        } catch (Throwable throwable) {
            throw new SpigotUpdaterException(throwable);
        }
    }

    public V getVersion() {
        return version;
    }

    public V getLatestVersion() throws SpigotUpdaterException {
        return getLatestInfo().version();
    }

    public ResourceInfo<V> getLatestInfo() throws SpigotUpdaterException {
        if (latest == null) {
            updateInfo();
        }
        return latest;
    }

    public void updateInfo() throws SpigotUpdaterException {
        JsonObject object = performGetRequest(buildUrl(Key.of("action", "getResource"), Key.of("id", resourceId))).asJsonObject();
        int resourceId = object.getAsInt("id");
        String title = object.getAsString("title");
        String tag = object.getAsString("tag");
        V version = versionParser.apply(object.getAsString("current_version"));
        JsonObject author = object.getAsObject("author");
        int authorId = author.getAsInt("id");
        String authorName = author.getAsString("username");
        JsonObject stats = object.getAsObject("stats");
        int downloadCount = stats.getAsInt("downloads");
        int updateCount = stats.getAsInt("updates");
        latest = new ResourceInfo<>(resourceId, title, tag, version, authorId, authorName, downloadCount, updateCount);
    }

    private IJson<?> performGetRequest(String url) throws SpigotUpdaterException {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(false);
            connection.setDoInput(true);

            connection.setConnectTimeout(7500);
            connection.setReadTimeout(12500);

            connection.connect();

            int code = connection.getResponseCode();
            if (code != 200) {
                try (InputStream errorStream = stream(connection)) {
                    throw new SpigotUpdaterException(
                        "Received response with code " + code + ": " + WRITER.toString(JsonParser.fromStream(errorStream)));
                }
            }
            try {
                return JsonParser.fromStream(connection.getInputStream());
            } finally {
                connection.disconnect();
            }
        } catch (SpigotUpdaterException exp) {
            throw exp;
        } catch (Throwable throwable) {
            throw new SpigotUpdaterException(throwable);
        }
    }

    private InputStream stream(HttpURLConnection connection) {
        try {
            return connection.getInputStream();
        } catch (IOException exp) {
            return connection.getErrorStream();
        }
    }

}
