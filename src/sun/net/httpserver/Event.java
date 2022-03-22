package sun.net.httpserver;

class Event
{
  ExchangeImpl exchange;

  protected Event(ExchangeImpl paramExchangeImpl)
  {
    this.exchange = paramExchangeImpl;
  }
}