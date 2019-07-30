import RectangleImpl from "./RectangleImpl";

class Header extends RectangleImpl {
    private static sequence: number = 0;

    private id: number;
    // TBD

    public static deserialize(input: any) {
        return new Header();
    }

    constructor(left: number = 0, top: number = 0, width: number = 0, height: number = 0) {
        super(left, top, width, height);
        this.id = Header.sequence++;
    }

    public getId() {
        return this.id;
    }
}

export default Header;
