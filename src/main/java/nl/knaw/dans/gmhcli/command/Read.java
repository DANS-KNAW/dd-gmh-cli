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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nl.knaw.dans.gmhcli.client.UrnNbnIdentifierApi;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "read",
         mixinStandardHelpOptions = true,
         description = "Read NBNs.")
@RequiredArgsConstructor
public class Read implements Callable<Integer> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @NonNull
    private final UrnNbnIdentifierApi api;

    @Parameters(index = "0",
                description = "The URN:NBN to read from the GMH Service.")
    private String urnNbn;

    @Option(names = { "-l", "--only-locations" },
            description = "Only show the locations of the NBN.")
    private boolean onlyLocations;

    @Override
    public Integer call() throws Exception {
        if (onlyLocations) {
            var locations = api.getLocationsByNbn(urnNbn);
            for (var location : locations) {
                System.out.println(MAPPER.writeValueAsString(location));
            }
        }
        else {
            System.out.println(MAPPER.writeValueAsString(api.getNbnRecord(urnNbn)));
        }
        return 0;
    }
}
