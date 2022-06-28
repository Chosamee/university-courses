#include "ns3/core-module.h"
#include "ns3/network-module.h"
#include "ns3/internet-module.h"
#include "ns3/wifi-module.h"
#include "ns3/applications-module.h"
#include "ns3/mobility-module.h"
#include "ns3/ipv4-global-routing-helper.h"

#include "streaming-helper.h"
#include "streaming-client.h"
#include "streaming-streamer.h"

using namespace ns3;

NS_LOG_COMPONENT_DEFINE ("assn3");

int 
main (int argc, char *argv[])
{
    CommandLine cmd;
    cmd.Parse(argc,argv);

    // LogComponentEnable("UdpRelEchoClientApplication", LOG_LEVEL_INFO);
    LogComponentEnable("UdpRelEchoServerApplication", LOG_LEVEL_INFO);

    // 1. Create nodes : Make 1 STA and 1 AP
    NS_LOG_INFO ("Create nodes.");
    NodeContainer wifiStaNode;
    wifiStaNode.Create(1);
    NodeContainer wifiApNode;
    wifiApNode.Create(1);

    // 2. Create PHY layer (wireless channel)
    YansWifiChannelHelper channel = YansWifiChannelHelper::Default();
    YansWifiPhyHelper phy = YansWifiPhyHelper::Default();
    phy.SetChannel (channel.Create());
    phy.Set ("Antennas", UintegerValue(3));
    phy.Set ("MaxSupportedTxSpatialStreams", UintegerValue (3));
    phy.Set ("MaxSupportedRxSpatialStreams", UintegerValue (3));
    phy.Set ("ChannelWidth", UintegerValue(80));
    phy.Set ("RxNoiseFigure", DoubleValue(45.0));

    // 3. Create WLAN setting
    WifiHelper wifi;
    wifi.SetStandard (WIFI_PHY_STANDARD_80211ac);
    wifi.SetRemoteStationManager ("ns3::ConstantRateWifiManager",
        "DataMode", StringValue("VhtMcs9"),
        "ControlMode", StringValue ("VhtMcs0"));
     
    // 4. Create MAC layer, StaDevice
    WifiMacHelper mac;
    Ssid ssid = Ssid("assn3");
    mac.SetType ("ns3::StaWifiMac",
        "Ssid", SsidValue(ssid),
        "ActiveProbing", BooleanValue (false));

    NetDeviceContainer staDevice;
    staDevice = wifi.Install(phy, mac, wifiStaNode);

    // 5. Create ApDevice
    mac.SetType("ns3::ApWifiMac",
        "Ssid", SsidValue (ssid),
        "EnableBeaconJitter", BooleanValue (false));
    
    NetDeviceContainer apDevice;
    apDevice = wifi.Install (phy, mac, wifiApNode);

    // 6. Create Network layer
    InternetStackHelper stack;
    stack.Install(wifiApNode);
    stack.Install(wifiStaNode);

    Ipv4AddressHelper address;
    address.SetBase("192.168.1.0", "255.255.255.0");
    Ipv4InterfaceContainer staNodeInterface;
    Ipv4InterfaceContainer apNodeInterface;
    staNodeInterface = address.Assign(staDevice);
    apNodeInterface = address.Assign(apDevice);
    

    // 7. Locate nodes
    MobilityHelper mobility;
    Ptr<ListPositionAllocator> positionAlloc = CreateObject<ListPositionAllocator> ();
    positionAlloc -> Add (Vector(0.0, 0.0, 0.0));
    positionAlloc -> Add (Vector(1.0, 0.0, 0.0));
    mobility.SetPositionAllocator (positionAlloc);
    mobility.SetMobilityModel("ns3::ConstantPositionMobilityModel");
    mobility.Install(wifiApNode);
    mobility.Install(wifiStaNode);

    // 8. Create Transport layer (UDP)
    
    UdpRelEchoServerHelper client(9);
    ApplicationContainer serverApp = client.Install (wifiStaNode.Get(0));
    serverApp.Start(Seconds(0.0));
    serverApp.Stop(Seconds(10.0));

    UdpRelEchoClientHelper streamer(staNodeInterface.GetAddress(0), 9);
    streamer.SetAttribute ("MaxPackets", UintegerValue(4294967292)); // Maximum number of packets = 2**32
    streamer.SetAttribute ("Interval", TimeValue(Seconds(1.0/90.0))); // packets/s
    streamer.SetAttribute ("PacketSize", UintegerValue (1472));

    ApplicationContainer clientApp = streamer.Install (wifiApNode.Get(0));
    clientApp.Start (Seconds (1.0));
    clientApp.Stop (Seconds(10.0));


    // 9. Simulation Run and Calc. throughput
    Simulator::Stop (Seconds (10.0));
    Simulator::Run ();
    Simulator::Destroy();

    // uint32_t totalPacketsRecv = DynamicCast<UdpEchoServer> (serverApp.Get(0))->GetReceived();
    // double throughput = totalPacketsRecv *payloadSize * 8/(10 * 1000000.0);
    // std::cout << "ThroughPut: " << throughput << "Mbps" << '\n';

    return 0;
}