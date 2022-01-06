"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[671],{3905:function(e,t,n){n.d(t,{Zo:function(){return c},kt:function(){return p}});var a=n(7294);function i(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function r(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function s(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?r(Object(n),!0).forEach((function(t){i(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):r(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,a,i=function(e,t){if(null==e)return{};var n,a,i={},r=Object.keys(e);for(a=0;a<r.length;a++)n=r[a],t.indexOf(n)>=0||(i[n]=e[n]);return i}(e,t);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);for(a=0;a<r.length;a++)n=r[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(i[n]=e[n])}return i}var o=a.createContext({}),u=function(e){var t=a.useContext(o),n=t;return e&&(n="function"==typeof e?e(t):s(s({},t),e)),n},c=function(e){var t=u(e.components);return a.createElement(o.Provider,{value:t},e.children)},d={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},m=a.forwardRef((function(e,t){var n=e.components,i=e.mdxType,r=e.originalType,o=e.parentName,c=l(e,["components","mdxType","originalType","parentName"]),m=u(n),p=i,h=m["".concat(o,".").concat(p)]||m[p]||d[p]||r;return n?a.createElement(h,s(s({ref:t},c),{},{components:n})):a.createElement(h,s({ref:t},c))}));function p(e,t){var n=arguments,i=t&&t.mdxType;if("string"==typeof e||i){var r=n.length,s=new Array(r);s[0]=m;var l={};for(var o in t)hasOwnProperty.call(t,o)&&(l[o]=t[o]);l.originalType=e,l.mdxType="string"==typeof e?e:i,s[1]=l;for(var u=2;u<r;u++)s[u]=n[u];return a.createElement.apply(null,s)}return a.createElement.apply(null,n)}m.displayName="MDXCreateElement"},9881:function(e,t,n){n.r(t),n.d(t,{frontMatter:function(){return l},contentTitle:function(){return o},metadata:function(){return u},toc:function(){return c},default:function(){return m}});var a=n(7462),i=n(3366),r=(n(7294),n(3905)),s=["components"],l={},o="wb",u={unversionedId:"intro",id:"intro",title:"wb",description:"Simple command line tool to work with Wikidata and Wikibase instances",source:"@site/docs/intro.md",sourceDirName:".",slug:"/intro",permalink:"/docs/intro",editUrl:"https://github.com/weso/wb/website/docs/intro.md",tags:[],version:"current",frontMatter:{},sidebar:"tutorialSidebar"},c=[{value:"Source code",id:"source-code",children:[],level:2},{value:"How to run wb",id:"how-to-run-wb",children:[],level:2},{value:"Build from source",id:"build-from-source",children:[],level:2},{value:"Usage",id:"usage",children:[],level:2},{value:"Subcommand info",id:"subcommand-info",children:[],level:2},{value:"Subcommand validate",id:"subcommand-validate",children:[],level:2},{value:"Subcommand SPARQL",id:"subcommand-sparql",children:[{value:"Examples",id:"examples",children:[{value:"Get information about Q42",id:"get-information-about-q42",children:[],level:4},{value:"Validate Q42 with entity schema E42 from Wikidata",id:"validate-q42-with-entity-schema-e42-from-wikidata",children:[],level:4},{value:"Validate Q5 with entity schema E1 from a different Wikibase instance",id:"validate-q5-with-entity-schema-e1-from-a-different-wikibase-instance",children:[],level:4},{value:"Run a sparql query",id:"run-a-sparql-query",children:[],level:4}],level:3}],level:2},{value:"Other distribution possibilities",id:"other-distribution-possibilities",children:[],level:2}],d={toc:c};function m(e){var t=e.components,n=(0,i.Z)(e,s);return(0,r.kt)("wrapper",(0,a.Z)({},d,n,{components:t,mdxType:"MDXLayout"}),(0,r.kt)("h1",{id:"wb"},"wb"),(0,r.kt)("p",null,"Simple command line tool to work with Wikidata and Wikibase instances"),(0,r.kt)("h2",{id:"source-code"},"Source code"),(0,r.kt)("p",null,"The source code is available in the ",(0,r.kt)("a",{parentName:"p",href:"https://github.com/weso/wb"},"weso/wb")," repo"),(0,r.kt)("h2",{id:"how-to-run-wb"},"How to run wb"),(0,r.kt)("p",null,"It is possible to package ",(0,r.kt)("em",{parentName:"p"},"wb")," and create binaries for different systems (Windows, Linux, MacOS, Docker) and publish them using ",(0,r.kt)("a",{parentName:"p",href:"https://scala-cli.virtuslab.org/docs/commands/package"},"scala-cli package"),"."),(0,r.kt)("h2",{id:"build-from-source"},"Build from source"),(0,r.kt)("p",null,"It requires ",(0,r.kt)("a",{parentName:"p",href:"https://scala-cli.virtuslab.org/"},"scala-cli")," which can be downloaded for Linux, Windows and MacOS. "),(0,r.kt)("p",null,"Once you download it, you can create a binary using:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-sh"},"$ scala-cli package . -o wb -f\n...generates a binary called wb\n")),(0,r.kt)("p",null,"and run the program with:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-sh"},"$ ./wb\n. . . displays help message\n")),(0,r.kt)("h2",{id:"usage"},"Usage"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-sh"},"Usage:\n    wb info\n    wb validate\n    wb sparql\n\nInformation about Wikidata or Wikibase instances\n\nExample: wb info --schema E42 \n  Prints information about entity schema E42 from Wikidata\n\nOptions and flags:\n    --help\n        Display this help text.\n    --version, -v\n        Print the version number and exit.\n\nSubcommands:\n    info\n        Get info about entity\n    validate\n        Validate an entity with an entity schema\n    sparql\n        Run SPARQL query\n")),(0,r.kt)("h2",{id:"subcommand-info"},"Subcommand info"),(0,r.kt)("p",null,"Can be used to obtain information about Wikibase entities"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-sh"},"Usage: wb info --entity <entityId> [--wikibase <string>] [--schemaFormat <format>] [--mode <mode>] [--verbose <string>]\n\nGet info about entity\n\nOptions and flags:\n    --help\n        Display this help text.\n    --entity <entityId>, -e <entityId>\n        Entity Id, example: Q42\n    --wikibase <string>\n        Wikibase, default: wikidata, values: wikidata|rhizome|gndtest\n    --schemaFormat <format>\n        Schema format, default = ShExC. Possible values = ShExC|ShExJ\n    --mode <mode>\n        Info mode, default = Out. Possible values = Out|Raw\n    --verbose <string>\n        verbose level. 0 = nothing, 1 = info msgs, 2 = all msgs\n")),(0,r.kt)("h2",{id:"subcommand-validate"},"Subcommand validate"),(0,r.kt)("p",null,"Can be used to validate entities using entity schemas written in ShEx"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-sh"},"Usage:\n    wb validate --schema <string> --entity <entityId> [--wikibase <string>] [--shape <string>] [--shex-engine <engine>] [--result-format <format>] [--verbose <string>]\n    wb validate --schema-file <file> --entity <entityId> [--wikibase <string>] [--shape <string>] [--shex-engine <engine>] [--result-format <format>] [--verbose <string>]\n\nValidate an entity with an entity schema\n\nOptions and flags:\n    --help\n        Display this help text.\n    --schema <string>\n        Entity schema Id, example: E42\n    --schema-file <file>\n        File that contains entity schema\n    --entity <entityId>, -e <entityId>\n        Entity Id, example: Q42\n    --wikibase <string>\n        Wikibase, default: wikidata, values: wikidata|rhizome|gndtest\n    --shape <string>\n        Shape to validate in entity schema (if not specified, it uses Start shape)\n    --shex-engine <engine>\n        ShEx engine. Available engines: ShExS,Jena\n    --result-format <format>\n        result-format, default = Details, values=Details|Compact|JSON\n    --verbose <string>\n        verbose level. 0 = nothing, 1 = info msgs, 2 = all msgs\n")),(0,r.kt)("h2",{id:"subcommand-sparql"},"Subcommand SPARQL"),(0,r.kt)("p",null,"Can be used to run SPARQL queries on Wikidata and Wikibase instances"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-sh"},"Usage: wb sparql --query-file <file> [--wikibase <string>] [--result-format <string>] [--verbose <string>]\n\nRun SPARQL query\n\nOptions and flags:\n    --help\n        Display this help text.\n    --query-file <file>\n        File that contains the SPARQL query\n    --wikibase <string>\n        Wikibase, default: wikidata, values: wikidata|rhizome|gndtest\n    --result-format <string>\n        Result format. Default=AsciiTable. Values=Table|JSON|XML\n    --verbose <string>\n        verbose level. 0 = nothing, 1 = info msgs, 2 = all msgs\n")),(0,r.kt)("h3",{id:"examples"},"Examples"),(0,r.kt)("h4",{id:"get-information-about-q42"},"Get information about Q42"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-sh"},"wb info --entity Q42\n")),(0,r.kt)("h4",{id:"validate-q42-with-entity-schema-e42-from-wikidata"},"Validate Q42 with entity schema E42 from Wikidata"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-sh"},"wb validate --entity Q42 --schema E42\n")),(0,r.kt)("h4",{id:"validate-q5-with-entity-schema-e1-from-a-different-wikibase-instance"},"Validate Q5 with entity schema E1 from a different Wikibase instance"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-sh"},"wb validate --entity Q5 --schema E1 --wikibase GNDTest\n")),(0,r.kt)("h4",{id:"run-a-sparql-query"},"Run a sparql query"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-sh"},"wb sparql --query-file examples/query1.sparql\n")),(0,r.kt)("h2",{id:"other-distribution-possibilities"},"Other distribution possibilities"),(0,r.kt)("p",null,"It is possible to distribute/package the program using any of the ",(0,r.kt)("a",{parentName:"p",href:"https://scala-cli.virtuslab.org/docs/commands/package"},"scala-cli package")," options like Windows, docker, etc."),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},"Author: Jose Emilio Labra Gayo")))}m.isMDXComponent=!0}}]);