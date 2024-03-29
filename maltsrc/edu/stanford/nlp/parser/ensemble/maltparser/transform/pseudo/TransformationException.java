package edu.stanford.nlp.parser.ensemble.maltparser.transform.pseudo;

import edu.stanford.nlp.parser.ensemble.maltparser.core.exception.MaltChainedException;

/**
 *  TransformationException extends the MaltChainedException class and is thrown by classes
 *  within the transform package.
 *
 * @author Johan Hall
 * @since 1.0
**/
public class TransformationException extends MaltChainedException {
	public static final long serialVersionUID = 8045568022124816379L; 
	/**
	 * Creates a TransformationException object with a message
	 * 
	 * @param message	the message
	 */
	public TransformationException(String message) {
		super(message);
	}
	/**
	 * Creates a TransformationException object with a message and a cause to the exception.
	 * 
	 * @param message	the message
	 * @param cause		the cause to the exception
	 */
	public TransformationException(String message, Throwable cause) {
		super(message, cause);
	}
}