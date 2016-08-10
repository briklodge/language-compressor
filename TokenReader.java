package compression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TokenReader
{
	BufferedReader reader;
	char[] accum = new char[255];
	int accumcount = 0;
	int start = 0;
	int end = 1;
	public TokenReader(String fileName)
	{
		try
		{
			File f = new File(fileName);
			reader = new BufferedReader(new FileReader(f));
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	public String nextWord()
	{
		String word = null;
		try
		{
			while(accumcount < 255)
			{
				int r = reader.read();
				if(r < 0 || r > 255)
					return null;
				if(accumcount > 0 && !Character.isAlphabetic(r) || !Character.isAlphabetic(accum[0]))
				{
					word = new String(accum, 0, accumcount);
					accum[0] = (char)r;
					accumcount = 1;
					break;
				}
				else
					accum[accumcount++] = (char)r;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return word;
	}
	public void close()
	{
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}