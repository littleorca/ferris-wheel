import * as React from "react";
import QueryTemplate from "../model/QueryTemplate";

interface QueryWizardProps extends React.ClassAttributes<any> {
    initialQueryTemplate?: QueryTemplate;
    onOk: (queryTemplate: QueryTemplate) => void;
    onCancel: () => void;
}

interface QueryWizard {
    readonly name: string;
    readonly title?: string;
    readonly description?: string;
    readonly accepts: (queryTemplate: QueryTemplate) => boolean;
    readonly component: React.SFC<QueryWizardProps>;
}

interface Extension {
    readonly queryWizard?: QueryWizard;
}

export default Extension;
export { QueryWizardProps, QueryWizard };

