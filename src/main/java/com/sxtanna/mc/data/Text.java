package com.sxtanna.mc.data;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Text
{

    @NotNull
    String JAR_NAME = "server_{t}.jar";


    @NotNull
    String MIN_MEMORY = "-Xms{s}M";
    @NotNull
    String MAX_MEMORY = "-Xmx{s}M";


    @NotNull
    List<String> ARGS = List.of(
            // gc configuration
            "-XX:+UseG1GC",
            "-XX:+ParallelRefProcEnabled",
            "-XX:MaxGCPauseMillis=200",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+DisableExplicitGC",
            "-XX:+AlwaysPreTouch",

            "-XX:G1HeapWastePercent=5",
            "-XX:G1MixedGCCountTarget=4",
            "-XX:G1MixedGCLiveThresholdPercent=90",
            "-XX:G1RSetUpdatingPauseTimePercent=5",

            "-XX:SurvivorRatio=32",
            "-XX:+PerfDisableSharedMem",
            "-XX:MaxTenuringThreshold=1",

            "-XX:G1NewSizePercent=30",
            "-XX:G1MaxNewSizePercent=40",
            "-XX:G1HeapRegionSize=8M",
            "-XX:G1ReservePercent=20",
            "-XX:InitiatingHeapOccupancyPercent=15",

            // specifics
            "-Dfile.encoding=UTF-8",

            "-Daikars.new.flags=true",
            "-Dusing.aikars.flags=https://mcflags.emc.gs",

            "-Dcom.mojang.eula.agree=true",
            "-Dpaper.debug-sync-loads=true");

    @NotNull
    List<String> OPEN_MODULES = List.of(
            "--add-opens",
            "java.base/java.net=ALL-UNNAMED",
            "--add-opens",
            "java.base/java.lang.invoke=ALL-UNNAMED");

    @NotNull
    List<String> ANSI_CONSOLE = List.of(
            "-Dterminal.jline=false",
            "-Dterminal.ansi=true");

}
