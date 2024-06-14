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
import nl.knaw.dans.gmhcli.client.ApiException;
import nl.knaw.dans.gmhcli.client.LocationApi;
import nl.knaw.dans.gmhcli.client.UrnnbnIdentifierApi;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "find",
         mixinStandardHelpOptions = true,
         description = "Find NBNs by their location. This only works for LTP locations.")
@RequiredArgsConstructor
public class Find implements Callable<Integer> {
    @NonNull
    private final LocationApi api;

    @Parameters(index = "0",
                paramLabel = "location",
                description = "The location for which to find NBNs.")
    private String location;

    @Override
    public Integer call() throws Exception {
        try {
            var locations = api.getNbnByLocation(location);
            for (var nbn : locations) {
                System.out.println(nbn);
            }
        } catch (ApiException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
        return 0;
    }
}
