import org.antlr.v4.runtime.Token;
import parser.GrammarBaseVisitor;
import parser.GrammarParser.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class MyVisitor extends GrammarBaseVisitor<String> {
	private static int currentLevel = 0; // indicates the current level of scope depth
	private static Map<Integer, ArrayList<String>> currentVariables = new HashMap<>();

	static {
		currentVariables.put(0, new ArrayList<>());
	}

	private static int conditionalIndex = 0;
	private static int variableIndex = 0;
	private Map<String, Integer> variableNameToId = null;
	private Map<String, Integer> variableNameToType = null;
	private Map<String, Integer> functionNameToId = new HashMap<>();
	private Map<String, Integer> functionNameToType = new HashMap<>();

	private static int getConditionalIndex() {
		return conditionalIndex++;
	}

	private static int getVariableIndex() {
		return variableIndex++;
	}

	@Override
	public String visitProgram(ProgramContext ctx) {
		String output = ".class public Test\n" +
				".super java/lang/Object\n";
		// for every function ANTLR parses (children of functionR list) append any non-main function to ArrayList functionNameToId
		ctx.functionR().stream().filter(function -> function instanceof FunctionContext).forEach(function -> {
			functionNameToId.put((((FunctionContext) function).functionname).getText(), functionNameToId.size());
		});
		String stmnt = visitChildren(ctx);
		output += stmnt;

		return output;
	}

	@Override
	public String visitMain(MainContext ctx) {
		String output = "";
		variableNameToId = new HashMap<>();
		variableNameToType = new HashMap<>();
		variableIndex = 0;
		String stmnt = visitStatementList(ctx.stmntList);
		functionNameToId.put("main", functionNameToId.size());

		output += ".method public static main([Ljava/lang/String;)V\n";
		output += ".limit stack 100\n";
		output += ".limit locals 100\n";
		output += stmnt;
		output += "\nreturn\n";
		output += ".end method";

		return output;
	}

	@Override
	public String visitFunction(FunctionContext ctx) {
		String output = "";
		variableNameToId = new HashMap<>();
		variableNameToType = new HashMap<>();
		variableIndex = 0;
		int labelFunction = functionToId(ctx.functionname);
		int type = typeToInt(ctx.type.getText());
		functionNameToType.put(ctx.functionname.getText(), typeToInt(ctx.type.getText()));
		String typeReturn = typeToLetterCapital(type);
		StringJoiner stringJoiner = new StringJoiner("");
		for (int i = 0; i < ctx.paramList.paramList.size(); i++) {
			stringJoiner.add("I");
			visit(ctx.paramList.paramList.get(i));
		}
		String parameters = stringJoiner.toString();
		String stmnt = visitStatementList(ctx.stmntList);


		output += ".method public static " + "fct" + String.valueOf(labelFunction) + "(" + parameters + ")" + typeReturn + "\n";
		output += ".limit stack 100\n";
		output += ".limit locals 100\n";
		output += stmnt;
		if (type != 3) { // return unless void
			output += "\n" + typeToLetter(type) + "return";
		} else {
			output += "\nreturn";
		}
		output += "\n.end method";

		return output;
	}

	@Override
	public String visitFunctioncall(FunctioncallContext ctx) {
		String output = "";
		StringJoiner stringJoiner = new StringJoiner("");
		for (int i = 0; i < ctx.paramList.paramList.size(); i++) {
			stringJoiner.add("I");
			output += visit(ctx.paramList.paramList.get(i)) + "\n";
		}
		String parameters = stringJoiner.toString();
		output += "invokestatic Test/fct" + functionToId(ctx.functionname) + "(" + parameters + ")" + typeToLetterCapital(functionToType(ctx.functionname));
		return output;
	}

	@Override
	public String visitExpressionCall(ExpressionCallContext ctx) {

		return visit(ctx.expression());
	}

	private String compare(String comparison) {
		String output = "";
		int labelIsEqual = getConditionalIndex();
		int labelAfterCompare = getConditionalIndex();
		output += "isub\n";
		output += comparison + " Label" + labelIsEqual + "\n";
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
		output += compare("ifle");
		return output;
	}

	@Override
	public String visitGreaterEquals(GreaterEqualsContext ctx) {
		String output = "";
		output += visit(ctx.left) + "\n";
		output += visit(ctx.right) + "\n";
		output += compare("ifge");
		return output;
	}

	@Override
	public String visitLess(LessContext ctx) {
		String output = "";
		output += visit(ctx.left) + "\n";
		output += visit(ctx.right) + "\n";
		output += compare("iflt");
		return output;
	}

	@Override
	public String visitGreater(GreaterContext ctx) {
		String output = "";
		output += visit(ctx.left) + "\n";
		output += visit(ctx.right) + "\n";
		output += compare("ifgt");
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
		output += "ifeq Label" + labelFalse + "\n";
		output += visit(ctx.left) + "\n";
		output += visit(ctx.right) + "\n";
		output += "isub\n";
		output += "ifeq Label" + labelBothTrue + "\n";
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
	public String visitBrackets(BracketsContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public String visitBracketsBool(BracketsBoolContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public String visitExpressionBool(ExpressionBoolContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public String visitNumber(NumberContext ctx) {
		return "ldc " + ctx.number.getText();
	}

	@Override
	public String visitStringString(StringStringContext ctx) {
		return "ldc " + ctx.stringContent.getText();
	}

	@Override
	public String visitAssignment(AssignmentContext ctx) {
		return assignVariable(ctx.var, ctx.expr);
	}

	@Override
	public String visitVarDecl(VarDeclContext ctx) {
		declareVariable(ctx.var, ctx.type.getText());
		return null; // "" leads to blank lines
	}

	private String assignVariable(Token variableName, VariablesContext assignment) {
		String output = "";
		output += visit(assignment) + "\n";
		if (assignment.getChild(0) instanceof ExpressionContext) { // if assignment is an Expression
			if (variableToType(variableName) != 0) { // if variable is not an Integer
				showWarning(variableName, "invalid or unsafe type conversion");
			}
		} else if (assignment.getChild(0) instanceof StringRecContext) { // if assignment is a string
			if (variableToType(variableName) != 2) { // if variable is not a string
				showWarning(variableName, "invalid or unsafe type conversion");
			}
		}
		output += typeToLetter(variableToType(variableName));
		output += "store ";
		output += variableToId(variableName);
		return output;
	}

	private void declareVariable(Token variableName, String variableType) {
		if (variableNameToId.get(variableName.getText()) == null) { // check if this might already have been declared
			variableNameToId.put(variableName.getText(), getVariableIndex());
			variableNameToType.put(variableName.getText(), typeToInt(variableType));
			(currentVariables.get(currentLevel)).add(variableName.getText());
		} else { // if so, show a warning
			showWarning(variableName, "variable already declared");
			//throw new PreviouslyDeclaredVariableException(variableName);
		}
	}

	@Override
	public String visitDeclAssi(DeclAssiContext ctx) {
		declareVariable(ctx.var, ctx.type.getText());
		return assignVariable(ctx.var, ctx.expr);
	}

	@Override
	public String visitPrint(PrintContext ctx) {
		return "  getstatic java/lang/System/out Ljava/io/PrintStream;\n" +
				visit(ctx.printable) + "\n" +
				"  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V";
	}

	@Override
	public String visitRecursiveString(RecursiveStringContext ctx) {
		String output = "";
		output += "getstatic java/lang/System/out Ljava/io/PrintStream;\n" +
				"new java/lang/StringBuilder\n" +
				"dup\n";
		output += "ldc \"\"\n";
		output += "invokespecial java/lang/StringBuilder/<init>(Ljava/lang/String;)V\n";
		StringJoiner stringJoiner = new StringJoiner("\n");
		for (int i = 0; i < ctx.stringList.size(); i++) {
			stringJoiner.add(visit(ctx.stringList.get(i)));
			String type = "";
			if (ctx.stringList.get(i) instanceof IntegerStringContext) { // if is expression
				if ((ctx.stringList.get(i).getChild(0)) instanceof VarContext) {
					int varType = variableNameToType.get(((VarContext) (ctx.stringList.get(i).getChild(0))).var.getText()); // if is variable, check variable type
					if (varType == 0) {
						type = "I";
					} else if (varType == 2) {
						type = "Ljava/lang/String;";
					}
				} else {
					type = "I";
				}
			} else {
				type = "Ljava/lang/String;";
			}
			stringJoiner.add("invokevirtual java/lang/StringBuilder/append(" + type + ")Ljava/lang/StringBuilder;");
		}
		stringJoiner.add("invokevirtual java/lang/StringBuilder/toString()Ljava/lang/String;");
		output += stringJoiner.toString();
		//}
		return output;
	}

	@Override
	public String visitReturn(ReturnContext ctx) {
		return visit(ctx.expr);
	}

	@Override
	public String visitVar(VarContext ctx) {
		int variableId = variableToId(ctx.var);
		int variableType = variableToType(ctx.var);
		return typeToLetter(variableType) + "load " + variableId;
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
		currentVariables.put(currentLevel, new ArrayList<>());
		String stmntThen = visitStatementList(ctx.stmntThenList);
		String stmntElse = "";
		currentVariables.get(currentLevel).forEach(this::invalidateVariable);
		currentVariables.remove(currentLevel);
		currentLevel--;
		if (ctx.stmntElseList != null) {
			currentLevel++;
			currentVariables.put(currentLevel, new ArrayList<>());
			stmntElse = visitStatementList(ctx.stmntElseList);
			currentVariables.get(currentLevel).forEach(this::invalidateVariable);
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

	@Override
	public String visitIfSingle(IfSingleContext ctx) {
		String output = "";

		currentLevel++;
		currentVariables.put(currentLevel, new ArrayList<>());
		String stmntThen = visit(ctx.stmnt);
		currentVariables.get(currentLevel).forEach(this::invalidateVariable);
		currentVariables.remove(currentLevel);
		currentLevel--;
		int labelAfterIf = getConditionalIndex();
		output += visit(ctx.eval) + "\n";
		output += "ifeq Label" + String.valueOf(labelAfterIf) + "\n";
		output += stmntThen;
		output += "\n" + "Label" + String.valueOf(labelAfterIf) + ":";
		return output;
	}

	@Override
	public String visitWhile(WhileContext ctx) {
		String output = "";

		currentLevel++;
		currentVariables.put(currentLevel, new ArrayList<>());
		String stmnt = visitStatementList(ctx.stmntList);
		currentVariables.get(currentLevel).forEach(this::invalidateVariable);
		currentVariables.remove(currentLevel);
		currentLevel--;
		int labelBeforeWhile = getConditionalIndex();
		int labelAfterWhile = getConditionalIndex();

		output += "Label" + String.valueOf(labelBeforeWhile) + ":\n";
		output += visit(ctx.eval) + "\n";
		output += "ifeq Label" + String.valueOf(labelAfterWhile) + "\n";
		output += stmnt;
		output += "\ngoto Label" + String.valueOf(labelBeforeWhile);
		output += "\n" + "Label" + String.valueOf(labelAfterWhile) + ":";

		return output;
	}

	private void showWarning(Token token, String warning) {
		System.err.println("line " + token.getLine() + ":" + token.getCharPositionInLine() + " " + warning);
	}

	private int functionToId(Token function) {
		Integer functionId = functionNameToId.get(function.getText());
		if (functionId == null) {
			showWarning(function, "undeclared function");
			functionId = -1;
		}
		return functionId;
	}

	private int functionToType(Token function) {
		Integer functionId = functionNameToType.get(function.getText());
		if (functionId == null) {
			showWarning(function, "undeclared function");
			functionId = -1;
		}
		return functionId;
	}

	private int variableToId(Token variable) {
		Integer variableId = variableNameToId.get(variable.getText());
		if (variableId == null) {
			showWarning(variable, "undeclared variable");
			variableId = -1;
		}
		return variableId;
	}

	private int variableToType(Token variable) {
		Integer variableType = variableNameToType.get(variable.getText());
		if (variableType == null) {
			showWarning(variable, "undeclared variable");
			variableType = -1;
		}
		return variableType;
	}

	private void invalidateVariable(String variable) {
		variableNameToId.remove(variable);
		variableNameToType.remove(variable);
	}

	private int typeToInt(String type) {
		switch (type) {
			case "int":
				return 0;
			case "bool":
				return 1;
			case "string":
				return 2;
			case "void":
				return 3;
			default:
				return -1;
		}
	}

	private String typeToLetter(int type) {
		switch (type) {
			case 0:
				return "i";
			case 1:
				return "i";
			case 2:
				return "a";
			default:
				return "";
		}
	}

	private String typeToLetterCapital(int type) {
		switch (type) {
			case 0:
				return "I";
			case 2:
				return "Ljava/lang/String;";
			case 3:
				return "V";
			default:
				return "";
		}
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
