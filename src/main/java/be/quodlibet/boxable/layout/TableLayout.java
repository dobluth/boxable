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

}
