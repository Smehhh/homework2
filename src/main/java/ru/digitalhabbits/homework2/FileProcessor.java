package ru.digitalhabbits.homework2;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.Runtime.getRuntime;
import static java.nio.charset.Charset.defaultCharset;
import static org.slf4j.LoggerFactory.getLogger;

public class FileProcessor {
    private static final Logger logger = getLogger(FileProcessor.class);
    public static final int CHUNK_SIZE = 2 * getRuntime().availableProcessors();
    private final LineProcessor lineProcessor = new LineCounterProcessor();
    public void process(@Nonnull String processingFileName, @Nonnull String resultFileName) {
        checkFileExists(processingFileName);
        int counter = 0;
        final File file = new File(processingFileName);
        Exchanger<String> exchanger = new Exchanger<>();
        Thread fileWriterThread = new Thread(new FileWriter(resultFileName, exchanger));
        ExecutorService service = Executors.newFixedThreadPool(CHUNK_SIZE);
        fileWriterThread.start();

        try (final Scanner scanner = new Scanner(file, defaultCharset())) {
            while (scanner.hasNext()) {
                List<Future<Pair<String, Integer>>> futures = new ArrayList<>();
                while (counter < CHUNK_SIZE && scanner.hasNextLine()) {
                    String s = scanner.nextLine();
                    counter++;
                    futures.add(service.submit(() -> lineProcessor.process(s)));
                }
                for (Future<Pair<String, Integer>> r : futures) {
                    Pair<String, Integer> p = r.get();
                    exchanger.exchange(String.format("%s %s", p.getKey(), p.getValue()));
                }
                counter = 0;
            }
        } catch (Exception exception) {
            logger.error("", exception);
        }
        fileWriterThread.interrupt();
        service.shutdown();
        try {
            service.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("Finish main thread {}", Thread.currentThread().getName());
    }
    private void checkFileExists(@Nonnull String fileName) {
        final File file = new File(fileName);
        if (!file.exists() || file.isDirectory()) {
            throw new IllegalArgumentException("File '" + fileName + "' not exists");
        }
    }
}
