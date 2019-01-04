
// JsUtilsTest.java --
//
// JsUtilsTest.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jetbrains.annotations.NonNls;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONArray;

public class JsUtilsGwtTest
    extends AbstractTestSupport
{

    //~ Static fields/initializers ---------------------------------------------

    @NonNls private static final String PARAMETERS =
        "[{\"name\":\"n1\", \"value\":\"v1\"},"
            + "{\"name\":\"n2\", \"value\":\"v2\"}]";

    //~ Methods ----------------------------------------------------------------

    public void testJsArrayToMap_empty()
    {
        JsArray<ActualParameterImpl> actuals = JsonUtils.safeEval("[]");

        assertTrue("empty", JsUtils.jsArrayToMap(actuals)
                                   .isEmpty());
    }

    public void testJsArrayToMap_nonEmpty()
    {
        JsArray<ActualParameterImpl> actuals = JsonUtils.safeEval(PARAMETERS);
        Map<String, String>    map     = JsUtils.jsArrayToMap(actuals);

        assertEquals("size", 2, map.size());
        assertEquals("param1", "v1", map.get("n1"));
        assertEquals("param1", "v2", map.get("n2"));
    }

    public void testMapToJsArray_empty()
    {
        JsArray<ActualParameterImpl> array = JsonUtils.safeEval("[]");

        JsUtils.mapToJsArray(Collections.<String, String>emptyMap(), array);
        assertEquals("result", "[]", new JSONArray(array).toString());
    }

    public void testMapToJsArray_nonEmpty()
    {
        JsArray<ActualParameterImpl> array  = JsonUtils.safeEval("[]");
        Map<String, String>    values = new TreeMap<String, String>();

        values.put("n1", "v1");
        values.put("n2", "v2");
        JsUtils.mapToJsArray(values, array);
        assertEquals("result", PARAMETERS, new JSONArray(array).toString());
    }

    public void testToList_empty()
    {
        JsArray<ActualParameterImpl> array = JsonUtils.safeEval("[]");
        List<ActualParameterImpl>    list  = ecinternal.client.request.JsUtils.toList(
                array);

        assertTrue("empty", list.isEmpty());
    }

    public void testToList_nonEmpty()
    {
        JsArray<ActualParameterImpl> array = JsonUtils.safeEval(PARAMETERS);
        List<ActualParameterImpl>    list  = ecinternal.client.request.JsUtils.toList(
                array);

        assertEquals("size", 2, list.size());
        assertEquals("param1 name", "n1", list.get(0).getName());
        assertEquals("param2 name", "n2", list.get(1).getName());
        assertEquals("param1 value", "v1", list.get(0).getValue());
        assertEquals("param2 value", "v2", list.get(1).getValue());
    }
}
