package com.ctrip.ferriswheel.core.asset;

public class Attachment extends NamedAssetNode implements NamedAsset {
    private String contentType;
    private byte[] content;

    protected Attachment(String name, AssetManager assetManager) {
        super(name, assetManager);
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    protected EvaluationState doEvaluate(EvaluationContext context) {
        // nothing to do at present
        return EvaluationState.DONE;
    }
}
