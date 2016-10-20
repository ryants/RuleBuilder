
import java.awt.*; // For Graphics

public class BNGWidget 
{
	protected BNGWidgetPanel panel;
	protected BNGLabel label;
	protected boolean selected;
	protected int height = 0;
	protected int width = 0;
	protected int x = 0;
	protected int y = 0;
	protected int x_offset = 0;
	protected int y_offset = 0;

	public BNGWidget( BNGWidgetPanel panel )
	{
		this.panel = panel;
		if (!this.getClass().getName().contentEquals("BNGLabel")) setLabel("w");
		
		selected = false;
	}

	public void display(Component c, Graphics2D g2d)
	{

	}

	void setSelected( boolean selected )
	{
		this.selected = selected;
	}

	public boolean contains(int x, int y )
	{
		return x > this.x && x < this.x + width && y > this.y && y < this.y + height;
	}


	public void setWidth(int width){ this.width = width; }
	public void setHeight(int height)
	{ 
		this.height = height; 
	    label.setY( y+height-label.y_offset );	
	}
	
	public void setX(int x)
	{ 
		this.x = x; 
        label.setX( x+width-label.x_offset );
	}
	
	public void setY(int y)
	{ 
		this.y = y; 
        label.setY( y+height-label.y_offset );
	}

	public void setLabel( BNGLabel label )
	{
		this.label = label;
	}

	public void setLabel( String new_label )
	{
		setLabel( new_label, false );
	}

	public void setLabel( String new_label, boolean peer )
	{
		//containing_panel.removeFlickrLabel( this.label );
		if ( this.label == null )
		{
			this.label = new BNGLabel( new_label, this, x, y, panel, false );
			this.label.x_offset = 0;
			this.label.y_offset = height+10;
		}
		else
		{
			this.label.setString( new_label );  // = new FlickrLabel( new_label, getX(), getY()+getHeight()+12, containing_panel );
			this.label.x_offset = 0;
			this.label.y_offset = height+10;
		}            
		
		if (panel != null ) panel.repaint();
	}
	
	public BNGLabel getLabel()
	{
		return label;
	}
	
	public int getHeightWithLabel()
	{
            int label_height = 0;
            if ( label != null ) label_height = label.height*2; //*2 to account for the label offset 
            return height+label_height;
	}

	public int getWidthWithLabel()
	{
            int label_width = 0;
            if ( label != null ) label_width = label.width; 
            return height > label_width ? width : label_width;
	}
}
