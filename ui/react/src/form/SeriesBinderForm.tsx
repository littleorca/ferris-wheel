import * as React from 'react';
import Series from '../model/Series';
import EditableList, { EditorProps } from '../ctrl/EditableList';
import SeriesForm from './SeriesForm';
import { toEditableString } from '../ctrl/UnionValueEdit';

const SeriesEditor = (props: EditorProps<Series>) => {
    return (
        <SeriesForm
            series={props.value}
            afterChange={props.onSubmit} />
    );
}

interface SeriesBinderFormProps {
    series: Series[],
    afterChange?: (series: Series[]) => void,
}

class SeriesBinderForm extends React.Component<SeriesBinderFormProps> {
    constructor(props: SeriesBinderFormProps) {
        super(props);

        // this.getKey = this.getKey.bind(this);
        this.getLabel = this.getLabel.bind(this);
        this.afterChange = this.afterChange.bind(this);
    }

    // protected getKey(series: Series, index: number) {
    // }

    protected getLabel(series: Series, index: number): string {
        const value = series.name;
        let label = value.toString();
        if (label === '') {
            label = toEditableString(value);
        }
        return label;
    }

    protected afterChange(series: Series[]) {
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(series);
        }
    }

    public render() {
        return (
            <div className="series-binder-form">
                <label>图例项（系列）</label>
                <EditableList<Series>
                    className="series-editor"
                    list={this.props.series}
                    // getKey={this.getKey}
                    getLabel={this.getLabel}
                    editor={SeriesEditor}
                    createItem={createSeries}
                    afterChange={this.afterChange} />
            </div>
        );
    }
}

function createSeries() {
    return new Series();
}

export default SeriesBinderForm;
export { SeriesBinderFormProps };
