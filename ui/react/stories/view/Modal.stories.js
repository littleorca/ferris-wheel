import * as React from "react";
import Modal from "../../src/view/Modal";
import Dialog from "../../src/view/Dialog";

class ModalStories extends React.Component {
    render() {
        return <div>
            <div>
                <button onClick={() => {
                    Modal.show(props => <div>
                        <h2>hello world!</h2>
                        <button onClick={props.close}>close</button>
                    </div>);
                }}>Simple</button>
            </div>
            <div>
                <button onClick={() => {
                    Modal.show(props => <div>
                        <h2>hello world!</h2>
                        <button onClick={props.close}>close</button>
                    </div>, document.getElementById("test-container"));
                }}>With parent</button>
            </div>
            <div>
                <button onClick={() => {
                    Modal.show(props =>
                        <Dialog
                            actions={[{
                                name: 'Ok',
                                callback: props.close
                            }, {
                                name: 'Cancel',
                                callback: props.close
                            }]}>Hello dialog!</Dialog>)
                }}>With dialog</button>
            </div>
            <div id="test-container" style={{
                position: "relative",
                width: 300,
                height: 300,
                border: "2px solid #0f0"
            }}>
                Parent for modal.
            </div>
        </div>;
    }
}

export default ModalStories;