package be.quodlibet.boxable.page;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public interface PageProvider {

	PDPage createPage();

	PDPage nextPage();

	PDPage previousPage();

	PDPage getCurrentPage();

	void setSize(PDRectangle size);

	PDRectangle getSize();

	PDDocument getDocument();

}
