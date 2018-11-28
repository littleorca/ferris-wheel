import Version from "./Version";
import Sheet from "./Sheet";

class Workbook {
    public version: Version;
    public flags: number;
    public name: string;
    public sheets: Sheet[];

    public static deserialize(input: any): Workbook {
        const version = Version.deserialize(input.version);
        const flags = input.flags;
        const name = input.name;
        const sheets = [];
        if (typeof input.sheets !== null) {
            for (const sheet of input.sheets) {
                sheets.push(Sheet.deserialize(sheet));
            }
        }
        return new Workbook(version, flags, name, sheets);
    }

    constructor(
        version: Version = new Version(),
        flags: number = 0,
        name: string = '',
        sheets: Sheet[] = []) {

        this.version = version;
        this.flags = flags;
        this.name = name;
        this.sheets = sheets;
    }

    public getSheetByName(name: string): Sheet | null {
        for (const sheet of this.sheets) {
            if (sheet.name === name) {
                return sheet;
            }
        }
        return null;
    }
}

export default Workbook;
