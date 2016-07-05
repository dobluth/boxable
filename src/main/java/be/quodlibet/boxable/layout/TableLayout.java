package be.quodlibet.boxable.layout;

import java.util.ArrayList;
import java.util.List;

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
}
