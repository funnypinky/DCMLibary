package RawDCMLibary.model;

import java.io.*;


/**
 * Constants and static methods which relate to file input, output or processing.
 */
public class FileUtility {

    /** New File Types should be added to the bottom and not inserted somewhere in the middle. */

    /** Ill defined file type. */
    public static final int ERROR = -1;

    /** Undefined file type. */
    public static final int UNDEFINED = 0;

    /** AFNI file type. extension: .head, .brik */
    public static final int AFNI = 1;

    /** Analyze format (Mayo). extension: .img, .hdr */
    public static final int ANALYZE = 2;

    /** Multiple files of type analyze. */
    public static final int ANALYZE_MULTIFILE = 3;

    /** AVI file type. Windows Media. extension: .avi */
    public static final int AVI = 4;

    /** Used by the Bio-Rad Pic format. extension: .pic && fileID(54L)==12345 */
    public static final int BIORAD = 5;

    /** extension: .bmp. */
    public static final int BMP = 6;
    
    /** Bmp multifile */
    public static final int BMP_MULTIFILE = 7;

    /**
     * Bruker file format. Reads a BRUKER file by first reading in the d3proc header file, second the reco header file,
     * third the acqp file int the same directory or up one or two two parent directories, and finally the 2dseq binary
     * file.
     */
    public static final int BRUKER = 8;

    /**
     * Cheshire file type (a kind of Analyze). extension: .imc Can also have .img extension
     */
    public static final int CHESHIRE = 9;

    /** Cheshire overlay file type. Contains VOIs. extension: .oly */
    public static final int CHESHIRE_OVERLAY = 10;

    /**
     * Used by FreeSurfer software. extension: -.info or -.info~ for header file -.nnn for slice data file where nnn is
     * the slice number
     */
    public static final int COR = 11;

    /** extension: .cur. */
    public static final int CUR = 12;

    /** extension: .dib. */
    public static final int DIB = 13;

    /** Digital Imaging and COmmunications in Medicine file type. Fully implemented versions 2 & 3. extension: .dcm */
    public static final int DICOM = 14;

    /** Gatan's Digital Micrograph version 3 file format. extension: .dm3 */
    public static final int DM3 = 15;

    /** FITS file type. extension: .fits */
    public static final int FITS = 16;

    /** GE Genesis 5X and LX. extension: .sig */
    public static final int GE_GENESIS = 17;

    /** Multiple files of type GE_GENESIS */
    public static final int GE_GENESIS_MULTIFILE = 18;

    /** GE Signa 4.x. */
    public static final int GE_SIGNA4X = 19;

    /** Multiple files of type GE_SIGNA4X */
    public static final int GE_SIGNA4X_MULTIFILE = 20;

    /** extension: .gif. */
    public static final int GIF = 21;

    /** extension: .ico. */
    public static final int ICO = 22;

    /** Image Cytometry Standard. extension: .ics, .ids */
    public static final int ICS = 23;

    /** Interfile file format used in Nuclear Medicine. extension: .hdr */
    public static final int INTERFILE = 24;
    
    /** Multiple files of type INTERFILE */
    public static final int INTERFILE_MULTIFILE = 25;

    /** Java Image Manangement Interface file type. */
    public static final int JIMI = 26;

    /** extension: .jpeg, .jpg. */
    public static final int JPEG = 27;

    /** Used by the Zeiss LSM 510 Dataserver. extension: .lsm */
    public static final int LSM = 28;

    /** Used by the Zeiss LSM 510 Dataserver. */
    public static final int LSM_MULTIFILE = 29;

    /** Siemens MAGNETOM VISION. extension: .ima */
    public static final int MAGNETOM_VISION = 30;

    /** Multiple files of type MAGNETOM_VISION */
    public static final int MAGNETOM_VISION_MULTIFILE = 31;

    /** Benes Trus special file type. extension: .map */
    public static final int MAP = 32;

    /** extension: .bin. */
    public static final int MEDIVISION = 33;

    /** MGH/MGZ volume format. */
    public static final int MGH = 34;

    /** Micro CT format for small animal imaging. extension: .log, .ct */
    public static final int MICRO_CAT = 35;

    /**
     * MINC file type. MINC is a medical imaging oriented extension of the NetCDF file format. NetCDF stands for
     * 'Network Common Data Form'. extension: .mnc
     */
    public static final int MINC = 36;
    
    /** Multiple files of type MINC. */
    public static final int MINC_MULTIFILE = 37;

    /** Not presently implemented. */
    public static final int MIPAV = 38;

    /** extension: .mrc. */
    public static final int MRC = 39;

    /** NIFTI format. extension: .img, .hdr, .nii */
    public static final int NIFTI = 40;

    /** NIFTI multi-file format. */
    public static final int NIFTI_MULTIFILE = 41;

    /** Nearly raw raster data. */
    public static final int NRRD = 42;
    
    /** Nearly raw raster data. */
    public static final int NRRD_MULTIFILE = 43;

    /** Washington University OSM dataset structure. extension: .wu */
    public static final int OSM = 44;

    /** extension: .pcx. */
    public static final int PCX = 45;

    /** extension: .pic. */
    public static final int PIC = 46;

    /** extension: .pict. */
    public static final int PICT = 47;

    /** extension: .png. */
    public static final int PNG = 48;

    /** extension: .psd. */
    public static final int PSD = 49;

    /** Quicktime file type. extension: .mov, .qt */
    public static final int QT = 50;

    /** RAW image data, no header. extension: .raw */
    public static final int RAW = 51;

    /** RAW MULTIFLE image data, no header. */
    public static final int RAW_MULTIFILE = 52;

    /**
     * SPM file format. SPM99 and SPM2 are slight variants of analyze with the same .img, .hdr file extensions. The user
     * could also change the extension .img to .spm to indicate SPM. The header extension would remain .hdr
     */
    public static final int SPM = 53;

    /** MetaMorph Stack (STK) file type. extension: .stk */
    public static final int STK = 54;

    /** MIPAV Surface XML file format. extension: .xml */
    public static final int SURFACE_XML = 55;

    /** extension: .tga. */
    public static final int TGA = 56;

    /** TIFF file; tagged header. extension: .tif, .tiff */
    public static final int TIFF = 57;

    /** Multiple files of TIFF images. */
    public static final int TIFF_MULTIFILE = 58;

    /** Optical coherence tomography. extension: .tmg */
    public static final int TMG = 59;

    /** VOI file, used to read VOIs. extension: .voi */
    public static final int VOI_FILE = 60;

    /** extension: .xbm. */
    public static final int XBM = 61;

    /** MIPAV XML file format. mipav xml image format. extension: .xml */
    public static final int XML = 62;

    /** MIPAV XML file format. */
    public static final int XML_MULTIFILE = 63;

    /** extension: .xpm. */
    public static final int XPM = 64;

    /** extension: "par","parv2","rec","frec". */
    public static final int PARREC = 65;
    
    /** extension: "par","parv2","rec","frec". */
    public static final int PARREC_MULTIFILE = 66;
    
    /** SPAR file format for use with PARREC images */
    public static final int SPAR = 67;

    /** MIPAV Surface XML file format. extension: .xml */
    public static final int SURFACEREF_XML = 68;

    /** MINC 2.0 (HDF5) */
    public static final int MINC_HDF = 69;

    /** Improvision OpenLab LIFF .liff */
    /** Do not confuse with Leica image file format .lif */
    public static final int LIFF = 70;

    /** Extension: .hdr for header, .bfloat for data */
    public static final int BFLOAT = 71;

    /** Extension: .hdr for header, .img for data */
    public static final int SIEMENSTEXT = 72;

    /** Zeiss ZVI has extension .zvi */
    public static final int ZVI = 73;

    public static final int JP2 = 74;
    
    /** extension .mat */
    public static final int MATLAB = 75;
    
    /** Vista file extension .v */
    public static final int VISTA = 76;

    
    /** Metaimage files are either
     *  separate .mhd header and .raw image data files or
     *  combined .mha header and image data file
     */
    public static final int METAIMAGE = 77;
    
    

    private static final String[] fileTypeStr = {"error", "undefined", "afni", "analyze", "analyze multifile", "avi",
            "biorad", "bmp", "bmp multifile", "bruker", "cheshire", "cheshire overlay", "cor", "cur", "dib", "dicom", "dm3", "fits",
            "GE genesis", "GE genisis multifile", "GE signa4x", "GE Signa4x multifile", "gif", "ico", "ics",
            "interfile", "interfile multifile", "jimi", "jpeg", "lsm", "lsm multifile", "magnetom vision", "Megnatom vision multifile", "map",
            "medivision", "mgh", "micro cat", "minc", "minc multifile", "mipav", "mrc", "nifti", "nifti multifile", "nrrd",
            "nrrd multifile", "osm", "pcx", "pic", "pict", "png", "psd", "qt", "raw", "raw multifile", "spm", "stk", "surface xml",
            "tga", "tiff", "tiff multifile", "tmg", "voi file", "xbm", "xml", "xml multifile", "xpm", "parrec", "parrec multifile",
            "spar", "surfaceref xml", "minc hdf", "liff", "bfloat", "siemens text", "zvi", "jp2", "mat", "v", "MetaImage"};

    

    /**
     * Returns the file type associated with a string.
     * 
     * @param s String to test
     * 
     * @return axis orientation
     */
    public static int getFileTypeFromStr(final String s) {

        // look through the array of strings to see if there's a match.
        try {

            for (int i = 0; i < FileUtility.fileTypeStr.length; i++) {

                if (FileUtility.getFileTypeStr(i).regionMatches(true, 0, s, 0, FileUtility.getFileTypeStr(i).length())) {
                    // because fileType indicies start at -1, must decrement
                    return i - 1;
                }
            }
        } catch (final ArrayIndexOutOfBoundsException aie) {
            return FileUtility.ERROR;
        }

        return FileUtility.ERROR;

    } // end getFileTypeFromStr()

    /**
     * Return the string associated with a file type.
     * 
     * @param m int representing the file type (see the above static definitions)
     * 
     * @return String representing the string associated with the file type.
     */
    public static String getFileTypeStr(final int m) {

        try {
            // because fileType indicies start at -1, must increment
            return FileUtility.fileTypeStr[m + 1];
        } catch (final ArrayIndexOutOfBoundsException aie) {}

        return "";

    } // end getFileTypeStr()

    /**
     * Only for FreeSurfer COR volume files Looks in the image directory and returns all images with the same root up to
     * the hyphen, sorted in lexicographical order. Will set the number of images (<code>nImages</code>) for the
     * calling program.
     * 
     * @param fileDir Directory to look for images.
     * @param fileName File name of the image.
     * @param quiet Whether to avoid displaying errors using the GUI.
     * 
     * @return An array of the image names to be read in or saved as.
     * 
     * @throws OutOfMemoryError If there is a problem allocating required memory.
     */
    public static final String[] getCORFileList(final String fileDir, final String fileName, boolean quiet)
            throws OutOfMemoryError {
        int i;
        int j = 0;
        int k;
        int result = 0;
        String[] fileList;
        String[] fileList2;
        String[] fileListBuffer;
        String fileTemp;
        File imageDir;
        String fileName2;
        String suffix2;
        boolean okNumber;
        int nImages;
        String fileName2Trimmed;

        imageDir = new File(fileDir);

        // Read directory and find no. of images
        fileListBuffer = imageDir.list();
        fileList = new String[fileListBuffer.length];

        final String subName = FileUtility.trimCOR(fileName); // subName = name without indexing numbers at end

        for (i = 0; i < fileListBuffer.length; i++) {
            fileName2 = fileListBuffer[i].trim();
            suffix2 = FileUtility.getCORSuffixFrom(fileName2);
            okNumber = true;

            for (k = 1; k < suffix2.length(); k++) {

                if ( !Character.isDigit(suffix2.charAt(k))) {

                    // modified to use Java.lang version 20 July 2004/parsonsd
                    // if ( suffix2.charAt( k ) < '0' || suffix2.charAt( k ) > '9' ) {
                    okNumber = false;
                }
            } // for (k = 0; k < suffix2.length(); k++)

            if (okNumber) {
                fileName2Trimmed = FileUtility.trimCOR(fileName2);
                if (fileName2Trimmed != null) {
                    if (fileName2Trimmed.equals(subName)) {
                        fileList[j] = fileListBuffer[i];
                        j++;
                    }
                }
            } // if (okNumber)
        } // for (i = 0; i < fileListBuffer.length; i++)

        // Number of images is index of last image read into fileList
        nImages = j;

        if (nImages == 0) {

            if ( !quiet) {
                System.err.println("FileIO: No COR images with that base name: " + subName);
            }
            System.err.println("FileIO: No COR images with that base name: " + subName + "\n");

            return null;
        }

        fileList2 = new String[nImages];

        for (i = 0; i < nImages; i++) {
            fileList2[i] = fileList[i];
        }

        // sort to ensure that files are in correct (lexicographical) order
        for (i = 0; i < nImages; i++) { // (bubble sort? ... )

            for (j = i + 1; j < nImages; j++) {
                result = fileList2[i].compareTo(fileList2[j]);

                if (result > 0) {
                    fileTemp = fileList2[i];
                    fileList2[i] = fileList2[j];
                    fileList2[j] = fileTemp;
                } // if (result > 0)
            } // for (j = i+1; j < nImages; j++)
        } // for (i = 0; i < nImages; i++)

        return fileList2;
    }

    /**
     * Only used for COR volume files with hyphen in name Breaks the filename into basename and suffix, then returns the
     * suffix.
     * 
     * @param fn The filename.
     * 
     * @return The suffix or file-extension. For example,
     *         <q>-info</q>. Note that suffix includes the separator '-'
     */
    public static final String getCORSuffixFrom(final String fn) {
        int s;
        String sfx = "";

        if (fn != null) {
            s = fn.lastIndexOf("-");

            if (s != -1) {
                sfx = fn.substring(s);
            }
        }

        return sfx.toLowerCase();
    }

    /**
     * Returns the extension of the file name, if file name does not have extension, then return empty string.
     * 
     * @param absolutePath the file name.
     * 
     * @return The file's extension.
     */
    public static final String getExtension(final String absolutePath) {

        if ( (absolutePath == null) || (absolutePath.length() == 0)) {
            return "";
        }

        final int index = absolutePath.lastIndexOf(".");

        if (index >= 0) {
            return absolutePath.substring(index);
        }

        return "";
    }

    /**
     * Returns the path information from the file name with the path information.
     * 
     * @param fileName the file name wiht the path information.
     * 
     * @return The path information.
     */
    public static final String getFileDirectory(final String fileName) {

        if ( (fileName == null) || (fileName.length() == 0)) {
            return null;
        }

        final int index = fileName.lastIndexOf(File.separator);

        if (index >= 0) {
            return fileName.substring(0, index + 1);
        }

        return null;
    }

    /**
     * Trims off the file extension and file name, but leaves the file index. An index might be 0001, or 140, for
     * example.
     * 
     * @param fName String file name to get index
     * 
     * @return String (index string)
     */
    public static final int getFileIndex(final String fName) {
        int i;

        // char ch;
        final int length = fName.lastIndexOf("."); // Start before suffix.

        for (i = length - 1; i > -1; i--) {

            if ( !Character.isDigit(fName.charAt(i))) {
                break;
            }
        }

        if (i <= -1) {
            return -1;
        }

        return (Integer.parseInt(fName.substring( (i + 1), length)));
    }

    /**
     * Looks in the image directory and returns all images with the same suffix as <code>fileName</code>, sorted in
     * lexicographical order.
     * 
     * @param fileDir Directory to look for images.
     * @param fileName File name of the image.
     * @param quiet Whether to avoid displaying errors using the GUI.
     * 
     * @return An array of the image names to be read in or saved as.
     * 
     * @throws OutOfMemoryError If there is a problem allocating required memory.
     */


    /**
     * Returns the file name without path information from file name with the path information.
     * 
     * @param absolutePath the file name with the path information.
     * 
     * @return The file name without path information.
     */
    public static final String getFileName(final String absolutePath) {

        if ( (absolutePath == null) || (absolutePath.length() == 0)) {
            return null;
        }

        final int index = absolutePath.lastIndexOf(File.separator);

        if (index >= 0) {

            if (index == (absolutePath.length() - 1)) {
                return null;
            }

            return absolutePath.substring(index + 1);
        }

        return absolutePath;
    }

    /**
     * Helper method to strip the image name of the extension, so when we save we don't have double extensions (like
     * genormcor.img.tif).
     * 
     * @param fileName Original name.
     * 
     * @return Name without extension, or original name if there was no extension.
     */
    public static final String stripExtension(final String fileName) {
        final int index = fileName.lastIndexOf(".");

        if (index != -1) {
            return fileName.substring(0, index);
        } else {
            return fileName;
        }
    }

    /**
     * Trims the numbers or file extension from COR file names. Any numbers or
     * <q>.info</q>
     * or
     * <q>.info~</q>
     * will be removed from after a hyphen in the given fname.
     * 
     * @param fName File name where the last characters are alpha-numerics indicating the image number or .info or
     *            .info~
     * 
     * @return File name without numbers on the end.
     */
    public static final String trimCOR(final String fName) {
        final int length = fName.lastIndexOf("-");

        if (length >= 0) {
            return (new String(fName.substring(0, length + 1)));
        } else {
            return null;
        }
    }

    /**
     * Trims the numbers and special character from the file name. Numerics and some special characters <code>[ - _
     * .</code>
     * are removed from the end of the file.
     * 
     * @param fName File name where the last characters are alpha-numerics indicating the image number.
     * 
     * @return File name without numbers on the end.
     */
    public static final String trimNumbersAndSpecial(final String fName) {
        int i;
        char ch;
        int length = fName.lastIndexOf("."); // Start before suffix.
        if (length == -1) {
            length = fName.length();
        }

        for (i = length - 1; i > -1; i--) {
            ch = fName.charAt(i);

            if ( !Character.isDigit(ch) && (ch != '-') && (ch != '.') && (ch != '_')) {
                break;
            }
        }

        String tmpStr;

        tmpStr = fName.substring(0, i + 1);

        boolean aCharIsPresent = false;

        // Determine if at least one letter is present
        for (i = 0; i < tmpStr.length(); i++) {
            ch = tmpStr.charAt(i);

            if (Character.isLetter(ch)) {
                aCharIsPresent = true;

                break;
            }
        }

        // If yes, then remove remaining numbers
        if (aCharIsPresent) {

            for (i = 0; i < tmpStr.length(); i++) {
                ch = tmpStr.charAt(i);

                if (Character.isDigit(ch)) {
                    tmpStr = tmpStr.substring(0, i) + tmpStr.substring(i + 1);
                    i = -1;
                }
            }
        }

        return (tmpStr);
    }

}
