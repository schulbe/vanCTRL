from bluetooth import *

server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

uuid = "1e0ca4ea-299d-4335-93eb-27fcfe7fa848"

advertise_service( server_sock, "SampleServer",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ],
                   #                   protocols = [ OBEX_UUID ]
                   )

print("Waiting for connection on RFCOMM channel %d" % port)

client_sock, client_info = server_sock.accept()
print("Accepted connection from ", client_info)

try:
    #    data = client_sock.recv(1024)
    #   if len(data) == 0:
    #      raise TypeError
    # print("received [%s]" % data)
    #client_sock.send(data)
    # client_sock.send(data)
    while True:
        msg = input("What should we send?")
        client_sock.send(bytes(msg, 'utf-8'))
        print("SENT MOFO!")
except IOError:
    pass

print("disconnected")

client_sock.close()
server_sock.close()
print("all done")
