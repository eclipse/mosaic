# Eclipse MOSAIC The Simulation Suite for Connected and Automated Mobility

Fork of https://github.com/eclipse/mosaic

[![License](https://img.shields.io/badge/License-EPL%202.0-green.svg)](https://opensource.org/licenses/EPL-2.0)

## License

Eclipse MOSAIC is licensed under the [Eclipse Public License Version 2](https://eclipse.org/legal/epl-v20.html).

## Docker based development environment
It preinstalls Java, Maven, SUMO and Eclipse MOSAIC

### 1. Fetch sources
```bash
git clone https://github.com/ide3a/mosaic
```

### 2. Build Docker image

```bash
cd mosaic
docker build . -t mosaic-docker
```

### 3. Run Docker container

#### MacOS/Linux

```bash
docker run -v $(pwd):/home/mosaic/mosaic -p 8443:8443 --rm -it mosaic-docker
```
#### Windows

```bash
docker run -v %cd%:/home/mosaic/mosaic -p 8443:8443 --rm -it mosaic-docker
```

### Compile inside the container
```bash
cd mosaic
mvn clean install
```
