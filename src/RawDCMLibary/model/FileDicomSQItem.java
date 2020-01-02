package RawDCMLibary.model;

import RawDCMLibary.DICOM.DICOMFile;
import RawDCMLibary.model.enums.Enums.VRtype;



/**
 * A table containing dicom tags. Common tags are not stored here and instead
 * should be stored in the reference tag table.  The reference tag table may refer
 * to another table within a dicom sequence.
 */public class FileDicomSQItem extends FileDicomTagTable {

    /** Whether the sequence should be written using an unknown length */
    private boolean writeAsUnknownLength = false;
    
    public FileDicomSQItem(DICOMFile parent, VRtype vr_type) {        
        super(vr_type);
    }

    public FileDicomSQItem(DICOMFile parent,
            FileDicomTagTable firstSliceTags, VRtype vr_type) {
        super(firstSliceTags, vr_type);
    }

    /**
     * Whether the sequence should be written using an unknown length, can be set as a preference by user.
     */
    public boolean doWriteAsUnknownLength() {
        return writeAsUnknownLength;
    }

    /**
     * Whether the sequence should be written using an unknown length, this includes adding
     * a sequence delimitation item to the sequence.
     */
    public void setWriteAsUnknownLength(boolean writeAsUnknownLength) {
        this.writeAsUnknownLength = writeAsUnknownLength;
    }
    
    /**
     * Gets the length as read in by the header (possibly undefined).
     *
     * @return  The length of the sequence as read in by the header
     */
    public final int getWritableLength(boolean includeTagInfo) {
        if(doWriteAsUnknownLength()) {
            return -1;
        }
        return getDataLength(includeTagInfo);
    }
}
