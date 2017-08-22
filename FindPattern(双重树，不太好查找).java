import sun.reflect.generics.tree.Tree;
import sun.text.normalizer.Trie;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindPattern {

    private TrieNode slotRoot;
    private TrieNode contentRoot;

    private class TrieNode {
        private HashMap<String, TrieNode> sons;
        private String name;
        private boolean isFollowSpace;
        private boolean isEnd;

        TrieNode(String name) {
            this.name = name;
            sons = new HashMap<>();
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
            for (String content : contents) {
                TrieNode trieNode = new TrieNode(content);
                parent.put(content, trieNode);
                parent = parent.sons.get(content);
            }
            parent.isEnd = true;
        }

        private void insertSlotRoot(String speech, int line) {
            TrieNode parent = this;
            Pattern pattern = Pattern.compile("<\\w*?>");
            Matcher matcher = pattern.matcher(speech);
            //System.out.println("-----");
            //System.out.println(speech);
            while (matcher.find()) {
                String slotName = speech.substring(matcher.start() + 1, matcher.end() - 1);
                TrieNode trieNode = new TrieNode(slotName);
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
        }

    }

    FindPattern() {
        slotRoot = new TrieNode("root");
        contentRoot = new TrieNode("root");
    }

    private TrieNode generateTrieNodeBySpeeches(String fileName) throws IOException {
        BufferedReader speechReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "utf-8"));
        String title = null;

        while ((title = speechReader.readLine()) != null) {
            if (title.startsWith("###")) {
                String[] info = title.split("###");
                int line = Integer.parseInt(info[1]) + 1;
                String orignalFileName = info[2];
                String speech = null;
                while ((speech = speechReader.readLine()) != null && speech.length() > 0) {
                    if (speech.startsWith("##")) {
                        line++;
                        continue;
                    }
                    slotRoot.insertSlotRoot(speech, line);
                }
            }
        }
        return null;
    }

    private boolean findSpeech(String speech) {
        speech = speech.replaceAll(" +", " ");
        TrieNode parent = slotRoot;
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

    private TrieNode generateTrieNodeByContents() throws IOException {
        HashMap<String, ArrayList<String>> contentsMap = new SpeechTool().readPatternByContentFile("bnf-dir-content.txt");
        contentsMap.forEach((k, v) -> {
            v.forEach(content -> contentRoot.insertContentRoot(k + " " + content));
        });
        return null;
    }

    private boolean findContent(String content) {
        content=content.replaceAll(" +"," ");
        String[] words = content.split(" ");
        TrieNode parent = contentRoot;
        for (String w : words) {
            if (parent.sons.containsKey(w)) {
                parent = parent.sons.get(w);
            } else {
                return false;
            }
        }
        return parent.isEnd;
    }

    private boolean findSentence(String sentence) {

        return false;
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


    public static void main(String[] args) throws IOException {
        FindPattern findPattern = new FindPattern();
        findPattern.generateTrieNodeBySpeeches("bnf-dir-start-parse-result.txt");
        System.out.println(findPattern.findSpeech("<prePlayCollection> <stableSpeech>"));
        findPattern.generateTrieNodeByContents();
        System.out.println(findPattern.findContent("stableSpeech    please"));

        //findPattern.passTree(findPattern.slotRoot);
    }
}
