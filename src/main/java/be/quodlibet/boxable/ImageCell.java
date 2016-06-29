package be.quodlibet.boxable;

import be.quodlibet.boxable.image.Image;

public class ImageCell extends Cell {

	private Image img;

	ImageCell(Row row, float width, Image image, boolean isCalculated) {
		super(row, width, null, isCalculated);
		this.img = image;
		if (image.getWidth() > getInnerWidth()) {
			scaleToFit();
		}
	}

	public void scaleToFit() {
		img = img.scaleByWidth(getInnerWidth());
	}

	ImageCell(Row row, float width, Image image, boolean isCalculated, HorizontalAlignment align,
			VerticalAlignment valign) {
		super(row, width, null, isCalculated, align, valign);
		this.img = image;
		if (image.getWidth() > getInnerWidth()) {
			scaleToFit();
		}
	}

	@Override
	public float getTextHeight() {
		return img.getHeight();
	}

	@Override
	public float getHorizontalFreeSpace() {
		return getInnerWidth() - img.getWidth();
	}

	@Override
	public float getVerticalFreeSpace() {
		return getInnerHeight() - img.getHeight();
	}

	/**
	 * <p>
	 * Method which retrieve {@link Image}
	 * </p>
	 * 
	 * @return {@link Image}
	 */
	public Image getImage() {
		return img;
	}
}
