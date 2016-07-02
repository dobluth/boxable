package be.quodlibet.boxable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.Test;

import com.google.common.io.Files;

import be.quodlibet.boxable.datatable.DataTable;
import be.quodlibet.boxable.layout.TableLayout;
import be.quodlibet.boxable.page.DefaultPageProvider;

/**
 *
 * @author Dries Horions <dries@quodlibet.be>
 */
public class DataTableTest {

	@Test
	public void listTestLandscape() throws IOException {

		// Initialize Document
		PDDocument doc = new PDDocument();
		PDPage page = new PDPage();
		// Create a landscape page
		page.setMediaBox(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
		doc.addPage(page);
		// Initialize table
		float margin = 10;
		float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
		float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
		float yStart = yStartNewPage;
		float bottomMargin = 0;

		// Create the data
		List<List<?>> data = new ArrayList<>();
		data.add(Arrays.asList("Column One", "Column Two", "Column Three", "Column Four", "Column Five"));
		for (int i = 1; i <= 100; i++) {
			data.add(new ArrayList<>(Arrays.asList("Row " + i + " Col One", "Row " + i + " Col Two",
					"Row " + i + " Col Three", "Row " + i + " Col Four", "Row " + i + " Col Five")));
		}

		Table dataTable = new Table(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, true, true,
				new DefaultPageProvider(doc, page.getMediaBox()));
		DataTable t = new DataTable(dataTable, page);
		t.addListToTable(data, DataTable.HASHEADER);
		dataTable.draw(new TableLayout());
		File file = new File("target/ListExampleLandscape.pdf");
		System.out.println("Sample file saved at : " + file.getAbsolutePath());
		Files.createParentDirs(file);
		doc.save(file);
		doc.close();
	}
}
