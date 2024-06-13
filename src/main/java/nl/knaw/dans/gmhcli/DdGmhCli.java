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

package nl.knaw.dans.gmhcli;

import lombok.extern.slf4j.Slf4j;
import nl.knaw.dans.gmhcli.client.ApiClient;
import nl.knaw.dans.gmhcli.client.ApiTokenApi;
import nl.knaw.dans.gmhcli.client.LocationApi;
import nl.knaw.dans.gmhcli.client.UrnnbnIdentifierApi;
import nl.knaw.dans.gmhcli.command.Find;
import nl.knaw.dans.gmhcli.command.Nbn;
import nl.knaw.dans.gmhcli.command.Read;
import nl.knaw.dans.gmhcli.command.Token;
import nl.knaw.dans.gmhcli.command.Write;
import nl.knaw.dans.gmhcli.config.DdGmhCliConfig;
import nl.knaw.dans.lib.util.AbstractCommandLineApp;
import nl.knaw.dans.lib.util.CliVersionProvider;
import nl.knaw.dans.lib.util.ClientProxyBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "gmh",
         mixinStandardHelpOptions = true,
         versionProvider = CliVersionProvider.class,
         description = "Manage NBN records in GMH")
@Slf4j
public class DdGmhCli extends AbstractCommandLineApp<DdGmhCliConfig> {
    public static void main(String[] args) throws Exception {
        new DdGmhCli().run(args);
    }

    public String getName() {
        return "Manage NBN records in GMH";
    }

    @Override
    public void configureCommandLine(CommandLine commandLine, DdGmhCliConfig config) {
        var apiClient = new ApiClient().setBearerToken(config.getGmh().getToken());
        UrnnbnIdentifierApi nbnApi = new ClientProxyBuilder<ApiClient, UrnnbnIdentifierApi>()
            .apiClient(apiClient)
            .basePath(config.getGmh().getUrl())
            .httpClient(config.getGmh().getHttpClient())
            .defaultApiCtor(UrnnbnIdentifierApi::new)
            .build();
        LocationApi locationApi = new ClientProxyBuilder<ApiClient, LocationApi>()
                .apiClient(apiClient)
            .basePath(config.getGmh().getUrl())
            .httpClient(config.getGmh().getHttpClient())
            .defaultApiCtor(LocationApi::new)
            .build();
        ApiTokenApi tokenApi = new ClientProxyBuilder<ApiClient, ApiTokenApi>()
            .apiClient(apiClient)
            .basePath(config.getGmh().getUrl())
            .httpClient(config.getGmh().getHttpClient())
            .defaultApiCtor(ApiTokenApi::new)
            .build();

        log.debug("Configuring command line");
        commandLine
            .addSubcommand(new Token(tokenApi))
            .addSubcommand(new CommandLine(new Nbn())
                .addSubcommand(new Write(nbnApi))
                .addSubcommand(new Read(nbnApi))
                .addSubcommand(new Find(locationApi)));
    }
}
