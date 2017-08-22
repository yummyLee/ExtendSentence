import java.io.*;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpeechTool {

    private int slotLimitLength = 23;
    private String speechStart = "speechStart";
    private String bnfFileName = "bnfFileName";
    private String prefix;
    private String startFileName;
    private String logFileName;
    private String contentFileName;
    private String slotFileName;
    private String targetFilePost;

    private ArrayList<String> angleBracketsCheck(String s) {
        ArrayList<String> res = new ArrayList<>();
        Pattern pattern = Pattern.compile("<\\w*?>");
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            res.add(matcher.group());
        }
        return res;
    }

    public String checkFrequencyOfSlot(String fileName, boolean isSingle) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(fileName).append("\r\n");
        if (!fileName.endsWith(".bnf")) {
            stringBuilder.append(currentDateLog()).append(fileName).append(" is not bnf file.").append("\r\n\r\n");
        } else {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            int lineCount = 0;
            String temp = null;
            HashMap<String, Integer> statistics = new HashMap<>();
            while ((temp = bufferedReader.readLine()) != null) {
                lineCount++;
                //System.out.println(temp);
                if (temp.startsWith("!slot <") && temp.length() > slotLimitLength) {
                    temp = temp.substring(7, temp.length() - 2);
                    stringBuilder.append(currentDateLog()).append("slot ").append(temp).append(" name length longer than 14 in line ").append(String.valueOf(lineCount)).append("\r\n");

                }
                ArrayList<String> matchRes = angleBracketsCheck(temp);
                matchRes.forEach((str) -> {
                    if (statistics.containsKey(str)) {
                        statistics.put(str, statistics.get(str) + 1);
                    } else {
                        statistics.put(str, 1);
                    }
                });
            }
            bufferedReader.close();

            statistics.forEach((str, count) -> {
                if (count < 3) {
                    stringBuilder.append(currentDateLog()).append("slot ").append(str).append(" appears less than 3 times").append("\r\n");
                }
            });

            stringBuilder.append("\r\n");
        }
        if (isSingle) {
            FileWriter fileWriter = new FileWriter(fileName.substring(fileName.lastIndexOf("\\") + 1) + "-stat-report.txt", true);
            fileWriter.append(stringBuilder.toString());
            fileWriter.append("\r\n");
            fileWriter.close();
        }
        return stringBuilder.toString();
    }

    public void handleDirBnf(String dirPath) throws IOException {
        FileWriter fileWriter = new FileWriter(dirPath.substring(dirPath.lastIndexOf("\\") + 1) + "-dir-stat-report.txt", true);
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            fileWriter.append(dirPath).append(" is not directory.").append("\r\n\r\n");
            fileWriter.close();
            return;
        }
        File[] bnfFiles = dir.listFiles();
        for (File bnf : bnfFiles != null ? bnfFiles : new File[0]) {
            String resLog = checkFrequencyOfSlot(bnf.getAbsolutePath(), false);
            fileWriter.append(resLog);
        }
        fileWriter.close();
    }

    private String currentDateLog() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]: ");
        return simpleDateFormat.format(new Date());
    }

    public HashMap<String, ArrayList<String>> findAngleBracketsContent1(String fileName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "utf-8"));
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        ArrayList<String> fileNameList = new ArrayList<>();
        fileNameList.add(new File(fileName).getName());
        result.put(bnfFileName, fileNameList);
        String temp = null;
        String startSlot = "";
        int readLineCount = 0;
        String slotName = "";
        while ((temp = bufferedReader.readLine()) != null) {
            readLineCount++;
            ArrayList<String> matchRes = angleBracketsCheck(temp);
            if (temp.startsWith("!start")) {
                startSlot = matchRes.get(0);
                slotName = startSlot;
                continue;
            }
            if (matchRes.size() > 0 && startSlot.equals(matchRes.get(0))) {
                int colonIndex = temp.indexOf(":");
                for (colonIndex = colonIndex + 1; colonIndex < temp.length(); colonIndex++) {
                    if (temp.charAt(colonIndex) != ' ') {
                        break;
                    }
                }
                ArrayList<String> arrayList = new ArrayList<>();
                String speech = temp.substring(colonIndex);
                if (speech.contains("<")) {
                    if (!speech.contains(";")) {
                        arrayList.add(readLineCount + "#" + speech.substring(0, speech.lastIndexOf("|")));
                    } else {
                        arrayList.add(readLineCount + "#" + speech.substring(0, speech.lastIndexOf(";")));
                    }
                }
                if (!temp.contains(";")) {
                    while ((temp = bufferedReader.readLine()) != null) {
                        readLineCount++;
                        speech = temp.replace("\"", "");
                        if (speech.contains("<")) {
                            if (!speech.contains(";")) {
                                int i = 0;
                                arrayList.add(readLineCount + "#" + speech.substring(0, speech.lastIndexOf("|")));
                            } else {
                                arrayList.add(readLineCount + "#" + speech.substring(0, speech.lastIndexOf(";")));
                            }
                        }
//                        System.out.println(readLineCount + "#" + speech);
                        if (temp.contains(";")) {
                            break;
                        }
                    }
                }
                result.put(speechStart, arrayList);
                //arrayList.forEach(System.out::println);
            } else if (temp.contains(":")) {
                int colonIndex = temp.indexOf(":");
                for (colonIndex = colonIndex + 1; colonIndex < temp.length(); colonIndex++) {
                    if (temp.charAt(colonIndex) != ' ') {
                        break;
                    }
                }
                ArrayList<String> arrayList = new ArrayList<>();
                String speech = temp.substring(colonIndex);
                speech = speech.replace(";", "");
                if (!speech.contains("<")) {
                    String[] sp = speech.split("\\|");
                    for (String s : sp) {
                        Pattern pattern = Pattern.compile("^ +$");
                        Matcher matcher = pattern.matcher(s);
                        if (!matcher.find() && s.length() > 0) {
                            s = s.replace("\"", "");
                            arrayList.add(s);
                        }
                    }
                } else {
                    arrayList.add(speech);
                }
                if (!temp.contains(";")) {
                    while ((temp = bufferedReader.readLine()) != null) {
                        readLineCount++;
                        speech = temp.replace("\"", "").replace(";", "");
                        if (!speech.contains("<")) {
                            String[] sp = speech.split("\\|");
                            for (int i = 0; i < sp.length; i++) {
                                Pattern pattern = Pattern.compile("^ +$");
                                Matcher matcher = pattern.matcher(sp[i]);
                                if (!matcher.find() && sp[i].length() > 0) {
                                    arrayList.add(sp[i]);
                                }
                            }
                        } else {
                            arrayList.add(speech);
                        }
                        if (temp.contains(";")) {
                            break;
                        }
                    }
                }
                result.put(matchRes.get(0).replace("<", "").replace(">", ""), arrayList);
            }
        }
        return result;
    }


    public void mergeBnfFiles(String dirPath, String targetFileName) throws IOException {

        prefix = dirPath.substring(dirPath.lastIndexOf("\\") + 1);
        startFileName = prefix + "-dir-start.txt";
        logFileName = prefix + "-dir-merge-report.txt";
        contentFileName = prefix + "-dir-content.txt";
        slotFileName = prefix + "-dir-slot.txt";
        targetFilePost = targetFileName.substring(targetFileName.lastIndexOf("\\") + 1, targetFileName.lastIndexOf("."));

        handleDirBnf(dirPath);

        File file = new File(targetFileName);
        if (file.exists()) {
            boolean isDelete = file.delete();
        } else {
            boolean isCreate = file.createNewFile();
        }
        File startFile = new File(startFileName);
        if (startFile.exists()) {
            startFile.delete();
            startFile.createNewFile();
        }


        FileWriter fileWriter = new FileWriter(logFileName, true);
        FileWriter startWriter = new FileWriter(startFileName);
        FileWriter contentWriter = new FileWriter(contentFileName);
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            fileWriter.append(currentDateLog()).append(dirPath).append(" is not directory.").append("\r\n\r\n");
            fileWriter.close();
            return;
        }
        File[] bnfFiles = dir.listFiles();
        HashMap<String, ArrayList<String>> mergeMap = new HashMap<>();
        for (File bnf : bnfFiles != null ? bnfFiles : new File[0]) {
            if (!bnf.getName().endsWith(".bnf")) {
                fileWriter.append(currentDateLog()).append(bnf.getAbsolutePath()).append(" is not bnf file.").append("\r\n");
                continue;
            }
            HashMap<String, ArrayList<String>> hashMap = findAngleBracketsContent1(bnf.getAbsolutePath());
            hashMap.forEach((k, set) -> {
                if (mergeMap.containsKey(k)) {
                    ArrayList<String> sSet = mergeMap.get(k);
                    sSet.addAll(set);
//                    System.out.println("---");
//                    sSet.forEach(System.out::println);
//                    System.out.println("---");
                    mergeMap.put(k, sSet);
                } else {
                    mergeMap.put(k, set);
                }
                if (k.equals(speechStart)) {
                    ArrayList<String> fileNameSet = hashMap.get(bnfFileName);
                    fileNameSet.forEach((fn) -> {
                        try {
                            startWriter.append("###").append(fn);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    try {
                        startWriter.append("\r\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    set.forEach((speech) -> {
                        try {
                            speech = replaceAB(speech, hashMap);
                            startWriter.append(speech).append("\r\n");
                            //System.out.println(speech);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    try {
                        startWriter.append("\r\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        startWriter.close();

        FileWriter mergeWriter = new FileWriter(targetFileName);
        mergeWriter.append("#BNF+IAT 1.0;\r\n")
                .append("!grammer ")
                .append(targetFilePost)
                .append(";\r\n\r\n");

        mergeMap.forEach((k, v) -> {
            try {
                if (!k.equals(speechStart)) {
                    mergeWriter.append("!slot <").append(k).append(">;\r\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        mergeWriter.append("\r\n");

        mergeWriter.append("!start <").append(targetFilePost).append("Start").append(">;\r\n");
        mergeWriter.append("<").append(targetFilePost).append("Start").append(">:\r\n");
        ArrayList<String> speeches = mergeMap.get(speechStart);
        int speechesLength = speeches.size();
        final int[] count = {0};
        speeches.forEach((speech) -> {
            try {
                String[] sp = speech.split("#");
                //System.out.println(speech);
                if (++count[0] != speechesLength) {
                    mergeWriter.append(sp[1]).append("|\r\n");
                } else {
                    mergeWriter.append(sp[1]).append(";\r\n\r\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        mergeMap.forEach((k, v) -> {

            if (!k.equals(speechStart)) {
                count[0] = 0;
                int contentSetLength = v.size();
                try {
                    contentWriter.append("#").append(k).append("\r\n");
                    mergeWriter.append("<").append(k).append(">:\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                v.forEach((content) -> {
                    try {
                        content = content.replaceAll(" +", " ");
                        contentWriter.append(content).append("\r\n");
                        mergeWriter.append("\"").append(content).append("\"");
                        if (++count[0] != contentSetLength) {
                            mergeWriter.append("|\r\n");
                        } else {
                            mergeWriter.append(";\r\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                try {
                    contentWriter.append("\r\n");
                    mergeWriter.append("\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        contentWriter.close();
        mergeWriter.close();
        fileWriter.close();
    }

    private String replaceAB(String oSpeech, HashMap<String, ArrayList<String>> hashMap) throws IOException {
        Pattern pattern = Pattern.compile("<\\w*?>");
        //System.out.println(oSpeech);
        boolean isContinue = false;
        while (true) {
            Matcher matcher = pattern.matcher(oSpeech);
            String ab = "";
            if (matcher.find()) {
                ab = matcher.group();
                isContinue = true;
            } else {
                break;
            }
            String abw = ab.substring(1, ab.length() - 1);
            if (hashMap.containsKey(abw)) {
                ArrayList<String> speeches = hashMap.get(abw);
                StringBuilder tempRes = new StringBuilder();
                if (speeches.size() == 1) {
                    tempRes.append(speeches.get(0));
                } else {
                    for (int i = 0; i < speeches.size(); i++) {
                        String speech = speeches.get(i);
                        if (!speech.contains("<")) {
                            return oSpeech;
                        }
                        tempRes.append("(").append(speech).append(")");
                        if (i != speeches.size() - 1) {
                            tempRes.append("|");
                        }
                    }
                }
                oSpeech = oSpeech.replace(ab, tempRes.toString());
            }
        }
        return isContinue ? replaceAB(oSpeech, hashMap) : oSpeech;
    }

    private String generatePatternByContent(ArrayList<String> contents) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        int i;
        for (i = 0; i < contents.size() - 1; i++) {
            sb.append(contents.get(i)).append("|");
        }
        sb.append(contents.get(i)).append(")");
        return sb.toString();
    }

    private HashMap<String, ArrayList<String>> readPatternByContentFile() throws IOException {
        return readPatternByContentFile(contentFileName);
    }

    public HashMap<String, ArrayList<String>> readPatternByContentFile(String contentFileName) throws IOException {
        BufferedReader contentFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(contentFileName), "utf-8"));
        String slotName = null;
        HashMap<String, ArrayList<String>> res = new HashMap<>();
        while ((slotName = contentFileReader.readLine()) != null) {
            if (slotName.startsWith("#")) {
                ArrayList<String> arrayList = new ArrayList<>();
                String content = null;
                while ((content = contentFileReader.readLine()) != null && content.length() > 0) {
                    arrayList.add(content);
                }
                slotName = slotName.substring(1);
                res.put(slotName, arrayList);
            }
        }
        contentFileReader.close();
        return res;
    }

    public static void main(String[] args) throws IOException {
        //checkFrequencyOfSlot(args[0]);
//        checkFrequencyOfSlot("speech_songs_test.bnf");
        SpeechTool speechTool = new SpeechTool();
        speechTool.mergeBnfFiles("bnf", "merge_res.bnf");
//        HashMap<String, ArrayList<String>> contentsMap = speechTool.readPatternByContentFile();
//        contentsMap.forEach((k, contents) -> {
//            System.out.println(k);
//            contents.forEach(System.out::println);
//        });
//        ArrayList<String> contents = contentsMap.get("prePlaySong");
//        contents.forEach(System.out::println);


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
    }

}
