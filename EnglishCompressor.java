package compression;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class EnglishCompressor
{

	int totalwords=0;
	
	
	ArrayList<String> words = new ArrayList<String>();
	ArrayList<Integer> wordFreqs = new ArrayList<Integer>();
	
	HashMap<String,Integer> wordCodes = new HashMap<String,Integer>(); 
	HuffNode huffTree;

	// for optimization, not model
	HashMap<String,Integer> wordIDs = new HashMap<String,Integer>(); 
	
	
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

		c.seed("/Users/kyle/projects/data/war_and_peace.txt");
//		c.seed("/Users/kyle/projects/data/Alice.txt");
		c.seedFinish();
		
		c.encode("/Users/kyle/projects/data/Alice.txt","/Users/kyle/projects/data/Alice_encode.txt");
		c.decode("/Users/kyle/projects/data/Alice_encode.txt","/Users/kyle/projects/data/Alice_decode.txt");
	}
	
	public EnglishCompressor()
	{
	}
	
	public int huffBitcount(int huffCode)
	{
		int bitcount = 30;
		while((huffCode ^ (1<<bitcount)) > huffCode)
			bitcount--;
		return bitcount;
	}
	
	public void encode(String inFile, String outFile)
	{
		TokenReader reader = new TokenReader(inFile);
		BitWriter writer = new BitWriter(outFile);
		String word = null;
		while((word = reader.nextWord()) != null)
		{
			Integer code = wordCodes.get(word);
//			System.out.println("e \""+word+"\" "+Integer.toBinaryString(huffCode & ((1<<bitcount)-1))+" "+bitcount);
			if(code == null)
			{
				code = wordCodes.get("");

				writer.writeBig(code, huffBitcount(code));
				byte[] strbytes = word.getBytes();

//				System.out.println("ecodel "+Integer.toBinaryString(code).substring(1)+" "+word);
//				System.out.println("lit "+code);
//				if(strbytes.length > 20)
//				{
//					System.out.println("e lit len "+code+" "+strbytes.length);
//				}
				for(byte b8 : strbytes)
					writer.writeBig(b8, 8);
				writer.writeBig(0, 8);
			}
			else
			{
//				System.out.println("ecode "+Integer.toBinaryString(code).substring(1)+" "+word);
				writer.writeBig(code, huffBitcount(code));
			}
		}
		int code = wordCodes.get("");
		writer.writeBig(code, huffBitcount(code));
		writer.writeBig(0, 8);
		writer.close();
		System.out.println();
		System.out.println();
	}
	
	public void decode(String inFile, String outFile)
	{
		try
		{
			BitReader reader = new BitReader(inFile);
			BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
			int bit;
			HuffNode cursor = huffTree;
			int code = 1;
			int bitcount = 0;
			while((bit = reader.readBig()) != -1)
			{
				if(bit == 0)
					cursor = cursor.getLeft();
				else
					cursor = cursor.getRight();
				code = (code<<1) | bit; 
				bitcount ++;
				if(cursor.getValue() != null)
				{
//					System.out.println("d \""+cursor.getValue()+"\" "+Integer.toBinaryString(huffCode)+" "+bitcount);
					if(cursor.getValue().equals(""))
					{
						byte[] string = new byte[256];
						int strlen = 0;
						int b8;
						while((b8 = reader.readBig8()) > 0)
						{
							string[strlen++] = (byte)(b8 & 255);
						}
						if(strlen == 0)
							break;
						String lit = new String(string, 0, strlen);

//						System.out.println("dcodel "+Integer.toBinaryString(code).substring(1)+" "+lit);
//						System.out.println("dcodel "+lit);
//						System.out.println("d lit: "+lit);
						writer.write(lit);
					}
					else
					{
//						System.out.println("dcode "+Integer.toBinaryString(code).substring(1)+" "+cursor.getValue());
						writer.write(cursor.getValue());
					}
					cursor = huffTree;
					code = 1;
					bitcount = 0;
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
//		Integer lastWordID = null;
		while((word = reader.nextWord()) != null)
		{
			totalwords++;
			Integer wordID = wordIDs.get(word);
			if(wordID == null)
			{
				wordID = words.size();
				wordIDs.put(word, wordID);
				words.add(word);
				wordFreqs.add(1);
			}
			else
			{
				wordFreqs.set(wordID, wordFreqs.get(wordID)+1);
			}
			
//			if(lastWordID != null)
//			{
//				firstTrans[lastWordID][wordID] ++;
//			}
//			lastWordID = wordID;
		}
		reader.close();
	}

	public void seedFinish()
	{
		huffTree = HuffNode.buildTree(words, wordFreqs);
		huffTree.poll(1, wordCodes);
		
		System.out.println("total words " + totalwords);
		System.out.println("unique words " + words.size());

		wordIDs = null;
	}
}
