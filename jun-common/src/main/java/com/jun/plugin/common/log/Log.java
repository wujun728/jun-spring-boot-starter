
package com.jun.plugin.common.log;

import com.jun.plugin.common.util.Charsets;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * 替换 系统 System.err 和 System.out 为log
 *
 * @author L.cm
 */
@Slf4j
public class Log extends PrintStream {
	private final boolean error;

	private Log(boolean error) throws UnsupportedEncodingException {
		super(error ? System.err : System.out, false, Charsets.UTF_8_NAME);
		this.error = error;
	}

	public static Log log(boolean isError) {
		try {
			return new Log(isError);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void print(String s) {
		if (error) {
			log.error(s);
		} else {
			log.info(s);
		}
	}

	/**
	 * 重写掉它，因为它会打印很多无用的新行
	 */
	@Override
	public void println() {
	}

	@Override
	public void println(String x) {
		if (error) {
			log.error(x);
		} else {
			log.info(x);
		}
	}

	@Override
	public PrintStream printf(String format, Object... args) {
		if (error) {
			log.error(String.format(format, args));
		} else {
			log.info(String.format(format, args));
		}
		return this;
	}

	@Override
	public PrintStream printf(Locale l, String format, Object... args) {
		if (error) {
			log.error(String.format(l, format, args));
		} else {
			log.info(String.format(l, format, args));
		}
		return this;
	}

	public static void main(String[] args) {
		Log.log.info("1111111111111111111");
		Log.log.error("fdafdcdee``````````````````");
	}
}
