package edu.stanford.nlp.parser.ensemble.maltparser.core.options;

import edu.stanford.nlp.parser.ensemble.maltparser.core.exception.MaltChainedException;

/**
 *  OptionException extends the MaltChainedException class and is thrown by classes
 *  within the options package.
 *
 * @author Johan Hall
 * @since 1.0
**/
public class OptionException extends MaltChainedException {
	public static final long serialVersionUID = 8045568022124816379L;

	/**
	 * Creates an OptionException object with a message
	 * 
	 * @param message	the message
	 */
	public OptionException(String message) {
		super(message);
	}
	/**
	 * Creates an OptionException object with a message and a cause to the exception.
	 * 
	 * @param message	the message
	 * @param cause		the cause to the exception
	 */
	public OptionException(String message, Throwable cause) {
		super(message, cause);
	}
}
