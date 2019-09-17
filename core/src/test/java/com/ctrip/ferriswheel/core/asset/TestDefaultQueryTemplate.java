package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.query.DataQuery;
import com.ctrip.ferriswheel.common.variant.DefaultParameter;
import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// TODO improve cases
public class TestDefaultQueryTemplate extends TestCase {
    public void testSetAndGet() {
        DefaultWorkbook assetManager = new DefaultWorkbook(new DefaultEnvironment(null));

        TableAutomatonInfo.QueryTemplateInfo query = new TableAutomatonInfo.QueryTemplateInfo();
        query.setScheme("test");
        query.addBuiltinParam("b1", new DefaultParameter("b1", new DynamicValue(Value.str("test1"))));

        DefaultQueryTemplate template = new DefaultQueryTemplate(assetManager, query);

        assertEquals("test", template.getScheme());

        assertEquals("test1", template.getBuiltinParam("b1").getValue().strValue());
        Set<String> names = template.getBuiltinParamNames();
        assertEquals(1, names.size());
        assertEquals("b1", names.iterator().next());
    }

    public void testRenderQuery() {
        DefaultWorkbook assetManager = new DefaultWorkbook(new DefaultEnvironment(null));
        TableAutomatonInfo.QueryTemplateInfo querySolution = new TableAutomatonInfo.QueryTemplateInfo();

        querySolution.setScheme("test");
        querySolution.addBuiltinParam("p1", new DefaultParameter("p1", new DynamicValue("NOW()")));
        querySolution.addBuiltinParam("p2", new DefaultParameter("p2", new DynamicValue(Value.dec(2))));

        DefaultQueryTemplate template = new DefaultQueryTemplate(assetManager, querySolution);

        Map<String, Variant> userParameters = new HashMap<>();
        userParameters.put("p2", Value.dec(3));
        userParameters.put("p3", Value.dec(10));

        DataQuery query = template.renderQuery(userParameters);
        assertEquals("test", query.getScheme());
    }
}
