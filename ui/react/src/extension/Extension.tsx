import * as React from "react";
import { QueryTemplate } from "../model";

interface QueryWizardProps extends React.ClassAttributes<any> {
    initialQueryTemplate?: QueryTemplate;
    onOk: (queryTemplate: QueryTemplate) => void;
    onCancel: () => void;
}

interface QueryWizard {
    name: string;
    title?: string;
    description?: string;
    accepts: (queryTemplate: QueryTemplate) => boolean;
    component: React.SFC<QueryWizardProps>;
}

interface Extension {
    queryWizard?: QueryWizard;
}

export default Extension;
export { QueryWizardProps, QueryWizard };
