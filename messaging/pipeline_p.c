//  Simple message queuing broker
//  Same as request-reply broker but using shared queue proxy

#include "zhelpers.h"
#include <czmq.h>

int main (void) 
{
    void *context = zmq_ctx_new ();

    //  Socket facing server. i.e. Eclipse MOSAIC
    void *backend = zmq_socket (context, ZMQ_PUSH);
    int rc = zmq_bind (backend, "tcp://127.0.0.1:1111");
    assert (rc == 0);

    //  Socket facing clients, i.e. Python
    void *frontend = zmq_socket (context, ZMQ_PULL);
    rc = zmq_bind (frontend, "tcp://127.0.0.1:2222");
    assert (rc == 0);

    //  Start the proxy
    zmq_proxy (frontend, backend, NULL);

    //  We never get here...
    zmq_close (frontend);
    zmq_close (backend);
    zmq_ctx_destroy (context);
    return 0;
}