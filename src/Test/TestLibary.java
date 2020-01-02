/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import RawDCMLibary.DICOM.DICOMFile;
import java.util.Arrays;

/**
 *
 * @author shaesler
 */
public class TestLibary {

    /**
     * @param args the command line arguments
     * @throws java.io.UnsupportedEncodingException
     */
    public static void main(String[] args) throws UnsupportedEncodingException, IOException {

        DICOMFile rf = new DICOMFile("T:\\ALLGEMEI\\Haesler\\temp\\test_1.dcm");
        rf.readHeader();
        int[] temp = rf.getPixelData();
        rf.getHistogram();
        Arrays.sort(temp);
        System.out.println(temp[temp.length-1]);
    }

}
