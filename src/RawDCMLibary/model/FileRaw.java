package RawDCMLibary.model;

import java.io.*;
import java.util.*;
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
 * The class reads and writes raw files of all data types: boolean, byte, short,
 * int, long, float, and double. For the read process an offset can be pass as a
 * parameter to identify the location in the file where the data starts. A
 * number of file formats while not "raw" have data at a specific location after
 * a fixed length but unknown header and therefore can be treated as raw.
 *
 * @version 0.1 Sept 2, 1997
 * @see FileIO
 * @see FileInfoXML
 * @see FileRawChunk
 */
public class FileRaw extends FileBase {

    //~ Instance fields ------------------------------------------------------------------------------------------------
    /**
     * DOCUMENT ME!
     */
    private int compressionType = FileInfoBase.COMPRESSION_NONE;

    /**
     * DOCUMENT ME!
     */
    private File file = null;

    /**
     * DOCUMENT ME!
     */
    private DICOMFile fileInfo;

    @SuppressWarnings("unused")
    private String fileName;

    /**
     * DOCUMENT ME!
     */
    private FileRawChunk fileRW = null;

    /**
     * DOCUMENT ME!
     */
    private int nImages;

    /**
     * DOCUMENT ME!
     */
    private int nTimePeriods;

    /**
     * DOCUMENT ME!
     */
    private int planarConfig;

    /**
     * DOCUMENT ME!
     */
    private long startPosition = 0;

    private int numColors = 3;

    /**
     * Allow reading from 4 color files with RGBA order
     */
    private boolean RGBAOrder = false;

    /**
     * flag that indicates if raFile should first be set to length of 0 *
     */
    private boolean zeroLengthFlag = true;

    private String dataFileName[] = null;

    // Default of 63 and 6 for 8 byte units.  For Analyze FileAnalyze sets to 7 and 3
    // for byte units.
    /**
     * Used in reading and writing boolean
     */
    private int minimumBitsMinus1 = 63;

    /**
     * Used in reading and writing boolean
     */
    private int shiftToDivide = 6;

    //~ Constructors ---------------------------------------------------------------------------------------------------
    /**
     * Constructor for Raw Files that will be used to write from 4D to 3D or
     * from 3D to 2D.
     *
     * @param fInfo fileinfo
     */
    public FileRaw(DICOMFile fInfo) {
        fileInfo = fInfo;

        // check to see if compression handling is to be used
        // compression performed in FileIO.readImage, FileIO.writeImage, and FileImageXML.readImage.
        // Note that bzip2 cannot decompress into InflaterInputStream in FileRawChunk.
        //compressionType = fInfo.getCompressionType();
    }

    /**
     * Raw reader/writer constructor.
     *
     * @param fileName Complete file name
     * @param fInfo Information that describes the image.
     * @param rwFlag Read/write flag.
     *
     * @exception IOException if there is an error making the files
     */
    public FileRaw(String fileName, DICOMFile fInfo, int rwFlag) throws IOException {
        fileInfo = fInfo;
        // compression performed in FileIO.readImage, FileIO.writeImage, and FileImageXML.readImage.
        // Note that bzip2 cannot decompress into InflaterInputStream in FileRawChunk.
        //compressionType = fInfo.getCompressionType();

        // check to see if compression handling is to be used
        if (compressionType == FileInfoBase.COMPRESSION_NONE) {
            file = new File(fileName);

            if (raFile != null) {

                try {
                    raFile.close();
                } catch (IOException ioex) {
                }
            }

            if (rwFlag == READ) {
                raFile = new RandomAccessFile(file, "r");
            } else if (rwFlag == READ_WRITE) {
                raFile = new RandomAccessFile(file, "rw");
            }
        }

        this.fileName = fileName;

        if (compressionType == FileInfoBase.COMPRESSION_NONE) {
            fileRW = new FileRawChunk(raFile, fileInfo);
        } else {
            fileRW = new FileRawChunk(fileName, fileInfo, rwFlag, compressionType);
        }
    }

    /**
     * Raw reader/writer constructor..
     *
     * @param fileName File name (no path)
     * @param fileDir File directory (with trailing file separator).
     * @param fInfo Information that describes the image.
     * @param showProgressBar Boolean indicating if progess bar should be
     * displayed.
     * @param rwFlag Read/write flag.
     *
     * @exception IOException if there is an error making the files
     */
    public FileRaw(String fileName, String fileDir, DICOMFile fInfo, int rwFlag)
            throws IOException {
        this(fileDir + fileName, fInfo, rwFlag);
    }

    //~ Methods --------------------------------------------------------------------------------------------------------
    /**
     * Closes random access file associated with this object.
     *
     * @throws IOException DOCUMENT ME!
     */
    public void close() throws IOException {

        if (compressionType == FileInfoBase.COMPRESSION_NONE) {

            if (raFile != null) {
                raFile.close();
                raFile = null;
            }
            // System.err.println("closed and nulled FileRaw: raFile");
        }
    }

    /**
     * Prepares this class for cleanup.
     */
    public void finalize() {
        file = null;
        fileName = null;
        fileInfo = null;

        if (fileRW != null) {

            try {
                fileRW.close();
                // System.err.println("closed FileRaw: fileRW (FileRawChunk)");
            } catch (IOException ex) {
            }

            fileRW.finalize();
        }

        fileRW = null;
        if (dataFileName != null) {
            for (int i = 0; i < dataFileName.length; i++) {
                dataFileName[i] = null;
            }
            dataFileName = null;
        }

        try {
            super.finalize();
        } catch (Throwable er) {
        }
    }

    /**
     * Used in reading and writing boolean
     *
     * @param minimumBitsMinus1
     */
    public void setMinimumBitsMinus1(int minimumBitsMinus1) {
        this.minimumBitsMinus1 = minimumBitsMinus1;
        if (fileRW != null) {
            fileRW.setMinimumBitsMinus1(minimumBitsMinus1);
        }
    }

    /**
     * Used in reading and writing boolean
     *
     * @param shiftToDivide
     */
    public void setShiftToDivide(int shiftToDivide) {
        this.shiftToDivide = shiftToDivide;
        if (fileRW != null) {
            fileRW.setShiftToDivide(shiftToDivide);
        }
    }

    /**
     * Accessor that returns the number of image slices saved.
     *
     * @return The number of images.
     */
    public int getNImages() {
        return nImages;
    }

    /**
     * Accessor that returns the number of time periods saved.
     *
     * @return The number of time periods.
     */
    public int getNTimePeriods() {
        return nTimePeriods;
    }

    /**
     * Accessor that returns the array of data file names
     *
     * @return
     */
    public String[] getDataFileName() {
        return dataFileName;
    }

    /**
     * This method reads a file and puts the data in the buffer.
     *
     * @param buffer float buffer where the data will be stored.
     * @param offset points to where the data of the image is located. It is
     * equal to the header length.
     * @param imageType ModelImage type (i.e. boolean, byte ...);
     *
     * @exception IOException if there is an error reading the file
     *
     * @see FileInfoXML
     * @see FileRawChunk
     */
    public void readImage(float[] buffer, long offset, int imageType) throws IOException {
        int i;
        int ii;
        int bufferSize = buffer.length;

        switch (imageType) {

            case BOOLEAN:
                try {
                fileRW.readImage(BOOLEAN, offset, bufferSize);

                byte[] tmpBuffer = fileRW.getByteBuffer();

                for (i = 0; i < bufferSize; i++) {
                    buffer[i] = tmpBuffer[i];
                }
                tmpBuffer = null;
            } catch (IOException error) {
                throw error;
            }

            break;

            case BYTE:
                try {
                fileRW.readImage(BYTE, offset, bufferSize);

                byte[] tmpBuffer = fileRW.getByteBuffer();

                for (i = 0; i < bufferSize; i++) {
                    buffer[i] = tmpBuffer[i];
                }
                tmpBuffer = null;
            } catch (IOException error) {
                throw error;
            }

            break;

            case UBYTE:
                try {
                fileRW.readImage(UBYTE, offset, bufferSize);

                byte[] tmpBuffer = fileRW.getByteBuffer();

                for (i = 0; i < bufferSize; i++) {

                    // buffer[i] = tmpBuffer[i];
                    buffer[i] = (float) (tmpBuffer[i] & 0xff);
                }
                tmpBuffer = null;
            } catch (IOException error) {
                throw error;
            }

            break;

            case SHORT:
                try {
                fileRW.readImage(SHORT, offset, bufferSize);

                short[] tmpBuffer = fileRW.getShortBuffer();

                for (i = 0; i < bufferSize; i++) {
                    buffer[i] = tmpBuffer[i];
                }
                tmpBuffer = null;
            } catch (IOException error) {
                throw error;
            }

            break;

            case USHORT:
                try {
                fileRW.readImage(USHORT, offset, bufferSize);

                short[] tmpBuffer = fileRW.getShortBuffer();

                for (i = 0; i < bufferSize; i++) {

                    // buffer[i] = tmpBuffer[i];
                    buffer[i] = (float) (tmpBuffer[i] & 0xffff);
                }
                tmpBuffer = null;
            } catch (IOException error) {
                throw error;
            }

            break;

            case INTEGER:
                try {
                fileRW.readImage(INTEGER, offset, bufferSize);

                int[] tmpBuffer = fileRW.getIntBuffer();

                for (i = 0; i < bufferSize; i++) {
                    buffer[i] = tmpBuffer[i];
                }
                tmpBuffer = null;
            } catch (IOException error) {
                throw error;
            }

            break;

            case UINTEGER:
                try {
                fileRW.readImage(UINTEGER, offset, bufferSize);

                int[] tmpBuffer = fileRW.getIntBuffer();

                for (i = 0; i < bufferSize; i++) {
                    buffer[i] = (float) (tmpBuffer[i] & 0xffffffffL);
                }
                tmpBuffer = null;
            } catch (IOException error) {
                throw error;
            }

            break;

            case LONG:
                try {
                fileRW.readImage(LONG, offset, bufferSize);

                long[] tmpBuffer = fileRW.getLongBuffer();

                for (i = 0; i < bufferSize; i++) {
                    buffer[i] = tmpBuffer[i];
                }
                tmpBuffer = null;
            } catch (IOException error) {
                throw error;
            }

            break;

            case FLOAT:
                try {
                fileRW.readImage(FLOAT, offset, bufferSize);

                float[] tmpBuffer = fileRW.getFloatBuffer();

                for (i = 0; i < bufferSize; i++) {
                    buffer[i] = tmpBuffer[i];
                    // Try array copy
                }
                tmpBuffer = null;
            } catch (IOException error) {
                throw error;
            }

            break;

            case DOUBLE:
                try {
                fileRW.readImage(DOUBLE, offset, bufferSize);

                double[] tmpBuffer = fileRW.getDoubleBuffer();

                for (i = 0; i < bufferSize; i++) {
                    buffer[i] = (float) tmpBuffer[i];
                }
                tmpBuffer = null;
            } catch (IOException error) {
                throw error;
            }

            break;

            case ARGB:
                ii = 0;
                i = 0;

                try {
                    fileRW.readImage(ARGB, offset, bufferSize / 4 * numColors);
                    if (numColors == 2) {
                        if (planarConfig == 0) { // RG

                            byte[] tmpBuffer = fileRW.getByteBuffer();

                            for (i = 0, ii = 0; i < tmpBuffer.length; i += 2, ii += 4) {
                                buffer[ii] = 1;
                                buffer[ii + 1] = tmpBuffer[i];
                                buffer[ii + 2] = tmpBuffer[i + 1];
                                buffer[ii + 3] = 0;
                            }
                            tmpBuffer = null;
                        } else { // RRRRR GGGGG

                            byte[] tmpBuffer = fileRW.getByteBuffer();
                            int bufferOffset = tmpBuffer.length / 2;

                            for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                buffer[ii] = 1;
                                buffer[ii + 1] = tmpBuffer[i];
                                buffer[ii + 2] = tmpBuffer[i + bufferOffset];
                                buffer[ii + 3] = 0;
                            }
                            tmpBuffer = null;
                        }
                    } // if (numColors == 2)
                    else if (numColors == 3) {
                        if (planarConfig == 0) { // RGB

                            byte[] tmpBuffer = fileRW.getByteBuffer();

                            for (i = 0, ii = 0; i < tmpBuffer.length; i += 3, ii += 4) {
                                buffer[ii] = 1;
                                buffer[ii + 1] = tmpBuffer[i];
                                buffer[ii + 2] = tmpBuffer[i + 1];
                                buffer[ii + 3] = tmpBuffer[i + 2];
                            }
                            tmpBuffer = null;
                        } else { // RRRRR GGGGG BBBBB

                            byte[] tmpBuffer = fileRW.getByteBuffer();
                            int bufferOffset = tmpBuffer.length / 3;

                            for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                buffer[ii] = 1;
                                buffer[ii + 1] = tmpBuffer[i];
                                buffer[ii + 2] = tmpBuffer[i + bufferOffset];
                                buffer[ii + 3] = tmpBuffer[i + (2 * bufferOffset)];
                            }
                            tmpBuffer = null;
                        }
                    } // else if (numColors == 3)
                    else { // numColors == 4
                        if (!RGBAOrder) { // ARGB order
                            if (planarConfig == 0) { // ARGB

                                byte[] tmpBuffer = fileRW.getByteBuffer();

                                for (i = 0; i < tmpBuffer.length; i++) {
                                    buffer[i] = tmpBuffer[i];
                                }
                                tmpBuffer = null;
                            } else { // AAAA RRRRR GGGGG BBBBB

                                byte[] tmpBuffer = fileRW.getByteBuffer();
                                int bufferOffset = tmpBuffer.length / 4;

                                for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                    buffer[ii] = tmpBuffer[i];
                                    buffer[ii + 1] = tmpBuffer[i + bufferOffset];
                                    buffer[ii + 2] = tmpBuffer[i + (2 * bufferOffset)];
                                    buffer[ii + 3] = tmpBuffer[i + (3 * bufferOffset)];
                                }
                                tmpBuffer = null;
                            }
                        } else { // RGBAOrder
                            if (planarConfig == 0) { // RGBA

                                byte[] tmpBuffer = fileRW.getByteBuffer();

                                for (i = 0; i < tmpBuffer.length; i += 4) {
                                    buffer[i] = tmpBuffer[i + 3];
                                    buffer[i + 1] = tmpBuffer[i];
                                    buffer[i + 2] = tmpBuffer[i + 1];
                                    buffer[i + 3] = tmpBuffer[i + 2];
                                }
                                tmpBuffer = null;
                            } else { // RRRRR GGGGG BBBBB AAAA

                                byte[] tmpBuffer = fileRW.getByteBuffer();
                                int bufferOffset = tmpBuffer.length / 4;

                                for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                    buffer[ii] = tmpBuffer[i + (3 * bufferOffset)];
                                    buffer[ii + 1] = tmpBuffer[i];
                                    buffer[ii + 2] = tmpBuffer[i + bufferOffset];
                                    buffer[ii + 3] = tmpBuffer[i + (2 * bufferOffset)];
                                }
                                tmpBuffer = null;
                            }
                        } // else RGBAOrder
                    } // numColors == 4

                } catch (IOException error) {
                    throw error;
                }

                break;

            case ARGB_USHORT:
                ii = 0;
                i = 0;

                try {
                    fileRW.readImage(ARGB_USHORT, offset, bufferSize / 4 * numColors);

                    if (numColors == 2) {
                        if (planarConfig == 0) { // RG

                            short[] tmpBuffer = fileRW.getShortBuffer();

                            for (i = 0, ii = 0; i < tmpBuffer.length; i += 2, ii += 4) {
                                buffer[ii] = 65535.0f;
                                buffer[ii + 1] = (float) (tmpBuffer[i] & 0xffff);
                                buffer[ii + 2] = (float) (tmpBuffer[i + 1] & 0xffff);
                                buffer[ii + 3] = 0.0f;
                            }
                            tmpBuffer = null;
                        } else { // RRRRR GGGGG

                            short[] tmpBuffer = fileRW.getShortBuffer();
                            int bufferOffset = tmpBuffer.length / 2;

                            for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                buffer[ii] = 65535.0f;
                                buffer[ii + 1] = (float) (tmpBuffer[i] & 0xffff);
                                buffer[ii + 2] = (float) (tmpBuffer[i + bufferOffset] & 0xffff);
                                buffer[ii + 3] = 0.0f;
                            }
                            tmpBuffer = null;
                        }
                    } // if (numColors == 2)
                    else if (numColors == 3) {
                        if (planarConfig == 0) { // RGB

                            short[] tmpBuffer = fileRW.getShortBuffer();

                            for (i = 0, ii = 0; i < tmpBuffer.length; i += 3, ii += 4) {
                                buffer[ii] = 65535.0f;
                                buffer[ii + 1] = (float) (tmpBuffer[i] & 0xffff);
                                buffer[ii + 2] = (float) (tmpBuffer[i + 1] & 0xffff);
                                buffer[ii + 3] = (float) (tmpBuffer[i + 2] & 0xffff);
                            }
                            tmpBuffer = null;
                        } else { // RRRRR GGGGG BBBBB

                            short[] tmpBuffer = fileRW.getShortBuffer();
                            int bufferOffset = tmpBuffer.length / 3;

                            for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                buffer[ii] = 65535.0f;
                                buffer[ii + 1] = (float) (tmpBuffer[i] & 0xffff);
                                buffer[ii + 2] = (float) (tmpBuffer[i + bufferOffset] & 0xffff);
                                buffer[ii + 3] = (float) (tmpBuffer[i + (2 * bufferOffset)] & 0xffff);
                            }
                            tmpBuffer = null;
                        }
                    } // else if (numColors == 3)
                    else { // numColors == 4
                        if (!RGBAOrder) { // ARGB order
                            if (planarConfig == 0) { // ARGB

                                short[] tmpBuffer = fileRW.getShortBuffer();

                                for (i = 0; i < tmpBuffer.length; i++) {
                                    buffer[i] = (float) (tmpBuffer[i] & 0xffff);
                                }
                                tmpBuffer = null;
                            } else { // AAAA RRRRR GGGGG BBBBB

                                short[] tmpBuffer = fileRW.getShortBuffer();
                                int bufferOffset = tmpBuffer.length / 4;

                                for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                    buffer[ii] = (float) (tmpBuffer[i] & 0xffff);
                                    buffer[ii + 1] = (float) (tmpBuffer[i + bufferOffset] & 0xffff);
                                    buffer[ii + 2] = (float) (tmpBuffer[i + (2 * bufferOffset)] & 0xffff);
                                    buffer[ii + 3] = (float) (tmpBuffer[i + (3 * bufferOffset)] & 0xffff);
                                }
                                tmpBuffer = null;
                            }
                        } // if (!RGBAOrder)
                        else { // RGBAOrder 
                            if (planarConfig == 0) { // RGBA

                                short[] tmpBuffer = fileRW.getShortBuffer();

                                for (i = 0; i < tmpBuffer.length; i += 4) {
                                    buffer[i] = (float) (tmpBuffer[i + 3] & 0xffff);
                                    buffer[i + 1] = (float) (tmpBuffer[i] & 0xffff);
                                    buffer[i + 2] = (float) (tmpBuffer[i + 1] & 0xffff);
                                    buffer[i + 3] = (float) (tmpBuffer[i + 2] & 0xffff);
                                }
                                tmpBuffer = null;
                            } else { // RRRRR GGGGG BBBBB AAAAA

                                short[] tmpBuffer = fileRW.getShortBuffer();
                                int bufferOffset = tmpBuffer.length / 4;

                                for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                    buffer[ii] = (float) (tmpBuffer[i + (3 * bufferOffset)] & 0xffff);
                                    buffer[ii + 1] = (float) (tmpBuffer[i] & 0xffff);
                                    buffer[ii + 2] = (float) (tmpBuffer[i + bufferOffset] & 0xffff);
                                    buffer[ii + 3] = (float) (tmpBuffer[i + (2 * bufferOffset)] & 0xffff);
                                }
                                tmpBuffer = null;
                            }
                        } // else RGBAOrder
                    } // else numColors == 4

                } catch (IOException error) {
                    throw error;
                }

                break;

            case ARGB_FLOAT:
                ii = 0;
                i = 0;

                try {
                    fileRW.readImage(ARGB_FLOAT, offset, bufferSize / 4 * numColors);

                    if (numColors == 2) {
                        if (planarConfig == 0) { // RG

                            float[] tmpBuffer = fileRW.getFloatBuffer();

                            for (i = 0, ii = 0; i < tmpBuffer.length; i += 2, ii += 4) {
                                buffer[ii] = 255.0f;
                                buffer[ii + 1] = tmpBuffer[i];
                                buffer[ii + 2] = tmpBuffer[i + 1];
                                buffer[ii + 3] = 0.0f;
                            }
                            tmpBuffer = null;
                        } else { // RRRRR GGGGG

                            float[] tmpBuffer = fileRW.getFloatBuffer();
                            int bufferOffset = tmpBuffer.length / 2;

                            for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                buffer[ii] = 255.0f;
                                buffer[ii + 1] = tmpBuffer[i];
                                buffer[ii + 2] = tmpBuffer[i + bufferOffset];
                                buffer[ii + 3] = 0.0f;
                            }
                            tmpBuffer = null;
                        }
                    } // if (numColors == 2)
                    else if (numColors == 3) {
                        if (planarConfig == 0) { // RGB

                            float[] tmpBuffer = fileRW.getFloatBuffer();

                            for (i = 0, ii = 0; i < tmpBuffer.length; i += 3, ii += 4) {
                                buffer[ii] = 255.0f;
                                buffer[ii + 1] = tmpBuffer[i];
                                buffer[ii + 2] = tmpBuffer[i + 1];
                                buffer[ii + 3] = tmpBuffer[i + 2];
                            }
                            tmpBuffer = null;
                        } else { // RRRRR GGGGG BBBBB

                            float[] tmpBuffer = fileRW.getFloatBuffer();
                            int bufferOffset = tmpBuffer.length / 3;

                            for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                buffer[ii] = 255.0f;
                                buffer[ii + 1] = tmpBuffer[i];
                                buffer[ii + 2] = tmpBuffer[i + bufferOffset];
                                buffer[ii + 3] = tmpBuffer[i + (2 * bufferOffset)];
                            }
                            tmpBuffer = null;
                        }
                    } // else if (numColors == 3)
                    else { // numColors == 4
                        if (!RGBAOrder) { // ARGB order
                            if (planarConfig == 0) { // ARGB

                                float[] tmpBuffer = fileRW.getFloatBuffer();

                                for (i = 0; i < tmpBuffer.length; i++) {
                                    buffer[i] = tmpBuffer[i];
                                }
                                tmpBuffer = null;
                            } else { // AAAA RRRRR GGGGG BBBBB

                                float[] tmpBuffer = fileRW.getFloatBuffer();
                                int bufferOffset = tmpBuffer.length / 4;

                                for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                    buffer[ii] = tmpBuffer[i];
                                    buffer[ii + 1] = tmpBuffer[i + bufferOffset];
                                    buffer[ii + 2] = tmpBuffer[i + (2 * bufferOffset)];
                                    buffer[ii + 3] = tmpBuffer[i + (3 * bufferOffset)];
                                }
                                tmpBuffer = null;
                            }
                        } // if (!RGBAOrder)
                        else { // RGBAOrder 
                            if (planarConfig == 0) { // RGBA

                                float[] tmpBuffer = fileRW.getFloatBuffer();

                                for (i = 0; i < tmpBuffer.length; i += 4) {
                                    buffer[i] = tmpBuffer[i + 3];
                                    buffer[i + 1] = tmpBuffer[i];
                                    buffer[i + 2] = tmpBuffer[i + 1];
                                    buffer[i + 3] = tmpBuffer[i + 2];
                                }
                                tmpBuffer = null;
                            } else { // RRRRR GGGGG BBBBB AAAAA

                                float[] tmpBuffer = fileRW.getFloatBuffer();
                                int bufferOffset = tmpBuffer.length / 4;

                                for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                    buffer[ii] = tmpBuffer[i + (3 * bufferOffset)];
                                    buffer[ii + 1] = tmpBuffer[i];
                                    buffer[ii + 2] = tmpBuffer[i + bufferOffset];
                                    buffer[ii + 3] = tmpBuffer[i + (2 * bufferOffset)];
                                }
                                tmpBuffer = null;
                            }
                        } // else RGBAOrder
                    } // else numColors == 4

                } catch (IOException error) {
                    throw error;
                }

                break;

            default:
                throw new IOException();
        }
    }

    /**
     * This method reads a file and puts the data in the buffer. Added here to
     * help speed the reading of DICOM images
     *
     * @param buffer float buffer where the data will be stored.
     * @param offset points to where the data of the image is located. It is
     * equal to the header length.
     * @param imageType ModelImage type (i.e. boolean, byte ...);
     *
     * @exception IOException if there is an error reading the file
     *
     * @see FileInfoXML
     * @see FileRawChunk
     */
    public void readImage(int[] buffer, long offset, int imageType) throws IOException {
        int i;
        int ii;
        int bufferSize = buffer.length;

        switch (imageType) {

            case BOOLEAN:
                try {
                fileRW.readImage(BOOLEAN, offset, bufferSize);

                BitSet bufferBitSet = fileRW.getBitSetBuffer();

                for (i = 0; i < bufferSize; i++) {
                    if (bufferBitSet.get(i)) {
                        buffer[i] = 1;
                    } else {
                        buffer[i] = 0;
                    }
                }
            } catch (IOException error) {
                throw error;
            }

            break;

            case BYTE:
                try {
                fileRW.readImage(BYTE, offset, bufferSize);

                byte[] tmpBuffer = fileRW.getByteBuffer();

                for (i = 0; i < bufferSize; i++) {
                    buffer[i] = tmpBuffer[i];
                }
            } catch (IOException error) {
                throw error;
            }

            break;

            case UBYTE:
                try {
                fileRW.readImage(UBYTE, offset, bufferSize);

                byte[] tmpBuffer = fileRW.getByteBuffer();

                for (i = 0; i < bufferSize; i++) {

                    // buffer[i] = tmpBuffer[i];
                    buffer[i] = (short) (tmpBuffer[i] & 0xff);
                }
            } catch (IOException error) {
                throw error;
            }

            break;

            case SHORT:
                try {
                fileRW.readImage(SHORT, offset, bufferSize);

                short[] tmpBuffer = fileRW.getShortBuffer();

                for (i = 0; i < bufferSize; i++) {
                    buffer[i] = tmpBuffer[i];
                }
                // buffer = tmpBuffer;
                //System.arraycopy(tmpBuffer, 0, buffer, 0, tmpBuffer.length);
            } catch (IOException error) {
                throw error;
            }

            break;

            case USHORT:
                try {
                fileRW.readImage(USHORT, offset, bufferSize);

                short[] tmpBuffer = fileRW.getShortBuffer();

                for (i = 0; i < bufferSize; i++) {

                    // buffer[i] = tmpBuffer[i];
                    buffer[i] = (short) (tmpBuffer[i] & 0xffff);
                }
            } catch (IOException error) {
                throw error;
            }

            break;

            case INTEGER:
                try {
                fileRW.readImage(INTEGER, offset, bufferSize);

                int[] tmpBuffer = fileRW.getIntBuffer();

                // for (i = 0; i < bufferSize; i++) {
                // buffer[i] = tmpBuffer[i];
                // }
                // buffer = tmpBuffer;
                System.arraycopy(tmpBuffer, 0, buffer, 0, tmpBuffer.length);
            } catch (IOException error) {
                throw error;
            }

            break;

            case UINTEGER:
                try {
                fileRW.readImage(UINTEGER, offset, bufferSize);

                int[] tmpBuffer = fileRW.getIntBuffer();

                for (i = 0; i < bufferSize; i++) {

                    // buffer[i] = tmpBuffer[i];
                    buffer[i] = (int) (tmpBuffer[i] & 0xffffffff);
                }
                int[] tmmp = Arrays.copyOf(buffer, buffer.length);
                Arrays.sort(tmmp);
                System.out.println(tmmp[0]);
                System.out.println(tmmp[tmmp.length - 1]);
            } catch (IOException error) {
                throw error;
            }

            break;

            case ARGB:
                ii = 0;
                i = 0;

                try {
                    fileRW.readImage(ARGB, offset, bufferSize / 4 * numColors);
                    if (numColors == 2) {
                        if (planarConfig == 0) { // RG

                            byte[] tmpBuffer = fileRW.getByteBuffer();

                            for (i = 0, ii = 0; i < tmpBuffer.length; i += 2, ii += 4) {
                                buffer[ii] = 1;
                                buffer[ii + 1] = tmpBuffer[i];
                                buffer[ii + 2] = tmpBuffer[i + 1];
                                buffer[ii + 3] = 0;
                            }
                        } else { // RRRRR GGGGG

                            byte[] tmpBuffer = fileRW.getByteBuffer();
                            int bufferOffset = tmpBuffer.length / 2;

                            for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                buffer[ii] = 1;
                                buffer[ii + 1] = tmpBuffer[i];
                                buffer[ii + 2] = tmpBuffer[i + bufferOffset];
                                buffer[ii + 3] = 0;
                            }
                        }
                    } // if (numColors == 2)
                    else if (numColors == 3) {
                        if (planarConfig == 0) { // RGB

                            byte[] tmpBuffer = fileRW.getByteBuffer();

                            for (i = 0, ii = 0; i < tmpBuffer.length; i += 3, ii += 4) {
                                buffer[ii] = 1;
                                buffer[ii + 1] = tmpBuffer[i];
                                buffer[ii + 2] = tmpBuffer[i + 1];
                                buffer[ii + 3] = tmpBuffer[i + 2];
                            }
                        } else { // RRRRR GGGGG BBBBB

                            byte[] tmpBuffer = fileRW.getByteBuffer();
                            int bufferOffset = tmpBuffer.length / 3;

                            for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                buffer[ii] = 1;
                                buffer[ii + 1] = tmpBuffer[i];
                                buffer[ii + 2] = tmpBuffer[i + bufferOffset];
                                buffer[ii + 3] = tmpBuffer[i + (2 * bufferOffset)];
                            }
                        }
                    } // else if (numColors == 3)
                    else { // numColors == 4
                        if (!RGBAOrder) { // ARGB order
                            if (planarConfig == 0) { // ARGB

                                byte[] tmpBuffer = fileRW.getByteBuffer();

                                for (i = 0; i < tmpBuffer.length; i++) {
                                    buffer[i] = tmpBuffer[i];
                                }
                            } else { // AAAA RRRRR GGGGG BBBBB

                                byte[] tmpBuffer = fileRW.getByteBuffer();
                                int bufferOffset = tmpBuffer.length / 4;

                                for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                    buffer[ii] = tmpBuffer[i];
                                    buffer[ii + 1] = tmpBuffer[i + bufferOffset];
                                    buffer[ii + 2] = tmpBuffer[i + (2 * bufferOffset)];
                                    buffer[ii + 3] = tmpBuffer[i + (3 * bufferOffset)];
                                }
                            }
                        } else { // RGBAOrder
                            if (planarConfig == 0) { // RGBA

                                byte[] tmpBuffer = fileRW.getByteBuffer();

                                for (i = 0; i < tmpBuffer.length; i += 4) {
                                    buffer[i] = tmpBuffer[i + 3];
                                    buffer[i + 1] = tmpBuffer[i];
                                    buffer[i + 2] = tmpBuffer[i + 1];
                                    buffer[i + 3] = tmpBuffer[i + 2];
                                }
                            } else { // RRRRR GGGGG BBBBB AAAA

                                byte[] tmpBuffer = fileRW.getByteBuffer();
                                int bufferOffset = tmpBuffer.length / 4;

                                for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                    buffer[ii] = tmpBuffer[i + (3 * bufferOffset)];
                                    buffer[ii + 1] = tmpBuffer[i];
                                    buffer[ii + 2] = tmpBuffer[i + bufferOffset];
                                    buffer[ii + 3] = tmpBuffer[i + (2 * bufferOffset)];
                                }
                            }
                        } // else RGBAOrder
                    } // numColors == 4

                } catch (IOException error) {
                    throw error;
                }

                break;

            case ARGB_USHORT:
                ii = 0;
                i = 0;

                try {
                    fileRW.readImage(ARGB_USHORT, offset, bufferSize / 4 * 3);
                    if (numColors == 2) {
                        if (planarConfig == 0) { // RG

                            short[] tmpBuffer = fileRW.getShortBuffer();

                            for (i = 0, ii = 0; i < tmpBuffer.length; i += 2, ii += 4) {
                                buffer[ii] = 32767;
                                buffer[ii + 1] = (short) (tmpBuffer[i] & 0xffff);
                                buffer[ii + 2] = (short) (tmpBuffer[i + 1] & 0xffff);
                                buffer[ii + 3] = 0;
                            }
                        } else { // RRRRR GGGGG

                            short[] tmpBuffer = fileRW.getShortBuffer();
                            int bufferOffset = tmpBuffer.length / 2;

                            for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                buffer[ii] = 32767;
                                buffer[ii + 1] = (short) (tmpBuffer[i] & 0xffff);
                                buffer[ii + 2] = (short) (tmpBuffer[i + bufferOffset] & 0xffff);
                                buffer[ii + 3] = 0;
                            }
                        }
                    } // if (numColors == 2)
                    else if (numColors == 3) {
                        if (planarConfig == 0) { // RGB

                            short[] tmpBuffer = fileRW.getShortBuffer();

                            for (i = 0, ii = 0; i < tmpBuffer.length; i += 3, ii += 4) {
                                buffer[ii] = 32767;
                                buffer[ii + 1] = (short) (tmpBuffer[i] & 0xffff);
                                buffer[ii + 2] = (short) (tmpBuffer[i + 1] & 0xffff);
                                buffer[ii + 3] = (short) (tmpBuffer[i + 2] & 0xffff);
                            }
                        } else { // RRRRR GGGGG BBBBB

                            short[] tmpBuffer = fileRW.getShortBuffer();
                            int bufferOffset = tmpBuffer.length / 3;

                            for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                buffer[ii] = 32767;
                                buffer[ii + 1] = (short) (tmpBuffer[i] & 0xffff);
                                buffer[ii + 2] = (short) (tmpBuffer[i + bufferOffset] & 0xffff);
                                buffer[ii + 3] = (short) (tmpBuffer[i + (2 * bufferOffset)] & 0xffff);
                            }
                        }
                    } // else if (numColors == 3)
                    else { // numColors == 4
                        if (!RGBAOrder) { // ARGB order
                            if (planarConfig == 0) { // ARGB

                                short[] tmpBuffer = fileRW.getShortBuffer();

                                for (i = 0; i < tmpBuffer.length; i++) {
                                    buffer[i] = (short) (tmpBuffer[i] & 0xffff);
                                }
                            } else { // AAAA RRRRR GGGGG BBBBB

                                short[] tmpBuffer = fileRW.getShortBuffer();
                                int bufferOffset = tmpBuffer.length / 4;

                                for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                    buffer[ii] = (short) (tmpBuffer[i] & 0xffff);
                                    buffer[ii + 1] = (short) (tmpBuffer[i + bufferOffset] & 0xffff);
                                    buffer[ii + 2] = (short) (tmpBuffer[i + (2 * bufferOffset)] & 0xffff);
                                    buffer[ii + 3] = (short) (tmpBuffer[i + (3 * bufferOffset)] & 0xffff);
                                }
                            }
                        } // if (!RGBAOrder)
                        else { // RGBAOrder 
                            if (planarConfig == 0) { // RGBA

                                short[] tmpBuffer = fileRW.getShortBuffer();

                                for (i = 0; i < tmpBuffer.length; i += 4) {
                                    buffer[i] = (short) (tmpBuffer[i + 3] & 0xffff);
                                    buffer[i + 1] = (short) (tmpBuffer[i] & 0xffff);
                                    buffer[i + 2] = (short) (tmpBuffer[i + 1] & 0xffff);
                                    buffer[i + 3] = (short) (tmpBuffer[i + 2] & 0xffff);
                                }
                            } else { // RRRRR GGGGG BBBBB AAAAA

                                short[] tmpBuffer = fileRW.getShortBuffer();
                                int bufferOffset = tmpBuffer.length / 4;

                                for (i = 0, ii = 0; i < bufferOffset; i++, ii += 4) {
                                    buffer[ii] = (short) (tmpBuffer[i + (3 * bufferOffset)] & 0xffff);
                                    buffer[ii + 1] = (short) (tmpBuffer[i] & 0xffff);
                                    buffer[ii + 2] = (short) (tmpBuffer[i + bufferOffset] & 0xffff);
                                    buffer[ii + 3] = (short) (tmpBuffer[i + (2 * bufferOffset)] & 0xffff);
                                }
                            }
                        } // else RGBAOrder
                    } // else numColors == 4

                } catch (IOException error) {
                    throw error;
                }

                break;

            default:
                throw new IOException();
        }
    }

    /**
     * Sets the name and directory to new values opens the file.
     *
     * @param fileName DOCUMENT ME!
     * @param fileDir File directory.
     * @param fInfo Information that describes the image.
     * @param rwFlag Read/write flag.
     *
     * @exception IOException if there is an error making the files
     */
    public void setImageFile(String fileName, DICOMFile fInfo, int rwFlag) throws IOException {

        try {

            fileInfo = fInfo;
            file = new File(fileName);

            if (raFile != null) {

                try {
                    raFile.close();
                } catch (IOException ioex) {
                }
            }

            if (rwFlag == READ) {
                raFile = new RandomAccessFile(file, "r");
            } else if (rwFlag == READ_WRITE) {
                raFile = new RandomAccessFile(file, "rw");
            }

            this.fileName = fileName;
            this.fileRW = new FileRawChunk(raFile, fInfo);
        } catch (OutOfMemoryError error) {
            throw error;
        }
    }

    /**
     * Sets the planar configuration for RGB images.
     *
     * @param _planarConfig 0 indicates pixels are RGB, RGB chunky<br>
     * 1 indicates pixels are RRR, GGG, BBB planar
     */
    public void setPlanarConfig(int _planarConfig) {
        planarConfig = _planarConfig;
        fileRW.setPlanarConfig(_planarConfig);
    }

    /**
     * Sets the number of colors used in RGB files
     *
     * @param numColors
     */
    public void setNumColors(int numColors) {
        this.numColors = numColors;
        fileRW.setNumColors(numColors);
    }

    public void setRGBAOrder(boolean RGBAOrder) {
        this.RGBAOrder = RGBAOrder;
        fileRW.setRGBAOrder(RGBAOrder);
    }

    /**
     * DOCUMENT ME!
     *
     * @param startPosition starting byte position for writing data
     */
    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    /**
     * setZeroLengthFlag
     *
     * @param zeroLengthFlag
     */
    public void setZeroLengthFlag(boolean zeroLengthFlag) {
        this.zeroLengthFlag = zeroLengthFlag;
    }

}
