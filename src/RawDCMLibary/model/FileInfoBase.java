package RawDCMLibary.model;

import static RawDCMLibary.DICOM.DICOMFile.*;
import RawDCMLibary.model.enums.Enums.DataType;
import RawDCMLibary.model.enums.Enums.Unit;
import RawDCMLibary.model.enums.Enums.UnitType;


/**
 * This structure contains the basic information that describes how the image is stored on disk.
 * 
 * <p>
 * Subclasses add additional information which is particular to that image-format.
 * </p>
 * 
 * <p>
 * This class needs work
 * </p>
 * 
 * <p>
 * 1. fixing (making consistent what to do when null pointers encountered. see getStartLocation and getUnitsOfMeasure
 * </p>
 * 
 * @version 0.9 June 30, 1998
 * @author Matthew J. McAuliffe, Ph.D.
 * @see FileBase
 */
public abstract class FileInfoBase {

    // ~ Static fields/initializers
    // -------------------------------------------------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 6605143084958470864L;

    /**
     * Each unit has a unit type with a given base. The base unit for a unit type is the SI base unit
     * as reported here: http://physics.nist.gov/cuu/Units/units.html
     * 
     * @author senseneyj
     *
     */
    

    /** Array of space units: inches, mm, etc. 
     * @deprecated 
     * */
    public static final String[] sUnits = {"Unknown", "Unknown", "Inches", "mils", "cm", "A", "nm", "um", "mm", "m", "km",
            "miles"};

    /** Array of time units: seconds, minutes, etc. 
     * @deprecated 
     */
    public static final String[] tUnits = {"nano seconds", "micro seconds", "milli seconds", "seconds", "minutes",
            "hours", "hertz", "part per million", "radians per second"};
    
    /** Unit of measurement unknown. */
    public static final int UNKNOWN_MEASURE = 1;

    /** Unit of measurement inches. */
    public static final int INCHES = 2;
    
    /** Units of measurement mil (thousandth of an inch) */
    public static final int MILS = 3;

    /** Unit of measurement centimeters. */
    public static final int CENTIMETERS = 4;

    /** Unit of measurement angstroms. */
    public static final int ANGSTROMS = 5;

    /** Unit of measurement nanometers. */
    public static final int NANOMETERS = 6;

    /** Unit of measurement micrometers. */
    public static final int MICROMETERS = 7;

    /** Unit of measurement millimeters. */
    public static final int MILLIMETERS = 8;

    /** Unit of measurement meters. */
    public static final int METERS = 9;

    /** Unit of measurement kilometers. */
    public static final int KILOMETERS = 10;

    /** Unit of measurement miles. */
    public static final int MILES = 11;

    /** Unit of measurement nanoseconds. */
    public static final int NANOSEC = 12;

    /** Unit of measurement microseconds. */
    public static final int MICROSEC = 13;

    /** Unit of measurement milliseconds. */
    public static final int MILLISEC = 14;

    /** Unit of measurement seconds. */
    public static final int SECONDS = 15;

    /** Unit of measurement minutes. */
    public static final int MINUTES = 16;

    /** Unit of measurement hours. */
    public static final int HOURS = 17;

    /** Unit of measurement hertz. */
    public static final int HZ = 18;

    /** Unit of measurement part-per-million. */
    public static final int PPM = 19;

    /** Radians per second. */
    public static final int RADS = 20;

    public static final int DEGREES = 21;

    /** String version of units of measurement - unknown. */
    public static final String UNKNOWN_STRING = "Unknown";

    /** String version of units of measurement - inches. */
    public static final String INCHES_STRING = "Inches";
    
    /** String version of units of measurement - mils. */
    public static final String MILS_STRING = "Mils";

    /** String version of units of measurement - centimeters. */
    public static final String CENTIMETERS_STRING = "Centimeters";

    /** String version of units of measurement - angstroms. */
    public static final String ANGSTROMS_STRING = "Angstroms";

    /** String version of units of measurement - nanometers. */
    public static final String NANOMETERS_STRING = "Nanometers";

    /** String version of units of measurement - micrometers. */
    public static final String MICROMETERS_STRING = "Micrometers";

    /** String version of units of measurement - millimeters. */
    public static final String MILLIMETERS_STRING = "Millimeters";

    /** String version of units of measurement - meters. */
    public static final String METERS_STRING = "Meters";

    /** String version of units of measurement - kilometers. */
    public static final String KILOMETERS_STRING = "Kilometers";

    /** String version of units of measurement - miles. */
    public static final String MILES_STRING = "Miles";

    /** String version of units of measurement - nanoseconds. */
    public static final String NANOSEC_STRING = "Nanoseconds";

    /** String version of units of measurement - microseconds. */
    public static final String MICROSEC_STRING = "Microseconds";

    /** String version of units of measurement - milliseconds. */
    public static final String MILLISEC_STRING = "Milliseconds";

    /** String version of units of measurement - seconds. */
    public static final String SECONDS_STRING = "Seconds";

    /** String version of units of measurement - minutes. */
    public static final String MINUTES_STRING = "Minutes";

    /** String version of units of measurement - hours. */
    public static final String HOURS_STRING = "Hours";

    /** String version of units of measurement - hertz. */
    public static final String HZ_STRING = "Hertz";

    /** String version of units of measurement - part-per-million. */
    public static final String PPM_STRING = "Part_Per_Million";

    /** String version of units of measurement - radians per second. */
    public static final String RADS_STRING = "Radians_Per_Second";

    /** String version of units of measurement - degrees. */
    public static final String DEGREES_STRING = "Degrees";

    /** Converting between space units. Conversion is to millimeters (the default).
     *  Table converts to mm by multiplication. Converts from mm by division.
     *  Example:
     *  inches * 25.4f = mm
     *  mm / 25.4f = inches.
     *  
     *  Converting between time units. Conversion is to seconds(the default).
     *  Table converts to seconds by multiplication. Converts from seconds by division.
     *  Example:
     *  minutes * 0.01667 = seconds
     *  seconds / 0.01667 = minutes.
     *  Hertz and radians per second convert to/from hz.
     *  parts-per-million does not convert.
     *  @deprecated should use enum conversions
     *  */
    public static final double[] conversionSpaceTimeUnits = {1, 1,
        // space units:
        25.4, 2.54e-2, 10, 1.0e-7, 1.0e-6, 1.0e-3, 1, 1.0e3, 1.0e6, 1609344, 
        // time units:
        1.0e-9, 1.0e-6, 1.0e-3, 1, 1.0/60.0, 1.0/3600.0, 
        // hertz, ppm, radians per second:
        1, 1, 0.159154943274 };
    
    public enum Modality {
    	/** Image modality unknown. */
    	UNKNOWN_MODALITY(0, "Unknown Modality"),
    	/** Image modality biomagnetic imaging. */
        BIOMAGNETIC_IMAGING(1, "Biomagnetic Imaging"),
        /** Image modality color flow doppler. */
        COLOR_FLOW_DOPPLER(2, "Color Flow Doppler"),
        /** Image modality CR. */
        COMPUTED_RADIOGRAPHY(3, "Computed Radiography"),
        /** Image modality CT. */
        COMPUTED_TOMOGRAPHY(4, "Computed Tomography"),
        /** Image modality duplex doppler. */
        DUPLEX_DOPPLER(5, "Duplex Doppler"),
        /** Image modality diaphanography. */
        DIAPHANOGRAPHY(6, "Diaphanography"),
        /** Image modality digital radiography. */
        DIGITAL_RADIOGRAPHY(7, "Digital Radiography"),
        /** Image modality endoscopy. */
        ENDOSCOPY(8, "Endoscopy"),
        /** Image modality general microscopy. */
        GENERAL_MICROSCOPY(9, "General Microscopy"),
        /** Image modality hard copy. */
        HARDCOPY(10, "Hardcopy"),
        /** Image modality intraoral radiography. */
        INTRAORAL_RADIOGRAPH(11, "Intraoral Radiography"),
        /** Image modality laser surface scan. */
        LASER_SURFACE_SCAN(12, "Laser Surface Scan"),
        /** Image modality MR angiography. */
        MAGNETIC_RESONANCE_ANGIOGRAPHY(13, "Magnetic Resonance Angiography"),
        /** Image modality mammography. */
        MAMMOGRAPHY(14, "Mammography"),
        /** Image modality MR. */
        MAGNETIC_RESONANCE(15, "Magnetic Resonance"),
        /** Image modality MR SPECT. */
        MAGNETIC_RESONANCE_SPECTROSCOPY(16, "Magnetic Resonance Spectroscopy"),
        /** Image modality nuclear medicine. */
        NUCLEAR_MEDICINE(17, "Nuclear Medicine"),
        /** Image modality other. */
        OTHER(18, "Other"),
        /** Image modality PET. */
        POSITRON_EMISSION_TOMOGRAPHY(19, "Positron Emission Tomography"),
        /** Image modality panoramtic X ray. */
        PANORAMIC_XRAY(20, "Panoramic XRay"),
        /** Image modality radio fluoroscopy. */
        RADIO_FLUOROSCOPY(21, "Radio Fluoroscopy"),
        /** Image modality radiographic imaging. */
        RADIOGRAPHIC_IMAGING(22, "Radiographic Imaging"),
        /** Image modality radiotherapy dose. */
        RADIOTHERAPY_DOSE(23, "Radiotherapy Dose"),
        /** Image modality radiotherapy image. */
        RADIOTHERAPY_IMAGE(24, "Radiotherapy Image"),
        /** Image modality radiotherapy plan. */
        RADIOTHERAPY_PLAN(25, "Radiotherapy Plan"),
        /** Image modality radiotherapy record. */
        RADIOTHERAPY_RECORD(26, "Radiotherapy Record"),
        /** Image modality radiotherapy structure set. */
        RADIOTHERAPY_STRUCTURE_SET(27, "Radiotherapy Structure Set"),
        /** Image modality slide microscopy. */
        SLIDE_MICROSCOPY(28, "Slide Microscopy"),
        /** Image modality SPECT. */
        SINGLE_PHOTON_EMISSION_COMPUTED_TOMOGRAPHY(29, "Single Photon Emission Computed Tomography"),
        /** Image modality thermography. */
        THERMOGRAPHY(30, "Thermography"),
        /** Image modality ultrasound. */
        ULTRASOUND(31, "Ultrasound"),
        /** Image modality X ray angiography. */
        XRAY_ANGIOGRAPHY(32, "XRay Angiography"),
        /** Image modality external camera photography. */
        EXTERNAL_CAMERA_PHOTOGRAPHY(33, "External Camera Photography"),
        /** Image modality Red Free. */
        RED_FREE(34, "Red Free"),
        /** Image modality Fluorescein Angiography. */
        FA(35, "FA"),
        /** Image modality IndoCyanine Green. */
        ICG(36, "ICG");
    	
    	/** The legacy number that can be used to reference a modality. */
    	private int legacyNum;
    	/** The way this modality will be displayed to the user. */
		private String printDisp;

		Modality(int legacyNum, String printDisp) {
    		this.legacyNum = legacyNum;
    		this.printDisp = printDisp;
    	}

		/* (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		public String toString() {
			return printDisp;
		}
		
		public int getLegacyNum() {
			return legacyNum;
		}
    	
    }
    
    /** Image modality unknown. */
    public static final int UNKNOWN_MODALITY = 0;

    /** Image modality biomagnetic imaging. */
    public static final int BIOMAGNETIC_IMAGING = 1;

    /** Image modality color flow doppler. */
    public static final int COLOR_FLOW_DOPPLER = 2;

    /** Image modality CR. */
    public static final int COMPUTED_RADIOGRAPHY = 3;

    /** Image modality CT. */
    public static final int COMPUTED_TOMOGRAPHY = 4;

    /** Image modality duplex doppler. */
    public static final int DUPLEX_DOPPLER = 5;

    /** Image modality diaphanography. */
    public static final int DIAPHANOGRAPHY = 6;

    /** Image modality digital radiography. */
    public static final int DIGITAL_RADIOGRAPHY = 7;

    /** Image modality endoscopy. */
    public static final int ENDOSCOPY = 8;

    /** Image modality general microscopy. */
    public static final int GENERAL_MICROSCOPY = 9;

    /** Image modality hard copy. */
    public static final int HARDCOPY = 10;

    /** Image modality intraoral radiography. */
    public static final int INTRAORAL_RADIOGRAPHY = 11;

    /** Image modality laser surface scan. */
    public static final int LASER_SURFACE_SCAN = 12;

    /** Image modality MR angiography. */
    public static final int MAGNETIC_RESONANCE_ANGIOGRAPHY = 13;

    /** Image modality mammography. */
    public static final int MAMMOGRAPHY = 14;

    /** Image modality MR. */
    public static final int MAGNETIC_RESONANCE = 15;

    /** Image modality MR SPECT. */
    public static final int MAGNETIC_RESONANCE_SPECTROSCOPY = 16;

    /** Image modality nuclear medicine. */
    public static final int NUCLEAR_MEDICINE = 17;

    /** Image modality other. */
    public static final int OTHER = 18;

    /** Image modality PET. */
    public static final int POSITRON_EMISSION_TOMOGRAPHY = 19;

    /** Image modality panoramtic X ray. */
    public static final int PANORAMIC_XRAY = 20;

    /** Image modality radio fluoroscopy. */
    public static final int RADIO_FLUOROSCOPY = 21;

    /** Image modality radiographic imaging. */
    public static final int RADIOGRAPHIC_IMAGING = 22;

    /** Image modality radiotherapy dose. */
    public static final int RADIOTHERAPY_DOSE = 23;

    /** Image modality radiotherapy image. */
    public static final int RADIOTHERAPY_IMAGE = 24;

    /** Image modality radiotherapy plan. */
    public static final int RADIOTHERAPY_PLAN = 25;

    /** Image modality radiotherapy record. */
    public static final int RADIOTHERAPY_RECORD = 26;

    /** Image modality radiotherapy structure set. */
    public static final int RADIOTHERAPY_STRUCTURE_SET = 27;

    /** Image modality slide microscopy. */
    public static final int SLIDE_MICROSCOPY = 28;

    /** Image modality SPECT. */
    public static final int SINGLE_PHOTON_EMISSION_COMPUTED_TOMOGRAPHY = 29;

    /** Image modality thermography. */
    public static final int THERMOGRAPHY = 30;

    /** Image modality ultrasound. */
    public static final int ULTRASOUND = 31;

    /** Image modality X ray angiography. */
    public static final int XRAY_ANGIOGRAPHY = 32;

    /** Image modality external camera photography. */
    public static final int EXTERNAL_CAMERA_PHOTOGRAPHY = 33;

    /** Image modality Red Free. */
    public static final int RED_FREE = 34;

    /** Image modality Fluorescein Angiography. */
    public static final int FA = 35;

    /** Image modality IndoCyanine Green. */
    public static final int ICG = 36;

    /** Array of modality strings -- again, numbering starts at 1, not 0. 
     * @deprecated No longer needed since enum works now*/
    private static final String[] modalityStr = {"Unknown Modality", "Biomagnetic Imaging", "Color Flow Doppler",
            "Computed Radiography", "Computed Tomography", "Duplex Doppler", "Diaphanography", "Digital Radiography",
            "Endoscopy", "General Microscopy", "Hardcopy", "Intraoral Radiography", "Laser Surface Scan",
            "Magnetic Resonance Angiography", "Mammography", "Magnetic Resonance", "Magnetic Resonance Spectroscopy",
            "Nuclear Medicine", "Other", "Positron Emission Tomography", "Panoramic XRay", "Radio Fluoroscopy",
            "Radiographic Imaging", "Radiotherapy Dose", "Radiotherapy Image", "Radiotherapy Plan",
            "Radiotherapy Record", "Radiotherapy Structure Set", "Slide Microscopy",
            "Single Photon Emission Computed Tomography", "Thermography", "Ultrasound", "XRay Angiography",
            "External Camera Photography", "Red Free", "FA", "ICG"};

    public enum AxisOrientation {
    	/** Axis orientation unknown. */
    	ORI_UNKNOWN_TYPE(0, "Unknown"),
    	/** Axis orientation Right to Left. */
    	ORI_R2L_TYPE(1, "Right to Left"),
    	/** Axis orientation Left to Right. */
    	ORI_L2R_TYPE(2, "Left to Right"),
    	/** Axis orientation Posterior to Anterior. */
    	ORI_P2A_TYPE(3, "Posterior to Anterior"),
    	/** Axis orientation Anterior to Posterior. */
    	ORI_A2P_TYPE(4, "Anterior to Posterior"),
    	/** Axis orientation Inferior to Superior. */
    	ORI_I2S_TYPE(5, "Inferior to Superior"),
    	/** Axis orientation Superior to Inferior. */
    	ORI_S2I_TYPE(6, "Superior to Inferior");
    	
    	private int legacyNum;
		private String dispString;

		AxisOrientation(int legacyNum, String dispString) {
    		this.legacyNum = legacyNum;
    		this.dispString = dispString;
    	}

		/* (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		public String toString() {
			return dispString;
		}
		
		public int getLegacyNum() {
			return legacyNum;
		}
    }
    
    /** Axis orientation unknown. */
    public static final int ORI_UNKNOWN_TYPE = 0;

    /** Axis orientation Right to Left. */
    public static final int ORI_R2L_TYPE = 1;

    /** Axis orientation Left to Right. */
    public static final int ORI_L2R_TYPE = 2;

    /** Axis orientation Posterior to Anterior. */
    public static final int ORI_P2A_TYPE = 3;

    /** Axis orientation Anterior to Posterior. */
    public static final int ORI_A2P_TYPE = 4;

    /** Axis orientation Inferior to Superior. */
    public static final int ORI_I2S_TYPE = 5;

    /** Axis orientation Superior to Inferior. */
    public static final int ORI_S2I_TYPE = 6;

    /** Array of axis orientation strings. */
    public static final String[] axisOrientationStr = {"Unknown", "Right to Left", "Left to Right",
            "Posterior to Anterior", "Anterior to Posterior", "Inferior to Superior", "Superior to Inferior"};

    public enum ImageOrientation {
    	AXIAL(0, AxisOrientation.ORI_R2L_TYPE, AxisOrientation.ORI_A2P_TYPE, AxisOrientation.ORI_I2S_TYPE),
    	CORONAL(1, AxisOrientation.ORI_R2L_TYPE, AxisOrientation.ORI_S2I_TYPE, AxisOrientation.ORI_A2P_TYPE),
    	SAGITTAL(2, AxisOrientation.ORI_A2P_TYPE, AxisOrientation.ORI_S2I_TYPE, AxisOrientation.ORI_R2L_TYPE),
    	UNKNOWN(3, AxisOrientation.ORI_UNKNOWN_TYPE, AxisOrientation.ORI_UNKNOWN_TYPE, AxisOrientation.ORI_UNKNOWN_TYPE);
    	
    	private int legacyNum;
    	private AxisOrientation xAxis;
    	private AxisOrientation yAxis;
    	private AxisOrientation zAxis;
    	
    	ImageOrientation(int legacyNum, AxisOrientation xAxis, AxisOrientation yAxis, AxisOrientation zAxis) {
    		this.legacyNum = legacyNum;
    		this.xAxis = xAxis;
    		this.yAxis = yAxis;
    		this.zAxis = zAxis;
    	}
    	
    	public int getLegacyNum() {
    		return legacyNum;
    	}
    	
    	public AxisOrientation getXOrient() {
    		return xAxis;
    	}
    	
    	public AxisOrientation getYOrient() {
    		return yAxis;
    	}
    	
    	public AxisOrientation getZOrient() {
    		return zAxis;
    	}
    }
    
    /** Axial orientation. */
    public static final int AXIAL = 0;

    /** Coronal orientation. */
    public static final int CORONAL = 1;

    /** Sagittal orientation. */
    public static final int SAGITTAL = 2;

    /** Unknown orientation. */
    public static final int UNKNOWN_ORIENT = 3;

    /** Array of image orientation strings. */
    private static final String[] imageOrientationStr = {"Axial", "Coronal", "Sagittal", "Unknown"};

    /** Unknown transform ID. */
    public static final int TRANSFORM_UNKNOWN = 0;

    /** Scanner Anatomical transform ID. */
    public static final int TRANSFORM_SCANNER_ANATOMICAL = 1;

    /** Another Dataset transform ID. */
    public static final int TRANSFORM_ANOTHER_DATASET = 2;

    /** Talairach Tournoux transform ID. */
    public static final int TRANSFORM_TALAIRACH_TOURNOUX = 3;

    /** MNI 152 transform ID. */
    public static final int TRANSFORM_MNI_152 = 4;

    /** Indicates no compression. */
    public static final int COMPRESSION_NONE = 0;

    /** Indicates zip compression of an image. */
    public static final int COMPRESSION_ZIP = 1;

    /** Indicates gzip compression of an image. */
    public static final int COMPRESSION_GZIP = 2;

    /** Indicates bzip2 compression of an image. */
    public static final int COMPRESSION_BZIP2 = 3;

    // ~ Instance fields
    // ------------------------------------------------------------------------------------------------

    /**
     * axis orientation used to support image ordering and display for medical images. We support the right hand rule
     * where the origin is the upper left hand of the image with the positive axis.
     * 
     * <p>
     * x - left to right y - top to botton z - into the screen
     * </p>
     */
    protected int[] axisOrientation = {FileInfoBase.ORI_UNKNOWN_TYPE, FileInfoBase.ORI_UNKNOWN_TYPE,
            FileInfoBase.ORI_UNKNOWN_TYPE};

    /** File name the the image was read from (image extension included - foo.img, foo.dcm ). */
    /** The file name which includes the path information. */
    protected String fileName;

    /** File suffix (ex. "jpg") */
    protected String fileSuffix;

    /** Indicates the image orientation (i.e. Axial, Coronal, ...) */
    protected int imageOrientation = FileInfoBase.UNKNOWN_ORIENT;

    /** Indicates the modality (medical image type) of the dataset. */
    protected int modality = FileInfoBase.UNKNOWN_MODALITY;

    /**
     * The origin to support image locations (ie. DICOM, MINC ...) it is relative to the image origin. the positive axis
     * are right hand rule.
     * 
     * <p>
     * x - left to right y - top to botton z - into the screen
     * </p>
     */
    protected float[] origin = new float[5]; // { 0, 0, 0, 0, 0};

    /** Used to indicate if the raw data was also compression (0 = no, 1 = zip). */
    private int compressionType = 0;

    /** The data type of the data (i.e. byte, short, float ... */
    private int dataType;

    /**
     * Pixel or voxel resolutions for each dimension - default = 1.0. The z-dim resolution should be the spacing between
     * the centers of adjacent slices; sometimes this will match the slice thickness, but not always.
     */
    private float[] dimResolutions = {(float) 1.0, (float) 1.0, (float) 1.0, (float) 1.0, (float) 1.0};

    /**
     * The Endianess of the data. Intel, DEC Alpha ***** LSB first byte LITTLE_ENDIAN (false) Motorola (MAC), SPARC
     * (SUN), SGI IRIX MSB first byte BIG_ENDIAN (true)
     */
    private boolean endianess = FileBase.LITTLE_ENDIAN;

    /**
     * Used for checking for endianess changes in the ModelImage.reallocate(final int type) function.
     */
    private boolean originalEndianess = FileBase.LITTLE_ENDIAN;

    /** Image extents as decribed by the image file format. */
    private int[] extents = new int[5];

    /** File format as defined in the Filebase. */
    private int fileFormat;

    /** Image maximum intensity for single channel image. */
    private double max;

    /** Image maximum intensity for the blue channel of an RGB image. */
    private double maxB;

    /** Image maximum intensity for the green channel of an RGB image. */
    private double maxG;

    /** Image maximum intensity for the red channel of an RGB image. */
    private double maxR;

    /** Image minimum intensity for single channel image. */
    private double min;

    /** Image minimum intensity for the blue channel of an RGB image. */
    private double minB;

    /** Image minimum intensity for the green channel of an RGB image. */
    private double minG;

    /** Image minimum intensity for the red channel of an RGB image. */
    private double minR;

    // 0 indicates 0 is white
    // 2 RGB
    // 3 indexed color LUT is saved with image

    /** Flag that indicates whether or not the image is in multiple files (tiff). */
    private boolean multiFile = false;

    /** Number of bytes to the start the image data - ie. the header length */
    private int offset;

    /** Image minimum intensity for single channel image. */
    private short photometric = 1; // 1 indicates 0 is black

    /** Some file formats have a pad value for pixels outside the acquisition domain. */
    private Short pixelPadValue;

    /** DICOM images have a rescale y-intercept value that we have also kept in the base. */
    private double rescaleIntercept = 0.0;

    /** DICOM images have a rescale slope value that we have also kept in the base. */
    private double rescaleSlope = 1.0;

    /**
     * The thickness of individual slices in the image volume. Stored in dicom tag 0018,0050 and various other places in
     * other file formats.
     */
    private float sliceThickness = 0;

    /** Describes the units of measure for the dataset. */
    protected Unit[] unitsOfMeasure = {Unit.MILLIMETERS, Unit.MILLIMETERS, Unit.MILLIMETERS,
            Unit.SECONDS, Unit.UNKNOWN_MEASURE};


    // ~ Methods
    // --------------------------------------------------------------------------------------------------------

    
    /**
     * Helper method to copy important file info type to another file info type.
     * 
     * @param originalInfo source file info.
     * @param newInfo destination file info.
     */
    public static void copyCoreInfo(final FileInfoBase[] originalInfo, final FileInfoBase[] newInfo) {
        FileInfoBase curInfo;

        for (int i = 0; i < newInfo.length; i++) {

            if (i < originalInfo.length) {
                curInfo = originalInfo[i];
            } else {
                curInfo = originalInfo[0];
            }

            newInfo[i].setAxisOrientation(curInfo.getAxisOrientation());
            newInfo[i].setDataType(curInfo.getDataType());
            newInfo[i].setEndianess(curInfo.getEndianess());
            newInfo[i].setExtents(curInfo.getExtents());
            newInfo[i].setImageOrientation(curInfo.getImageOrientation());

        }
    }

    /**
     * Helper method to copy core information from one fileinfo into another, this method also has a list of fileinfos
     * NOT to copy (used by JDialogRemoveSlices).
     * 
     * @param originalInfo FileInfoBase[] original file infos (longer list)
     * @param newInfo FileInfoBase[] new file infos (shorter list)
     * @param listNoCopy boolean[] boolean array of indices into the original fileinfos that should not be copied
     */
    public static void copyCoreInfo(final FileInfoBase[] originalInfo, final FileInfoBase[] newInfo,
            final boolean[] listNoCopy) {
        FileInfoBase curInfo;

        for (int i = 0, j = 0; i < originalInfo.length; i++) {

            if (i < originalInfo.length) {
                curInfo = originalInfo[i];
            } else {
                curInfo = originalInfo[0];
            }

            if ( !listNoCopy[i % listNoCopy.length]) {
                newInfo[j].setAxisOrientation(curInfo.getAxisOrientation());
                newInfo[j].setDataType(curInfo.getDataType());
                newInfo[j].setEndianess(curInfo.getEndianess());
                newInfo[j].setExtents(curInfo.getExtents());
                newInfo[j].setImageOrientation(curInfo.getImageOrientation());


                newInfo[j].setModality(curInfo.getModality());
                newInfo[j].setOrigin(curInfo.getOrigin());
                newInfo[j].setResolutions(curInfo.getResolutions()); // Added 10/23/2002
                j++; // increment the index into the newInfo
            }
        }
    }

    /**
     * Returns the axis orientation associated with a string.
     * 
     * @param s String to test
     * 
     * @return axis orientation
     */
    public static int getAxisOrientationFromStr(final String s) {

        // look through the array of strings to see if there's a match.
        try {

            for (int i = 0; i < FileInfoBase.axisOrientationStr.length; i++) {

                if (FileInfoBase.getAxisOrientationStr(i).regionMatches(true, 0, s, 0,
                        FileInfoBase.getAxisOrientationStr(i).length())) {
                    return i;
                }
            }
        } catch (final ArrayIndexOutOfBoundsException aie) {
            return FileInfoBase.ORI_UNKNOWN_TYPE;
        }

        return FileInfoBase.ORI_UNKNOWN_TYPE;

    } // end getModalityFromStr()

    /**
     * Return the string associated with an axis orientation.
     * 
     * @param m int representing the axis orientation (see the static definitions)
     * 
     * @return String representing the string associated with the axis orientation.
     */
    public static String getAxisOrientationStr(final int m) {

        try {
            return FileInfoBase.axisOrientationStr[m];
        } catch (final ArrayIndexOutOfBoundsException aie) {}

        return "";

    } // end getAxisOrientationStr()

    /**
     * Returns the image data type associated with a string.
     * 
     * @param s String to test
     * 
     * @return data type
     */
    public static int getDataTypeFromStr(final String s) {

        for (int i = 0; i < DataType.values().length; i++) {

            if ((DataType.values()[i].toString()).regionMatches(true, 0, s, 0, s.length())) {
            
                return DataType.values()[i].getLegacyNum();
            }
        }

        return SHORT; //default
    } // end getDataTypeFromStr()

    /**
     * Returns the endianess associated with a string.
     * 
     * @param s String to test
     * 
     * @return Big endian or little endian
     */
    public static boolean getEndianessFromStr(final String s) {

        if ( (s.indexOf("Big") != -1) || (s.indexOf("big") != -1) || (s.indexOf("BIG") != -1)) {
            return FileBase.BIG_ENDIAN;
        } else if ( (s.indexOf("Little") != -1) || (s.indexOf("little") != -1) || (s.indexOf("LITTLE") != -1)) {
            return FileBase.LITTLE_ENDIAN;
        }

        return FileBase.LITTLE_ENDIAN;
    } // end getEndianessFromStr()

    /**
     * Returns the image orientation associated with a string.
     * 
     * @param s String to test
     * 
     * @return image orientation
     */
    public static int getImageOrientationFromStr(final String s) {

        // look through the array of strings to see if there's a match.
        try {

            for (int i = 0; i < 3; i++) {

                if (FileInfoBase.getImageOrientationStr(i).regionMatches(true, 0, s, 0,
                        FileInfoBase.getImageOrientationStr(i).length())) {
                    return i;
                }
            }
        } catch (final ArrayIndexOutOfBoundsException aie) {
            return FileInfoBase.UNKNOWN_ORIENT;
        }

        return FileInfoBase.UNKNOWN_ORIENT;

    } // end getImageOrientationFromStr()

    /**
     * Return the string associated with an image orientation.
     * 
     * @param m the orientation (see the static definitions)
     * 
     * @return the string associated with the orientation.
     */
    public static String getImageOrientationStr(final int m) {

        try {
            return FileInfoBase.imageOrientationStr[m];
        } catch (final ArrayIndexOutOfBoundsException aie) {}

        return "";

    } // end getImageOrientationStr()

    /**
     * Returns the modality associated with a string.
     * 
     * @param s String to test
     * 
     * @return modality
     */
    public static int getModalityFromStr(final String s) {

        // look through the array of strings to see if there's a match.
        try {

            for (int i = 0; i < FileInfoBase.modalityStr.length; i++) {

                if (FileInfoBase.getModalityStr(i)
                        .regionMatches(true, 0, s, 0, FileInfoBase.getModalityStr(i).length())) {
                    return i;
                }
            }
        } catch (final ArrayIndexOutOfBoundsException aie) {
            return FileInfoBase.UNKNOWN_MODALITY;
        }

        return FileInfoBase.UNKNOWN_MODALITY;

    } // end getModalityFromStr()

    /**
     * Return all the modality strings as an array.
     * 
     * @return String[] - array containing the strings associated with modalities.
     */
    public static String[] getModalityStr() {

        return FileInfoBase.modalityStr;

    } // end getModalityStr()

    /**
     * Return the string associated with a modality.
     * 
     * @param m the modality (see the static definitions)
     * 
     * @return the string associated with the modality.
     */
    public static String getModalityStr(final int m) {

        try {
            return FileInfoBase.modalityStr[m];
        } catch (final ArrayIndexOutOfBoundsException aie) {}

        return "";

    } // end getModalityStr()

    /**
     * Returns the number of bytes per pixel based on the data type.
     * 
     * @param dataType the data type.
     * 
     * @return the number of bytes per pixel.
     * 
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static int getNumOfBytesPerPixel(final int dataType) {

        switch (dataType) {

            case BOOLEAN:
            case BYTE:
            case UBYTE:
                return 1;

            case SHORT:
            case USHORT:
                return 2;

            case INTEGER:
            case UINTEGER:
                return 4;

            case LONG:
                return 8;

            case FLOAT:
                return 4;

            case DOUBLE:
                return 8;

            case ARGB: // 4 * UBYTE(8 bits) = 4 bytes
                return 4;

            case ARGB_USHORT: // 4 * USHORT(16 bits) = 8 bytes
                return 8;

            case ARGB_FLOAT: // 4 * FLOAT(32 bits) = 16 bytes
                return 16;

            default:
                throw new IllegalArgumentException("The data type is illegal argument : " + dataType);
        }
    }

    /**
     * Helper method that returns the opposite axis orientation of the one sent in; that is, R2L for L2R, A2P for P2A,
     * etc.
     * 
     * @param orient DOCUMENT ME!
     * 
     * @return int Opposite image orientation
     */
    public static int oppositeOrient(final int orient) {
        int neworient = -1;

        switch (orient) {

            case FileInfoBase.ORI_A2P_TYPE:
                neworient = FileInfoBase.ORI_P2A_TYPE;
                break;

            case FileInfoBase.ORI_P2A_TYPE:
                neworient = FileInfoBase.ORI_A2P_TYPE;
                break;

            case FileInfoBase.ORI_R2L_TYPE:
                neworient = FileInfoBase.ORI_L2R_TYPE;
                break;

            case FileInfoBase.ORI_L2R_TYPE:
                neworient = FileInfoBase.ORI_R2L_TYPE;
                break;

            case FileInfoBase.ORI_I2S_TYPE:
                neworient = FileInfoBase.ORI_S2I_TYPE;
                break;

            case FileInfoBase.ORI_S2I_TYPE:
                neworient = FileInfoBase.ORI_I2S_TYPE;
                break;
        }

        return neworient;
    }

    /**
     * Helper method to determine if axis A and axis B are the same axis: that is, if both are the patient x-axis, they
     * will be either R2L or L2R.
     * 
     * @param axisA Axis A: one of the defined ORI_ types.
     * @param axisB Axis B: one of the defined ORI_ types.
     * 
     * @return boolean <code>true</code> if axis A and axis B are the same axis
     */
    public static boolean sameAxis(final int axisA, final int axisB) {

        if ( (axisA == FileInfoBase.ORI_R2L_TYPE) || (axisA == FileInfoBase.ORI_L2R_TYPE)) {

            if ( (axisB == FileInfoBase.ORI_R2L_TYPE) || (axisB == FileInfoBase.ORI_L2R_TYPE)) {
                return true;
            } else {
                return false;
            }
        } else if ( (axisA == FileInfoBase.ORI_A2P_TYPE) || (axisA == FileInfoBase.ORI_P2A_TYPE)) {

            if ( (axisB == FileInfoBase.ORI_A2P_TYPE) || (axisB == FileInfoBase.ORI_P2A_TYPE)) {
                return true;
            } else {
                return false;
            }
        } else if ( (axisA == FileInfoBase.ORI_S2I_TYPE) || (axisA == FileInfoBase.ORI_I2S_TYPE)) {

            if ( (axisB == FileInfoBase.ORI_S2I_TYPE) || (axisB == FileInfoBase.ORI_I2S_TYPE)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    

    /**
     * Prepares this class for cleanup.
     */
    public void finalize() {
        axisOrientation = null;
        origin = null;

        unitsOfMeasure = null;
        dimResolutions = null;

        fileName = null;
        fileSuffix = null;

        try {
            super.finalize();
        } catch (final Throwable er) {}
    }

    /* ********************************************************************** */

    /* ****************************** Accessors ***************************** */

    /* ********************************************************************** */
    
    /**
     * Get the direction for accessing each axis of data. This is based on the values in the axisOrientation array.
     * 
     * @return int[] Array of +/-1 values with one entry for each axis. A -1 indicates that the direction is reversed.
     */
    public int[] getAxisDirection() {

        final int[] axisOrient = getAxisOrientation();
        final int[] direction = new int[axisOrient.length];

        for (int i = 0; i < axisOrient.length; i++) {

            if ( (axisOrient[i] == FileInfoBase.ORI_R2L_TYPE) || (axisOrient[i] == FileInfoBase.ORI_A2P_TYPE)
                    || (axisOrient[i] == FileInfoBase.ORI_I2S_TYPE)) {
                direction[i] = 1;
            } else {
                direction[i] = -1;
            }
        }

        return direction;
    }

    /**
     * Returns orientation of each axis.
     * 
     * @return int[] orientation of each axis
     * 
     * @see #setAxisOrientation(int[])
     */
    public int[] getAxisOrientation() {
        return axisOrientation;
    }

    /**
     * Returns orientation of entered axis.
     * 
     * @param axis Axis to get orientation for
     * 
     * @return int orientation of specified axis
     * 
     * @see #setAxisOrientation(int, int)
     */
    public int getAxisOrientation(final int axis) {
        return axisOrientation[axis];
    }

    /**
     * Gets the compression type.
     * 
     * @return the compression type
     */
    public int getCompressionType() {
        return this.compressionType;
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
     * Descibes file endianess.
     * 
     * @return boolean <code>false</code> = litteEndian format <code>true</code> = bigEndian format
     */
    public final boolean getEndianess() {
        return endianess;
    }

    public final boolean getOriginalEndianess() {
    	return originalEndianess;
    }

    /**
     * Returns the dimensionality of the image.
     * 
     * @return int[] units (Inches or millimeters);
     */
    public final int[] getExtents() {
        return extents;
    }

    /**
     * Returns counter to start of image data.
     * 
     * @return String that indicates location of the file
     */
    public final String getFileDirectory() {
        return FileUtility.getFileDirectory(fileName);
    }

    /**
     * Returns file format.
     * 
     * @return int file format (TIFF, raw, Analyze...)
     */
    public final int getFileFormat() {
        return fileFormat;
    }

    /**
     * Returns the file name.
     * 
     * @return String indicating file name
     */
    public final String getFileName() {
        return FileUtility.getFileName(fileName);
    }

    /**
     * Returns the file suffix.
     * 
     * @return String representing the filename suffix
     */
    public final String getFileSuffix() {
        return fileSuffix;
    }

    /**
     * Returns the image orientation.
     * 
     * @return int representing orientation
     */
    public final int getImageOrientation() {
        return imageOrientation;
    }

    /**
     * Return whether or not the image's slices are time based.
     * 
     * @return boolean is 2.5 D
     */
    public final boolean getIs2_5D() {

        if ( (unitsOfMeasure.length > 2) && (unitsOfMeasure[2].getType() == UnitType.TIME)) {
            return true;
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return float[] LPSOrigin
     */
    public float[] getLPSOrigin() {
        final float[] LPSOrigin = new float[3];
        LPSOrigin[0] = origin[0];
        LPSOrigin[1] = origin[1];
        LPSOrigin[2] = origin[2];

        for (int j = 0; j < 3; j++) {

            if ( (getAxisOrientation()[j] == FileInfoBase.ORI_L2R_TYPE)
                    || (getAxisOrientation()[j] == FileInfoBase.ORI_R2L_TYPE)) {
                LPSOrigin[0] = getOrigin()[j];

            } else if ( (getAxisOrientation()[j] == FileInfoBase.ORI_P2A_TYPE)
                    || (getAxisOrientation()[j] == FileInfoBase.ORI_A2P_TYPE)) {
                LPSOrigin[1] = getOrigin()[j];

            } else if ( (getAxisOrientation()[j] == FileInfoBase.ORI_S2I_TYPE)
                    || (getAxisOrientation()[j] == FileInfoBase.ORI_I2S_TYPE)) {
                LPSOrigin[2] = getOrigin()[j];

            }
        }

        return LPSOrigin;
    }

    /**
     * Returns max pixel value of the image.
     * 
     * @return double Returns double max pixel value of the image
     */
    public final double getMax() {
        return max;
    }

    /**
     * Returns max blue pixel value of the image.
     * 
     * @return double Returns double blue max pixel value of the image
     */
    public final double getMaxB() {
        return maxB;
    }

    /**
     * Returns max green pixel value of the image.
     * 
     * @return double Returns double green max pixel value of the image
     */
    public final double getMaxG() {
        return maxG;
    }

    /**
     * Returns max red pixel value of the image.
     * 
     * @return double Returns double red max pixel value of the image
     */
    public final double getMaxR() {
        return maxR;
    }

    /**
     * Returns min pixel value of the image.
     * 
     * @return double Returns double min pixel value of the image
     */
    public final double getMin() {
        return min;
    }

    /**
     * Returns min blue pixel value of the image.
     * 
     * @return double Returns double blue min pixel value of the image
     */
    public final double getMinB() {
        return minB;
    }

    /**
     * Returns min green pixel value of the image.
     * 
     * @return couble Returns double green min pixel value of the image
     */
    public final double getMinG() {
        return minG;
    }

    /**
     * Returns min red pixel value of the image.
     * 
     * @return couble Returns double red min pixel value of the image
     */
    public final double getMinR() {
        return minR;
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
     * Returns whether or not the image is in multiple files (tiff).
     * 
     * @return boolean true indicates multiple files, false o.w.
     */
    public final boolean getMultiFile() {
        return multiFile;
    }

    /**
     * Returns the header offset.
     * 
     * @return int header offset
     */
    public final int getOffset() {
        return offset;
    }

    /**
     * Returns the origin.
     * 
     * @return float[] the origin
     */
    public float[] getOrigin() {
        return origin;
    }

    /**
     * Returns the origin value of the requested axis.
     * 
     * @param axis requested axis; x is 0, y is 1, z is 2
     * 
     * @return float orientation of axis
     */
    public float getOrigin(final int axis) {

        try {
            return origin[axis];
        } catch (final ArrayIndexOutOfBoundsException aiiobe) {
            throw aiiobe;
        } catch (final NullPointerException npe) {
            throw npe;
        }
    }

    /**
     * Gets the origin of a particular slice; resets for the z dimension.
     * 
     * @param slice Z-dimension slice.
     * 
     * @return float[] New start locations
     */
    public float[] getOriginAtSlice(final int slice) {
        final float[] newOrigin = new float[4];

        for (int i = 0; i < Math.min(4, origin.length); i++) {
            newOrigin[i] = origin[i];
        }

        int direction = 1;

        if ( (axisOrientation[2] == FileInfoBase.ORI_L2R_TYPE) || (axisOrientation[2] == FileInfoBase.ORI_P2A_TYPE)
                || (axisOrientation[2] == FileInfoBase.ORI_S2I_TYPE)) {
            direction = -1;
        }

        newOrigin[2] = origin[2] + (direction * dimResolutions[2] * slice);

        return newOrigin;
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
     * Returns pixel pad value.
     * 
     * @return Short Returns pixel pad value
     */
    public final Short getPixelPadValue() {
        return pixelPadValue;
    }

    /**
     * Returns the intercept.
     * 
     * @return double rescale intercept
     */
    public final double getRescaleIntercept() {
        return rescaleIntercept;
    }

    /**
     * Returns the slope.
     * 
     * @return double rescale slope
     */
    public final double getRescaleSlope() {
        return rescaleSlope;
    }

    /**
     * Returns the resolution of the requested dimension.
     * 
     * @param dim The dimension to return the resolution of.
     * 
     * @return The resolution of one of the image dimensions.
     */
    public final float getResolution(final int dim) {
        return dimResolutions[dim];
    }

    /**
     * Returns each dimension's resolution.
     * 
     * @return float[] dimension resolutions
     */
    public final float[] getResolutions() {
        return dimResolutions;
    }

    /**
     * Returns the size of the slice image in byte which represented by this object.
     * 
     * @return the size of the slice image in byte which represented by this object.
     */
    public int getSize() {
        final int[] extents = this.getExtents();

        if ( (extents == null) || (extents.length < 2)) {
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
     * Returns the units of measure. 
     * 
     * @return int[] units (Inches or millimeters);
     */
    public final int[] getUnitsOfMeasure() {
        int[] unitsInt = new int[unitsOfMeasure.length];
        for(int i = 0; i < unitsInt.length; i++) {
            if(unitsOfMeasure[i] != null) {
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
        if ( (unitsOfMeasure != null) && (dim < unitsOfMeasure.length) && (dim >= 0) && (unitsOfMeasure[dim] != null)) {
            return unitsOfMeasure[dim].getLegacyNum();
        } else { //The selected dimension does not exist in the image
            return Unit.UNKNOWN_MEASURE.getLegacyNum();
        }
    }

    /**
     * Returns the volume unit for the data. Assumes all three dimensions are the same units.
     * 
     * @return String associated volume unit of measure.
     */
    public String getVolumeUnitsOfMeasureStr() {
        if(getUnitsOfMeasure(0) != getUnitsOfMeasure(1) || getUnitsOfMeasure(1) != getUnitsOfMeasure(2)) {
            return Unit.getUnitFromLegacyNum(getUnitsOfMeasure(0))+"*"+
                        Unit.getUnitFromLegacyNum(getUnitsOfMeasure(1))+"*"+
                        Unit.getUnitFromLegacyNum(getUnitsOfMeasure(2));
        }
        return Unit.getUnitFromLegacyNum(getUnitsOfMeasure(0)).toString()+"^3";
    }
    
    /**
     * Returns the area unit for the data. Assumes both dimensions are the same units.
     * 
     * @return String associated volume unit of measure.
     */
    public String getAreaUnitsOfMeasureStr() {
        if(getUnitsOfMeasure(0) != getUnitsOfMeasure(1)) {
            return Unit.getUnitFromLegacyNum(getUnitsOfMeasure(0))+"*"+
                            Unit.getUnitFromLegacyNum(getUnitsOfMeasure(1));
        }
        return Unit.getUnitFromLegacyNum(getUnitsOfMeasure(0))+"^2";
    }

    /**
     * isDicomOrdered() returns true if the file is in dicom order, false otherwise.
     * 
     * @return true if the file is in dicom order, false otherwise
     */
    public boolean isDicomOrdered() {

        if ( (axisOrientation[0] == FileInfoBase.ORI_R2L_TYPE) && (axisOrientation[1] == FileInfoBase.ORI_A2P_TYPE)
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

        axisOrientation[0] = axOrient[0];
        axisOrientation[1] = axOrient[1];
        axisOrientation[2] = axOrient[2];
    }

    /**
     * Sets the image orientation in the specified axis. Creates the axisOrientation if the array has not yet been
     * created.
     * 
     * @param axOrient orientation
     * @param axis axis of orientation; x is 0, y is 1, z is 2.
     */
    public void setAxisOrientation(final int axOrient, final int axis) {

        System.out.println("axis orient is " + axOrient);

        if ( (axOrient == FileInfoBase.ORI_UNKNOWN_TYPE) || (axOrient == FileInfoBase.ORI_A2P_TYPE)
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
     * Sets format of image data.
     * 
     * @param type data type defined in ModelStorageBase
     */
    public final void setDataType(final int type) {
        dataType = type;
    }

    /**
     * Describes file endianess.
     * 
     * @param endness endianess of the file format
     */
    public void setEndianess(final boolean endness) {
        endianess = endness;
    }

    public void setOriginalEndianess(final boolean originalEndness) {
    	originalEndianess = originalEndness;
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
     * DOCUMENT ME!
     * 
     * @param directory DOCUMENT ME!
     */
    public final void setFileDirectory(final String directory) {
        if (directory != null) {
            setFileName(directory + FileUtility.getFileName(fileName));
        } else {
            setFileName(FileUtility.getFileName(fileName));
        }
    }

    /**
     * Sets the file format.
     * 
     * @param format File format
     */
    public final void setFileFormat(final int format) {
        fileFormat = format;
    }

    /**
     * Sets the file name.
     * 
     * @param fname image file name
     */
    public void setFileName(final String fname) {

        if ( (fname == null) || (fname.length() == 0)) {
            fileName = FileUtility.getFileDirectory(fileName);
        } else if (FileUtility.getFileDirectory(fname) == null) {

            if (FileUtility.getFileDirectory(fileName) == null) {
                fileName = fname;
            } else {
                fileName = FileUtility.getFileDirectory(fileName) + fname;
            }
        } else {
            fileName = fname;
        }
    }

    /**
     * Sets the file suffix.
     * 
     * @param suffix file suffix
     */
    public final void setFileSuffix(final String suffix) {
        fileSuffix = suffix;
    }

    /**
     * Sets the image orientation.
     * 
     * @param orient Orientation.
     */
    public void setImageOrientation(final int orient) {
        imageOrientation = orient;
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
     * Sets max blue pixel value of image.
     * 
     * @param Max max blue pixel value
     */
    public final void setMaxB(final double Max) {
        maxB = Max;
    }

    /**
     * Sets max green pixel value of image.
     * 
     * @param Max max green pixel value
     */
    public void setMaxG(final double Max) {
        maxG = Max;
    }

    /**
     * Sets max red pixel value of image.
     * 
     * @param Max max red pixel value
     */
    public void setMaxR(final double Max) {
        maxR = Max;
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
     * Sets min blue pixel value of image.
     * 
     * @param Min min blue pixel value
     */
    public final void setMinB(final double Min) {
        minB = Min;
    }

    /**
     * Sets min green pixel value of image.
     * 
     * @param Min min green pixel value
     */
    public final void setMinG(final double Min) {
        minG = Min;
    }

    /**
     * Sets min red pixel value of image.
     * 
     * @param Min min red pixel value
     */
    public final void setMinR(final double Min) {
        minR = Min;
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
     * Sets the flag for multiple files.
     * 
     * @param flag <code>true</code> indicates multiple files for image, <code>false</code> o.w.
     */
    public final void setMultiFile(final boolean flag) {
        multiFile = flag;
    }

    /**
     * Sets the header offset.
     * 
     * @param off the header offset
     */
    public final void setOffset(final int off) {
        offset = off;
    }

    /**
     * Sets the origin.
     * 
     * @param originlocat origin location array
     * 
     * @see #getStartLocations()
     */
    public void setOrigin(final float[] originlocat) {

        origin = originlocat.clone();
    }

    /**
     * Sets the start location in the specified axis. creates the startLocations if the arrray has not yet been created.
     * 
     * @param originCoord start location
     * @param axis axis of orientation; x is 0, y is 1, z is 2
     * 
     * @see #getStartLocations(int)
     */
    public void setOrigin(final float originCoord, final int axis) {

        

        origin[axis] = originCoord;
    }

    /**
     * Sets photometric interpretation.
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
     * </table>
     * 
     * @param value photometric value
     */
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
    public final void setResolutions(final float[] resolutions) {

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
    public final void setResolutions(final float resolution, final int dim) {
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
            for(int i=0; i<unitMeasure.length; i++) {
                localMeasure[i] = Unit.getUnitFromLegacyNum(unitMeasure[i]);
            }
            
            setUnitsOfMeasure(localMeasure);
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

    /**
     * Gives the information contained in FileInfo in a string.
     * 
     * @return String information contained in the FileInfo object
     */
    public String toString() {
        String s = "";

        s += "File info:\n";
        s += "Modality: ";
        s += FileInfoBase.getModalityStr(modality) + "\n";
        s += "\nFile name: " + fileName + "\n";
        s += "File suffix: " + fileSuffix + "\n";
        s += "File format: ";
        s += "Data type: ";
        s += "Offset: " + offset + "\n";
        s += "Endianess: ";

        if (endianess == FileBase.LITTLE_ENDIAN) {
            s += "LITTLE_ENDIAN\n";
        } else {
            s += "BIG_ENDIAN\n";
        }

        s += "Extents: ";

        for (final int element : extents) {
            s += element + " ";
        }

        s += "\nResolutions: ";

        for (final float element : dimResolutions) {
            s += element + " ";
        }

        s += "\nUnits of measure: ";

        for (final Unit element : unitsOfMeasure) {
            s += element + " ";
        }

        s += "\nImage orientation: ";
        s += FileInfoBase.getImageOrientationStr(imageOrientation);

        s += "\nAxis orientations: ";

        for (final int element : axisOrientation) {
            s += FileInfoBase.getAxisOrientationStr(element) + " ";
        }

        s += "\nImage origin locations: ";

        for (final float element : origin) {
            s += element + " ";
        }

        s += "\nMin: " + min + "\nMax: " + max + "\n";

        return s;
    }
    
    /**
     * 
     * @deprecated should now use enum
     * @param i
     * @return
     */
    public static String getUnitsOfMeasureAbbrevStr(int i) {
        Unit measure = Unit.getUnitFromLegacyNum(i);
        if(measure == null) {
            measure = Unit.UNKNOWN_MEASURE;
        }
        
        return measure.getAbbrev();
    }

    /**
     * @deprecated should now use enum
     * @param xUnits
     * @return
     */
    public static String getUnitsOfMeasureStr(int xUnits) {
        Unit measure = Unit.getUnitFromLegacyNum(xUnits);
        if(measure == null) {
            measure = Unit.UNKNOWN_MEASURE;
        }
        
        return measure.toString();
    }

    /**
     * @deprecated should now use enum
     * @param selectedOutput
     * @return
     */
    public static int getUnitsOfMeasureFromStr(String selectedOutput) {
        Unit measure = Unit.getUnit(selectedOutput);
        if(measure == null) {
            measure = Unit.UNKNOWN_MEASURE;
        }
        
        return measure.getLegacyNum();
    }

    /**
     * @deprecated should now use enum
     * @param selectedOutput
     * @return
     */
    public static int[] getAllSameDimUnits(int measure) {
        Unit origUnit = Unit.getUnitFromLegacyNum(measure);
        if(origUnit == null) {
            origUnit = Unit.UNKNOWN_MEASURE;
        }
        
        Unit[] allSame = UnitType.getUnitsOfType(origUnit.getType());
        int[] allUnitNum = new int[allSame.length]; 
        for(int i=0; i<allUnitNum.length; i++) {
            allUnitNum[i] = allSame[i].getLegacyNum();
        }
        return allUnitNum;
    }
    
    /**
     * Return all the abbreviated units of measure strings as an array.
     * 
     * @deprecated should now use enum
     * @return String[] - array containing the abbreviated strings associated with units of measure.
     */
    public static String[] getUnitsOfMeasureAbbrevStr() {

        String[] strAbbrev = new String[Unit.values().length];
        for(int i=0; i<strAbbrev.length; i++) {
            strAbbrev[i] = Unit.values()[i].getAbbrev();
        }
        
        return strAbbrev;

    } // end getUnitsOfMeasureAbbrevStr()
    
    /**
     * Return all the units of measure strings as an array.
     * @deprecated should use enum
     * @return String[] - array containing the strings associated with units of measure.
     */
    public static String[] getUnitsOfMeasureStr() {
        String[] str = new String[Unit.values().length];
        for(int i=0; i<str.length; i++) {
            str[i] = Unit.values()[i].toString();
        }
        
        return str;
    } // end getUnitsOfMeasureStr()
}
