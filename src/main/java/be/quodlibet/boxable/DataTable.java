package be.quodlibet.boxable;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import be.quodlibet.boxable.line.LineStyle;
import be.quodlibet.boxable.utils.FontUtils;

/**
 * Create PDF Tables from structured data
 *
 * @author Dries Horions <dries@quodlibet.be>
 */
public class DataTable {
	public static final Boolean HASHEADER = true;
	public static final Boolean NOHEADER = false;
	private Table table;
	private final Cell headerCellTemplate;
	private final Cell dataCellTemplateEven;
	private final Cell dataCellTemplateOdd;
	private final Cell firstColumnCellTemplate;
	private final Cell lastColumnCellTemplate;
	private final Cell defaultCellTemplate;

	/**
	 * <p>
	 * Create a DataTable object to be able to add data to a Table. A page needs
	 * to be passed to the constructor so the Template Cells can be created.
	 * </p>
	 *
	 * @param table
	 * @param page
	 * @throws IOException
	 */
	public DataTable(Table table, PDPage page) throws IOException {
		this.table = table;
		// Create a dummy pdf document, page and table to create template cells
		PDDocument ddoc = new PDDocument();
		PDPage dpage = new PDPage();
		dpage.setMediaBox(page.getMediaBox());
		dpage.setRotation(page.getRotation());
		ddoc.addPage(dpage);
		Table dummyTable = new Table(10f, table.getWidth());
		Row dr = dummyTable.createRow(0f);
		headerCellTemplate = dr.createCell(10f, "A", HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
		dataCellTemplateEven = dr.createCell(10f, "A", HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE);
		dataCellTemplateOdd = dr.createCell(10f, "A", HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE);
		firstColumnCellTemplate = dr.createCell(10f, "A", HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE);
		lastColumnCellTemplate = dr.createCell(10f, "A", HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE);
		defaultCellTemplate = dr.createCell(10f, "A", HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE);
		setDefaultStyles();
		ddoc.close();
	}

	/**
	 * <p>
	 * Default cell styles for all cells. By default, only the header cell has a
	 * different style than the rest of the table.
	 * </p>
	 */
	private void setDefaultStyles() {
		LineStyle thinline = new LineStyle(Color.BLACK, 0.75f);
		// Header style
		headerCellTemplate.setFillColor(new Color(137, 218, 245));
		headerCellTemplate.setTextColor(Color.BLACK);
		headerCellTemplate.setFont(PDType1Font.HELVETICA_BOLD);
		headerCellTemplate.setBorderStyle(thinline);

		// Normal cell style, all rows and columns are the same by default
		defaultCellTemplate.setFillColor(new Color(242, 242, 242));
		defaultCellTemplate.setTextColor(Color.BLACK);
		defaultCellTemplate.setFont(PDType1Font.HELVETICA);
		defaultCellTemplate.setBorderStyle(thinline);

		dataCellTemplateEven.copyCellStyle(defaultCellTemplate);
		dataCellTemplateOdd.copyCellStyle(defaultCellTemplate);
		firstColumnCellTemplate.copyCellStyle(defaultCellTemplate);
		lastColumnCellTemplate.copyCellStyle(defaultCellTemplate);
	}

	/**
	 * Set the table to add the csv content to
	 *
	 * @return
	 */
	public Table getTable() {
		return table;
	}

	/**
	 * <p>
	 * Set the Table that the CSV document will be added to
	 * </p>
	 *
	 * @param table
	 */
	public void setTable(Table table) {
		this.table = table;
	}

	/**
	 * <p>
	 * Get the Cell Template that will be applied to header cells.
	 * <p>
	 * 
	 * @return
	 */
	public Cell getHeaderCellTemplate() {
		return headerCellTemplate;
	}

	/**
	 * <p>
	 * Get the Cell Template that will be assigned to Data cells that are in
	 * even rows, and are not the first or last column
	 * </p>
	 *
	 * @return
	 */
	public Cell getDataCellTemplateEven() {
		return dataCellTemplateEven;
	}

	/**
	 * <p>
	 * Get the Cell Template that will be assigned to Data cells that are in odd
	 * rows, and are not the first or last column
	 * </p>
	 *
	 * @return
	 */
	public Cell getDataCellTemplateOdd() {
		return dataCellTemplateOdd;
	}

	/**
	 * <p>
	 * Get the Cell Template that will be assigned to cells in the first column
	 * </p>
	 *
	 * @return
	 */
	public Cell getFirstColumnCellTemplate() {
		return firstColumnCellTemplate;
	}

	/**
	 * <p>
	 * Get the Cell Template that will be assigned to cells in the last columns
	 *
	 * @return
	 */
	public Cell getLastColumnCellTemplate() {
		return lastColumnCellTemplate;
	}

	/**
	 * <p>
	 * Add a List of Lists to the Table
	 * </p>
	 *
	 * @param data
	 * @param hasHeader
	 * @param separator
	 * @throws IOException
	 */
	public void addListToTable(final List<List<?>> data, boolean hasHeader) throws IOException {
		boolean isHeader = hasHeader;
		boolean isFirst = true;
		boolean odd = true;
		Map<Integer, Float> colWidths = new HashMap<>();
		int numcols = 0;
		for (final List<?> row : data) {
			if (isFirst) {
				// calculate the width of the columns
				float totalWidth = 0.0f;
				for (int i = 0; i < row.size(); i++) {
					String cellValue = String.valueOf(row.get(i));
					float textWidth = FontUtils.getStringWidth(headerCellTemplate.getFont(), " " + cellValue + " ",
							headerCellTemplate.getFontSize());
					totalWidth += textWidth;
					numcols = i;
				}
				// totalWidth has the total width we need to have all columns
				// full sized.
				// calculate a factor to reduce/increase size by to make it fit
				// in our table
				float sizefactor = table.getWidth() / totalWidth;
				for (int i = 0; i <= numcols; i++) {
					String cellValue = "";
					if (row.size() >= i) {
						cellValue = String.valueOf(row.get(i));
					}
					float textWidth = FontUtils.getStringWidth(headerCellTemplate.getFont(), " " + cellValue + " ",
							headerCellTemplate.getFontSize());
					float widthPct = textWidth * 100 / table.getWidth();
					// apply width factor
					widthPct = widthPct * sizefactor;
					colWidths.put(i, widthPct);
				}
				isFirst = false;
			}
			if (isHeader) {
				// Add Header Row
				Row h = table.createRow(headerCellTemplate.getCellHeight());
				for (int i = 0; i <= numcols; i++) {
					String cellValue = String.valueOf(row.get(i));
					Cell c = h.createCell(colWidths.get(i), cellValue, headerCellTemplate.getAlign(),
							headerCellTemplate.getValign());
					// Apply style of header cell to this cell
					c.copyCellStyle(headerCellTemplate);
					c.setText(cellValue);
				}
				table.addHeaderRow(h);
				isHeader = false;
			} else {
				Row r = table.createRow(dataCellTemplateEven.getCellHeight());
				for (int i = 0; i <= numcols; i++) {
					// Choose the correct template for the cell
					Cell template = dataCellTemplateEven;
					if (odd) {
						template = dataCellTemplateOdd;
					}
					if (i == 0 & !firstColumnCellTemplate.hasSameStyle(defaultCellTemplate)) {
						template = firstColumnCellTemplate;
					}
					if (i == numcols & !lastColumnCellTemplate.hasSameStyle(defaultCellTemplate)) {
						template = lastColumnCellTemplate;
					}
					String cellValue = "";
					if (row.size() >= i) {
						cellValue = String.valueOf(row.get(i));
					}
					Cell c = r.createCell(colWidths.get(i), cellValue, template.getAlign(), template.getValign());
					// Apply style of header cell to this cell
					c.copyCellStyle(template);
					c.setText(cellValue);
				}
			}
			odd = !odd;
		}
	}
}
