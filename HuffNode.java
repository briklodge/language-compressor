package compression;

import java.util.ArrayList;
import java.util.Map;

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
	public void poll(int prefix, Map<String,Integer> idMap, ArrayList<Integer> result)
	{
		if(s!=null)
		{
			int id = idMap.get(s);
			while(result.size() < id+1) result.add(0);
			result.set(id, prefix);
		}
		if(l!=null)
		{
			l.poll((prefix<<1)|0, idMap, result);
		}
		if(r!=null)
		{
			r.poll((prefix<<1)|1, idMap, result);
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
}