package edu.uwm.cs351;

import java.util.EmptyStackException;
import edu.uwm.cs351.util.IntMath;
import edu.uwm.cs351.util.Stack;

/**
 * Class to perform integer calculations online given method calls.
 * It uses normal arithmetic operator precedence, defined on the Operation enum,
 * and assumes left associativity. A calculator can be in one of three states:
 * <ol>
 * <li> Clear: Nothing pending
 * <li> Ready: A value is available
 * <li> Waiting: An operator has been started and we're waiting for a value
 * </ol>
 * At any point if a division by zero is caused, the appropriate exception is raised.
 */
public class Calculator {
	private Stack<Long> operands = new Stack<Long>();
	private Stack<Operation> operators = new Stack<Operation>();
	
	private long defaultValue;
	private boolean expectingValue;
	
	/**
	 * Create a calculator in the "clear" state with "0" as the default value.
	 */
	public Calculator() { 
		//TODO initialize the fields
		//	This depends on which design you choose.
		defaultValue = 0;
		expectingValue = true;
	}
	
	/**
	 * Enter a value into the calculator.
	 * The current value is changed to the argument.
	 * @pre not "Ready" 
	 * @post "Ready"
	 * @param x value to enter
	 * @exception IllegalStateException if precondition not met
	 */
	public void value(long x) {
		// TODO implement this
		if (!expectingValue && !operands.isEmpty()) throw new IllegalStateException();
		
		operands.push(x);
		
		expectingValue = false;
	}
	
	/**
	 * Start a parenthetical expression.
	 * @pre not "Ready" 
	 * @post "Waiting"
	 * @exception IllegalStateException if precondition not met
	 */
	public void open() {
		// TODO implement this
		if (!expectingValue && !operands.isEmpty()) throw new IllegalStateException();
		
		operators.push(Operation.LPAREN);
		
		expectingValue = true;
	}
	
	/**
	 * End a parenthetical expression.
	 * The current value shows the computation result since
	 * @pre "Ready"
	 * @post "Ready"
	 * @throws EmptyStackException if no previous unclosed open.
	 * @exception IllegalStateException if precondition not met
	 */
	public void close() {
		// TODO implement this
		if (expectingValue) throw new IllegalStateException();
		if (operands.isEmpty() && operators.isEmpty()) throw new IllegalStateException();
		
		Operation op = operators.pop();
		if (op.equals(Operation.LPAREN)) {
			return;
		}
		
		long tmpPop = operands.pop();
		if (!operands.isEmpty()) {
			operands.push(tmpPop);
			do {
				long d2 = operands.pop();
				if (operands.isEmpty()) {
					operands.push(op.operate(defaultValue, d2));
					throw new EmptyStackException();
				}
				long d1 = operands.pop();
				if (op.equals(Operation.DIVIDE) && d2 == 0) {
					clear();
					defaultValue = 0;
					throw new ArithmeticException();
				}
				else {
					operands.push(op.operate(d1, d2));
				}
				op = operators.pop();
			} while (!op.equals(Operation.LPAREN));
		}
		else {
			if (op.equals(Operation.DIVIDE) && tmpPop == 0) {
				clear();
				defaultValue = 0;
				throw new ArithmeticException();
			}
			operands.push(op.operate(defaultValue, tmpPop));
			throw new EmptyStackException();
		}
		expectingValue = false;	
	}
	
	/**
	 * Start an operation using the previous computation and waiting for another argument.
	 * @param op operation to use, must be a binary operation, not null or a parenthesis.
	 * @pre not "Waiting"
	 * @post "Waiting"
	 * @throws IllegalArgumentException if the operator is illegal
	 * @exception IllegalStateException if precondition not met
	 */
	public void binop(Operation op) {
		// TODO implement this
		if (expectingValue && !operands.isEmpty()) throw new IllegalStateException();
		if (operands.isEmpty() && !operators.isEmpty()) throw new IllegalStateException();
		
		if (op.equals(Operation.LPAREN) || op.equals(Operation.RPAREN)) throw new IllegalArgumentException();
		
		if (!operators.isEmpty() && (op.precedence() <= operators.peek().precedence())) {
			while (!operands.isEmpty() && !operators.isEmpty() && (op.precedence() <= operators.peek().precedence())) {
				step();
			}
		}
		operators.push(op);
		
		expectingValue = true;
	}

	/**
	 * Replace the current value with its unsigned integer square root.
	 * @see IntMath#isqrt(long)
	 * @pre not "Waiting"
	 * @post "Ready"
	 * @exception IllegalStateException if precondition not met
	 */
	public void sqrt() {
		// TODO implement this
		if (expectingValue && !operands.isEmpty()) throw new IllegalStateException();
		if (operands.isEmpty() && !operators.isEmpty()) throw new IllegalStateException();
		
		if (operands.isEmpty()) {
			operands.push(IntMath.isqrt(defaultValue));
		}
		else {
			operands.push(IntMath.isqrt(operands.pop()));
		}
		expectingValue = false;
	}
	
	/**
	 * Compute one step.
	 */
	private void step() {
		// TODO implement this
		
		long tmpPop = operands.pop();
		if (operands.isEmpty()) {
			Operation op = operators.pop();
			if (!op.equals(Operation.LPAREN)) {
				if (op.equals(Operation.DIVIDE) && tmpPop == 0) {
					clear();
					defaultValue = 0;
					throw new ArithmeticException();
				}
				operands.push(op.operate(defaultValue, tmpPop));
			}
			else {
				operands.push(tmpPop);
			}
			return;
		}
		else {
			operands.push(tmpPop);
			long d2 = operands.pop();
			long d1 = operands.pop();
			Operation op = operators.pop();
			if (op.equals(Operation.DIVIDE) && d2 == 0) {
				clear();
				defaultValue = 0;
				throw new ArithmeticException();
			}
			else {
				operands.push(op.operate(d1, d2));
			}
		}
	}
	
	/**
	 * Return the current value.
	 * This is the last entered or computed value.
	 * @return current value.
	 */
	public long getCurrent() {
		if (operands.isEmpty())
			return defaultValue;
		else
			return operands.peek();
		// TODO implement this
	}
	
	/**
	 * Perform any pending calculations.
	 * Any previously unclosed opens are closed in the process.
	 * The new default value is the result of the computation.
	 * @pre not "Waiting"
	 * @post "Empty"
	 * @return result of computation
	 * @exception IllegalStateException if precondition not met
	 */
	public long compute() {
		// TODO implement this
		if (expectingValue && !operands.isEmpty()) throw new IllegalStateException();
		if (operands.isEmpty() && !operators.isEmpty()) throw new IllegalStateException();
		
		if (operands.isEmpty()) {
			expectingValue = true;
			return defaultValue;
		}
		if (operators.isEmpty()) {
			defaultValue = operands.pop();
			expectingValue = true;
			return defaultValue;
		}
		
		while (!operands.isEmpty() && !operators.isEmpty()) {
			long tmpPop = operands.pop();
			if (operands.isEmpty()) {
				do {
					Operation op = operators.pop();
					if (op.equals(Operation.LPAREN)) continue;
					if (op.equals(Operation.DIVIDE) && tmpPop == 0) {
						clear();
						defaultValue = 0;
						throw new ArithmeticException();
					}
					defaultValue = op.operate(defaultValue, tmpPop);
					expectingValue = true;
					if (!operands.isEmpty()) {
						operands.pop();
					}
					return defaultValue;
				} while (!operators.isEmpty());
				operands.push(tmpPop);
				break;
			}
			operands.push(tmpPop);
			Operation op = operators.pop();
			if (op.equals(Operation.LPAREN)) continue;
			long d2 = operands.pop();
			long d1 = operands.pop();
			if (op.equals(Operation.DIVIDE) && d2 == 0) {
				clear();
				defaultValue = 0;
				throw new ArithmeticException();
			}
			else {
				operands.push(op.operate(d1, d2));
			}
		
		}
		expectingValue = true;
		defaultValue = operands.peek();
		return operands.pop();	
	}
	
	/**
	 * Clear the calculator, reseting the default value to zero.
	 * @post "Clear"
	 */
	public void clear() {
		// TODO implement this
		while (!operands.isEmpty()) {
			operands.pop();
		}
		while (!operators.isEmpty()) {
			operators.pop();
		}
		expectingValue = true;
		defaultValue = 0;
	}
	
}
