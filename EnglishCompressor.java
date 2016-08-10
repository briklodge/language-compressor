package compression;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class EnglishCompressor
{

	HashMap<String,Integer> wordIDs = new HashMap<String,Integer>(); 
	ArrayList<String> words = new ArrayList<String>();
	ArrayList<Integer> wordFreq = new ArrayList<Integer>();
	ArrayList<Integer> wordHuffcode = new ArrayList<Integer>();
	HuffNode huffTree;
	
	short[][] firstTrans = new short[4096][4096];
	
	public static void main(String[] args)
	{
		EnglishCompressor c = new EnglishCompressor();
		
//		BitWriter writer = new BitWriter("/tmp/out.dat");
//		writer.writeBig(0x3f,6);
//		writer.writeBig(0x6,2);
//		writer.writeBig(0x0,4);
//		writer.writeBig(0x78,7);
//		writer.close();
//		BitReader reader = new BitReader("/tmp/out.dat");
//		for(int b=reader.readBig();b>=0;b=reader.readBig())
//			System.out.println(b);
		
		c.seed("/Users/kyle/projects/data/Alice.txt");
		c.encode("/Users/kyle/projects/data/Alice.txt","/Users/kyle/projects/data/Alice_encode.txt");
		c.decode("/Users/kyle/projects/data/Alice_encode.txt","/Users/kyle/projects/data/Alice_decode.txt");
	}
	
	public EnglishCompressor()
	{
	}
	
	public void encode(String inFile, String outFile)
	{
		TokenReader reader = new TokenReader(inFile);
		BitWriter writer = new BitWriter(outFile);
		String word;
		while((word = reader.nextWord()) != null)
		{
			Integer wordID = wordIDs.get(word);
			int huffCode = wordHuffcode.get(wordID);
			int bitcount = 20;
			while((huffCode ^ (1<<bitcount)) > huffCode)
				bitcount--;
//			System.out.println("e \""+word+"\" "+Integer.toBinaryString(huffCode & ((1<<bitcount)-1))+" "+bitcount);
			writer.writeBig(huffCode, bitcount);
		}
		writer.close();
	}
	
	public void decode(String inFile, String outFile)
	{
		try
		{
			BitReader reader = new BitReader(inFile);
			BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
			int bit;
			HuffNode cursor = huffTree;
//			int huffCode = 0;
//			int bitcount = 0;
			while((bit = reader.readBig()) != -1)
			{
				if(bit == 0)
					cursor = cursor.getLeft();
				else
					cursor = cursor.getRight();
//				huffCode = (huffCode<<1) | bit; 
//				bitcount ++;
				if(cursor.getValue() != null)
				{
//					System.out.println("d \""+cursor.getValue()+"\" "+Integer.toBinaryString(huffCode)+" "+bitcount);
					writer.write(cursor.getValue());
					cursor = huffTree;
//					huffCode = 0;
//					bitcount = 0;
				}
			}
			writer.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void seed(String fileName)
	{
		TokenReader reader = new TokenReader(fileName);
		String word;
		int totalwords=0;
		Integer lastWordID = null;
		while((word = reader.nextWord()) != null)
		{
			totalwords++;
			Integer wordID = wordIDs.get(word);
			if(wordID == null)
			{
				wordID = words.size();
				wordIDs.put(word, wordID);
				words.add(word);
				wordFreq.add(1);
			}
			else
			{
				wordFreq.set(wordID, wordFreq.get(wordID)+1);
			}
			
			if(lastWordID != null)
			{
				firstTrans[lastWordID][wordID] ++;
			}
			lastWordID = wordID;
			
		}
		reader.close();
		System.out.println("total words " + totalwords);
		System.out.println("unique words " + words.size());
		
		TreeSet<HuffNode> sortWords = new TreeSet<HuffNode>();
		for(int wordID=0 ; wordID<words.size() ; wordID++)
		{
			HuffNode node = new HuffNode(words.get(wordID), wordFreq.get(wordID));
			sortWords.add(node);
		}
		System.out.println("unique words " + sortWords.size());
		while(sortWords.size() > 1)
		{
			HuffNode first = sortWords.first();
			HuffNode sec = sortWords.higher(first);
			HuffNode merge = new HuffNode(first,sec);
			sortWords.remove(first);
			sortWords.remove(sec);
			sortWords.add(merge);
		}
		System.out.println("reduced "+sortWords.size());
		huffTree = sortWords.first();
		huffTree.poll(1,wordIDs,wordHuffcode);
		System.out.println("polled "+wordHuffcode.size());
		
		
//			for(int i=0 ; i<words.size() ; i++)
//			{
//				System.out.println(words.get(i)+" "+wordHuffcode.get(i));
//			}
		
//			float num = 0;
//			float dom = 0;
//			for(int i=0 ; i<firstTrans.length ; i++)
//			{
//				int tot = 0;
//				int max = 0;
//				for(int j=0 ; j<firstTrans[0].length ; j++)
//				{
//					int k = firstTrans[i][j];
//					tot += k;
//					if(k > max) max=k;
//				}
//				num += max*100;
//				dom += tot;
//				if(tot > 0)
//					System.out.print(max*100/tot + "% ");
//			}
//			System.out.println();
//			System.out.println("avg "+num/dom+"%");
		
	}
}
