package RawDCMLibary.model;

import java.io.*;

import java.util.*;
import java.util.zip.*;
import RawDCMLibary.DICOM.DICOMFile;
import static RawDCMLibary.DICOM.DICOMFile.ARGB;
import static RawDCMLibary.DICOM.DICOMFile.ARGB_FLOAT;
import static RawDCMLibary.DICOM.DICOMFile.ARGB_USHORT;
import static RawDCMLibary.DICOM.DICOMFile.BOOLEAN;
import static RawDCMLibary.DICOM.DICOMFile.BYTE;
import static RawDCMLibary.DICOM.DICOMFile.DOUBLE;
import static RawDCMLibary.DICOM.DICOMFile.FLOAT;
import static RawDCMLibary.DICOM.DICOMFile.INTEGER;
import static RawDCMLibary.DICOM.DICOMFile.LONG;
import static RawDCMLibary.DICOM.DICOMFile.SHORT;
import static RawDCMLibary.DICOM.DICOMFile.UBYTE;
import static RawDCMLibary.DICOM.DICOMFile.UINTEGER;
import static RawDCMLibary.DICOM.DICOMFile.USHORT;


/**
 * The class reads and writes raw files of all data types: boolean, byte, short, int, long, float, and double. For the
 * read process an offset can be passed as a parameter to identify the location in the file where the data starts. A
 * number of file formats while not "raw" have data at a specific location after a fixed length but unknown header and
 * therefore can be treated as raw.
 *
 * @version  0.1 Sept 2, 1997
 * @author   Matthew J. McAuliffe, Ph.D.
 * @see      FileRaw
 */
public class FileRawChunk extends FileBase {

    //~ Static fields/initializers -------------------------------------------------------------------------------------

    /** DOCUMENT ME! */
    public static final int RGB = 0;

    /** DOCUMENT ME! */
    public static final int RRRGGGBBB = 1;

    //~ Instance fields ------------------------------------------------------------------------------------------------

    /** DOCUMENT ME! */
    private BitSet bufferBitSet = null;

    /** DOCUMENT ME! */
    private byte[] bufferByte = null;

    /** DOCUMENT ME! */
    private double[] bufferDouble = null;

    /** DOCUMENT ME! */
    private float[] bufferFloat = null;

    /** DOCUMENT ME! */
    private int[] bufferInt = null;

    /** DOCUMENT ME! */
    private long[] bufferLong = null;

    /** DOCUMENT ME! */
    private short[] bufferShort = null;

    /** DOCUMENT ME! */
    private int bufferSize = 0;

    /** DOCUMENT ME! */
    private int compressionType = FileInfoBase.COMPRESSION_NONE;

    /** DOCUMENT ME! */
    private DeflaterOutputStream deflaterStream;

    /** DOCUMENT ME! */
    private DICOMFile fileInfo;

    /** DOCUMENT ME! */
    private InflaterInputStream inflaterStream;

    @SuppressWarnings("unused")
    private int numColors = 3;

    /** DOCUMENT ME! */
    private int planarConfig = 0; // 0 = rgb,rgb, 1 = rrr, ggg, bbb

    @SuppressWarnings("unused")
    private boolean RGBAOrder = false;

    /** DOCUMENT ME! */
    private int type = 0;
    
    // Default of 63 and 6 for 8 byte units.  For Analyze FileAnalyze sets to 7 and 3
    // for byte units.
    /** Used in reading and writing boolean */
    private int minimumBitsMinus1 = 63;
    
    /** Used in reading and writing boolean */
    private int shiftToDivide = 6;

    //~ Constructors ---------------------------------------------------------------------------------------------------

    /**
     * Raw reader/writer constructor.
     *
     * @param  file   random access file pointer
     * @param  fInfo  information that describes the image
     */
    public FileRawChunk(RandomAccessFile file, DICOMFile fInfo) {
        setImageFile(file, fInfo);
    }

    /**
     * Compressed raw reader/writer constructor.
     *
     * @param  fileName  the name of the file to read/write
     * @param  fInfo     information that describes the image
     * @param  rwFlag    whether we are reading or writing the file (READ or WRITE)
     * @param  compress  the compression method to use (should not be none)
     */
    public FileRawChunk(String fileName, DICOMFile fInfo, int rwFlag, int compress) {
        this.fileInfo = fInfo;
        this.compressionType = compress;

        if (rwFlag == READ) {

            try {

                if (compressionType == FileInfoBase.COMPRESSION_ZIP) {
                    inflaterStream = new ZipInputStream(new FileInputStream(fileName));
                    ((ZipInputStream) inflaterStream).getNextEntry();
                } else if (compressionType == FileInfoBase.COMPRESSION_GZIP) {
                    inflaterStream = new GZIPInputStream(new FileInputStream(fileName));
                } 
            } catch (FileNotFoundException fex) {
                System.err.println("zip file does not already exist");
            } catch (IOException ioex) {
                System.err.println("unable to seek to first zip entry");
            }
        } else {

            try {

                if (compressionType == FileInfoBase.COMPRESSION_ZIP) {
                    deflaterStream = new ZipOutputStream(new FileOutputStream(fileName));

                    String zipEntryName = fileName.substring(fileName.lastIndexOf(File.separator), fileName.length());
                    ((ZipOutputStream) deflaterStream).putNextEntry(new ZipEntry(zipEntryName));
                } else if (compressionType == FileInfoBase.COMPRESSION_GZIP) {
                    deflaterStream = new GZIPOutputStream(new FileOutputStream(fileName));
                } 
            } catch (IOException ioex) {
                System.err.println("exception");
                ioex.printStackTrace();
            }
        }
    }

    //~ Methods --------------------------------------------------------------------------------------------------------

    /**
     * Closes the file.
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public final void close() throws IOException {

        if (raFile != null) {
            raFile.close();
        }
    }

    /**
     * Prepares this class for cleanup.
     */
    public void finalize() {
        bufferBitSet = null;
        bufferByte = null;
        bufferShort = null;
        bufferInt = null;
        bufferLong = null;
        bufferFloat = null;
        bufferDouble = null;
        fileInfo = null;

        if (inflaterStream != null) {

            try {
                inflaterStream.close();
            } catch (Exception ex) { }
        }

        if (deflaterStream != null) {

            try {
                deflaterStream.close();
            } catch (Exception ex) { }
        }

        super.finalize();
    }
    
    /**
     * Used in reading and writing boolean
     * @param minimumBitsMinus1
     */
    public void setMinimumBitsMinus1(int minimumBitsMinus1) {
        this.minimumBitsMinus1 = minimumBitsMinus1;
    }
    
    /**
     * Used in reading and writing boolean
     * @param shiftToDivide
     */
    public void setShiftToDivide(int shiftToDivide) {
        this.shiftToDivide = shiftToDivide;
    }

    /**
     * Gets the Bitset buffer (binary image).
     *
     * @return  the buffer
     */
    public BitSet getBitSetBuffer() {
        return bufferBitSet;
    }

    /**
     * Gets the byte buffer (image).
     *
     * @return  the buffer
     */
    public byte[] getByteBuffer() {
        return bufferByte;
    }

    /**
     * Gets the double buffer (image).
     *
     * @return  the buffer
     */
    public double[] getDoubleBuffer() {
        return bufferDouble;
    }

    /**
     * Gets the float buffer (image).
     *
     * @return  the buffer
     */
    public float[] getFloatBuffer() {
        return bufferFloat;
    }

    /**
     * gGets the integer buffer (image).
     *
     * @return  the buffer
     */
    public int[] getIntBuffer() {
        return bufferInt;
    }

    /**
     * Gets the long buffer (image).
     *
     * @return  the buffer
     */
    public long[] getLongBuffer() {
        return bufferLong;
    }

    /**
     * Gets the short buffer (image).
     *
     * @return  the buffer
     */
    public short[] getShortBuffer() {
        return bufferShort;
    }

    /**
     * This method reads a raw chunk from a file.
     *
     * @param      type    type of data that is to be read
     * @param      start   points to where the data of the image is located.
     * @param      length  number of pixels to be read
     *
     * @exception  IOException  if there is an error reading the file
     */
    public void readImage(int type, long start, int length) throws IOException {
        int i;
        int index;
        int b1 = 0, b2 = 0, b3 = 0, b4 = 0, b5 = 0, b6 = 0, b7 = 0, b8 = 0;
        long b1L = 0, b2L = 0, b3L = 0, b4L = 0, b5L = 0, b6L = 0, b7L = 0, b8L = 0;
        boolean endianess = fileInfo.isEndianess();
        long temp = raFile.length();
        if (type == BOOLEAN) {
            if (compressionType == FileInfoBase.COMPRESSION_NONE) {
                if ((minimumBitsMinus1 == 7) && (shiftToDivide == 3)) {
                		if ((start + ((length + minimumBitsMinus1) >> shiftToDivide)) > raFile.length()) {
                	
                	        throw new IOException("End bound exceeds EOF");
                		}
                }
                else if   ((start + (8 * ((length + minimumBitsMinus1) >> shiftToDivide))) > raFile.length()) {
                    throw new IOException("End bound exceeds EOF");
                }
            }
        } else {

            if ((compressionType == FileInfoBase.COMPRESSION_NONE) && ((start + length) > raFile.length())) {
                throw new IOException("End bound exceeds EOF");
            }
        }

        try {

            if ((length != bufferSize) || (type != this.type)) {
                bufferSize = length;
                this.type = type;

                switch (type) {

                    case BOOLEAN:

                        // new BitSet(size) = new long[(size+63)>>6]
                        bufferBitSet = new BitSet(bufferSize);
                        if ((minimumBitsMinus1 == 7) && (shiftToDivide == 3)) {
                        	bufferByte = new byte[(bufferSize + minimumBitsMinus1) >> shiftToDivide];
                        }
                        else {
                            bufferByte = new byte[8 * ((bufferSize + minimumBitsMinus1) >> shiftToDivide)];
                        }
                        break;

                    case BYTE:
                        bufferByte = new byte[bufferSize];
                        break;

                    case UBYTE:
                        bufferShort = new short[bufferSize];

                        // bufferUByte = new byte[bufferSize];
                        bufferByte = new byte[bufferSize];
                        break;

                    case SHORT:
                        bufferShort = new short[bufferSize];
                        bufferByte = new byte[2 * bufferSize];
                        break;

                    case USHORT:
                        bufferShort = new short[bufferSize];
                        bufferByte = new byte[2 * bufferSize];
                        break;

                    case INTEGER:
                        bufferInt = new int[bufferSize];
                        bufferByte = new byte[4 * bufferSize];
                        break;

                    case UINTEGER:
                        bufferInt = new int[bufferSize];
                        bufferByte = new byte[4 * bufferSize];
                        break;

                    case LONG:
                        bufferLong = new long[bufferSize];
                        bufferByte = new byte[8 * bufferSize];
                        break;

                    case FLOAT:
                        bufferFloat = new float[bufferSize];
                        bufferByte = new byte[4 * bufferSize];
                        break;

                    case DOUBLE:
                        bufferDouble = new double[bufferSize];
                        bufferByte = new byte[8 * bufferSize];
                        break;

                    case ARGB:
                        bufferByte = new byte[bufferSize];
                        break;

                    case ARGB_USHORT:
                        bufferShort = new short[bufferSize];
                        bufferByte = new byte[2 * bufferSize];
                        break;
                        
                    case ARGB_FLOAT:
                        bufferFloat = new float[bufferSize];
                        bufferByte = new byte[4 * bufferSize];
                        break;

                    default:
                        throw new IOException("FileRawChunk: Unsupported data type");
                }
            }
        } catch (OutOfMemoryError error) {
            bufferBitSet = null;
            bufferByte = null;
            bufferShort = null;
            bufferInt = null;
            bufferLong = null;
            bufferFloat = null;
            bufferDouble = null;
            System.gc();
            throw error;
        }

        if (compressionType == FileInfoBase.COMPRESSION_NONE) {
            raFile.seek(start);
        }

        switch (type) {

            case BOOLEAN:
                try {

                    if (compressionType == FileInfoBase.COMPRESSION_NONE) {
                        raFile.read(bufferByte);
                    } else {

                        try {
                            int max = bufferByte.length;
                            int counter = 0;
                            int len = max;
                            int tempVal = 0;

                            while (counter < max) {
                                tempVal = inflaterStream.read(bufferByte, counter, len);

                                if (tempVal == -1) {
                                    break;
                                }

                                counter += tempVal;
                                len = (max - counter);
                            }
                        } catch (EOFException eofex) {
                            break;
                        }
                    }

                    for (i = 0; i < bufferSize; i++) {

                        if ((bufferByte[i >> 3] & (1 << (7-(i % 8)))) != 0) {
                            bufferBitSet.set(i);
                        } else {
                            bufferBitSet.clear(i);
                        }
                    }
                } catch (IOException error) {
                    throw error;
                }

                break;

            case BYTE:
                try {

                    if (compressionType == FileInfoBase.COMPRESSION_NONE) {
                        raFile.read(bufferByte);
                    } else {

                        try {
                            int max = bufferByte.length;
                            int counter = 0;
                            int len = max;
                            int tempVal = 0;

                            while (counter < max) {
                                tempVal = inflaterStream.read(bufferByte, counter, len);

                                if (tempVal == -1) {
                                    break;
                                }

                                counter += tempVal;
                                len = (max - counter);
                            }
                        } catch (EOFException eofex) {
                            break;
                        }
                    }
                } catch (IOException error) {
                    throw error;
                }

                break;

            case UBYTE:
                try {

                    if (compressionType == FileInfoBase.COMPRESSION_NONE) {
                        raFile.read(bufferByte);
                    } else {

                        try {
                            int max = bufferByte.length;
                            int counter = 0;
                            int len = max;
                            int tempVal = 0;

                            while (counter < max) {
                                tempVal = inflaterStream.read(bufferByte, counter, len);

                                if (tempVal == -1) {
                                    break;
                                }

                                counter += tempVal;
                                len = (max - counter);
                            }
                        } catch (EOFException eofex) {
                            break;
                        }
                    }

                    for (i = 0; i < bufferSize; i++) {
                        bufferShort[i] = (short) (bufferByte[i] & 0xff);
                    }
                } catch (IOException error) {
                    throw error;
                }

                break;

            case SHORT:
                try {

                    if (compressionType == FileInfoBase.COMPRESSION_NONE) {
                        raFile.read(bufferByte);
                    } else {

                        try {
                            int max = bufferByte.length;
                            int counter = 0;
                            int len = max;
                            int tempVal = 0;

                            while (counter < max) {
                                tempVal = inflaterStream.read(bufferByte, counter, len);

                                if (tempVal == -1) {
                                    break;
                                }

                                counter += tempVal;
                                len = (max - counter);
                            }
                        } catch (EOFException eofex) {
                            break;
                        }
                    }

                    if (endianess == BIG_ENDIAN) {

                        for (i = 0, index = 0; i < bufferSize; i++, index += 2) {

                            // index = i*2;
                            // b1 = getUnsignedByte(bufferByte, index);
                            // b2 = getUnsignedByte(bufferByte, index+1);
                            // bufferShort[i] = (short)((getUnsignedByte(bufferByte, index) << 8) +
                            // getUnsignedByte(bufferByte, index+1));
                            // OR
                            // bufferShort[i] = getBufferShort(bufferByte, index, endianess);
                            // OR
                            // Fastest
                            bufferShort[i] = (short) (((bufferByte[index] & 0x00ff) << 8) |
                                                          (bufferByte[index + 1] & 0x00ff));
                            // System.out.println ("here rawchunck.readimagebig = " + bufferShort[i]);
                        }
                    } else {

                        for (i = 0, index = 0; i < bufferSize; i++, index += 2) {
                            bufferShort[i] = (short) (((bufferByte[index + 1] & 0x00ff) << 8) |
                                                          (bufferByte[index] & 0x00ff));
                            // System.out.println ("here rawchunck.readimagelittle = " + bufferShort[i]);
                        }
                    }
                } catch (IOException error) {
                    throw error;
                }

                break;

            case USHORT:
                try {

                    if (compressionType == FileInfoBase.COMPRESSION_NONE) {
                        raFile.read(bufferByte);
                    } else {

                        try {
                            int max = bufferByte.length;
                            int counter = 0;
                            int len = max;
                            int tempVal = 0;

                            while (counter < max) {
                                tempVal = inflaterStream.read(bufferByte, counter, len);

                                if (tempVal == -1) {
                                    break;
                                }

                                counter += tempVal;
                                len = (max - counter);
                            }
                        } catch (EOFException eofex) {
                            break;
                        }
                    }

                    if (endianess == BIG_ENDIAN) {

                        for (i = 0, index = 0; i < bufferSize; i++) {
                            b1 = bufferByte[index++] & 0xff;
                            b2 = bufferByte[index++] & 0xff;
                            bufferShort[i] = (short) ((b1 << 8) | b2);
                        }
                    } else {

                        for (i = 0, index = 0; i < bufferSize; i++) {
                            b1 = bufferByte[index++] & 0xff;
                            b2 = bufferByte[index++] & 0xff;
                            bufferShort[i] = (short) ((b2 << 8) | b1);
                        }
                    }
                } catch (IOException error) {
                    throw error;
                }

                break;

            case INTEGER:
            case UINTEGER:
                try {

                    if (compressionType == FileInfoBase.COMPRESSION_NONE) {
                        raFile.read(bufferByte);
                    } else {

                        try {
                            int max = bufferByte.length;
                            int counter = 0;
                            int len = max;
                            int tempVal = 0;

                            while (counter < max) {
                                tempVal = inflaterStream.read(bufferByte, counter, len);

                                if (tempVal == -1) {
                                    break;
                                }

                                counter += tempVal;
                                len = (max - counter);
                            }
                        } catch (EOFException eofex) {
                            break;
                        }
                    }

                    if (endianess == BIG_ENDIAN) {

                        for (i = 0, index = 0; i < bufferSize; i++) {
                            b1 = bufferByte[index++] & 0x000000ff;
                            b2 = bufferByte[index++] & 0x000000ff;
                            b3 = bufferByte[index++] & 0x000000ff;
                            b4 = bufferByte[index++] & 0x000000ff;
                            bufferInt[i] = ((b1 << 24) | (b2 << 16) | (b3 << 8) | b4);
                        }
                    } else {

                        for (i = 0, index = 0; i < bufferSize; i++) {
                            b1 = bufferByte[index++] & 0x000000ff;
                            b2 = bufferByte[index++] & 0x000000ff;
                            b3 = bufferByte[index++] & 0x000000ff;
                            b4 = bufferByte[index++] & 0x000000ff;
                            bufferInt[i] = ((b4 << 24) | (b3 << 16) | (b2 << 8) | b1);
                        }
                    }
                } catch (IOException error) {
                    throw error;
                }

                break;

            case LONG:
                try {

                    if (compressionType == FileInfoBase.COMPRESSION_NONE) {
                        raFile.read(bufferByte);
                    } else {

                        try {
                            int max = bufferByte.length;
                            int counter = 0;
                            int len = max;
                            int tempVal = 0;

                            while (counter < max) {
                                tempVal = inflaterStream.read(bufferByte, counter, len);

                                if (tempVal == -1) {
                                    break;
                                }

                                counter += tempVal;
                                len = (max - counter);
                            }
                        } catch (EOFException eofex) {
                            break;
                        }
                    }

                    if (endianess == BIG_ENDIAN) {

                        for (i = 0, index = 0; i < bufferSize; i++) {
                            b1L = bufferByte[index++] & 0xffL;
                            b2L = bufferByte[index++] & 0xffL;
                            b3L = bufferByte[index++] & 0xffL;
                            b4L = bufferByte[index++] & 0xffL;
                            b5L = bufferByte[index++] & 0xffL;
                            b6L = bufferByte[index++] & 0xffL;
                            b7L = bufferByte[index++] & 0xffL;
                            b8L = bufferByte[index++] & 0xffL;
                            bufferLong[i] = ((b1L << 56) | (b2L << 48) | (b3L << 40) | (b4L << 32) | (b5L << 24) |
                                                 (b6L << 16) | (b7L << 8) | b8L);
                        }
                    } else {

                        for (i = 0, index = 0; i < bufferSize; i++) {
                            b1L = bufferByte[index++] & 0xffL;
                            b2L = bufferByte[index++] & 0xffL;
                            b3L = bufferByte[index++] & 0xffL;
                            b4L = bufferByte[index++] & 0xffL;
                            b5L = bufferByte[index++] & 0xffL;
                            b6L = bufferByte[index++] & 0xffL;
                            b7L = bufferByte[index++] & 0xffL;
                            b8L = bufferByte[index++] & 0xffL;
                            bufferLong[i] = ((b8L << 56) | (b7L << 48) | (b6L << 40) | (b5L << 32) | (b4L << 24) |
                                                 (b3L << 16) | (b2L << 8) | b1L);
                        }
                    }
                } catch (IOException error) {
                    throw error;
                }

                break;

            case FLOAT:
                try {

                    if (compressionType == FileInfoBase.COMPRESSION_NONE) {
                        raFile.read(bufferByte);
                    } else {

                        try {
                            int max = bufferByte.length;
                            int counter = 0;
                            int len = max;
                            int tempVal = 0;

                            while (counter < max) {
                                tempVal = inflaterStream.read(bufferByte, counter, len);

                                if (tempVal == -1) {
                                    break;
                                }

                                counter += tempVal;
                                len = (max - counter);
                            }
                        } catch (EOFException eofex) {
                            break;
                        }
                    }

                    // Float tmpFloat = new Float(0);
                    int tmpInt;

                    if (endianess == BIG_ENDIAN) {

                        for (i = 0, index = 0; i < bufferSize; i++) {
                            b1 = bufferByte[index++] & 0xff;
                            b2 = bufferByte[index++] & 0xff;
                            b3 = bufferByte[index++] & 0xff;
                            b4 = bufferByte[index++] & 0xff;
                            tmpInt = ((b1 << 24) | (b2 << 16) | (b3 << 8) | b4);

                            // bufferFloat[i] = tmpFloat.intBitsToFloat(tmpInt);
                            bufferFloat[i] = Float.intBitsToFloat(tmpInt);
                        }
                    } else {

                        for (i = 0, index = 0; i < bufferSize; i++) {
                            b1 = bufferByte[index++] & 0xff;
                            b2 = bufferByte[index++] & 0xff;
                            b3 = bufferByte[index++] & 0xff;
                            b4 = bufferByte[index++] & 0xff;
                            tmpInt = ((b4 << 24) | (b3 << 16) | (b2 << 8) | b1);

                            // bufferFloat[i] = tmpFloat.intBitsToFloat(tmpInt);
                            bufferFloat[i] = Float.intBitsToFloat(tmpInt);
                        }
                    }
                } catch (IOException error) {
                    throw error;
                }

                break;

            case DOUBLE:
                try {

                    if (compressionType == FileInfoBase.COMPRESSION_NONE) {
                        raFile.read(bufferByte);
                    } else {

                        try {
                            int max = bufferByte.length;
                            int counter = 0;
                            int len = max;
                            int tempVal = 0;

                            while (counter < max) {
                                tempVal = inflaterStream.read(bufferByte, counter, len);

                                if (tempVal == -1) {
                                    break;
                                }

                                counter += tempVal;
                                len = (max - counter);
                            }
                        } catch (EOFException eofex) {
                            break;
                        }
                    }

                    // Double tmpDouble = new Double(0);
                    long tmpLong;

                    if (endianess == BIG_ENDIAN) {

                        for (i = 0, index = 0; i < bufferSize; i++) {
                            b1 = bufferByte[index++] & 0xff;
                            b2 = bufferByte[index++] & 0xff;
                            b3 = bufferByte[index++] & 0xff;
                            b4 = bufferByte[index++] & 0xff;
                            b5 = bufferByte[index++] & 0xff;
                            b6 = bufferByte[index++] & 0xff;
                            b7 = bufferByte[index++] & 0xff;
                            b8 = bufferByte[index++] & 0xff;
                            tmpLong = (((long) b1 << 56) | ((long) b2 << 48) | ((long) b3 << 40) | ((long) b4 << 32) |
                                           ((long) b5 << 24) | ((long) b6 << 16) | ((long) b7 << 8) | (long) b8);

                            // bufferDouble[i] = tmpDouble.longBitsToDouble(tmpLong);
                            bufferDouble[i] = Double.longBitsToDouble(tmpLong);
                        }
                    } else {

                        for (i = 0, index = 0; i < bufferSize; i++) {
                            b1 = bufferByte[index++] & 0xff;
                            b2 = bufferByte[index++] & 0xff;
                            b3 = bufferByte[index++] & 0xff;
                            b4 = bufferByte[index++] & 0xff;
                            b5 = bufferByte[index++] & 0xff;
                            b6 = bufferByte[index++] & 0xff;
                            b7 = bufferByte[index++] & 0xff;
                            b8 = bufferByte[index++] & 0xff;
                            tmpLong = (((long) b8 << 56) | ((long) b7 << 48) | ((long) b6 << 40) | ((long) b5 << 32) |
                                           ((long) b4 << 24) | ((long) b3 << 16) | ((long) b2 << 8) | (long) b1);
                            bufferDouble[i] = Double.longBitsToDouble(tmpLong);
                        }
                    }
                } catch (IOException error) {
                    throw error;
                }

                break;

            case ARGB:
                try {

                    if (compressionType == FileInfoBase.COMPRESSION_NONE) {
                        raFile.read(bufferByte);
                    } else {

                        try {
                            int max = bufferByte.length;
                            int counter = 0;
                            int len = max;
                            int tempVal = 0;

                            while (counter < max) {
                                tempVal = inflaterStream.read(bufferByte, counter, len);

                                if (tempVal == -1) {
                                    break;
                                }

                                counter += tempVal;
                                len = (max - counter);
                            }
                        } catch (EOFException eofex) {
                            break;
                        }
                    }
                } catch (IOException error) {
                    throw error;
                }

                break;

            case ARGB_USHORT:
                try {

                    if (compressionType == FileInfoBase.COMPRESSION_NONE) {
                        raFile.read(bufferByte);
                    } else {

                        try {
                            int max = bufferByte.length;
                            int counter = 0;
                            int len = max;
                            int tempVal = 0;

                            while (counter < max) {
                                tempVal = inflaterStream.read(bufferByte, counter, len);

                                if (tempVal == -1) {
                                    break;
                                }

                                counter += tempVal;
                                len = (max - counter);
                            }
                        } catch (EOFException eofex) {
                            break;
                        }
                    }

                    if (endianess == BIG_ENDIAN) {

                        for (i = 0, index = 0; i < bufferSize; i++) {
                            b1 = bufferByte[index++] & 0xff;
                            b2 = bufferByte[index++] & 0xff;
                            bufferShort[i] = (short) ((b1 << 8) | b2);
                        }
                    } else {

                        for (i = 0, index = 0; i < bufferSize; i++) {
                            b1 = bufferByte[index++] & 0xff;
                            b2 = bufferByte[index++] & 0xff;
                            bufferShort[i] = (short) ((b2 << 8) | b1);
                        }
                    }
                } catch (IOException error) {
                    throw error;
                }

                break;
                
            case ARGB_FLOAT:
                try {

                    if (compressionType == FileInfoBase.COMPRESSION_NONE) {
                        raFile.read(bufferByte);
                    } else {

                        try {
                            int max = bufferByte.length;
                            int counter = 0;
                            int len = max;
                            int tempVal = 0;

                            while (counter < max) {
                                tempVal = inflaterStream.read(bufferByte, counter, len);

                                if (tempVal == -1) {
                                    break;
                                }

                                counter += tempVal;
                                len = (max - counter);
                            }
                        } catch (EOFException eofex) {
                            break;
                        }
                    }
                    
                    int tmpInt;

                    if (endianess == BIG_ENDIAN) {

                        for (i = 0, index = 0; i < bufferSize; i++) {
                            b1 = bufferByte[index++] & 0xff;
                            b2 = bufferByte[index++] & 0xff;
                            b3 = bufferByte[index++] & 0xff;
                            b4 = bufferByte[index++] & 0xff;
                            tmpInt =  ((b1 << 24) | (b2 << 16) | (b3 << 8) | b4);
                            bufferFloat[i] = Float.intBitsToFloat(tmpInt);
                        }
                    } else {

                        for (i = 0, index = 0; i < bufferSize; i++) {
                            b1 = bufferByte[index++] & 0xff;
                            b2 = bufferByte[index++] & 0xff;
                            b3 = bufferByte[index++] & 0xff;
                            b4 = bufferByte[index++] & 0xff;
                            tmpInt =  ((b4 << 24) | (b3 << 16) | (b2 << 8) | b1);
                            bufferFloat[i] = Float.intBitsToFloat(tmpInt);
                        }
                    }
                } catch (IOException error) {
                    throw error;
                }

                break;


            default:
                throw new IOException();
        }
    }

    /**
     * Sets the classes file handle to the past in the method.
     *
     * @param  file   random access file pointer
     * @param  fInfo  information that describes the image
     */
    public void setImageFile(RandomAccessFile file, DICOMFile fInfo) {
        fileInfo = fInfo;

        if (raFile != null) {

            try {
                raFile.close();
            } catch (IOException ex) { }
        }

        raFile = file;
    }

    /**
     * Sets the number of colors used in RGB files.
     *
     * @param  numColors  DOCUMENT ME!
     */
    public void setNumColors(int numColors) {
        this.numColors = numColors;
    }

    /**
     * Sets the planar configuration for RGB images.
     *
     * @param  _planarConfig  0 indicates pixels are RGB, RGB chunky 1 indicates pixels are RRR, GGG, BBB planar
     */
    public void setPlanarConfig(int _planarConfig) {
        planarConfig = _planarConfig;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  RGBAOrder  DOCUMENT ME!
     */
    public void setRGBAOrder(boolean RGBAOrder) {
        this.RGBAOrder = RGBAOrder;
    }

    /**
     * This method writes a raw byte buffer to a file.
     *
     * @param   buffer  the image data buffer
     * @param   start   start of data in the read image file in units of extents[0]*extents[1]
     * @param   end     end of data in the read image file in units of extents[0]*extents[1]
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public void writeBufferByte(byte[] buffer, int start, int end) throws IOException {
    	
        raFile.write(buffer);
    }

    /**
     * This method writes a double buffer to a file.
     *
     * @param   buffer     the image data buffer
     * @param   start      start of data in the read image file in units of extents[0]*extents[1]
     * @param   end        end of data in the read image file in units of extents[0]*extents[1]
     * @param   endianess  the endianess of the data
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public void writeBufferDouble(double[] buffer, int start, int end, boolean endianess) throws IOException {
        int i, index;

        if ((end - start) != bufferSize) {
            bufferSize = end - start;

            try {
                bufferByte = new byte[8 * bufferSize];
            } catch (OutOfMemoryError error) {
                bufferByte = null;
                System.gc();
                throw error;
            }
        }

        

        try {
            long tmpLong;

            if (endianess == BIG_ENDIAN) {

                for (i = 0, index = 0; i < bufferSize; i++) {
                    tmpLong = Double.doubleToLongBits(buffer[i]);
                    bufferByte[index++] = (byte) (tmpLong >>> 56);
                    bufferByte[index++] = (byte) (tmpLong >>> 48);
                    bufferByte[index++] = (byte) (tmpLong >>> 40);
                    bufferByte[index++] = (byte) (tmpLong >>> 32);
                    bufferByte[index++] = (byte) (tmpLong >>> 24);
                    bufferByte[index++] = (byte) (tmpLong >>> 16);
                    bufferByte[index++] = (byte) (tmpLong >>> 8);
                    bufferByte[index++] = (byte) (tmpLong & 0xff);
            }
            } else {

                for (i = 0, index = 0; i < bufferSize; i++) {
                    tmpLong = Double.doubleToLongBits(buffer[i]);
                    bufferByte[index++] = (byte) (tmpLong & 0xff);
                    bufferByte[index++] = (byte) (tmpLong >>> 8);
                    bufferByte[index++] = (byte) (tmpLong >>> 16);
                    bufferByte[index++] = (byte) (tmpLong >>> 24);
                    bufferByte[index++] = (byte) (tmpLong >>> 32);
                    bufferByte[index++] = (byte) (tmpLong >>> 40);
                    bufferByte[index++] = (byte) (tmpLong >>> 48);
                    bufferByte[index++] = (byte) (tmpLong >>> 56);
                }
            }

            raFile.write(bufferByte);
        } catch (IOException error) {
            throw error;
        }
    }

    /**
     * This method writes a float buffer to a file.
     *
     * @param   buffer     the image data buffer
     * @param   start      start of data in the read image file in units of extents[0]*extents[1]
     * @param   end        end of data in the read image file in units of extents[0]*extents[1]
     * @param   endianess  the endianess of the data
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public void writeBufferFloat(float[] buffer, int start, int end, boolean endianess) throws IOException {
        int i, index;

        if ((end - start) != bufferSize) {
            bufferSize = end - start;

            try {
                bufferByte = new byte[4 * bufferSize];
            } catch (OutOfMemoryError error) {
                bufferByte = null;
                System.gc();
                throw error;
            }
        }

        try {
            int tmpInt;

            if (endianess == BIG_ENDIAN) {

                for (i = 0, index = 0; i < bufferSize; i++) {
                    tmpInt = Float.floatToIntBits(buffer[i]);
                    bufferByte[index++] = (byte) (tmpInt >>> 24);
                    bufferByte[index++] = (byte) (tmpInt >>> 16);
                    bufferByte[index++] = (byte) (tmpInt >>> 8);
                    bufferByte[index++] = (byte) (tmpInt & 0xff);
                }
            } else {

                for (i = 0, index = 0; i < bufferSize; i++) {
                    tmpInt = Float.floatToIntBits(buffer[i]);
                    bufferByte[index++] = (byte) (tmpInt & 0xff);
                    bufferByte[index++] = (byte) (tmpInt >>> 8);
                    bufferByte[index++] = (byte) (tmpInt >>> 16);
                    bufferByte[index++] = (byte) (tmpInt >>> 24);
                }
            }

            raFile.write(bufferByte);
        } catch (IOException error) {
            throw error;
        }
    }

    /**
     * This method writes a int buffer to a file.
     *
     * @param   buffer     the image data buffer
     * @param   start      start of data in the read image file in units of extents[0]*extents[1]
     * @param   end        end of data in the read image file in units of extents[0]*extents[1]
     * @param   endianess  the endianess of the data
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public void writeBufferInt(int[] buffer, int start, int end, boolean endianess) throws IOException {
        int i, index;

        // boolean endianess = image.getFileInfo(0).getEndianess();
        if ((end - start) != bufferSize) {
            bufferSize = end - start;

            try {
                bufferByte = new byte[4 * bufferSize];
            } catch (OutOfMemoryError error) {
                bufferByte = null;
                System.gc();
                throw error;
            }
        }

        try {
            int tmpInt;

            if (endianess == BIG_ENDIAN) {

                for (i = 0, index = 0; i < bufferSize; i++) {
                    tmpInt = buffer[i];
                    bufferByte[index++] = (byte) (tmpInt >>> 24);
                    bufferByte[index++] = (byte) (tmpInt >>> 16);
                    bufferByte[index++] = (byte) (tmpInt >>> 8);
                    bufferByte[index++] = (byte) (tmpInt & 0xff);
                }
            } else {

                for (i = 0, index = 0; i < bufferSize; i++) {
                    tmpInt = buffer[i];
                    bufferByte[index++] = (byte) (tmpInt & 0xff);
                    bufferByte[index++] = (byte) (tmpInt >>> 8);
                    bufferByte[index++] = (byte) (tmpInt >>> 16);
                    bufferByte[index++] = (byte) (tmpInt >>> 24);
                }
            }

            raFile.write(bufferByte);
        } catch (IOException error) {
            throw error;
        }
    }

    /**
     * This method writes a int buffer to a file.
     *
     * @param   buffer     the image data buffer
     * @param   start      start of data in the read image file in units of extents[0]*extents[1]
     * @param   end        end of data in the read image file in units of extents[0]*extents[1]
     * @param   endianess  the endianess of the data
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public void writeBufferLong(long[] buffer, int start, int end, boolean endianess) throws IOException {
        int i, index;

        // boolean endianess = image.getFileInfo(0).getEndianess();
        if ((end - start) != bufferSize) {
            bufferSize = end - start;

            try {
                bufferByte = new byte[8 * bufferSize];
            } catch (OutOfMemoryError error) {
                bufferByte = null;
                System.gc();
                throw error;
            }
        }

        try {
            long tmpLong;

            if (endianess == BIG_ENDIAN) {

                for (i = 0, index = 0; i < bufferSize; i++) {
                    tmpLong = buffer[i];
                    bufferByte[index++] = (byte) (tmpLong >>> 56);
                    bufferByte[index++] = (byte) (tmpLong >>> 48);
                    bufferByte[index++] = (byte) (tmpLong >>> 40);
                    bufferByte[index++] = (byte) (tmpLong >>> 32);
                    bufferByte[index++] = (byte) (tmpLong >>> 24);
                    bufferByte[index++] = (byte) (tmpLong >>> 16);
                    bufferByte[index++] = (byte) (tmpLong >>> 8);
                    bufferByte[index++] = (byte) (tmpLong & 0xff);
                }
            } else {

                for (i = 0, index = 0; i < bufferSize; i++) {
                    tmpLong = buffer[i];
                    bufferByte[index++] = (byte) (tmpLong & 0xff);
                    bufferByte[index++] = (byte) (tmpLong >>> 8);
                    bufferByte[index++] = (byte) (tmpLong >>> 16);
                    bufferByte[index++] = (byte) (tmpLong >>> 24);
                    bufferByte[index++] = (byte) (tmpLong >>> 32);
                    bufferByte[index++] = (byte) (tmpLong >>> 40);
                    bufferByte[index++] = (byte) (tmpLong >>> 48);
                    bufferByte[index++] = (byte) (tmpLong >>> 56);
                }
            }

            raFile.write(bufferByte);
        } catch (IOException error) {
            throw error;
        }
    }

    /**
     * This method writes a RGB buffer to a file.
     *
     * @param   buffer  the image data buffer
     * @param   start   start of data in the read image file in units of extents[0]*extents[1]
     * @param   end     end of data in the read image file in units of extents[0]*extents[1]
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public void writeBufferRGB(int[] buffer, int start, int end) throws IOException {
        int i, j;

        if ((end - start) != bufferSize) {
            bufferSize = end - start;

            try {
                bufferByte = new byte[3 * bufferSize];
            } catch (OutOfMemoryError error) {
                bufferByte = null;
                System.gc();
                throw error;
            }
        }

        if (planarConfig == 0) {

            try {

                for (i = 0, j = 0; i < (4 * bufferSize); i += 4, j += 3) {
                    bufferByte[j] = (byte) (buffer[i + 1] & 0xff);
                    bufferByte[j + 1] = (byte) (buffer[i + 2] & 0xff);
                    bufferByte[j + 2] = (byte) (buffer[i + 3] & 0xff);
                }

                raFile.write(bufferByte);
            } catch (IOException error) {
                throw error;
            }
        } else {

            try {

                for (i = 0, j = 0; i < (4 * bufferSize); i += 4, j++) {
                    bufferByte[j] = (byte) (buffer[i + 1] & 0xff);
                    bufferByte[j + bufferSize] = (byte) (buffer[i + 2] & 0xff);
                    bufferByte[j + (2 * bufferSize)] = (byte) (buffer[i + 3] & 0xff);
                }

                raFile.write(bufferByte);
            } catch (IOException error) {
                throw error;
            }
        }
    }

    /**
     * This method writes a RGB_USHORT buffer to a file.
     *
     * @param   buffer     the image data buffer
     * @param   start      start of data in the read image file in units of extents[0]*extents[1]
     * @param   end        end of data in the read image file in units of extents[0]*extents[1]
     * @param   endianess  the endianess of the data
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public void writeBufferRGB_USHORT(int[] buffer, int start, int end, boolean endianess) throws IOException {
        int i, j;

        if ((end - start) != bufferSize) {
            bufferSize = end - start;

            try {
                bufferByte = new byte[6 * bufferSize];
            } catch (OutOfMemoryError error) {
                bufferByte = null;
                System.gc();
                throw error;
            }
        }

        if (planarConfig == 0) {

            try {

                if (endianess == BIG_ENDIAN) {

                    for (i = 0, j = 0; i < (4 * bufferSize); i += 4, j += 6) {
                        bufferByte[j] = (byte) (buffer[i + 1] >>> 8);
                        bufferByte[j + 1] = (byte) (buffer[i + 1] & 0xff);
                        bufferByte[j + 2] = (byte) (buffer[i + 2] >>> 8);
                        bufferByte[j + 3] = (byte) (buffer[i + 2] & 0xff);
                        bufferByte[j + 4] = (byte) (buffer[i + 3] >>> 8);
                        bufferByte[j + 5] = (byte) (buffer[i + 3] & 0xff);
                    }
                } // if (endianess == BIG_ENDIAN)
                else { // endianess == LITTLE_ENDIAN

                    for (i = 0, j = 0; i < (4 * bufferSize); i += 4, j += 6) {
                        bufferByte[j] = (byte) (buffer[i + 1] & 0xff);
                        bufferByte[j + 1] = (byte) (buffer[i + 1] >>> 8);
                        bufferByte[j + 2] = (byte) (buffer[i + 2] & 0xff);
                        bufferByte[j + 3] = (byte) (buffer[i + 2] >>> 8);
                        bufferByte[j + 4] = (byte) (buffer[i + 3] & 0xff);
                        bufferByte[j + 5] = (byte) (buffer[i + 3] >>> 8);
                    }
                } // else endianess == LITTLE_ENDIAN

                raFile.write(bufferByte);
            } catch (IOException error) {
                throw error;
            }
        } else {

            try {

                if (endianess == BIG_ENDIAN) {

                    for (i = 0, j = 0; i < (4 * bufferSize); i += 4, j += 1) {
                        bufferByte[j] = (byte) (buffer[i + 1] >>> 8);
                        bufferByte[j + 1] = (byte) (buffer[i + 1] & 0xff);
                        bufferByte[j + bufferSize] = (byte) (buffer[i + 2] >>> 8);
                        bufferByte[j + 1 + bufferSize] = (byte) (buffer[i + 2] & 0xff);
                        bufferByte[j + (2 * bufferSize)] = (byte) (buffer[i + 3] >>> 8);
                        bufferByte[j + 1 + (2 * bufferSize)] = (byte) (buffer[i + 3] & 0xff);
                    }
                } // if (endianess == BIG_ENDIAN)
                else { // endianess == LITTLE_ENDIAN

                    for (i = 0, j = 0; i < (4 * bufferSize); i += 4, j += 1) {
                        bufferByte[j] = (byte) (buffer[i + 1] & 0xff);
                        bufferByte[j + 1] = (byte) (buffer[i + 1] >>> 8);
                        bufferByte[j + bufferSize] = (byte) (buffer[i + 2] & 0xff);
                        bufferByte[j + 1 + bufferSize] = (byte) (buffer[i + 2] >>> 8);
                        bufferByte[j + (2 * bufferSize)] = (byte) (buffer[i + 3] & 0xff);
                        bufferByte[j + 1 + (2 * bufferSize)] = (byte) (buffer[i + 3] >>> 8);
                    }
                } // else endianess == LITTLE_ENDIAN

                raFile.write(bufferByte);
            } catch (IOException error) {
                throw error;
            }
        }
    }

    /**
     * This method writes a raw short buffer to a file.
     *
     * @param   buffer     the image data buffer
     * @param   start      start of data in the read image file in units of extents[0]*extents[1]
     * @param   end        end of data in the read image file in units of extents[0]*extents[1]
     * @param   endianess  the endianess of the data
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public void writeBufferShort(short[] buffer, int start, int end, boolean endianess) throws IOException {
        int i, index;

        // boolean endianess = image.getFileInfo(0).getEndianess();
        if ((end - start) != bufferSize) {
            bufferSize = end - start;

            try {
                bufferByte = new byte[2 * bufferSize];
            } catch (OutOfMemoryError error) {
                bufferByte = null;
                System.gc();
                throw error;
            }
        }

        try {

            if (endianess == BIG_ENDIAN) {

                for (i = 0, index = 0; i < bufferSize; i++) {
                    bufferByte[index++] = (byte) (buffer[i] >>> 8);
                    bufferByte[index++] = (byte) (buffer[i] & 0xff);
                }
            } else {

                for (i = 0, index = 0; i < bufferSize; i++) {
                    bufferByte[index++] = (byte) (buffer[i] & 0xff);
                    bufferByte[index++] = (byte) (buffer[i] >>> 8);
                }
            }

            raFile.write(bufferByte);
        } catch (IOException error) {
            throw error;
        }
    }

    /**
     * This method writes a raw unsigned byte buffer to a file.
     *
     * @param   buffer  image data buffer
     * @param   start   start of data in the read image file in units of extents[0]*extents[1]
     * @param   end     end of data in the read image file in units of extents[0]*extents[1]
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public void writeBufferUByte(short[] buffer, int start, int end) throws IOException {
        int i;

        if ((end - start) != bufferSize) {
            bufferSize = end - start;

            try {
                bufferByte = new byte[bufferSize];
            } catch (OutOfMemoryError error) {
                bufferByte = null;
                System.gc();
                throw error;
            }
        }

        try {

            for (i = 0; i < bufferSize; i++) {
                bufferByte[i] = (byte) (buffer[i] & 0xff);
            }

            raFile.write(bufferByte);
        } catch (IOException error) {
            throw error;
        }
    }

    /**
     * This method writes an unisgned int buffer to a file.
     *
     * @param   buffer     the image data buffer
     * @param   start      start of data in the read image file in units of extents[0]*extents[1]
     * @param   end        end of data in the read image file in units of extents[0]*extents[1]
     * @param   endianess  the endianess of the data
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public void writeBufferUInt(long[] buffer, int start, int end, boolean endianess) throws IOException {
        int i, index;

        // boolean endianess = image.getFileInfo(0).getEndianess();
        if ((end - start) != bufferSize) {
            bufferSize = end - start;

            try {
                bufferByte = new byte[4 * bufferSize];
            } catch (OutOfMemoryError error) {
                bufferByte = null;
                System.gc();
                throw error;
            }
        }

        try {
            long tmpLong;

            if (endianess == BIG_ENDIAN) {

                for (i = 0, index = 0; i < bufferSize; i++) {
                    tmpLong = buffer[i];
                    bufferByte[index++] = (byte) (tmpLong >>> 24);
                    bufferByte[index++] = (byte) (tmpLong >>> 16);
                    bufferByte[index++] = (byte) (tmpLong >>> 8);
                    bufferByte[index++] = (byte) (tmpLong & 0xff);
                }
            } else {

                for (i = 0, index = 0; i < bufferSize; i++) {
                    tmpLong = buffer[i];
                    bufferByte[index++] = (byte) (tmpLong & 0xff);
                    bufferByte[index++] = (byte) (tmpLong >>> 8);
                    bufferByte[index++] = (byte) (tmpLong >>> 16);
                    bufferByte[index++] = (byte) (tmpLong >>> 24);
                }
            }

            raFile.write(bufferByte);
        } catch (IOException error) {
            throw error;
        }
    }

    /**
     * This method writes a raw unsigned short buffer to a file.
     *
     * @param   buffer     the image data buffer
     * @param   start      start of data in the read image file in units of extents[0]*extents[1]
     * @param   end        end of data in the read image file in units of extents[0]*extents[1]
     * @param   endianess  the endianess of the data
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public void writeBufferUShort(int[] buffer, int start, int end, boolean endianess) throws IOException {
        int i, index;

        // boolean endianess = image.getFileInfo(0).getEndianess();
        if ((end - start) != bufferSize) {
            bufferSize = end - start;

            try {
                bufferByte = new byte[2 * bufferSize];
            } catch (OutOfMemoryError error) {
                bufferByte = null;
                System.gc();
                throw error;
            }
        }

        try {

            if (endianess == BIG_ENDIAN) {

                for (i = 0, index = 0; i < bufferSize; i++) {
                    bufferByte[index++] = (byte) (buffer[i] >>> 8);
                    bufferByte[index++] = (byte) (buffer[i] & 0xff);
                }
            } else {

                for (i = 0, index = 0; i < bufferSize; i++) {
                    bufferByte[index++] = (byte) (buffer[i] & 0xff);
                    bufferByte[index++] = (byte) (buffer[i] >>> 8);
                }
            }

            raFile.write(bufferByte);
        } catch (IOException error) {
            throw error;
        }
    }
}
