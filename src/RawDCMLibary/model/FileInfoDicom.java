package RawDCMLibary.model;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import RawDCMLibary.DICOM.DICOMFile;
import static RawDCMLibary.DICOM.DICOMFile.ARGB;
import static RawDCMLibary.DICOM.DICOMFile.ARGB_USHORT;
import static RawDCMLibary.DICOM.DICOMFile.BOOLEAN;
import static RawDCMLibary.DICOM.DICOMFile.BYTE;
import static RawDCMLibary.DICOM.DICOMFile.FLOAT;
import static RawDCMLibary.DICOM.DICOMFile.SHORT;
import static RawDCMLibary.DICOM.DICOMFile.UBYTE;
import static RawDCMLibary.DICOM.DICOMFile.UINTEGER;
import static RawDCMLibary.DICOM.DICOMFile.USHORT;
import RawDCMLibary.model.FileDicomTagInfo.VR;
import RawDCMLibary.model.enums.Enums.Unit;
import RawDCMLibary.model.enums.Enums.VRtype;


/**
 * <p>This class contains DICOM header information. It uses a table to store all the information about the tags. The
 * FileDicomTagTable listing all known tags with empty values is in DicomDictionary. Also stored here is the offset of
 * the image, the image number, the resolutions, some pixel data, the bytes allocated, and the dimensions of the
 * image.</p>
 *
 * <p>This file also contains a table used for displaying the tag information. There is an option to display just the
 * standard tags or both the standard and the private tags.</p>
 *
 * @version  1.0 Aug 1, 1999
 * @see      FileDicom
 * @see      FileDicomTag
 * @see      FileDicomTagTable
 * @see      JDialogFileInfoDICOM
 */
public class FileInfoDicom extends FileInfoBase {

    //~ Static fields/initializers -------------------------------------------------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -3072660161266896186L;

    
    /** (0028,0103) Pixel Representations */
    public static final int UNDEFINED_PIXEL_REP = -1;
    
    /** (0028,0103) Pixel Representations */
    public static final int UNSIGNED_PIXEL_REP = 0;
    
    /** (0028,0103) Pixel Representations */
    public static final int SIGNED_PIXEL_REP = 1;

    /**
     * these are the DICOM tag ID numbers corresponding to the tags which are anonymized. They are kept here for
     * simplicity of updating (when it is decided that other tags are must be anonymized) and to the advantage of
     * calling objects.
     */
    public static final String[] anonymizeTagIDs = {
        "0010,0010", // patient name
        "0010,0020", // patient ID
        "0010,0030", // patient's birth date
        "0010,0032", // patient's birth time
        "0010,0040", // patient's sex
        "0010,0050", // patient Insurance plan code sequence
        "0010,1000", // other patient IDs
        "0010,1001", // other patient names
        "0010,1005", // patient's birth name
        "0010,1010", // patient's age
        "0010,1020", // patient's size
        "0010,1030", // patient's weight
        "0010,1040", // patient's address
        "0010,1060", // patient's mother's birth name
        "0010,1090", // medical record locator
        "0010,2154", // patient's telephone numbers
        "0010,2160", // patient ethnic group
        "0010,2180", // occupation
        "0010,21B0", // additional patient's history
        "0010,21D0", // patient's last menstrual date
        "0010,21F0", // patient religious preference
        "0010,4000", // patient comments.

        "0008,0014", // instance creator UID
        "0008,0018", // SOP instance UID
        "0008,0050", // accession number
        "0008,0080", // institution name
        "0008,0081", // institution address
        "0008,0090", // referring physician's name
        "0008,0092", // referring physician's address
        "0008,0094", // referring physician's telephone numbers
        "0008,1010", // station name
        "0008,1030", // study description
        "0008,103E", // series description
        "0008,1040", // institutional department name
        "0008,1048", // physician(s) of record
        "0008,1050", // performing physician's name
        "0008,1060", // name of physician reading study
        "0008,1070", // operator's name
        "0008,1080", // admitting diagnoses description
        "0008,1155", // Referenced SOP instance UID
        "0008,2111", // derivation description

        "0018,1000", // device serial number
        "0018,1030", // protocol name

        "0020,000D", // study instance UID
        "0020,000E", // series instance UID
        "0020,0010", // study ID
        "0020,0052", // frame of reference UID
        "0020,0200", // synchronization frame of reference UID
        "0020,4000", // image comments

        // "0040,0275",// request attributes sequence
        "0040,A124", // UID
        "0040,A730", // content sequence
        
        "0088,0140", // storage media file-set UID

        "3006,0024", // referenced frame of reference UID
        "3006,00C2", // related frame of reference UID
    };

    //~ Instance fields ------------------------------------------------------------------------------------------------

    /** Bits allocated per pixel. */
    public short bitsAllocated = -1;

    /** Number of bytes per pixel. */
    public short bytesPerPixel = 1;

    /** True if the DICOM file has the ID DICM at beginning at file pointer 128. */
    public boolean containsDICM = true;

    /** Data type. */
    public int displayType;

    /** DICOM instance number. */
    public int instanceNumber = -1;

    /** Whether the tag currently being processed and read in is a sequence tag. */
    public boolean isCurrentTagSQ = false;

    /** True indicates image has multiple image planes in a single file. */
    public boolean multiFrame = false;

    // Most of this variables are used to simplify access to important tags. Should
    // NOT be used when writing images to file since some of the info may not
    // be up to data. Might clean this up at some time in the future.

    /** True is image format < DICOM 3.0. */
    public boolean olderVersion = false; // an older version is a version < DICOM v3.0

    /** Orientation - SAGITTAL, CORONAL, AXIAL. */
    public String orientation;

    /** Type of image - color, monochrome etc. */
    public String photometricInterp = "MONOCHROME2";

    /**
     * Pixel Padding Value (0028, 0120). Value of pixels added to non-rectangular image to pad to rectangular format.
     */
    public Short pixelPaddingValue = null;

    /** DICOM tag (0028, 0103) 
     *  -1 = undefined
     *   0 = unsigned 
     *   1 = signed */
    public short pixelRepresentation = UNDEFINED_PIXEL_REP;
    

    /** 0 = RGB, RGB, 1 = R R R, G G G, B B B. */
    public short planarConfig = 0; //

    /** DICOM slice location. */
    public float sliceLocation;

    /** VR type can be IMPLICIT or EXPLICIT. */
    private VRtype vr_type = VRtype.IMPLICIT;

    /** DICOM x coordianate of the slice location. */
    public float xLocation = 0;

    /** DICOM y coordianate of the slice location. */
    public float yLocation = 0;

    /** DICOM z coordianate of the slice location. */
    public float zLocation = 0;

    /** Stores all the information about the DICOM tags. */
    private FileDicomTagTable tagTable;
    
    /** whether it is enhanced dicom or not **/
    private boolean isEnhancedDicom = false;

    //~ Constructors ---------------------------------------------------------------------------------------------------

    /**
     * DICOM file information constructor. Used for the file info which contains the reference tag table for an image.
     *
     * @param  name       file name
     * @param  directory  file directory
     * @param  format     format (in this case, DICOM)
     */
    public FileInfoDicom(String name, String directory, int format) {
        this(name, directory, format, null);
    }

    /**
     * DICOM file information constructor. Used for the file info which does NOT contain the reference tag table for an
     * image.
     *
     * @param  name       file name
     * @param  directory  file directory
     * @param  format     format (in this case, DICOM)
     * @param  refInfo    The reference file info, containing the reference tag table.
     */
    public FileInfoDicom(String name, String directory, int format, DICOMFile refInfo) {
        
        FileDicomTagTable refTagTable = null;
        if(refInfo != null) {
            this.vr_type = refInfo.getVrType();
            refTagTable = refInfo.getTagTable();
        }
            
        tagTable = new FileDicomTagTable(refTagTable, vr_type);
    }

    //~ Methods --------------------------------------------------------------------------------------------------------

    /**
     * Prepares this class for cleanup.
     */
    public void finalize() {
        orientation = null;
        photometricInterp = null;

        if (tagTable != null) {
            tagTable.reset();
            //tagTable.finalize();
        }

        tagTable = null;

        super.finalize();
    }
    
    /**
     * returns the ModelLUT, if there, which might be specified by the <code>0028,1201</code>, <code>0028,1202</code>,
     * <code>0028,1203</code> tags.
     * 
     * @return DOCUMENT ME!
     */


    /**
     * This method extracts directional cosines from the DICOM image header (tag = "0020, 0037"). Since the third row of
     * transformation matrix is not given in the tag but can be derived by taking the cross product. The result is then
     * put into a MIPAV transformation matrix object and returned.
     *
     * @return  the transformation object extracted from the DICOM header. If this tag is null then the class returns
     *          null.
     */
    public void getPatientOrientation() {
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
            if (index6 == notSet)
                dirCos[1][2] = Double.valueOf(orientation.substring(index5 + 1)).doubleValue();
            else
                dirCos[1][2] = Double.valueOf(orientation.substring(index5 + 1, index6)).doubleValue();
                
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
     * @return  A reference to the tag table.
     */
    public final FileDicomTagTable getTagTable() {
        return tagTable;
    }

    /**
     * @param tagTable the tagTable to set, useful when a tag table has been populated from
     * an enhanced DICOM series.
     */
    public final void setTagTable(FileDicomTagTable tagTable) {
        this.tagTable = tagTable;
    }

    /**
     * Accessor for the status of this dicom info.
     *
     * @return  boolean <code>true</code> for images that think they are multi-frame.
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
     * Parse the string for Objects seperated by "\". Uses the local method getValue to look at the Object held in the
     * tag and decipher it for the user class into an array of strings.
     *
     * <p>parseTagValue has not been updated for the newer versions of DicomTag</p>
     *
     * @param   tagName  The name given as the key to search the Hashtable for.
     *
     * @return  the array of Strings that were coded into the tag; a single string is given if the string in the tag has
     *          no "\" seporators. Each String in the array is a single value, and there are value multiplicity number
     *          of Strings in the array. NOTE: user class must convert this list to the correct type; (which does
     *          indicate user class knows what returned string is) If null then there were zero tokens in the tag
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
     * In anonymization cases it may be necessary to hide the fact that MIPAV has processed this image 
     * (to remove any NIH affiliation).  This method removes those tags that identify any secondary capture device information
     * contained within the image, to make it appear as if the image has not been processed.
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
    public final void setInfoFromTags() {
        setInfoFromTags(this.tagTable, false);
    }
    
    /**
     * After the tag table is filled, check for a number of tags to set up some fields in this file info object.
     */
    private final void setInfoFromTags(FileDicomTagTable tagTable, boolean insideSequenceTag) {
        HashMap<Integer, LengthStorageUnit> lengthComp = new HashMap<Integer, LengthStorageUnit>();
        Iterator<Entry<FileDicomKey,FileDicomTag>> itr = tagTable.getTagList().entrySet().iterator();
        FileDicomTag tag = null;
       
        while(itr.hasNext()) {
            tag = itr.next().getValue();
            if(!insideSequenceTag && tag.getElement() != 0) {
                appendLengthTag(tag, lengthComp);
            }
            if(tag.getValue(false) instanceof FileDicomSQ) {
                FileDicomSQ sq = (FileDicomSQ) tag.getValue(false);
                for(int i=0; i<sq.getSequence().size(); i++) {
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

        if ( ( (getDataType() == UBYTE) || (getDataType() == USHORT))
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
    
    private void updateLengthTags(HashMap<Integer, LengthStorageUnit> lengthComp) {
        Iterator<Integer> itr = lengthComp.keySet().iterator();
        while(itr.hasNext()) {
            int group = itr.next();
            try {
                Integer length = (Integer) tagTable.get(new FileDicomKey(group, 0)).getValue(false);
                tagTable.get(new FileDicomKey(group, 0)).setValue(new Integer(lengthComp.get(group).get()));
                
            } catch(NullPointerException e) {
                //A length for this group does not exist, this is allowable in DICOM.
            }
        }
    }

    private void appendLengthTag(FileDicomTag tag, HashMap<Integer, LengthStorageUnit> lengthComp) {
        LengthStorageUnit length = null;
        int group = tag.getGroup();
        if((length = lengthComp.get(group)) == null) {
            lengthComp.put(group, length = new LengthStorageUnit(0));
        }
        if(tag.getValueRepresentation() != VR.SQ) {
            length.add(tag.getDataLength());
        } else {
            length.add(((FileDicomSQ)tag.getValue(false)).getDataLength());
        }
        if(vr_type == VRtype.EXPLICIT) {
            if(tag.getType().reservedBytes()) {
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
     * This class is a basic storage unit for a primitive integer variable.  By creating this storage unit,
     * modified map values that are stored using this method only need to be accessed once.
     * 
     * @author senseneyj
     *
     */
    class LengthStorageUnit {
        
        /**Internal integer value stored by this unit. */
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
     * Changes a few dicom tags associated with a secondary capture image (marking it as having been created with
     * mipav). This is often used after a new image is created as the result of an algorithm and the source image's file
     * infos are copied over to the new image.
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
            Logger.getLogger(FileInfoDicom.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns the MIPAV modality constant for a given DICOM modality string.
     *
     * @param   value  The DICOM modality tag value.
     *
     * @return  The equivalent MIPAV modality constant (or UNKNOWN_MODALITY if there is no equivalent found).
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

    /**
     * Sets fields in this file info based on a given tag's key and value.
     *
     * @param  tag  The tag to use to update the file info fields (not all tags cause field changes).
     */
    protected final void setInfoFromTag(FileDicomTag tag) {
        FileDicomKey tagKey = tag.getInfo().getKey();
        //TODO: JDK7 allows this to turn into switch statements
        // ordering by type 1 tags first.  Then in numerical order.
        try {
            if (tagKey.equals("0008,0060")) {
                setModalityFromDicomStr(((String) tag.getValue(false)).trim()); // type 1
                // setModalityFromDicomStr() covers the possibility of value == ""
            } else if (tagKey.equals("0018,0088")) {
                super.setResolutions(Float.parseFloat(tag.getValue(false).toString()), 2); // type 1
                setUnitsOfMeasure(Unit.MILLIMETERS, 2);
            } else if (tagKey.equals("0028,0100")) {
                bitsAllocated = ((Short) tag.getValue(false)).shortValue(); // type 1
            } else if (tagKey.equals("0028,0103")) {
                pixelRepresentation = ((Short) tag.getValue(false)).shortValue(); // type 1
            } else if (tagKey.equals("0028,1052")) {
                super.setRescaleIntercept(Double.valueOf((String) tag.getValue(false)).doubleValue());  // type 1
                // only used in CT images, so don't notify that not found
            } else if (tagKey.equals("0028,1053")) {
                super.setRescaleSlope(Double.valueOf((String) tag.getValue(false)).doubleValue()); // type 1
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
                if (index1 != -1)
                    xLocation = Float.valueOf(orientation.substring(0, index1)).floatValue();
                if (index1 != -1 && index2 != -1)
                    yLocation = Float.valueOf(orientation.substring(index1 + 1, index2)).floatValue();
                if (index2 != -1)
                    zLocation = Float.valueOf(orientation.substring(index2 + 1)).floatValue();
                    
            } else if (tagKey.equals("0020,0013")) { // type 2
                instanceNumber = Integer.parseInt(tag.getValue(true).toString());
            } else if (tagKey.equals("0020,1041")) { // type 3
                sliceLocation =  Float.valueOf(tag.getValue(true).toString()).floatValue();
            } else if (tagKey.equals("0028,0030") &&
                           ((tagTable.get("0018,1164") == null) || (tagTable.get("0018,1164").getValue(false) == null))) { // type 2
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
                planarConfig = ((Number)tag.getValue(false)).shortValue();
            } else if(tagKey.equals("0028,0004")) { //requires bitsAllocated(0028,0100) and pixelRepresentation(0028,0103) to be set
                setInfoFromTag(tagTable.get(new FileDicomKey("0028,0100")));
                if (tagTable.get(new FileDicomKey("0028,0103"))  != null) {       
                    setInfoFromTag(tagTable.get(new FileDicomKey("0028,0103")));
                }
                photometricInterp = ((String) tag.getValue(false)).trim();
                
                if ( (photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
                        && (bitsAllocated == 1)) {
                    setDataType(BOOLEAN);
                    displayType = BOOLEAN;
                    bytesPerPixel = 1;
                } else if ( (photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
                        && (pixelRepresentation == FileInfoDicom.UNSIGNED_PIXEL_REP)
                        && (bitsAllocated == 8)) {
                    setDataType(UBYTE);
                    displayType = UBYTE;
                    bytesPerPixel = 1;
                } else if ( (photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
                        && (pixelRepresentation == FileInfoDicom.SIGNED_PIXEL_REP)
                        && (bitsAllocated == 8)) {
                    setDataType(BYTE);
                    displayType = BYTE;
                    bytesPerPixel = 1;
                } else if ( (photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
                        && (pixelRepresentation == FileInfoDicom.UNSIGNED_PIXEL_REP)
                        && (bitsAllocated > 16)) {
                    setDataType(UINTEGER);
                    displayType = UINTEGER;
                    bytesPerPixel = 4;
                } else if ( (photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
                        && (pixelRepresentation == FileInfoDicom.UNSIGNED_PIXEL_REP)
                        && (bitsAllocated > 8)) {
                    setDataType(USHORT);
                    displayType = USHORT;
                    bytesPerPixel = 2;
                } else if ( (photometricInterp.equals("MONOCHROME1") || photometricInterp.equals("MONOCHROME2"))
                        && (pixelRepresentation == FileInfoDicom.SIGNED_PIXEL_REP) && (bitsAllocated > 8)) {
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
                        && (pixelRepresentation == FileInfoDicom.UNSIGNED_PIXEL_REP)
                        && (bitsAllocated == 8)) {
                    setDataType(UBYTE);
                    displayType = UBYTE;
                    bytesPerPixel = 1;
    
                    final int[] dimExtents = new int[2];
                    dimExtents[0] = 4;
                    dimExtents[1] = 256;
                }
            } else if(tagKey.equals("0028,1201") || tagKey.equals("0028,1202") || tagKey.equals("0028,1203")) {
            //for keyNum, from dicom standard, 1 is red, 2 is green, 3 is blue
                int keyNum = Integer.valueOf(tagKey.getElement().substring(tagKey.getElement().length()-1));
                Object data = tag.getValue(false);
                if (data instanceof Number[]) {
                    final int lutVals = ((Number[])data).length;
                    
                }
                
            } 
        } catch(NumberFormatException ex) {
            System.err.println("Tag "+tag.getKey().toString()+" does not contain a number.");
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
     * @param  value  Object used is the value of DICOM tag (0008,0060)
     */
    protected final void setModalityFromDicomStr(String value) {
        super.setModality(getModalityFromDicomStr(value));
    }

    public VRtype getVr_type() {
        return vr_type;
    }

    public void setVr_type(VRtype vr_type) {
        this.vr_type = vr_type;
    }
}
