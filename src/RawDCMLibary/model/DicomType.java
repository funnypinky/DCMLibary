package RawDCMLibary.model;

public interface DicomType {

    public Object[] read(byte[] data);
    
    public byte[] write(Object obj);
}
