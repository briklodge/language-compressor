package compression;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class BitReader 
{
	InputStream reader;
	int accum;
	int accumcount;
	
	public BitReader(String fileName)
	{
		try
		{
			File f = new File(fileName);
			reader = new FileInputStream(f);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public int readBig()
	{
		int bit = -1;
		try
		{
			if(accumcount == 0)
			{
				accum = reader.read();
				if(accum < 0) return -1;
				accumcount = 8;
			}
//			System.out.println("r "+accum+" "+accumcount);
			bit = (accum>>(accumcount-1)) & 1;
			accumcount --;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return bit;
	}
	
	public int readBig8()
	{
		int b8 = 0;
		for(int i=0 ; i<8 ; i++)
		{
			int b = readBig();
			if(b < 0) return -1;
			b8 = (b8<<1) | b;
		}
		return b8;
	}
}
