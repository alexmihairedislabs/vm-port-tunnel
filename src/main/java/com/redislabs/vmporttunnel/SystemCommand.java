package com.redislabs.vmporttunnel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class SystemCommand {

    public static final Logger log = LoggerFactory.getLogger(SystemCommand.class);

    @Getter
    @AllArgsConstructor
    public static class SystemCommandResult {
        private int returnCode;
        private List<String> output;
    }

    public static SystemCommandResult run(String command) throws IOException, InterruptedException {
        return new SystemCommand(command).run();
    }

    private final String command;

    public SystemCommandResult run() throws IOException, InterruptedException {
        log.info("Running command: {}", command);

        Process p = Runtime.getRuntime().exec(command);

        log.info("Output:");
        BufferedReader outputReader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        List<String> output = new ArrayList<>();
        String s;
        while ((s = outputReader.readLine()) != null) {
            log.info(s);
            output.add(s);
        }

        return new SystemCommandResult(p.waitFor(), output);
    }
}
