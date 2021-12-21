# :desktop_computer: Server Starter [![GitHub Latest Release](https://img.shields.io/github/v/release/Sxtanna/server-starter?style=flat-square)](https://github.com/Sxtanna/server-starter/releases)
### **Utility application for quickly starting auto-updating server instances.**


<br/>

## :books: Configuration 

### :computer: **Type** 
`default: spigot`
> The type of server that will be started
 - Spigot
 - Bungee
 - Purpur
 - Puffer
 - Puffer-Purpur
 - Velocity
 - ~Custom Jar URL~ :no_entry:

### :file_folder: **Path**
`default: '' (meaning the working directory)`
> The path, absolute or relative, where the server should be

### :bar_chart: **Size**
`default: 2048:2048`
> The `min:max` memory allocation to use for the server, min value must not be greater than max
 - small
 - large
 - `min:max` pattern `(?<min>\d+):(?<max>\d+)`
 
### :gem: **Version**
 `default: latest`
> The version of minecraft to attempt to start for the provided type, patch version can be dropped
 - 1.8.8
 - 1.9.4
 - 1.10.2
 - 1.11.2
 - 1.12.2
 - 1.13.2
 - 1.14.4
 - 1.15.2
 - 1.16.5
 - 1.17.1
 - 1.18.1
 - `cM.M.P` custom version, prefixed with `c` ex. `c1.14.1`
> Velocity always requires a supplied custom version
 - `-v c3.0.0`
 
 
## :scroll: **How To**

`java -jar server-starter-[version].jar {cli-override-options}`

### :notebook: **CLI Options**
> CLI Options will override options stored in the generated `server-starter.conf` configuration file

#### Type
```
alias: t
usage: --type spigot
```
#### Path
```
alias: p
usage: --path C:\Users\Username\Desktop\Server
```
#### Size
```
alias: s
usage: --size 1024:4096
```
#### Version
```
alias: v
usage: --version 1.17.1
```


### **Custom Config Entries**

#### No Gui Argument
> ex. `no-gui="--noconsole"` <- because for some reason `-nogui` doesn't always work :)
```
path: no-gui
type: String
```