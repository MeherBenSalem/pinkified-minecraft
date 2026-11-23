package com.sugarplumstudios.pinkified.client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A small TOML reader and writer for the client config. It writes the same layout Forge's config
 * system used ({@code [section]}, tab-indented {@code key = value} lines with {@code #} comments)
 * and reads any file in that layout, so the file is shared by every loader. Pure Java.
 */
public final class ConfigFile {
    private static final Logger LOGGER = LoggerFactory.getLogger("pinkified");

    public record Section(String name, String comment) {
    }

    /** One typed setting. Before the file is read it reports its default. */
    public abstract static class Value<T> {
        final String section;
        final String key;
        final String comment;
        final T def;
        private volatile T value;

        Value(String section, String key, String comment, T def) {
            this.section = section;
            this.key = key;
            this.comment = comment;
            this.def = def;
            this.value = def;
        }

        public T get() {
            return value;
        }

        public T getDefault() {
            return def;
        }

        public void set(T newValue) {
            if (newValue != null) value = clamp(newValue);
        }

        public void reset() {
            value = def;
        }

        T clamp(T v) {
            return v;
        }

        /** Parses a raw value (quotes already removed); null when it cannot be read. */
        abstract T parse(String raw);

        abstract String write(T v);

        List<String> extraComments() {
            return List.of();
        }
    }

    public static final class BoolValue extends Value<Boolean> {
        public BoolValue(String section, String key, String comment, boolean def) {
            super(section, key, comment, def);
        }

        @Override
        Boolean parse(String raw) {
            if (raw.equalsIgnoreCase("true")) return Boolean.TRUE;
            if (raw.equalsIgnoreCase("false")) return Boolean.FALSE;
            return null;
        }

        @Override
        String write(Boolean v) {
            return v.toString();
        }
    }

    public static final class StringValue extends Value<String> {
        public StringValue(String section, String key, String comment, String def) {
            super(section, key, comment, def);
        }

        @Override
        String parse(String raw) {
            return raw;
        }

        @Override
        String write(String v) {
            return quote(v);
        }
    }

    public static final class DoubleValue extends Value<Double> {
        private final double min;
        private final double max;

        public DoubleValue(String section, String key, String comment, double def, double min, double max) {
            super(section, key, comment, def);
            this.min = min;
            this.max = max;
        }

        @Override
        Double clamp(Double v) {
            if (v.isNaN()) return def;
            return Math.max(min, Math.min(max, v));
        }

        @Override
        Double parse(String raw) {
            try {
                return Double.parseDouble(raw);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        String write(Double v) {
            return Double.toString(v);
        }

        @Override
        List<String> extraComments() {
            return List.of("Range: " + min + " ~ " + max);
        }
    }

    public static final class EnumValue<E extends Enum<E>> extends Value<E> {
        private final Class<E> type;

        public EnumValue(String section, String key, String comment, E def) {
            super(section, key, comment, def);
            this.type = def.getDeclaringClass();
        }

        @Override
        E parse(String raw) {
            for (E e : type.getEnumConstants()) {
                if (e.name().equalsIgnoreCase(raw.trim())) return e;
            }
            return null;
        }

        @Override
        String write(E v) {
            return quote(v.name());
        }

        @Override
        List<String> extraComments() {
            StringBuilder sb = new StringBuilder("Allowed Values: ");
            E[] all = type.getEnumConstants();
            for (int i = 0; i < all.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(all[i].name());
            }
            return List.of(sb.toString());
        }
    }

    private final List<Section> sections;
    private final List<Value<?>> values;
    private Path path;
    private long lastModified = -1;

    public ConfigFile(List<Section> sections, List<Value<?>> values) {
        this.sections = sections;
        this.values = values;
    }

    /** Reads the file at {@code file}; a missing or incomplete file is (re)written with every value. */
    public synchronized void load(Path file) {
        this.path = file;
        boolean complete = read();
        if (!complete) save();
        else lastModified = modified();
    }

    /** Reads the file again when its modification time changed since it was last read or written. */
    public synchronized boolean reloadIfChanged() {
        if (path == null) return false;
        long now = modified();
        if (now == lastModified) return false;
        read();
        lastModified = now;
        return true;
    }

    public synchronized void save() {
        if (path == null) return;
        try {
            Files.createDirectories(path.getParent());
            Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
            Files.write(tmp, render().getBytes(StandardCharsets.UTF_8));
            try {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicNotSupported) {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
            }
            lastModified = modified();
        } catch (IOException e) {
            LOGGER.warn("Pinkified could not write {}", path, e);
        }
    }

    private long modified() {
        try {
            return Files.exists(path) ? Files.getLastModifiedTime(path).toMillis() : -1;
        } catch (IOException e) {
            return -1;
        }
    }

    /** Applies the file's values; returns true when every value was present and readable. */
    private boolean read() {
        if (!Files.exists(path)) return false;
        Map<String, String> raw;
        try {
            raw = parse(Files.readAllLines(path, StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Pinkified could not read {}; using defaults", path, e);
            return false;
        }
        boolean complete = true;
        for (Value<?> v : values) complete &= apply(v, raw.get(v.section + "." + v.key));
        return complete;
    }

    private static <T> boolean apply(Value<T> v, String raw) {
        if (raw == null) return false;
        T parsed = v.parse(raw);
        if (parsed == null) {
            LOGGER.warn("Pinkified config: invalid value '{}' for {}.{}, using the default", raw, v.section, v.key);
            v.reset();
            return false;
        }
        v.set(parsed);
        return true;
    }

    /** Parses {@code [section]} headers and {@code key = value} lines into "section.key" -> raw value. */
    static Map<String, String> parse(List<String> lines) {
        Map<String, String> out = new HashMap<>();
        String section = "";
        for (String line : lines) {
            String s = line.strip();
            if (s.isEmpty() || s.startsWith("#")) continue;
            if (s.startsWith("[") && s.endsWith("]")) {
                section = s.substring(1, s.length() - 1).strip();
                continue;
            }
            int eq = s.indexOf('=');
            if (eq <= 0) continue;
            String key = s.substring(0, eq).strip();
            if (key.length() >= 2 && key.startsWith("\"") && key.endsWith("\"")) key = key.substring(1, key.length() - 1);
            out.put(section + "." + key, value(s.substring(eq + 1).strip()));
        }
        return out;
    }

    private static String value(String v) {
        if (v.startsWith("\"")) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < v.length(); i++) {
                char c = v.charAt(i);
                if (c == '\\' && i + 1 < v.length()) {
                    sb.append(v.charAt(++i));
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
        int hash = v.indexOf('#');
        return (hash >= 0 ? v.substring(0, hash) : v).strip();
    }

    static String quote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    String render() {
        StringBuilder sb = new StringBuilder();
        for (Section section : sections) {
            sb.append('#').append(section.comment()).append('\n');
            sb.append('[').append(section.name()).append("]\n");
            for (Value<?> v : values) {
                if (!v.section.equals(section.name())) continue;
                appendValue(sb, v);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private static <T> void appendValue(StringBuilder sb, Value<T> v) {
        sb.append("\t#").append(v.comment).append('\n');
        for (String extra : v.extraComments()) sb.append("\t#").append(extra).append('\n');
        sb.append('\t').append(v.key).append(" = ").append(v.write(v.get())).append('\n');
    }

    /** For tests and diagnostics: the names of every value as "section.key". */
    List<String> keys() {
        List<String> out = new ArrayList<>();
        for (Value<?> v : values) out.add((v.section + "." + v.key).toLowerCase(Locale.ROOT));
        return out;
    }
}
