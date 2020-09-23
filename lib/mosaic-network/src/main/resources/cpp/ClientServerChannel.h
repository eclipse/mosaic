#ifndef __CLIENTSERVERCHANNEL_H__
#define __CLIENTSERVERCHANNEL_H__

#undef NaN
#include "ClientServerChannelMessages.pb.h"

#include <memory> // shared_ptr

typedef int SOCKET;
constexpr const int SOCKET_ERROR = -1;
constexpr const int INVALID_SOCKET = -1;

/**
 * Abstraction of socket communication between Ambassador and Federate (e.g. ns-3 or OMNeT++).
 */
namespace ClientServerChannelSpace {

enum CMD {
	CMD_UNDEF=-1,
//--> Federation management
    CMD_INIT = 1,
	CMD_SHUT_DOWN = 2,
//--> Update messages
	CMD_UPDATE_NODE = 10,
	CMD_REMOVE_NODE = 11,
//--> Advance Time
    CMD_ADVANCE_TIME = 20,
    CMD_NEXT_EVENT = 21,
	CMD_MSG_RECV = 22,
//--> Communication
    CMD_MSG_SEND = 30,
    CMD_CONF_RADIO = 31,
//--> General
	CMD_END = 40,
	CMD_SUCCESS = 41
};

enum RADIO_NUMBER {
	NO_RADIO=0,
	SINGLE_RADIO=1,
	DUAL_RADIO=2
};

enum CHANNEL_MODE {
	SINGLE_CHANNEL=1,		/* Radio stays on one channel the whole time */
    DUAL_CHANNEL=2	/* Radio alternates between two channels */
};

enum UPDATE_NODE_TYPE {
	UPDATE_ADD_RSU = 1,
	UPDATE_ADD_VEHICLE = 2,
	UPDATE_MOVE_NODE = 3,
	UPDATE_REMOVE_NODE = 4
};

enum RADIO_CHANNEL {
	SCH1 = 0,
	SCH2 = 1,
	SCH3 = 2,
	CCH = 3,
	SCH4 = 4,
	SCH5 = 5,
	SCH6 = 6,
	UNDEF_CHANNEL = 7
};

struct CSC_init_return{
    int64_t start_time;
    int64_t end_time;
};

struct CSC_node_data{
	int id;
	double x;
	double y;
};

struct CSC_radio_config{
	bool turnedOn;
	uint32_t ip_address;
	uint32_t subnet;
	int tx_power;
	CHANNEL_MODE channelmode;
	RADIO_CHANNEL primary_channel;
	RADIO_CHANNEL secondary_channel;
};

struct CSC_config_message{
	int64_t time;
    int msg_id;
    int node_id;
    RADIO_NUMBER num_radios;
    CSC_radio_config primary_radio;
    CSC_radio_config secondary_radio;
};

struct CSC_update_node_return{
	UPDATE_NODE_TYPE type;
	int64_t time;
	std::vector<CSC_node_data> properties;
};

struct CSC_topo_address{
	uint32_t ip_address;
	int ttl;
};

struct CSC_send_message{
	int64_t time;
	int node_id;
	RADIO_CHANNEL channel_id;
	int message_id;
	int length;
	CSC_topo_address topo_address;
};

class ClientServerChannel {

	public:
		/** Constructor. */
		ClientServerChannel();

		/** Destructor. */
		virtual ~ClientServerChannel();

		/** Prepares connection with a socket bound to the given port on host. */
		virtual int	prepareConnection(std::string host, uint32_t port);

		/** Accepts connection to socket */
		virtual void connect();

		/*################## READING ####################*/

		/** reads a command via protobuf and returns it */
		virtual CMD	readCommand();

		/** reads an initialization message and returns it */
		virtual int readInit(CSC_init_return &return_value);

		/** reads a add RSU message and returns it */
		virtual int readUpdateNode(CSC_update_node_return &return_value);

		/** Reads a configuration message from the channel and returns it */
		virtual int readConfigurationMessage(CSC_config_message &return_value);

		/** Reads a send message command and returns the corresponding message struct */
		virtual int readSendMessage(CSC_send_message &return_value);

		/** Reads TimeMessage from the channel and returns the contained time as a long */
		virtual int64_t readTimeMessage();

		/*################## WRITING ####################*/

		/** Byte protocol control method for writeCommand. */
		virtual void writeCommand(CMD cmd);

		/** Write a message containing a port number to the output */
		virtual void writePort(uint32_t port);

		/** Request a time advance from the RTI */
		virtual void writeTimeMessage(int64_t time);

		/** Signal and hand a received Message to the RTI */
		virtual void writeReceiveMessage(uint64_t time, int node_id, int message_id, RADIO_CHANNEL channel, int rssi);

	private:
		/** Initial server sock, which accepts connection of Ambassador. */
		SOCKET servsock;

		/** Working sock for communication. */
		SOCKET sock;

		/** Socket name **/
		std::string channel_name;

		/** Converts commands to protobuf-internal commands */
		virtual CommandMessage_CommandType cmdToProtoCMD(CMD cmd);

		/** Converts protobuf commands to CMD enum */
		virtual CMD protoCMDToCMD(CommandMessage_CommandType cmd);

		/** Reads a Varint from a socket and returns it */
		virtual std::shared_ptr < uint32_t > readVarintPrefix(SOCKET sock);

		/** converts a channel given as a protobuf internal enum to our channel enum */
		virtual RADIO_CHANNEL protoChannelToChannel(RadioChannel protoChannel);

		/** converts a channel given as our channel enum to a protobuf internal channel enum */
		virtual RadioChannel channelToProtoChannel(RADIO_CHANNEL channel);
};

}//END NAMESPACE
#endif
