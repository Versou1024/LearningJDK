package sun.security.provider.certpath;

import java.security.cert.PolicyNode;
import java.security.cert.PolicyQualifierInfo;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

final class PolicyNodeImpl
  implements PolicyNode
{
  private static final String ANY_POLICY = "2.5.29.32.0";
  private PolicyNodeImpl mParent;
  private HashSet mChildren;
  private String mValidPolicy;
  private HashSet mQualifierSet;
  private boolean mCriticalityIndicator;
  private HashSet mExpectedPolicySet;
  private boolean mOriginalExpectedPolicySet;
  private int mDepth;
  private boolean isImmutable;

  PolicyNodeImpl(PolicyNodeImpl paramPolicyNodeImpl, String paramString, Set paramSet1, boolean paramBoolean1, Set paramSet2, boolean paramBoolean2)
  {
    this.isImmutable = false;
    this.mParent = paramPolicyNodeImpl;
    this.mChildren = new HashSet();
    if (paramString != null)
      this.mValidPolicy = paramString;
    else
      this.mValidPolicy = "";
    if (paramSet1 != null)
      this.mQualifierSet = new HashSet(paramSet1);
    else
      this.mQualifierSet = new HashSet();
    this.mCriticalityIndicator = paramBoolean1;
    if (paramSet2 != null)
      this.mExpectedPolicySet = new HashSet(paramSet2);
    else
      this.mExpectedPolicySet = new HashSet();
    this.mOriginalExpectedPolicySet = (!(paramBoolean2));
    if (this.mParent != null)
    {
      this.mDepth = (this.mParent.getDepth() + 1);
      this.mParent.addChild(this);
    }
    else
    {
      this.mDepth = 0;
    }
  }

  PolicyNodeImpl(PolicyNodeImpl paramPolicyNodeImpl1, PolicyNodeImpl paramPolicyNodeImpl2)
  {
    this(paramPolicyNodeImpl1, paramPolicyNodeImpl2.mValidPolicy, paramPolicyNodeImpl2.mQualifierSet, paramPolicyNodeImpl2.mCriticalityIndicator, paramPolicyNodeImpl2.mExpectedPolicySet, false);
  }

  public PolicyNode getParent()
  {
    return this.mParent;
  }

  public Iterator<PolicyNodeImpl> getChildren()
  {
    return Collections.unmodifiableSet(this.mChildren).iterator();
  }

  public int getDepth()
  {
    return this.mDepth;
  }

  public String getValidPolicy()
  {
    return this.mValidPolicy;
  }

  public Set<PolicyQualifierInfo> getPolicyQualifiers()
  {
    return Collections.unmodifiableSet(this.mQualifierSet);
  }

  public Set<String> getExpectedPolicies()
  {
    return Collections.unmodifiableSet(this.mExpectedPolicySet);
  }

  public boolean isCritical()
  {
    return this.mCriticalityIndicator;
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer(asString());
    Iterator localIterator = getChildren();
    while (localIterator.hasNext())
      localStringBuffer.append((PolicyNodeImpl)localIterator.next());
    return localStringBuffer.toString();
  }

  boolean isImmutable()
  {
    return this.isImmutable;
  }

  void setImmutable()
  {
    if (this.isImmutable)
      return;
    Iterator localIterator = this.mChildren.iterator();
    while (localIterator.hasNext())
    {
      PolicyNodeImpl localPolicyNodeImpl = (PolicyNodeImpl)localIterator.next();
      localPolicyNodeImpl.setImmutable();
    }
    this.isImmutable = true;
  }

  private void addChild(PolicyNodeImpl paramPolicyNodeImpl)
  {
    if (this.isImmutable)
      throw new IllegalStateException("PolicyNode is immutable");
    this.mChildren.add(paramPolicyNodeImpl);
  }

  void addExpectedPolicy(String paramString)
  {
    if (this.isImmutable)
      throw new IllegalStateException("PolicyNode is immutable");
    if (this.mOriginalExpectedPolicySet)
    {
      this.mExpectedPolicySet.clear();
      this.mOriginalExpectedPolicySet = false;
    }
    this.mExpectedPolicySet.add(paramString);
  }

  void prune(int paramInt)
  {
    if (this.isImmutable)
      throw new IllegalStateException("PolicyNode is immutable");
    if (this.mChildren.size() == 0)
      return;
    Iterator localIterator = this.mChildren.iterator();
    while (localIterator.hasNext())
    {
      PolicyNodeImpl localPolicyNodeImpl = (PolicyNodeImpl)localIterator.next();
      localPolicyNodeImpl.prune(paramInt);
      if ((localPolicyNodeImpl.mChildren.size() == 0) && (paramInt > this.mDepth + 1))
        localIterator.remove();
    }
  }

  void deleteChild(PolicyNode paramPolicyNode)
  {
    if (this.isImmutable)
      throw new IllegalStateException("PolicyNode is immutable");
    this.mChildren.remove(paramPolicyNode);
  }

  PolicyNodeImpl copyTree()
  {
    return copyTree(null);
  }

  private PolicyNodeImpl copyTree(PolicyNodeImpl paramPolicyNodeImpl)
  {
    PolicyNodeImpl localPolicyNodeImpl1 = new PolicyNodeImpl(paramPolicyNodeImpl, this);
    Iterator localIterator = this.mChildren.iterator();
    while (localIterator.hasNext())
    {
      PolicyNodeImpl localPolicyNodeImpl2 = (PolicyNodeImpl)localIterator.next();
      localPolicyNodeImpl2.copyTree(localPolicyNodeImpl1);
    }
    return localPolicyNodeImpl1;
  }

  Set getPolicyNodes(int paramInt)
  {
    HashSet localHashSet = new HashSet();
    getPolicyNodes(paramInt, localHashSet);
    return localHashSet;
  }

  private void getPolicyNodes(int paramInt, Set paramSet)
  {
    if (this.mDepth == paramInt)
    {
      paramSet.add(this);
    }
    else
    {
      Iterator localIterator = this.mChildren.iterator();
      while (localIterator.hasNext())
      {
        PolicyNodeImpl localPolicyNodeImpl = (PolicyNodeImpl)localIterator.next();
        localPolicyNodeImpl.getPolicyNodes(paramInt, paramSet);
      }
    }
  }

  Set getPolicyNodesExpected(int paramInt, String paramString, boolean paramBoolean)
  {
    if (paramString.equals("2.5.29.32.0"))
      return getPolicyNodes(paramInt);
    return getPolicyNodesExpectedHelper(paramInt, paramString, paramBoolean);
  }

  private Set getPolicyNodesExpectedHelper(int paramInt, String paramString, boolean paramBoolean)
  {
    HashSet localHashSet = new HashSet();
    if (this.mDepth < paramInt)
    {
      Iterator localIterator = this.mChildren.iterator();
      while (localIterator.hasNext())
      {
        PolicyNodeImpl localPolicyNodeImpl = (PolicyNodeImpl)localIterator.next();
        localHashSet.addAll(localPolicyNodeImpl.getPolicyNodesExpectedHelper(paramInt, paramString, paramBoolean));
      }
    }
    else if (paramBoolean)
    {
      if (this.mExpectedPolicySet.contains("2.5.29.32.0"))
        localHashSet.add(this);
    }
    else if (this.mExpectedPolicySet.contains(paramString))
    {
      localHashSet.add(this);
    }
    return localHashSet;
  }

  Set getPolicyNodesValid(int paramInt, String paramString)
  {
    HashSet localHashSet = new HashSet();
    if (this.mDepth < paramInt)
    {
      Iterator localIterator = this.mChildren.iterator();
      while (localIterator.hasNext())
      {
        PolicyNodeImpl localPolicyNodeImpl = (PolicyNodeImpl)localIterator.next();
        localHashSet.addAll(localPolicyNodeImpl.getPolicyNodesValid(paramInt, paramString));
      }
    }
    else if (this.mValidPolicy.equals(paramString))
    {
      localHashSet.add(this);
    }
    return localHashSet;
  }

  private static String policyToString(String paramString)
  {
    if (paramString.equals("2.5.29.32.0"))
      return "anyPolicy";
    return paramString;
  }

  String asString()
  {
    if (this.mParent == null)
      return "anyPolicy  ROOT\n";
    StringBuffer localStringBuffer = new StringBuffer();
    int i = 0;
    int j = getDepth();
    while (i < j)
    {
      localStringBuffer.append("  ");
      ++i;
    }
    localStringBuffer.append(policyToString(getValidPolicy()));
    localStringBuffer.append("  CRIT: ");
    localStringBuffer.append(isCritical());
    localStringBuffer.append("  EP: ");
    Iterator localIterator = getExpectedPolicies().iterator();
    while (localIterator.hasNext())
    {
      String str = (String)localIterator.next();
      localStringBuffer.append(policyToString(str));
      localStringBuffer.append(" ");
    }
    localStringBuffer.append(" (");
    localStringBuffer.append(getDepth());
    localStringBuffer.append(")\n");
    return localStringBuffer.toString();
  }
}