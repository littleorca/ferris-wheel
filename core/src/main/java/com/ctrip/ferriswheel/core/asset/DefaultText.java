package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.core.view.Layout;
import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.intf.Text;
import com.ctrip.ferriswheel.core.view.Layout;
import com.ctrip.ferriswheel.core.view.TextLayout;

public class DefaultText extends NamedAssetNode implements Text {
    private final TextLayout layout = new TextLayout();
    private final ValueNode content;

    protected DefaultText(String name, DefaultSheet sheet) {
        super(name, sheet.getWorkbook().getAssetManager());
        setParentAsset(sheet);
        this.content = new ValueNode(getAssetManager(), Value.BLANK, null);

        bindChild(content);
    }

    @Override
    public Layout getLayout() {
        return layout;
    }

    @Override
    public ValueNode getContent() {
        return content;
    }

    DefaultSheet getSheet() {
        return (DefaultSheet) getParentAsset();
    }

}
