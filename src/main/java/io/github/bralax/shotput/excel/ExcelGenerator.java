package io.github.bralax.shotput.excel;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import io.github.bralax.shotput.Shotput;
import io.github.bralax.shotput.endpoint.Endpoint;
import io.github.bralax.shotput.endpoint.Parameter;
import io.github.bralax.shotput.endpoint.Response;

/** Class responsible for generating an excel file.
 * @author Brandon Lax
 */
public class ExcelGenerator {
    public static void generateExcel(List<Endpoint> endpoints, File out) {
        try {
        PrintWriter printWriter = new PrintWriter(out.getAbsolutePath() + "/endpoints.csv");
        printWriter.print("Index,Group,Type,Endpoint,Title,Response Type,Description,Path Parameter, Path Parameter Description, Query Parameter, Query Parameter Description,");
        printWriter.print("Form Parameter, Form Parameter Description, Request Header, Request Header Description, Response Header, Response Header Description,");
        printWriter.println("Example Response Code,Example Response Context,Example Response");
        for (int i = 0; i < endpoints.size(); i++) {
            Endpoint endpoint = endpoints.get(i);
            int max = maxLength(endpoint.pathParamLength(), endpoint.formParamLength(), endpoint.headerParamLength(), endpoint.queryParamLength(), endpoint.responseHeaderLength(), endpoint.exampleResponseLength());
            if (max > 0) {
                for (int j = 0; j < max; j++) {
                    if (j == 0) {
                        printWriter.print((i + 1) + "," + prepForCSV(endpoint.getGroup()) + "," + prepForCSV(endpoint.getType()) + "," + prepForCSV(endpoint.getEndpoint()) + "," + prepForCSV(endpoint.getTitle()) + "," +  prepForCSV(endpoint.getResponseType()) + "," + prepForCSV(endpoint.getDescription()) + ",");
                    } else {
                        printWriter.print(",,,,,,,");
                    }
                    if (j < endpoint.pathParamLength()) {
                        Parameter param = endpoint.pathParam(j);
                        printWriter.print(prepForCSV(param.getName()) + "," + prepForCSV(param.getDescription()) + ",");
                    } else {
                        printWriter.print(",,");
                    }

                    if (j < endpoint.queryParamLength()) {
                        Parameter param = endpoint.queryParam(j);
                        printWriter.print(prepForCSV(param.getName()) + "," + prepForCSV(param.getDescription()) + ",");
                    } else {
                        printWriter.print(",,");
                    }

                    if (j < endpoint.formParamLength()) {
                        Parameter param = endpoint.formParam(j);
                        printWriter.print(prepForCSV(param.getName()) + "," + prepForCSV(param.getDescription()) + ",");
                    } else {
                        printWriter.print(",,");
                    }

                    if (j < endpoint.headerParamLength()) {
                        Parameter param = endpoint.headerParam(j);
                        printWriter.print(prepForCSV(param.getName()) + "," + prepForCSV(param.getDescription()) + ",");
                    } else {
                        printWriter.print(",,");
                    }

                    if (j < endpoint.responseHeaderLength()) {
                        Parameter param = endpoint.responseHeader(j);
                        printWriter.print(prepForCSV(param.getName()) + "," + prepForCSV(param.getDescription()) + ",");
                    } else {
                        printWriter.print(",,");
                    }

                    if (j < endpoint.exampleResponseLength()) {
                        Response param = endpoint.exapleResponse(j);
                        String example = param.getExplanation();
                        example = example != null ? example : "";
                        printWriter.print(prepForCSV( "" +param.getStatusCode()) + "," + prepForCSV(example) + "," + prepForCSV(param.getExample()) + ",");
                    } else {
                        printWriter.print(",,,");
                    }
                    printWriter.println("");
                }
            } else {
                printWriter.println((i + 1) + "," + prepForCSV(endpoint.getGroup()) + "," + prepForCSV(endpoint.getType()) + "," + prepForCSV(endpoint.getEndpoint()) + "," + prepForCSV(endpoint.getTitle()) + "," +  prepForCSV(endpoint.getResponseType()) + "," + prepForCSV(endpoint.getDescription()) + ",");
            }
        }
        printWriter.flush();
        printWriter.close();
        } catch (IOException exception) {
            Shotput.getLogger().error("Failed to generate CSV!", exception);
        }
    }

    private static int maxLength(int... vals) {
        int max = 0; // the minimum possible length is 0
        for (int val: vals) {
            if (val > max) {
                max = val;
            }
        }
        return max;
    }

    private static String prepForCSV(String original) {
        original = original.strip();
        original = original.replace("\n", " ");
        original = original.replace(",", " ");
        return original;
    }
    
}
