import SheetAsset from "./SheetAsset";
import Layout from "./Layout";

class Sheet {
    public name: string;
    public assets: SheetAsset[];
    public layout: Layout;

    public static deserialize(input: any): Sheet {
        const name = input.name;
        const assets = [];
        if (typeof input.assets !== 'undefined') {
            for (const inputAsset of input.assets) {
                assets.push(SheetAsset.deserialize(inputAsset));
            }
        }
        const layout = typeof input.layout !== 'undefined' ?
            Layout.deserialize(input.layout) : undefined;
        return new Sheet(name, assets, layout);
    }

    constructor(name: string = '',
        assets: SheetAsset[] = [],
        layout: Layout = new Layout()) {
        this.name = name;
        this.assets = assets;
        this.layout = layout;
    }

    public getAssetByName(name: string): SheetAsset | null {
        for (const asset of this.assets) {
            if (typeof asset.table !== 'undefined' &&
                asset.table.name === name) {
                return asset;

            } else if (typeof asset.chart !== 'undefined' &&
                asset.chart.name === name) {
                return asset;

            } else if (typeof asset.text !== 'undefined' &&
                asset.text.name === name) {
                return asset;

            } else if (typeof asset.form !== 'undefined' &&
                asset.form.name === name) {
                return asset;
            }
        }
        return null;
    }

    public clone() {
        return SheetAsset.deserialize(this);
    }
}

export default Sheet;
