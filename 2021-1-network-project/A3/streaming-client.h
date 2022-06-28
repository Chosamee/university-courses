#ifndef UDP_REL_ECHO_SERVER_H
#define UDP_REL_ECHO_SERVER_H

#include "ns3/application.h"
#include "ns3/event-id.h"
#include "ns3/ptr.h"
#include "ns3/address.h"
#include "ns3/traced-callback.h"
#include <map>
#include <vector>

namespace ns3 {

class Socket;
class Packet;

class UdpRelEchoServer : public Application 
{
public:
  static TypeId GetTypeId (void);
  UdpRelEchoServer ();
  virtual ~UdpRelEchoServer ();

protected:
  virtual void DoDispose (void);

private:

  virtual void StartApplication (void);
  virtual void StopApplication (void);

  void HandleRead (Ptr<Socket> socket);

  uint16_t m_port; //!< Port on which we listen for incoming packets.
  Ptr<Socket> m_socket; //!< IPv4 Socket
  Ptr<Socket> m_socket6; //!< IPv6 Socket
  Address m_local; //!< local multicast address
  std::map<int, int> frameBuffer;
  std::map<int, std::vector<int>> packetBuffer;

  void FrameGenerator();
  void FrameConsumer();
  int frameCnt = 0;

  EventId m_consumeEvent;
  EventId m_generateEvent;
  void ScheduleConsume(Time dt);
  void ScheduleGenerate(Time dt);

  Ptr<Socket> m_sock;
  Address from_fixed;
  Address localAddress_fixed;

  /// Callbacks for tracing the packet Rx events
  TracedCallback<Ptr<const Packet> > m_rxTrace;

  /// Callbacks for tracing the packet Rx events, includes source and destination addresses
  TracedCallback<Ptr<const Packet>, const Address &, const Address &> m_rxTraceWithAddresses;
};

} // namespace ns3

#endif /* UDP_ECHO_SERVER_H */

