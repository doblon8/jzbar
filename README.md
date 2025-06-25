# jzbar
Java Foreign Function & Memory bindings for
[ZBar](https://zbar.sourceforge.net/).
## Installation
jzbar is published to Maven Central. Add it as a dependency in your project's build configuration using the coordinates:
- Group ID: `io.github.doblon8`
- Artifact ID: `jzbar`
- Version: `0.0.1`
## Publishing to Maven Central
To publish this project to Maven Central:
1. Create a `settings.xml` file with the following content:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <profiles>
    <profile>
      <id>jzbar</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.keyname><!-- your GPG key ID --></gpg.keyname>
        <gpg.passphrase><!-- your GPG passphrase --></gpg.passphrase>
      </properties>
    </profile>
  </profiles>
  <servers>
    <server>
      <id>central</id>
      <username><!-- your token username --></username>
      <password><!-- your token password --></password>
    </server>
  </servers>
</settings>
```
2. Make sure your GPG key is published to a public keyserver, e.g.:
```sh
gpg --keyserver keyserver.ubuntu.com --send-keys <your-key-id>
```
3. Install dependencies:
```sh
mvn clean install -P publish
```
4. Tag your release. For example:
```sh
git tag v0.0.1
git push origin v0.0.1
```
5. Build and publish to Maven Central:
```sh
mvn clean deploy -P publish
```

