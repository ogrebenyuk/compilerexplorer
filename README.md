Compiler Explorer plugin for CLion
---

This plugin shows compilation results from a remote Compiler Explorer instance.

Default settings are good for a wide variety of applications.

A big feature is _local preprocessing_: first run local compiler, then send preprocessed source for remote compilation.
When using this feature (enabled by default), local and remote compilers need to target the same platform: 
either run on the same platform, or one has to cross-compile to the other.

When compiling large source files, tune your Compiler Explorer instance to increase size and time thresholds 
(`etc/config/compiler-explorer.defaults.properties`):<br/>
`compileTimeoutMs=1000000`<br/>
`max-asm-size=1000000000`<br/>
