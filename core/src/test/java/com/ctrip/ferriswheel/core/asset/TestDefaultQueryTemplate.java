package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.variant.impl.DynamicVariantImpl;
import com.ctrip.ferriswheel.core.bean.ValueRule;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo;
import com.ctrip.ferriswheel.common.variant.impl.Value;
import com.ctrip.ferriswheel.common.query.DataQuery;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.variant.VariantRule;
import com.ctrip.ferriswheel.common.variant.VariantType;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// TODO improve cases
public class TestDefaultQueryTemplate extends TestCase {
    public void testSetAndGet() {
        DefaultAssetManager assetManager = new DefaultAssetManager();

        TableAutomatonInfo.QueryTemplateInfo query = new TableAutomatonInfo.QueryTemplateInfo();
        query.setScheme("test");
        query.addBuiltinParam("b1", new DynamicVariantImpl(Value.str("test1")));
        query.addUserParamRule("b1", new ValueRule());

        DefaultQueryTemplate template = new DefaultQueryTemplate(assetManager, query);

        assertEquals("test", template.getScheme());

        assertEquals("test1", template.getBuiltinParam("b1").strValue());
        Set<String> names = template.getBuiltinParamNames();
        assertEquals(1, names.size());
        assertEquals("b1", names.iterator().next());

        VariantRule rule = template.getUserParamRule("b1");
        assertNotNull(rule);

        names = template.getUserParamNames();
        assertEquals(1, names.size());
        assertEquals("b1", names.iterator().next());
    }

    public void testRenderQuery() {
        DefaultAssetManager assetManager = new DefaultAssetManager();
        TableAutomatonInfo.QueryTemplateInfo querySolution = new TableAutomatonInfo.QueryTemplateInfo();

        querySolution.setScheme("test");
        querySolution.addBuiltinParam("p1", new DynamicVariantImpl("NOW()"));
        querySolution.addBuiltinParam("p2", new DynamicVariantImpl(Value.dec(2)));

        querySolution.addUserParamRule("p2", new ValueRule()
                .type(VariantType.DECIMAL)
                .nullable(false)
                .allowValue(Value.dec(2))
                .allowValue(Value.dec(3)));

        querySolution.addUserParamRule("p3", new ValueRule()
                .type(VariantType.DECIMAL)
                .nullable(true));

        DefaultQueryTemplate template = new DefaultQueryTemplate(assetManager, querySolution);

        Map<String, Variant> userParameters = new HashMap<>();
        userParameters.put("p2", Value.dec(3));
        userParameters.put("p3", Value.dec(10));

        DataQuery query = template.renderQuery(userParameters);
        assertEquals("test", query.getScheme());
    }
}
