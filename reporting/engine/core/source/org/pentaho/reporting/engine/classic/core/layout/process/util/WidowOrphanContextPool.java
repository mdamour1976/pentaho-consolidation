package org.pentaho.reporting.engine.classic.core.layout.process.util;

import org.pentaho.reporting.engine.classic.core.layout.model.LayoutNodeTypes;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.context.StaticBoxLayoutProperties;

public class WidowOrphanContextPool
{
  private static class BlockContextPool extends StackedObjectPool<BlockWidowOrphanContext>
  {
    private BlockContextPool()
    {
    }

    protected BlockWidowOrphanContext create()
    {
      return new BlockWidowOrphanContext();
    }
  }

  private static class CanvasContextPool extends StackedObjectPool<CanvasWidowOrphanContext>
  {
    private CanvasContextPool()
    {
    }

    protected CanvasWidowOrphanContext create()
    {
      return new CanvasWidowOrphanContext();
    }
  }

  private CanvasContextPool canvasContextPool;
  private BlockContextPool blockContextPool;

  public WidowOrphanContextPool()
  {
    canvasContextPool = new CanvasContextPool();
    blockContextPool = new BlockContextPool();
  }

  public WidowOrphanContext create(final RenderBox box,
                                   final WidowOrphanContext context)
  {
    if ((box.getLayoutNodeType() & LayoutNodeTypes.MASK_BOX_BLOCK) == LayoutNodeTypes.MASK_BOX_BLOCK)
    {
      final StaticBoxLayoutProperties properties = box.getStaticBoxLayoutProperties();
      final int widows = properties.getWidows();
      final int orphans = properties.getOrphans();
      final BlockWidowOrphanContext retval = blockContextPool.get();
      retval.init(blockContextPool, context, box, widows, orphans);
      return retval;
    }

    if ((box.getLayoutNodeType() & LayoutNodeTypes.MASK_BOX_ROW) == LayoutNodeTypes.MASK_BOX_ROW)
    {
      // todo: Make this a row-context later .. (Not needed for 3.9)
      // return new CanvasWidowOrphanContext(context);
    }

    final CanvasWidowOrphanContext retval = canvasContextPool.get();
    retval.init(canvasContextPool, context);
    return retval;
  }

  public void free (final WidowOrphanContext context)
  {
    context.clearForPooledReuse();
  }
}
