package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.text.Text;
import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.action.UpdateText;
import com.ctrip.ferriswheel.core.bean.TextData;
import com.ctrip.ferriswheel.core.view.LayoutImpl;
import com.ctrip.ferriswheel.core.view.TextLayout;

public class DefaultText extends SheetAssetNode implements Text {
    private final TextLayout layout = new TextLayout(); // FIXME layout change not tracked
    private final ValueNode content;

    protected DefaultText(String name, AssetManager assetManager) {
        super(name, assetManager);
        this.content = new ValueNode(getAssetManager(), Value.BLANK, null);

        bindChild(content);
    }

    @Override
    public TextLayout getLayout() {
        return layout;
    }

    @Override
    public ValueNode getContent() {
        return content;
    }

    @Override
    protected EvaluationState doEvaluate(EvaluationContext context) {
        UpdateText action = new UpdateText(getSheet().getName(), getName(),
                new TextData(getName(),
                        new DynamicValue(content.getFormulaString(),
                                Value.from(content.getData())), layout));
        getSheet().publicly(action, () -> {
        }); // TODO refactor revise logger
        return EvaluationState.DONE;
    }
}
