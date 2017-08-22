# 句法工具
### 一、	功能概述
#### 1、__对一种固定的句法格式的句子进行解析。__     
例如下面这个句法：  
> __我是 [狗[蛋|剩]子的|猫的] (男主人|女主人) 的朋友__    
> 方括号[a|b|c]表示从中选出a或b或c或空。   
> 圆括号(a|b|c)表示从中选出a或b或c。 

那么上述的句法可以解析出一个句子：“我是狗蛋子的女主人的朋友”。 

本工具的作用就是根据句法解析出所有的句子。
如： 
- 我是 狗蛋子的 男主人 的朋友
- 我是 狗蛋子的 女主人 的朋友
- 我是 狗剩子的 男主人 的朋友
- 我是 狗剩子的 女主人 的朋友
- 我是 狗子的 男主人 的朋友
- 我是 狗子的 女主人 的朋友
- 我是 猫的 男主人 的朋友
- 我是 猫的 女主人 的朋友
- 我是 男主人 的朋友   
- 我是 女主人 的朋友  

并对格式不正确的句法给出错误提示。

#### 2、__BNF文件中槽位信息检查以及合并。__     
- 检查单个bnf文件或目录下所有bnf文件中是否出现槽位使用次数小于3以及槽位长度大于14。
- 合并指定目录下的所有bnf文件，对合并中文件里槽内容出现重复的情况在命令行给出提示。    
如下面的槽位内容：   
&ltSongs&gt:    
"songs" |    
"song" |    
"songs";    
其中“songs”出现了两次，所以在合并时会在命令行中给出提示。  

#### 3、搜索给定句子是否能够匹配上BNF文件中说法
如BNF文件中存在__&ltpreSongs&gt &ltSongs&gt__这一说法，其中槽位__&ltpreSongs&gt__中包含“listen to”这一内容，槽位__&ltSongs&gt__中包含“songs”这一内容，那么句子“listen to songs”可以匹配__&ltpreSongs&gt &ltSongs__&gt这一说法。  



### 二、	使用说明
该工具以Java为开发语言编写而成，未进行封装，所以使用需要配置好Java的开发环境。   

代码文件名为ExtendSentence.java，  SpeechTool.java，FindPattern.java。  

在命令行中进入代码所在目录对文件进行编译：
编译指令为：
> javac –encoding utf8 *.java    

编译完成后，会生成几个class格式的文件  

__执行指令包含5种格式：__
1. 解析说法，对文件中的说法进行扩展   
	> java FindPattern 0 &lt句法文件路径名&gt &lt结果文件保存路径名&gt   
	
	执行完成后可在代码所在文件查看错误检测日志文件 <句法文件名>-error-info.txt
	
2. 对单个bnf文件中的槽位使用次数是否大于3或槽位名长度是否大于14进行检测  
	> java FindPattern 1 &ltbnf文件路径名&gt    
	
	执行完成后在代码所在路径可查看日志文件：&lt文件名&gt-stat-report.txt

3. 对指定目录下所有bnf文件中的使用次数是否大于3或槽名长度是否大于14进行检测
	> java FindPattern 2 &lt目录名&gt  
	
	执行完成后在代码所在路径可查看到日志文件：&lt目录名&gt-dir-stat-report.txt  

4. 合并指定目录下所有的bnf文件到一个bnf文件
	> java FindPattern 3 &lt目录名&gt &lt输出文件名&gt   

	执行过程中可在命令行中看到bnf中某些槽内内容的提示。  
	执行完成后在指定路径可查看到合并的bnf，在代码所在路径可查看到检测日志文件&lt目录名&gt-dir-stat-report.txt，合并日志文件&lt目录名&gt-dir-merge-report.txt  

5. 搜索给定句子是否能够匹配上BNF文件中说法
	>java FindPattern 4 &ltbnf文件目录名&gt &lt待匹配文件（包含多条待匹配的句子）&gt  
	
	完成之后可在代码所在路径下查看匹配结果文件<待匹配文件名>-match-result.txt
	若匹配成功则可看到：
	> &lt句子&gt: &lt说法&gt in &lt说法所在的bnf文件&gt line &lt说法在bnf文件中的行数&gt
	
	若匹配失败则可看到：
	>&lt句子&gt: match failed


