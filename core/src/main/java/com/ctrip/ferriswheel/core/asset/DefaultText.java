package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.core.view.LayoutImpl;
import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.api.text.Text;
import com.ctrip.ferriswheel.core.view.TextLayout;

public class DefaultText extends SheetAssetNode implements Text {
    private final TextLayout layout = new TextLayout();
    private final ValueNode content;

    protected DefaultText(String name, DefaultSheet sheet) {
        super(name, sheet.getWorkbook().getAssetManager());
        setParent(sheet);
        this.content = new ValueNode(getAssetManager(), Value.BLANK, null);

        bindChild(content);
    }

    @Override
    public LayoutImpl getLayout() {
        return layout;
    }

    @Override
    public ValueNode getContent() {
        return content;
    }

    DefaultSheet getSheet() {
        return (DefaultSheet) getParent();
    }

}
