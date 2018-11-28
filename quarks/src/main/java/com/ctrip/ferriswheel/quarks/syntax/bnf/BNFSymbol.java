package com.ctrip.ferriswheel.quarks.syntax.bnf;

import com.ctrip.ferriswheel.quarks.Symbol;

public abstract class BNFSymbol implements Symbol {
	private static final long serialVersionUID = 1L;

	private String symbol;

	public static BNFSymbol terminator() {
		return Terminator.TERMINATOR;
	}

	public static Terminal terminalSymbol(String symbol) {
		return new Terminal(symbol);
	}

	public static NonTerminal nonTerminalSymbol(String symbol) {
		return new NonTerminal(symbol);
	}

	BNFSymbol(String symbol) {
		this.symbol = symbol;
		/*
		 * if(symbolIdProvider != null) { this.intSymbol =
		 * symbolIdProvider.getIntSymbol(symbol); }
		 */
	}

	@Override
	public String getSymbol() {
		return symbol;
	}

	/*
	 * @Override public int getID() { return intSymbol; }
	 */

	@Override
	public int hashCode() {
		return symbol.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Symbol))
			return false;
		Symbol s = (Symbol) o;
		return symbol.equals(s.getSymbol()) && isTerminal() == s.isTerminal() && isTerminator() == s.isTerminator();
	}

	@Override
	public String toString() {
		if (isTerminator()) {
			return "#";

		} else if (isTerminal()) {
			return "\"" + symbol + "\"";

		} else {
			return "<" + symbol + ">";
		}
	}

	static class Terminator extends BNFSymbol {
		private static final long serialVersionUID = 1L;

		static final Terminator TERMINATOR = new Terminator();

		private Terminator() {
			super("#");
		}

		@Override
		public boolean isTerminal() {
			return true;
		}

		@Override
		public boolean isTerminator() {
			return true;
		}

	}

	static class Terminal extends BNFSymbol {
		private static final long serialVersionUID = 1L;

		Terminal(String symbol) {
			super(symbol);
		}

		@Override
		public boolean isTerminal() {
			return true;
		}

		@Override
		public boolean isTerminator() {
			return false;
		}
	}

	static class NonTerminal extends BNFSymbol {
		private static final long serialVersionUID = 1L;

		NonTerminal(String symbol) {
			super(symbol);
		}

		@Override
		public boolean isTerminal() {
			return false;
		}

		@Override
		public boolean isTerminator() {
			return false;
		}
	}

}
