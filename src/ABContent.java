//import java.io.*;
//import java.util.HashMap;
//import java.util.TreeSet;
//import java.util.function.BiConsumer;
//
//public class ABContent {
//
//    public static HashMap<String, TreeSet<String>> findAngleBracketsContent(String fileName) throws IOException {
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "utf-8"));
//        HashMap<String, TreeSet<String>> hashMap = new HashMap<>();
//        String temp = null;
//        while ((temp = bufferedReader.readLine()) != null) {
//            boolean isStart = false;
//            if (temp.startsWith("!start")) {
//                isStart = true;
//                bufferedReader.readLine();
//                temp = "<speechStart>:";
//            }
//            if (temp.startsWith("<") && temp.endsWith(":") || isStart) {
//                TreeSet<String> set = new TreeSet<>();
//                String content = null;
//                System.out.println(temp);
//                while ((content = bufferedReader.readLine()) != null && content.length() > 0) {
//                    boolean mark = true;
//                    for (char cc : content.toCharArray()) {
//                        if (cc != ' ') {
//                            mark = false;
//                        }
//                    }
//                    if (mark || content.length() < 1) {
//                        break;
//                    }
//                    content = content.replace("\"", "").replace("|", "").replace(";", "");
//                    if (set.contains(content)) {
//                        System.out.println(temp + "repeated" + content);
//                    }
//                    set.add(content);
//                }
//                hashMap.put(temp.substring(1, temp.length() - 2), set);
//            }
//        }
//        return hashMap;
//    }
//
//
//    public static void main(String[] args) throws IOException {
////        HashMap<String, TreeSet<String>> hashMap = findAngleBracketsContent(args[0]);
//////        hashMap.forEach((s, strings) -> {
//////            System.out.println(s + ":");
//////            strings.forEach(System.out::println);
//////        });
////        if (args.length > 1) {
////            TreeSet<String> set = hashMap.get(args[1]);
////            set.forEach(System.out::println);
////        }
//
//    }
//}
//
