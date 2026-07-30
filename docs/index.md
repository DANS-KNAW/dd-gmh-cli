dd-gmh-cli
==========

Command-line interface for the "Gemeenschappelijke Metadata Harvester" (GMH) service. The `dd-gmh-cli` tool allows you to manage URN:NBN (National Bibliographic
Number) records within the GMH Resolver service. With this tool, you can authenticate and retrieve an API token, create or update NBN records (both single and
bulk operations), read existing NBN records to retrieve their resolution locations, and find NBNs that resolve to a specific location.

SYNOPSIS
--------

```bash
# Authentication
dd-gmh-cli token [ -u | --username <username> ] [ -q | --quiet ]

# NBN management
dd-gmh-cli nbn write [ -q | --quiet ] [ -f | --force ] \
   { <nbn> <location>... | -i | --input-file <inputFile> [ -w | --wait <waitDuration> ] }
dd-gmh-cli nbn read [ -l | --only-locations ] <nbn>
dd-gmh-cli nbn find <location>
```

For more information on a subcommand use:

```bash
dd-gmh-cli <subcommand> --help
```
