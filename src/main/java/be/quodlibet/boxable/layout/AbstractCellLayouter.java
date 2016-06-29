package be.quodlibet.boxable.layout;

import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.layout.style.DefaultStyle;

/**
 *
 * @author Dries Horions <dries@quodlibet.be>
 */
abstract class AbstractCellLayouter {
	
	abstract void layout(Cell cell);

	DefaultStyle style;

}
