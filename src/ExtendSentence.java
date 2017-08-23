import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtendSentence {

    public static int[] findPattern(String s, int type) {
        int[] indices = new int[2];
        char leftChar = '(';
        char rightChar = ')';

        indices[0] = indices[1] = -1;
        switch (type) {
            case 2:
                leftChar = '(';
                rightChar = ')';
                break;
            case 1:
                leftChar = '[';
                rightChar = ']';
            default:
                break;
        }

        Stack<Character> stack = new Stack<>();
        char[] chars = s.toCharArray();
        for (int i = 0; i < s.length(); i++) {
            if (chars[i] == leftChar) {
                indices[0] = i;
                break;
            }
        }
        if (indices[0] == -1) {
            return indices;
        }
        stack.push(chars[indices[0]]);
        int j = indices[0] + 1;
        while (j < s.length()) {
            if (chars[j] == leftChar) {
                stack.push(leftChar);
            }
            if (chars[j] == rightChar) {
                stack.pop();
                if (stack.empty()) {
                    indices[1] = j;
                    return indices;
                }
            }
            j++;
        }
        return indices;
    }

    public static ArrayList<String> extendOne(String s) throws IOException {

        //System.out.println("one--" + s);

        boolean isError = false;

        ArrayList<String> results = new ArrayList<>();

        int[][] indices = new int[2][2];
        indices[0] = findPattern(s, 1);
        indices[1] = findPattern(s, 2);
        int sign = -1;
        boolean[] isFind = new boolean[2];
        isFind[0] = indices[0][1] != -1;
        isFind[1] = indices[1][1] != -1;
        if (isFind[0] && !isFind[1]) {
            sign = 0;
        } else if (!isFind[0] && isFind[1]) {
            sign = 1;
        } else if (isFind[0] && isFind[1]) {
            sign = indices[0][0] < indices[1][0] ? 0 : 1;
        } else {
            results.add(s);
            return results;
        }

        ArrayList<String> mids = extendIn(s.substring(indices[sign][0] + 1, indices[sign][1]));
        if (sign == 0) {
            mids.add("");
        }
        ArrayList<String> posts = extendOne(s.substring(indices[sign][1] + 1));
        String pre = s.substring(0, indices[sign][0]);
        for (String mid : mids) {
            if (isError) {
                break;
            }
            for (String post : posts) {
                String temp = pre + mid + post;
                temp = temp.replaceAll(" +", " ");
                if (temp.contains("]") || temp.contains("[") || temp.contains("(") || temp.contains(")")) {
                    isError = true;
                    break;
                } else {
                    results.add(temp);
                }
            }
        }

        if (!isError) {
            return results;
        } else {
            return new ArrayList<>();
        }


    }

    public static int isRight(char c) {
        if (c == '(' || c == '[') {
            return 1;
        } else if (c == ')' || c == ']') {
            return 2;
        }
        return 0;
    }

    public static ArrayList<String> extendIn(String s) throws IOException {

        //System.out.println("in--" + s);
        Stack<Character> stack = new Stack<>();
        char[] chars = s.toCharArray();
        int lastIndex = 0;
        ArrayList<String> arrayList = new ArrayList<>();
        ArrayList<String> finalResult = new ArrayList<>();
        for (int i = 0; i < chars.length; i++) {
            int isRightInt = isRight(chars[i]);
            if (isRightInt == 1) {
                stack.push(chars[i]);
            } else if (isRightInt == 2) {
                stack.pop();
            } else if (chars[i] == '|' && stack.empty()) {
                arrayList.add(s.substring(lastIndex, i));
                lastIndex = i + 1;
            }
        }
        arrayList.add(s.substring(lastIndex));
        for (String str : arrayList) {
            //System.out.println("**"+str);
            finalResult.addAll(extendOne(str));
        }
        return finalResult;
    }

    public void batchProcess(String fileName, String resFileName, boolean isForSearch) throws IOException {

        FileReader fileReader = new FileReader(fileName);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String s;
        FileWriter errorWriter = new FileWriter(new File(fileName).getName() + "-error-info.txt", true);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
        errorWriter.append(simpleDateFormat.format(new Date())).append(fileName).append("\r\n");


        FileWriter fileWriter = new FileWriter(resFileName);

        int startSign = 0;
        while ((s = bufferedReader.readLine()) != null) {
            if (Objects.equals(s, "")) {
                fileWriter.append("\r\n");
                startSign = 0;
                continue;
            }
            if (startSign == 0) {
                fileWriter.append(s).append("\r\n");
                startSign++;
                continue;
            }
            s = s.replace("\"", "");
            ArrayList<String> res = extendOne(s);
            if (res.size() == 0) {
                errorWriter.append("句法格式错误：").append(s);
            }
            for (String resStr : res) {
                int count = 0;
                resStr = resStr.replaceAll(" +", " ");
                for (int i = 0; i < resStr.length(); i++) {
                    if (resStr.charAt(i) == ' ') {
                        count++;
                    } else {
                        break;
                    }
                }
//                System.out.println(count);
                resStr = resStr.substring(count);
                if (isForSearch) {
                    fileWriter.append("#");
                }
                fileWriter.append(resStr).append("\r\n");
            }
            if (isForSearch) {
                fileWriter.append("##").append("\r\n");
            }
        }
        errorWriter.append("\r\n");
        errorWriter.close();
        fileWriter.close();
        bufferedReader.close();
    }

    private HashMap<String, ArrayList<String>> getHashMapFromMyContent(String contentFileName) throws IOException {
        BufferedReader cBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(contentFileName), "utf-8"));
        HashMap<String, ArrayList<String>> hashMap = new HashMap<>();
        String slotName = null;
        while ((slotName = cBufferedReader.readLine()) != null) {
            if (slotName.startsWith("#")) {
                String cLine = null;
                ArrayList<String> contents = new ArrayList<>();
                while ((cLine = cBufferedReader.readLine()) != null && cLine.length() > 0) {
                    contents.add(cLine);
                }
                hashMap.put(slotName.substring(1), contents);
            }
        }
        return hashMap;
    }

    private void getAllSentenceFromParseResult(String parseResultFileName, String contentFileName) throws IOException {
        BufferedReader prBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(parseResultFileName), "utf-8"));
        String speech = null;
        HashMap<String, ArrayList<String>> contents = getHashMapFromMyContent(contentFileName);
        FileWriter fileWriter = new FileWriter(parseResultFileName.split("-")[0] + "-all-sentences.txt");
        while ((speech = prBufferedReader.readLine()) != null) {
            if (speech.startsWith("###") || !speech.contains("#")) {
                continue;
            }
            ArrayList<String> sentences = getSentenceFrom(speech.split("#")[1], contents);
            sentences.forEach((s) -> {
                try {
                    fileWriter.append(s).append("\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        fileWriter.close();
    }

    private ArrayList<String> getSentenceFrom(String sentence, HashMap<String, ArrayList<String>> hashMap) {
        if (sentence.length() == 0) {
            return new ArrayList<>();
        }
        //System.out.println("s=" + sentence);
        Pattern pattern = Pattern.compile("<\\w*?>");
        Matcher matcher = pattern.matcher(sentence);
        ArrayList<String> content = new ArrayList<>();
        ArrayList<String> posts;
        ArrayList<String> res = new ArrayList<>();
        if (matcher.find()) {
            String ab = matcher.group();
            content.addAll(hashMap.get(ab.replace("<", "").replace(">", "")));
            //System.out.println(content.size());
//            System.out.println(ab + "--content size = " + content.size());
            content.add(ab);
//            System.out.println(ab + "--content size = " + content.size());
            posts = getSentenceFrom(sentence.substring(matcher.end()), hashMap);
        } else {
            content = new ArrayList<>();
            posts = new ArrayList<>();
        }

        System.out.println("---content----");
        content.forEach(System.out::println);

        if (posts.size() == 0) {
            return content;
        } else {
            content.forEach((c) -> {
                //System.out.println("ccc = " + c);
                posts.forEach((post) -> {
                    //System.out.println("post = " + post);
                    res.add(c + " " + post);
                });
            });
        }
        return res;
    }


    public static void main(String[] args) throws IOException {

        //       extendOne("[聪明的|可爱的] 我是 [狗[蛋|剩]子的|猫的] [帅气的|可爱的] (男主人|女主人) 的 [宝宝的|大大的] 玩具").forEach(System.out::println);
        new ExtendSentence().batchProcess("bnfs-dir-start.txt", "bnfs-dir-start-parse-result.txt", false);
        new ExtendSentence().getAllSentenceFromParseResult("bnfs-dir-start-parse-result.txt", "bnfs-dir-content.txt");
//        batchProcess(args[0], args[1]);
        //batchProcess("ss.txt", "res.txt");
    }

}
