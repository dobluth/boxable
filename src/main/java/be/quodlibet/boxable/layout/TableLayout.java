package be.quodlibet.boxable.layout;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.layout.cell.CellLayouter;

/**
 * <p>
 * The table layout class bundles all information that are related to the
 * table's appearance.
 * </p>
 * 
 * @author dobluth
 *
 */
public class TableLayout {

	private final List<CellLayouter> cellLayouters = new ArrayList<>();

	private PDFont font = PDType1Font.HELVETICA;

	private PDFont fontBold = PDType1Font.HELVETICA_BOLD;

	private float fontSize = 6;

	private float pageTopMargin = 10;

	private float pageBottomMargin = 10;

	private float margin = 10;

	private float headerBottomMargin = 4f;

	private boolean drawLines = true;

	private boolean drawContent = true;

	private boolean drawDebug = false;

	public void layout(final Cell cell) {
		for (final CellLayouter layouter : cellLayouters) {
			layouter.layoutCell(cell);
		}
	}

	public TableLayout addCellLayouter(final CellLayouter cellLayouter) {
		if (cellLayouter != null) {
			cellLayouters.add(cellLayouter);
		}
		return this;
	}

	public TableLayout clearCellLayouters() {
		cellLayouters.clear();
		return this;
	}

	public float pageTopMargin() {
		return pageTopMargin;
	}

	public TableLayout pageTopMargin(final float pageTopMargin) {
		this.pageTopMargin = pageTopMargin;
		return this;
	}

	public float pageBottomMargin() {
		return pageBottomMargin;
	}

	public TableLayout pageBottomMargin(final float pageBottomMargin) {
		this.pageBottomMargin = pageBottomMargin;
		return this;
	}

	public float margin() {
		return margin;
	}

	public TableLayout margin(final float margin) {
		this.margin = margin;
		return this;
	}

	public float headerBottomMargin() {
		return headerBottomMargin;
	}

	public TableLayout headerBottomMargin(final float headerBottomMargin) {
		this.headerBottomMargin = headerBottomMargin;
		return this;
	}

	public boolean drawLines() {
		return drawLines;
	}

	public TableLayout drawLines(final boolean drawLines) {
		this.drawLines = drawLines;
		return this;
	}

	public boolean drawContent() {
		return drawContent;
	}

	public TableLayout drawContent(final boolean drawContent) {
		this.drawContent = drawContent;
		return this;
	}

	public boolean drawDebug() {
		return drawDebug;
	}

	public TableLayout drawDebug(final boolean drawDebug) {
		this.drawDebug = drawDebug;
		return this;
	}

	public PDFont font() {
		return font;
	}

	public TableLayout font(final PDFont font) {
		if (font == null) {
			throw new IllegalArgumentException("Font must not be null");
		}
		this.font = font;
		return this;
	}

	public PDFont fontBold() {
		return fontBold;
	}

	public TableLayout fontBold(final PDFont fontBold) {
		if (fontBold == null) {
			throw new IllegalArgumentException("Bold font must not be null");
		}
		this.fontBold = fontBold;
		return this;
	}

	public float fontSize() {
		return fontSize;
	}

	public TableLayout fontSize(final float fontSize) {
		this.fontSize = fontSize;
		return this;
	}
}
