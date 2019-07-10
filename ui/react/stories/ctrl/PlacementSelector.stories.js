import React, { Component } from 'react';
import PlacementSelector, { PlacementItem } from "../../src/ctrl/PlacementSelector";
import { action } from '@storybook/addon-actions';

class PlacementSelectorStories extends Component {
    constructor(props) {
        super(props);

        this.onToggle = this.onToggle.bind(this);
    }

    onToggle(item, checked) {
        action('PlacementSelector toggle')(item, checked);
    }

    render() {
        return (
            <div>
                <h3>PlacementSelector</h3>
                <PlacementSelector
                    availableItems={new Set([
                        PlacementItem.TOP,
                        PlacementItem.RIGHT,
                        PlacementItem.BOTTOM,
                        PlacementItem.LEFT,
                        PlacementItem.TOP_LEFT,
                        PlacementItem.TOP_RIGHT,
                        PlacementItem.BOTTOM_LEFT,
                        PlacementItem.BOTTOM_RIGHT,
                        PlacementItem.CENTER,
                    ])}
                    onToggle={this.onToggle} />
            </div>
        );
    }
}

export default PlacementSelectorStories;