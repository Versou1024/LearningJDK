package sun.reflect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class Label
{
  private List patches = new ArrayList();

  void add(ClassFileAssembler paramClassFileAssembler, short paramShort1, short paramShort2, int paramInt)
  {
    this.patches.add(new PatchInfo(paramClassFileAssembler, paramShort1, paramShort2, paramInt));
  }

  public void bind()
  {
    Iterator localIterator = this.patches.iterator();
    while (localIterator.hasNext())
    {
      PatchInfo localPatchInfo = (PatchInfo)localIterator.next();
      int i = localPatchInfo.asm.getLength();
      short s = (short)(i - localPatchInfo.instrBCI);
      localPatchInfo.asm.emitShort(localPatchInfo.patchBCI, s);
      localPatchInfo.asm.setStack(localPatchInfo.stackDepth);
    }
  }

  static class PatchInfo
  {
    ClassFileAssembler asm;
    short instrBCI;
    short patchBCI;
    int stackDepth;

    PatchInfo(ClassFileAssembler paramClassFileAssembler, short paramShort1, short paramShort2, int paramInt)
    {
      this.asm = paramClassFileAssembler;
      this.instrBCI = paramShort1;
      this.patchBCI = paramShort2;
      this.stackDepth = paramInt;
    }
  }
}