package eu.slipo.workbench.web.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

// TODO: Check if there is already a library with the same functionality

public class CsvUtils {

    public static String detectDelimiter(Path path) throws FileNotFoundException, IOException {
        try (
            FileInputStream fis = new FileInputStream(path.toString());
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
        ) {
            int index = 0;
            Set<Character> delimiter = new HashSet<Character>();

            Map<Character, Integer> prev = null;
            Map<Character, Integer> curr = null;

            while (br.ready() && index < 100) {
                String line = br.readLine();

                // Count character frequency for the current line
                curr = new HashMap<Character, Integer>();

                for (char c : line.toCharArray()) {
                    curr.put(c, curr.getOrDefault(c, 0) + 1);
                    if (index == 0) {
                        delimiter.add(c);
                    }
                }
                // Compare to the previous line
                if (index != 0) {
                    for (Character c : curr.keySet()) {
                        if (!curr.get(c).equals(prev.getOrDefault(c, 0))) {
                            delimiter.remove(c);
                        }
                    }
                    delimiter.retainAll(curr.keySet());
                }
                // Check if a delimiter is already found
                if ((index != 0) && (delimiter.size() == 1)) {
                    return String.valueOf(delimiter.stream().findFirst().orElse(';'));
                }

                prev = curr;
                index++;
            }
        }

        return ";";
    }

    public static String detectQuote(Path path, char delimiter) throws FileNotFoundException, IOException {
        try (
            FileInputStream fis = new FileInputStream(path.toString());
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
        ) {
            int index = 0;

            Set<Character> quote = new HashSet<Character>();
            Set<Character> curr = null;

            while (br.ready() && index < 100) {
                String line = br.readLine();
                String[] words = StringUtils.split(line, Character.toString(delimiter));

                // Get quotes for the current line
                curr = new HashSet<Character>();

                for (String w : words) {
                    if ((w != null) && (w.length() > 1) && (w.charAt(0) == w.charAt(w.length() - 1))) {
                        curr.add(w.charAt(0));
                        if (index == 0) {
                            quote.add(w.charAt(0));
                        }
                    }
                }

                // Compare to the previous line
                quote.retainAll(curr);

                // Check if a delimiter is already found
                if ((index != 0) && (quote.size() == 1)) {
                    return quote.stream().findFirst().orElse(';').toString();
                }

                index++;
            }
        }

        return null;
    }

}
