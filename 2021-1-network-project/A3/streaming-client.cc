#include "ns3/log.h"
#include "ns3/ipv4-address.h"
#include "ns3/ipv6-address.h"
#include "ns3/address-utils.h"
#include "ns3/nstime.h"
#include "ns3/inet-socket-address.h"
#include "ns3/inet6-socket-address.h"
#include "ns3/socket.h"
#include "ns3/udp-socket.h"
#include "ns3/simulator.h"
#include "ns3/socket-factory.h"
#include "ns3/packet.h"
#include "ns3/uinteger.h"
#include <random>

#include "streaming-client.h"
#include "ns3/seq-ts-header.h"

namespace ns3 {

NS_LOG_COMPONENT_DEFINE ("UdpRelEchoServerApplication");

NS_OBJECT_ENSURE_REGISTERED (UdpRelEchoServer);

TypeId
UdpRelEchoServer::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::UdpRelEchoServer")
    .SetParent<Application> ()
    .SetGroupName("Applications")
    .AddConstructor<UdpRelEchoServer> ()
    .AddAttribute ("Port", "Port on which we listen for incoming packets.",
                   UintegerValue (9),
                   MakeUintegerAccessor (&UdpRelEchoServer::m_port),
                   MakeUintegerChecker<uint16_t> ())
    .AddTraceSource ("Rx", "A packet has been received",
                     MakeTraceSourceAccessor (&UdpRelEchoServer::m_rxTrace),
                     "ns3::Packet::TracedCallback")
    .AddTraceSource ("RxWithAddresses", "A packet has been received",
                     MakeTraceSourceAccessor (&UdpRelEchoServer::m_rxTraceWithAddresses),
                     "ns3::Packet::TwoAddressTracedCallback")
  ;
  return tid;
}

UdpRelEchoServer::UdpRelEchoServer ()
{
  NS_LOG_FUNCTION (this);
  m_consumeEvent = EventId ();
  m_generateEvent = EventId ();
}

UdpRelEchoServer::~UdpRelEchoServer()
{
  NS_LOG_FUNCTION (this);
  m_socket = 0;
  m_socket6 = 0;
}

void
UdpRelEchoServer::DoDispose (void)
{
  NS_LOG_FUNCTION (this);
  Application::DoDispose ();
}

void 
UdpRelEchoServer::StartApplication (void)
{
  NS_LOG_FUNCTION (this);

  if (m_socket == 0)
    {
      TypeId tid = TypeId::LookupByName ("ns3::UdpSocketFactory");
      m_socket = Socket::CreateSocket (GetNode (), tid);
      InetSocketAddress local = InetSocketAddress (Ipv4Address::GetAny (), m_port);
      if (m_socket->Bind (local) == -1)
        {
          NS_FATAL_ERROR ("Failed to bind socket");
        }
      if (addressUtils::IsMulticast (m_local))
        {
          Ptr<UdpSocket> udpSocket = DynamicCast<UdpSocket> (m_socket);
          if (udpSocket)
            {
              // equivalent to setsockopt (MCAST_JOIN_GROUP)
              udpSocket->MulticastJoinGroup (0, m_local);
            }
          else
            {
              NS_FATAL_ERROR ("Error: Failed to join multicast group");
            }
        }
    }

  if (m_socket6 == 0)
    {
      TypeId tid = TypeId::LookupByName ("ns3::UdpSocketFactory");
      m_socket6 = Socket::CreateSocket (GetNode (), tid);
      Inet6SocketAddress local6 = Inet6SocketAddress (Ipv6Address::GetAny (), m_port);
      if (m_socket6->Bind (local6) == -1)
        {
          NS_FATAL_ERROR ("Failed to bind socket");
        }
      if (addressUtils::IsMulticast (local6))
        {
          Ptr<UdpSocket> udpSocket = DynamicCast<UdpSocket> (m_socket6);
          if (udpSocket)
            {
              udpSocket->MulticastJoinGroup (0, local6);
            }
          else
            {
              NS_FATAL_ERROR ("Error: Failed to join multicast group");
            }
        }
    }
  m_socket->SetRecvCallback (MakeCallback (&UdpRelEchoServer::HandleRead, this));
  m_socket6->SetRecvCallback (MakeCallback (&UdpRelEchoServer::HandleRead, this));

  // modified
  ScheduleConsume (Seconds(1.0 + 1.0/60.0));
  ScheduleGenerate (Seconds(0.0));
}

void 
UdpRelEchoServer::StopApplication ()
{
  NS_LOG_FUNCTION (this);

  if (m_socket != 0) 
    {
      m_socket->Close ();
      m_socket->SetRecvCallback (MakeNullCallback<void, Ptr<Socket> > ());
    }
  if (m_socket6 != 0) 
    {
      m_socket6->Close ();
      m_socket6->SetRecvCallback (MakeNullCallback<void, Ptr<Socket> > ());
    }
    Simulator::Cancel(m_consumeEvent);
    Simulator::Cancel(m_generateEvent);
}

void 
UdpRelEchoServer::HandleRead (Ptr<Socket> socket)
{
  NS_LOG_FUNCTION (this << socket);
  m_sock = socket;
  Ptr<Packet> packet;
  Address from;
  Address localAddress;

  while ((packet = socket->RecvFrom (from)))
    {
      socket->GetSockName (localAddress);
      from_fixed = from;
      localAddress_fixed = localAddress;
      m_rxTrace (packet);
      m_rxTraceWithAddresses (packet, from, localAddress);
      SeqTsHeader seqTs;
      packet -> RemoveHeader (seqTs);
      uint32_t currentSequenceNumber = seqTs.GetSeq();
      
      // ==========================
      //  use force packet loss
      /*
      int loss_rate = 5;
      std::random_device rd;
      std::mt19937 gen(rd());
      std::uniform_int_distribution<int> dis(0, 99);

      if (dis(gen) >= loss_rate)
      {
      packetBuffer[currentSequenceNumber/100].push_back(currentSequenceNumber%100);
      NS_LOG_INFO("frame index " <<currentSequenceNumber/100<<" seqs "<< currentSequenceNumber%100);
      }
      */
      // use normal mode
      packetBuffer[currentSequenceNumber/100].push_back(currentSequenceNumber%100);
      NS_LOG_INFO("frame index " <<currentSequenceNumber/100<<" seqs "<< currentSequenceNumber%100);
      // ============================

      packet->RemoveAllPacketTags ();
      packet->RemoveAllByteTags ();
    }
}

void
UdpRelEchoServer::ScheduleConsume(Time dt)
{
  NS_LOG_FUNCTION (this << dt);
  m_consumeEvent = Simulator::Schedule (dt, &UdpRelEchoServer::FrameConsumer, this);
}

void
UdpRelEchoServer::ScheduleGenerate(Time dt)
{
  NS_LOG_FUNCTION (this << dt);
  m_generateEvent = Simulator::Schedule (dt, &UdpRelEchoServer::FrameGenerator, this);
}

void
UdpRelEchoServer::FrameGenerator()
{
  for (auto iter = packetBuffer.begin(); iter != packetBuffer.end();)
  {
    if (frameBuffer.size() < 40)
    {
      if (iter->second.size() == 100)
      {
        frameBuffer[iter->first] = 1;
        packetBuffer.erase(iter++);
      }
      else
      {
        ++iter;
      }
    }
    else
    {
      ++iter;
    }
  }
  ScheduleGenerate(Seconds(1.0/20.0));
}

void
UdpRelEchoServer::FrameConsumer()
{
  if(frameBuffer.find(frameCnt) != frameBuffer.end())
  {
    NS_LOG_INFO("FrameConsumerLog::Consume " << frameCnt);
    frameBuffer.erase(frameBuffer.find(frameCnt));
  }
  else
  {
    NS_LOG_INFO("FrameConsumerLog::NoConsume " << frameCnt);
  }

  //============ empty last buffer ============
  for (auto fiter = frameBuffer.begin(); fiter != frameBuffer.end();)
  {
    if (fiter->first < frameCnt)
    {
      frameBuffer.erase(fiter++);
    }
    else
    {
      ++fiter;
    }
  }
  //==========================================

  Ptr<Packet> p = Create<Packet> (100);
  TypeId tid = TypeId::LookupByName ("ns3::UdpSocketFactory");
  m_sock = Socket::CreateSocket (GetNode (), tid);
  InetSocketAddress local = InetSocketAddress (Ipv4Address::GetAny(), 9);
  m_sock->Bind(local);

  SeqTsHeader seqTs;
  if (frameBuffer.size() > 30)
  {
    seqTs.SetSeq(-1);
    p -> AddHeader (seqTs);
    m_sock->SendTo (p, 0, from_fixed);

  }
  else if (frameBuffer.size() < 5)
  {
    seqTs.SetSeq(-2);
    p -> AddHeader (seqTs);
    m_sock->SendTo (p, 0, from_fixed);
  }
  NS_LOG_INFO("FrameConsumerLog::RamainFrames: #" << frameBuffer.size());
  // use to plot
  // NS_LOG_INFO(Simulator::Now ().GetSeconds ()<<"\t"<<frameBuffer.size());
  frameCnt += 1;
  ScheduleConsume(Seconds(1.0/60.0));
}

} // Namespace ns3
