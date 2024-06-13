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

import lombok.RequiredArgsConstructor;
import nl.knaw.dans.gmhcli.api.CredentialsDto;
import nl.knaw.dans.gmhcli.client.ApiTokenApi;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.Console;
import java.util.concurrent.Callable;

@Command(name = "token",
         mixinStandardHelpOptions = true,
         description = "Get a token for the GMH API.")
@RequiredArgsConstructor
public class Token implements Callable<Integer> {
    private final ApiTokenApi api;

    @Option(names = { "-u", "--username" },
            description = "The username to use for authentication")
    private String username;

    @Option(names = { "-q", "--quiet" },
            description = "Do no output informational messages on stderr. (Token is still printed to stdout.)")
    private boolean quiet;

    public Integer call() throws Exception {
        try {
            if (!quiet) {
                System.err.println("WARNING: this will render the current token invalid. (Ctrl-C to abort.)");
            }
            if (username == null) {
                username = getUsername();
            }
            var password = getPassword();
            var creds = new CredentialsDto().username(username).password(password);
            var token = api.token(creds);
            if (!quiet) {
                System.err.printf("Token (re-)generated for user %s%n", username);
            }
            System.out.println(token);
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
        return 0;
    }

    public String getUsername() {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("No console available");
        }
        return console.readLine("Enter username: ");
    }

    public String getPassword() {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("No console available");
        }
        char[] passwordArray = console.readPassword("Enter password: ");
        return new String(passwordArray);
    }
}
