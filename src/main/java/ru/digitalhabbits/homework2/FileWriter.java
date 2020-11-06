package ru.digitalhabbits.homework2;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Exchanger;

import static java.lang.Thread.currentThread;
import static org.slf4j.LoggerFactory.getLogger;

public class FileWriter implements Runnable {
    private static final Logger logger = getLogger(FileWriter.class);
    private String fileName;
    private Exchanger<String> exchanger;

    public FileWriter(String fileName, Exchanger<String> exchanger) {
        this.fileName = fileName;
        this.exchanger = exchanger;
    }

    @Override
    public void run() {
        logger.info("Started writer thread {}", currentThread().getName());
        File resFile = new File(fileName);
        java.io.FileWriter fileWriter = null;

        try {
            if (!resFile.exists())
                resFile.createNewFile();
            fileWriter = new java.io.FileWriter(resFile);
            while (!currentThread().isInterrupted()) {
                try {
                    fileWriter.write(exchanger.exchange(null) + "\n");
                }catch (InterruptedException e) {
                    break;
                }
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
             logger.error(e.getMessage());
    }
        logger.info("Finish writer thread {}", currentThread().getName());
    }

}


