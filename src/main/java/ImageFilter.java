import javax.imageio.ImageIO; // For ImageIO
import javax.swing.filechooser.FileFilter; // For filtering files by extension
import java.io.*; // For File

class ImageFilter extends FileFilter 
{
   
	
	String image_types_description = ""; // Just a list of image extensions supported by imageio
	String [] image_extensions; // array of image extensions supported by imageio
	
	ImageFilter()
	{
		 ImageIO.scanForPlugins();
		
		// Determine supported image types
		image_extensions = ImageIO.getWriterFileSuffixes();
		image_types_description = "";
	
		for ( int i = 0; i < image_extensions.length; i++ ) image_types_description += " *." + image_extensions[i]; 
	}
		
	public boolean accept(File f) {
	if (f.isDirectory())
	return true;
	String s = f.getName();
	int i = s.lastIndexOf('.');

	String ext = ""; // file extension
	
	if (i > 0 && i < s.length() - 1) ext = s.substring(i + 1);
			
	for ( int itr = 0; itr < image_extensions.length; itr++ )
	{
		if ( image_extensions[itr].equalsIgnoreCase(ext) ) return true;
	}

	return false;
	}

	public String getDescription() 
	{
		
		
		return image_types_description;
	}
	}