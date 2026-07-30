Description
===========

The `dd-gmh-cli` tool provides a command-line interface for interacting with the "Gemeenschappelijke Metadata Harvester" (GMH) service to manage URN:NBN
(National Bibliographic Number) records.

Subcommands
-----------

The CLI is organized into the following subcommands:

### `token`

Retrieves an authentication token for the GMH Service. The token is required to perform write operations. You can provide your username using the `-u` or
`--username` option, and the tool will prompt you for your password securely. If no username is provided, it will prompt for both. Note that generating a new
token renders the previously generated token invalid. Use `-q` or `--quiet` to suppress warning messages.

### `nbn write`

Creates or updates NBN records in the GMH Service.

* **Single record mode**: Provide the NBN and one or more locations directly as arguments: `dd-gmh-cli nbn write <nbn> <location>...`
* **Batch mode**: Provide a CSV file containing multiple NBNs and locations using the `-i` or `--input-file` option. You can also configure a pause between each
  request using the `-w` or `--wait` option (e.g., `2s`, `500ms`, defaults to `1s`).
* **Force update**: By default, the command only creates new records. If an NBN is already registered, it will fail unless you provide the `-f` or `--force`
  option to update the existing record.

#### Input file format for batch mode

The input file must be a CSV file with a header row containing exactly the columns `NBN` and `LOCATION`. Each row will trigger a write operation for that
specific NBN. Only one location per NBN is supported per row.

Example `input.csv`:

```csv
NBN,LOCATION
urn:nbn:nl:ui:13-1234-56,https://example.com/dataset/1
urn:nbn:nl:ui:13-9876-54,https://example.com/dataset/2
```

### `nbn read`

Reads the details of a specific NBN record from the GMH service. Provide the `<nbn>` as an argument. The command will output the complete JSON representation of
the NBN record. If you only want to retrieve the locations the NBN resolves to, use the `-l` or `--only-locations` option.

### `nbn find`

Finds NBNs that resolve to a specific location. Provide the `<location>` URL as an argument. Note that the GMH service currently only supports this
reverse-lookup operation for Long-Term Preservation (LTP) locations.
