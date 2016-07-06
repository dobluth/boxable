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

	private PDPageContentStream stream;

	public void closeStream() throws IOException {
		if (stream != null) {
			stream.close();
			stream = null;
		}
	}

	public PDPageContentStream stream(final PageProvider pageProvider) throws IOException {
		if (stream == null) {
			stream = PDStreamUtils.createStream(pageProvider);
		}
		return stream;
	}
}
