import * as React from "react";
import * as ReactDOM from "react-dom";
import "./Modal.css";

interface ModalProps extends React.ClassAttributes<any> {
    close(): void;
}

class Modal {
    public static show(renderer: React.SFC<ModalProps>, parent?: Element) {
        if (typeof parent === "undefined") {
            parent = document.body;
        }
        const container = document.createElement("div");
        container.className = "modal-container";
        parent.appendChild(container);

        const close = () => {
            ReactDOM.unmountComponentAtNode(container);
            if (container.parentNode !== null) {
                container.parentNode.removeChild(container);
            }
        }

        const props = {
            close
        }

        ReactDOM.render(<div className="modal-wrapper">
            {renderer(props)}
        </div>, container);
    }
}

export default Modal;
export { ModalProps }
