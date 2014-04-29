import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import parser.GrammarLexer;
import parser.GrammarParser;

public class Main {
	public static void main(String[] args) {
		try {
			ANTLRInputStream input = new ANTLRFileStream("code.q");
			GrammarLexer lexer = new GrammarLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			GrammarParser parser = new GrammarParser(tokens);

			ParseTree tree = parser.addition();
			new MyVisitor().visit(tree);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}