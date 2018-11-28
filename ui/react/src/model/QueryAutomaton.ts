import QueryTemplate from "./QueryTemplate";
import NamedValue from "./NamedValue";
import DataQuery from "./DataQuery";

class QueryAutomaton {
    public template: QueryTemplate;
    public params: NamedValue[];
    public query: DataQuery;

    public static deserialize(input: any): QueryAutomaton {
        const template = typeof input.template !== 'undefined' ?
            QueryTemplate.deserialize(input.template) : undefined;
        const params = [];
        if (typeof input.params !== 'undefined' && input.params !== null) {
            for (const param of input.params) {
                params.push(NamedValue.deserialize(param));
            }
        }
        const query = typeof input.query !== 'undefined' ?
            DataQuery.deserialize(input.query) : undefined;
        return new QueryAutomaton(template, params, query);
    }

    constructor(
        template: QueryTemplate = new QueryTemplate(),
        params: NamedValue[] = [],
        query: DataQuery = new DataQuery()) {

        this.template = template;
        this.params = params;
        this.query = query;
    }
}

export default QueryAutomaton;
