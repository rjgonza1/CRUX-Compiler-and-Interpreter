package mips;

import java.util.regex.Pattern;

import ast.*;
import types.*;

public class CodeGen implements ast.CommandVisitor {
    
    private StringBuffer errorBuffer = new StringBuffer();
    private TypeChecker tc;
    private Program program;
    private ActivationRecord currentFunction;
    private String funcLabel;

    public CodeGen(TypeChecker tc)
    {
        this.tc = tc;
        this.program = new Program();
    }
    
    public boolean hasError()
    {
        return errorBuffer.length() != 0;
    }
    
    public String errorReport()
    {
        return errorBuffer.toString();
    }

    private class CodeGenException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
        public CodeGenException(String errorMessage) {
            super(errorMessage);
        }
    }
    
    public boolean generate(Command ast)
    {
        try {
            currentFunction = ActivationRecord.newGlobalFrame();
            ast.accept(this);
            return !hasError();
        } catch (CodeGenException e) {
            return false;
        }
    }
    
    public Program getProgram()
    {
        return program;
    }

    @Override
    public void visit(ExpressionList node) 
    {
        for(Expression e: node)
        	e.accept(this);
    }

    @Override
    public void visit(DeclarationList node) 
    {
    	for(Declaration d: node)
        	d.accept(this);
    }

    @Override
    public void visit(StatementList node) 
    {
        for(Statement s: node)
        {
        	s.accept(this);
        	if(s instanceof Call)
        	{
        		Type statementType = tc.getType((Command) s); 
        		if(!(statementType.equivalent(new VoidType())))
        		{
        			if(statementType.equivalent(new IntType()))
            			program.popInt("$t0");
            		else if(statementType.equivalent(new FloatType()))
            			program.popFloat("$f0");
        		}
        	}
        }
    }

    @Override
    public void visit(AddressOf node) 
    {
        currentFunction.getAddress(program, "$t0", node.symbol());
        program.pushInt("$t0");
    }

    @Override
    public void visit(LiteralBool node) 
    {
    	if(node.value() == ast.LiteralBool.Value.TRUE)
    		program.appendInstruction("li $t0, 1");
    	else
    		program.appendInstruction("li $t0, 0");
    	program.pushInt("$t0");
    }

    @Override
    public void visit(LiteralFloat node) 
    {
        program.appendInstruction("li.s $f0, " + node.value());
        program.pushFloat("$f0");
    }

    @Override
    public void visit(LiteralInt node) 
    {
        program.appendInstruction("li $t0, " + node.value());
        program.pushInt("$t0");
    }

    @Override
    public void visit(VariableDeclaration node) 
    {
    	currentFunction.add(program, node);
    }

    @Override
    public void visit(ArrayDeclaration node) 
    {
    	currentFunction.add(program, node);
    }

    @Override
    public void visit(FunctionDefinition node) 
    {
    	boolean isMain = node.symbol().name().equals("main");
    	int functionPos = 0;
    	funcLabel = program.newLabel();
    	if(isMain)
    		functionPos = program.appendInstruction(node.symbol().name() + ": ");
    	else
    		functionPos = program.appendInstruction("func." + node.symbol().name() + ": ");
    	
        currentFunction = new ActivationRecord(node, currentFunction);
        
        node.body().accept(this);
        program.insertPrologue(functionPos+1, currentFunction.stackSize());
        
        program.appendInstruction(funcLabel + ": ");
        Type retType = ((FuncType) node.symbol().type()).returnType();
        if(!(retType.equivalent(new VoidType())))
        {
        	if(retType.equivalent(new IntType()) || retType.equivalent(new BoolType()))
        		program.popInt("$v0");
        	else if(retType.equivalent(new FloatType()))
        		program.popFloat("$v0");
        }
        
        if(isMain)
        	program.appendExitSequence();
        else
        {
        	program.appendEpilogue(currentFunction.stackSize());
        	currentFunction = currentFunction.parent();   
        }  	 	
    }

    @Override
    public void visit(Addition node) 
    {
        node.leftSide().accept(this);
        node.rightSide().accept(this);
        Type nodeType = tc.getType(node);
        if(nodeType.equivalent(new IntType()))
        {
        	program.popInt("$t1");
        	program.popInt("$t0");
        	program.appendInstruction("add $t2, $t1, $t0");
        	program.pushInt("$t2");
        }
        else if(nodeType.equivalent(new FloatType()))
        {
        	program.popFloat("$f1");
        	program.popFloat("$f0");
        	program.appendInstruction("add.s $f2, $f1, $f0");
        	program.pushFloat("$f2");
        }
    }

    @Override
    public void visit(Subtraction node) 
    {
    	node.leftSide().accept(this);
        node.rightSide().accept(this);
        Type nodeType = tc.getType(node);
        if(nodeType.equivalent(new IntType()))
        {
        	program.popInt("$t1");
        	program.popInt("$t0");
        	program.appendInstruction("sub $t2, $t0, $t1");
        	program.pushInt("$t2");
        }
        else if(nodeType.equivalent(new FloatType()))
        {
        	program.popFloat("$f1");
        	program.popFloat("$f0");
        	program.appendInstruction("sub.s $f2, $f0, $f1");
        	program.pushFloat("$f2");
        }    
    }

    @Override
    public void visit(Multiplication node) 
    {
    	node.leftSide().accept(this);
        node.rightSide().accept(this);
        Type nodeType = tc.getType(node);
        if(nodeType.equivalent(new IntType()))
        {
        	program.popInt("$t1");
        	program.popInt("$t0");
        	program.appendInstruction("mul $t2, $t1, $t0");
        	program.pushInt("$t2");
        }
        else if(nodeType.equivalent(new FloatType()))
        {
        	program.popFloat("$f1");
        	program.popFloat("$f0");
        	program.appendInstruction("mul.s $f2, $f1, $f0");
        	program.pushFloat("$f2");
        }    
    }

    @Override
    public void visit(Division node) 
    {
    	node.leftSide().accept(this);
        node.rightSide().accept(this);
        Type nodeType = tc.getType(node);
        if(nodeType.equivalent(new IntType()))
        {
        	program.popInt("$t1");
        	program.popInt("$t0");
        	program.appendInstruction("div $t2, $t1, $t0");
        	program.pushInt("$t2");
        }
        else if(nodeType.equivalent(new FloatType()))
        {
        	program.popFloat("$f1");
        	program.popFloat("$f0");
        	program.appendInstruction("div.s $f2, $f1, $f0");
        	program.pushFloat("$f2");
        }
    }

    @Override
    public void visit(LogicalAnd node)
    {
    	node.leftSide().accept(this);
        node.rightSide().accept(this);
        program.popInt("$t1");
       	program.popInt("$t0");
        program.appendInstruction("and $t2, $t1, $t0");
        program.pushInt("$t2");
    }

    @Override
    public void visit(LogicalOr node) 
    {
    	node.leftSide().accept(this);
        node.rightSide().accept(this);
        program.popInt("$t1");
       	program.popInt("$t0");
        program.appendInstruction("or $t2, $t1, $t0");
        program.pushInt("$t2");
    }
    
    @Override
    public void visit(LogicalNot node) 
    {
       	program.popInt("$t0");
        program.appendInstruction("neg $t1, $t0");
        program.pushInt("$t2");
    }

    @Override
    public void visit(Comparison node) 
    {
        node.leftSide().accept(this);
        node.rightSide().accept(this);
        ast.Comparison.Operation nodeOp = node.operation();
        Type nodeType = tc.getType((Command) node.leftSide());
        String falseLabel = program.newLabel();
        String trueLabel = program.newLabel();
        
        if(nodeType.equivalent(new IntType()))
        {
        	program.popInt("$t1");
    		program.popInt("$t0");
        	switch(nodeOp)
            {
            	case GT: 	
            		program.appendInstruction("sgt $t2, $t0, $t1");
            		break;
            	case GE:
            		program.appendInstruction("sge $t2, $t0, $t1");
            		break;
            	case EQ:
            		program.appendInstruction("seq $t2, $t0, $t1");
            		break;
            	case NE:
            		program.appendInstruction("sne $t2, $t0, $t1");
            		break;
            	case LE:
            		program.appendInstruction("sle $t2, $t0, $t1");
            		break;
            	case LT:
            		program.appendInstruction("slt $t2, $t0, $t1");
            		break;
            	default:
            		throw new CodeGenException("Not a comparison type"); //shouldn't happen
            }
        }
        else if(nodeType.equivalent(new FloatType()))
        {
        	program.popFloat("$f2");
        	program.popFloat("$f0");
        	switch(nodeOp)
            {
            	case GT: 	
            		program.appendInstruction("c.lt.s $f0, $f1");
            		getFloatCondition(falseLabel, trueLabel);
            		break;
            	case GE:
            		program.appendInstruction("c.le.s $f0, $f2");
            		getFloatCondition(falseLabel, trueLabel);
            		break;
            	/*Special case for EQ/NE because of interchangable lhs and rhs */
            	case EQ:
            		program.appendInstruction("c.eq.s $f0, $f2");
            		program.appendInstruction("bc1f " + falseLabel);
                	program.appendInstruction("li $t2, 1");
                	program.appendInstruction("b " + trueLabel);
                	program.appendInstruction(falseLabel + ": ");
                	program.appendInstruction("li $t2, 0");
                	program.appendInstruction(trueLabel + ": ");
            		break;
            	case NE:
            		program.appendInstruction("c.eq.s $f0, $f2");
            		program.appendInstruction("bc1f " + falseLabel);
                	program.appendInstruction("li $t2, 0");
                	program.appendInstruction("b " + trueLabel);
                	program.appendInstruction(falseLabel + ": ");
                	program.appendInstruction("li $t2, 1");
                	program.appendInstruction(trueLabel + ": ");
            		break;
            	case LE:
            		program.appendInstruction("c.le.s $f0, $f2");
            		getFloatCondition(falseLabel, trueLabel);
            		break;
            	case LT:
            		program.appendInstruction("c.lt.s $f0, $f2");
            		getFloatCondition(falseLabel, trueLabel);
            		break;
            	default:
            		throw new CodeGenException("Not a comparison type"); //shouldn't happen
            }
        }
        program.pushInt("$t2");
    }

    @Override
    public void visit(Dereference node) 
    {
    	node.expression().accept(this);
    	program.popInt("$t0");
        Type nodeType = tc.getType(node);
        if(nodeType.equivalent(new IntType()) || nodeType.equivalent(new BoolType()))
        {
        	program.appendInstruction("lw $t1, 0($t0)");
        	program.pushInt("$t1");
        }
        else if(nodeType.equivalent(new FloatType()))
        {
        	program.appendInstruction("lwc1 $f0, 0($t0)");
        	program.pushFloat("$f0");
        }
    }

    @Override
    public void visit(Index node) 
    {
    	node.base().accept(this);
    	node.amount().accept(this);
    	program.popInt("$t0"); //amount
    	program.popInt("$t1"); //base
    	int indexAmount = currentFunction.getIndexOffset(tc, node.amount());  
    	program.appendInstruction("li $t2, " + indexAmount);
    	program.appendInstruction("mul $t3, $t0, $t2");
    	program.appendInstruction("add $t4, $t1, $t3");
    	program.pushInt("$t4");
    }

    @Override
    public void visit(Assignment node) 
    {
    	node.destination().accept(this);
        node.source().accept(this);
        Type assignType = tc.getType((Command) node.source());
        if(assignType.equivalent(new IntType()) || assignType.equivalent(new BoolType()))
        {
        	program.popInt("$t0");
        	program.popInt("$t1");
        	program.appendInstruction("sw $t0, 0($t1)");
        }
        else if(assignType.equivalent(new FloatType()))
        {
        	program.popFloat("$f0");
        	program.popInt("$t0");
        	program.appendInstruction("swc1 $f0, 0($t0)");
        }
    }

    @Override
    public void visit(Call node) 
    {
        node.arguments().accept(this);
        FuncType funcType = (FuncType) node.function().type();
//        if(node.function().name().matches("(read|print)(Bool|Float|Int|ln)"))
        program.appendInstruction("jal func." + node.function().name());
//        else
//        	program.appendInstruction("jal cruxfunc." + node.function().name());
        
        int argOffset = currentFunction.getArgOffset(tc, node.arguments());
        if(argOffset > 0)
        	program.appendInstruction("addi $sp, $sp, " + String.valueOf(argOffset));
        
        if(!(funcType.returnType().equivalent(new VoidType())))
        {
        	program.appendInstruction("subu $sp, $sp, 4");
        	program.appendInstruction("sw $v0, 0($sp)");
        }
    }

    @Override
    public void visit(IfElseBranch node) 
    {
    	String elseRetLabel = program.newLabel();
    	String joinRetLabel = program.newLabel();
    	
    	node.condition().accept(this);
    	program.popInt("$t5");
    	program.appendInstruction("beqz $t5, " + elseRetLabel);
    	
        node.thenBlock().accept(this);
        program.appendInstruction("b " + joinRetLabel);
        
        program.appendInstruction(elseRetLabel + ": ");
        node.elseBlock().accept(this);
        
        program.appendInstruction(joinRetLabel + ": ");
    }

    @Override
    public void visit(WhileLoop node) 
    {
    	String condRetLabel = program.newLabel();
    	String joinRetLabel = program.newLabel();
    	
    	program.appendInstruction(condRetLabel + ": ");
    	node.condition().accept(this);
    	program.popInt("$t6");
    	program.appendInstruction("beqz $t6, " + joinRetLabel);
    	
    	node.body().accept(this);
    	program.appendInstruction("b " + condRetLabel);
    	program.appendInstruction(joinRetLabel + ": ");
    }

    @Override
    public void visit(Return node) 
    {
        node.argument().accept(this);
        program.appendInstruction("b " + funcLabel);
    }

    @Override
    public void visit(ast.Error node) {
        String message = "CodeGen cannot compile a " + node;
        errorBuffer.append(message);
        throw new CodeGenException(message);
    }
    
    private void getFloatCondition(String falseLabel, String trueLabel)
    {
    	program.appendInstruction("bc1f " + falseLabel);
    	program.appendInstruction("li $t2, 1");
    	program.appendInstruction("b " + trueLabel);
    	program.appendInstruction(falseLabel + ": ");
    	program.appendInstruction("li $t2, 0");
    	program.appendInstruction(trueLabel + ": ");
    }
}
