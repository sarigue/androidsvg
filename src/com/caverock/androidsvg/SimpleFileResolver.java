package com.caverock.androidsvg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;

public class SimpleFileResolver extends SimpleAssetResolver
{

	public SimpleFileResolver(AssetManager assetManager)
	{
		super(assetManager);
	}

	private HashMap<String, Typeface> mTypeFaceList = new HashMap<String, Typeface>();
	
	public void registerTypeface(String fontFamily, Typeface typeface)
	{
		registerTypeface(fontFamily, 0, null, typeface);
	}

	public void registerTypeface(String fontFamily, int fontWeight, Typeface typeface)
	{
		registerTypeface(fontFamily, fontWeight, null, typeface);
	}

	public void registerTypeface(String fontFamily, String fontStyle, Typeface typeface)
	{
		registerTypeface(fontFamily, 0, fontStyle, typeface);
	}

	public void registerTypeface(String fontFamily, int fontWeight, String fontStyle, Typeface typeface)
	{
		String key = getKey(fontFamily, fontWeight, fontStyle);
		mTypeFaceList.put(key, typeface);
	}

	@Override
	public Typeface resolveFont(String fontFamily, int fontWeight, String fontStyle)
	{
		String key = getKey(fontFamily, fontWeight, fontStyle);
		if (mTypeFaceList.containsKey(key))
		{
			return mTypeFaceList.get(key);
		}
		return super.resolveFont(fontFamily, fontWeight, fontStyle);
	}
	
	@Override
	public Bitmap resolveImage(String filename)
	{
		if (filename == null)
		{
			return null;
		}
		
		// Filename is URL
		
		if (filename.startsWith("http://") || filename.startsWith("https://"))
		{
			try
			{
				InputStream is = (InputStream)new URL(filename).getContent();
				Bitmap b = BitmapFactory.decodeStream(is);
				is.close();
				return b;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return null;
			}
		}
		
		// Filename is a file on terminal
		
		File f = new File(filename);
		if (f.exists())
		{
			try
			{
				return BitmapFactory.decodeFile(f.getAbsolutePath());
			}
			catch(OutOfMemoryError e)
			{
				e.printStackTrace();
				return super.resolveImage(filename);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return super.resolveImage(filename);
			}
		}
		
		// Other
		
		return super.resolveImage(filename);
	}
	
	
	@Override
	public boolean isFormatSupported(String mimeType)
	{
		return super.isFormatSupported(mimeType);
	}
	
	
	
	private String getKey(String fontFamily, int fontWeight, String fontStyle)
	{
		String key = fontFamily;
		if (fontWeight > 0)
		{
			key += "-"+String.valueOf(fontWeight);
		}
		if (fontStyle != null && ! fontStyle.equals(""))
		{
			key += "-"+fontStyle;
		}
		return key;
	}
	
}
