import Rectangle from "./Rectangle";

class RectangleImpl implements Rectangle {
    private left: number;
    private top: number;
    private width: number;
    private height: number;

    constructor(left: number = 0, top: number = 0, width: number = 0, height: number = 0) {
        this.setRectangle(left, top, width, height);
    }

    public setRectangle(left: number, top: number, width: number, height: number) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public getLeft() {
        return this.left;
    }

    public setLeft(left: number) {
        this.left = left;
    }

    public getTop() {
        return this.top;
    }

    public setTop(top: number) {
        this.top = top;
    }

    public getRight() {
        return this.left + this.width;
    }

    public getBottom() {
        return this.top + this.height;
    }

    public getWidth() {
        return this.width;
    }

    public setWidth(width: number) {
        this.width = width;
    }

    public getHeight() {
        return this.height;
    }

    public setHeight(height: number) {
        this.height = height;
    }

    public isXInside(x: number) {
        return x >= this.left && x < this.getRight();
    }

    public isYInside(y: number) {
        return y >= this.top && y < this.getBottom();
    }

    public isPointInside(x: number, y: number) {
        return this.isXInside(x) && this.isYInside(y);
    }

    public moveTo(x: number, y: number) {
        this.moveToX(x);
        this.moveToY(y);
    }

    public moveToX(x: number) {
        this.setLeft(x);
    }

    public moveToY(y: number) {
        this.setTop(y);
    }

};

export default RectangleImpl;
