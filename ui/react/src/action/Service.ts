import EditRequest from "./EditRequest";
import EditResponse from "./EditResponse";

type OkCallback = (resp: EditResponse) => void;
type ErrorCallback = () => void;

interface Service {
    call: (
        request: EditRequest,
        onOk: OkCallback,
        onError: ErrorCallback
    ) => void;

    isBusy: () => boolean;
}

export default Service;
export { OkCallback, ErrorCallback }
