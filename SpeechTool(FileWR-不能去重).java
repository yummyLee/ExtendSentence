//import java.io.*;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.TreeSet;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class SpeechTool {
//
//    private static int slotLimitLength = 23;
//
//    private static ArrayList<String> angleBracketsCheck(String s) {
//        ArrayList<String> res = new ArrayList<>();
//        Pattern pattern = Pattern.compile("<\\w*?>");
//        Matcher matcher = pattern.matcher(s);
//        while (matcher.find()) {
//            res.add(matcher.group());
//        }
//        return res;
//    }
//
//    private static void checkFrequencyOfSlot(String fileName) throws IOException {
//
//        FileWriter fileWriter = new FileWriter(fileName.substring(fileName.lastIndexOf("\\") + 1) + "-statistics-report.txt", true);
//        fileWriter.append(fileName).append("\r\n");
//        if (!fileName.endsWith(".bnf")) {
//            fileWriter.append(fileName).append(" is not bnf file.").append("\r\n\r\n");
//            fileWriter.close();
//            return;
//        }
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
//        int lineCount = 0;
//        String temp = null;
//        HashMap<String, Integer> statistics = new HashMap<>();
//        while ((temp = bufferedReader.readLine()) != null) {
//            lineCount++;
//            if (temp.startsWith("!start")) {
//                bufferedReader.readLine();
//                lineCount++;
//                continue;
//            }
//            //System.out.println(temp);
//            if (temp.startsWith("!slot <") && temp.length() > slotLimitLength) {
//                temp = temp.substring(7, temp.length() - 2);
//                try {
//                    fileWriter.append(currentDateLog()).append("slot ").append(temp).append(" name length longer than 14 in line ").append(String.valueOf(lineCount)).append("\r\n");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            ArrayList<String> matchRes = angleBracketsCheck(temp);
//            matchRes.forEach((str) -> {
//                if (statistics.containsKey(str)) {
//                    statistics.put(str, statistics.get(str) + 1);
//                } else {
//                    statistics.put(str, 1);
//                }
//            });
//        }
//        bufferedReader.close();
//
//        statistics.forEach((str, count) -> {
//            if (count < 3) {
//                try {
//                    fileWriter.append(currentDateLog()).append("slot ").append(str).append(" appears less than 3 times").append("\r\n");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        fileWriter.append("\r\n\r\n");
//        fileWriter.close();
//    }
//
//    private static void handleDirBnf(String dirPath) throws IOException {
//        FileWriter fileWriter = new FileWriter(dirPath.substring(dirPath.lastIndexOf("\\") + 1) + "-dir-stat-report.txt", true);
//        File dir = new File(dirPath);
//        if (!dir.isDirectory()) {
//            fileWriter.append(dirPath).append(" is not directory.").append("\r\n\r\n");
//            fileWriter.close();
//            return;
//        }
//        File[] bnfFiles = dir.listFiles();
//        for (File bnf : bnfFiles != null ? bnfFiles : new File[0]) {
//            checkFrequencyOfSlot(bnf.getAbsolutePath());
//        }
//    }
//
//    private static String currentDateLog() {
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]: ");
//        return simpleDateFormat.format(new Date());
//    }
//
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
//                //System.out.println(temp);
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
//                        System.out.println(temp + "repeated " + content);
//                    }
//                    set.add(content);
//                }
//                hashMap.put(temp.substring(1, temp.length() - 2), set);
//            }
//        }
//        return hashMap;
//    }
//
//    private static void mergeBnfFiles(String dirPath, String targetFileName) throws IOException {
//
//        final String prefix = dirPath.substring(dirPath.lastIndexOf("\\") + 1);
//        final String startFileName = prefix + "-dir-start.txt";
//        final String logFileName = prefix + "-dir-merge-report.txt";
//        final String contentFileName = prefix + "-dir-content.txt";
//        final String slotFileName = prefix + "-dir-slot.txt";
//
//        FileWriter fileWriter = new FileWriter(logFileName, true);
//        File dir = new File(dirPath);
//        if (!dir.isDirectory()) {
//            fileWriter.append(currentDateLog()).append(dirPath).append(" is not directory.").append("\r\n\r\n");
//            fileWriter.close();
//            return;
//        }
//        FileWriter startWriter = new FileWriter(startFileName);
//        FileWriter contentWriter = new FileWriter(contentFileName);
//        FileWriter slotWriter = new FileWriter(slotFileName);
//        File[] bnfFiles = dir.listFiles();
//        HashMap<String,TreeSet<String>> mergeMap = new HashMap<>();
//        for (File bnf : bnfFiles != null ? bnfFiles : new File[0]) {
//            if (!bnf.getName().endsWith(".bnf")) {
//                fileWriter.append(currentDateLog()).append(bnf.getAbsolutePath()).append(" is not bnf file.").append("\r\n");
//                continue;
//            }
//            HashMap<String, TreeSet<String>> hashMap = findAngleBracketsContent(bnf.getAbsolutePath());
//            hashMap.forEach((s, set) -> {
//                if (s.equals("speechStart")) {
//                    set.forEach((speech) -> {
//                        try {
//                            startWriter.append(speech).append("\r\n");
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    });
//                } else {
//                    try {
//                        slotWriter.append(s).append("\r\n");
//                        contentWriter.append(s).append("\r\n");
//                        set.forEach((content) -> {
//                            try {
//                                contentWriter.append(content).append("\r\n");
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        });
//                        contentWriter.append("\r\n");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }
//        fileWriter.close();
//        startWriter.close();
//        contentWriter.close();
//        slotWriter.close();
//
//        FileWriter mergeWriter = new FileWriter(targetFileName);
//        mergeWriter.append("#BNF+IAT 1.0;\r\n")
//                .append("!grammer ")
//                .append(targetFileName.substring(targetFileName.lastIndexOf("\\") + 1, targetFileName.lastIndexOf(".")))
//                .append("\r\n\r\n");
//
//
//    }
//
//    public static void main(String[] args) throws IOException {
//        //checkFrequencyOfSlot(args[0]);
////        checkFrequencyOfSlot("speech_songs_test.bnf");
//        mergeBnfFiles("D:\\JavaPro\\ExtendSentence", "merge_res.bnf");
//
//        switch (args[0]) {
//            case "0":
//                checkFrequencyOfSlot(args[1]);
//                break;
//            case "1":
//                handleDirBnf(args[1]);
//                break;
//            case "2":
//                mergeBnfFiles(args[1], args[2]);
//                break;
//            default:
//                System.out.println("wrong arguments");
//                break;
//        }
//    }
//
//}
