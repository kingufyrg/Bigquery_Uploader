package com.betstone.etl.process.ready.jars;

import com.betstone.etl.ScorecardHandler;
import com.betstone.etl.io.IOUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MacroExecuter {

    public boolean execute(ScorecardHandler scorecardHandler) {
        try {
            ScorecardHandler.LOGGER.info("Borrando archivos procesados repetidos...");
            IOUtils.deleteAllRepeatedExcelFiles(scorecardHandler);
            ScorecardHandler.LOGGER.info("Archivos procesados repetidos borrados.");
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("cmd.exe", "/c", "ConsoleApp2.exe");
            builder.directory(new File(Paths.get(
                    IOUtils.getPropertiesValue("excel.macro.exe")).toString()));
            Process process = builder.start();
            StreamGlobber streamGlobber =
                    new StreamGlobber(process.getInputStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamGlobber);
            int exitCode = process.waitFor();
            assert exitCode == 0;
            return true;
        } catch (IOException e) {
            ScorecardHandler.LOGGER.fatal("Error de I/O");
            ScorecardHandler.LOGGER.fatal(e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            ScorecardHandler.LOGGER.fatal("Error de Interrupción de Hilo");
            e.printStackTrace();
        }
        return false;
    }


    private static class StreamGlobber implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGlobber(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .forEach(consumer);
        }
    }
}
