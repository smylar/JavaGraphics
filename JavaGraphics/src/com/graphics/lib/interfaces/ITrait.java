package com.graphics.lib.interfaces;

import java.util.Observer;

public interface ITrait extends Observer {
    
    //@Deprecated //looking to enforce final parent entries, added default so it can be removed from objects that do that
    //default void setParent(ICanvasObject parent) {};
}
