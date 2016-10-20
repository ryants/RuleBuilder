/*
	 * BNGLabel.java
	 *
	 * Created on December 17, 2005, 1:14 PM by Matthew
	 */

	/**
	 *
	 * @author  matthew
	 */
	import javax.swing.*;
	import java.awt.*;
	import java.awt.event.*; // For keyboard interactions
	import javax.swing.text.DefaultCaret; // For Caret
	import java.util.*; // For iterator
	import java.awt.Color;
	import java.awt.Dimension;
	import java.awt.Font;
	import java.awt.Graphics;
	import java.awt.Graphics2D;
	import java.awt.Shape;
	import java.awt.event.MouseAdapter;
	import java.awt.event.MouseEvent;
	import java.awt.event.MouseMotionAdapter;
	import java.awt.event.WindowAdapter;
	import java.awt.event.WindowEvent;
	import java.awt.font.FontRenderContext;
	import java.awt.font.TextHitInfo;
	import java.awt.font.TextLayout;
	import java.awt.geom.AffineTransform;
	import java.awt.geom.Rectangle2D;
	import java.io.Serializable;
	import java.io.*; // For file IO in objectWrite and Read
	import javax.swing.event.*; // For document listener

//	 Have to make thse things extend widget... need to figure out keyboard input without
//	 using JComponent 
	public class BNGLabel extends BNGWidget
	{
			public Font font  = new Font("Arial", Font.PLAIN, 12);
			
	        private class BlinkTimerListener implements ActionListener
	        {
	        
	        BlinkTimerListener()
	        {
	        }
	        
	        public void actionPerformed( ActionEvent evt )
	            {
	                if ( editable )
	                {
	                if (cursor_visible)
	                {
	                    cursor_visible = false;
	                    caret_color = Color.BLACK;
	                }
	                else
	                {
	                    cursor_visible = true;
	                    caret_color = background_color;
	                }
	                
	                panel.repaint();
	                blink_timer.restart();
	                }
	            }
	    }

	    private class DialogCloser extends WindowAdapter implements ActionListener 
	    {
	    
	    public void actionPerformed(ActionEvent event) 
	    {
	        handleClose();
	    }
	    
	    public void windowClosing(WindowEvent e)
	    {
		handleClose();
	    }
	    
	    private void handleClose()
	    {
	        String text = text_field.getText();
	        
		if ( text.length() == 0 )
	        {
	            return;
	        }
	        
	        contents = text;
	        layout = new TextLayout( contents, font, frc );
	        hitInfo = layout.getNextRightHit( layout.getCharacterCount()-1 );
	        
	        dialog.setVisible( false );
	        dialog.dispose();
	        
	        panel.repaint();
	    }
	  }
	   
	    private class TextFieldChangeListener  implements DocumentListener 
	    {
	    
	    public void changedUpdate(DocumentEvent e) 
	    {
	        handleChange();
	    }
	    
	    public void insertUpdate(DocumentEvent e) 
	    {
	        handleChange();
	    }
	    
	    public void removeUpdate(DocumentEvent e) 
	    {
	        handleChange();
	    }
	    
	    private void handleChange()
	    {
	        if ( text_field.getText() != null )
	        {
	            if ( text_field.getText().length() != 0 )
	            {
	                done_button.setEnabled( true );
	            }
	            else
	            {
	                done_button.setEnabled( false );
	            }
	        }
	        else
	        {
	            done_button.setEnabled( false );
	        }
	    }
	  }
	    
	    public class KeyboardControl implements KeyListener 
	    {
	        KeyboardControl(){}
	        
	        public void keyPressed(KeyEvent evt) 
	        {
	        	
	            int key = evt.getKeyCode();
	            if ( key == KeyEvent.VK_BACK_SPACE ) 
	            {
	                TextHitInfo current_hit = hitInfo;
	                removeCharacterAt( hitInfo.getInsertionIndex()-1 );
	                
	                hitInfo = layout.getNextLeftHit(hitInfo.getInsertionIndex());
	                if ( hitInfo == null )
	                {
	                    hitInfo = current_hit;
	                }
	                
	                if (contents.length() != 0 )
	                {
	                    layout = new TextLayout(contents,font,frc);
	                }
	                else
	                {
	                    layout = new TextLayout("...",font,frc);
	                }
	                
	                
	                panel.repaint();
	            }
	            else if ( key == KeyEvent.VK_DELETE ) 
	            {
	                removeCharacterAt( hitInfo.getInsertionIndex() );
	                if (contents.length() != 0 )
	                {
	                    layout = new TextLayout(contents,font,frc);
	                }
	                else
	                {
	                    layout = new TextLayout("...",font,frc);
	                }
	                
	                panel.repaint();
	            }
	            else if ( key == KeyEvent.VK_RIGHT ) 
	            {
	                TextHitInfo current_hit = hitInfo;
	                hitInfo = layout.getNextRightHit(hitInfo.getInsertionIndex());
	                if (hitInfo == null)
	                hitInfo = current_hit;
	                panel.repaint();
	                
	                caret_position++;
	                
	                panel.repaint();
	            }
	            else if ( key == KeyEvent.VK_LEFT ) 
	            {
	                TextHitInfo current_hit = hitInfo;
	                hitInfo = layout.getNextLeftHit(hitInfo.getInsertionIndex());
	                if (hitInfo == null)
	            	hitInfo = current_hit;
	                panel.repaint();
	                
	                caret_position--;
	                
	                panel.repaint();
	            }
	            
	            
	        }
	        
	        public void keyReleased(KeyEvent evt) {
	            
	        }
	        
	        public void keyTyped(KeyEvent evt) 
	        {
	                // Make sure the char is alphanumeric, underscore or tilde
	                
	                if ( evt.getKeyChar() >= 'a' && evt.getKeyChar() <= 'z' 
		             || evt.getKeyChar() >= 'A' && evt.getKeyChar() <= 'Z'
	                     || evt.getKeyChar() >= '0' && evt.getKeyChar() <= '9'
	                     || evt.getKeyChar() == '_'
	                     || evt.getKeyChar() == '~' 
	                     || evt.getKeyChar() == '.')
	                {
	                    if ( layout == null )
	                    {
	                        addCharacterAt( 0,  evt.getKeyChar() );
	                        layout = new TextLayout(contents,font,frc);
	                        hitInfo = layout.getNextRightHit(0);
	                        panel.repaint();
	                    }
	                    else //if ( contents.length() != 0 )
	                    { 
	                        addCharacterAt( hitInfo.getCharIndex(), evt.getKeyChar() );
	                        layout = new TextLayout(contents,font,frc);
	                        hitInfo = layout.getNextRightHit(hitInfo.getInsertionIndex());
	                        if (hitInfo == null)
	                        hitInfo = layout.getNextLeftHit(1);
	                        panel.repaint();
	                    }
	                }
	        }
	    }
	    
	    
	    class MouseHandler extends MouseAdapter 
	    {
	        
	        
	    public void mouseClicked(MouseEvent e) 
	    {
	      if ( !on ) return;  
	        
	      caretColor = caret_color;
	      hit1 = getHitLocation(e.getX(), e.getY());
	      hit2 = hit1;
	      panel.repaint();
	    }

	    public void mousePressed(MouseEvent e) 
	    {
	      if ( !on ) return;  
	        
	      caretColor = caret_color;
	      hit1 = getHitLocation(e.getX(), e.getY());
	      hit2 = hit1;
	      panel.repaint();
	    }

	    public void mouseReleased(MouseEvent e) 
	    {
	      if ( !on ) return;  
	        
	      hit2 = getHitLocation(e.getX(), e.getY());
	      panel.repaint();
	    }
	  }

	  class MouseMotionHandler extends MouseMotionAdapter 
	  {
	        public void mouseExited(MouseEvent e)
	        {
	        }
	        
	        public void mouseEntered(MouseEvent e)
	        {
	        }
	      
	    public void mouseDragged(MouseEvent e) {
	      caretColor = panel.getBackground();
	      hit2 = getHitLocation(e.getX(), e.getY());
	      panel.repaint();
	    }
	  }
	    
	    // Serialization explicit version
	    private static final long serialVersionUID = 1;
	  
	    private String contents;
	    private String old_contents;
	    
	    int caret_position = 0;
	    private boolean on = true;
	    transient private KeyboardControl keyboard_control = new KeyboardControl();
	    transient private FontRenderContext frc = new FontRenderContext(null, false, false);
	    transient private TextLayout layout;
	    private int hit1, hit2;
	    transient private Color caretColor;
	    transient private TextHitInfo hitInfo;
	    transient private Rectangle2D rect;
	    private float rx, ry, rw, rh;
	    transient private javax.swing.Timer blink_timer;
	    transient private BlinkTimerListener blink_timer_listener = new BlinkTimerListener();
	    transient private Color text_color = Color.BLACK;
	    transient private Color caret_color = Color.BLACK;
	    private boolean cursor_visible = true;
	    transient private Color background_color = new Color( 0, 0, 255, 5); // Light Blue
	    transient private JTextField text_field;
	    transient private JButton done_button;
	    transient private JDialog dialog;
	    private BNGWidget owner;
	    
	    private boolean editable;
	    
	    /** Creates a new instance of BNGLabel */
	    public BNGLabel(String label, BNGWidget owner, int x, int y, BNGWidgetPanel panel, boolean on) 
	    { 
	    	super(panel);
	    	
	        this.on = on;
	        this.setOwner(owner);
	        
	        setX( x );
	        setY( y );
	        
	        contents = label;
	        if ( 0 != contents.length() )
	        {
	            layout = new TextLayout(contents, font, frc);
	        
	            hitInfo = layout.getNextRightHit( layout.getCharacterCount()-1 );
	        }
	        
	        blink_timer = new javax.swing.Timer(750, blink_timer_listener );
	        
	    }
	    
	    KeyboardControl getKeyboardListener()
	    {
	        return keyboard_control;
	    }
	    
	    public boolean contains( int mouse_x, int mouse_y )
	    {
		    return mouse_x > getX()-5 && mouse_x < getX() + getWidth() + 5 && mouse_y > getY() - 5 && mouse_y < getY() + getHeight() + 5;  
	        // pad contains box by 5 to make it easier to click single character labels
	    }
	    
	    public void setX( int x )
	    {
	        super.x = x;
	    }
	    
	    public void setY( int y)
	    {
	        super.y = y;

	    }
	    
	    public int getWidth()
	    {
	        int value = 0;
	        if ( layout == null && contents.length() != 0)
	        {
	            layout = new TextLayout(contents,font,frc);
	            
	        }

	        value = (int)layout.getBounds().getWidth();
	        
	        return value;
	    }
	    
	    public int getHeight()
	    {
	        int value = 0;
	        if ( layout == null && contents.length() != 0)
	        {
	            layout = new TextLayout(contents,font,frc);
	            
	        }

	        value = (int)layout.getBounds().getHeight();
	        return value;
	    }
	    
	    public int getHitLocation(int mouseX, int mouseY) 
	    {
	    	hitInfo = layout.hitTestChar(mouseX, mouseY, rect);
	    	return hitInfo.getInsertionIndex();
	  }
	    
	    public void setEditable( boolean editable )
	    {
	        if ( editable )
	        {
	        	// Remember current string in case they try to make it empty
	        	old_contents = this.contents;
	        	
	            // Get keyboard focus
	        	panel.requestFocus();
	        	panel.removeKeyListener( panel.getKeyboardListener() );
	            panel.addKeyListener( keyboard_control );

	            
	            blink_timer.start();
	        }
	        else
	        { 
	            // Give keyboard focus back
	            panel.removeKeyListener( keyboard_control );
	            panel.addKeyListener( panel.getKeyboardListener() );
	            blink_timer.stop();
	            
	            if ( contents.equals("") ) contents = old_contents;  
	            owner.setLabel( contents );
	        }
	        
	        this.editable = editable;
	    }
	    
	    public void display( Component c, Graphics2D g2d )
	    {	        
	            if ( editable )
	            {
	                int bx = getX() - 5;
	                int by = getY() - 5;
	                int bwidth = getWidth() + 10;
	                int bheight = getHeight() + 10;
	                
	                g2d.setColor( Color.black );
	                g2d.drawRect( bx, by, bwidth, bheight );
	                g2d.setColor( background_color );
	                g2d.fillRect( bx, by, bwidth, bheight );
	                
	                int index = hitInfo.getInsertionIndex();
	     
	                System.out.println("InsetionIndex: " + index);
	                System.out.println("Contents Length: " + layout.getCharacterCount());
	                Shape[] carets = layout.getCaretShapes(index);
	            
	                for (int i = 0; i < carets.length; i++) 
	                {
	                    if (carets[i] != null)
	                    {
	                        AffineTransform at = AffineTransform.getTranslateInstance(getX(), getY()+10);
	                        Shape shape = at.createTransformedShape(carets[i]);
	                        g2d.setColor( caret_color );
	                        //g2.setStroke(caretStrokes[i]);
	                        g2d.draw(shape);
	                        g2d.setColor( text_color );
	                    }
	                }
	            }
	            
	            if ( selected )
	            {
	                g2d.setColor( Color.BLUE );
	            }
	            else
	            {
	                g2d.setColor( text_color );
	            }
	            
	            //layout.draw( g2d, getX()-(int)layout.getBounds().getX() , getY()-(int)layout.getBounds().getY() );
	            layout.draw( g2d, getX(), getY()+10);
	            
	            g2d.setColor( g2d.getBackground() );

	    }
	    
	    public void removeCharacterAt(int pos) 
	    {
	        
	        String new_string = new String();
	                for ( int i = 0; i < contents.length(); i++ )
	                {
	                    if ( i == pos )
	                    {
	                        continue;
	                    }
	                    new_string += contents.charAt( i );
	                }
	                
	                
	        contents = new_string;
	        
	    }
	    
	    public void addCharacterAt(int pos, char c )
	    {
	                String new_string = new String();
	                if ( contents.length() == 0 || pos >= contents.length() )
	                {
	                    contents += c;
	                    return;
	                }
	                
	                for ( int i = 0; i < contents.length(); i++ )
	                {
	                    if ( i == pos )
	                    {
	                        new_string += c;
	                    }
	                    new_string += contents.charAt( i );
	                }
	                
	                contents = new_string;
	                
	    }
	   
	    public int getX() 
	    {
	    	//return getOwner().x +(int)layout.getBounds().getX() + x_offset;
	    	return getOwner().x + x_offset;
	    }
	    
	    public int getY()
	    {
	    	//return getOwner().y +(int)layout.getBounds().getY() + y_offset;
	    	return getOwner().y + y_offset;
	    }
	    
	    public String getString() 
	    {
	        return contents;
	    }
	    
	    public void setString( String string ) 
	    {
	        setString( string, false );
	    }
	    
	    public void setString( String string, boolean peer ) 
	    {
	        contents = string;
	         if ( 0 != contents.length() )
	        {
	            layout = new TextLayout(contents, font, frc);
	        
	            //hitInfo = layout.getNextRightHit( layout.getCharacterCount()-1 );
	        }
	        
	    }
	    
	    // Lots of transient data members to recreate after a clone operation
	   private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	   {
	    stream.defaultReadObject();
	    font = new Font("Arial", Font.PLAIN, 12);
	    frc = new FontRenderContext(null, false, false);
	    
	    text_color = Color.BLACK;
	    caret_color = Color.BLACK;
	    cursor_visible = true;
	    background_color = new Color( 0, 0, 255, 5); // Light Blue
	    keyboard_control = new KeyboardControl();
	    
	    if (contents.length() != 0)
	    {
	        layout = new TextLayout(contents,font,frc);
	        hitInfo = layout.getNextRightHit( layout.getCharacterCount()-1 );
	    }
	        
	        blink_timer_listener = new BlinkTimerListener();
	        blink_timer = new javax.swing.Timer(750, blink_timer_listener );
	        //blink_timer.start();
	   
	   }
	    
	   public void setContainingPanel(BNGWidgetPanel panel) 
	   {

	       super.panel = panel;
	
	    }
	   
	    public void setFont(Font font) 
	    {
	        this.font = font;
	        if (contents.length() != 0) layout = new TextLayout(contents, font, frc);
	    }

	    public void setOn() 
	    {
	        on = true;
	    }

	    public void setOff() 
	    {
	        on = false;
	    }

	    public int getXOffset() 
	    {
	        return x_offset;
	    }

	    public void setXOffset(int label_x_offset) 
	    {
	        super.x_offset = label_x_offset;
	    }

	    public int getYOffset() 
	    {
	        return y_offset;
	    }

	    public void setYOffset(int label_y_offset) 
	    {
	        super.y_offset = label_y_offset;
	    }

	    public BNGWidget getOwner() 
	    {
	        return owner;
	    }

	    public void setOwner(BNGWidget owner) 
	    {
	        this.owner = owner;
	    }	    
	}
