package be.quodlibet.boxable.layout.style;

import java.awt.Color;

import org.apache.pdfbox.pdmodel.font.PDFont;

import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.VerticalAlignment;
import be.quodlibet.boxable.line.LineStyle;

/**
 *
 * @author Dries Horions <dries@quodlibet.be>
 */
public class Style {

	public LineStyle border;

	public PDFont font;

	public PDFont fontBold;

	public int fontsize;

	public Color textcolor;

	public Color fillcolor;

	public HorizontalAlignment align;

	public VerticalAlignment valign;

	public LineStyle getBorder() {
		return border;
	}

	public void setBorder(LineStyle border) {
		this.border = border;
	}

	public PDFont getFont() {
		return font;
	}

	public void setFont(PDFont font) {
		this.font = font;
	}

	public PDFont getFontBold() {
		return fontBold;
	}

	public void setFontBold(PDFont fontBold) {
		this.fontBold = fontBold;
	}

	public int getFontSize() {
		return fontsize;
	}

	public void setFontSize(int fontsize) {
		this.fontsize = fontsize;
	}

	public Color getTextColor() {
		return textcolor;
	}

	public void setTextColor(final Color textcolor) {
		this.textcolor = textcolor;
	}

	public Color getFillColor() {
		return fillcolor;
	}

	public void setFillColor(final Color fillcolorDefault) {
		this.fillcolor = fillcolorDefault;
	}

	public HorizontalAlignment getHorizontalAlignment() {
		return align;
	}

	public void setHorizontalAlignment(final HorizontalAlignment horizontalAlignment) {
		this.align = horizontalAlignment;
	}

	public VerticalAlignment getVerticalAlignment() {
		return valign;
	}

	public void setVerticalAlignment(final VerticalAlignment verticalAlignment) {
		this.valign = verticalAlignment;
	}

}
