import org.antlr.v4.runtime.Token;
import parser.GrammarBaseVisitor;
import parser.GrammarParser.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class MyVisitor extends GrammarBaseVisitor<String> {
	private static int currentLevel = 0; // indicates the current level of scope depth
	private static Map<Integer, ArrayList> currentVariables = new HashMap<>();

	static {
		currentVariables.put(0, new ArrayList<String>());
	}

	private static int conditionalIndex = 0;
	private static int variableIndex = 0;
	private Map<String, Integer> variableNameToId;
	private Map<String, Integer> variableNameToType;
	private Map<String, Map<String, Integer>> variablesFunctions = new HashMap<>();
	private Map<String, Integer> functions = new HashMap<>();

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
		for (FunctionRContext function : ctx.functionR()) {
			if (function instanceof FunctionContext) {
				functions.put((((FunctionContext) function).functionname).getText(), functions.size());
			}
		}
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
		functions.put("main", functions.size());
		variablesFunctions.put("main", variableNameToId);

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
		variablesFunctions.put(ctx.functionname.getText(), variableNameToId);
		int labelFunction = requireFunctionIndex(ctx.functionname);
		StringJoiner stringJoiner = new StringJoiner("");
		for (int i = 0; i < ctx.paramList.paramList.size(); i++) {
			stringJoiner.add("I");
			visit(ctx.paramList.paramList.get(i));
		}
		String parameters = stringJoiner.toString();
		String stmnt = visitStatementList(ctx.stmntList);


		output += ".method public static " + "fct" + String.valueOf(labelFunction) + "(" + parameters + ")I\n";
		output += ".limit stack 100\n";
		output += ".limit locals 100\n";
		output += stmnt;
		output += "\nireturn\n";
		output += ".end method";

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
		output += "invokestatic Test/fct" + requireFunctionIndex(ctx.functionname) + "(" + parameters + ")I";
		return output;
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
	public String visitKlammer(KlammerContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public String visitKlammerBool(KlammerBoolContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public String visitExpressionBool(ExpressionBoolContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public String visitZahl(ZahlContext ctx) {
		return "ldc " + ctx.zahl.getText();
	}

	@Override
	public String visitStringString(StringStringContext ctx) {
		return "ldc " + ctx.stringContent.getText();
	}

	@Override
	public String visitAssignment(AssignmentContext ctx) {
		try {
			if (ctx.expr.getChild(0) instanceof ExpressionContext) { // is int
				if (variableToType(ctx.var.getText()) != 0) {
					throw new Exception();
				}
			} else if (ctx.expr.getChild(0) instanceof StringRecContext) { // is int
				if (variableToType(ctx.var.getText()) != 2) {
					throw new Exception();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String output = "";
		output += visit(ctx.expr) + "\n";
		output += typeToLetter(variableToType(ctx.var.getText()));
		output += "store ";
		output += variableToId(ctx.var.getText());
		return output;
	}

	@Override
	public String visitVarDecl(VarDeclContext ctx) {
		if (variableNameToId.containsKey(ctx.var.getText())) {
			/*throw new VariableAlreadyDefinedException(ctx.var);*/
		}
		declareVariable(ctx.var.getText(), ctx.type.getText());
		return "";
	}

	private void declareVariable(String variableName, String variableType) {
		variableNameToId.put(variableName, getVariableIndex());
		variableNameToType.put(variableName, typeToInt(variableType));
		((ArrayList<String>) currentVariables.get(currentLevel)).add(variableName);
	}

	@Override
	public String visitDeclAssi(DeclAssiContext ctx) {
		declareVariable(ctx.var.getText(), ctx.type.getText());
		return visit(ctx.expr) + "\n" +
				"istore " + requireVariableIndex(ctx.var);
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
		//if(ctx.stringList.size() == 1) {
		//	output += visit(ctx.stringList.get(0));
		//} else {
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
				if ((((IntegerStringContext) ctx.stringList.get(i)).getChild(0)) instanceof VarContext) {
					int varType = variableNameToType.get(((VarContext) (((IntegerStringContext) ctx.stringList.get(i)).getChild(0))).var.getText()); // if is variable, check variable type
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
		int type = variableNameToType.get(ctx.var.getText());
		int id = variableNameToId.get(ctx.var.getText()); // TODO exceptions
		return typeToLetter(type) + "load " + requireVariableIndex(ctx.var);
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

	@Override
	public String visitIfSingle(IfSingleContext ctx) {
		String output = "";

		currentLevel++;
		currentVariables.put(currentLevel, new ArrayList<String>());
		String stmntThen = visit(ctx.stmnt);
		for (String current : (ArrayList<String>) currentVariables.get(currentLevel)) {
			invalidateVariable(current);
		}
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
		currentVariables.put(currentLevel, new ArrayList<String>());
		String stmnt = visitStatementList(ctx.stmntList);
		for (String current : (ArrayList<String>) currentVariables.get(currentLevel)) {
			invalidateVariable(current);
		}
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

	private int requireVariableIndex(Token varNameToken) {
		Integer varIndex = variableNameToId.get(varNameToken.getText());
		if (varIndex == null) {
			/*throw new UndeclaredVariableException(varNameToken);*/
		}
		return varIndex;
	}

	private int requireVariableType(Token varNameToken) {
		Integer varType = variableNameToType.get(varNameToken.getText());
		return varType;
	}

	private int requireFunctionIndex(Token varNameToken) {
		Integer fctIndex = functions.get(varNameToken.getText());
		if (fctIndex == null) {
			/*throw new UndeclaredVariableException(varNameToken);*/
		}
		return fctIndex;
	}

	private int variableToId(String variable) {
		Integer variableId = variableNameToId.get(variable);
		if (variableId == null) {
			/*throw new UndeclaredVariableException(varNameToken);*/
		}
		return variableId;
	}

	private int variableToType(String variable) {
		Integer variableType = variableNameToType.get(variable);
		if (variableType == null) {
			/*throw new UndeclaredVariableException(varNameToken);*/
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
			default:
				return -1;
		}
	}

	private String typeToLetter(int type) {
		switch (type) {
			case 0:
				return "i";
			case 2:
				return "a";
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
