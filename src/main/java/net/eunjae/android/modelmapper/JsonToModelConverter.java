package net.eunjae.android.modelmapper;

import org.json.JSONException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class JsonToModelConverter extends AbstractHttpMessageConverter<Object> {

	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	private final Charset defaultCharset;

	private final List<Charset> availableCharsets;

	private boolean writeAcceptCharset = true;

	private ModelMapper modelMapper;

	/**
	 * Create a new StringHttpMessageConverter instance with a default {@link Charset} of ISO-8859-1,
	 * and default list of available {@link Charset}'s from {@link Charset#availableCharsets()}.
	 */
	public JsonToModelConverter() {
		this(DEFAULT_CHARSET);
	}

	/**
	 * Create a new StringHttpMessageConverter instance with a default {@link Charset},
	 * and default list of available {@link Charset}'s from {@link Charset#availableCharsets()}.
	 *
	 * @param defaultCharset the Charset to use
	 */
	public JsonToModelConverter(Charset defaultCharset) {
		this(defaultCharset, new ArrayList<Charset>(Charset.availableCharsets().values()));
	}

	/**
	 * Create a new StringHttpMessageConverter instance with a default {@link Charset},
	 * and list of available {@link Charset}'s.
	 *
	 * @param defaultCharset    the Charset to use
	 * @param availableCharsets the list of available Charsets
	 */
	public JsonToModelConverter(Charset defaultCharset, List<Charset> availableCharsets) {
		super(new MediaType("application", "json", defaultCharset), MediaType.ALL);
		this.defaultCharset = defaultCharset;
		this.availableCharsets = availableCharsets;
		init();
	}

	protected void init() {
		this.modelMapper = ModelMapper.newInstance();
	}

	@Override
	protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		Charset charset = getContentTypeCharset(inputMessage.getHeaders().getContentType());
		String json = FileCopyUtils.copyToString(new InputStreamReader(inputMessage.getBody(), charset));
		return readInternal(clazz, json);
	}

	protected Object readInternal(Class<?> clazz, String json) {
		Object obj = null;
		try {
			obj = modelMapper.generate(clazz, json);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}

	@Override
	protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
		String s = (String) o;
		if (writeAcceptCharset) {
			outputMessage.getHeaders().setAcceptCharset(getAcceptedCharsets());
		}
		Charset charset = getContentTypeCharset(outputMessage.getHeaders().getContentType());
		FileCopyUtils.copy(s, new OutputStreamWriter(outputMessage.getBody(), charset));
	}

	/**
	 * The default {@link Charset} is ISO-8859-1. Can be overridden in subclasses,
	 * or through the use of the alternate constructor.
	 *
	 * @return default Charset
	 */
	public Charset getDefaultCharset() {
		return this.defaultCharset;
	}

	/**
	 * Indicates whether the {@code Accept-Charset} should be written to any outgoing request.
	 * <p/>
	 * Default is {@code true}.
	 */
	public void setWriteAcceptCharset(boolean writeAcceptCharset) {
		this.writeAcceptCharset = writeAcceptCharset;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}

	protected List<Charset> getAcceptedCharsets() {
		return this.availableCharsets;
	}

	private Charset getContentTypeCharset(MediaType contentType) {
		if (contentType != null && contentType.getCharSet() != null) {
			return contentType.getCharSet();
		} else {
			return getDefaultCharset();
		}
	}
}