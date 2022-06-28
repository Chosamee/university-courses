#include "streaming-streamer.h"
#include "streaming-client.h"
#include "streaming-helper.h"
#include "ns3/uinteger.h"
#include "ns3/names.h"

namespace ns3 {

UdpRelEchoServerHelper::UdpRelEchoServerHelper (uint16_t port)
{
  m_factory.SetTypeId (UdpRelEchoServer::GetTypeId ());
  SetAttribute ("Port", UintegerValue (port));
}

void 
UdpRelEchoServerHelper::SetAttribute (
  std::string name, 
  const AttributeValue &value)
{
  m_factory.Set (name, value);
}

ApplicationContainer
UdpRelEchoServerHelper::Install (Ptr<Node> node) const
{
  return ApplicationContainer (InstallPriv (node));
}

ApplicationContainer
UdpRelEchoServerHelper::Install (std::string nodeName) const
{
  Ptr<Node> node = Names::Find<Node> (nodeName);
  return ApplicationContainer (InstallPriv (node));
}

ApplicationContainer
UdpRelEchoServerHelper::Install (NodeContainer c) const
{
  ApplicationContainer apps;
  for (NodeContainer::Iterator i = c.Begin (); i != c.End (); ++i)
    {
      apps.Add (InstallPriv (*i));
    }

  return apps;
}

Ptr<Application>
UdpRelEchoServerHelper::InstallPriv (Ptr<Node> node) const
{
  Ptr<Application> app = m_factory.Create<UdpRelEchoServer> ();
  node->AddApplication (app);

  return app;
}

UdpRelEchoClientHelper::UdpRelEchoClientHelper (Address address, uint16_t port)
{
  m_factory.SetTypeId (UdpRelEchoClient::GetTypeId ());
  SetAttribute ("RemoteAddress", AddressValue (address));
  SetAttribute ("RemotePort", UintegerValue (port));
}

UdpRelEchoClientHelper::UdpRelEchoClientHelper (Address address)
{
  m_factory.SetTypeId (UdpRelEchoClient::GetTypeId ());
  SetAttribute ("RemoteAddress", AddressValue (address));
}

void 
UdpRelEchoClientHelper::SetAttribute (
  std::string name, 
  const AttributeValue &value)
{
  m_factory.Set (name, value);
}

void
UdpRelEchoClientHelper::SetFill (Ptr<Application> app, std::string fill)
{
  app->GetObject<UdpRelEchoClient>()->SetFill (fill);
}

void
UdpRelEchoClientHelper::SetFill (Ptr<Application> app, uint8_t fill, uint32_t dataLength)
{
  app->GetObject<UdpRelEchoClient>()->SetFill (fill, dataLength);
}

void
UdpRelEchoClientHelper::SetFill (Ptr<Application> app, uint8_t *fill, uint32_t fillLength, uint32_t dataLength)
{
  app->GetObject<UdpRelEchoClient>()->SetFill (fill, fillLength, dataLength);
}

ApplicationContainer
UdpRelEchoClientHelper::Install (Ptr<Node> node) const
{
  return ApplicationContainer (InstallPriv (node));
}

ApplicationContainer
UdpRelEchoClientHelper::Install (std::string nodeName) const
{
  Ptr<Node> node = Names::Find<Node> (nodeName);
  return ApplicationContainer (InstallPriv (node));
}

ApplicationContainer
UdpRelEchoClientHelper::Install (NodeContainer c) const
{
  ApplicationContainer apps;
  for (NodeContainer::Iterator i = c.Begin (); i != c.End (); ++i)
    {
      apps.Add (InstallPriv (*i));
    }

  return apps;
}

Ptr<Application>
UdpRelEchoClientHelper::InstallPriv (Ptr<Node> node) const
{
  Ptr<Application> app = m_factory.Create<UdpRelEchoClient> ();
  node->AddApplication (app);

  return app;
}

} // namespace ns3
