import * as React from 'react';
import ActionHandler from '../action/ActionHandler';
import ActionHerald from '../action/ActionHerald';

interface SharedViewProps<T> extends React.ClassAttributes<T> {
    editable?: boolean;
    onAction?: ActionHandler;
    herald?: ActionHerald;
}

export default SharedViewProps;
