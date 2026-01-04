package ru.job4j.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Search {
    static Map<String, String> arg = new HashMap<>();


    public static void main(String[] args) throws IOException {
        inputArgs(args);
        Path directory = Paths.get("data");
        Files.createDirectories(directory);
        Path start = Paths.get(arg.get("-d"));
        try (FileOutputStream output = new FileOutputStream("data/" + arg.get("-o"))) {
            for (Path path : search(start, findPredicate(arg.get("-t")))) {
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

    private static void inputArgs(String[] args) {
        if (args.length != 4) {
            throw new IllegalArgumentException("Неверное количество параметров");
        }
        for (int i = 0; args.length > i; i++) {
            String[] temp = args[i].split("=");
            if (temp.length != 2) {
                throw new IllegalArgumentException("Неверный параметр: " + args[i]);
            }
            arg.put(temp[0], temp[1]);
        }
        if (!arg.containsKey("-d")) {
            throw new IllegalArgumentException("Отсутвует директория в которой начинать поиск.");
        }
        if (!arg.containsKey("-n")) {
            throw new IllegalArgumentException("Отсутсвует имя файла, маска, либо регулярное выражение.");
        }
        if (!arg.containsKey("-t")) {
            throw new IllegalArgumentException("Отсутсвует тип поиска.");
        }
        if (!arg.containsKey("-o")) {
            throw new IllegalArgumentException("Отсутвует название файла, в который будет записан результат.");
        }
        String dirStr = arg.get("-d");
        if (dirStr == null || dirStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Параметр -d (директория поиска) не указан");
        }
        Path path = Paths.get(dirStr);
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Директория поиска задана некорректно: " + dirStr);
        }
        String fileMaskRegex = arg.get("-n");
        if (fileMaskRegex == null || fileMaskRegex.trim().isEmpty()) {
            throw new IllegalArgumentException("Параметр -n (имя файла, маска, либо регулярное выражение) не указан");
        }
        if (!(isValidRegex(fileMaskRegex) | isValidWildcardPattern(fileMaskRegex) | isValidFileName(fileMaskRegex))) {
            throw new IllegalArgumentException("Параметр -n (имя файла, маска, либо регулярное выражение) не корректен");
        }
        String typeSearch = arg.get("-t");
        if (typeSearch == null || typeSearch.trim().isEmpty()) {
            throw new IllegalArgumentException("Параметр -t тип поиска не указан");
        }
        if (!((typeSearch.equals("name") && isValidFileName(fileMaskRegex))
                | (typeSearch.equals("mask") && isValidWildcardPattern(fileMaskRegex))
                | (typeSearch.equals("regex") && isValidRegex(fileMaskRegex)))) {
            throw new IllegalArgumentException("Параметр -t типа поиска не соотвествует параметру -n.");
        }
        String outPutFileName = arg.get("-o");
        if (outPutFileName == null || outPutFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Параметр -0 (имя файла для вывода) не указан.");
        }
        if (!(isValidFileName(outPutFileName))) {
            throw new IllegalArgumentException("Параметр -0 (имя файла для вывода) не корректно.");
        }
    }

    public static Predicate<Path> findPredicate(String stringArgs) {
        if (stringArgs.equals("name")) {
            Predicate<Path> fullName = path -> path.toString().endsWith("\\" + arg.get("-n"));
            return fullName;
        }
        if (stringArgs.equals("mask")) {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + arg.get("-n"));
            Predicate<Path> mask = path -> Files.isRegularFile(path) && matcher.matches(path.getFileName());
            return mask;
        }
        Pattern pattern = Pattern.compile(arg.get("-n"));
        Predicate<Path> regex = path -> pattern.matcher(path.toString()).find();
        return regex;
    }

    public static boolean isValidRegex(String regex) {
        if (regex == null) {
            return false;
        }
        try {
            Pattern.compile(regex);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidWildcardPattern(String wildcard) {
        if (wildcard == null) {
            return false;
        }
        try {
            String regex = "^"
                    + wildcard
                    .replace("\\", "\\\\")
                    .replace(".", "\\.")
                    .replace("*", ".*")
                    .replace("?", ".")
                    + "$";
            Pattern.compile(regex);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }


    public static boolean isValidFileName(String fileName) {
        final String FILE_NAME_PATTERN = "^[\\w\\-]+\\.[A-Za-z]{1,10}$";
        final Pattern pattern = Pattern.compile(FILE_NAME_PATTERN);
        return pattern.matcher(fileName).matches();
    }
}