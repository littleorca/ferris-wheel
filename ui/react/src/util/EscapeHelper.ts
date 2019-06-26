// TODO WARN: This class should keep sync with the server side one.
class EscapeHelper {
    //    public static final String RE_SIMPLE_NAME = "^[a-zA-Z0-9\\_$\\u4E00-\\u9FA5]+$";
    private static readonly RE_SIMPLE_NAME = /^[a-zA-Z0-9\\_$]+$/;
    private static readonly SINGLE_QUOTE = '\'';

    public static escapeNameIfNeeded(name: string): string {
        if (this.RE_SIMPLE_NAME.test(name)) {
            return name;
        }
        return this.escapeName(name);
    }

    public static escapeName(name: string) {
        let escapedName = this.SINGLE_QUOTE;
        let offset = 0, pos;
        while ((pos = name.indexOf(this.SINGLE_QUOTE, offset)) != -1) {
            escapedName += name.substring(offset, pos) + this.SINGLE_QUOTE + this.SINGLE_QUOTE;
            offset = pos + 1;
        }
        if (offset < name.length) {
            escapedName += name.substring(offset, name.length);
        }
        escapedName += this.SINGLE_QUOTE;
        return escapedName;
    }

    public static unescapeNameIfNeeded(name: string): string {
        if (!name.startsWith(this.SINGLE_QUOTE)) {
            return name;
        }
        let sb = "";
        let pos = 1;
        let offset = pos;
        while ((pos = name.indexOf(this.SINGLE_QUOTE, offset)) != -1) {
            if (pos > offset) {
                sb += name.substring(offset, pos);
            }
            offset = pos + 1;
            if (offset < name.length && name.charAt(offset) == this.SINGLE_QUOTE) {
                sb += this.SINGLE_QUOTE;
                offset++;
            } else {
                break;
            }
        }
        if (offset != name.length) {
            throw new Error();
        }
        return sb;
    }

    /**
     * Escape special chars for CSV field.
     * 
     * Fields containing line breaks (CRLF), double quotes, and commas
     * should be enclosed in double-quotes.
     * 
     * If double-quotes are used to enclose fields, then a double-quote
     * appearing inside a field must be escaped by preceding it with
     * another double quote.
     * 
     * @see https://tools.ietf.org/html/rfc4180
     */
    public static escapeForCSV(str: string) {
        if (!/[\r\n",]/.test(str)) {
            return str;
        }
        return "\"" + str.replace("\"", "\"\"") + "\"";
    }

    /**
     * "	&quot;
     * '	&apos;
     * <	&lt;
     * >	&gt;
     * &	&amp;
     */
    public static escapeForHtml(str: string) {
        if (!/["'<>&]/.test(str)) {
            return str;
        }

        return str.replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}

export default EscapeHelper;
