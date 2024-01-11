Some of the packages needed by PX-MBT are distributed through [jitpack](https://jitpack.io/). If they fail to download (e.g. maybe jitpack site is down) you can install them manually.
These are the packages distributed from jitpack. The file `pom.xml` of PX-MBT specifies which version it usues.

* [aplib version 1.8.1](https://github.com/iv4xr-project/aplib/releases/tag/1.8.1)
* [jocc version 1.1.0](https://github.com/iv4xr-project/jocc/releases/tag/1.1.0)
* [iv4xrdemo version 2.3.4c](https://github.com/iv4xr-project/iv4xrDemo/releases/tag/2.3.4c)
* [iv4xr-mbt version 1.2.2](https://github.com/iv4xr-project/iv4xr-mbt/releases/tag/1.2.2)

In case package X fails to download from jitpack, download X manually from the link above. Unzip it, and do `mvn install` from X's directory.
