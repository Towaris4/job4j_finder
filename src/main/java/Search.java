package ru.job4j.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Search {
    public static void main(String[] args) throws IOException {
        Predicate<Path> regex = path -> path.toString().matches(".*\\.class$");
        String globPattern = "glob:**.class";
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(globPattern);
        Predicate<Path> mask = matcher::matches;
        Predicate<Path> fullName = path -> path.toString().endsWith("\\fileName");
        Path directory = Paths.get("data");
        Files.createDirectories(directory);
        Path start = Paths.get(".");
        try (FileOutputStream output = new FileOutputStream("data/dataResult1.txt")) {
            for (Path path : search(start, regex)) {
                output.write(path.toString().getBytes());
                output.write(System.lineSeparator().getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Path> search(Path root, Predicate<Path> condition) throws IOException {
        ru.job4j.io.SearchFiles searcher = new ru.job4j.io.SearchFiles(condition);
        Files.walkFileTree(root, searcher);
        return searcher.getPaths();
    }
    /*private static void validateArgs(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Root folder is null. Usage  ROOT_FOLDER.");
        }
        if (args.length !=  2) {
            throw new IllegalArgumentException("invalid number of parameters");
        }
        File file = new File(args[0]);
        if (!file.isDirectory()) {
            throw new IllegalArgumentException(String.format("Not directory %s", args[0]));
        }
        if (!args[1].startsWith(".")) {
            throw new IllegalArgumentException(String.format("Not expansion %s", args[1]));
        }
    }*/
}