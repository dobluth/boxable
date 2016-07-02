package be.quodlibet.boxable.layout.cell;

import be.quodlibet.boxable.Cell;

/**
 * <p>
 * A {@code CellLayouter} sets the layout (either the whole layout or just a
 * part) for a {@linkplain Cell}. The layout of a cell consists of all
 * attributes of the cell that control its appearance (for example borders,
 * fonts, colors, but not its position).
 * </p>
 * <p>
 * A {@code CellLayouter} can leave a cell unchanged, so it is possible to have
 * a {@code CellLayouter} that only affects {@linkplain Cell#isHeaderCell()
 * header cells}, for example.
 * </p>
 * <p>
 * Several {@code CellLayouter}s can be combined to achieve any desirable
 * effect.
 * </p>
 * 
 * @author dobluth
 *
 */
public interface CellLayouter {

	/**
	 * <p>
	 * May set some or all layout attributes of the specified {@linkplain Cell}
	 * (or even none).
	 * </p>
	 * 
	 * @param cell
	 *            The {@linkplain Cell}
	 */
	public void layout(Cell cell);
}
