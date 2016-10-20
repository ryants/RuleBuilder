
/*
 * BNGLInputMalformedException.java
 *
 * Created on July 24, 2005, 12:00 PM
 */

import java.beans.*;
import java.io.Serializable;

/**
 * @author matthew
 */
public class BNGLInputMalformedException extends Exception implements Serializable 
{
    private Exception hiddenException_;
    public BNGLInputMalformedException( String error )
    {
       super(error);
    }
 
}   