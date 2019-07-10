import React, { Component } from 'react';
import AssetView from '../../src/view/AssetView';
import SheetAsset from '../../src/model/SheetAsset';
import Text from '../../src/model/Text';
import Values from '../../src/model/Values';

class AssetViewStories extends Component {
    sheetAsset = new SheetAsset(undefined, undefined,
        new Text('test_text', Values.str('hello\n\tworld!')));

    render() {
        return (
            <div>
                <h3>AssetView</h3>
                <AssetView
                    asset={this.sheetAsset} />
            </div>
        );
    }
}

export default AssetViewStories;