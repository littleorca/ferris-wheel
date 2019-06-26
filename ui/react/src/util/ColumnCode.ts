export function toColumnCode(columnIndex: number) {
    if (columnIndex < 0) {
        throw new Error("Invalid column index: " + columnIndex);
    }
    // column code's radix is 26 but without zero
    // think that for radix = 10, 0 eq 00, but here A not eq to AA
    let cc = "";
    while (columnIndex >= 0) {
        const n = columnIndex % 26;
        cc = String.fromCharCode('A'.charCodeAt(0) + n) + cc;
        if (columnIndex < 26) {
            break;
        }
        columnIndex = columnIndex / 26 - 1;
    }
    return cc;
}