
class Vars {
    public static ensure<T>(o: T | undefined | null, msg?: string): T {
        if (typeof o === "undefined" || o === null) {
            throw new Error(msg);
        }
        return o;
    }

    public static ifndef<T>(o: T | undefined | null, callback: () => void) {
        if (typeof o === "undefined" || o == null) {
            callback();
        }
        return o;
    }
}

export default Vars;
