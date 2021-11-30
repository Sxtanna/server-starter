package com.sxtanna.mc;

import org.jetbrains.annotations.NotNull;

import com.sxtanna.mc.conf.ServerStarterConf;
import com.sxtanna.mc.data.Size;
import com.sxtanna.mc.data.Text;
import com.sxtanna.mc.data.Type;
import com.sxtanna.mc.data.Vers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;

final class ServerStarter
{

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                                                                        .withLocale(Locale.getDefault())
                                                                        .withZone(ZoneId.systemDefault());


    @NotNull
    private final Type              type;
    @NotNull
    private final Path              path;
    @NotNull
    private final Size              size;
    @NotNull
    private final Vers              vers;
    @NotNull
    private final ServerStarterConf conf;


    ServerStarter(@NotNull final Type type, @NotNull final Path path, @NotNull final Size size, @NotNull final Vers vers, @NotNull final ServerStarterConf conf)
    {
        this.type = type;
        this.path = path;
        this.size = size;
        this.vers = vers;
        this.conf = conf;

        System.out.printf("Starting a %s, %s %s server in '%s'%n", size, type, vers, path);
    }


    public void saveServerJar() throws IOException
    {
        final var path = this.path.resolve(Text.JAR_NAME.replace("{t}", this.type.getName().toLowerCase(Locale.ROOT))).toAbsolutePath();
        System.out.printf("Resolving Server Jar at %s%n", path.toFile());


        if (Files.notExists(path))
        {
            System.out.println("Server Jar not found, creating directories...");

            Files.createDirectories(path.getParent());
        }
        else
        {
            final var attr = Files.readAttributes(path, BasicFileAttributes.class);

            final var create = attr.creationTime().toInstant();
            final var modify = attr.lastModifiedTime().toInstant();

            final var latest = modify.isAfter(create) ? modify : create;

            System.out.printf("Server Jar was last downloaded [%s]%n", formatter.format(latest));

            if (latest.isAfter(Instant.now().minus(7, ChronoUnit.DAYS)))
            {
                System.out.println("Skipping download...");
                return;
            }
        }


        final var latestVersion = this.type.getLatestVersion(this.vers);
        System.out.printf("Latest version of %s is %s%n", this.vers.name(), latestVersion);

        final var versionJarURL = this.type.getVersionJarURL(this.vers, latestVersion.orElseThrow());
        System.out.printf("Latest jarfile of %s is %s%n", this.vers.name(), versionJarURL);


        System.out.printf("Downloading Server Jar to %s%n", path.toFile());

        try (final var into = new FileOutputStream(path.toFile()))
        {
            try (final var from = new URL(versionJarURL.orElseThrow()).openStream())
            {
                into.getChannel().transferFrom(Channels.newChannel(from), 0L, Long.MAX_VALUE);
            }
        }
    }

    public void execServerJar() throws IOException, InterruptedException
    {
        final var builder = new ProcessBuilder();

        final var args = new ArrayList<String>();

        args.add("java");

        if (this.vers.compareTo(Vers.V1_17_1) >= 0)
        {
            args.addAll(Text.OPEN_MODULES);
        }

        if (isRunningInUnsupportedConsole())
        {
            args.addAll(Text.ANSI_CONSOLE);
        }

        args.add(Text.MIN_MEMORY.replace("{s}", String.valueOf(this.size.getMinMemory())));
        args.add(Text.MAX_MEMORY.replace("{s}", String.valueOf(this.size.getMaxMemory())));

        if (this.type == Type.BUNGEE)
        {
            args.addAll(Text.ARGS_PROXIES);
        }
        else
        {
            args.addAll(Text.ARGS_SERVERS);
        }

        args.add("-jar");
        args.add(Text.JAR_NAME.replace("{t}", this.type.getName().toLowerCase(Locale.ROOT)));

        if (!conf.conf().hasPath("no-gui"))
        {
            args.add("-nogui");
        }
        else
        {
            args.add(conf.conf().getString("no-gui"));
        }


        builder.command(args);
        builder.directory(path.toFile());

        builder.inheritIO();

        final var process = builder.start();

        Runtime.getRuntime().addShutdownHook(new Thread(process::destroyForcibly));

        // await process end
        final var code = process.waitFor();
        System.out.println("Server exited with code " + code + "...");
    }


    private static boolean isRunningInUnsupportedConsole()
    {
        try
        {
            return ManagementFactory.getRuntimeMXBean()
                                    .getInputArguments()
                                    .stream()
                                    .anyMatch(it -> it.contains("idea_rt.jar") || Text.ANSI_CONSOLE.stream().anyMatch(it::contains));
        }
        catch (final Throwable ex)
        {
            return false;
        }
    }

}
