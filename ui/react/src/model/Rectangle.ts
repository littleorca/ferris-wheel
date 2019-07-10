interface Rectangle {
    setRectangle(left: number, top: number, width: number, height: number): void;

    getLeft(): number;

    setLeft(left: number): void;

    getTop(): number;

    setTop(top: number): void;

    getRight(): number;

    getBottom(): number;

    getWidth(): number;

    setWidth(width: number): void

    getHeight(): number;

    setHeight(height: number): void;

    isXInside(x: number): boolean;

    isYInside(y: number): boolean;

    isPointInside(x: number, y: number): boolean;

    moveTo(x: number, y: number): void;

    moveToX(x: number): void;

    moveToY(y: number): void;

};

export default Rectangle;
