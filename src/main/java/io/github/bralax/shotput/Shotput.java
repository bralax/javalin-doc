package io.github.bralax.shotput;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import io.github.bralax.shotput.code.JavaGenerator;
import io.github.bralax.shotput.code.SampleCodeGenerator;
import io.github.bralax.shotput.endpoint.Endpoint;
import io.github.bralax.shotput.endpoint.Parameter;
import io.github.bralax.shotput.markdown.Scribe;
import io.github.bralax.shotput.openapi.OpenApiGenerator;
import io.github.bralax.shotput.parser.CodeParser;

import io.swagger.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Shotput {
    private List<Endpoint> endpoints;
    private File file;
    private boolean excel;
    private boolean html;
    private File out;
    private List<SampleCodeGenerator> generators;
    private Config config;
    private boolean swagger;

    public Shotput(Config config, File src, boolean excel, boolean html, boolean swagger, File out) {
        this.endpoints = new ArrayList<>();
        this.generators = new ArrayList<>();
        registerGenerator(new JavaGenerator());
        this.file = src;
        this.out = out;
        this.config = config;
        this.swagger = swagger;
        if (!excel && !html) {
            this.excel = true;
            this.html = true;
        } else {
            this.excel = excel;
            this.html = html;
        }
    }

    public Shotput registerGenerator(SampleCodeGenerator generator) {
        this.generators.add(generator);
        return this;
    }

    public void start() throws IOException {
        CombinedTypeSolver solver = new CombinedTypeSolver();
        solver.add(new ReflectionTypeSolver());
        solver.add(new JarTypeSolver(getClass().getClassLoader().getResource("javalin-3.9.1.jar").openStream()));
        //solver.add(new JavaParserTypeSolver(this.file));
        this.addFolderSymbolSolvers(solver, this.file);
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(solver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        this.parse(this.file);
        //System.out.println(this.endpoints.size());
        if (this.excel) {
            this.generateExcel();
        }
        if (this.html) {
            this.generateHTML();
        }
        if (this.swagger) {
            this.generateOpenApi();
        }
    }

    private void addFolderSymbolSolvers(CombinedTypeSolver solver, File file) {
        if (file != null) {
            if (file.isDirectory()) {
                boolean isEmpty = true;
                for (File f: file.listFiles()) {
                    if (f.isDirectory()) {
                        System.out.println(f.getAbsolutePath());
                        solver.add(new JavaParserTypeSolver(f));
                        isEmpty = false;
                    }
                }
                if (isEmpty) {
                    solver.add(new JavaParserTypeSolver(file));
                }
            }
        }
    }

    private void parse(File f) throws IOException{
       // System.out.println(f.getAbsolutePath());
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                this.parse(file);
            }
        } else if (f.getName().endsWith(".java")) {
            CompilationUnit unit =  StaticJavaParser.parse(f.toPath());
            CodeParser parser = new CodeParser();
            parser.visitPreOrder(unit);
            List<Endpoint> newEndpoints = parser.getEndpoints();
            //System.out.println(newEndpoints);
            this.endpoints.addAll(newEndpoints);
        }
    }

    

    private void generateExcel() {
        //System.out.println(this.out.getAbsolutePath());
        try {
        PrintWriter printWriter = new PrintWriter(this.out.getAbsolutePath() + "/endpoints.csv");
        printWriter.print("Index,Type,Endpoint,Response Type,Description,Path Parameter, Path Parameter Description, Query Parameter, Query Parameter Description,");
        printWriter.print("Form Parameter, Form Parameter Description, Request Header, Request Header Description, Response Header, Response Header Description,");
        printWriter.println("Response Status,Response Status Description");
        for (int i = 0; i < this.endpoints.size(); i++) {
            Endpoint endpoint = endpoints.get(i);
            int max = maxLength(endpoint.pathParamLength(), endpoint.formParamLength(), endpoint.headerParamLength(), endpoint.queryParamLength(), endpoint.responseHeaderLength(), endpoint.responseStatusLength());
            if (max > 0) {
                for (int j = 0; j < max; j++) {
                    if (j == 0) {
                        printWriter.print((i + 1) + "," + prepForCSV(endpoint.getType()) + "," + prepForCSV(endpoint.getEndpoint()) + "," +  prepForCSV(endpoint.getResponseType()) + "," + prepForCSV(endpoint.getDescription()) + ",");
                    } else {
                        printWriter.print(",,,,,");
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

                    /*if (j < endpoint.responseStatusLength()) {
                        Parameter param = endpoint.responseStatus(j);
                        printWriter.print(prepForCSV(param.getName()) + "," + prepForCSV(param.getDescription()) + ",");
                    } else {
                        printWriter.print(",,");
                    }*/
                    printWriter.println("");
                }
            } else {
                printWriter.println((i + 1) + "," + prepForCSV(endpoint.getType()) + "," + prepForCSV(endpoint.getEndpoint()) + "," +  prepForCSV(endpoint.getResponseType()) + "," + prepForCSV(endpoint.getDescription()) + ",");
            }
        }
        printWriter.flush();
        printWriter.close();
        } catch (IOException exception) {
            System.err.println("Failed to generate CSV!");
        }
    }

    private int maxLength(int... vals) {
        int max = 0; // the minimum possible length is 0
        for (int val: vals) {
            if (val > max) {
                max = val;
            }
        }
        return max;
    }

    private String prepForCSV(String original) {
        original = original.strip();
        original = original.replace("\n", " ");
        original = original.replace(",", " ");
        return original;
    }

    private void generateHTML() throws IOException{
        new Scribe(out.getAbsolutePath(), this.config, this.generators).writeDocs(this.groupEndpoints(endpoints));
    }


    private void generateOpenApi() {
       OpenAPI api = new OpenApiGenerator(config).generate(endpoints);
       try {
        PrintWriter printWriter = new PrintWriter(this.out.getAbsolutePath() + "/openapi.json");
        printWriter.print(Json.pretty().writeValueAsString(api));
        printWriter.flush();
        printWriter.close();
       } catch (IOException ex) {
            System.err.println("Failed to generate OpenApi Spec!");
       }
    }

    private Map<String, List<Endpoint>> groupEndpoints(List<Endpoint> endpoints) {
        Map<String, List<Endpoint>> grouped = new HashMap<>();
        for (Endpoint endpoint: endpoints) {
            String group = endpoint.getGroup();
            if (grouped.containsKey(group)) {
                grouped.get(group).add(endpoint);
            } else {
                List<Endpoint> groupList = new ArrayList<>();
                groupList.add(endpoint);
                grouped.put(group, groupList);
            }
        }
        return grouped;
    }


    
}