class Color {
    public red: number;
    public green: number;
    public blue: number;
    public alpha: number;

    public static deserialize(input: any): Color {
        const red = input.red;
        const green = input.green;
        const blue = input.blue;
        const alpha = input.alpha;

        return new Color(red, green, blue, alpha);
    }

    constructor(
        red: number = 0,
        green: number = 0,
        blue: number = 0,
        alpha: number = 1) {

        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }
}

/**
 * Convert float color with range [0, 1] to 8-bits integer value with range [0, 255]
 * @param f float color with range [0, 1]
 */
function floatToInt8(f: number) {
    return Math.floor(f * 255);
}

/**
 * Convert 8-bits color with range [0, 255] to float value with range [0, 1]
 * @param i 8-bits color with range [0, 255]
 */
function int8ToFloat(i: number) {
    return i / 255.0;
}

export default Color;
export { floatToInt8, int8ToFloat };
