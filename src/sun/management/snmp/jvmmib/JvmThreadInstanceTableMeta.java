package sun.management.snmp.jvmmib;

import com.sun.jmx.snmp.SnmpOid;
import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.agent.SnmpMib;
import com.sun.jmx.snmp.agent.SnmpMibSubRequest;
import com.sun.jmx.snmp.agent.SnmpMibTable;
import com.sun.jmx.snmp.agent.SnmpStandardObjectServer;
import com.sun.jmx.snmp.agent.SnmpTableEntryFactory;
import java.io.Serializable;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class JvmThreadInstanceTableMeta extends SnmpMibTable
  implements Serializable
{
  private JvmThreadInstanceEntryMeta node;
  protected SnmpStandardObjectServer objectserver;

  public JvmThreadInstanceTableMeta(SnmpMib paramSnmpMib, SnmpStandardObjectServer paramSnmpStandardObjectServer)
  {
    super(paramSnmpMib);
    this.objectserver = paramSnmpStandardObjectServer;
  }

  protected JvmThreadInstanceEntryMeta createJvmThreadInstanceEntryMetaNode(String paramString1, String paramString2, SnmpMib paramSnmpMib, MBeanServer paramMBeanServer)
  {
    return new JvmThreadInstanceEntryMeta(paramSnmpMib, this.objectserver);
  }

  public void createNewEntry(SnmpMibSubRequest paramSnmpMibSubRequest, SnmpOid paramSnmpOid, int paramInt)
    throws SnmpStatusException
  {
    if (this.factory != null)
      this.factory.createNewEntry(paramSnmpMibSubRequest, paramSnmpOid, paramInt, this);
    else
      throw new SnmpStatusException(6);
  }

  public boolean isRegistrationRequired()
  {
    return false;
  }

  public void registerEntryNode(SnmpMib paramSnmpMib, MBeanServer paramMBeanServer)
  {
    this.node = createJvmThreadInstanceEntryMetaNode("JvmThreadInstanceEntry", "JvmThreadInstanceTable", paramSnmpMib, paramMBeanServer);
  }

  public synchronized void addEntry(SnmpOid paramSnmpOid, ObjectName paramObjectName, Object paramObject)
    throws SnmpStatusException
  {
    if (!(paramObject instanceof JvmThreadInstanceEntryMBean))
      throw new ClassCastException("Entries for Table \"JvmThreadInstanceTable\" must implement the \"JvmThreadInstanceEntryMBean\" interface.");
    super.addEntry(paramSnmpOid, paramObjectName, paramObject);
  }

  public void get(SnmpMibSubRequest paramSnmpMibSubRequest, SnmpOid paramSnmpOid, int paramInt)
    throws SnmpStatusException
  {
    JvmThreadInstanceEntryMBean localJvmThreadInstanceEntryMBean = (JvmThreadInstanceEntryMBean)getEntry(paramSnmpOid);
    synchronized (this)
    {
      this.node.setInstance(localJvmThreadInstanceEntryMBean);
      this.node.get(paramSnmpMibSubRequest, paramInt);
    }
  }

  public void set(SnmpMibSubRequest paramSnmpMibSubRequest, SnmpOid paramSnmpOid, int paramInt)
    throws SnmpStatusException
  {
    if (paramSnmpMibSubRequest.getSize() == 0)
      return;
    JvmThreadInstanceEntryMBean localJvmThreadInstanceEntryMBean = (JvmThreadInstanceEntryMBean)getEntry(paramSnmpOid);
    synchronized (this)
    {
      this.node.setInstance(localJvmThreadInstanceEntryMBean);
      this.node.set(paramSnmpMibSubRequest, paramInt);
    }
  }

  public void check(SnmpMibSubRequest paramSnmpMibSubRequest, SnmpOid paramSnmpOid, int paramInt)
    throws SnmpStatusException
  {
    if (paramSnmpMibSubRequest.getSize() == 0)
      return;
    JvmThreadInstanceEntryMBean localJvmThreadInstanceEntryMBean = (JvmThreadInstanceEntryMBean)getEntry(paramSnmpOid);
    synchronized (this)
    {
      this.node.setInstance(localJvmThreadInstanceEntryMBean);
      this.node.check(paramSnmpMibSubRequest, paramInt);
    }
  }

  public void validateVarEntryId(SnmpOid paramSnmpOid, long paramLong, Object paramObject)
    throws SnmpStatusException
  {
    this.node.validateVarId(paramLong, paramObject);
  }

  public boolean isReadableEntryId(SnmpOid paramSnmpOid, long paramLong, Object paramObject)
    throws SnmpStatusException
  {
    return this.node.isReadable(paramLong);
  }

  public long getNextVarEntryId(SnmpOid paramSnmpOid, long paramLong, Object paramObject)
    throws SnmpStatusException
  {
    long l = this.node.getNextVarId(paramLong, paramObject);
    while (!(isReadableEntryId(paramSnmpOid, l, paramObject)))
      l = this.node.getNextVarId(l, paramObject);
    return l;
  }

  public boolean skipEntryVariable(SnmpOid paramSnmpOid, long paramLong, Object paramObject, int paramInt)
  {
    JvmThreadInstanceEntryMBean localJvmThreadInstanceEntryMBean;
    try
    {
      localJvmThreadInstanceEntryMBean = (JvmThreadInstanceEntryMBean)getEntry(paramSnmpOid);
      synchronized (this)
      {
        this.node.setInstance(localJvmThreadInstanceEntryMBean);
        return this.node.skipVariable(paramLong, paramObject, paramInt);
      }
    }
    catch (SnmpStatusException localSnmpStatusException)
    {
    }
    return false;
  }
}