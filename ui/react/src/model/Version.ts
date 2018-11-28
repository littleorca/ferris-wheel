class Version {
    public major: number;
    public minor: number;
    public build: number;

    public static deserialize(input: any): Version {
        if (typeof input === 'undefined' || input === null) {
            return new Version();
        }

        const major = input.major;
        const minor = input.minor;
        const build = input.build;
        return new Version(major, minor, build);
    }

    constructor(
        major: number = 0,
        minor: number = 0,
        build: number = 0) {

        this.major = major;
        this.minor = minor;
        this.build = build;
    }

    public toString() {
        return this.major + "." + this.minor + "." + this.build;
    }
}

export default Version;
