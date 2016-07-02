package be.quodlibet.boxable.layout.cell;

import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.layout.Style;

/**
 *
 * @author Dries Horions <dries@quodlibet.be>
 */
public class DefaultCellLayouter implements CellLayouter {

	private final Style commonStyle;

	private final Style headerStyle;

	private final Style dataStyle;

	public DefaultCellLayouter(final Style commonStyle, final Style headerStyle, final Style dataStyle) {
		this.commonStyle = commonStyle;
		this.headerStyle = headerStyle;
		this.dataStyle = dataStyle;
	}

	@Override
	public void layout(final Cell cell) {
		cell.withBorder(commonStyle.getBorder()).withFont(commonStyle.getFont()).withFontBold(commonStyle.getFontBold())
				.withFontSize(commonStyle.getFontSize());

		if (cell.isHeaderCell()) {
			cell.withFillColor(headerStyle.getFillColor()).withTextColor(headerStyle.getTextColor())
					.withHorizontalAlignment(headerStyle.getHorizontalAlignment());
		} else {
			cell.withFillColor(dataStyle.getFillColor()).withTextColor(dataStyle.getTextColor())
					.withHorizontalAlignment(dataStyle.getHorizontalAlignment());
		}
	}

}
