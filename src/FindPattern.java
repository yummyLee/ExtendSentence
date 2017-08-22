import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindPattern {

    private TrieNode slotRoot;
    private TrieNode contentRoot;

    private class TrieNode {
        private HashMap<String, TrieNode> sons;
        private String name;
        private int line;
        private String bnfFileName;
        private Set<String> slotSet;
        private boolean isFollowSpace;
        private boolean isEnd;

        TrieNode(String name, int line, String bnfFileName) {
            this.name = name;
            this.line = line;
            this.bnfFileName = bnfFileName;
            sons = new HashMap<>();
            slotSet = new HashSet<>();
            isFollowSpace = false;
            isEnd = false;
        }

        TrieNode(String name) {
            this.name = name;
            sons = new HashMap<>();
            slotSet = new HashSet<>();
            isFollowSpace = false;
            isEnd = false;
        }

        void setFollowSpace(boolean isFollowSpace) {
            this.isFollowSpace = isFollowSpace;
        }

        void put(String str, TrieNode trieNode) {
            //System.out.println("---"+str);
            if (sons.containsKey(str)) {
            } else {
                sons.put(str, trieNode);
            }
        }

        private void insertContentRoot(String sentence) {
            TrieNode parent = this;
            String[] contents = sentence.split(" ");
            for (int i = 1; i < contents.length; i++) {
                String content = contents[i];
                TrieNode trieNode = new TrieNode(content);
                parent.put(content, trieNode);
                parent = parent.sons.get(content);
            }
            parent.slotSet.add(contents[0]);
            parent.isEnd = true;
        }

        private void insertSlotRoot(String speech, int line, String bnfFileName) {
            TrieNode parent = this;
            Pattern pattern = Pattern.compile("<\\w*?>");
            Matcher matcher = pattern.matcher(speech);
            //System.out.println("-----");
            //System.out.println(speech);
            while (matcher.find()) {
                String slotName = speech.substring(matcher.start() + 1, matcher.end() - 1);
                TrieNode trieNode = new TrieNode(slotName, line, bnfFileName);
                if (matcher.end() + 1 < speech.length()) {
                    if (speech.charAt(matcher.end() + 1) == ' ') {
                        trieNode.setFollowSpace(true);
                    }
                }
                //System.out.println(slotName);
                parent.put(slotName, trieNode);
                //System.out.println(slotName);
                parent = parent.sons.get(slotName);
            }
            parent.isEnd = true;
            parent.line = line;
        }

    }

    FindPattern() {
        slotRoot = new TrieNode("root", 0, "");
        contentRoot = new TrieNode("root");
    }

    private TrieNode generateTrieNodeBySpeeches(String fileName) throws IOException {
        BufferedReader speechReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "utf-8"));
        String title = null;

        while ((title = speechReader.readLine()) != null) {
            if (title.startsWith("###")) {
                String speech = null;
                while ((speech = speechReader.readLine()) != null && speech.length() > 0) {
                    if (speech.equals("##")) {
                        continue;
                    }
                    //System.out.println(speech);
                    String[] sp = speech.split("#");
                    int line = Integer.parseInt(sp[1]);
                    slotRoot.insertSlotRoot(sp[2], line, title.replace("###", ""));
                }
            }
        }
        speechReader.close();
        return null;
    }

    private boolean findSpeech(String speech, TrieNode root) {
        speech = speech.replaceAll(" +", " ");
        TrieNode parent = root;
        Pattern pattern = Pattern.compile("<\\w*?>");
        Matcher matcher = pattern.matcher(speech);
        while (matcher.find()) {
            String slotName = speech.substring(matcher.start() + 1, matcher.end() - 1);
            TrieNode trieNode;
            if (parent.sons.containsKey(slotName)) {
                trieNode = parent.sons.get(slotName);
                if (matcher.end() + 1 < speech.length()) {
                    if (speech.charAt(matcher.end() + 1) == ' ' && !parent.isFollowSpace) {
                        return false;
                    }
                }
                parent = trieNode;
            } else {
                return false;
            }
        }
        if (!parent.isEnd) {
            return false;
        }
        return true;
    }

    private TrieNode generateTrieNodeByContents(String contentFileName) throws IOException {
        HashMap<String, ArrayList<String>> contentsMap = new SpeechTool().readPatternByContentFile(contentFileName);
        contentsMap.forEach((k, v) -> v.forEach(content -> contentRoot.insertContentRoot(k + " " + content)));
        return null;
    }

    private Set<String> findContent(String content, TrieNode root) {
        content = content.replaceAll(" +", " ");
        String[] words = content.split(" ");
        TrieNode parent = root;
        for (String w : words) {
            if (parent.sons.containsKey(w)) {
                parent = parent.sons.get(w);
            } else {
                return new HashSet<>();
            }
        }
        return parent.slotSet;
    }

    private String findSentence(String sentence, TrieNode sRoot, TrieNode cRoot, String speech) {
        //System.out.println("sentence = " + sentence + ", speech = " + speech);
        sentence = sentence.replaceAll(" +", " ");
        if (sentence.charAt(0) == ' ') {
            sentence = sentence.substring(1);
        }
        String[] words = sentence.split(" ");
        StringBuilder sb = new StringBuilder();
        TrieNode pSRoot = sRoot;
//        TrieNode pCRoot = cRoot;

//        System.out.println("<--findSentence--->" + sentence + " <--> " + sRoot.name);
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(words[i]);
            Set<String> slotSet;
            if (words[i].startsWith("<")) {
                slotSet = new HashSet<>();
                slotSet.add(words[i].substring(1, words[i].length() - 1));
            } else {
                slotSet = findContent(sb.toString(), cRoot);
            }
//            System.out.println("C->" + sb.toString());
            for (String slot : slotSet) {
                //System.out.println(slot);
                if (pSRoot.sons.containsKey(slot)) {
                    TrieNode ssRoot = pSRoot.sons.get(slot);
                    if (ssRoot.isEnd && i == words.length - 1) {
//                        System.out.println("Found");
                        return speech + "<" + slot + "> in " + ssRoot.bnfFileName + " line " + ssRoot.line;
                    }
                    String nextRes;
                    if (speech.length() > 0) {

                        nextRes = speech + " <" + slot + "> ";
                    } else {
                        nextRes = "<" + slot + "> ";
                    }
                    //System.out.println(sentence);
                    //System.out.println(sb);
                    String res = findSentence(sentence.substring(sb.length() + 1), ssRoot, cRoot, nextRes);
                    if (res.length() > 0) {
                        return res;
                    }
                }
            }
        }
        return "";
    }

    private void passTree(TrieNode trieNode) {
        //System.out.println("---");
        trieNode.sons.forEach((s, t) -> {
            System.out.println(s + "--" + t.isEnd);
            passTree(t);
        });
//        HashMap<String, TrieNode> map = trieNode.sons.get("prePlayCollection").sons;
//        System.out.println(map.size());
//        map.forEach((s, t) -> {
//            System.out.println(s);
//        });
    }

    private void batchSearch(String bnfDirName, String sentenceFileName) throws IOException {
        SpeechTool speechTool = new SpeechTool();
        String filePostfix = bnfDirName.substring(bnfDirName.lastIndexOf("\\") + 1);
        speechTool.mergeBnfFiles(bnfDirName, "merge_res.bnf");
        File startFile = new File(filePostfix + "-dir-start.txt");
        File contentFile = new File(filePostfix + "-dir-content.txt");
        File parseResultFile = new File(filePostfix + "-dir-start-parse-result.txt");
        File mergeReportFile = new File(filePostfix + "-dir-merge-report.txt");
        ExtendSentence extendSentence = new ExtendSentence();
        extendSentence.batchProcess(startFile.getName(), parseResultFile.getName(), true);
        generateTrieNodeBySpeeches(parseResultFile.getName());
        generateTrieNodeByContents(contentFile.getName());

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(sentenceFileName), "utf-8"));
        FileWriter fileWriter = new FileWriter(sentenceFileName + "-match-result.txt");
        String readLine = null;
        while ((readLine = bufferedReader.readLine()) != null) {
            String res = findSentence(readLine, slotRoot, contentRoot, "");
            fileWriter.append(readLine).append(": ");
            if (res.length() == 0) {
                fileWriter.append("match failed");
            } else {
                fileWriter.append(res);
            }
            fileWriter.append("\r\n");
        }
        //findPattern.passTree(findPattern.slotRoot);
        //startFile.delete();
        contentFile.delete();
        //parseResultFile.delete();
        mergeReportFile.delete();
        bufferedReader.close();
        fileWriter.close();
    }

    public static void main(String[] argss) throws IOException {
        String[] args = new String[3];
        args[0] = "1";
        args[1] = "bnfs";
        args[2] = "testMatch.txt";

        switch (args[0]) {
            case "0":
                ExtendSentence extendSentence = new ExtendSentence();
                extendSentence.batchProcess(args[1], args[2], false);
                break;
            case "1":
                SpeechTool speechTool = new SpeechTool();
                speechTool.checkFrequencyOfSlot(args[1], true);
                break;
            case "2":
                SpeechTool speechTool1 = new SpeechTool();
                speechTool1.handleDirBnf(args[1]);
                break;
            case "3":
                SpeechTool speechTool2 = new SpeechTool();
                speechTool2.mergeBnfFiles(args[1], args[2]);
                break;
            case "4":
                FindPattern findPattern = new FindPattern();
                findPattern.batchSearch(args[1], args[2]);
                break;
            default:
                break;
        }
    }
}
