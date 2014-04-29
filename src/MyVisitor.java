import parser.GrammarBaseVisitor;
import parser.GrammarParser.PlusContext;
import parser.GrammarParser.ZahlContext;

public class MyVisitor extends GrammarBaseVisitor<String> {
	@Override
	public String visitPlus(PlusContext ctx) {
		return visitChildren(ctx) + "\n" +
				ctx.rechts.getText() + "\n" +
				"addition";
	}

	@Override
	public String visitZahl(ZahlContext ctx) {
		return ctx.zahl.getText();
	}

	@Override
	protected String aggregateResult(String aggregate, String nextResult) {
		if (aggregate == null) {
			return nextResult;
		}
		if (nextResult == null) {
			return aggregate;
		}
		return aggregate + "\n" + nextResult;
	}
}
