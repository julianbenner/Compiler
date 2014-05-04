import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import parser.GrammarLexer;
import parser.GrammarParser;

import java.io.PrintWriter;

public class Main {
	public static void main(String[] args) {
		try {
			ANTLRInputStream input = new ANTLRFileStream("code.q");
			GrammarLexer lexer = new GrammarLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			GrammarParser parser = new GrammarParser(tokens);
			ParseTree tree = parser.program();

			String compiledFileName = "C:\\Users\\Julian\\Downloads\\jasmin-2.4\\jasmin-2.4\\out.j";
			PrintWriter writer = new PrintWriter(compiledFileName, "UTF-8");
			writer.println(new MyVisitor().visit(tree));
			writer.close();
			System.out.println("Compiled to " + compiledFileName);
		} catch (Exception e) {
			System.err.println("Compilation error at " + e.getStackTrace()[0]);
		}
	}
}