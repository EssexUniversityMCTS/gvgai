package controllers.Return42.util;

/**
 * Indicates, that a timeout occurred during an operation.
 * Only makes sense for operations which take an ElapsedCpuTimer as argument.
 */
public class TimeoutException extends Exception {

	private static final long serialVersionUID = -8403816718378949568L;
}
