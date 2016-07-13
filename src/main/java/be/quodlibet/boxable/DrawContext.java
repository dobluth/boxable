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
class DrawContext implements DrawResult {

	private final PageProvider pageProvider;

	private PDPageContentStream stream;

	private boolean removeTopBorders = false;

	private boolean tableIsBroken;

	private float yPosition;

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

	public boolean removeTopBorders() {
		return removeTopBorders;
	}

	public DrawContext removeTopBorders(final boolean removeTopBorders) {
		this.removeTopBorders = removeTopBorders;
		return this;
	}

	public DrawContext markTableBroken() {
		tableIsBroken = true;
		return this;
	}

	@Override
	public boolean tableIsBroken() {
		return tableIsBroken;
	}

	public DrawContext yPosition(final float yPosition) {
		this.yPosition = yPosition;
		return this;
	}

	@Override
	public float yPosition() {
		return yPosition;
	}

	public DrawContext advanceY(final float distance) {
		yPosition -= distance;
		return this;
	}
}
