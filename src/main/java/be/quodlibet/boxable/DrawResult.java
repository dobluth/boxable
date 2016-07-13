package be.quodlibet.boxable;

/**
 * <p>
 * Reflects the resulting state after
 * {@linkplain Table#draw(be.quodlibet.boxable.layout.TableLayout, be.quodlibet.boxable.page.PageProvider)
 * drawing} a {@linkplain Table}.
 * </p>
 * 
 * @author dobluth
 *
 */
public interface DrawResult {

	public boolean tableIsBroken();

	public float yPosition();
}