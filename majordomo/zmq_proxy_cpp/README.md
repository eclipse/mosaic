# Docker container for ZMQ proxy

### Clone libzmq repository
git clone https://github.com/zeromq/libzmq.git

### Build the container in the repo

cd libzmq
docker build . -t libzmq-docker

### Build the container in this folder

docker build . -t zeromq-docker

### Run the container

docker run -v $(pwd):/home/zeromq/zeromq --rm -it zeromq-docker

### Compile the code

cd zeromq
gcc -o zproxy zproxy.cpp -lzmq
