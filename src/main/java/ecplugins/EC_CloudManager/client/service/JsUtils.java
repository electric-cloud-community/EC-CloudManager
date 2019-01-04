
// JsUtils.java --
//
// JsUtils.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.jetbrains.annotations.Nullable;

import com.google.gwt.core.client.JsArray;

public class JsUtils
{

    //~ Constructors -----------------------------------------------------------

    private JsUtils()
    {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Convert a JavaScript array of actual parameters to a POJO string map.
     *
     * @param   actuals  The javascript array.
     *
     * @return  A map of parameter names to values.
     */
    @NotNull public static Map<String, String> jsArrayToMap(
            @Nullable JsArray<ActualParameterImpl> actuals)
    {
        Map<String, String> result = new HashMap<String, String>();

        if (actuals == null) {
            return result;
        }

        int len = actuals.length();

        for (int i = 0; i < len; ++i) {
            ActualParameterImpl actual = actuals.get(i);

            result.put(actual.getName(), actual.getValue());
        }

        return result;
    }

    /**
     * Convert a POJO string map to a JavaScript array of actual parameters.
     *
     * @param  values   The name to value map.
     * @param  actuals  The javascript array.
     */
    public static void mapToJsArray(
            @NotNull Map<String, String>    values,
            @NotNull JsArray<ActualParameterImpl> actuals)
    {
        actuals.setLength(0);

        for (Map.Entry<String, String> entry : values.entrySet()) {
            ActualParameterImpl actual = ActualParameterImpl.create(
                    entry.getKey(),
                    entry.getValue());

            actuals.push(actual);
        }
    }
}
