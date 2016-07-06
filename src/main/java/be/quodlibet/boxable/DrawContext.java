package be.quodlibet.boxable;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDPageContentStream;

import be.quodlibet.boxable.page.PageProvider;
import be.quodlibet.boxable.utils.PDStreamUtils;

/**
 * <p>
 * Holds data that is needed in the process of
 * {@linkplain Table#draw(be.quodlibet.boxable.layout.TableLayout, be.quodlibet.boxable.page.PageProvider)
 * drawing a table}.
 * </p>
 * 
 * @author dobluth
 *
 */
class DrawContext {

	private final PageProvider pageProvider;

	private PDPageContentStream stream;

	public DrawContext(final PageProvider pageProvider) {
		this.pageProvider = pageProvider;
	}

	public void closeStream() throws IOException {
		if (stream != null) {
			stream.close();
			stream = null;
		}
	}

	public PDPageContentStream stream() throws IOException {
		if (stream == null) {
			stream = PDStreamUtils.createStream(pageProvider);
		}
		return stream;
	}

	public PageProvider pageProvider() {
		return pageProvider;
	}
}
