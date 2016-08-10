package compression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class HuffNode implements Comparable<HuffNode>
{
	private String s;
	private int freq; 
	private HuffNode l;
	private HuffNode r;
	public HuffNode(String s, int freq)
	{
		this.s=s;
		this.freq=freq;
	}
	public HuffNode(HuffNode l, HuffNode r)
	{
		this.l=l;
		this.r=r;
		this.freq = l.freq+r.freq;
	}
	public int compareTo(HuffNode n)
	{
		if(freq > n.freq) return 1;
		if(n==this) return 0;
		else return -1;
	}
	public void poll(int prefix, Map<String,Integer> wordCodes)
	{
		if(s!=null)
		{
			wordCodes.put(s, prefix);
		}
		if(l!=null)
		{
			l.poll((prefix<<1)|0, wordCodes);
		}
		if(r!=null)
		{
			r.poll((prefix<<1)|1, wordCodes);
		}
	}
	public String getValue()
	{
		return s;
	}
	public HuffNode getLeft()
	{
		return l;
	}
	public HuffNode getRight()
	{
		return r;
	}
	
	public static HuffNode buildTree(List<String> words, List<Integer> freqs)
	{
		TreeSet<HuffNode> sortNodes = new TreeSet<HuffNode>();
		int freqMin = 2;
		int litFreq = 0;
		for(int wordID=0 ; wordID<words.size() ; wordID++)
		{
			int freq = freqs.get(wordID);
			if(freq >= freqMin)
			{
				HuffNode node = new HuffNode(words.get(wordID), freq);
				sortNodes.add(node);
			}
			else
			{
				litFreq += freq;
			}
		}
		System.out.println("litfreq "+litFreq);
		HuffNode litNode = new HuffNode("", litFreq);
		sortNodes.add(litNode);
		
		while(sortNodes.size() > 1)
		{
			HuffNode first = sortNodes.first();
			HuffNode sec = sortNodes.higher(first);
			HuffNode merge = new HuffNode(first,sec);
			sortNodes.remove(first);
			sortNodes.remove(sec);
			sortNodes.add(merge);
		}
		return sortNodes.first();
	}
}