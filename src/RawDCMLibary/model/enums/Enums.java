/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RawDCMLibary.model.enums;

import java.util.ArrayList;

/**
 *
 * @author shaesler
 */
public class Enums {

    public enum VRtype {
        /**
         * Used to indicate that the DICOM tags are explicit (i.e they have
         * VRs).
         */
        EXPLICIT(false),
        /**
         * Used to indicate that the DICOM tags are implicit (i.e. VRs defined
         * by DICOM dictionary).
         */
        IMPLICIT(true);

        private boolean legacyVal;

        VRtype(boolean legacyVal) {
            this.legacyVal = legacyVal;
        }

        public boolean getLegacyVal() {
            return legacyVal;
        }
    }

    public enum UnitType {
        NONE,
        LENGTH,
        TIME,
        FREQUENCY,
        CONCENTRATION,
        VELOCITY,
        ANGLE;

        public static Unit[] getUnitsOfType(UnitType t) {
            ArrayList<Unit> arUnit = new ArrayList<Unit>();
            for (Unit u : Unit.values()) {
                if (u.getType() == t) {
                    arUnit.add(u);
                }
            }

            return arUnit.toArray(new Unit[arUnit.size()]);
        }

        public Unit[] getUnitsOfType() {
            return getUnitsOfType(this);
        }

        public Unit getBase() {
            switch (this) {
                case NONE:
                    return Unit.UNKNOWN_MEASURE;
                case LENGTH:
                    return Unit.METERS;
                case TIME:
                    return Unit.SECONDS;
                case FREQUENCY:
                    return Unit.HZ;
                case CONCENTRATION:
                    return Unit.PPM;
                case VELOCITY:
                    return Unit.METERS_PER_SEC;
                case ANGLE:
                    return Unit.DEGREES;
            }
            return Unit.UNKNOWN_MEASURE;
        }

    }

    public enum Unit {
        /**
         * Unit of measurement unknown.
         */
        UNKNOWN_MEASURE(1, "Unknown", "unk", UnitType.NONE),
        /**
         * Unit of measurement inches.
         */
        INCHES(2, "Inches", "in", UnitType.LENGTH, 39.3700787),
        /**
         * Units of measurement mil (thousandth of an inch)
         */
        MILS(3, "Mils", "mils", UnitType.LENGTH, 3.93700787E4),
        /**
         * Unit of measurement centimeters.
         */
        CENTIMETERS(4, "Centimeters", "cm", UnitType.LENGTH, 1.0E2),
        /**
         * Unit of measurement angstroms.
         */
        ANGSTROMS(5, "Angstroms", "A", UnitType.LENGTH, 1.0E10),
        /**
         * Unit of measurement nanometers.
         */
        NANOMETERS(6, "Nanometers", "nm", UnitType.LENGTH, 1.0E9),
        /**
         * Unit of measurement micrometers.
         */
        MICROMETERS(7, "Micrometers", "um", UnitType.LENGTH, 1.0E6),
        /**
         * Unit of measurement millimeters.
         */
        MILLIMETERS(8, "Millimeters", "mm", UnitType.LENGTH, 1.0E3),
        /**
         * Unit of measurement meters.
         */
        METERS(9, "Meters", "m", UnitType.LENGTH, 1),
        /**
         * Unit of measurement kilometers.
         */
        KILOMETERS(10, "Kilometers", "km", UnitType.LENGTH, 1.0E-3),
        /**
         * Unit of measurement miles.
         */
        MILES(11, "Miles", "mi", UnitType.LENGTH, 6.21371192E-4),
        /**
         * Unit of measurement nanoseconds.
         */
        NANOSEC(12, "Nanoseconds", "nsec", UnitType.TIME, 1.0E9),
        /**
         * Unit of measurement microseconds.
         */
        MICROSEC(13, "Microseconds", "usec", UnitType.TIME, 1.0E6),
        /**
         * Unit of measurement milliseconds.
         */
        MILLISEC(14, "Milliseconds", "msec", UnitType.TIME, 1.0E3),
        /**
         * Unit of measurement seconds.
         */
        SECONDS(15, "Seconds", "sec", UnitType.TIME, 1),
        /**
         * Unit of measurement minutes.
         */
        MINUTES(16, "Minutes", "min", UnitType.TIME, 1.66666667E-2),
        /**
         * Unit of measurement hours.
         */
        HOURS(17, "Hours", "hr", UnitType.TIME, 2.77777778E-4),
        /**
         * Unit of measurement hertz.
         */
        HZ(18, "Hertz", "hz", UnitType.FREQUENCY, 0.159154943274),
        /**
         * Unit of measurement part-per-million.
         */
        PPM(19, "Part_Per_Million", "ppm", UnitType.CONCENTRATION),
        /**
         * Radians per second.
         */
        RADS(20, "Radians_Per_Second", "rads", UnitType.FREQUENCY, 6.2831853),
        /**
         * Degrees
         */
        DEGREES(21, "Degrees", "deg", UnitType.ANGLE),
        /**
         * Meters per second
         */
        METERS_PER_SEC(22, "Meters_Per_Second", "m/s", UnitType.VELOCITY);

        @SuppressWarnings("serial")
        public class UnsupportedUnitConversion extends Exception {

            public UnsupportedUnitConversion(String string) {
                super(string);
            }

        }

        private int legacyNum;
        private String str;
        private String abbrev;
        private UnitType type;
        private double convFactor;

        Unit(int legacyNum, String str, String abbrev, UnitType type) {
            this.legacyNum = legacyNum;
            this.str = str;
            this.abbrev = abbrev;
            this.type = type;

        }

        Unit(int legacyNum, String str, String abbrev, UnitType type, double convFactor) {
            this(legacyNum, str, abbrev, type);

            this.convFactor = convFactor;
        }

        public String toString() {
            return str;
        }

        public String getAbbrev() {
            return abbrev;
        }

        public UnitType getType() {
            return type;
        }

        public double getConvFactor() {
            return convFactor;
        }

        public int getLegacyNum() {
            return legacyNum;
        }

        /**
         * Method converts the <code>origValue</code> quantity from the current
         * units to the <code>resultUnit</code>.
         *
         * @param origValue A value in units of <code>this</code>
         * @param resultUnit The units to convert to
         * @return The converted quantity
         */
        public double convertTo(double origValue, Unit resultUnit) {
            return origValue * getConversionFactor(resultUnit);
        }

        /**
         * Method converts the <code>origValue</code> that is currently in units
         * of <code>origUnit</code> to the units specified by <code>this</code>
         * unit.
         *
         * @param origValue A value in units of <code>origUnit</code>
         * @param origUnit The current units of the quantity specified
         * @return The quantity converted into the current units
         */
        public double convertFrom(double origValue, Unit origUnit) {
            return origValue * origUnit.getConversionFactor(this);
        }

        public double getConversionFactor(Unit resultUnit) {
            if (type != resultUnit.type) {
                System.err.println("Cannot convert from " + str + " to " + resultUnit);
            }

            return (1 / convFactor) * resultUnit.convFactor;
        }

        public static Unit getUnit(String str) {
            for (Unit u : Unit.values()) {
                if (u.str.equals(str)) {
                    return u;
                }
            }

            return Unit.UNKNOWN_MEASURE;
        }

        public static Unit getUnitFromAbbrev(String abbrev) {
            for (Unit u : Unit.values()) {
                if (u.abbrev.equals(abbrev)) {
                    return u;
                }
            }

            return Unit.UNKNOWN_MEASURE;
        }

        public static Unit getUnitFromLegacyNum(int legacyNum) {
            for (Unit u : Unit.values()) {
                if (u.getLegacyNum() == legacyNum) {
                    return u;
                }
            }

            return Unit.UNKNOWN_MEASURE;
        }

    }
        public enum DataType {
        /**
         * Data buffer is of type Boolean (1 bit per voxel).
         */
        BOOLEAN(0, "Boolean", 0, 1),
        /**
         * Data buffer is of type signed byte (8 bits per voxel).
         */
        BYTE(1, "Byte", Byte.MIN_VALUE, Byte.MAX_VALUE),
        /**
         * Data buffer is of type unsigned byte (8 bits per voxel).
         */
        UBYTE(2, "Unsigned Byte", 0, 255),
        /**
         * Data buffer is of type signed short (16 bits per voxel).
         */
        SHORT(3, "Short", Short.MIN_VALUE, Short.MAX_VALUE),
        /**
         * Data buffer is of type unsigned short (16 bits per voxal).
         */
        USHORT(4, "Unsigned Short", 0, 65535),
        /**
         * Data buffer is of type signed integer (32 bits per voxel).
         */
        INTEGER(5, "Integer", Integer.MIN_VALUE, Integer.MAX_VALUE),
        /**
         * Data buffer is of type signed long integer (64 bits per voxel).
         */
        LONG(6, "Long", Long.MIN_VALUE, Long.MAX_VALUE),
        /**
         * Data buffer is of type float (32 bits per voxel).
         */
        FLOAT(7, "Float", -Float.MAX_VALUE, Float.MAX_VALUE),
        /**
         * Data buffer is of type double (64 bits per voxel).
         */
        DOUBLE(8, "Double", -Double.MAX_VALUE, Double.MAX_VALUE),
        /**
         * Data buffer is of type ARGB where each channel (A = alpha, R = red, G
         * = green, B = blue) is represented by a unsigned byte value. (4 *
         * UBYTE(8 bits) = 4 bytes)
         */
        ARGB(9, "ARGB", UBYTE.typeMin, UBYTE.typeMax),
        /**
         * Data buffer is of type ARGB where each channel (A = alpha, R = red, G
         * = green, B = blue) is represented by a unsigned short value. (4 *
         * USHORT(16 bits) = 8 bytes)
         */
        ARGB_USHORT(10, "ARGB Ushort", USHORT.typeMin, USHORT.typeMax),
        /**
         * Data buffer is of type ARGB where each channel (A = alpha, R = red, G
         * = green, B = blue) is represented by a float value. (4 * FLOAT(32
         * bits) = 16 bytes)
         */
        ARGB_FLOAT(11, "ARGB Float", -Float.MAX_VALUE, Float.MAX_VALUE),
        /**
         * Data buffer is of type complex type floats (2 x 64 bits per voxel).
         */
        COMPLEX(12, "Complex", -Float.MAX_VALUE, Float.MAX_VALUE),
        /**
         * Data buffer is of type complex type of doubles (2 x 128 bits per
         * voxel).
         */
        DCOMPLEX(13, "Complex Double", -Double.MAX_VALUE, Double.MAX_VALUE),
        /**
         * Data buffer is of type unsigned integer (32 bits per voxel).
         */
        UINTEGER(14, "Unsigned Integer", 0, 4294967295L);

        /**
         * These variables preserve the legacy ordering of these enums.
         */
        private int legacyNum;
        private String legacyName;
        /**
         * Min value allowed of the type
         */
        private Number typeMin;
        /**
         * Max value allowed of the type
         */
        private Number typeMax;

        DataType(int legacyNum, String legacyName, Number typeMin, Number typeMax) {
            this.legacyNum = legacyNum;
            this.legacyName = legacyName;
            this.typeMin = typeMin;
            this.typeMax = typeMax;
        }

        public int getLegacyNum() {
            return legacyNum;
        }

        /**
         * This method returns the min value allowed of the type.
         *
         * @return A java.lang.Number
         */
        public Number getTypeMin() {
            return typeMin;
        }

        /**
         * This method return the max value allowed of the type.
         *
         * @return A java.lang.Number
         */
        public Number getTypeMax() {
            return typeMax;
        }

        public String toString() {
            return legacyName;
        }

        public static DataType getDataType(int legacyNum) {
            for (DataType d : DataType.values()) {
                if (d.legacyNum == legacyNum) {
                    return d;
                }
            }
            return null;
        }
    }
}
