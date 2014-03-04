package net.eunjae.android.modelmapper;

import org.springframework.http.converter.HttpMessageNotReadableException;

public class InvalidCallbackMethodException extends HttpMessageNotReadableException {

	public InvalidCallbackMethodException(String s) {
		super(s);
	}
}
