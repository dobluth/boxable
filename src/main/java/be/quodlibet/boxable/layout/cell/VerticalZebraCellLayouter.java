package be.quodlibet.boxable.layout.cell;

import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.layout.Style;

/**
 *
 * @author Dries Horions <dries@quodlibet.be>
 */
public class VerticalZebraCellLayouter implements CellLayouter {

	private final Style oddStyle;

	private final Style evenStyle;

	public VerticalZebraCellLayouter(final Style oddStyle, final Style evenStyle) {
		this.oddStyle = oddStyle;
		this.evenStyle = evenStyle;
	}

	@Override
	public void layout(Cell cell) {
		if (!cell.isHeaderCell()) {
			if (cell.getColumnIndex() % 2 == 0) {
				cell.withFillColor(evenStyle.getFillColor()).withTextColor(evenStyle.getTextColor())
						.withHorizontalAlignment(evenStyle.getHorizontalAlignment()).withVerticalAlignment(evenStyle.getVerticalAlignment());
			} else {
				cell.withFillColor(oddStyle.getFillColor()).withTextColor(oddStyle.getTextColor())
						.withHorizontalAlignment(oddStyle.getHorizontalAlignment()).withVerticalAlignment(oddStyle.getVerticalAlignment());
			}
		}
	}

}
