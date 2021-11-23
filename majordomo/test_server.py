import zmq
from zeromq_interact_pb2 import ZInteract

def main():
    context = zmq.Context()
    publisherSocket = context.socket(zmq.PUB)
    publisherSocket.connect('tcp://localhost:5321')
    publishName = "VehicleUpdates".encode('utf-8')

    counter = 0

    print('Server started...')
    while True:
        counter += 0.00000001
        message = str(counter)
        publisherSocket.send_multipart([publishName, message.encode('utf-8')])

if __name__ == "__main__":
    main()
