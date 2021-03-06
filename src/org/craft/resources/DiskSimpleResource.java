package org.craft.resources;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class DiskSimpleResource extends AbstractResource
{

	private File   file;
	private byte[] bytes;

	public DiskSimpleResource(ResourceLocation location, File file, ResourceLoader loader) throws FileNotFoundException
	{
		super(location, new FileInputStream(file), loader);
		this.file = file;
	}

	@Override
	public byte[] getData()
	{
		if(bytes == null)
		{
			try
			{
				byte[] buffer = new byte[65565];
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int i;
				while((i = getInputStream().read(buffer, 0, buffer.length)) != -1)
				{
					baos.write(buffer, 0, i);
				}
				baos.flush();
				baos.close();
				bytes = baos.toByteArray();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return bytes;
	}

	public File asFile()
	{
		return file;
	}

}
