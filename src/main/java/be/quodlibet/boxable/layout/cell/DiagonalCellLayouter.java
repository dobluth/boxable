package be.quodlibet.boxable.layout.cell;

import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.layout.Style;

/**
 *
 * @author Dries Horions <dries@quodlibet.be>
 */
public class DiagonalCellLayouter implements CellLayouter {

	private final Style style;

	public DiagonalCellLayouter(final Style style) {
		this.style = style;
	}

	@Override
	public void layoutCell(Cell cell) {

		int lastHeaderRowIndex = cell.getRow().getTable().getHeader().getRowIndex();
		if ((cell.getRowIndex() - lastHeaderRowIndex) == cell.getColumnIndex()) {
			cell.withFillColor(style.getFillColor()).withTextColor(style.getTextColor())
					.withHorizontalAlignment(style.getHorizontalAlignment()).withVerticalAlignment(style.getVerticalAlignment());
		}

	}

}
