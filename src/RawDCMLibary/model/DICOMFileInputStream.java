package RawDCMLibary.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.InflaterInputStream;
import RawDCMLibary.DICOM.DICOMFile;
import static RawDCMLibary.DICOM.DICOMFile.FLOAT;
import static RawDCMLibary.DICOM.DICOMFile.SHORT;
import RawDCMLibary.DICOM.DICOM_Constants;
import RawDCMLibary.exceptions.DICOM_Exception;
import RawDCMLibary.model.FileDicomTagInfo.NumType;
import RawDCMLibary.model.FileDicomTagInfo.StringType;
import RawDCMLibary.model.FileDicomTagInfo.VR;
import static RawDCMLibary.model.FileInfoBase.*;
import RawDCMLibary.model.enums.Enums.Unit;
import RawDCMLibary.model.enums.Enums.VRtype;

/**
 * <p>
 * This class contains DICOM header information. It uses a table to store all
 * the information about the tags. The FileDicomTagTable listing all known tags
 * with empty values is in DicomDictionary. Also stored here is the offset of
 * the image, the image number, the resolutions, some pixel data, the bytes
 * allocated, and the dimensions of the image.</p>
 *
 * <p>
 * This file also contains a table used for displaying the tag information.
 * There is an option to display just the standard tags or both the standard and
 * the private tags.</p>
 *
 * @version 1.0 Aug 1, 1999
 * @see FileDicom
 * @see FileDicomTag
 * @see FileDicomTagTable
 * @see JDialogFileInfoDICOM
 */
public class DICOMFileInputStream extends FileDicomBase {

    //~ Static fields/initializers -------------------------------------------------------------------------------------
    /**
     * Use serialVersionUID for interoperability.
     *
     */
    /**
     * The tag marking the start of the image data.
     */
    public static final String IMAGE_TAG = "7F[0-9A-F][0-9A-F],0010";

    private static final long serialVersionUID = -3072660161266896186L;

    private final int UNDEFINED_LENGTH = -2;

    private FileInputStream inFileStream = null;

    private boolean endianess;

    /**
     * The currently known extents of the image, as indicated by the header
     */
    private int[] extents = new int[2];

    private boolean isEnhanced = false;

    /**
     * When Dicom image data is 'encapsulated,' it may be in pieces, or
     * 'fragments.' don't know quite why, or if pieces should be kept together.
     * If in fragments, the image data may span several slices, called a
     * 'frame.'
     */
    private boolean encapsulated = false;

    private boolean encapsulatedJP2 = false;

    private boolean isEnhanced4D = false;

    private int enhancedNumSlices;

    private int enhancedNumVolumes;
    /**
     * Header loop keeps executing when true
     */
    private boolean flag = true;

    private int metaGroupLength = 0;

    private int elementLength;

    /**
     * True if the DICOM image header has been read.
     */
    private boolean hasHeaderBeenRead = false;

    /**
     * Meta data structure in which to save all the DICOM tags.
     */
    private DICOMFile fileInfo;

    private int ID_OFFSET = 128;

    private final int FIRST_ELEMENT = 132;

    /**
     * Holds sequence of files described in DICOMDIR *
     */
    private FileDicomSQ dirInfo;
    /**
     * Buffer pointer (aka file pointer).
     */
    private int bPtr = 0;
    private int dataType;
    /**
     * JPEG compression may be lossy or lossless.
     */
    private boolean lossy = false;
    /**
     * Value Representation - see FileDicomTagInfo.
     */
    private byte[] vrBytes = new byte[2];
    /**
     * Whether adequate processing of the file has occurred to allowed image to
     * be extracted, this includes getting offset and pixel representation.
     */
    private boolean imageLoadReady = false;

    /**
     * First number (DICOM group) in ordered pair of numbers that uniquely
     * identifies a data element.
     */
    private int groupWord;

    /**
     * Second number (DICOM element in a group) in ordered pair of numbers that
     * uniquely identifies a data element.
     */
    private int elementWord;

    private FileDicomTagTable[] enhancedTagTables;
    private String nameSQ;

    private FileRaw rawFile;

    private byte[] bufferByte = null;

    private short[] bufferShort = null;

    private int[] bufferInt = null;

    private long[] bufferLong = null;

    private float[] bufferFloat = null;

    private double[] bufferDouble = null;

    private int bufferSize = 0;

    private BitSet bufferBitSet = null;

    private int compressionType = FileInfoBase.COMPRESSION_NONE;

    private InflaterInputStream inflaterStream;

    /**
     * Open file input stream for file.
     *
     * @param fileName name of the file to open
     *
     * @return true if sucessfully open file for reading
     */
    public boolean openForRead(final String fileName) {

        try {
            inFileStream = new FileInputStream(fileName);
            this.rawFile = new FileRaw(fileInfo);
        } catch (final FileNotFoundException e) {
            return (false);
        }

        return (true);
    }

    /**
     * Read file stream data into buffer.
     *
     * @param count number of bytes to read into buffer
     *
     * @return The actual number of bytes read.
     *
     * @throws DICOM_Exception Throws an exception if there was a problem
     * readiing in the data to the port.
     */
    public int readBinary(final int count) throws DICOM_Exception {
        super.tagBuffer = new byte[count];
        super.fLength = count;
        int actualNumOfByteRead = 0;

        try {
            actualNumOfByteRead = inFileStream.read(super.tagBuffer, 0, count);
        } catch (final IOException e) {
            close();
            throw new DICOM_Exception("DICOM_FileIO.readBinary( " + Arrays.toString(super.tagBuffer) + ", " + count + " ): " + e);
        }

        if (actualNumOfByteRead < 0) {
            close();
            throw new DICOM_Exception("DICOM_FileIO.readBinary( " + Arrays.toString(super.tagBuffer) + ", " + count + " ) = " + actualNumOfByteRead);
        }

        return (actualNumOfByteRead);
    }

    public boolean readHeader() throws IOException {

        endianess = FileBase.LITTLE_ENDIAN; // all DICOM files start as little endian (tags 0002)
        flag = true;
        int exceptionCount = 0;
        int maxExceptionCount = 10;

        metaGroupLength = 0;
        elementLength = 0;
        fileInfo.setEndianess(endianess);

        skipBytes(ID_OFFSET); // Find "DICM" tag

        // In v. 3.0, within the ID_OFFSET is general header information that
        // is not encoded into data elements and not present in DICOM v. 2.0.
        // However, it is optional.
        if (!getString(4).equals("DICM")) {
            fileInfo.containsDICM = false;
            seek(0); // set file pointer to zero
        }

        fileInfo.setDataType(SHORT); // Default file type

        tagTable = fileInfo.getTagTable();
        while (flag == true) {
            if (fileInfo.containsDICM) {
                // endianess is defined in a tag and set here, after the transfer
                // syntax group has been read in
                if (getFilePointer() >= (ID_OFFSET + 4 + metaGroupLength)) {
                    endianess = fileInfo.isEndianess();
                }
            } else {
                if (getFilePointer() >= metaGroupLength) {
                    endianess = fileInfo.isEndianess();
                }
            }
            FileDicomKey key = null;
            int tagElementLength = 0;
            try {
                key = getNextTag(endianess);
                tagElementLength = elementLength;
            } catch (ArrayIndexOutOfBoundsException aie) {
                System.err.println("Reached end of file while attempting to read: " + getFilePointer() + "\n");
                key = new FileDicomKey("7FE0,0010"); //process image tag
                vrBytes = new byte[]{'O', 'W'};
                int imageLoc = locateImageTag(0, numEmbeddedImages);
                seek(imageLoc);
            }
            int bPtrOld = getFilePointer();
            try {
                flag = processNextTag(tagTable, key, endianess, false);
                if (flag == false && imageLoadReady == false) {
                    System.err.println("Error parsing tag: " + key + "\n");
                    break;
                }
            } catch (IOException | CloneNotSupportedException e) {
                System.err.println("Error parsing tag: " + key + "\n");
                exceptionCount++;
                // Prevent infinite looping
                if (exceptionCount >= maxExceptionCount) {
                    break;
                }
            }
            if (bPtrOld + tagElementLength != getFilePointer()) {
                System.err.println("Possible invalid tag length specified, processing and tag lengths do not agree.");
            }
            if (tagElementLength != -1 && bPtrOld + tagElementLength > getFilePointer()) {
                seek(bPtrOld + tagElementLength); //processing tag was likely not successful, report error but continue parsing
                System.err.println("Skipping tag due to file corruption (or image tag reached): " + key + "\n");
            }

            if (getFilePointer() >= fLength || (elementLength == -1 && key.toString().matches(IMAGE_TAG))) { // for dicom files that contain no image information, the image tag will never be encountered
                int imageLoc = locateImageTag(0, numEmbeddedImages);
                if (!notDir) { // Done reading tags, if DICOMDIR then don't do anything else
                    flag = false;
                } else if (imageLoc != -1 && !imageLoadReady) {
                    seek(imageLoc);
                    flag = true; //image tag exists but has not been processed yet
                } else {
                    flag = false;
                }
            }
        }

        if (notDir) {
            hasHeaderBeenRead = true;
            return true;
        } else {
            return true;
        }
    }

    private boolean processImageData(int[] extents2, int imageNumber, int imageTagLoc) throws IOException {
        if (imageTagLoc != locateImageTag(0, numEmbeddedImages)) {

        }

        fileInfo.setInfoFromTags();
        final int imageLength = extents[0] * extents[1] * fileInfo.bitsAllocated / 8;

        if (fileInfo.getModality() == FileInfoBase.POSITRON_EMISSION_TOMOGRAPHY) {
            fileInfo.displayType = FLOAT;
        } else if (fileInfo.displayType == -1) { //if displayType has not been set
            fileInfo.displayType = fileInfo.getDataType();
        }

        if (!encapsulated) {
            if (fileInfo.getVr_type() == VRtype.IMPLICIT) {
                fileInfo.setImageOffset(imageTagLoc - 4 > 0 ? imageTagLoc - 4 : imageTagLoc); // no image length, subtract 4
            } // for explicit tags - see Part 5 page 27 1998
            else {
                fileInfo.setImageOffset(imageTagLoc);
            }
        } else { // encapsulated
            fileInfo.setImageOffset(imageTagLoc - 12 > 0 ? imageTagLoc - 12 : imageTagLoc);
        }
        if (extents[0] == 0 || extents[1] == 0) {
            extents = guessImageLength(extents);
        }

        seek(fileInfo.getImageOffset());

        fileInfo.setExtents(extents);

        imageLoadReady = true;
        return !imageLoadReady;

    }

    /**
     * Helper method for dicom files that do not specify a valid extents
     */
    private int[] guessImageLength(int[] extents) throws IOException {
        int possImageLength = (int) ((this.rawFile.getRaFile().length() - fileInfo.getImageOffset()) * (fileInfo.bytesPerPixel));
        if (possImageLength % ((int) Math.sqrt(possImageLength)) == 0) { //most likely for squares unless enhanced dicom and no extents have been found
            extents[0] = (int) Math.sqrt(possImageLength);
            extents[1] = extents[0];
        } else {
            ArrayList<Integer> factor = new ArrayList<Integer>();
            sqSearch:
            for (int i = possImageLength - 1; i > 1; i--) {
                if (possImageLength % i == 0) {
                    factor.add(i);
                    if (possImageLength / i == i) { //located square 3D sequence
                        extents[0] = i;
                        extents[1] = i;
                        break sqSearch;
                    }
                }
            }
            if (extents[0] == 0 || extents[1] == 0) { //no square factors found, so just use middle divisors
                if (factor.size() > 1) {
                    int middle = factor.size() / 2;
                    extents[0] = factor.get(middle - 1);
                    extents[1] = factor.get(middle);
                } else { //no factors found, so just use image length and 1
                    extents[0] = possImageLength;
                    extents[1] = 1;
                }
            }
        }
        return extents;
    }

    private FileDicomKey getNextTag(boolean endianess) throws IOException {
        // ******* Gets the next element
        getNextElement(endianess); // gets group, element, length
        final String name = convertGroupElement(groupWord, elementWord);
        final FileDicomKey key = new FileDicomKey(name);
        return key;
    }

    /**
     * Converts the integer values of the group word and element word into a
     * string that is the hexadecimal representation of group word and element
     * word, separated by a comma.
     *
     * @param groupWord The group word of the element.
     * @param elementWord The element word of the element.
     *
     * @return String representation of the group element.
     */
    private String convertGroupElement(final int groupWord, final int elementWord) {
        String first, second;

        first = Integer.toString(groupWord, 16);

        while (first.length() < 4) { // prepend with '0' as needed
            first = "0" + first;
        }

        first = first.toUpperCase();
        second = Integer.toString(elementWord, 16);

        while (second.length() < 4) { // prepend with '0' as needed
            second = "0" + second;
        }

        second = second.toUpperCase();

        return (first + "," + second); // name is the hex string of the tag
    }

    /**
     * Closes both the input and output streams if not null.
     */
    public void close() {

        if (inFileStream != null) {
            try {
                inFileStream.close();
            } catch (final IOException e) {
            }
        }

    }

    /**
     * Increments the location, then reads the elementWord, groupWord, and
     * elementLength. It also tests for an end of file and resets the
     * elementWord if it encounters one.
     *
     * @param bigEndian <code>true</code> indicates big endian byte order,
     * <code>false</code> indicates little endian.
     *
     * @throws IOException DOCUMENT ME!
     */
    private void getNextElement(final boolean bigEndian) throws IOException {

        groupWord = getUnsignedShort(bigEndian);
        elementWord = getUnsignedShort(bigEndian);
        // Preferences.debug("(just found: )"+Integer.toString(groupWord, 0x10) + ":"+Integer.toString(elementWord,
        // 0x10)+" - " , Preferences.DEBUG_FILEIO); System.err.print("( just found: ) "+ Integer.toString(groupWord, 0x10) +
        // ":"+Integer.toString(elementWord, 0x10)+ " - ");

        if (fileInfo.getVr_type() == VRtype.EXPLICIT) {

            /*
             * explicit tags carry an extra 4 bytes after the tag (group, element) information to describe the type of
             * tag. the element dictionary describes this info, so we skip past it here. (apr 2004)
             */
            final String tagname = convertGroupElement(groupWord, elementWord);

            if (tagname.equals(DICOMFile.SEQ_ITEM_BEGIN) || tagname.equals(DICOMFile.SEQ_ITEM_END)
                    || tagname.equals(DICOMFile.SEQ_ITEM_UNDEF_END) || tagname.equals("FFFE,EEEE")) // reserved
            {
                elementLength = getInt(bigEndian);
            } else {
                read(byteBuffer4); // Reads the explicit VR and following two bytes.
                elementLength = getLength(bigEndian, byteBuffer4[0], byteBuffer4[1], byteBuffer4[2], byteBuffer4[3]);
                // Preferences.debug(" length " + Integer.toString(elementLength, 0x10) + "\n", Preferences.DEBUG_FILEIO);
            }
        } else {

            // either IMPLICIT or group element is not SEQ_ITEM_BEGIN
            read(byteBuffer4);
            elementLength = getLength(bigEndian, byteBuffer4[0], byteBuffer4[1], byteBuffer4[2], byteBuffer4[3]);
        }
    }

    private int getLength(final boolean endianess, final byte b1, final byte b2, final byte b3, final byte b4)
            throws IOException {
        boolean implicit = false;

        if ((fileInfo.getVr_type() == VRtype.IMPLICIT) || (groupWord == 2)) {

            if (fileInfo.containsDICM) {

                // at this point transfer syntax not read; we know endianess
                // is little endian but vr may be explicit
                if ((getFilePointer() <= (FIRST_ELEMENT + metaGroupLength)) || (groupWord == 2)) {

                    if (((b1 < 65) || (b1 > 90)) && ((b2 < 65) || (b2 > 90))) {
                        implicit = true;
                    } else {
                        implicit = false;
                    }
                } else {
                    implicit = true; // transfer syntax has been read, implicit set
                }
            } else {

                // at this point transfer syntax not read; we know endianess
                // is little endian but vr may be explicit
                if ((getFilePointer() <= metaGroupLength) || (groupWord == 2)) {

                    if (((b1 < 65) || (b1 > 90)) && ((b2 < 65) || (b2 > 90))) {
                        implicit = true;
                    } else {
                        implicit = false;
                    }
                } else {
                    implicit = true; // transfer syntax has been read, implicit set
                }
            }
        }

        // displays the individual bytes. It could be better, for instance
        // printing as individual integer values:
        // System.err.print("[ "+Integer.toString(b1, 0x10)+" " +
        // Integer.toString(b2, 0x10)+" " +
        // Integer.toString(b3, 0x10)+" " +
        // Integer.toString(b4, 0x10)+" ]"
        // );
        if (implicit) {

            // implicit VR with 32-bit length
            if (endianess == FileBase.LITTLE_ENDIAN) {
                return ((b1 & 0xff) + ((b2 & 0xff) << 8) + ((b3 & 0xff) << 16) + ((b4 & 0xff) << 24));
            } else {
                return (((b1 & 0xff) << 24) + ((b2 & 0xff) << 16) + ((b3 & 0xff) << 8) + (b4 & 0xff));
            }
        } // explicit VR with 32-bit length
        else if (((b1 == 79) && (b2 == 66)) || ((b1 == 79) && (b2 == 87)) || ((b1 == 83) && (b2 == 81))
                || ((b1 == 85) && (b2 == 78)) || ((b1 == 85) && (b2 == 84))) {

            // VR = 'OB', or 'OW' or 'SQ' or 'UN' or 'UT'
            vrBytes[0] = b1;
            vrBytes[1] = b2;
            fileInfo.isCurrentTagSQ = new String(vrBytes).equals("SQ");

            // SQ - check for length FFFFFFFF (undefined), otherwise should be 0.
            if ((b1 == 83) && (b2 == 81)) { // 'SQ'

                // but i can't figure out why we're making a big deal out
                // of SQ types; UNDEF_LENGTH -is- -1 which -is- FF FF FF FF.
                // maybe ensuring return type?
                read(byteBuffer4); // reads 4 byte length w/o endianess for testing

                if ((byteBuffer4[0] == 255) && (byteBuffer4[1] == 255) && (byteBuffer4[2] == 255)
                        && (byteBuffer4[3] == 255)) {
                    return UNDEFINED_LENGTH;
                } else {
                    seek(getFilePointer() - 0x4);

                    return getInt(endianess); // rereads length using proper endianess
                }
            } else {
                return getInt(endianess);
            }
        } // explicit VR with 16-bit length
        else {
            vrBytes[0] = b1; // these are not VR for item tags!!!!!!!!!
            vrBytes[1] = b2;

            fileInfo.isCurrentTagSQ = new String(vrBytes).equals("SQ");

            if (endianess == FileBase.LITTLE_ENDIAN) {
                return ((b3 & 0xff) | ((b4 & 0xff) << 8));
            } else {
                return (((b3 & 0xff) << 8) | (b4 & 0xff));
            }
        }
    }

    private void setModality(int modalityFromDicomStr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * (0028,0103) Pixel Representations
     */
    public static final int UNDEFINED_PIXEL_REP = -1;

    /**
     * (0028,0103) Pixel Representations
     */
    public static final int UNSIGNED_PIXEL_REP = 0;

    /**
     * (0028,0103) Pixel Representations
     */
    public static final int SIGNED_PIXEL_REP = 1;

    //~ Instance fields ------------------------------------------------------------------------------------------------
    /**
     * Bits allocated per pixel.
     */
    public short bitsAllocated = -1;

    /**
     * Number of bytes per pixel.
     */
    public short bytesPerPixel = 1;

    /**
     * True if the DICOM file has the ID DICM at beginning at file pointer 128.
     */
    public boolean containsDICM = true;

    /**
     * Data type.
     */
    public int displayType;

    /**
     * DICOM instance number.
     */
    public int instanceNumber = -1;

    /**
     * Whether the tag currently being processed and read in is a sequence tag.
     */
    public boolean isCurrentTagSQ = false;

    /**
     * True indicates image has multiple image planes in a single file.
     */
    public boolean multiFrame = false;

    // Most of this variables are used to simplify access to important tags. Should
    // NOT be used when writing images to file since some of the info may not
    // be up to data. Might clean this up at some time in the future.
    /**
     * True is image format < DICOM 3.0.
     */
    public boolean olderVersion = false; // an older version is a version < DICOM v3.0

    /**
     * Orientation - SAGITTAL, CORONAL, AXIAL.
     */
    public String orientation;

    /**
     * Type of image - color, monochrome etc.
     */
    public String photometricInterp = "MONOCHROME2";

    /**
     * Pixel Padding Value (0028, 0120). Value of pixels added to
     * non-rectangular image to pad to rectangular format.
     */
    public Short pixelPaddingValue = null;

    /**
     * DICOM tag (0028, 0103) -1 = undefined 0 = unsigned 1 = signed
     */
    public short pixelRepresentation = UNDEFINED_PIXEL_REP;

    /**
     * 0 = RGB, RGB, 1 = R R R, G G G, B B B.
     */
    public short planarConfig = 0; //

    /**
     * DICOM slice location.
     */
    public float sliceLocation;

    /**
     * VR type can be IMPLICIT or EXPLICIT.
     */
    private VRtype vr_type = VRtype.IMPLICIT;

    /**
     * DICOM x coordianate of the slice location.
     */
    public float xLocation = 0;

    /**
     * DICOM y coordianate of the slice location.
     */
    public float yLocation = 0;

    /**
     * DICOM z coordianate of the slice location.
     */
    public float zLocation = 0;

    /**
     * Stores all the information about the DICOM tags.
     */
    private FileDicomTagTable tagTable;

    /**
     * whether it is enhanced dicom or not *
     */
    private boolean isEnhancedDicom = false;

    //~ Constructors ---------------------------------------------------------------------------------------------------
    public DICOMFileInputStream(DICOMFile fileInfo) {
        this.fileInfo = fileInfo;
    }

    //~ Methods --------------------------------------------------------------------------------------------------------
    /**
     * This method extracts directional cosines from the DICOM image header (tag
     * = "0020, 0037"). Since the third row of transformation matrix is not
     * given in the tag but can be derived by taking the cross product. The
     * result is then put into a MIPAV transformation matrix object and
     * returned.
     *
     * @return the transformation object extracted from the DICOM header. If
     * this tag is null then the class returns null.
     */
    public final void getPatientOrientation() {
        int index1, index2, index3, index4, index5, index6;
        int notSet = -1;
        double[][] dirCos = null;

        try {
            dirCos = new double[4][4]; // row, col

            String orientation = (String) tagTable.getValue("0020,0037");

            if (orientation == null) {

                //return null;
            }

            index1 = index2 = index3 = index4 = index5 = index6 = notSet;
            for (int i = 0; i < orientation.length(); i++) {

                if (orientation.charAt(i) == '\\') {

                    if (index1 == notSet) {
                        index1 = i;
                    } else if (index2 == notSet) {
                        index2 = i;
                    } else if (index3 == notSet) {
                        index3 = i;
                    } else if (index4 == notSet) {
                        index4 = i;
                    } else if (index5 == notSet) {
                        index5 = i;
                    } else {
                        index6 = i;
                        break;
                    }
                }
            }

            dirCos[0][0] = Double.valueOf(orientation.substring(0, index1)).doubleValue();
            dirCos[0][1] = Double.valueOf(orientation.substring(index1 + 1, index2)).doubleValue();
            dirCos[0][2] = Double.valueOf(orientation.substring(index2 + 1, index3)).doubleValue();
            dirCos[0][3] = 0;

            dirCos[1][0] = Double.valueOf(orientation.substring(index3 + 1, index4)).doubleValue();
            dirCos[1][1] = Double.valueOf(orientation.substring(index4 + 1, index5)).doubleValue();
            if (index6 == notSet) {
                dirCos[1][2] = Double.valueOf(orientation.substring(index5 + 1)).doubleValue();
            } else {
                dirCos[1][2] = Double.valueOf(orientation.substring(index5 + 1, index6)).doubleValue();
            }

            dirCos[1][3] = 0;

            // cross product
            dirCos[2][0] = (dirCos[0][1] * dirCos[1][2]) - (dirCos[0][2] * dirCos[1][1]);
            dirCos[2][1] = (dirCos[0][2] * dirCos[1][0]) - (dirCos[0][0] * dirCos[1][2]);
            dirCos[2][2] = (dirCos[0][0] * dirCos[1][1]) - (dirCos[0][1] * dirCos[1][0]);
            dirCos[2][3] = 0;

            dirCos[3][0] = 0;
            dirCos[3][1] = 0;
            dirCos[3][2] = 0;
            dirCos[3][3] = 1;

        } catch (OutOfMemoryError error) {
            dirCos = null;
            System.gc();
        }
    }

    /**
     * Returns a reference to the tag table for this dicom file info.
     *
     * @return A reference to the tag table.
     */
    public final FileDicomTagTable getTagTable() {
        return tagTable;
    }

    /**
     * @param tagTable the tagTable to set, useful when a tag table has been
     * populated from an enhanced DICOM series.
     */
    public final void setTagTable(FileDicomTagTable tagTable) {
        this.tagTable = tagTable;
    }

    /**
     * Accessor for the status of this dicom info.
     *
     * @return boolean <code>true</code> for images that think they are
     * multi-frame.
     */
    public final boolean isMultiFrame() {
        return multiFrame;
    }

    /**
     * Sets whether the DICOM image is a multiFrame image.
     */
    public final void setMultiFrame(boolean multiFrame) {
        this.multiFrame = multiFrame;
    }

    /**
     * Parse the string for Objects seperated by "\". Uses the local method
     * getValue to look at the Object held in the tag and decipher it for the
     * user class into an array of strings.
     *
     * <p>
     * parseTagValue has not been updated for the newer versions of DicomTag</p>
     *
     * @param tagName The name given as the key to search the Hashtable for.
     *
     * @return the array of Strings that were coded into the tag; a single
     * string is given if the string in the tag has no "\" seporators. Each
     * String in the array is a single value, and there are value multiplicity
     * number of Strings in the array. NOTE: user class must convert this list
     * to the correct type; (which does indicate user class knows what returned
     * string is) If null then there were zero tokens in the tag
     */
    public final String[] parseTagValue(String tagName) {

        if (tagName == null) {
            return null;
        }

        String str = (String) tagTable.getValue(tagName);

        if (str == null) {
            return null;
        }

        StringTokenizer strTokeniser = new StringTokenizer(str, "\\");
        int i = 0;
        String[] tokenList;

        if (strTokeniser.countTokens() == 0) {
            tokenList = null;
        } else {
            tokenList = new String[strTokeniser.countTokens()];

            while (strTokeniser.hasMoreTokens()) {
                tokenList[i] = new String(strTokeniser.nextToken());
                i++;
            }
        }

        return tokenList;
    }

    /**
     * In anonymization cases it may be necessary to hide the fact that MIPAV
     * has processed this image (to remove any NIH affiliation). This method
     * removes those tags that identify any secondary capture device information
     * contained within the image, to make it appear as if the image has not
     * been processed.
     *
     * @param fileInfo File info structure to set.
     */
    public void removeStampSecondaryCapture() {
        //remove (0018,1012): Date of Secondary Capture
        getTagTable().removeTag("0018,1012");

        // remove (0018,1014): Time of Secondary Capture
        getTagTable().removeTag("0018,1014");

        // remove (0018,1016): Secondary Capture Device manufacturer
        getTagTable().removeTag("0018,1016");

        // remove (0018,1018): Secondary Capture Device Manufacturer's Model Name
        getTagTable().removeTag("0018,1018");

        // remove (0018,1019): Secondary Capture Device Software Version(s)
        getTagTable().removeTag("0018,1019");
    }

    private void updateLengthTags(HashMap<Integer, LengthStorageUnit> lengthComp) {
        Iterator<Integer> itr = lengthComp.keySet().iterator();
        while (itr.hasNext()) {
            int group = itr.next();
            try {
                Integer length = (Integer) tagTable.get(new FileDicomKey(group, 0)).getValue(false);
                if (length.intValue() != lengthComp.get(group).get()) {
                    System.err.println("Computed group: " + Integer.toHexString(group) + " length does not agree with stored value.\n");
                }
                tagTable.get(new FileDicomKey(group, 0)).setValue(new Integer(lengthComp.get(group).get()));

            } catch (NullPointerException e) {
                //A length for this group does not exist, this is allowable in DICOM.
            }
        }
    }

    private void appendLengthTag(FileDicomTag tag, HashMap<Integer, LengthStorageUnit> lengthComp) {
        LengthStorageUnit length = null;
        int group = tag.getGroup();
        if ((length = lengthComp.get(group)) == null) {
            lengthComp.put(group, length = new LengthStorageUnit(0));
        }
        if (tag.getValueRepresentation() != VR.SQ) {
            length.add(tag.getDataLength());
        } else {
            length.add(((FileDicomSQ) tag.getValue(false)).getDataLength());
        }
        if (vr_type == VRtype.EXPLICIT) {
            if (tag.getType().reservedBytes()) {
                length.add(2); //include reserved bytes
                length.add(4); //include 4 bytes for length
            } else {
                length.add(2); //include 2 bytes for length
            }
            length.add(2); //include vr bytes
        } else {
            length.add(4); //include 4 bytes for length
        }
        length.add(2); //include group bytes
        length.add(2); //include element bytes

    }

    /**
     * This class is a basic storage unit for a primitive integer variable. By
     * creating this storage unit, modified map values that are stored using
     * this method only need to be accessed once.
     *
     * @author senseneyj
     *
     */
    class LengthStorageUnit {

        /**
         * Internal integer value stored by this unit.
         */
        private int value;

        /**
         * Creates a storage wrapper for a primitive integer variable.
         */
        public LengthStorageUnit(int i) {
            this.value = i;
        }

        /**
         * Adds the parameter i to the stored value.
         */
        public void add(int i) {
            value += i;
        }

        /**
         * Sets the stored value of this storage unit to i.
         */
        public void set(int i) {
            value = i;
        }

        /**
         * Gets the stored value of this storage unit to i.
         */
        public int get() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    /**
     * Changes a few dicom tags associated with a secondary capture image
     * (marking it as having been created with mipav). This is often used after
     * a new image is created as the result of an algorithm and the source
     * image's file infos are copied over to the new image.
     */
    public void setSecondaryCaptureTags() {

        try {
            // Secondary Capture SOP UID
            getTagTable().setValue("0002,0002", "1.2.840.10008.5.1.4.1.1.7 ", 26);
            getTagTable().setValue("0008,0016", "1.2.840.10008.5.1.4.1.1.7 ", 26);

            // bogus Implementation UID made up by Matt
            getTagTable().setValue("0002,0012", "1.2.840.34379.17", 16);
            getTagTable().setValue("0002,0013", "MIPAV--NIH", 10);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(DICOMFileInputStream.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns the MIPAV modality constant for a given DICOM modality string.
     *
     * @param value The DICOM modality tag value.
     *
     * @return The equivalent MIPAV modality constant (or UNKNOWN_MODALITY if
     * there is no equivalent found).
     */
    protected static final int getModalityFromDicomStr(String value) {

        if (value.equals("BI")) {
            return BIOMAGNETIC_IMAGING;
        } else if (value.equals("CD")) {
            return COLOR_FLOW_DOPPLER;
        } else if (value.equals("CR")) {
            return COMPUTED_RADIOGRAPHY;
        } else if (value.equals("CT")) {
            return COMPUTED_TOMOGRAPHY;
        } else if (value.equals("DD")) {
            return DUPLEX_DOPPLER;
        } else if (value.equals("DG")) {
            return DIAPHANOGRAPHY;
        } else if (value.equals("DX")) {
            return DIGITAL_RADIOGRAPHY;
        } else if (value.equals("ES")) {
            return ENDOSCOPY;
        } else if (value.equals("GM")) {
            return GENERAL_MICROSCOPY;
        } else if (value.equals("HC")) {
            return HARDCOPY;
        } else if (value.equals("IO")) {
            return INTRAORAL_RADIOGRAPHY;
        } else if (value.equals("LS")) {
            return LASER_SURFACE_SCAN;
        } else if (value.equals("MA")) {
            return MAGNETIC_RESONANCE_ANGIOGRAPHY;
        } else if (value.equals("MG")) {
            return MAMMOGRAPHY;
        } else if (value.equals("MR")) {
            return MAGNETIC_RESONANCE;
        } else if (value.equals("MS")) {
            return MAGNETIC_RESONANCE_SPECTROSCOPY;
        } else if (value.equals("NM")) {
            return NUCLEAR_MEDICINE;
        } else if (value.equals("OT")) {
            return OTHER;
        } else if (value.equals("PT")) {
            return POSITRON_EMISSION_TOMOGRAPHY;
        } else if (value.equals("PX")) {
            return PANORAMIC_XRAY;
        } else if (value.equals("RF")) {
            return RADIO_FLUOROSCOPY;
        } else if (value.equals("RG")) {
            return RADIOGRAPHIC_IMAGING;
        } else if (value.equals("RTDOSE")) {
            return RADIOTHERAPY_DOSE;
        } else if (value.equals("RTIMAGE")) {
            return RADIOTHERAPY_IMAGE;
        } else if (value.equals("RTPLAN")) {
            return RADIOTHERAPY_PLAN;
        } else if (value.equals("RTRECORD")) {
            return RADIOTHERAPY_RECORD;
        } else if (value.equals("RTSTRUCT")) {
            return RADIOTHERAPY_STRUCTURE_SET;
        } else if (value.equals("SM")) {
            return SLIDE_MICROSCOPY;
        } else if (value.equals("ST")) {
            return SINGLE_PHOTON_EMISSION_COMPUTED_TOMOGRAPHY;
        } else if (value.equals("TG")) {
            return THERMOGRAPHY;
        } else if (value.equals("US")) {
            return ULTRASOUND;
        } else if (value.equals("XA")) {
            return XRAY_ANGIOGRAPHY;
        } else if (value.equals("XC")) {
            return EXTERNAL_CAMERA_PHOTOGRAPHY;
        } else {
            return UNKNOWN_MODALITY;
        }
    }

    public boolean isEnhancedDicom() {
        return isEnhancedDicom;
    }

    public void setIsEnhancedDicom(boolean isEnhancedDicom) {
        this.isEnhancedDicom = isEnhancedDicom;
    }

    /**
     * Uses the DICOM tag value to set the Image modality field.
     *
     * @param value Object used is the value of DICOM tag (0008,0060)
     */
    protected final void setModalityFromDicomStr(String value) {
        setModality(getModalityFromDicomStr(value));
    }

    public VRtype getVr_type() {
        return vr_type;
    }

    public void setVr_type(VRtype vr_type) {
        this.vr_type = vr_type;
    }

    /**
     *
     * @param tagTable The tag table where this key will be stored
     * @param key The key that is being processed
     * @param endianess
     * @return
     * @throws IOException
     */
    private boolean processNextTag(FileDicomTagTable tagTable, FileDicomKey key, boolean endianess, boolean inSequence) throws IOException, CloneNotSupportedException {
        String strValue = null;
        Object data = null;
        VR vr = null; // value representation of data
        String name = key.toString(); // string representing the tag
        int tagVM;

        if ((fileInfo.getVr_type() == VRtype.IMPLICIT) || (groupWord == 2)) {

            // implicit VR means VR is based on tag as defined in dictionary
            vr = DicomDictionary.getType(key);
            tagVM = DicomDictionary.getVM(key);

            // the tag was not found in the dictionary..
            if (vr == null) {
                if (Integer.parseInt(key.getElement(), 16) == 0) {
                    vr = VR.UL;
                } else {
                    vr = VR.UN;
                    tagVM = 1;
                }
            }
        } else { // Explicit VR  
            try {
                vr = VR.valueOf(new String(vrBytes));
            } catch (Exception e) {

            } finally {
                if (key.toString().matches(DICOMFile.IMAGE_TAG)) {
                    if (!key.getGroup().equals("7FDF")) { //defunct scanner companies use this as another private group sometimes
                        vr = VR.OB;
                    }
                } else if ((vr == VR.UN || vr == VR.XX || vr == null) && new DicomDictionary().containsTag(key)) {
                    vr = new DicomDictionary().getType(key);
                } else if (vr == null) {
                    vr = VR.UN;

                }
            }

            if (!new DicomDictionary().containsTag(key)) {
                tagVM = 1;
                if (vr.getType() instanceof FileDicomTagInfo.NumType) {
                    tagVM = elementLength / ((NumType) vr.getType()).getNumBytes();
                }

                //if (isSiemensMRI && name.startsWith("0019")) {
                //    processSiemensMRITag(name, key, vr, tagVM, tagTable);
                //} else if (isSiemensMRI2 && name.startsWith("0051")) {
                //    processSiemensMRI2Tag(name, key, vr, tagVM, tagTable);
                //} else {
                // put private tags with explicit VRs in file info hashtable
                tagTable.putPrivateTagValue(new FileDicomTagInfo(key, vr, tagVM, "PrivateTag",
                        "Private Tag"));

            } else {
                final FileDicomTagInfo info = new DicomDictionary().getInfo(key);
                // this is required if DicomDictionary contains wild card characters
                info.setKey(key);
                tagTable.putPrivateTagValue(info);
                tagVM = info.getValueMultiplicity();
                tagTable.get(key).setValueRepresentation(vr);

            }
        }

        if ((elementWord == 0) && (elementLength == 0)) { // End of file
            throw new IOException("Error while reading header");
        }

        try {

            if (vr.getType().equals(StringType.STRING) || vr.getType().equals(StringType.DATE)) {
                strValue = getString(elementLength);
                tagTable.setValue(key, strValue, elementLength);
            }

//            if (!isSiemensMRI && name.startsWith("0019")) {
//                if (name.equals("0019,0010") && strValue != null && strValue.trim().equals("SIEMENS MR HEADER")) {
//                    isSiemensMRI = true;
//                }
//            } else if (!isSiemensMRI2 && name.startsWith("0051")) {
//                if (name.equals("0051,0010") && strValue != null && strValue.trim().equals("SIEMENS MR HEADER")) {
//                    isSiemensMRI2 = true;
//                }
//            }
            switch (vr) {
                case AT:
                    int groupWord = getUnsignedShort(fileInfo.isEndianess());
                    int elementWord = getUnsignedShort(fileInfo.isEndianess());
                    FileDicomKey innerKey = new FileDicomKey(groupWord, elementWord);
                    tagTable.setValue(key, innerKey, elementLength);
                    break;
                case OW:
                    if (name.equals("0028,1201") || name.equals("0028,1202") || name.equals("0028,1203")) {
                        //return getColorPallete(tagTable, new FileDicomKey(name));  //for processing either red(1201), green(1202), or blue(1203)
                    }
                case OB:
                    if (name.matches(DICOMFile.IMAGE_TAG) && !inSequence) { //can be either OW or OB
                        return processImageData(extents, numEmbeddedImages, getFilePointer() + (fileInfo.getVr_type() == VRtype.IMPLICIT ? 4 : 0)); //finished reading image tags and all image data, get final image for display
                    }
                    data = getByte(tagVM, elementLength, endianess);
                    tagTable.setValue(key, data, elementLength);
                    break;
                case UN:
                    if (elementLength != -1) {
                        processUnknownVR(tagTable, strValue, key, tagVM, strValue);
                        break;
                    } //else is implicit sequence, so continue
                case SQ:
                    processSequence(tagTable, key, name, endianess);
                    if (flag == false) {
                        return false;
                    }
                    break;
            }

            if (vr.getType() instanceof NumType) {
                switch (((NumType) vr.getType())) {
                    case SHORT:
                        data = getShort(tagVM, elementLength, endianess);
                        break;
                    case LONG:
                        data = getInteger(tagVM, elementLength, endianess);
                        break;
                    case FLOAT:
                        data = getFloat(tagVM, elementLength, endianess);
                        break;
                    case DOUBLE:
                        data = getDouble(tagVM, elementLength, endianess);
                        break;
                }
                tagTable.setValue(key, data, elementLength);
            }
        } catch (final OutOfMemoryError e) {
            e.printStackTrace();
            throw new IOException();
        }

        if (name.equals("0002,0000")) { // length of the transfer syntax group
            if (data != null) {
                metaGroupLength = ((Integer) (data)).intValue() + 12; // 12 is the length of 0002,0000 tag
            }
        } else if (name.equals("0004,1220")) {
            notDir = false;
        } else if (name.equals("0002,0010")) {
            boolean supportedTransferSyntax = processTransferSyntax(strValue);
            if (!supportedTransferSyntax) {
                return false;
            }

        } else if (name.equals("0028,0010") && !inSequence) { // Set the extents, used for reading the image in FileInfoDicom's processTags
            extents[1] = ((Short) data).intValue();
            // fileInfo.columns = extents[1];
        } else if (name.equals("0028,0011") && !inSequence) {
            extents[0] = ((Short) data).intValue();
            // fileInfo.rows = extents[0];
        } else if (!isEnhanced && name.equals("0002,0002")) {                           // need to determine if this is enhanced dicom
            if (strValue.trim().equals(DICOM_Constants.UID_EnhancedMRStorage) // if it is, set up all the additional fileinfos needed and attach
                    || strValue.trim().equals(DICOM_Constants.UID_EnhancedCTStorage) // the childTagTables to the main tagTable
                    || strValue.trim().equals(DICOM_Constants.UID_EnhancedXAStorage)) {
                isEnhanced = true;
            }
        } else if (isEnhanced && name.equals("0028,0008")) {
            final int nImages = Integer.valueOf(strValue.trim()).intValue();
            fileInfo.setIsEnhancedDicom(true);
            if (nImages > 1) {
                enhancedTagTables = new FileDicomTagTable[nImages - 1];
            }
        }

        return true;
    }

    private boolean processTransferSyntax(String strValue) {
        // Transfer Syntax UID: DICOM part 10 page 13, part 5 p. 42-48, Part 6 p. 53
        // 1.2.840.10008.1.2 Implicit VR Little Endian (Default)
        // 1.2.840.10008.1.2.1 Explicit VR Little Endian
        // 1.2.840.10008.1.2.2 Explicit VR Big Endian
        // 1.2.840.10008.1.2.4.50 8-bit Lossy JPEG (JPEG Coding Process 1)
        // 1.2.840.10008.1.2.4.51 12-bit Lossy JPEG (JPEG Coding Process 4)
        // 1.2.840.10008.1.2.4.57 Lossless JPEG Non-hierarchical (JPEG Coding Process 14)
        // we should bounce out if we don't support this transfer syntax
        if (strValue.trim().equals("1.2.840.10008.1.2")) {
            fileInfo.setEndianess(FileBase.LITTLE_ENDIAN);
            fileInfo.setVr_type(VRtype.IMPLICIT);
            encapsulated = false;
        } else if (strValue.trim().equals("1.2.840.10008.1.2.1")) {
            fileInfo.setEndianess(FileBase.LITTLE_ENDIAN);
            fileInfo.setVr_type(VRtype.EXPLICIT);
            encapsulated = false;
        } else if (strValue.trim().equals("1.2.840.10008.1.2.2")) {
            fileInfo.setEndianess(FileBase.BIG_ENDIAN);
            fileInfo.setVr_type(VRtype.EXPLICIT);
            encapsulated = false;
        } else if (strValue.trim().startsWith("1.2.840.10008.1.2.4.")) { // JPEG
            fileInfo.setEndianess(FileBase.LITTLE_ENDIAN);
            fileInfo.setVr_type(VRtype.EXPLICIT);
            encapsulated = true;
            if (strValue.trim().equals(DICOM_Constants.UID_TransferJPEG2000LOSSLESS)) {
                encapsulatedJP2 = true;
            }

            if (strValue.trim().equals("1.2.840.10008.1.2.4.57")
                    || strValue.trim().equals("1.2.840.10008.1.2.4.58")
                    || strValue.trim().equals("1.2.840.10008.1.2.4.65")
                    || strValue.trim().equals("1.2.840.10008.1.2.4.66")
                    || strValue.trim().equals("1.2.840.10008.1.2.4.70")
                    || strValue.trim().equals("1.2.840.10008.1.2.4.90")) {
                lossy = false;
            } else {
                lossy = true;
            }
        } else {
            return false; // unable to process tags without recognized transfer syntax
        }
        return true;
    }

    private void processSequence(FileDicomTagTable tagTable, FileDicomKey key, String name, boolean endianess) throws IOException {
        final int len = elementLength;
        // save these values because they'll change as the sequence is read in below.
        FileDicomSQ sq;
        // ENHANCED DICOM per frame
        if (name.equals("5200,9230")) {
            isEnhanced = true;
            int numSlices = 0;
            sq = getSequence(endianess, len);
            final Vector<FileDicomSQItem> v = sq.getSequence();
            Iterator<FileDicomTag> itr = v.get(0).getTagList().values().iterator();
            TreeSet<Integer> sliceInt = new TreeSet<Integer>(); //keeps track of which slices have already been seen
            while (itr.hasNext()) { //put tags in base FileInfoDicom
                tagTable.put(itr.next());
            }
            numSlices = checkMaxSlice(tagTable, numSlices, sliceInt);
            for (int i = 1; i < v.size(); i++) { //each entire children tag table is just what's in v
                if (enhancedTagTables == null) {
                    flag = false;
                    return;
                }
                if (enhancedTagTables.length >= i) {
                    enhancedTagTables[i - 1] = v.get(i);
                } else {
                    flag = false;
                    return;
                }
                numSlices = checkMaxSlice(enhancedTagTables[i - 1], numSlices, sliceInt);
            }
            enhancedNumSlices = numSlices;

            // remove tag 5200,9230 if its there
            final FileDicomTag removeTag = tagTable.get(key);
            if (removeTag != null) {
                tagTable.removeTag(key);
            }
        } else {
            if (name.equals("0004,1220")) {
                dirInfo = getSequence(endianess, len);
                sq = new FileDicomSQ();
                sq.setWriteAsUnknownLength(len == -1);
            } else {
                sq = getSequence(endianess, len);

            }
            // System.err.print( "SEQUENCE DONE: Sequence Tags: (" + name + "); length = " +
            // Integer.toString(len, 0x10) + "\n");

            try {
                try {
                    tagTable.setValue(key, sq, elementLength);
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(DICOMFileInputStream.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (final NullPointerException e) {
            }

        }
    }

    /**
     * Helper method for enhanced dicom which finds the maximum slice number in
     * the dataset. Also determines whether the dataset is a 4D enhanced
     * dataset.
     *
     * @param tagTable2
     */
    private int checkMaxSlice(FileDicomTagTable tagTable, int numSlices, TreeSet<Integer> sliceInt) {
        FileDicomTag frameTag = tagTable.get("0020,9111");
        int currNum = 0;
        if (frameTag != null) {
            // OK...so we should be relying on 0020,9056 (Stack ID)
            // to tell us the number of volumes in the dataet
            // but we find that it is not being implemented in the dataset we have
            // Look at 0020.9057 (In-Stack Pos ID), to get numSlices since
            // these should all be unique for 1 volume. If we find that there
            // are duplicates, then that means we are dealing with a 4D datset
            // we will get the number of slices in a volumne. Then determine
            // number of volumes by taking total num slices / num slices per volume
            // ftp://medical.nema.org/medical/dicom/final/cp583_ft.pdf
            currNum = ((Number) ((FileDicomSQ) frameTag.getValue(false)).getItem(0).get("0020,9057").getValue(false)).intValue();
        }
        if (!isEnhanced4D) {
            isEnhanced4D = !sliceInt.add(currNum); //if slice already existed, sliceInt returns false, sets isEnhanced4D to true
        }
        if (currNum > numSlices) {
            numSlices = currNum;
            System.out.println("Found slice " + numSlices);
        }
        return numSlices;
    }

    private void processUnknownVR(FileDicomTagTable tagTable, String name, FileDicomKey key, int tagVM, String strValue) throws IOException {
        try {
            try {
                // set the value if the tag is in the dictionary (which means it isn't private..) or has already
                // been put into the tag table without a value (private tag with explicit vr)
                if (new DicomDictionary().containsTag(key) || tagTable.containsTag(key)) {
                    tagTable.setValue(key, readUnknownData(), elementLength);
                } else {
                    tagTable
                            .putPrivateTagValue(new FileDicomTagInfo(key, VR.UN, tagVM, "PrivateTag", "Private Tag"));

                    tagTable.setValue(key, readUnknownData(), elementLength);
                }
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(DICOMFileInputStream.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (final OutOfMemoryError e) {
            e.printStackTrace();
            throw new IOException("Out of memory storing unknown tags in FileDicom.readHeader");
        } catch (final NullPointerException npe) {
        }
    }

    /**
     * Reads a length of the data and deposits it into a single Short or an
     * array of Short as needed by the tag's VM.
     *
     * @return Object
     *
     * @throws IOException DOCUMENT ME!
     *
     * @param vm value multiplicity of the DICOM tag data. VM does not represent
     * how many to find.
     * @param length number of bytes to read out of data stream; the length is
     * not used.
     * @param endianess byte order indicator; here <code>true</code> indicates
     * big-endian and <code>false</code> indicates little-endian.
     */
    private Object getByte(final int vm, final int length, final boolean endianess) throws IOException {
        int len = (elementLength == UNDEFINED_LENGTH) ? 0 : elementLength;
        int i = 0;
        Object readObject = null; // the Object we read in

        if (vm > 1) {
            final Byte[] array = new Byte[length];

            while (len > 0) { // we should validate with VM here too
                array[i] = new Byte((byte) getByte());
                len -= 1;
                i++;
            }

            readObject = array;
        } else if ((vm < 1) && (length > 2)) {

            // not a valid VM, but we don't initialise the VM to 1,
            // so we will use this fact to guess at valid data.
            // we actually do it as above.
            final Byte[] array = new Byte[length];

            while (len > 0) { // we should validate with VM here too
                array[i] = new Byte((byte) getByte());
                len -= 1;
                i++;
            }

            readObject = array;
        } else if (length > 0) {
            final Byte[] array = new Byte[length];

            while (len > 0) { // we should validate with VM here too
                array[i] = new Byte((byte) getByte());
                len -= 1;
                i++;
            }

            readObject = array;
        }

        return readObject;
    }

    /**
     * Reads a length of the data and deposits it into a single Short or an
     * array of Short as needed by the tag's VM.
     *
     * @return Object
     *
     * @throws IOException DOCUMENT ME!
     *
     * @param vm value multiplicity of the DICOM tag data. VM does not represent
     * how many to find.
     * @param length number of bytes to read out of data stream; the length is
     * not used.
     * @param endianess byte order indicator; here <code>true</code> indicates
     * big-endian and <code>false</code> indicates little-endian.
     */
    private Object getShort(final int vm, final int length, final boolean endianess) throws IOException {
        int len = (elementLength == UNDEFINED_LENGTH) ? 0 : elementLength;
        int i = 0;
        Object readObject = null; // the Object we read in

        if (vm > 1) {
            final Short[] array = new Short[length / 2];

            while (len > 0) { // we should validate with VM here too
                array[i] = new Short((short) getUnsignedShort(endianess));
                len -= 2;
                i++;
            }

            readObject = array;
        } else if (((vm < 1) && (length > 2))) {

            // not a valid VM, but we don't initialise the VM to 1,
            // so we will use this fact to guess at valid data.
            // we actually do it as above.
            final Short[] array = new Short[length / 2];

            while (len > 0) { // we should validate with VM here too
                array[i] = new Short((short) getUnsignedShort(endianess));
                len -= 2;
                i++;
            }

            readObject = array;
        } else if (((vm == 1) && (length > 2))) {

            // not a valid VM, but we don't initialise the VM to 1,
            // so we will use this fact to guess at valid data.
            // we actually do it as above.
            readObject = new Short((short) getUnsignedShort(endianess));
            len -= 2;

            while (len > 0) { // we should validate with VM here too
                getUnsignedShort(endianess);
                len -= 2;
                i++;
            }
        } else if (length == 2) {
            readObject = new Short((short) getUnsignedShort(endianess));
        }

        return readObject;
    }

    /**
     * Gets private tags or other tags where the type is unknown; does not
     * change the data, so it may be written out correctly.
     *
     * @return A Byte array of length elementLength with the data stored in it.
     *
     * @throws IOException DOCUMENT ME!
     */
    private Object readUnknownData() throws IOException {
        byte[] bytesValue;
        Byte[] bytesV;

        if (elementLength < 0) {
            return null;
        }

        bytesValue = new byte[elementLength];
        read(bytesValue);
        bytesV = new Byte[elementLength];

        for (int k = 0; k < bytesValue.length; k++) {
            bytesV[k] = new Byte(bytesValue[k]);
        }

        return bytesV;
    }

    /**
     * Reads a length of the data and deposits it into a single Double or an
     * array of Double as needed by the tag's VM.
     *
     * @return Object
     *
     * @throws IOException DOCUMENT ME!
     *
     * @param vm value multiplicity of the DICOM tag data. VM does not represent
     * how many to find.
     * @param length number of bytes to read out of data stream; the length is
     * not used.
     * @param endianess byte order indicator; here <code>true</code> indicates
     * big-endian and <code>false</code> indicates little-endian.
     */
    private Object getDouble(final int vm, final int length, final boolean endianess) throws IOException {
        int len = (elementLength == UNDEFINED_LENGTH) ? 0 : elementLength;
        int i = 0;
        Object readObject = null;

        if (vm > 1) {
            final Double[] array = new Double[length / 8];

            while (len > 0) { // we should validate with VM here too
                array[i] = new Double(getDouble(endianess));
                len -= 8;
                i++;
            }

            readObject = array;
        } else if ((vm < 1) && (length > 8)) {

            // not a valid VM, but we don't initialise the VM to 1,
            // so we will use this fact to guess at valid data
            // we actually do it as above.
            final Double[] array = new Double[length / 8];

            while (len > 0) {
                array[i] = new Double(getDouble(endianess));
                len -= 8;
                i++;
            }

            readObject = array;
        } else if (((vm == 1) && (length > 8))) {

            // not a valid VM, but we don't initialise the VM to 1,
            // so we will use this fact to guess at valid data.
            // we actually do it as above.
            readObject = new Double(getDouble(endianess));
            len -= 8;

            while (len > 0) { // we should validate with VM here too
                getDouble(endianess);
                len -= 8;
                i++;
            }
        } else if (length == 8) {
            readObject = new Double(getDouble(endianess));
        }

        return readObject;
    }

    /**
     * Reads a length of the data and deposits it into a single Float or an
     * array of Float as needed by the tag's VM.
     *
     * @return Object
     *
     * @throws IOException DOCUMENT ME!
     *
     * @param vm value multiplicity of the DICOM tag data. VM does not represent
     * how many to find.
     * @param length number of bytes to read out of data stream; the length is
     * not used.
     * @param endianess byte order indicator; here <code>true</code> indicates
     * big-endian and <code>false</code> indicates little-endian.
     */
    private Object getFloat(final int vm, final int length, final boolean endianess) throws IOException {
        int len = (elementLength == UNDEFINED_LENGTH) ? 0 : elementLength;
        int i = 0;
        Object readObject = null;

        if (vm > 1) {
            final Float[] array = new Float[length / 4];

            while (len > 0) { // we should validate with VM here too
                array[i] = new Float(getFloat(endianess));
                len -= 4;
                i++;
            }

            readObject = array;
        } else if ((vm < 1) && (length > 4)) {

            // not a valid VM, but we don't initialise the VM to 1,
            // so we will use this fact to guess at valid data
            // we actually do it as above.
            final Float[] array = new Float[length / 4];

            while (len > 0) {
                array[i] = new Float(getFloat(endianess));
                len -= 4;
                i++;
            }

            readObject = array;
        } else if (((vm == 1) && (length > 4))) {

            // not a valid VM, but we don't initialise the VM to 1,
            // so we will use this fact to guess at valid data.
            // we actually do it as above.
            readObject = new Float(getFloat(endianess));
            len -= 4;

            while (len > 0) { // we should validate with VM here too
                getFloat(endianess);
                len -= 4;
                i++;
            }
        } else if (length == 4) {
            readObject = new Float(getFloat(endianess));
        }

        return readObject;
    }

    /**
     * Reads a length of the data and deposits it into a single Integer or an
     * array of Integer as needed by the tag's VM.
     *
     * @return Object
     *
     * @throws IOException DOCUMENT ME!
     *
     * @param vm value multiplicity of the DICOM tag data. VM does not represent
     * how many to find.
     * @param length number of bytes to read out of data stream; the length is
     * not used.
     * @param endianess byte order indicator; here <code>true</code> indicates
     * big-endian and <code>false</code> indicates little-endian.
     */
    private Object getInteger(final int vm, final int length, final boolean endianess) throws IOException {
        int len = (elementLength == UNDEFINED_LENGTH) ? 0 : elementLength;
        int i = 0;
        Object readObject = null;

        if (vm > 1) {
            final Integer[] array = new Integer[length / 4];

            while (len > 0) { // we should validate with VM here too
                array[i] = new Integer(getInt(endianess));
                len -= 4;
                i++;
            }

            readObject = array;
        } else if ((vm < 1) && (length > 4)) {

            // not a valid VM, but we don't initialise the VM to 1,
            // so we will use this fact to guess at valid data.
            // we actually do it as above.
            final Integer[] array = new Integer[length / 4];

            while (len > 0) {
                array[i] = new Integer(getInt(endianess));
                len -= 4;
                i++;
            }

            readObject = array;
        } else if (((vm == 1) && (length > 4))) {

            // not a valid VM, but we don't initialise the VM to 1,
            // so we will use this fact to guess at valid data.
            // we actually do it as above.
            readObject = new Integer(getInt(endianess));
            len -= 4;

            while (len > 0) { // we should validate with VM here too
                getInt(endianess);
                len -= 4;
                i++;
            }
        } else if (length == 4) {
            readObject = new Integer(getInt(endianess));
        }

        return readObject;
    }

    /**
     * Gets the sequence in a sequence tag. Sequences of items have special
     * encodings that are detailed in the DICOM standard. There is usually an
     * "item" tag, then a dataset encoded exactly like other tags, then a tag
     * indicating the end of the sequence.
     *
     * <P>
     * For further information see the DICOM Standard, Part 5, Section 7.
     * </P>
     *
     * @param endianess Big or little
     * @param seqLength Length of this sequence, although possibly left
     * undefined.
     *
     * @return A DicomSQ object which stores the new tags and their info
     *
     * @see DicomSQ
     */
    private FileDicomSQ getSequence(final boolean endianess, final int seqLength) throws IOException {
        final FileDicomSQ sq = new FileDicomSQ();
        sq.setWriteAsUnknownLength(seqLength == -1);

        // There is no more of the tag to read if the length of the tag
        // is zero. In fact, trying to get the Next element is potentially
        // bad, so we'll just shut down the reading here.
        if (seqLength == 0) {
            return sq;
        }

        // hold on to where the sequence is before items for measuring
        // distance from beginning of sequence
        final int seqStart = getFilePointer();

        getNextElement(endianess); // gets the first ITEM tag
        nameSQ = convertGroupElement(groupWord, elementWord);

        while (!nameSQ.equals(DICOMFile.SEQ_ITEM_UNDEF_END)) {
            if (nameSQ.equals(DICOMFile.SEQ_ITEM_BEGIN)) {

                // elementLength here is the length of the
                // item as it written into the File
                FileDicomSQItem item = null;
                if (elementLength == 0) {
                    item = new FileDicomSQItem(null, fileInfo.getVr_type());
                } else {
                    item = getDataSet(elementLength, endianess);
                }
                item.setWriteAsUnknownLength(elementLength == -1); //if reported item length is -1, item will continue to be written with unknown length
                if (item != null) {
                    sq.addItem(item);
                }
            }

            //if defined sequence length, will not read next tag once length has been reached
            if (seqLength == -1 || seqStart + seqLength > getFilePointer()) {
                getNextElement(endianess); // gets the first ITEM tag
                nameSQ = convertGroupElement(groupWord, elementWord);
            } else {
                return sq;
            }
        }

        return sq;
    }

    /**
     * Reads a set of DICOM tags from a DICOM sequence item, ending with the
     * data-element end tag <code>
     * FFFE,E00D</code>. This list of tags in a DICOM sequence item and places
     * them into a hashtable held within a FileDicomItem.
     *
     * @param itemLength Length of the item in bytes.
     * @param endianess Big (true) or little (false).
     *
     * @return The sequence item read in.
     *
     * @see FileDicomItem
     */
    private FileDicomSQItem getDataSet(int itemLength, final boolean endianess) throws IOException {
        final FileDicomSQItem table = new FileDicomSQItem(null, fileInfo.getVr_type());
        table.setWriteAsUnknownLength(itemLength == -1); //if reported item length is -1, item will continue to be written with unknown length

        final int startfptr = getFilePointer();
        boolean dataSetflag = true; //whether dicom header processing should continue
        while (dataSetflag && !nameSQ.equals(DICOMFile.SEQ_ITEM_END) && (getFilePointer() - startfptr < itemLength || itemLength == -1)) {
            FileDicomKey key = getNextTag(endianess);
            nameSQ = key.toString();
            if (!nameSQ.equals(DICOMFile.SEQ_ITEM_END) && !nameSQ.matches(DICOMFile.IMAGE_TAG)) {
                try {
                    dataSetflag = processNextTag(table, key, endianess, true);
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(DICOMFileInputStream.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (nameSQ.matches(DICOMFile.IMAGE_TAG)) {
                numEmbeddedImages++;
                seek(getFilePointer() + elementLength); //embedded image not displayed //TODO: make this image availbale in the dicom infobox
            }
        }

        return table;
    }

    public int[] getPixelData() {
        int numberFrame = Integer.valueOf(this.fileInfo.getTagTable().get("0028,0008")!= null ? this.fileInfo.getTagTable().get("0028,0008").getValue(false).toString().trim():"1");
        int length = this.fileInfo.getExtents()[0] * this.fileInfo.getExtents()[1] * numberFrame;
        int[] imageBuffer = new int[length];
        try {
            readRawPixelData(imageBuffer, fileInfo.getDataType(), 0);
            return imageBuffer;
        } catch (IOException ex) {
            Logger.getLogger(DICOMFileInputStream.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public int[] readImage() {
        int numberFrame = Integer.valueOf(this.fileInfo.getTagTable().get("0028,0008")!= null ? this.fileInfo.getTagTable().get("0028,0008").getValue(false).toString().trim():"1");
        int length = this.fileInfo.getExtents()[0] * this.fileInfo.getExtents()[1] * numberFrame;
        int[] imageBuffer = new int[length];
        try {
            readImage(imageBuffer, fileInfo.getDataType(), 0);
            return imageBuffer;
        } catch (IOException ex) {
            Logger.getLogger(DICOMFileInputStream.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void readRawPixelData(final int[] buffer, final int imageType, final int imageNo) throws IOException {
        try { // rafile (type RandomAccessFile) for header, rawfile (type FileRaw) for image data.
            rawFile.setImageFile(fileInfo.getFilePath(), fileInfo, FileBase.READ);
            rawFile.readImage(buffer, (long) fileInfo.getImageOffset() + (imageNo * buffer.length * fileInfo.bytesPerPixel), imageType);
            rawFile.raFile.close();
            rawFile.raFile = null;
        } catch (final IOException error) {
            System.err.println("ReadDICOM IOexception error");
            throw (error);
        }
    }

    /**
     * Reads a DICOM image file and stores the data into the given short buffer.
     * This method reads the image header (@see readHeader()) then sets various
     * fields within the FileInfo which are relevant to correctly interpreting
     * the image. This list includes:
     *
     * <ul>
     * <li>units of measure</li>
     * <li>pixel pad</li>
     * <li>file info minimum and maximum</li>
     * </ul>
     *
     * <p>
     * This method would be used for short- (byte-) size image datasuch as PET
     * data. This method is faster than the float buffer version of this method
     * since not as much type-conversion is needed.
     * </p>
     *
     * @param buffer 2D buffer used for temporary storage of data
     * @param imageType The type of image (i.e. SHORT, BYTE, ...)
     * @param imageNo For multiFrame images, imageNo >=1. For single slice image
     * imageNo = 0.
     *
     * @return The image
     *
     * @exception IOException if there is an error reading the file
     *
     * @see FileRaw
     */
    public void readImage(final int[] buffer, final int imageType, final int imageNo) throws IOException {
        // System.out.println("in read image short");
        // Read in header info, if something goes wrong, print out error
        if (hasHeaderBeenRead == false) {
            if (!readHeader()) {
                throw (new IOException("DICOM header file error"));
            }
        }

        if (fileInfo.getUnitsOfMeasure(0) != Unit.CENTIMETERS.getLegacyNum()) {
            fileInfo.setUnitsOfMeasure(Unit.MILLIMETERS.getLegacyNum(), 0);
        }

        if (fileInfo.getUnitsOfMeasure(1) != Unit.CENTIMETERS.getLegacyNum()) {
            fileInfo.setUnitsOfMeasure(Unit.MILLIMETERS.getLegacyNum(), 1);
        }

        fileInfo.setUnitsOfMeasure(Unit.MILLIMETERS.getLegacyNum(), 2);

        // Needed for correct display of the image
        // set to null if there is no pixel pad value
        fileInfo.setPixelPadValue(fileInfo.pixelPaddingValue);

        if (!encapsulated) {

            try { // rafile (type RandomAccessFile) for header, rawfile (type FileRaw) for image data.
                rawFile.setImageFile(fileInfo.getFilePath(), fileInfo, FileBase.READ);
                rawFile.readImage(buffer, (long) fileInfo.getImageOffset() + (imageNo * buffer.length * fileInfo.bytesPerPixel), imageType);
                rawFile.raFile.close();
                rawFile.raFile = null;
            } catch (final IOException error) {
                error.printStackTrace();
                System.err.println("ReadDICOM IOexception error");
                throw (error);
            }
        } //else { // encapsulated

//            if (jpegData == null) {
//                if (encapsulatedJP2) {
//                    System.out.println("calling encapsulatedJP2ImageData");
//                    jpegData = encapsulatedJP2ImageData(imageType);
//
//                } else {
//                    System.out.println("Calling encapsulatedImageData");
//                    jpegData = encapsulatedImageData();
//                }
//            }
//
//            if (jpegData != null) {
//
//                try {
//                    int j = imageNo * buffer.length;
//
//                    for (int i = 0; i < buffer.length; i++) {
//                        buffer[i] = (short) jpegData[j];
//                        j++;
//                    }
//                } catch (final ArrayIndexOutOfBoundsException aioobe) {
//                }
//
//                // this means there was only one image - not multiframe.
//                // if image WAS multiframe, we don't want to keep reading in the jpegData buffer
//                // it will be non null the second time through, and won't be read in again.
//                if (jpegData.length == buffer.length) {
//                    jpegData = null;
//                }
//            }
//        }
        // Matt changed from double to float for speed purposes 2/2003 not great increase but 5-10%.
        double tmp;
        short pixelPad = Short.MIN_VALUE;
        double min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;

        // fix pixel padding and remap to HU units
        if (fileInfo.getPixelPadValue() != null) {
            pixelPad = fileInfo.getPixelPadValue();
        }
        // System.out.println("pixel _pad = "+ pixelPad);

        float slope = (float) fileInfo.getRescaleSlope();
        final float intercept = (float) fileInfo.getRescaleIntercept();
        if (slope == 0) {
            slope = 1;
        }

        // System.out.println(" slope = " + slope + " intercept = " + intercept);
        // Why is this here? It overwrites the slope and intercept.
        // if (fileInfo.getModality() == FileInfoBase.MAGNETIC_RESONANCE) {
        // slope = 1;
        // intercept = 0;
        // }
        boolean setOneMinMax = false;
        for (final double element : buffer) {
            tmp = element;

            if (tmp != pixelPad) {
                setOneMinMax = true;
                if (tmp < min) {
                    min = tmp;
                }
                if (tmp > max) {
                    max = tmp;
                }
            }
        }
        if (setOneMinMax == false) {
            min = max = buffer[0];
        }

        fileInfo.setMin(min);
        fileInfo.setMax(max);

        // System.out.println("min = " + min + " max = " + max);
        if ((pixelPad <= min) || (pixelPad >= max)) {

            for (int i = 0; i < buffer.length; i++) {

                // tmp = buffer[i];
                // need to fix - we're altering image data so that when the file is
                // written, it is not exactly the same as when it was read in, i.e.,
                // there are no pixel pad values stored in the buffer; they've all been
                // converted to the minimum value.
                if (buffer[i] != pixelPad) {
                    buffer[i] = (short) ((buffer[i] * slope) + intercept);
                } else {
                    buffer[i] = (short) ((min * slope) + intercept);
                }
            }
        } else {
            if ((slope != 1) || (intercept != 0)) {
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = (short) ((buffer[i] * slope) + intercept); // Rescale data
                }
            }
        }
    }
}
