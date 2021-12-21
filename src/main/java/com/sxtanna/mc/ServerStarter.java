package com.sxtanna.mc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.sxtanna.mc.conf.ServerStarterConf;
import com.sxtanna.mc.data.Size;
import com.sxtanna.mc.data.Text;
import com.sxtanna.mc.data.Type;
import com.sxtanna.mc.data.Vers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

final class ServerStarter
{

    private static final String GENERIC_STOP_COMMAND = "server-starter-generic-stop";

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

        final var mitigation = applyExploitMitigation(this.path, this.type, this.vers);
        if (mitigation != null)
        {
            args.add(mitigation);

            System.out.println(" ___                                 _             \n" +
                               "(_      /  '_/  /|/| '_/'_ __/'     /_|     /'_ _/ \n" +
                               "/__)(/)(()/ /  /   |/ //(/(///()/) (  |/)/)(/(-(/  \n" +
                               "    /                  _/             / /          \n");
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

        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
               .redirectError(ProcessBuilder.Redirect.INHERIT);

        final var process = builder.start();

        Runtime.getRuntime().addShutdownHook(new Thread(process::destroyForcibly));

        final var redirect = awaitGenericStop(type, process);

        // await process end
        final var code = process.waitFor();

        redirect.cancel(true);

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

    private static @NotNull CompletableFuture<Void> awaitGenericStop(@NotNull final Type type, @NotNull final Process process)
    {
        final var reader = new BufferedReader(new InputStreamReader(System.in));
        final var writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        final var future = new CompletableFuture<Void>();
        final var thread = new Thread(() ->
                                      {
                                          // This makes perfect sense, and I will die on this hill.

                                          // trw over reader and writer
                                          try (reader; writer)
                                          {
                                              // while the future hasn't been completed
                                              while (!future.isDone())
                                              {
                                                  final String input;

                                                  // wait until the reader is ready, or the future has been completed
                                                  while (!reader.ready() && !future.isDone())
                                                  {
                                                      Thread.onSpinWait(); // this is a workaround to prevent the blocking of this thread by #readLine
                                                  }


                                                  // if the future is completed, do nothing
                                                  if (future.isDone())
                                                  {
                                                      break;
                                                  }

                                                  // read input
                                                  input = reader.readLine();


                                                  final String pass;

                                                  if (!GENERIC_STOP_COMMAND.equals(input))
                                                  {
                                                      pass = input;
                                                  }
                                                  else
                                                  {
                                                      if (!type.canGracefulClose())
                                                      {
                                                          process.destroy();
                                                          break;
                                                      }
                                                      else
                                                      {
                                                          pass = type.getStop();
                                                      }
                                                  }

                                                  if (pass == null)
                                                  {
                                                      continue;
                                                  }

                                                  // write input to the process
                                                  writer.write(pass);
                                                  writer.newLine();
                                                  writer.flush();
                                              }
                                          }
                                          catch (final IOException ignored)
                                          {
                                          }
                                      }, "generic-stop");

        thread.start();
        future.whenComplete(($0, $1) -> thread.interrupt());

        return future;
    }


    private static @Nullable String applyExploitMitigation(@NotNull final Path path, @NotNull final Type type, @NotNull final Vers vers)
    {
        String      args = null;
        InputStream save = null;

        try
        {
            if (vers.compareTo(Vers.V1_11_2) <= 0)
            {
                args = "-Dlog4j.configurationFile=log4j2_17-111.xml";
                save = ServerStarterMain.class.getResourceAsStream("log4j2_17-111.xml");
            }
            else if (vers.compareTo(Vers.V1_16_5) <= 0)
            {
                args = "-Dlog4j.configurationFile=log4j2_112-116.xml";
                save = ServerStarterMain.class.getResourceAsStream("log4j2_112-116.xml");
            }
            else if (vers.compareTo(Vers.V1_17_1) <= 0)
            {
                args = "-Dlog4j2.formatMsgNoLookups=true";
            }
        }
        catch (final Throwable ignored)
        {
            return null;
        }

        if (save != null)
        {
            try
            {
                Files.copy(save, path.resolve(args.substring(args.indexOf('='))), StandardCopyOption.REPLACE_EXISTING);
            }
            catch (final IOException ex)
            {
                System.out.println("Failed to save log4j configuration file");
                ex.printStackTrace();
            }
        }

        return args;
    }

}
