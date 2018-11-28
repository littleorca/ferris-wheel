package com.ctrip.ferriswheel.quarks.util;

import com.ctrip.ferriswheel.quarks.exception.QuarksLexicalException;
import com.ctrip.ferriswheel.quarks.exception.QuarksSyntaxException;
import com.ctrip.ferriswheel.quarks.syntax.lr.LR1ParsingTableConstructor;
import com.ctrip.ferriswheel.quarks.syntax.lr.ParsingTable;

import java.io.*;

public class LRParsingTableGenerator {

	public static void main(String... args) throws QuarksLexicalException, QuarksSyntaxException, IOException {
		if (args == null || args.length < 2) {
			System.err.println("Usage: $0 <bnf file path> $1 <table output file> [dot output file]");
			System.exit(1);
		}
		String bnf = args[0];// "quarks.bnf";
		LR1ParsingTableConstructor constructor = new LR1ParsingTableConstructor();
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(bnf);
		if (is == null) {
			is = new FileInputStream(bnf);
		}
		ParsingTable table = constructor.construct(is);
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(args[1]));
		oos.writeObject(table);
		oos.close();

		if (args.length > 2) {
			ClosureUtil.dumpGraphvizDot(constructor.getRootClosure(), new FileOutputStream(args[2]));
		}
	}


}
