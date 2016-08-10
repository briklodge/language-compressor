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
	private int code;
	
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
	public void incFreq()
	{
		freq ++;
	}
	public int compareTo(HuffNode n)
	{
		if(freq > n.freq) return 1;
		if(n==this) return 0;
		else return -1;
	}
	public void updateCode(int prefix)
	{
		if(s!=null)
		{
//			wordCodes.put(s, prefix);
			code = prefix;
		}
		if(l!=null)
		{
			l.updateCode((prefix<<1)|0);
		}
		if(r!=null)
		{
			r.updateCode((prefix<<1)|1);
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
	public int getCode()
	{
		return code;
	}
	public String toString()
	{
		if(s!=null) return s;
		else return "{"+l.toString()+","+r.toString()+"}";
	}
	
	public static HuffNode buildTree(Map<String, HuffNode> wordNodes)
	{
		TreeSet<HuffNode> sortNodes = new TreeSet<HuffNode>();
		int freqMin = 2;
//		int litFreq = 0;
		HuffNode litNode = wordNodes.get("");
		for(HuffNode node : wordNodes.values())
		{
			if(node == litNode) continue;
			
			int freq = node.freq;
			if(freq >= freqMin)
			{
				sortNodes.add(node);
			}
			else
			{
//				litFreq += freq;
				litNode.freq += freq;
			}
		}
//		System.out.println("litfreq "+litFreq);
//		HuffNode litNode = new HuffNode("", litFreq);
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
		sortNodes.first().updateCode(1);
		return sortNodes.first();
	}
}