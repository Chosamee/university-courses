#include <iostream>
#include <fstream>

#include "ns3/core-module.h"
#include "ns3/network-module.h"
#include "ns3/applications-module.h"
#include "ns3/bridge-module.h"
#include "ns3/csma-module.h"
#include "ns3/internet-module.h"

using namespace ns3;

static void
RXtime (std::string context, Ptr<const Packet> p, const Address &a)
{
    static double bytes0, bytes1, bytes2=0;
    if (context == "Default"){
        bytes0+=p->GetSize();
        NS_LOG_UNCOND("0\t" << Simulator::Now().GetSeconds()
            << "\t" << bytes0*8/1000000/(Simulator::Now().GetSeconds()-1));
    }
    else if (context == "Flow1"){
        bytes1+=p->GetSize();
        NS_LOG_UNCOND("1\t" << Simulator::Now().GetSeconds()
            << "\t" << bytes1*8/1000000/(Simulator::Now().GetSeconds()-1));
    } else if(context == "Flow2"){
        bytes2+=p->GetSize();
        NS_LOG_UNCOND("2\t" << Simulator::Now().GetSeconds()
            << "\t" << bytes2*8/1000000/(Simulator::Now().GetSeconds()-3));
    }
}

NS_LOG_COMPONENT_DEFINE ("CsmaBridgeExample");

int 
main (int argc, char *argv[])
{
	#if 0 
	LogComponentEnable ("CsmaBridgeExample", LOG_LEVEL_INFO);
	#endif

	CommandLine cmd;
	cmd.Parse (argc, argv);

	NS_LOG_INFO ("Create nodes.");
	NodeContainer terminals;
	terminals.Create (4);

	NodeContainer csmaSwitch;
	csmaSwitch.Create (1);

	NS_LOG_INFO ("Build Topology");
	CsmaHelper csma;
	csma.SetChannelAttribute ("DataRate", DataRateValue (5000000));
	csma.SetChannelAttribute ("Delay", TimeValue (MicroSeconds (10)));

	NetDeviceContainer terminalDevices;
	NetDeviceContainer switchDevices;

	for (int i = 0; i < 4; i++)
	{
		NetDeviceContainer link = csma.Install (NodeContainer (terminals.Get (i), csmaSwitch));
		terminalDevices.Add (link.Get (0));
		switchDevices.Add (link.Get (1));
	}
	
	Ptr<Node> switchNode = csmaSwitch.Get (0);
	BridgeHelper bridge;
	bridge.Install (switchNode, switchDevices);

	InternetStackHelper internet;
	internet.Install (terminals);

	NS_LOG_INFO ("Assign IP Addresses.");
	Ipv4AddressHelper ipv4;
	ipv4.SetBase ("10.1.1.0", "255.255.255.0");
	ipv4.Assign (terminalDevices);


	// Default Flow n2 -> n1
	NS_LOG_INFO ("Create Applications.");
	uint16_t port = 9;
	PacketSinkHelper sink ("ns3::UdpSocketFactory",
						Address (InetSocketAddress (Ipv4Address::GetAny (), port)));
	OnOffHelper onoff ("ns3::UdpSocketFactory", 
						Address (InetSocketAddress (Ipv4Address ("10.1.1.2"), port)));
	onoff.SetAttribute ("OnTime", StringValue ("ns3::ConstantRandomVariable[Constant=1]"));
	onoff.SetAttribute ("OffTime", StringValue ("ns3::ConstantRandomVariable[Constant=0]"));
	onoff.SetAttribute ("DataRate", DataRateValue (10000000));
	ApplicationContainer app = onoff.Install (terminals.Get(2));
	app.Start (Seconds (1.0));
	app.Stop (Seconds (10.0));
	app = sink.Install (terminals.Get (1));
	app.Start (Seconds (1.0));
	app.Get(0)->TraceConnect("Rx", "Default", MakeCallback(&RXtime));


	// Flow1 n0 -> n1
	PacketSinkHelper sink2 ("ns3::UdpSocketFactory",
						Address (InetSocketAddress (Ipv4Address::GetAny (), port+1)));
	OnOffHelper onoff2 ("ns3::UdpSocketFactory", 
						Address (InetSocketAddress (Ipv4Address ("10.1.1.2"), port+1)));
	onoff2.SetAttribute ("DataRate", DataRateValue (5000000));
	onoff2.SetAttribute ("OnTime", StringValue ("ns3::ConstantRandomVariable[Constant=1]"));
	onoff2.SetAttribute ("OffTime", StringValue ("ns3::ConstantRandomVariable[Constant=0]"));
	ApplicationContainer app2 = onoff2.Install (terminals.Get (0));
	app2.Start (Seconds (1.0));
	app2.Stop (Seconds (10.0));
	app2 = sink2.Install(terminals.Get (1));
	app2.Start (Seconds (1.0));
	app2.Get(0)->TraceConnect("Rx", "Flow1", MakeCallback(&RXtime));


	// Flow2 n3 -> n0
	PacketSinkHelper sink3 ("ns3::UdpSocketFactory",
						Address (InetSocketAddress (Ipv4Address::GetAny (), port+2)));
	OnOffHelper onoff3 ("ns3::UdpSocketFactory", 
						Address (InetSocketAddress (Ipv4Address ("10.1.1.1"), port+2)));
	onoff3.SetAttribute("DataRate", DataRateValue(10000000));
	onoff3.SetAttribute ("OnTime", StringValue ("ns3::ConstantRandomVariable[Constant=1]"));
	onoff3.SetAttribute ("OffTime", StringValue ("ns3::ConstantRandomVariable[Constant=0]"));
	ApplicationContainer app3 = onoff3.Install (terminals.Get (3));
	app3.Start (Seconds (3.0));
	app3.Stop (Seconds (13.0));
	app3 = sink3.Install (terminals.Get (0));
	app3.Start (Seconds (3.0));
	app3.Get(0)->TraceConnect("Rx", "Flow2", MakeCallback(&RXtime));

	NS_LOG_INFO ("Configure Tracing.");


	AsciiTraceHelper ascii;
	csma.EnableAsciiAll (ascii.CreateFileStream ("csma-bridge.tr"));

	csma.EnablePcapAll ("csma-bridge", false);

	NS_LOG_INFO ("Run Simulation.");
	Simulator::Run ();
	Simulator::Stop (Seconds(15.0));
	Simulator::Destroy ();
	NS_LOG_INFO ("Done.");
}