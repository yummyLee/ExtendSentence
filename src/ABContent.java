import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class ABContent {

    private HashMap<String, Set<String>> findAngleBracketsContent(String fileName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "utf-8"));
        HashMap<String, Set<String>> hashMap = new HashMap<>();
        String temp = null;
        while ((temp = bufferedReader.readLine()) != null) {
            if (temp.startsWith("<") && temp.endsWith(":")) {
                Set<String> set = new HashSet<>();
                String content = null;
                while ((content = bufferedReader.readLine()) != null && content.length() > 0) {
                    content = content.replace("\"", "").replace("|", "");
                    set.add(content);
                }
                hashMap.put(temp.substring(1, temp.length() - 2), set);
            }
        }
        return hashMap;
    }

    public static void main(String[] args) throws IOException {
        ABContent abContent = new ABContent();
        HashMap<String, Set<String>> hashMap = abContent.findAngleBracketsContent(args[0]);
//        hashMap.forEach((s, strings) -> {
//            System.out.println(s + ":");
//            strings.forEach(System.out::println);
//        });
        Set<String> set = hashMap.get(args[1]);
        if (set.contains(args[2])) {
            System.out.println("Exist");
        }
        set.forEach(System.out::println);
    }
}

