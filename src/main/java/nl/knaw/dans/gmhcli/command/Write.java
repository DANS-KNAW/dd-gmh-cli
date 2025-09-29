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
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
@Command(name = "write",
         mixinStandardHelpOptions = true,
         description = "Write a new NBN record to the GMH Service.")
public class Write implements Callable<Integer> {
    @NonNull
    private final UrnNbnIdentifierApi api;

    @Parameters(index = "0",
                paramLabel = "nbn",
                description = "The URN:NBN to write to the GMH Service.")
    private String urnNbn;

    @Parameters(index = "1..*",
                paramLabel = "location",
                description = "The locations to which the NBN should resolve.")
    private List<String> urls;

    @Option(names = { "-q", "--quiet" },
            description = "Do no output informational messages on stderr.")
    private boolean quiet;

    @Option(names = { "-f", "--force" },
            description = "Force the registration of the NBN, even if it is already registered.")
    private boolean force;

    @Override
    public Integer call() throws Exception {
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
}


