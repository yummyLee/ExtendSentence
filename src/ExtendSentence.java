import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

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
            FileWriter fileWriter = new FileWriter("errorInfo.txt");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
            fileWriter.append(simpleDateFormat.format(new Date()));
            fileWriter.append("句法格式错误：").append(s);
            fileWriter.close();
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

    public static void batchProcess(String fileName, String resFileName) throws IOException {

        FileReader fileReader = new FileReader(fileName);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String s;

        FileWriter fileWriter = new FileWriter(resFileName);

        int startSign = 0;
        while ((s = bufferedReader.readLine()) != null) {
            if (Objects.equals(s, "")) {
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
                fileWriter.append(resStr).append("\r\n");
            }
        }
        fileWriter.close();
    }


    public static void main(String[] args) throws IOException {

        extendOne("[聪明的|可爱的] 我是 [狗[蛋|剩]子的|猫的] [帅气的|可爱的] (男主人|女主人) 的 [宝宝的|大大的] 玩具").forEach(System.out::println);
        //batchProcess(new File(args[0]), args[1]);
//        batchProcess(args[0], args[1]);
        //batchProcess("ss.txt", "res.txt");
    }

}
