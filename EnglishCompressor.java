package compression;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class EnglishCompressor
{

	int totalwords=0;
	
	
//	ArrayList<String> words = new ArrayList<String>();
//	ArrayList<Integer> wordFreqs = new ArrayList<Integer>();
	
	HashMap<String,HuffNode> globalWordNodes = new HashMap<String,HuffNode>(); 
	HuffNode globalHuffTree;

	final int contextMask = 255;
	@SuppressWarnings("unchecked")
	HashMap<String,HuffNode>[] contextWordNodes = new HashMap[contextMask+1]; 
	HuffNode[] contextTrees = new HuffNode[contextMask+1];
	
	// for optimization, not model
//	HashMap<String,Integer> wordIDs = new HashMap<String,Integer>(); 
	
	
	public static void main(String[] args)
	{
		EnglishCompressor c = new EnglishCompressor();
		
		c.seedGlobal("/Users/kyle/projects/data/war_and_peace.txt");
		c.seedGlobal("/Users/kyle/projects/data/Alice.txt");
		c.seedGlobalFinish();

		c.seedMarkov("/Users/kyle/projects/data/war_and_peace.txt");
		c.seedMarkov("/Users/kyle/projects/data/Alice.txt");
		c.seedMarkovFinish();
		
		c.encode("/Users/kyle/projects/data/Alice.txt","/Users/kyle/projects/data/Alice_encode.txt");
		c.decode("/Users/kyle/projects/data/Alice_encode.txt","/Users/kyle/projects/data/Alice_decode.txt");
	}
	
	public EnglishCompressor()
	{
	}
	
	public int huffBitcount(int code)
	{
		int bitcount = 30;
		while((code ^ (1<<bitcount)) > code)
			bitcount--;
		return bitcount;
	}
	
	public int huffTrim(int code)
	{
		int mask = -1;
		while((code & mask) == code && mask != 0)
			mask = mask>>>1;
		return code & mask;
	}
	
	public void seedGlobal(String fileName)
	{
		TokenReader reader = new TokenReader(fileName);
		String word;
		while((word = reader.nextWord()) != null)
		{
			totalwords++;
			HuffNode node = globalWordNodes.get(word);
			if(node == null)
				globalWordNodes.put(word, new HuffNode(word, 1));
			else
				node.incFreq();
		}
		globalWordNodes.put("", new HuffNode("",0));
		reader.close();
	}

	public void seedGlobalFinish()
	{
		globalHuffTree = HuffNode.buildTree(globalWordNodes);
		
		System.out.println("total words " + totalwords);
		System.out.println("unique words " + globalWordNodes.size());
		System.out.println();
	}
	
	public void seedMarkov(String fileName)
	{
		TokenReader reader = new TokenReader(fileName);
		String word;
		int context = 0;
		while((word = reader.nextWord()) != null)
		{
			if(contextWordNodes[context] == null)
			{
				contextWordNodes[context] = new HashMap<String,HuffNode>();
				contextWordNodes[context].put("", new HuffNode("",0));
			}
			HuffNode contextNode = contextWordNodes[context].get(word);
			if(contextNode == null)
				contextWordNodes[context].put(word, new HuffNode(word,1));
			else
				contextNode.incFreq();

			HuffNode globalNode = globalWordNodes.get(word);
			if(globalNode != null && globalNode.getCode() != 0)
			{
				int code = globalNode.getCode();
				int codebits = huffBitcount(code);
				context = ((context<<codebits) | huffTrim(code)) & contextMask;

			}
		}

		reader.close();
	}
	
	public void seedMarkovFinish()
	{
		for(int i=0 ; i<contextTrees.length ; i++)
		{
			contextTrees[i] = HuffNode.buildTree(contextWordNodes[i]);
		}
	}
	
	public void encode(String inFile, String outFile)
	{
		TokenReader reader = new TokenReader(inFile);
		BitWriter writer = new BitWriter(outFile);
		String word = null;
		int context = 0;
//		int litCode = globalWordNodes.get("").getCode();
		while((word = reader.nextWord()) != null)
		{
			HuffNode contextNode = contextWordNodes[context].get(word);
			
			if(contextNode == null || contextNode.getCode() == 0)
			{
				int code = contextWordNodes[context].get("").getCode();
				writer.writeBig(code, huffBitcount(code));
				byte[] strbytes = word.getBytes();

//				System.out.println("ecodel "+Integer.toBinaryString(context)+": "+Integer.toBinaryString(code).substring(1)+" \""+word+"\"");
				
				for(byte b8 : strbytes)
					writer.writeBig(b8, 8);
				writer.writeBig(0, 8);
			}
			else
			{
//				System.out.println("ecode "+Integer.toBinaryString(context)+": "+Integer.toBinaryString(contextNode.getCode()).substring(1)+" \""+word+"\"");
//				System.out.println(contextNode);
				
				writer.writeBig(contextNode.getCode(), huffBitcount(contextNode.getCode()));
			}

			HuffNode globalNode = globalWordNodes.get(word);
			if(globalNode != null && globalNode.getCode() != 0)
			{
				int code = globalNode.getCode();
				int codebits = huffBitcount(code);

//				System.out.println("updatec "+Integer.toBinaryString(code).substring(1));
				context = ((context<<codebits) | huffTrim(code)) & contextMask;
			}
		}
		int litCode = contextWordNodes[context].get("").getCode();
		writer.writeBig(litCode, huffBitcount(litCode));
		writer.writeBig(0, 8);
		writer.close();
//		System.out.println();
//		System.out.println();
	}
	
	public void decode(String inFile, String outFile)
	{
		try
		{
			BitReader reader = new BitReader(inFile);
			BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
			int bit;
			int context = 0;
			HuffNode cursor = contextTrees[context];
			int dcode = 1;
			int bitcount = 0;
			while((bit = reader.readBig()) != -1)
			{
				if(bit == 0)
					cursor = cursor.getLeft();
				else
					cursor = cursor.getRight();
				dcode = (dcode<<1) | bit; 
				bitcount ++;
				String word = cursor.getValue();
				if(word != null)
				{
					if(word.equals(""))
					{
						byte[] string = new byte[256];
						int strlen = 0;
						int b8;
						while((b8 = reader.readBig8()) > 0)
							string[strlen++] = (byte)(b8 & 255);
						if(strlen == 0)
							break;
						word = new String(string, 0, strlen);

//						System.out.println("dcodel "+Integer.toBinaryString(context)+": "+Integer.toBinaryString(dcode).substring(1)+" \""+word+"\"");
						writer.write(word);
					}
					else
					{
//						System.out.println("dcode "+Integer.toBinaryString(context)+": "+Integer.toBinaryString(dcode).substring(1)+" \""+word+"\"");
						writer.write(word);
					}
					
					HuffNode globalNode = globalWordNodes.get(word);
					if(globalNode != null && globalNode.getCode() != 0)
					{
						int code = globalNode.getCode();
						int codebits = huffBitcount(code);
//						System.out.println("updatec "+Integer.toBinaryString(code).substring(1));
						context = ((context<<codebits) | huffTrim(code)) & contextMask;
					}
					
					cursor = contextTrees[context];
//					if(cursor == null)
//						cursor = globalHuffTree;
					dcode = 1;
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
}
