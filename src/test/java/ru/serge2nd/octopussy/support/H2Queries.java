package ru.serge2nd.octopussy.support;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static java.util.Arrays.asList;

public class H2Queries {
    public static final String FIRST_SCALAR = "SELECT CAST(5 AS TINYINT) AS C2";
    public static final String SECOND_SCALAR = "SELECT CAST(7 AS TINYINT) AS C2";
    public static final String TWO_SCALARS = FIRST_SCALAR + "\nUNION\n" + SECOND_SCALAR;
    public static final List<Byte> SCALAR_VALUES = asList((byte)5, (byte)7);

    public static final String FIRST_ROW = "SELECT\n" +
            "  CAST(0 AS BIT) AS C1,\n" +
            "  CAST(-128 AS TINYINT) AS C2,\n" +
            "  CAST(-32768 AS SMALLINT) AS C3,\n" +
            "  CAST(-2147483648 AS INT) AS C4,\n" +
            "  CAST(-9223372036854775808 AS BIGINT) AS C5,\n" +
            "  CAST(-9223372036854775807.65 AS DECIMAL(21, 2)) AS C6,\n" +
            "  CAST(-999.9999 AS REAL) AS C7,\n" +
            "  CAST(-99999999.99999999 AS DOUBLE) AS C8,\n" +
            "  CAST('21:30:16' AS TIME) AS C9,\n" +
            "  CAST('2020-03-22' AS DATE) AS C10,\n" +
            "  CAST('2019-01-09T15:08:00' AS TIMESTAMP) AS C11,\n" +
            "  CAST(X'FEDCBA' AS VARBINARY) AS C12,\n" +
            "  CAST('fedcba' AS VARCHAR) AS C13,\n" +
            "  CAST(X'FEDCBA' AS BLOB) AS C14,\n" +
            "  CAST('fedcba' AS CLOB) AS C15";

    public static final String SECOND_ROW = "SELECT\n" +
            "  CAST(1 AS BIT) AS C1,\n" +
            "  CAST(127 AS TINYINT) AS C2,\n" +
            "  CAST(32767 AS SMALLINT) AS C3,\n" +
            "  CAST(2147483647 AS INT) AS C4,\n" +
            "  CAST(9223372036854775807 AS BIGINT) AS C5,\n" +
            "  CAST(9223372036854775807.65 AS DECIMAL(21, 2)) AS C6,\n" +
            "  CAST(999.9999 AS REAL) AS C7,\n" +
            "  CAST(99999999.99999999 AS DOUBLE) AS C8,\n" +
            "  CAST('15:30:16' AS TIME) AS C9,\n" +
            "  CAST('2018-09-24' AS DATE) AS C10,\n" +
            "  CAST('2019-01-01T01:45:38' AS TIMESTAMP) AS C11,\n" +
            "  CAST(X'ABCDEF' AS VARBINARY) AS C12,\n" +
            "  CAST('abcdef' AS VARCHAR) AS C13,\n" +
            "  CAST(X'ABCDEF' AS BLOB) AS C14,\n" +
            "  CAST('abcdef' AS CLOB) AS C15";

    public static final String TWO_ROWS = FIRST_ROW + "\nUNION\n" + SECOND_ROW;

    public static final List<List<?>> VALUES = asList(
            asList(Boolean.FALSE, (byte)-128, (short)-32768, -2147483648, BigInteger.valueOf(-9223372036854775808L), new BigDecimal("-9223372036854775807.65"),
                    -999.9999f, -99999999.99999999, LocalTime.parse("21:30:16"), LocalDate.parse("2020-03-22"), LocalDateTime.parse("2019-01-09T15:08:00"),
                    new byte[] {-2, -36, -70}, "fedcba", new byte[] {-2, -36, -70}, "fedcba"),
            asList(Boolean.TRUE, (byte)127, (short)32767, 2147483647, BigInteger.valueOf(9223372036854775807L), new BigDecimal("9223372036854775807.65"),
                    999.9999f, 99999999.99999999, LocalTime.parse("15:30:16"), LocalDate.parse("2018-09-24"), LocalDateTime.parse("2019-01-01T01:45:38"),
                    new byte[] {-85, -51, -17}, "abcdef", new byte[] {-85, -51, -17}, "abcdef"));
}
