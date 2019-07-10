
export const containerStyle: React.CSSProperties = {
    display: "block",
    margin: 0,
    padding: 0,
    boxSizing: "border-box",
    direction: "ltr",
};

export const masterContainerStyle: React.CSSProperties = {
    ...containerStyle,
    position: "relative",
    overflow: "hidden",
}

export const overlayStyle: React.CSSProperties = {
    ...containerStyle,
    position: "absolute",
    overflow: "hidden",
};

export const cornerOverlayStyle: React.CSSProperties = {
    ...overlayStyle,
    zIndex: 3,
};

export const headersOverlayStyle: React.CSSProperties = {
    ...overlayStyle,
    zIndex: 2,
};

export const scrollableContainerStyle: React.CSSProperties = {
    ...containerStyle,
    position: "absolute",
    top: 0,
    right: 0,
    bottom: 0,
    left: 0,
    overflow: "scroll",
};

export const rowStyle: React.CSSProperties = {
    boxSizing: "border-box",
};

export const cellStyle: React.CSSProperties = {
    boxSizing: "border-box",
    overflow: "hidden",
};

export const helperContainerStyle: React.CSSProperties = {
    ...containerStyle,
    position: "absolute",
    zIndex: 4,
    left: 0,
    top: 0,
    width: 0,
    height: 0,
    border: "0 none",
    overflow: "visible",
};

export const resizeHelperStyle: React.CSSProperties = {
    ...containerStyle,
    display: "none",
    position: "absolute",
};

export const columnResizeHandleStyle: React.CSSProperties = {
    ...containerStyle,
    position: "absolute",
    top: 0,
    right: 0,
    bottom: 0,
    width: 4,
    cursor: "col-resize"
};

// export const rowResizeHandleStyle: React.CSSProperties = {
//     ...containerStyle,
//     position: "absolute",
//     right: 0,
//     bottom: 0,
//     left: 0,
//     height: 4,
//     cursor: "row-resize"
// };

export const selectionBorderStyle: React.CSSProperties = {
    ...containerStyle,
    position: "absolute",
    width: 0,
    height: 0,
};

export const editContainerStyle: React.CSSProperties = {
    ...containerStyle,
    position: "absolute",
    zIndex: 1,
    overflow: "visible",
};

export const defaultEditStyle: React.CSSProperties = {
    ...containerStyle,
    width: "100%",
    height: "100%",
    border: "0 none",
    outline: "none",
    resize: "none",
    overflow: "hidden",
};
