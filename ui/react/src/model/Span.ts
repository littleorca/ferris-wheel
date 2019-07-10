class Span {
    public start: number;
    public end: number;

    public static deserialize(input: any): Span {
        if (typeof input === 'undefined' || input === null) {
            return new Span();
        }

        const start = input.start;
        const end = input.end;
        return new Span(start, end);
    }

    constructor(
        start: number = 0,
        end: number = 0) {

        this.start = start;
        this.end = end;
    }

    clone() {
        return new Span(this.start, this.end);
    }
}

export default Span;
