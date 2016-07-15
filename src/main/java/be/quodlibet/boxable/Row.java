
/*
 Quodlibet.be
 */
package be.quodlibet.boxable;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import be.quodlibet.boxable.image.Image;
import be.quodlibet.boxable.layout.TableLayout;

public class Row {

	public static Boolean HEADER = true;
	public static Boolean NOHEADER = false;
	private Table table;
	PDOutlineItem bookmark;
	List<Cell> cells;
	private boolean headerRow = false;
	float height;

	Row(Table table, List<Cell> cells, float height) {
		this.table = table;
		this.cells = cells;
		this.height = height;
	}

	Row(Table table, float height) {
		this.table = table;
		this.cells = new ArrayList<>();
		this.height = height;
	}

	Row(Table table) {
		this.table = table;
		this.cells = new ArrayList<>();

	}

	/**
	 * <p>
	 * Creates a cell with provided width, cell value and default left top
	 * alignment
	 * </p>
	 *
	 * @param width
	 * @param value
	 * @return
	 */
	public Cell createCell(float widthPct, String value) {
		Cell cell = new Cell(this, widthPct, value, true);
		if (headerRow) {
			// set all cell as header cell
			cell.setHeaderCell(true);
		}
		setBorders(cell, cells.isEmpty());
		if (headerRow) {
			// set all cell as header cell
			cell.setHeaderCell(true);
		}
		cells.add(cell);
		return cell;
	}

	/**
	 * <p>
	 * Creates a image cell with provided width and {@link Image}
	 * </p>
	 *
	 * @param width
	 *            Cell's width
	 * @param img
	 *            {@link Image} in the cell
	 * @return
	 */
	public ImageCell createImageCell(float width, Image img) {
		ImageCell cell = new ImageCell(this, width, img, true);
		setBorders(cell, cells.isEmpty());
		cells.add(cell);
		return cell;
	}

	public Cell createImageCell(float width, Image img, HorizontalAlignment align, VerticalAlignment valign) {
		Cell cell = new ImageCell(this, width, img, true, align, valign);
		setBorders(cell, cells.isEmpty());
		cells.add(cell);
		return cell;
	}

	/**
	 * <p>
	 * Creates a cell with provided width, cell value, horizontal and vertical
	 * alignment
	 * </p>
	 *
	 * @param width
	 * @param value
	 * @param align
	 * @param valign
	 * @return
	 */
	public Cell createCell(float width, String value, HorizontalAlignment align, VerticalAlignment valign) {
		Cell cell = new Cell(this, width, value, true, align, valign);
		if (headerRow) {
			// set all cell as header cell
			cell.setHeaderCell(true);
		}
		setBorders(cell, cells.isEmpty());
		if (headerRow) {
			// set all cell as header cell
			cell.setHeaderCell(true);
		}
		cells.add(cell);
		return cell;
	}

	/**
	 * <p>
	 * Creates a cell with the same width as the corresponding header cell
	 * </p>
	 *
	 * @param value
	 * @return
	 */
	public Cell createCell(String value) {
		float headerCellWidth = 0;
		float headerCellWidthPct = 0;
		if (table.getHeader().getCells().size() > cells.size() & !this.isHeaderRow()) {
			headerCellWidth = table.getHeader().getCells().get(cells.size()).getWidth();
			headerCellWidthPct = table.getHeader().getCells().get(cells.size()).getWidthPct();
		} else {
			headerCellWidth = 0;
			headerCellWidthPct = 0;
		}
		Cell cell;
		if (headerCellWidth > 0) {
			cell = new Cell(this, headerCellWidth, value, false);
		} else {
			// The header cell has no width yet, use the percentage
			cell = new Cell(this, headerCellWidthPct, value, true);
		}
		setBorders(cell, cells.isEmpty());
		cells.add(cell);
		return cell;
	}

	/**
	 * <p>
	 * Remove left border to avoid double borders from previous cell's right
	 * border
	 * </p>
	 *
	 * @param cell
	 * @param leftBorder
	 */
	private void setBorders(final Cell cell, final boolean leftBorder) {
		if (!leftBorder) {
			cell.setLeftBorderStyle(null);
		}
	}

	/**
	 * <p>
	 * remove top borders of cells to avoid double borders from cells in
	 * previous row
	 * </p>
	 */
	void removeTopBorders() {
		for (final Cell cell : cells) {
			cell.setTopBorderStyle(null);
		}
	}

	/**
	 * <p>
	 * Gets maximal height of the cells in current row therefore row's height.
	 * </p>
	 *
	 * @return Row's height
	 */
	public float getHeight() {
		float maxheight = 0.0f;
		for (Cell cell : this.cells) {
			float cellHeight = cell.getCellHeight();

			if (cellHeight > maxheight) {
				maxheight = cellHeight;
			}
		}

		if (maxheight > height) {
			this.height = maxheight;
		}

		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public List<Cell> getCells() {
		return cells;
	}

	public int getColCount() {
		return cells.size();
	}

	public float getWidth() {
		return table.getWidth();
	}

	public PDOutlineItem getBookmark() {
		return bookmark;
	}

	public void setBookmark(final PDOutlineItem bookmark) {
		this.bookmark = bookmark;
	}

	protected float getLastCellExtraWidth() {
		float cellWidth = 0;
		for (Cell cell : cells) {
			cellWidth += cell.getWidth();
		}

		float lastCellExtraWidth = this.getWidth() - cellWidth;
		return lastCellExtraWidth;
	}

	public float xEnd(final TableLayout tableLayout) {
		return tableLayout.margin() + getWidth();
	}

	public boolean isHeaderRow() {
		return headerRow;
	}

	public void setHeaderRow(boolean headerRow) {
		this.headerRow = headerRow;
	}

	public void initWidths() {
		for (final Cell c : cells) {
			c.setWidth(0);
		}
	}

	public Table getTable() {
		return table;
	}

	public int getRowIndex() {
		return table.getRows().indexOf(this);
	}

}
