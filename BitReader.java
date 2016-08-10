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
}
