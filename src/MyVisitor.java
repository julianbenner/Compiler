import org.antlr.v4.runtime.Token;
import parser.GrammarBaseVisitor;
import parser.GrammarParser.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyVisitor extends GrammarBaseVisitor<String> {
	private static int currentLevel = 0;
	private static Map<Integer, ArrayList> currentVariables = new HashMap<>();

	static {
		currentVariables.put(0, new ArrayList<String>());
	}

	private static int conditionalIndex = 0;
	private Map<String, Integer> variables = new HashMap<>();

	private static int getConditionalIndex() {
		return conditionalIndex++;
	}

	private String compare(String comparison) {
		String output = "";
		int labelIsEqual = getConditionalIndex();
		int labelAfterCompare = getConditionalIndex();
		output += "isub\n";
		output += comparison + " Label" + labelIsEqual +"\n";
		output += "ldc 0\n";
		output += "goto Label" + labelAfterCompare + "\n";
		output += "Label" + labelIsEqual + ":\n";
		output += "ldc 1\n";
		output += "Label" + labelAfterCompare + ":";
		return output;
	}

	@Override
	public String visitEquals(EqualsContext ctx) {
		String output = "";
		output += visit(ctx.left) + "\n";
		output += visit(ctx.right) + "\n";
		output += compare("ifeq");
		return output;
	}

	@Override
	public String visitNotEquals(NotEqualsContext ctx) {
		String output = "";
		output += visit(ctx.left) + "\n";
		output += visit(ctx.right) + "\n";
		output += compare("ifne");
		return output;
	}

	@Override
	public String visitLessEquals(LessEqualsContext ctx) {
		String output = "";
		output += visit(ctx.left) + "\n";
		output += visit(ctx.right) + "\n";
		output += compare("ifge");
		return output;
	}

	@Override
	public String visitGreaterEquals(GreaterEqualsContext ctx) {
		String output = "";
		output += visit(ctx.left) + "\n";
		output += visit(ctx.right) + "\n";
		output += compare("ifle");
		return output;
	}

	@Override
	public String visitLess(LessContext ctx) {
		String output = "";
		output += visit(ctx.left) + "\n";
		output += visit(ctx.right) + "\n";
		output += compare("ifg");
		return output;
	}

	@Override
	public String visitGreater(GreaterContext ctx) {
		String output = "";
		output += visit(ctx.left) + "\n";
		output += visit(ctx.right) + "\n";
		output += compare("ifl");
		return output;
	}

	@Override
	public String visitAnd(AndContext ctx) {
		String output = "";
		int labelBothTrue = getConditionalIndex();
		int labelFalse = getConditionalIndex();
		int labelAfterCompare = getConditionalIndex();
		output += visit(ctx.left) + "\n"; // TODO geht sicher auch ohne doppeltes Ausfuehren
		output += "ldc 0\n";
		output += "isub\n";
		output += "ifeq Label" + labelFalse +"\n";
		output += visit(ctx.left) + "\n";
		output += visit(ctx.right) + "\n";
		output += "isub\n";
		output += "ifeq Label" + labelBothTrue +"\n";
		output += "Label" + labelFalse + ":\n";
		output += "ldc 0\n";
		output += "goto Label" + labelAfterCompare + "\n";
		output += "Label" + labelBothTrue + ":\n";
		output += "ldc 1\n";
		output += "Label" + labelAfterCompare + ":";
		return output;
	}

	@Override
	public String visitAdd(AddContext ctx) {
		return visitChildren(ctx) + "\n" +
				(ctx.operator.getText().equals("+") ? "iadd" : "isub");
	}

	@Override
	public String visitMult(MultContext ctx) {
		return visitChildren(ctx) + "\n" +
				(ctx.operator.getText().equals("*") ? "imul" : "idiv");
	}

	@Override
	public String visitVerschachtelung(VerschachtelungContext ctx) {
		return visitChildren(ctx) + "\n";
	}

	@Override
	public String visitKlammer(KlammerContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public String visitKlammerBool(KlammerBoolContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public String visitZahl(ZahlContext ctx) {
		return "ldc " + ctx.zahl.getText();
	}

	@Override
	public String visitAssignment(AssignmentContext ctx) {
		return visit(ctx.expr) + "\n" +
				"istore " + requireVariableIndex(ctx.var);
	}

	@Override
	public String visitVarDecl(VarDeclContext ctx) {
		if (variables.containsKey(ctx.var.getText())) {
			/*throw new VariableAlreadyDefinedException(ctx.var);*/
		}
		variables.put(ctx.var.getText(), variables.size());
		return "";
	}

	@Override
	public String visitDeclAssi(DeclAssiContext ctx) {
		int temp3 = currentLevel;
		variables.put(ctx.var.getText(), variables.size());
		ArrayList<String> temp = ((ArrayList<String>) currentVariables.get(temp3));
		Token temp2 = ctx.var;
		temp.add(temp2.getText());
		return visit(ctx.expr) + "\n" +
				"istore " + requireVariableIndex(ctx.var);
	}

	@Override
	public String visitIntegerPrint(IntegerPrintContext ctx) {
		return "  getstatic java/lang/System/out Ljava/io/PrintStream;\n" +
				visit(ctx.integer) + "\n" +
				"  invokevirtual java/io/PrintStream/println(I)V";
	}

	@Override
	public String visitVar(VarContext ctx) {
		return "iload " + requireVariableIndex(ctx.var);
	}

	@Override
	public String visitTrue(TrueContext ctx) {
		return "ldc 1";
	}

	@Override
	public String visitFalse(FalseContext ctx) {
		return "ldc 0";
	}

	@Override
	public String visitIf(IfContext ctx) {
		String output = "";

		currentLevel++;
		currentVariables.put(currentLevel, new ArrayList<String>());
		String stmntThen = visitStatementList(ctx.stmntThenList);
		String stmntElse = "";
		for (String current : (ArrayList<String>) currentVariables.get(currentLevel)) {
			invalidateVariable(current);
		}
		currentVariables.remove(currentLevel);
		currentLevel--;
		if (ctx.stmntElseList != null) {
			currentLevel++;
			currentVariables.put(currentLevel, new ArrayList<String>());
			stmntElse = visitStatementList(ctx.stmntElseList);
			for (String current : (ArrayList<String>) currentVariables.get(currentLevel)) {
				invalidateVariable(current);
			}
			currentVariables.remove(currentLevel);
			currentLevel--;
		}
		int labelAfterIf = getConditionalIndex();
		if (ctx.stmntElseList == null) {
			output += visit(ctx.eval) + "\n";
			output += "ifeq Label" + String.valueOf(labelAfterIf) + "\n";
			output += stmntThen;
			output += "\n" + "Label" + String.valueOf(labelAfterIf) + ":";
		} else {
			int labelElse = getConditionalIndex();
			output += visit(ctx.eval) + "\n";
			output += "ifeq Label" + String.valueOf(labelElse) + "\n";
			output += stmntThen;
			output += "\ngoto Label" + String.valueOf(labelAfterIf) + "\n";
			output += "Label" + String.valueOf(labelElse) + ":\n";
			output += stmntElse;
			output += "\n" + "Label" + String.valueOf(labelAfterIf) + ":";
		}
		return output;
	}

	private int requireVariableIndex(Token varNameToken) {
		Integer varIndex = variables.get(varNameToken.getText());
		if (varIndex == null) {
			/*throw new UndeclaredVariableException(varNameToken);*/
		}
		return varIndex;
	}

	private void invalidateVariable(String variable) {
		variables.remove(variable);
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
