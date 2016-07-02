
/*
 Quodlibet.be
 */
package be.quodlibet.boxable;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.util.Matrix;

import be.quodlibet.boxable.layout.DefaultCellLayouter;
import be.quodlibet.boxable.line.LineStyle;
import be.quodlibet.boxable.page.PageProvider;
import be.quodlibet.boxable.text.Token;
import be.quodlibet.boxable.text.WrappingFunction;
import be.quodlibet.boxable.utils.FontUtils;
import be.quodlibet.boxable.utils.PDStreamUtils;

public class Table {

	public PDDocument document;

	private PDPage currentPage;
	private PDPageContentStream tableContentStream;
	private List<PDOutlineItem> bookmarks;
	private List<Row> header = new ArrayList<>();
	private List<Row> rows = new ArrayList<>();

	private float yStartNewPage;
	private float yStart;
	private float width;

	private float headerBottomMargin = 4f;

	private boolean tableIsBroken = false;
	private boolean removeTopBorders = false;

	private PageProvider pageProvider;
	private List<DefaultCellLayouter> layouters;

	private boolean drawDebug;

	// Defaults
	PDFont font = PDType1Font.HELVETICA;
	float fontSize = 6;
	private float pageTopMargin = 10;
	private float pageBottomMargin = 10;
	private float margin = 10;
	private boolean drawLines = true;
	private boolean drawContent = true;
	
	public Table() {
	}

	public Table(float yStart, float yStartNewPage, float pageTopMargin, float pageBottomMargin, float width,
			float margin, PDDocument document, PDPage currentPage, boolean drawLines, boolean drawContent,
			PageProvider pageProvider) throws IOException {
		this.pageTopMargin = pageTopMargin;
		this.document = document;
		this.drawLines = drawLines;
		this.drawContent = drawContent;
		// Initialize table
		this.yStartNewPage = yStartNewPage;
		this.margin = margin;
		this.width = width;
		this.yStart = yStart;
		this.pageBottomMargin = pageBottomMargin;
		this.currentPage = currentPage;
		this.pageProvider = pageProvider;
	}

	public Table(float yStartNewPage, float pageTopMargin, float pageBottomMargin, float width, float margin,
			PDDocument document, boolean drawLines, boolean drawContent, PageProvider pageProvider) throws IOException {
		this.pageTopMargin = pageTopMargin;
		this.document = document;
		this.drawLines = drawLines;
		this.drawContent = drawContent;
		// Initialize table
		this.yStartNewPage = yStartNewPage;
		this.margin = margin;
		this.width = width;
		this.pageProvider = pageProvider;
		this.pageBottomMargin = pageBottomMargin;

		// Fonts needs to be loaded before page creation
		this.currentPage = pageProvider.nextPage();
	}

	public Table addLayouter(DefaultCellLayouter l) {
		getLayouters().add(l);
		return this;
	}

	public Table clearLayouters() {
		getLayouters().clear();
		return this;
	}

	public List<DefaultCellLayouter> getLayouters() {
		if (layouters == null) {
			layouters = new ArrayList<>();
		}
		return layouters;
	}

	public void setLayouters(List<DefaultCellLayouter> layouters) {
		this.layouters = layouters;
	}

	public void setPage(PageProvider provider) {
		this.pageProvider = provider;
		this.currentPage = pageProvider.getCurrentPage();
		this.document = pageProvider.getDocument();
	}

	protected PDType0Font loadFont(String fontPath) throws IOException {
		return FontUtils.loadFont(getDocument(), fontPath);
	}

	protected PDDocument getDocument() {
		return document;
	}

	public void drawTitle(String title, PDFont font, int fontSize, float tableWidth, float height, String alignment,
			float freeSpaceForPageBreak, boolean drawHeaderMargin) throws IOException {
		drawTitle(title, font, fontSize, tableWidth, height, alignment, freeSpaceForPageBreak, null, drawHeaderMargin);
	}

	public void drawTitle(String title, PDFont font, int fontSize, float tableWidth, float height, String alignment,
			float freeSpaceForPageBreak, WrappingFunction wrappingFunction, boolean drawHeaderMargin)
			throws IOException {

		ensureStreamIsOpen();

		if (isEndOfPage(freeSpaceForPageBreak)) {
			this.tableContentStream.close();
			pageBreak();
		}

		if (title == null) {
			// if you don't have title just use the height of maxTextBox in your
			// "row"
			yStart -= height;
		} else {
			PDPageContentStream articleTitle = createPdPageContentStream();
			Paragraph paragraph = new Paragraph(title, font, fontSize, tableWidth, HorizontalAlignment.get(alignment),
					wrappingFunction);
			paragraph.setDrawDebug(drawDebug);
			yStart = paragraph.write(articleTitle, margin, yStart);
			if (paragraph.getHeight() < height) {
				yStart -= (height - paragraph.getHeight());
			}

			articleTitle.close();

			if (drawDebug) {
				// margin
				PDStreamUtils.rect(tableContentStream, margin, yStart, width, headerBottomMargin, Color.CYAN);
			}
		}

		if (drawHeaderMargin) {
			yStart -= headerBottomMargin;
		}
	}

	public float getWidth() {
		return width;
	}

	public Row createRow(float height) {
		Row row = new Row(this, height);
		this.rows.add(row);
		return row;
	}

	public Row createRow() {
		Row row = new Row(this);
		this.rows.add(row);
		return row;
	}

	public Row createRow(Collection<?> cells) {
		return createRow(cells, false);
	}

	public Row createRow(Collection<?> cells, Boolean isHeader) {
		Row row = new Row(this);
		if (isHeader) {
			this.addHeaderRow(row);
		}
		for (Object value : cells) {
			if (value instanceof Cell) {
				Cell vCell = (Cell) value;
				Cell c = row.createCell(vCell.getWidth(), vCell.getText());
				c.copyCellStyle(vCell);
			} else {
				row.createCell(value.toString());
			}
		}
		this.rows.add(row);
		return row;
	}

	public Row createRow(List<Cell> cells, float height) {
		Row row = new Row(this, cells, height);
		this.rows.add(row);
		return row;
	}

	private void initColumnWidths() {
		Row lastHeaderRow = this.header.get(this.header.size() - 1);
		Boolean hasWidthPct = false;
		List<Cell> cells = lastHeaderRow.getCells();
		for (Cell c : cells) {
			c.layout();
			if (c.getWidth() > 0 || c.getWidthPct() > 0) {
				hasWidthPct = true;
			}
		}
		if (!hasWidthPct) {
			// calculate the total width of the columns
			float totalWidth = 0.0f;
			for (Cell c : cells) {

				String cellValue = c.getText();
				float textWidth = FontUtils.getStringWidth(c.getFont(), " " + cellValue + " ", c.getFontSize());
				totalWidth += textWidth;
			}
			// totalWidth has the total width we need to have all columns full
			// sized.
			// calculate a factor to reduce/increase size by to make it fit in
			// our table
			float sizefactor = getWidth() / totalWidth;
			for (Cell c : cells) {
				String cellValue = c.getText();
				float textWidth = FontUtils.getStringWidth(c.getFont(), " " + cellValue + " ", c.getFontSize());
				float widthPct = textWidth * 100 / getWidth();
				// apply width factor
				widthPct = widthPct * sizefactor;
				c.setWidthPct(widthPct);
				c.setWidth((getWidth() / 100) * widthPct);
			}
		}
		for (Row r : this.getRows()) {
			initRowColumnWidths(r, lastHeaderRow);
		}
	}

	private void initRowColumnWidths(Row r, Row lastHeaderRow) {
		Boolean hasWidthPct = false;
		List<Cell> cells = r.getCells();
		for (Cell c : cells) {
			c.layout();
			if (c.getWidth() > 0 || c.getWidthPct() > 0) {
				hasWidthPct = true;
			}
		}
		if (!hasWidthPct) {
			for (int i = 0; i < cells.size(); i++) {
				Cell c = cells.get(i);
				c.setWidthPct(((Cell) lastHeaderRow.getCells().get(i)).getWidthPct());
				c.setWidth(((Cell) lastHeaderRow.getCells().get(i)).getWidth());
			}
		}

	}

	/**
	 * Draw the table on a specific vertical Position on the current page of a
	 * pageProvider
	 *
	 * @param yStartPosition
	 * @param provider
	 * @return
	 * @throws IOException
	 */
	public float draw(float yStartPosition, PageProvider provider) throws IOException {
		this.yStart = yStartPosition;
		this.setPage(provider);
		return draw();
	}

	/**
	 * Draw the table at the top of the current page of a pageProvider
	 *
	 * @param provider
	 * @return
	 * @throws IOException
	 */
	public float draw(PageProvider provider) throws IOException {
		this.setPage(provider);
		return draw();
	}

	public float draw() throws IOException {
		// if certain settings are not provided, default them
		if (yStartNewPage == 0) {
			yStartNewPage = pageProvider.getCurrentPage().getMediaBox().getHeight() - (2 * margin);
		}
		if (yStart == 0) {
			yStart = yStartNewPage;
		}

		// Since the page size can have changed, recalculate all column widths
		this.width = pageProvider.getCurrentPage().getMediaBox().getWidth() - (2 * margin);
		for (Row r : rows) {
			r.initWidths();
		}

		// If the last header line doesn't have widths assigned, calculate the
		// width based on the content.
		initColumnWidths();

		ensureStreamIsOpen();

		for (Row row : rows) {
			if (header.contains(row)) {
				// check if header row height and first data row height can fit
				// the page
				// if not draw them on another side
				if (isEndOfPage(getMinimumHeight())) {
					pageBreak();
				}
			}
			drawRow(row);
		}
		endTable();

		return yStart;
	}

	private void drawRow(Row row) throws IOException {
		// if it is not header row or first row in the table then remove row's
		// top border
		if (row != header && row != rows.get(0)) {
			row.removeTopBorders();
		}
		// draw the bookmark
		if (row.getBookmark() != null) {
			PDPageXYZDestination bookmarkDestination = new PDPageXYZDestination();
			bookmarkDestination.setPage(currentPage);
			bookmarkDestination.setTop((int) yStart);
			row.getBookmark().setDestination(bookmarkDestination);
			this.addBookmark(row.getBookmark());
		}

		// we want to remove the borders as often as possible
		removeTopBorders = true;

		if (isEndOfPage(row)) {

			// Draw line at bottom of table
			endTable();

			// insert page break
			pageBreak();

			// redraw all headers on each currentPage
			if (!header.isEmpty()) {
				for (Row headerRow : header) {
					drawRow(headerRow);
				}
				// after you draw all header rows on next page please keep
				// removing top borders to avoid double border drawing
				removeTopBorders = true;
			} else {
				// after a page break, we have to ensure that top borders get
				// drawn
				removeTopBorders = false;
			}
		}
		// if it is first row in the table, we have to draw the top border
		if (row == rows.get(0)) {
			removeTopBorders = false;
		}

		if (removeTopBorders) {
			row.removeTopBorders();
		}

		// if it is header row or first row in the table, we have to draw the
		// top border
		if (row == rows.get(0)) {
			removeTopBorders = false;
		}

		if (removeTopBorders) {
			row.removeTopBorders();
		}

		if (drawLines) {
			drawVerticalLines(row);
		}

		if (drawContent) {
			drawCellContent(row);
		}
	}

	/**
	 * <p>
	 * Method to switch between the {@link PageProvider} and the abstract method
	 * {@link Table#createPage()}, preferring the {@link PageProvider}.
	 * </p>
	 * <p>
	 * Will be removed once {@link #createPage()} is removed.
	 * </p>
	 *
	 * @return
	 */
	private PDPage createNewPage() {
		if (pageProvider != null) {
			return pageProvider.nextPage();
		}

		return createPage();
	}

	/**
	 * @deprecated Use a {@link PageProvider} instead
	 * @return
	 */
	@Deprecated
	// remove also createNewPage()
	protected PDPage createPage() {
		throw new IllegalStateException(
				"You either have to provide a " + PageProvider.class.getCanonicalName() + " or override this method");
	}

	private PDPageContentStream createPdPageContentStream() throws IOException {
		return new PDPageContentStream(getDocument(), getCurrentPage(), PDPageContentStream.AppendMode.APPEND, true);
	}

	private void drawCellContent(Row row) throws IOException {

		// position into first cell (horizontal)
		float cursorX = margin;
		float cursorY;

		for (Cell cell : row.getCells()) {

			// remember horizontal cursor position, so we can advance to the
			// next cell easily later
			float cellStartX = cursorX;
			if (cell instanceof ImageCell) {
				final ImageCell imageCell = (ImageCell) cell;

				cursorY = yStart - cell.getTopPadding()
						- (cell.getTopBorder() == null ? 0 : cell.getTopBorder().getWidth());

				// image cell vertical alignment
				switch (cell.getValign()) {
				case TOP:
					break;
				case MIDDLE:
					cursorY -= cell.getVerticalFreeSpace() / 2;
					break;
				case BOTTOM:
					cursorY -= cell.getVerticalFreeSpace();
					break;
				}

				cursorX += cell.getLeftPadding() + (cell.getLeftBorder() == null ? 0 : cell.getLeftBorder().getWidth());

				// image cell horizontal alignment
				switch (cell.getAlign()) {
				case CENTER:
					cursorX += cell.getHorizontalFreeSpace() / 2;
					break;
				case LEFT:
					break;
				case RIGHT:
					cursorX += cell.getHorizontalFreeSpace();
					break;
				}
				imageCell.getImage().draw(document, tableContentStream, cursorX, cursorY);

			} else {
				// no text without font
				if (cell.getFont() == null) {
					throw new IllegalArgumentException("Font is null on Cell=" + cell.getText());
				}

				// font settings
				this.tableContentStream.setFont(cell.getFont(), cell.getFontSize());

				if (cell.isTextRotated()) {
					// debugging mode - drawing (default!) padding of rotated
					// cells
					// left
					// PDStreamUtils.rect(tableContentStream, cursorX, yStart,
					// 5, cell.getHeight(), Color.GREEN);
					// top
					// PDStreamUtils.rect(tableContentStream, cursorX, yStart,
					// cell.getWidth(), 5 , Color.GREEN);
					// bottom
					// PDStreamUtils.rect(tableContentStream, cursorX, yStart -
					// cell.getHeight(), cell.getWidth(), -5 , Color.GREEN);
					// right
					// PDStreamUtils.rect(tableContentStream, cursorX +
					// cell.getWidth() - 5, yStart, 5, cell.getHeight(),
					// Color.GREEN);

					cursorY = yStart - cell.getInnerHeight() - cell.getTopPadding()
							- (cell.getTopBorder() != null ? cell.getTopBorder().getWidth() : 0);

					switch (cell.getAlign()) {
					case CENTER:
						cursorY += cell.getVerticalFreeSpace() / 2;
						break;
					case LEFT:
						break;
					case RIGHT:
						cursorY += cell.getVerticalFreeSpace();
						break;
					}
					// respect left padding and descend by font height to get
					// position of the base line
					cursorX += cell.getLeftPadding()
							+ (cell.getLeftBorder() == null ? 0 : cell.getLeftBorder().getWidth())
							+ FontUtils.getHeight(cell.getFont(), cell.getFontSize())
							+ FontUtils.getDescent(cell.getFont(), cell.getFontSize());

					switch (cell.getValign()) {
					case TOP:
						break;
					case MIDDLE:
						cursorX += cell.getHorizontalFreeSpace() / 2;
						break;
					case BOTTOM:
						cursorX += cell.getHorizontalFreeSpace();
						break;
					}

				} else {
					// debugging mode - drawing (default!) padding of rotated
					// cells
					// left
					// PDStreamUtils.rect(tableContentStream, cursorX, yStart,
					// 5, cell.getHeight(), Color.RED);
					// top
					// PDStreamUtils.rect(tableContentStream, cursorX, yStart,
					// cell.getWidth(), 5 , Color.RED);
					// bottom
					// PDStreamUtils.rect(tableContentStream, cursorX, yStart -
					// cell.getHeight(), cell.getWidth(), -5 , Color.RED);
					// right
					// PDStreamUtils.rect(tableContentStream, cursorX +
					// cell.getWidth() - 5, yStart, 5, cell.getHeight(),
					// Color.RED);

					// position at top of current cell descending by font height
					// - font descent, because we are
					// positioning the base line here
					cursorY = yStart - cell.getTopPadding() - FontUtils.getHeight(cell.getFont(), cell.getFontSize())
							- FontUtils.getDescent(cell.getFont(), cell.getFontSize())
							- (cell.getTopBorder() == null ? 0 : cell.getTopBorder().getWidth());

					if (drawDebug) {
						// @formatter:off
						// top padding
						PDStreamUtils.rect(tableContentStream,
								cursorX + (cell.getLeftBorder() == null ? 0 : cell.getLeftBorder().getWidth()),
								yStart - (cell.getTopBorder() == null ? 0 : cell.getTopBorder().getWidth()),
								cell.getWidth() - (cell.getLeftBorder() == null ? 0 : cell.getLeftBorder().getWidth())
										- (cell.getRightBorder() == null ? 0 : cell.getRightBorder().getWidth()),
								cell.getTopPadding(), Color.RED);
						// bottom padding
						PDStreamUtils.rect(tableContentStream,
								cursorX + (cell.getLeftBorder() == null ? 0 : cell.getLeftBorder().getWidth()),
								yStart - cell.getHeight()
										+ (cell.getBottomBorder() == null ? 0 : cell.getBottomBorder().getWidth())
										+ cell.getBottomPadding(),
								cell.getWidth() - (cell.getLeftBorder() == null ? 0 : cell.getLeftBorder().getWidth())
										- (cell.getRightBorder() == null ? 0 : cell.getRightBorder().getWidth()),
								cell.getBottomPadding(), Color.RED);
						// left padding
						PDStreamUtils.rect(tableContentStream,
								cursorX + (cell.getLeftBorder() == null ? 0 : cell.getLeftBorder().getWidth()),
								yStart - (cell.getTopBorder() == null ? 0 : cell.getTopBorder().getWidth()),
								cell.getLeftPadding(),
								cell.getHeight() - (cell.getTopBorder() == null ? 0 : cell.getTopBorder().getWidth())
										- (cell.getBottomBorder() == null ? 0 : cell.getBottomBorder().getWidth()),
								Color.RED);
						// right padding
						PDStreamUtils.rect(tableContentStream,
								cursorX + cell.getWidth()
										- (cell.getRightBorder() == null ? 0 : cell.getRightBorder().getWidth()),
								yStart - (cell.getTopBorder() == null ? 0 : cell.getTopBorder().getWidth()),
								-cell.getRightPadding(),
								cell.getHeight() - (cell.getTopBorder() == null ? 0 : cell.getTopBorder().getWidth())
										- (cell.getBottomBorder() == null ? 0 : cell.getBottomBorder().getWidth()),
								Color.RED);
						// @formatter:on
					}

					// respect left padding
					cursorX += cell.getLeftPadding()
							+ (cell.getLeftBorder() == null ? 0 : cell.getLeftBorder().getWidth());

					// the widest text does not fill the inner width of the
					// cell? no
					// problem, just add it ;)
					switch (cell.getAlign()) {
					case CENTER:
						cursorX += cell.getHorizontalFreeSpace() / 2;
						break;
					case LEFT:
						break;
					case RIGHT:
						cursorX += cell.getHorizontalFreeSpace();
						break;
					}

					switch (cell.getValign()) {
					case TOP:
						break;
					case MIDDLE:
						cursorY -= cell.getVerticalFreeSpace() / 2;
						break;
					case BOTTOM:
						cursorY -= cell.getVerticalFreeSpace();
						break;
					}

				}

				// remember this horizontal position, as it is the anchor for
				// each
				// new line
				float lineStartX = cursorX;
				float lineStartY = cursorY;

				// if it is head row or if it is header cell then please use
				// bold font
				if (row.equals(header) || cell.isHeaderCell()) {
					this.tableContentStream.setFont(cell.getParagraph().getFont(true, false), cell.getFontSize());
				}
				this.tableContentStream.setNonStrokingColor(cell.getTextColor());

				int italicCounter = 0;
				int boldCounter = 0;

				// print all lines of the cell
				for (Map.Entry<Integer, List<Token>> entry : cell.getParagraph().getMapLineTokens().entrySet()) {

					// calculate the width of this line
					float freeSpaceWithinLine = cell.getParagraph().getMaxLineWidth()
							- cell.getParagraph().getLineWidth(entry.getKey());
					// TODO: need to implemented rotated text yo!
					if (cell.isTextRotated()) {
						cursorY = lineStartY;
						switch (cell.getAlign()) {
						case CENTER:
							cursorY += freeSpaceWithinLine / 2;
							break;
						case LEFT:
							break;
						case RIGHT:
							cursorY += freeSpaceWithinLine;
							break;
						}
					} else {
						cursorX = lineStartX;
						switch (cell.getAlign()) {
						case CENTER:
							cursorX += freeSpaceWithinLine / 2;
							break;
						case LEFT:
							// it doesn't matter because X position is always
							// the same
							// as row above
							break;
						case RIGHT:
							cursorX += freeSpaceWithinLine;
							break;
						}
					}

					// iterate through tokens in current line
					PDFont currentFont = cell.getParagraph().getFont(false, false);
					for (Token token : entry.getValue()) {
						switch (token.getType()) {
						case OPEN_TAG:
							if ("b".equals(token.getData())) {
								boldCounter++;
							} else if ("i".equals(token.getData())) {
								italicCounter++;
							}
							break;
						case CLOSE_TAG:
							if ("b".equals(token.getData())) {
								boldCounter = Math.max(boldCounter - 1, 0);
							} else if ("i".equals(token.getData())) {
								italicCounter = Math.max(italicCounter - 1, 0);
							}
							break;
						case PADDING:
							cursorX += Float.parseFloat(token.getData());
							break;
						case ORDERING:
							this.tableContentStream.beginText();
							currentFont = cell.getParagraph().getFont(boldCounter > 0, italicCounter > 0);
							this.tableContentStream.setFont(currentFont, cell.getFontSize());
							if (cell.isTextRotated()) {
								final AffineTransform transform = AffineTransform.getTranslateInstance(cursorX,
										cursorY);
								transform.concatenate(AffineTransform.getRotateInstance(Math.PI * 0.5f));
								transform.concatenate(AffineTransform.getTranslateInstance(-cursorX, -cursorY));
								tableContentStream.setTextMatrix(new Matrix(transform));
								tableContentStream.newLineAtOffset(cursorX, cursorY);
								this.tableContentStream.showText(token.getData());
								this.tableContentStream.endText();
								this.tableContentStream.closePath();
								cursorY += currentFont.getStringWidth(token.getData()) / 1000 * cell.getFontSize();
							} else {
								this.tableContentStream.newLineAtOffset(cursorX, cursorY);
								this.tableContentStream.showText(token.getData());
								this.tableContentStream.endText();
								this.tableContentStream.closePath();
								cursorX += currentFont.getStringWidth(token.getData()) / 1000 * cell.getFontSize();
							}
							break;
						case BULLET:
							if (cell.isTextRotated()) {
								// move cursorX up because bullet needs to be in
								// the middle of font height
								cursorX += FontUtils.getHeight(currentFont, cell.getFontSize()) / 2;
								PDStreamUtils.rect(tableContentStream, cursorX, cursorY,
										currentFont.getStringWidth(token.getData()) / 1000 * cell.getFontSize(),
										currentFont.getStringWidth(" ") / 1000 * cell.getFontSize(),
										cell.getTextColor());
								// move cursorY for two characters (one for
								// bullet, one for space after bullet)
								cursorY += 2 * currentFont.getStringWidth(" ") / 1000 * cell.getFontSize();
								// return cursorY to his original place
								cursorX -= FontUtils.getHeight(currentFont, cell.getFontSize()) / 2;
							} else {
								// move cursorY up because bullet needs to be in
								// the middle of font height
								cursorY += FontUtils.getHeight(currentFont, cell.getFontSize()) / 2;
								PDStreamUtils.rect(tableContentStream, cursorX, cursorY,
										currentFont.getStringWidth(token.getData()) / 1000 * cell.getFontSize(),
										currentFont.getStringWidth(" ") / 1000 * cell.getFontSize(),
										cell.getTextColor());
								// move cursorX for two characters (one for
								// bullet, one for space after bullet)
								cursorX += 2 * currentFont.getStringWidth(" ") / 1000 * cell.getFontSize();
								// return cursorY to his original place
								cursorY -= FontUtils.getHeight(currentFont, cell.getFontSize()) / 2;
							}
							break;
						case TEXT:
							this.tableContentStream.beginText();
							currentFont = cell.getParagraph().getFont(boldCounter > 0, italicCounter > 0);
							this.tableContentStream.setFont(currentFont, cell.getFontSize());
							if (cell.isTextRotated()) {
								final AffineTransform transform = AffineTransform.getTranslateInstance(cursorX,
										cursorY);
								transform.concatenate(AffineTransform.getRotateInstance(Math.PI * 0.5f));
								transform.concatenate(AffineTransform.getTranslateInstance(-cursorX, -cursorY));
								tableContentStream.setTextMatrix(new Matrix(transform));
								tableContentStream.newLineAtOffset(cursorX, cursorY);
								this.tableContentStream.showText(token.getData());
								this.tableContentStream.endText();
								this.tableContentStream.closePath();
								cursorY += currentFont.getStringWidth(token.getData()) / 1000 * cell.getFontSize();
							} else {
								try {
									this.tableContentStream.newLineAtOffset(cursorX, cursorY);
									this.tableContentStream.showText(token.getData());
									this.tableContentStream.endText();
									this.tableContentStream.closePath();
									cursorX += currentFont.getStringWidth(token.getData()) / 1000 * cell.getFontSize();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							break;
						}
					}
					if (cell.isTextRotated()) {
						cursorX = cursorX + cell.getParagraph().getFontHeight();
					} else {
						cursorY = cursorY - cell.getParagraph().getFontHeight();
					}
				}
			}
			// set cursor to the start of this cell plus its width to advance to
			// the next cell
			cursorX = cellStartX + cell.getWidth();
		}
		// Set Y position for next row
		yStart = yStart - row.getHeight();

	}

	private void drawVerticalLines(Row row) throws IOException {
		float xStart = margin;

		// give an extra margin to the latest cell
		float xEnd = row.xEnd();

		Iterator<Cell> cellIterator = row.getCells().iterator();
		while (cellIterator.hasNext()) {
			Cell cell = cellIterator.next();

			fillCellColor(cell, yStart, xStart, cellIterator);

			drawCellBorders(row, cell, xStart, xEnd);

			xStart += getWidth(cell, cellIterator);
		}

	}

	private void drawCellBorders(Row row, Cell cell, float xStart, float xEnd) throws IOException {

		float yEnd = yStart - row.getHeight();

		// top
		LineStyle topBorder = cell.getTopBorder();
		if (topBorder != null) {
			float y = yStart - topBorder.getWidth() / 2;
			drawLine(xStart, y, xStart + cell.getWidth(), y, topBorder);
		}

		// right
		LineStyle rightBorder = cell.getRightBorder();
		if (rightBorder != null) {
			float x = xStart + cell.getWidth() - rightBorder.getWidth() / 2;
			drawLine(x, yStart - (topBorder == null ? 0 : topBorder.getWidth()), x, yEnd, rightBorder);
		}

		// bottom
		LineStyle bottomBorder = cell.getBottomBorder();
		if (bottomBorder != null) {
			float y = yEnd + bottomBorder.getWidth() / 2;
			drawLine(xStart, y, xStart + cell.getWidth() - (rightBorder == null ? 0 : rightBorder.getWidth()), y,
					bottomBorder);
		}

		// left
		LineStyle leftBorder = cell.getLeftBorder();
		if (leftBorder != null) {
			float x = xStart + leftBorder.getWidth() / 2;
			drawLine(x, yStart, x, yEnd + (bottomBorder == null ? 0 : bottomBorder.getWidth()), leftBorder);
		}

	}

	private void drawLine(float xStart, float yStart, float xEnd, float yEnd, LineStyle border) throws IOException {
		PDStreamUtils.setLineStyles(tableContentStream, border);
		tableContentStream.moveTo(xStart, yStart);
		tableContentStream.lineTo(xEnd, yEnd);
		tableContentStream.stroke();
		tableContentStream.closePath();
	}

	private void fillCellColor(Cell cell, float yStart, float xStart, Iterator<Cell> cellIterator) throws IOException {

		if (cell.getFillColor() != null) {
			this.tableContentStream.setNonStrokingColor(cell.getFillColor());

			// y start is bottom pos
			yStart = yStart - cell.getHeight();
			float height = cell.getHeight() - (cell.getTopBorder() == null ? 0 : cell.getTopBorder().getWidth());

			float cellWidth = getWidth(cell, cellIterator);
			this.tableContentStream.addRect(xStart, yStart, cellWidth, height);
			this.tableContentStream.fill();
			this.tableContentStream.closePath();

			// Reset NonStroking Color to default value
			this.tableContentStream.setNonStrokingColor(Color.BLACK);
		}
	}

	private float getWidth(Cell cell, Iterator<Cell> cellIterator) {
		float width;
		if (cellIterator.hasNext()) {
			width = cell.getWidth();
		} else {
			width = cell.getExtraWidth();
		}
		return width;
	}

	private void ensureStreamIsOpen() throws IOException {
		if (tableContentStream == null) {
			tableContentStream = createPdPageContentStream();
		}
	}

	private void endTable() throws IOException {
		this.tableContentStream.close();
		this.tableContentStream = null;
		yStart -= margin;// add margin at bottom of table
	}

	public PDPage getCurrentPage() {
		checkNotNull(this.currentPage, "No current page defined.");
		return this.currentPage;
	}

	private boolean isEndOfPage(Row row) {
		float currentY = yStart - row.getHeight();
		boolean isEndOfPage = currentY <= pageBottomMargin;
		if (isEndOfPage) {
			setTableIsBroken(true);
		}

		// If we are closer than bottom margin, consider this as
		// the end of the currentPage
		// If you add rows that are higher then bottom margin, this needs to be
		// checked
		// manually using getNextYPos
		return isEndOfPage;
	}

	private boolean isEndOfPage(float freeSpaceForPageBreak) {
		float currentY = yStart - freeSpaceForPageBreak;
		boolean isEndOfPage = currentY <= pageBottomMargin;
		if (isEndOfPage) {
			setTableIsBroken(true);
		}
		return isEndOfPage;
	}

	public void pageBreak() throws IOException {
		if (tableContentStream != null) {
			tableContentStream.close();
		}

		this.currentPage = createNewPage();
		yStartNewPage = pageProvider.getCurrentPage().getMediaBox().getHeight() - (2 * margin);
		this.yStart = yStartNewPage - pageTopMargin;
		this.tableContentStream = createPdPageContentStream();
	}

	private void addBookmark(PDOutlineItem bookmark) {
		if (bookmarks == null)
			bookmarks = new ArrayList<>();
		bookmarks.add(bookmark);
	}

	public List<PDOutlineItem> getBookmarks() {
		return bookmarks;
	}

	/**
	 *
	 * @param header
	 * @deprecated Use {@link #addHeaderRow(Row)} instead, as it supports
	 *             multiple header rows
	 */
	@Deprecated
	public void setHeader(Row header) {
		this.header.clear();
		addHeaderRow(header);
	}

	/**
	 * <p>
	 * Calculate height of all table cells (essentially, table height).
	 * </p>
	 * <p>
	 * IMPORTANT: Doesn't acknowledge possible page break. Use with caution.
	 * </p>
	 *
	 * @return {@link Table}'s height
	 */
	public float getHeaderAndDataHeight() {
		float height = 0;
		for (Row row : rows) {
			height += row.getHeight();
		}
		return height;
	}

	/**
	 * <p>
	 * Calculates minimum table height that needs to be drawn (all header rows +
	 * first data row heights).
	 * </p>
	 *
	 * @return height
	 */
	public float getMinimumHeight() {
		float height = 0.0f;
		int firstDataRowIndex = 0;
		if (!header.isEmpty()) {
			for (Row headerRow : header) {
				// count all header rows height
				height += headerRow.getHeight();
				firstDataRowIndex++;
			}
		}

		if (rows.size() > firstDataRowIndex) {
			height += rows.get(firstDataRowIndex).getHeight();
		}

		return height;
	}

	/**
	 * <p>
	 * Setting current row as table header row
	 * </p>
	 *
	 * @param row
	 */
	public void addHeaderRow(Row row) {
		this.header.add(row);
		row.setHeaderRow(true);
	}

	/**
	 * <p>
	 * Retrieves last table's header row
	 * </p>
	 *
	 * @return header row
	 */
	public Row getHeader() {
		if (header == null) {
			throw new IllegalArgumentException("Header Row not set on table");
		}

		return header.get(header.size() - 1);
	}

	public float getMargin() {
		return margin;
	}

	protected void setYStart(float yStart) {
		this.yStart = yStart;
	}

	public boolean isDrawDebug() {
		return drawDebug;
	}

	public void setDrawDebug(boolean drawDebug) {
		this.drawDebug = drawDebug;
	}

	public boolean tableIsBroken() {
		return tableIsBroken;
	}

	public void setTableIsBroken(boolean tableIsBroken) {
		this.tableIsBroken = tableIsBroken;
	}

	public List<Row> getRows() {
		return rows;
	}

	public PDFont getFont() {
		return font;
	}

	public void setFont(PDFont font) {
		this.font = font;
	}

	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}

	public float getPageTopMargin() {
		return pageTopMargin;
	}

	public void setPageTopMargin(float pageTopMargin) {
		this.pageTopMargin = pageTopMargin;
	}

	public float getPageBottomMargin() {
		return pageBottomMargin;
	}

	public void setPageBottomMargin(float pageBottomMargin) {
		this.pageBottomMargin = pageBottomMargin;
	}

}
