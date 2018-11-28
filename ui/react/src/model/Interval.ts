class Interval {
    public from: number;
    public to: number;

    public static deserialize(input: any): Interval {
        const from = input.from;
        const to = input.to;

        return new Interval(from, to);
    }

    constructor(
        from: number = 0,
        to: number = 0) {

        this.from = from;
        this.to = to;
    }
}

export default Interval;
