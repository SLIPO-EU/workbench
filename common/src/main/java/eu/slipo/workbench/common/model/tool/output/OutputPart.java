package eu.slipo.workbench.common.model.tool.output;

import org.springframework.util.Assert;

import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.tool.AnyTool;

public interface OutputPart <T extends AnyTool>
{
    String key();

    EnumOutputType outputType();
    
    EnumTool tool();

    static <Y extends AnyTool, E extends Enum<E> & OutputPart<Y>> E fromKey(
        String key, Class<E> enumeration)
    {
        Assert.notNull(key, "A key is required");
        Assert.notNull(enumeration, "The enumeration class is required");

        for (E e: enumeration.getEnumConstants())
            if (key.equals(e.key()))
                return e;
        return null;
    }
}