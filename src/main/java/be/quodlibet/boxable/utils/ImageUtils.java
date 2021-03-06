package be.quodlibet.boxable.utils;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import be.quodlibet.boxable.image.Image;

/**
 * <p>
 * Utility methods for images
 * </p>
 * 
 * @author mkuehne
 * @author hstimac
 */
public class ImageUtils {

	// utility class, no instance needed
	private ImageUtils() {
	}

	/**
	 * <p>
	 * Simple reading image from file
	 * </p>
	 * 
	 * @param imageFile
	 *            {@link File} from which image will be loaded
	 * @return {@link Image}
	 * @throws IOException
	 */
	public static Image readImage(File imageFile) throws IOException {
		final BufferedImage bufferedImage = ImageIO.read(imageFile);
		return new Image(bufferedImage);
	}

	/**
	 * <p>
	 * Provide an ability to scale {@link Image} on desired {@link Dimension}
	 * </p>
	 * 
	 * @param imgDim
	 *            Original image {@link Dimension} which will be scaled
	 * @param boundary
	 *            Boundary {@link Dimension} where image will be applied
	 * @return Appropriate scaled image {@link Dimension} based on boundary
	 *         {@link Dimension}
	 */
	public static float[] getScaledDimension(float imageWidth, float imageHeight, float boundWidth, float boundHeight) {
		float newImageWidth = imageWidth;
		float newImageHeight = imageHeight;
		
		// first check if we need to scale width
		if (imageWidth > boundWidth) {
			newImageWidth = boundWidth;
			// scale height to maintain aspect ratio
			newImageHeight = (newImageWidth * imageHeight) / imageWidth;
		}

		// then check if the new height is also bigger than expected
		if (newImageHeight > boundHeight) {
			newImageHeight = boundHeight;
			// scale width to maintain aspect ratio
			newImageWidth = (newImageHeight * imageWidth) / imageHeight;
		}
		
		float[] imageDimension = {newImageWidth, newImageHeight};
		return imageDimension;
	}
}
