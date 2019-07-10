import React, { Component } from 'react';
import GroupView, { GroupItem } from '../../src/view/GroupView';
import { action } from '@storybook/addon-actions'

class GroupViewStories extends Component {
    render() {
        return (
            <div>
                <h3>GroupView</h3>
                <GroupView>
                    <GroupItem
                        name="g1"
                        title="Group 1">
                        <p>This is group 1</p>
                        <p>enjoy!</p>
                    </GroupItem>
                    <GroupItem
                        name="g2"
                        title="Group 2">
                        <p>This is group 2</p>
                        <p>enjoy!</p>
                    </GroupItem>
                </GroupView>
            </div>
        );
    }
}

export default GroupViewStories;