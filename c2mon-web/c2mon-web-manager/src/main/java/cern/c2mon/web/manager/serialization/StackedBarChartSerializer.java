package cern.c2mon.web.manager.serialization;

import cern.c2mon.web.manager.statistics.daqlog.charts.JFreeStackedBarChart;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.jfree.data.category.CategoryDataset;

import java.io.IOException;

public class StackedBarChartSerializer extends JsonSerializer<JFreeStackedBarChart> {

  /**
   * Example JSON output:
   *
   * <p>
   * {
   *   "title" : "Graph Title",
   *   "description" : "Graph Description"
   *   "xAxis": {
   *     "label" : "Category of thing",
   *     "data" : ["1", "2", "3", "4"]
   *   },
   *   "yAxis": {
   *     "label" : "Number of things",
   *     "data" : ["41", "56", "34", "44"]
   *   }
   * }
   * </p>
   */
  @Override
  public void serialize(JFreeStackedBarChart chart, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonGenerationException {
    generator.writeStartObject();

    generator.writeStringField("title", chart.getTitle());
    generator.writeStringField("subtitle", chart.getParagraphTitle());
    generator.writeStringField("description", chart.getGraphDescription());

    generator.writeFieldName("xaxis");
    generator.writeStartObject();
    generator.writeStringField("label", chart.getDomainAxis());
    generator.writeFieldName("categories");
    generator.writeStartArray();
    for (Object object : chart.getJFreeChart().getCategoryPlot().getDataset().getColumnKeys()) {
      generator.writeString(object.toString());
    }
    generator.writeEndArray();
    generator.writeEndObject();

    generator.writeFieldName("yaxis");
    generator.writeStartObject();
    generator.writeStringField("label", chart.getRangeAxis());

    generator.writeFieldName("series");
    generator.writeStartArray();
    CategoryDataset dataset = chart.getJFreeChart().getCategoryPlot().getDataset();
    for (int r = 0; r < dataset.getRowCount(); r++) {
      generator.writeStartObject();
      generator.writeStringField("name", dataset.getRowKey(r).toString());

      generator.writeFieldName("data");
      generator.writeStartArray();
      for (int c = 0; c < dataset.getColumnCount(); c++) {
        Number number = dataset.getValue(r, c);
        if (number != null) {
          generator.writeNumber(number.doubleValue());
        } else {
          generator.writeNumber(0.0);
        }
      }
      generator.writeEndArray();
      generator.writeEndObject();
    }
    generator.writeEndArray();
    generator.writeEndObject();

    generator.writeEndObject();
  }
}