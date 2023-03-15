# change-assembly-version-plugin

This plug-in tries to change specific line(s) in file(s). The effect is to change the assembly version.

Specifically, it will try to find the file `FileName` and inside that file look for strings matching the `RegexPattern`, replacing it with the actual current build number, as specified by `ReplacementPattern`.
