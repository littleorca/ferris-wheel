import * as React from 'react';
import ActionHandler from '../action/ActionHandler';
import ActionHerald from '../action/ActionHerald';
import Extension from '../extension/Extension';

interface SharedViewProps<T> extends React.ClassAttributes<T> {
    editable?: boolean;
    onAction?: ActionHandler;
    herald?: ActionHerald;
    controlPortal?: Element;
    extensions?: Extension[];
}

export default SharedViewProps;
