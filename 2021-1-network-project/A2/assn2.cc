#include <fstream>
#include "ns3/core-module.h"
#include "ns3/network-module.h"
#include "ns3/internet-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/applications-module.h"

#include "udp-reliable-echo-helper.h"
#include "udp-reliable-echo-client.h"
#include "udp-reliable-echo-server.h"

using namespace ns3;

NS_LOG_COMPONENT_DEFINE ("assn2");

// =========================
// static void PacketTx (Ptr<const Packet> p)
// {
//     NS_LOG_UNCOND (Simulator::Now().GetSeconds()<<"\t"
//                         <<"A new packet is sent at Node 0");
// }

// static void PacketRx (Ptr<const Packet> p)
// {
//     NS_LOG_UNCOND (Simulator::Now().GetSeconds()<<"\t"
//                     <<"A packet is received at Node 1");
// }
// ===========================

int 
main (int argc, char *argv[])
{
    CommandLine cmd;
    cmd.Parse(argc,argv);

    LogComponentEnable("UdpRelEchoClientApplication", LOG_LEVEL_INFO);
    // LogComponentEnable("UdpRelEchoServerApplication", LOG_LEVEL_INFO);
    // LogComponentEnable("OnOffApplication", LOG_LEVEL_INFO);

    // Create nodes
    NS_LOG_INFO ("Create nodes.");
    Ptr<Node> nClient   = CreateObject<Node> ();
    Ptr<Node> nOnoff    = CreateObject<Node> ();
    Ptr<Node> nRtr      = CreateObject<Node> ();
    Ptr<Node> nReceiver = CreateObject<Node> ();

    NodeContainer nodes = NodeContainer (nClient, nOnoff, nRtr, nReceiver);

    NodeContainer nClientnRtr    = NodeContainer(nClient, nRtr);
    NodeContainer nOnoffnRtr     = NodeContainer(nOnoff, nRtr);
    NodeContainer nRtrnReceiver  = NodeContainer(nRtr, nReceiver);

    InternetStackHelper stack;
    stack.Install (nodes);

    // Create P2P channels
    PointToPointHelper p2p;
    p2p.SetDeviceAttribute ("DataRate", StringValue ("1Mbps"));
    p2p.SetChannelAttribute ("Delay", StringValue ("5ms"));
    // modified ==================
    p2p.SetQueue("ns3::DropTailQueue", "MaxSize", StringValue("1500B"));
    // ~modified ===================

    NetDeviceContainer dClientdRtr   = p2p.Install (nClientnRtr);
    NetDeviceContainer dOnoffdRtr    = p2p.Install (nOnoffnRtr);
    NetDeviceContainer dRtrdReceiver = p2p.Install (nRtrnReceiver);

    // Add IP addresses
    NS_LOG_INFO ("Assign IP Addresses.");
    Ipv4AddressHelper ipv4;
    ipv4.SetBase ("10.1.1.0", "255.255.255.0");
    Ipv4InterfaceContainer iClientiRtr = ipv4.Assign (dClientdRtr);
    ipv4.SetBase ("10.1.2.0", "255.255.255.0");
    Ipv4InterfaceContainer iOnoffiRtr = ipv4.Assign (dOnoffdRtr);
    ipv4.SetBase ("10.1.3.0", "255.255.255.0");
    Ipv4InterfaceContainer iRtriReceiver = ipv4.Assign (dRtrdReceiver);

    // Set up the routing tables
    Ipv4GlobalRoutingHelper::PopulateRoutingTables ();

    // Implement TCP & UDP sinks to the destinations
    uint16_t reliablePort = 9;
    uint16_t onoffPort = 10;

    // Client
    UdpRelEchoClientHelper client(iRtriReceiver.GetAddress(1), reliablePort);
	client.SetAttribute("PacketSize", UintegerValue(1024));
    client.SetAttribute("MaxPackets", UintegerValue(1000000));
	client.SetAttribute("Interval", TimeValue(Seconds(0.01)));

    ApplicationContainer clientApps;
    clientApps.Add(client.Install(nClient));
    clientApps.Start(Seconds(1.0));
    clientApps.Stop(Seconds(30.0));

    UdpRelEchoServerHelper server(reliablePort);
    ApplicationContainer serverApps(server.Install(nReceiver));
    serverApps.Start(Seconds(0.0));
    serverApps.Stop(Seconds(31.0));

    // Onoff
    OnOffHelper onoff("ns3::UdpSocketFactory", InetSocketAddress(iRtriReceiver.GetAddress(1), onoffPort));
    onoff.SetAttribute("OnTime", StringValue("ns3::ConstantRandomVariable[Constant=1]"));
    onoff.SetAttribute("OffTime", StringValue("ns3::ConstantRandomVariable[Constant=1]"));
    onoff.SetAttribute("DataRate", DataRateValue(10000000));
    onoff.SetAttribute("PacketSize", UintegerValue(512));
    ApplicationContainer onoffApp = onoff.Install(nOnoff);
    onoffApp.Start (Seconds (1.0));
    onoffApp.Stop (Seconds (30.0));

    PacketSinkHelper sink("ns3::UdpSocketFactory", Address (InetSocketAddress(Ipv4Address::GetAny(), onoffPort)));
    ApplicationContainer sinkApp = sink.Install(nReceiver);
    sinkApp.Start(Seconds(0.0));
    sinkApp.Stop(Seconds(31.0));

    // clientApps.Get(0) -> TraceConnectWithoutContext("Tx", MakeCallback (&PacketTx));
    // serverApps.Get(0) -> TraceConnectWithoutContext("Rx", MakeCallback (&PacketRx));

    Simulator::Stop (Seconds (33));
    Simulator::Run ();
    Simulator::Destroy ();

    return 0;
}