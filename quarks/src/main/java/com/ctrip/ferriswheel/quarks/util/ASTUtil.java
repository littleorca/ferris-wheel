package com.ctrip.ferriswheel.quarks.util;

import com.ctrip.ferriswheel.quarks.Token;
import com.ctrip.ferriswheel.quarks.AbstractSyntaxTree;
import com.ctrip.ferriswheel.quarks.Symbol;

import java.util.Iterator;

/**
 * Abstract Syntax Tree Utils.
 * 
 */
public class ASTUtil {

	/**
	 * get the terminal symbol for a ast node. call the method make sure the
	 * pre-condition : from the node to the terminal node just only one path
	 * like following format :
	 * <ul>
	 * <li><strong>&lt;assignment_operator&gt; -- "="</strong> : will get "="
	 * symbol</li>
	 * <li><strong>&lt;unary_operator&gt; -- '&'</strong> : will get '&' symbol
	 * </li>
	 * </ul>
	 * 
	 * @return
	 */
	public static Symbol getTerminalSymbol(AbstractSyntaxTree<?> node) {
		if (node == null) {
			throw new NullPointerException();
		}
		do {
			switch (node.getChildCount()) {
			case 0:
				return node.getHandle();
			case 1:
				node = node.getChild(0);
				break;
			default:
				throw new IllegalArgumentException("Exist Multiple path from node to terminal node.");
			}
		} while (node != null);
		return null;
	}

	/**
	 * Similar to {@link #getTerminalSymbol(AbstractSyntaxTree)}
	 * 
	 * @param node
	 *            abstract syntax tree node
	 * @return
	 */
	public static Token getTerminalToken(AbstractSyntaxTree<?> node) {
		if (node == null) {
			throw new NullPointerException();
		}
		do {
			switch (node.getChildCount()) {
			case 0:
				return node.getToken();
			case 1:
				node = node.getChild(0);
				break;
			default:
				throw new IllegalStateException("Exist Multiple path from node to terminal node.");
			}
		} while (node != null);
		return null;
	}

	/**
	 * 
	 * Dump the expression.
	 * 
	 * @param node
	 *            abstract syntax tree node
	 * @return
	 * @see #dumpExp(AbstractSyntaxTree, boolean, boolean)
	 */
	public static String dumpExp(AbstractSyntaxTree<?> node) {
		return dumpExp(node, true, false);
	}

	/**
	 * Dump the expression.
	 * 
	 * @param node
	 * @param includeExtra
	 * @return
	 */
	public static String dumpExp(AbstractSyntaxTree<?> node, boolean includeExtra, boolean preCompile) {
		StringBuilder sb = new StringBuilder();
		Iterator iterator = node.childrenIterator();

		while (iterator.hasNext()) {
			AbstractSyntaxTree<?> child = (AbstractSyntaxTree<?>) iterator.next();
			Token token = child.getToken();
			if (token != null && (token.getType() == Token.Type.Operator || token.getType() == Token.Type.Delimiter)) {
				sb.append(token.getString()).append(" ");
			} else {
				if (includeExtra)
					sb.append(" {");
				dumpExp(sb, child, preCompile);
				if (includeExtra)
					sb.append("} ");
			}
		}
		if (includeExtra)
			sb.append('\n');
		return sb.toString();
	}

	/**
	 * 
	 * @param sb
	 * @param node
	 */
	public static void dumpExp(StringBuilder sb, AbstractSyntaxTree<?> node, boolean preCompile) {
		if (node.getChildCount() > 0) {
			Iterator iterator = node.childrenIterator();
			while (iterator.hasNext()) {
				dumpExp(sb, (AbstractSyntaxTree<?>) iterator.next(), preCompile);
			}
		} else {
			Token token = node.getToken();
			Token.Type tokenType = token.getType();
			if (tokenType == Token.Type.String) {
				sb.append('"');
				sb.append(preCompile ? "?" : token.getString());
				sb.append('"');
			} else {
				sb.append(preCompile ? "?" : token.getString());
			}
			sb.append(" ");
		}
	}

	public static String dump(AbstractSyntaxTree<?> node) {
		StringBuilder sb = new StringBuilder();
		dump(sb, node, "", false);
		return sb.toString();
	}

	public static String dump(AbstractSyntaxTree<?> node, boolean showToken) {
		StringBuilder sb = new StringBuilder();
		dump(sb, node, "", showToken);
		return sb.toString();
	}

	public static void dump(StringBuilder sb, AbstractSyntaxTree<?> node, String prefix, boolean showToken) {
		if (node.getParent() != null && node.getParent().getChild(0) != node) {
			sb.append(prefix);
		}
		if (prefix.endsWith("`"))
			prefix = prefix.substring(0, prefix.length() - 1) + " ";

		int s = sb.length();
		sb.append("-").append(node.getHandle());
		if(showToken && node.getToken() != null) {
			String token = node.getToken().getString();
			if(!node.getHandle().getSymbol().equals(token)) {
				sb.append(":").append(token);
			}
		}
		if (node.getChildCount() == 0)
			sb.append("\r\n");
		else
			sb.append("-");

		StringBuilder sb2 = new StringBuilder(prefix);
		s = sb.length() - s - 3;
		for (int i = 0; i < s; i++) {
			sb2.append(" ");
		}
		prefix = sb2.toString();

		for (int i = 0; i < node.getChildCount(); i++) {
			AbstractSyntaxTree<?> child = node.getChild(i);
			if (i + 1 < node.getChildCount())
				dump(sb, child, prefix + "  |", showToken);
			else
				dump(sb, child, prefix + "  `", showToken);
		}
	}


}
