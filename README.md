# 2Time

## About

2Time is an implementation of the attack detailed in [A Natural Language Approach to Automated Cryptanalysis
of Two-time Pads](https://www.cs.jhu.edu/~jason/papers/mason+al.ccs06.pdf) by Mason et al. 

## Usage Example

Here we present a toy example of 2Time in action. The corpus generated in this example is unusually small. Additionally, the individual files that make up the corpus lack the sort of underlying structure that 2Time's algorithm is designed to exploit. All of the files used in this example can be found in the [demo materials](https://github.com/bthendel/2Time/tree/master/demo%20materials) folder.

We start by placing three novels by Jules Verne in a folder called corpusData.

```
bhendel@workstation:~/corpusData$ ls -la
total 1516
drwxr-xr-x  2 bhendel bhendel   4096 Jun 17 08:00 .
drwxr-xr-x 43 bhendel bhendel   4096 Jun 17 07:59 ..
-rw-r--r--  1 bhendel bhendel 517675 Jun 14 14:37 A Journey to the Centre of the Earth.txt
-rw-r--r--  1 bhendel bhendel 398631 Jun 14 14:38 Around the World in 80 Days.txt
-rw-r--r--  1 bhendel bhendel 621865 Jun 14 14:36 Twenty Thousand Leagues under the Sea.txt
```

Now we generate our corpus. All files in the target folder as well as its subdirectories will be added to the corpus.

```
bhendel@workstation:~$ java -jar 2Time.jar --inputDir corpusData --outputCorpus verne.corpus
[+] Reading file /home/bhendel/corpusData/Twenty Thousand Leagues under the Sea.txt into corpus.
[+] Reading file /home/bhendel/corpusData/A Journey to the Centre of the Earth.txt into corpus.
[+] Reading file /home/bhendel/corpusData/Around the World in 80 Days.txt into corpus.
[+] Writing corpus to verne.corpus

Done.
```

Now we choose two random selections of text from “The Mysterious Island”, which was not put into the corpus.

```
"terminating in a white tuft, had betrayed their origin. So Herbert"
"silent, ran in advance. The cart came out, the gate was reclosed, "
```

We XOR the text samples together into a file called “message”.

```
bhendel@workstation:~$ python XORUtil.py "terminating in a white tuft, had betrayed their origin. So Herbert" "silent, ran in advance. The cart came out, the gate was reclosed, " message
```

Now we attempt to peel apart the messages. 

```
bhendel@workstation:~$ java -jar 2Time.jar --inputCorpus verne.corpus --inputData message --outputData out
[+] Processing byte 1...
[+] Processing byte 2...

... snip ...

[+] Processing byte 65...
[+] Processing byte 66...
[+] Done.

Message 1:

 oritiating to advance. The chierrake out their opigin. So Anded, 

Message 2:

 class, ran to a white tuft, azurserrayed, the gave was recedebert

```
Comparing the real text to the output of 2Time, we can see some striking similarities.

![text similarities](https://github.com/bthendel/2Time/blob/master/resources/text.jpg)

The original paper claims that the algorithm outputs near 100% accurate results for emails and HTMl documents given a corpus of 300k files.
