/*
 * Copyright (C) 2024 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.gmhcli.command;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nl.knaw.dans.gmhcli.api.NbnLocationsObjectDto;
import nl.knaw.dans.gmhcli.client.ApiException;
import nl.knaw.dans.gmhcli.client.UrnNbnIdentifierApi;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
@Command(name = "write",
         mixinStandardHelpOptions = true,
         description = "Write a new NBN record to the GMH Service.")
public class Write implements Callable<Integer> {
    @NonNull
    private final UrnNbnIdentifierApi api;

    static class SingleNbnGroup {
        @Parameters(index = "0",
                    arity = "1",
                    paramLabel = "nbn",
                    description = "The URN:NBN to write to the GMH Service.")
        String urnNbn;

        @Parameters(index = "1..*",
                    paramLabel = "location",
                    description = "The locations to which the NBN should resolve.",
                    arity = "1..*")
        List<String> urls;
    }

    static class BatchGroup {
        @Option(names = { "-i", "--input-file" },
                description = "CSV file with columns NBN, LOCATION. Each row results in a write-operation for that NBN.",
                required = true)
        Path inputFile;

        @Option(names = { "-w", "--wait" },
                description = "Duration to wait between rows, e.g. '2s', '500ms'. Only valid with --input-file.",
                defaultValue = "1s")
        String waitDuration;
    }

    @ArgGroup(exclusive = true, multiplicity = "1")
    private Exclusive exclusive;

    static class Exclusive {
        @ArgGroup(exclusive = false, multiplicity = "1")
        SingleNbnGroup singleNbnGroup;

        @ArgGroup(exclusive = false, multiplicity = "1")
        BatchGroup batchGroup;
    }

    @Option(names = { "-q", "--quiet" },
            description = "Do no output informational messages on stderr.")
    private boolean quiet;

    @Option(names = { "-f", "--force" },
            description = "Force the registration of the NBN, even if it is already registered.")
    private boolean force;

    @Override
    public Integer call() throws Exception {
        if (exclusive.singleNbnGroup != null) {
            return handleSingleNbn(exclusive.singleNbnGroup);
        }
        if (exclusive.batchGroup != null) {
            return handleBatch(exclusive.batchGroup);
        }
        // Should never reach here due to Picocli exclusivity enforcement
        return 2;
    }

    private Integer handleSingleNbn(SingleNbnGroup group) {
        var urnNbn = group.urnNbn;
        var urls = group.urls;
        try {
            var dto = new NbnLocationsObjectDto().identifier(urnNbn).locations(urls);
            var action = "Created";
            if (force) {
                action = "Updated or created";
                api.updateNbnRecord(urnNbn, urls);
            }
            else {
                api.createNbnLocations(dto);
            }
            if (!quiet) {
                System.err.printf("OK. %s NBN '%s' to resolve to the following locations:%n", action, urnNbn);
                for (var location : urls) {
                    System.err.printf("  <%s>%n", location);
                }
            }
        }
        catch (ApiException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
        return 0;
    }

    private Integer handleBatch(BatchGroup group) {
        var inputFile = group.inputFile;
        var waitDuration = group.waitDuration;
        var wait = parseWaitDuration(waitDuration);
        if (wait == null) {
            System.err.println("Error: Invalid wait duration format. Use e.g. '2s', '500ms'.");
            return 2;
        }
        try (var is = Files.newInputStream(inputFile);
            var parser = CSVParser.parse(is, StandardCharsets.UTF_8, CSVFormat.DEFAULT.builder().setHeader().get())) {
            for (CSVRecord record : parser) {
                var nbn = record.get("NBN");
                var locationField = record.get("LOCATION");
                var locations = List.of(locationField.split("[;,]"));
                var dto = new NbnLocationsObjectDto().identifier(nbn).locations(locations);
                var action = "Created";
                try {
                    if (force) {
                        action = "Updated or created";
                        api.updateNbnRecord(nbn, locations);
                    }
                    else {
                        api.createNbnLocations(dto);
                    }
                    if (!quiet) {
                        System.err.printf("OK. %s NBN '%s' to resolve to the following locations:%n", action, nbn);
                        for (var location : locations) {
                            System.err.printf("  <%s>%n", location);
                        }
                    }
                }
                catch (ApiException e) {
                    System.err.printf("Error for NBN '%s': %s%n", nbn, e.getMessage());
                }
                if (!wait.isZero()) {
                    try {
                        Thread.sleep(wait.toMillis());
                    }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return 3;
                    }
                }
            }
        }
        catch (Exception e) {
            System.err.println("Error reading input file: " + e.getMessage());
            return 2;
        }
        return 0;
    }

    private Duration parseWaitDuration(String waitDuration) {
        if (waitDuration == null || waitDuration.isBlank()) {
            // 1s as default
            return Duration.ofSeconds(1);
        }
        try {
            return Duration.parse("PT" + waitDuration.replace("ms", "MILLI"));
        }
        catch (Exception e) {
            return null;
        }
    }
}
