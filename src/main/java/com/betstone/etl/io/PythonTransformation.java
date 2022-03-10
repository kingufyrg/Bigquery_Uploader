package com.betstone.etl.io;

import com.betstone.etl.ScorecardHandler;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PythonTransformation {

    public void execute() {
        String report = "ETL_daemon.py";
        ScorecardHandler.LOGGER.info("Iniciando Transformación de datos");
        ScorecardHandler.LOGGER.info("ETL Python");
        commandPython(report);
        ScorecardHandler.LOGGER.info("Proceso de tranformación terminado");
    }

    private boolean commandPython(String script) {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            //script = "ETL_daemon.py";
            //builder.command("cmd.exe", "/c", "python " + script, "exit()");
            ScorecardHandler.LOGGER.info(script);
            builder.command("bash", "-c", "python3 " + script, "exit()");
                    File scriptsDirectory = new File(IOUtils.getPropertiesValue("python.scripts"));
            ScorecardHandler.LOGGER.info("Definir directorio");
                    builder.directory(scriptsDirectory);
            ScorecardHandler.LOGGER.info("Definir archivo");
            Process process = builder.start();
            ScorecardHandler.LOGGER.info("Corriendo archivo");
            StreamGlobber streamGlobber =
                    new StreamGlobber(process.getInputStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamGlobber);
            boolean exitCode = process.waitFor(10, TimeUnit.MINUTES);
            return true;
        } catch (IOException e) {
            ScorecardHandler.LOGGER.fatal("Error de I/O");
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
