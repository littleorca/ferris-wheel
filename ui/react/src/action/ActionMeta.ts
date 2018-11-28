abstract class ActionMeta {
    public isLocalAction(): boolean {
        return false;
    }

    public isSheetAction(): boolean {
        return false;
    }

    public isAssetAction(): boolean {
        return false;
    }

    public targetSheet(): string | undefined {
        return undefined;
    }

    public targetAsset(): string | undefined {
        return undefined;
    }
}

export default ActionMeta;
