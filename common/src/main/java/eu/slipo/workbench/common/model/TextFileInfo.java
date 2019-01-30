package eu.slipo.workbench.common.model;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;

public class TextFileInfo extends FileInfo
{
    private static final long serialVersionUID = 1L;

    private String encoding;
    
    private Long lineCount;
    
    public TextFileInfo(String name, String path, long size, long modifiedOn)
    {
        super(name, path, size, modifiedOn);
    }

    public TextFileInfo(String name, String path, long size, ZonedDateTime modifiedOn)
    {
        super(name, path, size, modifiedOn);
    }
    
    public TextFileInfo(String name, String path, BasicFileAttributes attrs)
    {
        super(name, path, attrs);
    }
    
    public TextFileInfo(Path path, long size, long modifiedOn)
    {
        super(path.getFileName().toString(), path.toString(), size, modifiedOn);
    }
    
    public TextFileInfo(Path path, BasicFileAttributes attrs)
    {
        super(path.getFileName().toString(), path.toString(), attrs);
    }
    
    public void setEncoding(Charset encoding)
    {
        this.encoding = encoding.name();
    }
    
    public Charset getEncoding()
    {
        return Charset.forName(encoding);
    }
    
    public void setLineCount(Long lineCount)
    {
        this.lineCount = lineCount;
    }
    
    public Long getLineCount()
    {
        return lineCount;
    }
}
