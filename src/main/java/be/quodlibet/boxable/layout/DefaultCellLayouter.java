package be.quodlibet.boxable.layout;

import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.layout.style.DefaultStyle;

/**
 *
 * @author Dries Horions <dries@quodlibet.be>
 */
public class DefaultCellLayouter extends AbstractCellLayouter {

	public DefaultCellLayouter(DefaultStyle style) {
		this.style = style;
	}

	public DefaultCellLayouter(DefaultStyle.Styles s) {
		style = new DefaultStyle(s);
	}

	@Override
	public void layout(Cell cell) {
		cell.withBorder(style.border).withFont(style.font).withFontBold(style.font).withFontSize(style.fontsize);

		if (cell.isHeaderCell()) {
			// All header cells accept the last one use accentcolor1
			if (cell.getRow().equals(cell.getRow().getTable().getHeader())) {
				cell.withFillColor(style.fillcolorAccent2).withTextColor(style.textcolorAccent2)
						.withAlign(style.alignAccent2);
			} else {
				cell.withFillColor(style.fillcolorAccent1).withTextColor(style.textcolorAccent1)
						.withAlign(style.alignAccent1);
			}
		} else {
			cell.withFillColor(style.fillcolorDefault).withTextColor(style.textcolorDefault)
					.withAlign(style.alignDefault);
		}
	}

}
