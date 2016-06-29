package be.quodlibet.boxable.layout;

import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.layout.style.DefaultStyle;

/**
 *
 * @author Dries Horions <dries@quodlibet.be>
 */
public class VerticalZebraCellLayouter extends DefaultCellLayouter {

	public VerticalZebraCellLayouter(DefaultStyle style) {
		super(style);
	}

	public VerticalZebraCellLayouter(DefaultStyle.Styles s) {
		super(s);
	}

	@Override
	public void layout(Cell cell) {
		if (!cell.isHeaderCell()) {
			if (cell.getColumnIndex() % 2 == 0) {
				cell.withFillColor(style.fillcolorAccent5).withTextColor(style.textcolorAccent5)
						.withAlign(style.alignAccent5).withValign(style.valignAccent5);
			} else {
				cell.withFillColor(style.fillcolorAccent6).withTextColor(style.textcolorAccent6)
						.withAlign(style.alignAccent6).withValign(style.valignAccent6);
			}
		}
	}

}
