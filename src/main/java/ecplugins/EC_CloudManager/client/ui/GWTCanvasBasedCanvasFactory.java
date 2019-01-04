
// GWTCanvasBasedCanvasFactory.java --
//
// GWTCanvasBasedCanvasFactory.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.ui;

import com.google.gwt.canvas.client.Canvas;
import com.googlecode.gchart.client.GChartCanvasFactory;
import com.googlecode.gchart.client.GChartCanvasLite;
import org.jetbrains.annotations.Nullable;

/**
 * Factory class for canvas based chart rendering. If the browser supports
 * canvas elements, this factory creates a GWTCanvasBasedCanvasLite object to
 * handle rendering, otherwise it returns null, telling GChart to use the
 * default implementation.
 */
public class GWTCanvasBasedCanvasFactory
    implements GChartCanvasFactory
{

    //~ Methods ----------------------------------------------------------------

    @Nullable
    @Override
    public GChartCanvasLite create()
    {

        if (Canvas.isSupported()) {
            return new GWTCanvasBasedCanvasLite();
        }
        else {
            return null;
        }
    }
}
