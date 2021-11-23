//
//  Simple message queuing broker in C++
//  Same as request-reply broker but using QUEUE device
//

#include <zmq.hpp>
#include <zhelpers.hpp>

int main (int argc, char *argv[])
{
    zmq::context_t context(1);

    //  Socket facing server. i.e. Eclipse MOSAIC
    zmq::socket_t backend (context, ZMQ_XSUB);
    backend.bind("tcp://*:5321");

    //  Socket facing clients i.e. Python
    zmq::socket_t frontend (context, ZMQ_XSUB);
    frontend.bind("tcp://*:6666");

    //  Start the proxy
    zmq::proxy(static_cast<void*>(frontend),
               static_cast<void*>(backend),
               nullptr);
    return 0;
}