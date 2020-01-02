/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RawDCMLibary.DICOM;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import RawDCMLibary.exceptions.DICOM_Exception;
import RawDCMLibary.model.DICOMFileInputStream;
import RawDCMLibary.model.FileBase;
import RawDCMLibary.model.FileDicomKey;
import RawDCMLibary.model.FileDicomSQ;
import RawDCMLibary.model.FileDicomTag;
import RawDCMLibary.model.FileDicomTagInfo.VR;
import RawDCMLibary.model.FileDicomTagTable;
import RawDCMLibary.model.FileInfoBase;
import static RawDCMLibary.model.FileInfoBase.BIOMAGNETIC_IMAGING;
import static RawDCMLibary.model.FileInfoBase.COLOR_FLOW_DOPPLER;
import static RawDCMLibary.model.FileInfoBase.COMPUTED_RADIOGRAPHY;
import static RawDCMLibary.model.FileInfoBase.COMPUTED_TOMOGRAPHY;
import static RawDCMLibary.model.FileInfoBase.DIAPHANOGRAPHY;
import static RawDCMLibary.model.FileInfoBase.DIGITAL_RADIOGRAPHY;
import static RawDCMLibary.model.FileInfoBase.DUPLEX_DOPPLER;
import static RawDCMLibary.model.FileInfoBase.ENDOSCOPY;
import static RawDCMLibary.model.FileInfoBase.EXTERNAL_CAMERA_PHOTOGRAPHY;
import static RawDCMLibary.model.FileInfoBase.GENERAL_MICROSCOPY;
import static RawDCMLibary.model.FileInfoBase.HARDCOPY;
import static RawDCMLibary.model.FileInfoBase.INTRAORAL_RADIOGRAPHY;
import static RawDCMLibary.model.FileInfoBase.LASER_SURFACE_SCAN;
import static RawDCMLibary.model.FileInfoBase.MAGNETIC_RESONANCE;
import static RawDCMLibary.model.FileInfoBase.MAGNETIC_RESONANCE_ANGIOGRAPHY;
import static RawDCMLibary.model.FileInfoBase.MAGNETIC_RESONANCE_SPECTROSCOPY;
import static RawDCMLibary.model.FileInfoBase.MAMMOGRAPHY;
import static RawDCMLibary.model.FileInfoBase.NUCLEAR_MEDICINE;
import static RawDCMLibary.model.FileInfoBase.OTHER;
import static RawDCMLibary.model.FileInfoBase.PANORAMIC_XRAY;
import static RawDCMLibary.model.FileInfoBase.POSITRON_EMISSION_TOMOGRAPHY;
import static RawDCMLibary.model.FileInfoBase.RADIOGRAPHIC_IMAGING;
import static RawDCMLibary.model.FileInfoBase.RADIOTHERAPY_DOSE;
import static RawDCMLibary.model.FileInfoBase.RADIOTHERAPY_IMAGE;
import static RawDCMLibary.model.FileInfoBase.RADIOTHERAPY_PLAN;
import static RawDCMLibary.model.FileInfoBase.RADIOTHERAPY_RECORD;
import static RawDCMLibary.model.FileInfoBase.RADIOTHERAPY_STRUCTURE_SET;
import static RawDCMLibary.model.FileInfoBase.RADIO_FLUOROSCOPY;
import static RawDCMLibary.model.FileInfoBase.SINGLE_PHOTON_EMISSION_COMPUTED_TOMOGRAPHY;
import static RawDCMLibary.model.FileInfoBase.SLIDE_MICROSCOPY;
import static RawDCMLibary.model.FileInfoBase.THERMOGRAPHY;
import static RawDCMLibary.model.FileInfoBase.ULTRASOUND;
import static RawDCMLibary.model.FileInfoBase.UNKNOWN_MODALITY;
import static RawDCMLibary.model.FileInfoBase.XRAY_ANGIOGRAPHY;
import RawDCMLibary.model.FileRawChunk;
import RawDCMLibary.model.enums.Enums.Unit;
import RawDCMLibary.model.enums.Enums.VRtype;
import java.util.Arrays;

/**
 *
 * @author shaesler
 */
public class DICOMFile {

    /**
     * Pixel or voxel resolutions for each dimension - default = 1.0. The z-dim
     * resolution should be the spacing between the centers of adjacent slices;
     * sometimes this will match the slice thickness, but not always.
     */
    private double[] dimResolutions = {1.0, 1.0, 1.0, 1.0, 1.0};

    private short photometric = 1; // 1 indicates 0 is black

    /**
     * Some file formats have a pad value for pixels outside the acquisition
     * domain.
     */
    private Short pixelPadValue;

    /**
     * DICOM images have a rescale y-intercept value that we have also kept in
     * the base.
     */
    private double rescaleIntercept = 0.0;

    /**
     * DICOM images have a rescale slope value that we have also kept in the
     * base.
     */
    private double rescaleSlope = 1.0;

    /**
     * The thickness of individual slices in the image volume. Stored in dicom
     * tag 0018,0050 and various other places in other file formats.
     */
    private float sliceThickness = 0;

    /**
     * The tag marking the start of the image data.
     */
    public static final String IMAGE_TAG = "7FE0,0010";

    /**
     * The tag marking the beginning of a dicom sequence.
     */
    public static final String SEQ_ITEM_BEGIN = "FFFE,E000";

    /**
     * The tag marking the end of a dicom sequence.
     */
    public static final String SEQ_ITEM_END = "FFFE,E00D";

    /**
     * The tag marking the end of an undefined length dicom sequence.
     */
    public static final String SEQ_ITEM_UNDEF_END = "FFFE,E0DD";

    private final int UNDEFINED_LENGTH = -2;
    /**
     * The Endianess of the data. Intel, DEC Alpha ***** LSB first byte
     * LITTLE_ENDIAN (false) Motorola (MAC), SPARC (SUN), SGI IRIX MSB first
     * byte BIG_ENDIAN (true)
     */
    private boolean endianess = FileBase.LITTLE_ENDIAN;

    /**
     * Whether private siemens tags have been found and are being processed
     */
    private boolean isSiemensMRI, isSiemensMRI2;

    /**
     * Value Representation - see FileDicomTagInfo.
     */
    private byte[] vrBytes = new byte[2];

    /**
     * Length of the value field of data element.
     */
    private int elementLength;

    /**
     * Second number (DICOM element in a group) in ordered pair of numbers that
     * uniquely identifies a data element.
     */
    private int elementWord;

    /**
     * First number (DICOM group) in ordered pair of numbers that uniquely
     * identifies a data element.
     */
    private int groupWord;

    private String filePath;

    private long fileLength;

    private VRtype vrType = VRtype.EXPLICIT;

    public static boolean LITTLE_ENDIAN = false;

    /**
     * Byte order. Leftmost byte is most significant.
     */
    public static boolean BIG_ENDIAN = true;

    /**
     * Whether the tag currently being processed and read in is a sequence tag.
     */
    public boolean isCurrentTagSQ = false;

    /**
     * Indicates the modality (medical image type) of the dataset.
     */
    protected int modality = FileInfoBase.UNKNOWN_MODALITY;

    /**
     * Used to indicate that the data buffer is of type Boolean (1 bit per
     * voxel).
     */
    public static final int BOOLEAN = 0;

    /**
     * Used to indicate that the data buffer is of type signed byte (8 bits per
     * voxel).
     */
    public static final int BYTE = 1;

    /**
     * Used to indicate that the data buffer is of type unsigned byte (8 bits
     * per voxel).
     */
    public static final int UBYTE = 2;

    /**
     * Used to indicate that the data buffer is of type signed short (16 bits
     * per voxel).
     */
    public static final int SHORT = 3;

    /**
     * Used to indicate that the data buffer is of type unsigned short (16 bits
     * per voxal).
     */
    public static final int USHORT = 4;

    /**
     * Used to indicate that the data buffer is of type signed integer (32 bits
     * per voxel).
     */
    public static final int INTEGER = 5;

    /**
     * Used to indicate that the data buffer is of type unsigned integer (32
     * bits per voxel).
     */
    public static final int UINTEGER = 14;

    /**
     * Used to indicate that the data buffer is of type signed long integer (64
     * bits per voxel).
     */
    public static final int LONG = 6;

    /**
     * Used to indicate that the data buffer is of type float (32 bits per
     * voxel).
     */
    public static final int FLOAT = 7;

    /**
     * Used to indicate that the data buffer is of type double (64 bits per
     * voxel).
     */
    public static final int DOUBLE = 8;

    /**
     * Used to indicate that the data buffer is of type ARGB where each channel
     * (A = alpha, R = red, G = green, B = blue) is represented by a unsigned
     * byte value. (4 * UBYTE(8 bits) = 4 bytes)
     */
    public static final int ARGB = 9;

    /**
     * Used to indicate that the data buffer is of type ARGB where each channel
     * (A = alpha, R = red, G = green, B = blue) is represented by a unsigned
     * short value. (4 * USHORT(16 bits) = 8 bytes)
     */
    public static final int ARGB_USHORT = 10;

    /**
     * Used to indicate that the data buffer is of type ARGB where each channel
     * (A = alpha, R = red, G = green, B = blue) is represented by a float
     * value. (4 * FLOAT(32 bits) = 16 bytes)
     */
    public static final int ARGB_FLOAT = 11;

    private int dataType;

    /**
     * Pointer to file to read or write from.
     */
    public RandomAccessFile raFile;

    private DICOMFileInputStream dcmFileStream;

    protected Unit[] unitsOfMeasure = {Unit.MILLIMETERS, Unit.MILLIMETERS, Unit.MILLIMETERS,
        Unit.SECONDS, Unit.UNKNOWN_MEASURE};
    private File file;

    /**
     * DOCUMENT ME!
     */
    private FileInfoBase fileInfo;

    /**
     * DOCUMENT ME!
     */
    private FileRawChunk fileRW = null;

    /**
     * Image extents as decribed by the image file format.
     */
    private int[] extents = new int[5];

    /**
     * Number of bytes to the start the image data - ie. the header length
     */
    private int imageOffset;
    private double min;
    private double max;

    private int[] imageStream;

    private int[][] pixelArray;

    private int defaultWindowWidth = 0;

    private int defaultWindowLevel = 0;

    /**
     * Used to indicate if the raw data was also compression (0 = no, 1 = zip).
     */
    private int compressionType = 0;

    protected int[] axisOrientation = {FileInfoBase.ORI_UNKNOWN_TYPE, FileInfoBase.ORI_UNKNOWN_TYPE,
        FileInfoBase.ORI_UNKNOWN_TYPE};

    public DICOMFile(String filePath, String fileName) {
        dcmFileStream = new DICOMFileInputStream(this);
        File file = new File(filePath + fileName);
        this.fileLength = (int) file.length();
        this.tagTable = new FileDicomTagTable(vr_type);
    }

    public DICOMFile(String fullFileName) {
        File file = new File(fullFileName);
        this.fileLength = (int) file.length();
        this.filePath = file.getAbsolutePath();
        dcmFileStream = new DICOMFileInputStream(this);
        this.tagTable = new FileDicomTagTable(vr_type);
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public VRtype getVrType() {
        return vrType;
    }

    public void setVrType(VRtype vrType) {
        this.vrType = vrType;
    }

    public boolean isEndianess() {
        return endianess;
    }

    public void setEndianess(boolean endianess) {
        this.endianess = endianess;
    }

    public DICOMFileInputStream getDcmFileStream() {
        return dcmFileStream;
    }

    public void setDcmFileStream(DICOMFileInputStream dcmFileStream) {
        this.dcmFileStream = dcmFileStream;
    }

    public void readHeader() throws IOException {
        if (dcmFileStream.openForRead(this.filePath)) {
            try {
                dcmFileStream.readBinary((int) this.fileLength);
                dcmFileStream.readHeader();
                readImage();
                this.defaultWindowLevel = Short.valueOf(this.tagTable.getValue("0028,1050") != null ? this.tagTable.getValue("0028,1050").toString() : "0");
                this.defaultWindowWidth = Short.valueOf(this.tagTable.getValue("0028,1051") != null ? this.tagTable.getValue("0028,1051").toString() : "0");
            } catch (DICOM_Exception ex) {
                Logger.getLogger(DICOMFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void readImage() {
        if (dcmFileStream.openForRead(this.filePath)) {
            this.imageStream = dcmFileStream.readImage();
            if (this.imageStream != null) {
                this.pixelArray = new int[extents[1]][extents[0]];
                int i = 0;
                for (int r = 0; r < this.extents[1] && i < this.imageStream.length; r++) {
                    for (int c = 0; c < this.extents[0] && i < this.imageStream.length; c++) {
                        this.pixelArray[r][c] = this.imageStream[i++];
                    }
                }
            }
        }
    }

    public int[] getPixelData() {
        if (dcmFileStream.openForRead(this.filePath)) {
            return dcmFileStream.getPixelData();
        }
        return null;
    }

    public int[] getImageStream() {
        return imageStream;
    }
    

    public Map<Integer, Integer> getHistogram() {
        Map<Integer, Integer> histogram = new HashMap<>();
        int[] pxArray = getPixelData();
        Arrays.sort(pxArray);
        for (int i = 0; i < pxArray.length; i++) {
            if (histogram.containsKey(pxArray[i])) {
                Integer value = histogram.get(pxArray[i]);
                value += 1;
                histogram.put(pxArray[i], value);
            } else {
                histogram.put(pxArray[i], 1);
            }
        }
        return histogram;
    }

    public BufferedImage getDefaultBufferedImage() {
        return getBufferedImage(this.defaultWindowLevel, this.defaultWindowWidth);
    }

    public BufferedImage getBufferedImage(final int center, final int width) {
        BufferedImage outImage = new BufferedImage(this.extents[0], this.extents[1], BufferedImage.TYPE_BYTE_GRAY);
        short[][] imageArray = new short[this.extents[1]][this.extents[0]];
        final short minPixel = 0;
        final short maxPixel = 255;
        for (int r = 0; r < this.extents[1]; r++) {
            for (int c = 0; c < this.extents[0]; c++) {
                int x = this.pixelArray[r][c];
                if (x <= center - 0.5 - (width - 1) / 2) {
                    imageArray[r][c] = minPixel;
                } else if (x > center - 0.5 + (width + 1) / 2) {
                    imageArray[r][c] = maxPixel;
                } else {
                    imageArray[r][c] = (short) (((x - (center - 0.5)) / (width - 1) + 0.5) * (maxPixel - minPixel) + minPixel);

                }
                int value = imageArray[r][c] << 16 | imageArray[r][c] << 8 | imageArray[r][c];
                outImage.setRGB(c, r, value);
            }
        }
        return outImage;
    }

    public boolean isIsCurrentTagSQ() {
        return isCurrentTagSQ;
    }

    public void setIsCurrentTagSQ(boolean isCurrentTagSQ) {
        this.isCurrentTagSQ = isCurrentTagSQ;
    }
    //~ Static fields/initializers -------------------------------------------------------------------------------------

    /**
     * Use serialVersionUID for interoperability.
     */
    private static final long serialVersionUID = -3072660161266896186L;

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

    /**
     * these are the DICOM tag ID numbers corresponding to the tags which are
     * anonymized. They are kept here for simplicity of updating (when it is
     * decided that other tags are must be anonymized) and to the advantage of
     * calling objects.
     */
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

            // if (Preferences.isDebug()) xfrm.print();
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

    /**
     * Public method for populating ModelImage data fields.
     */
//    public final void setInfoFromTags() {
//        setInfoFromTags(this.tagTable, false);
//    }
//    /**
//     * After the tag table is filled, check for a number of tags to set up some fields in this file info object.
//     */
//    private final void setInfoFromTags(FileDicomTagTable tagTable, boolean insideSequenceTag) {
//        HashMap<Integer, LengthStorageUnit> lengthComp = new HashMap<Integer, LengthStorageUnit>();
//        Iterator<Map.Entry<FileDicomKey,FileDicomTag>> itr = tagTable.getTagList().entrySet().iterator();
//        FileDicomTag tag = null;
//       
//        while(itr.hasNext()) {
//            tag = itr.next().getValue();
//            if(!insideSequenceTag && tag.getElement() != 0) {
//                appendLengthTag(tag, lengthComp);
//            }
//            if(tag.getValue(false) instanceof FileDicomSQ) {
//                FileDicomSQ sq = (FileDicomSQ) tag.getValue(false);
//                for(int i=0; i<sq.getSequence().size(); i++) {
//                    setInfoFromTags(sq.getSequence().get(i), true);
//                }
//            }
//            setInfoFromTag(tag);
//        }
//        
//        updateLengthTags(lengthComp);
//        
//        if (getModality() == FileInfoBase.POSITRON_EMISSION_TOMOGRAPHY) {
//            displayType = FLOAT;
//            // a bit of a hack - indicates Model image should be reallocated to float for PET image the data is
//            // stored
//            // as 2 bytes (short) but is "normalized" using the slope parameter required for PET images (intercept
//            // always 0 for PET).
//        }
//
//        if ( ( (getDataType() == UBYTE) || (getDataType() == USHORT))
//                && (getRescaleIntercept() < 0)) {
//            // this performs a similar method as the pet adjustment for float images stored on disk as short to read
//            // in
//            // signed byte and signed short images stored on disk as unsigned byte or unsigned short with a negative
//            // rescale intercept
//            if (getDataType() == UBYTE) {
//                displayType = BYTE;
//            } else if (getDataType() == USHORT) {
//                displayType = SHORT;
//            }
//        }
//    }
    private void updateLengthTags(HashMap<Integer, LengthStorageUnit> lengthComp) {
        Iterator<Integer> itr = lengthComp.keySet().iterator();
        while (itr.hasNext()) {
            int group = itr.next();
            try {
                Integer length = (Integer) tagTable.get(new FileDicomKey(group, 0)).getValue(false);
                if (length != lengthComp.get(group).get()) {
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
            Logger.getLogger(DICOMFile.class.getName()).log(Level.SEVERE, null, ex);
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

//    /**
//     * Sets fields in this file info based on a given tag's key and value.
//     *
//     * @param  tag  The tag to use to update the file info fields (not all tags cause field changes).
//     */
//    protected final void setInfoFromTag(FileDicomTag tag) {
//        if (tag == null) {
//            System.err.println("tag == null on entry to setInfoFromTag(FileDicomTag tag)\n");
//            return;
//        }
//        else if (tag.getInfo() == null) {
//            System.err.println("tag.getInfo() == null on entry to setInfoFromTag(FileDicomTag tag)\n");
//            return;    
//        }
//        FileDicomKey tagKey = tag.getInfo().getKey();
//        //TODO: JDK7 allows this to turn into switch statements
//        // ordering by type 1 tags first.  Then in numerical order.
//        try {
//            if (tagKey.equals("0008,0060")) {
//                setModalityFromDicomStr(((String) tag.getValue(false)).trim()); // type 1
//                // setModalityFromDicomStr() covers the possibility of value == ""
//            } else if (tagKey.equals("0018,0088")) {
//                super.setResolutions(Float.parseFloat(tag.getValue(false).toString()), 2); // type 1
//                setUnitsOfMeasure(Unit.MILLIMETERS, 2);
//            } else if (tagKey.equals("0028,0100")) {
//                bitsAllocated = ((Short) tag.getValue(false)).shortValue(); // type 1
//            } else if (tagKey.equals("0028,0103")) {
//                pixelRepresentation = ((Short) tag.getValue(false)).shortValue(); // type 1
//            } else if (tagKey.equals("0028,1052")) {
//                super.setRescaleIntercept(Double.valueOf((String) tag.getValue(false)).doubleValue());  // type 1
//                // only used in CT images, so don't notify that not found
//            } else if (tagKey.equals("0028,1053")) {
//                super.setRescaleSlope(Double.valueOf((String) tag.getValue(false)).doubleValue()); // type 1
//                // only used in CT and PET images, so don't notify that not found
//            } else if (tagKey.equals("0018,0050")) {
//                
//    
//                setSliceThickness(Float.parseFloat(((String) tag.getValue(false)).trim())); // type 2
//            } else if (tagKey.equals("0018,602C")) {
//                setResolutions(((Double) tag.getValue(false)).floatValue(), 0);
//                setUnitsOfMeasure(Unit.CENTIMETERS, 0);
//            } else if (tagKey.equals("0018,602E")) {
//                setResolutions(((Double) tag.getValue(false)).floatValue(), 1);
//                setUnitsOfMeasure(Unit.CENTIMETERS, 1);
//            } else if (tagKey.equals("0020,0032")) { // type 2c
//                orientation = ((String) tag.getValue(false)).trim();
//                int index1 = -1, index2 = -1;
//                for (int i = 0; i < orientation.length(); i++) {
//    
//                    if (orientation.charAt(i) == '\\') {
//    
//                        if (index1 == -1) {
//                            index1 = i;
//                        } else {
//                            index2 = i;
//                        }
//                    }
//                }
//                if (index1 != -1)
//                    xLocation = Float.parseFloat(orientation.substring(0, index1));
//                if (index1 != -1 && index2 != -1)
//                    yLocation = Float.parseFloat(orientation.substring(index1 + 1, index2));
//                if (index2 != -1)
//                    zLocation = Float.parseFloat(orientation.substring(index2 + 1));
//              
//                if (index1 == -1 || index2 == -1)
//                    System.err.println(("Warning reading tag 0020, 0032 - too few items \n");
//                
//            } else if (tagKey.equals("0020,0013")) { // type 2
//                instanceNumber = Integer.parseInt(tag.getValue(true).toString());
//            } else if (tagKey.equals("0020,1041")) { // type 3
//                sliceLocation =  Float.parseFloat(tag.getValue(true).toString());
//            } else if (tagKey.equals("0028,0030") &&
//                           ((tagTable.get("0018,1164") == null) || (tagTable.get("0018,1164").getValue(false) == null))) { // type 2
//                // y resolution followed by x resolution
//    
//                String valueStr = ((String) tag.getValue(false)).trim();
//                String firstHalf, secondHalf;
//                int index = 0;
//    
//                for (int i = 0; i < valueStr.length(); i++) {
//    
//                    if (valueStr.charAt(i) == '\\') {
//                        index = i;
//                    }
//                }
//    
//                firstHalf = valueStr.substring(0, index).trim();
//                secondHalf = valueStr.substring(index + 1, valueStr.length()).trim();
//    
//                Float f1 = null;
//                Float f2 = null;
//    
//                if ((firstHalf != null) && (firstHalf.length() > 0)) {
//    
//                    try {
//                        f1 = new Float(firstHalf);
//                    } catch (NumberFormatException e) {
//                        setResolutions(1.0f, 1);
//                        // MipavUtil.displayError("Number format error: Pixel spacing = " + s);
//                    }
//    
//                    if (f1 != null) {
//                        setResolutions(f1.floatValue(), 1);
//                    }
//                } else {
//                    setResolutions(1.0f, 1);
//                }
//    
//                if ((secondHalf != null) && (secondHalf.length() > 0)) {
//    
//                    try {
//                        f2 = new Float(secondHalf);
//                    } catch (NumberFormatException e) {
//                        setResolutions(getResolution(1), 0);
//                        // MipavUtil.displayError("Number format error: Pixel spacing = " + s);
//                    }
//    
//                    if (f2 != null) {
//                        setResolutions(f2.floatValue(), 0);
//                    }
//                } else {
//                    setResolutions(1.0f, 0);
//                }
//            } else if (tagKey.equals("0018,1164")) { // type 2
//    
//                String valueStr = ((String) tag.getValue(false)).trim();
//                String firstHalf, secondHalf;
//                int index = 0;
//    
//                for (int i = 0; i < valueStr.length(); i++) {
//    
//                    if (valueStr.charAt(i) == '\\') {
//                        index = i;
//                    }
//                }
//    
//                firstHalf = valueStr.substring(0, index).trim();
//                secondHalf = valueStr.substring(index + 1, valueStr.length()).trim();
//    
//                Float f1 = null;
//                Float f2 = null;
//    
//                if ((firstHalf != null) && (firstHalf.length() > 0)) {
//    
//                    try {
//                        f1 = new Float(firstHalf);
//                    } catch (NumberFormatException e) {
//                        setResolutions(1.0f, 0);
//                        // MipavUtil.displayError("Number format error: Pixel spacing = " + s);
//                    }
//    
//                    if (f1 != null) {
//                        setResolutions(f1.floatValue(), 0);
//                    }
//                } else {
//                    setResolutions(1.0f, 0);
//                }
//    
//                if ((secondHalf != null) && (secondHalf.length() > 0)) {
//    
//                    try {
//                        f2 = new Float(secondHalf);
//                    } catch (NumberFormatException e) {
//                        setResolutions(getResolution(0), 1);
//                        // MipavUtil.displayError("Number format error: Pixel spacing = " + s);
//                    }
//    
//                    if (f2 != null) {
//                        setResolutions(f2.floatValue(), 1);
//                    }
//                } else {
//                    setResolutions(1.0f, 1);
//                }
//            } else if (tagKey.equals("0028,0120")) { // type 3
//                pixelPaddingValue = (Short) tag.getValue(false);
//            } else if (tagKey.equals("0028,0006")) {
//                planarConfig = ((Number)tag.getValue(false)).shortValue();
//            } else if(tagKey.equals("0028,0004")) { //requires bitsAllocated(0028,0100) and pixelRepresentation(0028,0103) to be set
//                setInfoFromTag(tagTable.get(new FileDicomKey("0028,0100")));
//                if (tagTable.get(new FileDicomKey("0028,0103"))  == null) {
//                    System.err.println("In DICOMFileInputStream.setInfoFromTag tagTable.get(new FileDicomKey(\"0028,0103\"))  == null\n");
//                }
//                else {
//                    setInfoFromTag(tagTable.get(new FileDicomKey("0028,0103")));
//                }
//                photometricInterp = ((String) tag.getValue(false)).trim();
//                
//                if ( (photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
//                        && (bitsAllocated == 1)) {
//                    setDataType(ModelStorageBase.BOOLEAN);
//                    displayType = ModelStorageBase.BOOLEAN;
//                    bytesPerPixel = 1;
//                } else if ( (photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
//                        && (pixelRepresentation == DICOMFileInputStream.UNSIGNED_PIXEL_REP)
//                        && (bitsAllocated == 8)) {
//                    setDataType(ModelStorageBase.UBYTE);
//                    displayType = ModelStorageBase.UBYTE;
//                    bytesPerPixel = 1;
//                } else if ( (photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
//                        && (pixelRepresentation == DICOMFileInputStream.SIGNED_PIXEL_REP)
//                        && (bitsAllocated == 8)) {
//                    setDataType(ModelStorageBase.BYTE);
//                    displayType = ModelStorageBase.BYTE;
//                    bytesPerPixel = 1;
//                } else if ( (photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
//                        && (pixelRepresentation == DICOMFileInputStream.UNSIGNED_PIXEL_REP)
//                        && (bitsAllocated > 16)) {
//                    setDataType(ModelStorageBase.UINTEGER);
//                    displayType = ModelStorageBase.UINTEGER;
//                    bytesPerPixel = 4;
//                } else if ( (photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
//                        && (pixelRepresentation == DICOMFileInputStream.UNSIGNED_PIXEL_REP)
//                        && (bitsAllocated > 8)) {
//                    setDataType(ModelStorageBase.USHORT);
//                    displayType = ModelStorageBase.USHORT;
//                    bytesPerPixel = 2;
//                } else if ( (photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
//                        && (pixelRepresentation == DICOMFileInputStream.SIGNED_PIXEL_REP) && (bitsAllocated > 8)) {
//                    setDataType(ModelStorageBase.SHORT);
//                    displayType = ModelStorageBase.SHORT;
//                    bytesPerPixel = 2;
//                } else if (photometricInterp.equals("RGB") && (bitsAllocated == 8)) { //requires 0028,0006 to be set
//                    setInfoFromTag(tagTable.get(new FileDicomKey("0028,0006")));
//                    setDataType(ModelStorageBase.ARGB);
//                    displayType = ModelStorageBase.ARGB;
//                    bytesPerPixel = 3;
//                } else if (photometricInterp.equals("RGB") && (bitsAllocated == 16)) { //requires 0028,0006 to be set
//                    setInfoFromTag(tagTable.get(new FileDicomKey("0028,0006")));
//                    setDataType(ModelStorageBase.ARGB_USHORT);
//                    displayType = ModelStorageBase.ARGB_USHORT;
//                    bytesPerPixel = 6;
//                } else if (photometricInterp.equals("YBR_FULL_422") && (bitsAllocated == 8)) {  //requires 0028,0006 to be set
//                    setInfoFromTag(tagTable.get(new FileDicomKey("0028,0006")));
//                    setDataType(ModelStorageBase.ARGB);
//                    displayType = ModelStorageBase.ARGB;
//                    bytesPerPixel = 3;
//                } else if (photometricInterp.equals("PALETTE COLOR")
//                        && (pixelRepresentation == DICOMFileInputStream.UNSIGNED_PIXEL_REP)
//                        && (bitsAllocated == 8)) {
//                    setDataType(ModelStorageBase.UBYTE);
//                    displayType = ModelStorageBase.UBYTE;
//                    bytesPerPixel = 1;
//    
//                    final int[] dimExtents = new int[2];
//                    dimExtents[0] = 4;
//                    dimExtents[1] = 256;
//    
//                } else {
//                    System.err.println("File DICOM: readImage() - Unsupported pixel Representation" + "\n");
//                }
//            } else if(tagKey.equals("0028,1201") || tagKey.equals("0028,1202") || tagKey.equals("0028,1203")) {
//            //for keyNum, from dicom standard, 1 is red, 2 is green, 3 is blue
//                int keyNum = Integer.valueOf(tagKey.getElement().substring(tagKey.getElement().length()-1));
//                Object data = tag.getValue(false);
//            } 
//        } catch(NumberFormatException ex) {
//            System.err.println("Tag "+tag.getKey().toString()+" does not contain a number.");
//        }
//    }
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
        this.setModality(getModalityFromDicomStr(value));
    }

    public VRtype getVr_type() {
        return vr_type;
    }

    public void setVr_type(VRtype vr_type) {
        this.vr_type = vr_type;
    }

    /**
     * Sets the modality.
     *
     * @param mod modality
     */
    public final void setModality(final int mod) {
        modality = mod;
    }

    /**
     * Sets format of image data.
     *
     * @param type data type defined in ModelStorageBase
     */
    public final void setDataType(final int type) {
        dataType = type;
    }

    /**
     * Returns the units of measure.
     *
     * @return int[] units (Inches or millimeters);
     */
    public final int[] getUnitsOfMeasure() {
        int[] unitsInt = new int[unitsOfMeasure.length];
        for (int i = 0; i < unitsInt.length; i++) {
            if (unitsOfMeasure[i] != null) {
                unitsInt[i] = unitsOfMeasure[i].getLegacyNum();
            } else {
                unitsInt[i] = Unit.UNKNOWN_MEASURE.getLegacyNum();
            }
        }

        return unitsInt;
    }

    /**
     * Returns the units of measure for the given dimension.
     *
     * @param dim dimension index
     *
     * @return int units (Inches or millimeters);
     */
    public int getUnitsOfMeasure(final int dim) {

        // could try catch array out of bounds ...
        if ((unitsOfMeasure != null) && (dim < unitsOfMeasure.length) && (dim >= 0) && (unitsOfMeasure[dim] != null)) {
            return unitsOfMeasure[dim].getLegacyNum();
        } else { //The selected dimension does not exist in the image
            return Unit.UNKNOWN_MEASURE.getLegacyNum();
        }
    }

    /**
     * Sets units of measure for image, on a per dimension basis.
     *
     *
     * @param unitMeasure Unit of measure for the dimension
     * @param dim Dimension to set unit of measure in
     */
    public final void setUnitsOfMeasure(final Unit unitMeasure, final int dim) {
        unitsOfMeasure[dim] = unitMeasure;
    }

    /**
     * Sets units of measure for image, on a per dimension basis.
     *
     * @param unitMeasure Unit of measure for the dimension
     * @param dim Dimension to set unit of measure in
     */
    public final void setUnitsOfMeasure(final int unitMeasure, final int dim) {
        setUnitsOfMeasure(Unit.getUnitFromLegacyNum(unitMeasure), dim);
    }

    public void setPhotometric(final short value) {
        photometric = value;
    }

    /**
     * Sets pixel pad value: used in some Dicom images.
     *
     * @param value pixel pad value
     */
    public final void setPixelPadValue(final Short value) {
        pixelPadValue = value;
    }

    /**
     * Sets the rescale intercept.
     *
     * @param intercept the intercept
     */
    public final void setRescaleIntercept(final double intercept) {
        rescaleIntercept = intercept;
    }

    /**
     * Sets the rescale slope.
     *
     * @param slope the slope
     */
    public final void setRescaleSlope(final double slope) {
        rescaleSlope = slope;
    }

    /**
     * Sets the resolutions of the image.
     *
     * @param resolutions resolution object
     */
    public final void setResolutions(final double[] resolutions) {

        if (resolutions != null) {
            dimResolutions = resolutions.clone();
        }
    }

    /**
     * Sets the resolutions of the image, on a per dimension basis.
     *
     * @param resolution Resolution for the dimension
     * @param dim Dimension to set resolution in
     */
    public final void setResolutions(final double resolution, final int dim) {
        dimResolutions[dim] = resolution;
    }

    /**
     * Sets the thickness of the image slices.
     *
     * @param thickness The slice thickness.
     */
    public void setSliceThickness(final float thickness) {
        sliceThickness = thickness;
    }

    public FileInfoBase getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfoBase fileInfo) {
        this.fileInfo = fileInfo;
    }

    public int getImageOffset() {
        return imageOffset;
    }

    public void setImageOffset(int imageOffset) {
        this.imageOffset = imageOffset;
    }

    public String getPhotometricInterp() {
        return photometricInterp;
    }

    public void setPhotometricInterp(String photometricInterp) {
        this.photometricInterp = photometricInterp;
    }

    public Short getPixelPaddingValue() {
        return pixelPaddingValue;
    }

    public void setPixelPaddingValue(Short pixelPaddingValue) {
        this.pixelPaddingValue = pixelPaddingValue;
    }

    public short getPixelRepresentation() {
        return pixelRepresentation;
    }

    public void setPixelRepresentation(short pixelRepresentation) {
        this.pixelRepresentation = pixelRepresentation;
    }

    /**
     * Returns pixel pad value.
     *
     * @return Short Returns pixel pad value
     */
    public final Short getPixelPadValue() {
        return pixelPadValue;
    }

    public double getRescaleIntercept() {
        return rescaleIntercept;
    }

    public double getRescaleSlope() {
        return rescaleSlope;
    }

    /**
     * Sets min pixel value of image.
     *
     * @param Min Min pixel value
     */
    public final void setMin(final double Min) {
        min = Min;
    }

    /**
     * Sets max pixel value of image.
     *
     * @param Max max pixel value
     */
    public void setMax(final double Max) {
        max = Max;
    }

    /**
     * Returns data type.
     *
     * @return int type of data in file
     */
    public final int getDataType() {
        return dataType;
    }

    /**
     * Returns the modality.
     *
     * @return int indicating modality
     */
    public final int getModality() {
        return modality;
    }

    /**
     * Sets dimensionality of the images.
     *
     * @param dims dimensionality for x,y, and z ... dimensions
     */
    public final void setExtents(final int[] dims) {

        if (dims != null) {
            extents = dims.clone();
        }
    }

    /**
     * Sets dimensionality for image, on a per dimension basis.
     *
     * @param extent Extent of this dimension
     * @param dim Dimension to set extent in
     */
    public void setExtents(final int extent, final int dim) {
        extents[dim] = extent;
    }

    /**
     * Returns the dimensionality of the image.
     *
     * @return int[] units (Inches or millimeters);
     */
    public final int[] getExtents() {
        return extents;
    }

    protected final void setInfoFromTag(FileDicomTag tag) {
        if (tag == null) {
            return;
        } else if (tag.getInfo() == null) {
            return;
        }
        FileDicomKey tagKey = tag.getInfo().getKey();
        //TODO: JDK7 allows this to turn into switch statements
        // ordering by type 1 tags first.  Then in numerical order.
        try {
            if (tagKey.equals("0008,0060")) {
                setModalityFromDicomStr(((String) tag.getValue(false)).trim()); // type 1
                // setModalityFromDicomStr() covers the possibility of value == ""
            } else if (tagKey.equals("0018,0088")) {
                this.setResolutions(Float.parseFloat(tag.getValue(false).toString()), 2); // type 1
                setUnitsOfMeasure(Unit.MILLIMETERS, 2);
            } else if (tagKey.equals("0028,0100")) {
                bitsAllocated = ((Short) tag.getValue(false)).shortValue(); // type 1
            } else if (tagKey.equals("0028,0103")) {
                pixelRepresentation = ((Short) tag.getValue(false)).shortValue(); // type 1
            } else if (tagKey.equals("0028,1052")) {
                this.setRescaleIntercept(Double.valueOf((String) tag.getValue(false)).doubleValue());  // type 1
                // only used in CT images, so don't notify that not found
            } else if (tagKey.equals("0028,1053")) {
                this.setRescaleSlope(Double.valueOf((String) tag.getValue(false)).doubleValue()); // type 1
                // only used in CT and PET images, so don't notify that not found
            } else if (tagKey.equals("0018,0050")) {

                setSliceThickness(Float.parseFloat(((String) tag.getValue(false)).trim())); // type 2
            } else if (tagKey.equals("0018,602C")) {
                setResolutions(((Double) tag.getValue(false)).floatValue(), 0);
                setUnitsOfMeasure(Unit.CENTIMETERS, 0);
            } else if (tagKey.equals("0018,602E")) {
                setResolutions(((Double) tag.getValue(false)).floatValue(), 1);
                setUnitsOfMeasure(Unit.CENTIMETERS, 1);
            } else if (tagKey.equals("0020,0032")) { // type 2c
                orientation = ((String) tag.getValue(false)).trim();
                int index1 = -1, index2 = -1;
                for (int i = 0; i < orientation.length(); i++) {

                    if (orientation.charAt(i) == '\\') {

                        if (index1 == -1) {
                            index1 = i;
                        } else {
                            index2 = i;
                        }
                    }
                }
                if (index1 != -1) {
                    xLocation = Float.valueOf(orientation.substring(0, index1)).floatValue();
                }
                if (index1 != -1 && index2 != -1) {
                    yLocation = Float.valueOf(orientation.substring(index1 + 1, index2)).floatValue();
                }
                if (index2 != -1) {
                    zLocation = Float.valueOf(orientation.substring(index2 + 1)).floatValue();
                }

            } else if (tagKey.equals("0020,0013")) { // type 2
                instanceNumber = Integer.parseInt(tag.getValue(true).toString());
            } else if (tagKey.equals("0020,1041")) { // type 3
                sliceLocation = Float.valueOf(tag.getValue(true).toString()).floatValue();
            } else if (tagKey.equals("0028,0030")
                    && ((tagTable.get("0018,1164") == null) || (tagTable.get("0018,1164").getValue(false) == null))) { // type 2
                // y resolution followed by x resolution

                String valueStr = ((String) tag.getValue(false)).trim();
                String firstHalf, secondHalf;
                int index = 0;

                for (int i = 0; i < valueStr.length(); i++) {

                    if (valueStr.charAt(i) == '\\') {
                        index = i;
                    }
                }

                firstHalf = valueStr.substring(0, index).trim();
                secondHalf = valueStr.substring(index + 1, valueStr.length()).trim();

                Float f1 = null;
                Float f2 = null;

                if ((firstHalf != null) && (firstHalf.length() > 0)) {

                    try {
                        f1 = new Float(firstHalf);
                    } catch (NumberFormatException e) {
                        setResolutions(1.0f, 1);
                        // MipavUtil.displayError("Number format error: Pixel spacing = " + s);
                    }

                    if (f1 != null) {
                        setResolutions(f1.floatValue(), 1);
                    }
                } else {
                    setResolutions(1.0f, 1);
                }

                if ((secondHalf != null) && (secondHalf.length() > 0)) {

                    try {
                        f2 = new Float(secondHalf);
                    } catch (NumberFormatException e) {
                        setResolutions(getResolution(1), 0);
                        // MipavUtil.displayError("Number format error: Pixel spacing = " + s);
                    }

                    if (f2 != null) {
                        setResolutions(f2.floatValue(), 0);
                    }
                } else {
                    setResolutions(1.0f, 0);
                }
            } else if (tagKey.equals("0018,1164")) { // type 2

                String valueStr = ((String) tag.getValue(false)).trim();
                String firstHalf, secondHalf;
                int index = 0;

                for (int i = 0; i < valueStr.length(); i++) {

                    if (valueStr.charAt(i) == '\\') {
                        index = i;
                    }
                }

                firstHalf = valueStr.substring(0, index).trim();
                secondHalf = valueStr.substring(index + 1, valueStr.length()).trim();

                Float f1 = null;
                Float f2 = null;

                if ((firstHalf != null) && (firstHalf.length() > 0)) {

                    try {
                        f1 = new Float(firstHalf);
                    } catch (NumberFormatException e) {
                        setResolutions(1.0f, 0);
                        // MipavUtil.displayError("Number format error: Pixel spacing = " + s);
                    }

                    if (f1 != null) {
                        setResolutions(f1.floatValue(), 0);
                    }
                } else {
                    setResolutions(1.0f, 0);
                }

                if ((secondHalf != null) && (secondHalf.length() > 0)) {

                    try {
                        f2 = new Float(secondHalf);
                    } catch (NumberFormatException e) {
                        setResolutions(getResolution(0), 1);
                        // MipavUtil.displayError("Number format error: Pixel spacing = " + s);
                    }

                    if (f2 != null) {
                        setResolutions(f2.floatValue(), 1);
                    }
                } else {
                    setResolutions(1.0f, 1);
                }
            } else if (tagKey.equals("0028,0120")) { // type 3
                pixelPaddingValue = (Short) tag.getValue(false);
            } else if (tagKey.equals("0028,0006")) {
                planarConfig = ((Number) tag.getValue(false)).shortValue();
            } else if (tagKey.equals("0028,0004")) { //requires bitsAllocated(0028,0100) and pixelRepresentation(0028,0103) to be set
                setInfoFromTag(tagTable.get(new FileDicomKey("0028,0100")));
                if (tagTable.get(new FileDicomKey("0028,0103")) == null) {
                } else {
                    setInfoFromTag(tagTable.get(new FileDicomKey("0028,0103")));
                }
                photometricInterp = ((String) tag.getValue(false)).trim();

                if ((photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
                        && (bitsAllocated == 1)) {
                    setDataType(BOOLEAN);
                    displayType = BOOLEAN;
                    bytesPerPixel = 1;
                } else if ((photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
                        && (pixelRepresentation == UNSIGNED_PIXEL_REP)
                        && (bitsAllocated == 8)) {
                    setDataType(UBYTE);
                    displayType = UBYTE;
                    bytesPerPixel = 1;
                } else if ((photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
                        && (pixelRepresentation == SIGNED_PIXEL_REP)
                        && (bitsAllocated == 8)) {
                    setDataType(BYTE);
                    displayType = BYTE;
                    bytesPerPixel = 1;
                } else if ((photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
                        && (pixelRepresentation == UNSIGNED_PIXEL_REP)
                        && (bitsAllocated > 16)) {
                    setDataType(UINTEGER);
                    displayType = UINTEGER;
                    bytesPerPixel = 4;
                } else if ((photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
                        && (pixelRepresentation == UNSIGNED_PIXEL_REP)
                        && (bitsAllocated > 8)) {
                    setDataType(USHORT);
                    displayType = USHORT;
                    bytesPerPixel = 2;
                } else if ((photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
                        && (pixelRepresentation == SIGNED_PIXEL_REP) && (bitsAllocated > 8)) {
                    setDataType(SHORT);
                    displayType = SHORT;
                    bytesPerPixel = 2;
                } else if (photometricInterp.equals("RGB") && (bitsAllocated == 8)) { //requires 0028,0006 to be set
                    setInfoFromTag(tagTable.get(new FileDicomKey("0028,0006")));
                    setDataType(ARGB);
                    displayType = ARGB;
                    bytesPerPixel = 3;
                } else if (photometricInterp.equals("RGB") && (bitsAllocated == 16)) { //requires 0028,0006 to be set
                    setInfoFromTag(tagTable.get(new FileDicomKey("0028,0006")));
                    setDataType(ARGB_USHORT);
                    displayType = ARGB_USHORT;
                    bytesPerPixel = 6;
                } else if (photometricInterp.equals("YBR_FULL_422") && (bitsAllocated == 8)) {  //requires 0028,0006 to be set
                    setInfoFromTag(tagTable.get(new FileDicomKey("0028,0006")));
                    setDataType(ARGB);
                    displayType = ARGB;
                    bytesPerPixel = 3;
                } else if (photometricInterp.equals("PALETTE COLOR")
                        && (pixelRepresentation == UNSIGNED_PIXEL_REP)
                        && (bitsAllocated == 8)) {
                    setDataType(UBYTE);
                    displayType = UBYTE;
                    bytesPerPixel = 1;

                    final int[] dimExtents = new int[2];
                    dimExtents[0] = 4;
                    dimExtents[1] = 256;

                } else {
                }
            } else if (tagKey.equals("0028,1201") || tagKey.equals("0028,1202") || tagKey.equals("0028,1203")) {
                //for keyNum, from dicom standard, 1 is red, 2 is green, 3 is blue
                int keyNum = Integer.valueOf(tagKey.getElement().substring(tagKey.getElement().length() - 1));
                Object data = tag.getValue(false);
                if (data instanceof Number[]) {
                    final int lutVals = ((Number[]) data).length;

                }

            }
        } catch (NumberFormatException ex) {

        }
    }

    /**
     * Sets (copies) units of measure for image.
     *
     * @param unitMeasure unit of measure for a specified dimension
     */
    public final void setUnitsOfMeasure(final Unit[] unitMeasure) {
        if (unitMeasure != null) {
            unitsOfMeasure = unitMeasure.clone();
        }
    }

    /**
     * Sets (copies) units of measure for image.
     *
     * @param unitMeasure unit of measure for a specified dimension
     */
    public final void setUnitsOfMeasure(final int[] unitMeasure) {
        if (unitMeasure != null) {
            Unit[] localMeasure = new Unit[unitMeasure.length];
            for (int i = 0; i < unitMeasure.length; i++) {
                localMeasure[i] = Unit.getUnitFromLegacyNum(unitMeasure[i]);
            }

            setUnitsOfMeasure(localMeasure);
        }
    }

    /**
     * Photometric interpretion.
     *
     * <table border=true>
     * <tr>
     * <td>1 indicates</td>
     * <td>0 is black</td>
     * </tr>
     * <tr>
     * <td>0 indicates</td>
     * <td>0 is white</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>RGB</td>
     * </tr>
     * <tr>
     * <td>3</td>
     * <td>indexed color LUT is saved with image</td>
     * </tr>
     * <tr>
     * <td>4</td>
     * <td>Transparency Mask</td>
     * </tr>
     * </table>
     *
     * @return short Returns interpretation
     */
    public final short getPhotometric() {
        return photometric;
    }

    /**
     * Returns the resolution of the requested dimension.
     *
     * @param dim The dimension to return the resolution of.
     *
     * @return The resolution of one of the image dimensions.
     */
    public final double getResolution(final int dim) {
        return dimResolutions[dim];
    }

    /**
     * Returns each dimension's resolution.
     *
     * @return float[] dimension resolutions
     */
    public final double[] getResolutions() {
        return dimResolutions;
    }

    /**
     * Returns the size of the slice image in byte which represented by this
     * object.
     *
     * @return the size of the slice image in byte which represented by this
     * object.
     */
    public int getSize() {
        final int[] extents = this.getExtents();

        if ((extents == null) || (extents.length < 2)) {
            return -1;
        }

        return extents[0] * extents[1] * FileInfoBase.getNumOfBytesPerPixel(getDataType());
    }

    /**
     * Returns the thickness of the image slices.
     *
     * @return slice thickness
     */
    public final float getSliceThickness() {
        return sliceThickness;
    }

    /**
     * Returns the volume unit for the data. Assumes all three dimensions are
     * the same units.
     *
     * @return String associated volume unit of measure.
     */
    public String getVolumeUnitsOfMeasureStr() {
        if (getUnitsOfMeasure(0) != getUnitsOfMeasure(1) || getUnitsOfMeasure(1) != getUnitsOfMeasure(2)) {
            return Unit.getUnitFromLegacyNum(getUnitsOfMeasure(0)) + "*"
                    + Unit.getUnitFromLegacyNum(getUnitsOfMeasure(1)) + "*"
                    + Unit.getUnitFromLegacyNum(getUnitsOfMeasure(2));
        }
        return Unit.getUnitFromLegacyNum(getUnitsOfMeasure(0)).toString() + "^3";
    }

    /**
     * Returns the area unit for the data. Assumes both dimensions are the same
     * units.
     *
     * @return String associated volume unit of measure.
     */
    public String getAreaUnitsOfMeasureStr() {
        if (getUnitsOfMeasure(0) != getUnitsOfMeasure(1)) {
            return Unit.getUnitFromLegacyNum(getUnitsOfMeasure(0)) + "*"
                    + Unit.getUnitFromLegacyNum(getUnitsOfMeasure(1));
        }
        return Unit.getUnitFromLegacyNum(getUnitsOfMeasure(0)) + "^2";
    }

    /**
     * isDicomOrdered() returns true if the file is in dicom order, false
     * otherwise.
     *
     * @return true if the file is in dicom order, false otherwise
     */
    public boolean isDicomOrdered() {

        if ((axisOrientation[0] == FileInfoBase.ORI_R2L_TYPE) && (axisOrientation[1] == FileInfoBase.ORI_A2P_TYPE)
                && (axisOrientation[2] == FileInfoBase.ORI_I2S_TYPE)) {
            return true;
        }

        return false;
    }

    /**
     * Sets (copies) orientation of each axis.
     *
     * @param axOrient axis orientation array
     *
     * @see #getAxisOrientation()
     */
    public void setAxisOrientation(final int[] axOrient) {

        if ((axOrient == null) || (axOrient.length != 3)) {
            return;
        }

        axisOrientation[0] = axOrient[0];
        axisOrientation[1] = axOrient[1];
        axisOrientation[2] = axOrient[2];
    }

    /**
     * Sets the image orientation in the specified axis. Creates the
     * axisOrientation if the array has not yet been created.
     *
     * @param axOrient orientation
     * @param axis axis of orientation; x is 0, y is 1, z is 2.
     */
    public void setAxisOrientation(final int axOrient, final int axis) {

        // System.out.println("axis orient is " + axOrient);
        if ((axis < 0) || (axis > 2)) {
            return;
        }

        if ((axOrient == FileInfoBase.ORI_UNKNOWN_TYPE) || (axOrient == FileInfoBase.ORI_A2P_TYPE)
                || (axOrient == FileInfoBase.ORI_P2A_TYPE) || (axOrient == FileInfoBase.ORI_R2L_TYPE)
                || (axOrient == FileInfoBase.ORI_L2R_TYPE) || (axOrient == FileInfoBase.ORI_S2I_TYPE)
                || (axOrient == FileInfoBase.ORI_I2S_TYPE)) {
            axisOrientation[axis] = axOrient;
        } else {
            axisOrientation[axis] = FileInfoBase.ORI_UNKNOWN_TYPE;
        }
    }

    /**
     * Sets the compression type.
     *
     * @param type compression type
     */
    public void setCompressionType(final int type) {
        this.compressionType = type;
    }

    /**
     * Public method for populating ModelImage data fields.
     */
    public final void setInfoFromTags() {
        setInfoFromTags(this.tagTable, false);
    }

    /**
     * After the tag table is filled, check for a number of tags to set up some
     * fields in this file info object.
     */
    private final void setInfoFromTags(FileDicomTagTable tagTable, boolean insideSequenceTag) {
        HashMap<Integer, LengthStorageUnit> lengthComp = new HashMap<Integer, LengthStorageUnit>();
        Iterator<Map.Entry<FileDicomKey, FileDicomTag>> itr = tagTable.getTagList().entrySet().iterator();
        FileDicomTag tag = null;

        while (itr.hasNext()) {
            tag = itr.next().getValue();
            if (!insideSequenceTag && tag.getElement() != 0) {
                appendLengthTag(tag, lengthComp);
            }
            if (tag.getValue(false) instanceof FileDicomSQ) {
                FileDicomSQ sq = (FileDicomSQ) tag.getValue(false);
                for (int i = 0; i < sq.getSequence().size(); i++) {
                    setInfoFromTags(sq.getSequence().get(i), true);
                }
            }
            setInfoFromTag(tag);
        }

        updateLengthTags(lengthComp);

        if (getModality() == FileInfoBase.POSITRON_EMISSION_TOMOGRAPHY) {
            displayType = FLOAT;
            // a bit of a hack - indicates Model image should be reallocated to float for PET image the data is
            // stored
            // as 2 bytes (short) but is "normalized" using the slope parameter required for PET images (intercept
            // always 0 for PET).
        }

        if (((getDataType() == UBYTE) || (getDataType() == USHORT))
                && (getRescaleIntercept() < 0)) {
            // this performs a similar method as the pet adjustment for float images stored on disk as short to read
            // in
            // signed byte and signed short images stored on disk as unsigned byte or unsigned short with a negative
            // rescale intercept
            if (getDataType() == UBYTE) {
                displayType = BYTE;
            } else if (getDataType() == USHORT) {
                displayType = SHORT;
            }
        }
    }

}
