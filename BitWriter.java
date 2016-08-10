package compression;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class BitWriter
{
	OutputStream writer;
	int accum;
	int accumcount;
	
	public BitWriter(String fileName)
	{
		try
		{
			File f = new File(fileName);
			writer = new FileOutputStream(f);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void writeBig(int bits, int bitcount)
	{
		try
		{
			while(accumcount + bitcount >= 8)
			{
				int newbits = 8-accumcount;
				int shifted = bits>>(bitcount-newbits);
				accum |= shifted & ((1<<newbits)-1);
				accumcount += newbits;
				
//				System.out.println("w "+Integer.toBinaryString(accum)+" "+accumcount);
				writer.write(accum&255);
				accum = 0;
				accumcount = 0;
				bitcount -= newbits;
			}
			accum |= ((bits&((1<<bitcount)-1))<<(8-accumcount-bitcount));
			accumcount += bitcount;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void close()
	{
		try
		{
//			System.out.println("w "+Integer.toBinaryString(accum)+" "+accumcount);
			writer.write(accum&255);
			accum = 0;
			accumcount = 0;
			writer.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}