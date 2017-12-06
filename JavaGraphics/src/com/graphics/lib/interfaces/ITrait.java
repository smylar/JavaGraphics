package com.graphics.lib.interfaces;

import java.util.Observer;

public interface ITrait extends Observer {
    void setParent(ICanvasObject parent);
}
