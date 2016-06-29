package be.quodlibet.boxable.layout;

import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.layout.style.DefaultStyle;

/**
 *
 * @author Dries Horions <dries@quodlibet.be>
 */
public class MatrixCellLayouter extends DefaultCellLayouter {

	public MatrixCellLayouter(DefaultStyle style) {
		super(style);
	}

	public MatrixCellLayouter(DefaultStyle.Styles s) {
		super(s);
	}

	@Override
	public void layout(Cell cell) {

		int lastHeaderRowIndex = cell.getRow().getTable().getHeader().getRowIndex();
		if ((cell.getRowIndex() - lastHeaderRowIndex) == cell.getColumnIndex()) {
			cell.withFillColor(style.fillcolorAccent2).withTextColor(style.textcolorAccent2)
					.withAlign(style.alignAccent2).withValign(style.valignAccent2);
		}

	}

}
