# wb - Wikidata and WikiBase instances tools

Some tools to work with Wikidata and Wikibase instances

## Run 

TODO: It is possible to create binaries for the different systems and publish them using scala-cli


## Build from source

It requires [scala-cli](https://scala-cli.virtuslab.org/) which can be downloaded for Linux, Windows and MacOS. 

Once you download it, you can create a binary using:

```sh
$ scala-cli package . -o wb -f
```

and run the program with:

```
$ ./wb
```

## Usage

```
Usage:
    wb info
    wb validate
    wb sparql

Information about Wikidata or Wikibase instances

Example: wb info --schema E42 
  Prints information about entity schema E42 from Wikidata

Options and flags:
    --help
        Display this help text.
    --version, -v
        Print the version number and exit.

Subcommands:
    info
        Get info about entity
    validate
        Validate an entity with an entity schema
    sparql
        Run SPARQL query
```

## Subcommand info

Can be used to obtain information about Wikibase entities

```
Usage: wb info --entity <entityId> [--wikibase <string>] [--schemaFormat <format>] [--mode <mode>] [--verbose <string>]

Get info about entity

Options and flags:
    --help
        Display this help text.
    --entity <entityId>, -e <entityId>
        Entity Id, example: Q42
    --wikibase <string>
        Wikibase, default: wikidata, values: wikidata|rhizome|gndtest
    --schemaFormat <format>
        Schema format, default = ShExC. Possible values = ShExC|ShExJ
    --mode <mode>
        Info mode, default = Out. Possible values = Out|Raw
    --verbose <string>
        verbose level. 0 = nothing, 1 = info msgs, 2 = all msgs
```
## Subcommand validate

Can be used to validate entities using entity schemas written in ShEx

```
Usage:
    wb validate --schema <string> --entity <entityId> [--wikibase <string>] [--shape <string>] [--shex-engine <engine>] [--result-format <format>] [--verbose <string>]
    wb validate --schema-file <file> --entity <entityId> [--wikibase <string>] [--shape <string>] [--shex-engine <engine>] [--result-format <format>] [--verbose <string>]

Validate an entity with an entity schema

Options and flags:
    --help
        Display this help text.
    --schema <string>
        Entity schema Id, example: E42
    --schema-file <file>
        File that contains entity schema
    --entity <entityId>, -e <entityId>
        Entity Id, example: Q42
    --wikibase <string>
        Wikibase, default: wikidata, values: wikidata|rhizome|gndtest
    --shape <string>
        Shape to validate in entity schema (if not specified, it uses Start shape)
    --shex-engine <engine>
        ShEx engine. Available engines: ShExS,Jena
    --result-format <format>
        result-format, default = Details, values=Details|Compact|JSON
    --verbose <string>
        verbose level. 0 = nothing, 1 = info msgs, 2 = all msgs
```

## Subcommand SPARQL

Can be used to run SPARQL queries on Wikidata and Wikibase instances

```
Usage: wb sparql --query-file <file> [--wikibase <string>] [--result-format <string>] [--verbose <string>]

Run SPARQL query

Options and flags:
    --help
        Display this help text.
    --query-file <file>
        File that contains the SPARQL query
    --wikibase <string>
        Wikibase, default: wikidata, values: wikidata|rhizome|gndtest
    --result-format <string>
        Result format. Default=AsciiTable. Values=Table|JSON|XML
    --verbose <string>
        verbose level. 0 = nothing, 1 = info msgs, 2 = all msgs
```

### Examples

#### Get information about Q42

```
wb info --entity Q42
```

#### Validate Q42 with entity schema E42 from Wikidata


```
wb validate --entity Q42 --schema E42
```

#### Validate Q5 with entity schema E1 from a different Wikibase instance

```
wb validate --entity Q5 --schema E1 --wikibase GNDTest
```

#### Run a sparql query 
```
wb sparql --query-file examples/query1.sparql
```

## Other distribution possibilities

It is possible to distribute/package the program using any of the [scala-cli package](https://scala-cli.virtuslab.org/docs/commands/package) options like Windows, docker, etc.

- Author: Jose Emilio Labra Gayo
