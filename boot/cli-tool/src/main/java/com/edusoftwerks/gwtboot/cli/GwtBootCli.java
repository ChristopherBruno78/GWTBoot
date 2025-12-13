package com.edusoftwerks.gwtboot.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(
    name = "gwt-boot",
    version = "1.0.0",
    description = "GWT Boot CLI - Unified command-line tool for GWT Boot projects",
    mixinStandardHelpOptions = true,
    subcommands = {
        BootCommand.class,
        ActivityCommand.class,
        ServiceCommand.class
    }
)
public class GwtBootCli implements Callable<Integer> {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GwtBootCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        Console.info("GWT Boot CLI - Version 1.0.0");
        Console.info("");
        Console.info("Usage: gwt-boot <command> [options]");
        Console.info("");
        Console.info("Commands:");
        Console.info("  boot <artifactId>    Generate a new GWT Boot project from archetype");
        Console.info("  activity <name>      Create a new activity in the current project");
        Console.info("  service <name>       Create a new GWT RPC service in the current project");
        Console.info("  help                 Show this help message");
        Console.info("  version              Show version information");
        Console.info("");
        Console.info("Examples:");
        Console.info("  gwt-boot boot myapp");
        Console.info("  gwt-boot activity dashboard");
        Console.info("  gwt-boot service UserAuth");
        Console.info("");
        return 0;
    }
}
